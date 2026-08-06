#!/usr/bin/env python3
"""Fail-closed validator for the Nightmare Spell modpack manifest shell."""

from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any

ALLOWED_COMPONENT_ROLES = {"execution_provider", "presentation_provider", "infrastructure", "content"}
ALLOWED_SIDES = {"client", "server", "both"}
ALLOWED_SOURCES = {"modrinth", "curseforge", "direct"}
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

    components = require_list(root.get("components"), "components")
    seen_ids: set[str] = set()
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
        if source.get("type") not in ALLOWED_SOURCES:
            raise ManifestError(f"{path}.source.type is unsupported")
        require_text(source.get("project"), f"{path}.source.project")
        require_text(source.get("file"), f"{path}.source.file")
        digest = require_text(source.get("sha256"), f"{path}.source.sha256").lower()
        if len(digest) != SHA256_LENGTH or any(ch not in "0123456789abcdef" for ch in digest):
            raise ManifestError(f"{path}.source.sha256 must be a 64-character hexadecimal digest")
        require_text(component.get("license"), f"{path}.license")
        require_text(component.get("removal_behavior"), f"{path}.removal_behavior")

    packaging = require_object(root.get("packaging"), "packaging")
    if packaging.get("format") != "directory-shell-v1":
        raise ManifestError("packaging.format must equal directory-shell-v1")
    include = require_list(packaging.get("include"), "packaging.include")
    normalized = [require_text(value, f"packaging.include[{index}]") for index, value in enumerate(include)]
    if normalized != sorted(set(normalized)):
        raise ManifestError("packaging.include must be unique and lexicographically sorted")
    for required_path in ("README.md", "manifest.json"):
        if required_path not in normalized:
            raise ManifestError(f"packaging.include must contain {required_path}")


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
