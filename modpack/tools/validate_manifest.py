#!/usr/bin/env python3
"""Fail-closed validator for the Nightmare Spell modpack manifest."""

from __future__ import annotations

import argparse
import json
from pathlib import PurePosixPath, Path
from typing import Any

ALLOWED_COMPONENT_ROLES = {"execution_provider", "presentation_provider", "infrastructure", "content"}
ALLOWED_SIDES = {"client", "server", "both"}
# A pinned digest exists to verify a third-party download. A first-party JAR
# built from this repository has no digest until it is built, so it declares an
# artifact glob and a package path instead, and provenance records the digest
# that actually shipped.
LOCAL_SOURCE = "local_gradle_build"
REMOTE_SOURCES = {"modrinth", "curseforge", "direct"}
ALLOWED_SOURCES = REMOTE_SOURCES | {LOCAL_SOURCE}
SHA256_LENGTH = 64


class ManifestError(ValueError):
    pass


def require_object(value: Any, path: str) -> dict[str, Any]:
    if not isinstance(value, dict):
        raise ManifestError(f"{path} must be an object")
    return value


def require_list(value: Any, path: str) -> list[Any]:
    if not isinstance(value, list):
        raise ManifestError(f"{path} must be an array")
    return value


def require_text(value: Any, path: str) -> str:
    if not isinstance(value, str) or not value.strip():
        raise ManifestError(f"{path} must be non-blank text")
    return value.strip()


def require_safe_relative_path(value: Any, path: str) -> str:
    text = require_text(value, path)
    candidate = PurePosixPath(text)
    if candidate.is_absolute() or ".." in candidate.parts or candidate.as_posix() in {"", "."}:
        raise ManifestError(f"{path} must be a safe relative path")
    if "\\" in text:
        raise ManifestError(f"{path} must use forward slashes")
    return candidate.as_posix()


