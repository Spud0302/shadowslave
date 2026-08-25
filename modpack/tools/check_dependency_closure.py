#!/usr/bin/env python3
"""Fail closed when the modpack manifest omits a dependency the mod requires.

The mod declares its hard dependencies in neoforge.mods.toml. The pack declares
what it ships in manifest.json. Nothing previously compared the two, and they
drifted: shadowslave requires combat_core at an exact version while the manifest
listed only GeckoLib and SmartBrainLib. A `required` dependency that is absent
makes NeoForge refuse to load the mod, so the assembled pack could not boot --
and modpack CI did not notice, because it packaged a fixture and never launched
the archive.

    python modpack/tools/check_dependency_closure.py

Exit codes: 0 the manifest covers every required dependency, 1 it does not,
2 a file could not be read or parsed.

This checks *declared* closure only. It proves the manifest names everything the
mod asks for; it does not prove the JARs resolve, assemble, or boot. Those need
the pack to actually be built and launched.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

DEFAULT_MODS_TOML = "mod/src/main/templates/META-INF/neoforge.mods.toml"
DEFAULT_MANIFEST = "modpack/manifest.json"

# Satisfied by the pack's loader and Minecraft version rather than by a shipped
# component, so they are closure-relevant but not component-relevant.
PLATFORM_MOD_IDS = {"neoforge", "minecraft", "forge", "fml"}

DEPENDENCY_BLOCK = re.compile(r"\[\[dependencies\.[^\]]+\]\]")
FIELD = re.compile(r'^\s*(\w+)\s*=\s*"([^"]*)"\s*$')


class ClosureError(Exception):
    pass


def parse_required_dependencies(toml_text):
    """Return [(mod_id, version_range)] for every type="required" dependency.

    Deliberately a narrow parser rather than a TOML library: this file is a
    Gradle *template* containing ${...} placeholders, which is not valid TOML
    until expanded. Only the fixed modId/type fields are read, and the version
    range is passed through verbatim.
    """
    dependencies = []
    current = None
    for line in toml_text.splitlines():
        if DEPENDENCY_BLOCK.match(line.strip()):
            if current is not None:
                dependencies.append(current)
            current = {}
            continue
        if current is None:
            continue
        if line.strip().startswith("[") and not DEPENDENCY_BLOCK.match(line.strip()):
            dependencies.append(current)
            current = None
            continue
        match = FIELD.match(line)
        if match:
            current[match.group(1)] = match.group(2)
    if current is not None:
        dependencies.append(current)

    return [(d["modId"], d.get("versionRange", ""))
            for d in dependencies
            if d.get("type") == "required" and d.get("modId")]


def manifest_mod_ids(manifest):
    covered = {}
    owner = manifest.get("canonical_state_owner") or {}
    if owner.get("mod_id"):
        covered[owner["mod_id"]] = "canonical_state_owner"
    for component in manifest.get("components") or []:
        if component.get("mod_id"):
            covered[component["mod_id"]] = "component %s" % component.get("id", "?")
    return covered


def check(mods_toml, manifest_path):
    try:
        toml_text = Path(mods_toml).read_text(encoding="utf-8")
    except OSError as exc:
        raise ClosureError("cannot read %s: %s" % (mods_toml, exc))
    try:
        manifest = json.loads(Path(manifest_path).read_text(encoding="utf-8"))
    except (OSError, ValueError) as exc:
        raise ClosureError("cannot read %s: %s" % (manifest_path, exc))

    required = parse_required_dependencies(toml_text)
    if not required:
        raise ClosureError("no required dependencies found in %s; the parser or the "
                           "file format has changed" % mods_toml)

    covered = manifest_mod_ids(manifest)
    missing, satisfied = [], []
    for mod_id, version_range in required:
        if mod_id in PLATFORM_MOD_IDS:
            satisfied.append((mod_id, version_range, "pack platform"))
        elif mod_id in covered:
            satisfied.append((mod_id, version_range, covered[mod_id]))
        else:
            missing.append((mod_id, version_range))
    return required, satisfied, missing


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--mods-toml", default=DEFAULT_MODS_TOML)
    parser.add_argument("--manifest", default=DEFAULT_MANIFEST)
    parser.add_argument("--quiet", action="store_true")
    args = parser.parse_args(argv)

    try:
        required, satisfied, missing = check(args.mods_toml, args.manifest)
    except ClosureError as exc:
        print("ERROR: %s" % exc, file=sys.stderr)
        return 2

    if not args.quiet:
        for mod_id, version_range, by in satisfied:
            print("  ok      %-16s %-20s <- %s" % (mod_id, version_range, by))
        for mod_id, version_range in missing:
            print("  MISSING %-16s %-20s <- nothing in the manifest"
                  % (mod_id, version_range))

    if missing:
        print("\nERROR: %d of %d required dependencies are not shipped by the pack: %s"
              % (len(missing), len(required), ", ".join(m for m, _ in missing)),
              file=sys.stderr)
        print("A required dependency that is absent makes NeoForge refuse to load the "
              "mod, so the assembled pack cannot boot.", file=sys.stderr)
        return 1

    if not args.quiet:
        print("\nOK: all %d required dependencies are covered." % len(required))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