def validate_manifest(data: Any) -> None:
    root = require_object(data, "manifest")
    if root.get("schema_version") != 1:
        raise ManifestError("schema_version must equal 1")

    pack = require_object(root.get("pack"), "pack")
    for field in ("id", "name", "version", "minecraft_version"):
        require_text(pack.get(field), f"pack.{field}")
    if pack.get("java_major") != 21:
        raise ManifestError("pack.java_major must equal 21")

    loader = require_object(pack.get("loader"), "pack.loader")
    if loader.get("type") != "neoforge":
        raise ManifestError("pack.loader.type must equal neoforge")
    require_text(loader.get("version"), "pack.loader.version")

    owner = require_object(root.get("canonical_state_owner"), "canonical_state_owner")
    require_text(owner.get("component_id"), "canonical_state_owner.component_id")
    if require_text(owner.get("mod_id"), "canonical_state_owner.mod_id") != "shadowslave":
        raise ManifestError("canonical_state_owner.mod_id must equal shadowslave")
    if owner.get("source") != "local_gradle_build":
        raise ManifestError("canonical_state_owner.source must equal local_gradle_build")
    require_text(owner.get("artifact_glob"), "canonical_state_owner.artifact_glob")
    owner_package_path = require_safe_relative_path(
        owner.get("package_path"), "canonical_state_owner.package_path"
    )
    if not owner_package_path.startswith("mods/") or not owner_package_path.endswith(".jar"):
        raise ManifestError("canonical_state_owner.package_path must be a JAR under mods/")

    components = require_list(root.get("components"), "components")
    seen_ids: set[str] = set()
    seen_package_paths: set[str] = set()
    for index, raw_component in enumerate(components):
        path = f"components[{index}]"
        component = require_object(raw_component, path)
        component_id = require_text(component.get("id"), f"{path}.id")
        if component_id in seen_ids:
            raise ManifestError(f"duplicate component id: {component_id}")
        seen_ids.add(component_id)
        if component_id == owner["component_id"] or component.get("mod_id") == owner["mod_id"]:
            raise ManifestError("canonical state owner must not be duplicated in components")

        require_text(component.get("mod_id"), f"{path}.mod_id")
        require_text(component.get("version"), f"{path}.version")
        if component.get("role") not in ALLOWED_COMPONENT_ROLES:
            raise ManifestError(f"{path}.role is unsupported")
        if component.get("side") not in ALLOWED_SIDES:
            raise ManifestError(f"{path}.side is unsupported")
        if not isinstance(component.get("required"), bool):
            raise ManifestError(f"{path}.required must be boolean")
        if component.get("owns_canonical_state") is not False:
            raise ManifestError(f"{path}.owns_canonical_state must be false")

        source = require_object(component.get("source"), f"{path}.source")
        source_type = source.get("type")
        if source_type not in ALLOWED_SOURCES:
            raise ManifestError(f"{path}.source.type is unsupported")
        require_text(source.get("project"), f"{path}.source.project")

        if source_type == LOCAL_SOURCE:
            require_text(source.get("artifact_glob"), f"{path}.source.artifact_glob")
            local_package_path = require_safe_relative_path(
                source.get("package_path"), f"{path}.source.package_path"
            )
            if not local_package_path.startswith("mods/") or not local_package_path.endswith(".jar"):
                raise ManifestError(f"{path}.source.package_path must be a JAR under mods/")
            if local_package_path == owner_package_path:
                raise ManifestError(
                    f"{path}.source.package_path collides with the canonical state owner"
                )
            if local_package_path in seen_package_paths:
                raise ManifestError(f"duplicate component package path: {local_package_path}")
            seen_package_paths.add(local_package_path)
            # Reject a pinned digest rather than ignoring it: a digest that is
            # never checked reads as a guarantee this build cannot make.
            for unsupported in ("file", "sha256"):
                if source.get(unsupported) is not None:
                    raise ManifestError(
                        f"{path}.source.{unsupported} is not valid for a {LOCAL_SOURCE} component"
                    )
        else:
            require_text(source.get("file"), f"{path}.source.file")
            digest = require_text(source.get("sha256"), f"{path}.source.sha256").lower()
            if len(digest) != SHA256_LENGTH or any(ch not in "0123456789abcdef" for ch in digest):
                raise ManifestError(f"{path}.source.sha256 must be a 64-character hexadecimal digest")

        require_text(component.get("license"), f"{path}.license")
        require_text(component.get("removal_behavior"), f"{path}.removal_behavior")

    packaging = require_object(root.get("packaging"), "packaging")
    if packaging.get("format") != "deterministic-zip-v1":
        raise ManifestError("packaging.format must equal deterministic-zip-v1")
    include = require_list(packaging.get("include"), "packaging.include")
    normalized = [
        require_safe_relative_path(value, f"packaging.include[{index}]")
        for index, value in enumerate(include)
    ]
    if normalized != sorted(set(normalized)):
        raise ManifestError("packaging.include must be unique and lexicographically sorted")
    for required_path in ("README.md", "manifest.json"):
        if required_path not in normalized:
            raise ManifestError(f"packaging.include must contain {required_path}")
    if owner_package_path in normalized or "provenance.json" in normalized:
        raise ManifestError("packaging.include collides with a generated package path")

    output_name = require_safe_relative_path(packaging.get("output_name"), "packaging.output_name")
    if "/" in output_name or not output_name.endswith(".zip"):
        raise ManifestError("packaging.output_name must be a ZIP filename")


def load_and_validate(path: Path) -> None:
    with path.open("r", encoding="utf-8") as handle:
        validate_manifest(json.load(handle))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("manifest", nargs="?", default="modpack/manifest.json")
    args = parser.parse_args()
    try:
        load_and_validate(Path(args.manifest))
    except (OSError, json.JSONDecodeError, ManifestError) as error:
        print(f"ERROR: {error}")
        return 1
    print(f"OK: {args.manifest}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
