#!/usr/bin/env python3
"""Build a deterministic Nightmare Spell modpack archive."""

from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import zipfile

try:
    from modpack.tools.validate_manifest import load_and_validate
except ModuleNotFoundError:
    from validate_manifest import load_and_validate

FIXED_ZIP_TIME = (1980, 1, 1, 0, 0, 0)
FILE_MODE = 0o100644 << 16
PROVENANCE_PATH = "provenance.json"


class PackageError(ValueError):
    pass


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def safe_archive_path(value: str, field: str) -> str:
    path = PurePosixPath(value)
    if path.is_absolute() or ".." in path.parts or not path.parts:
        raise PackageError(f"{field} must be a safe relative archive path")
    normalized = path.as_posix()
    if normalized in {"", "."}:
        raise PackageError(f"{field} must be a safe relative archive path")
    return normalized


def read_include(modpack_root: Path, relative_path: str) -> bytes:
    checked = safe_archive_path(relative_path, "packaging.include")
    root = modpack_root.resolve()
    source = (root / checked).resolve()
    try:
        source.relative_to(root)
    except ValueError as error:
        raise PackageError(f"packaging include escapes modpack root: {checked}") from error
    if not source.is_file():
        raise PackageError(f"packaging include does not exist: {checked}")
    return source.read_bytes()


def zip_info(path: str) -> zipfile.ZipInfo:
    info = zipfile.ZipInfo(path, FIXED_ZIP_TIME)
    info.compress_type = zipfile.ZIP_DEFLATED
    info.create_system = 3
    info.external_attr = FILE_MODE
    return info


def component_target(component: dict[str, object]) -> str:
    source = component["source"]
    assert isinstance(source, dict)
    file_name = str(source["file"])
    if PurePosixPath(file_name).name != file_name or not file_name.endswith(".jar"):
        raise PackageError(f"component {component['id']} source.file must be a JAR filename")
    return safe_archive_path(f"mods/{file_name}", f"component {component['id']} package path")


def build_package(
    manifest_path: Path,
    core_jar: Path,
    output_path: Path,
    component_jars: dict[str, Path] | None = None,
) -> str:
    load_and_validate(manifest_path)
    with manifest_path.open("r", encoding="utf-8") as handle:
        manifest = json.load(handle)

    if not core_jar.is_file():
        raise PackageError(f"core JAR does not exist: {core_jar}")

    supplied_components = component_jars or {}
    declared_components = {component["id"]: component for component in manifest["components"]}
    unknown_components = sorted(set(supplied_components) - set(declared_components))
    if unknown_components:
        raise PackageError(f"component JAR supplied for undeclared component(s): {unknown_components}")

    modpack_root = manifest_path.resolve().parent
    packaging = manifest["packaging"]
    core_target = safe_archive_path(
        manifest["canonical_state_owner"]["package_path"],
        "canonical_state_owner.package_path",
    )

    entries: dict[str, bytes] = {}
    for relative_path in packaging["include"]:
        archive_path = safe_archive_path(relative_path, "packaging.include")
        entries[archive_path] = read_include(modpack_root, relative_path)
    entries[core_target] = core_jar.read_bytes()

    for component_id, component in declared_components.items():
        local_path = supplied_components.get(component_id)
        if local_path is None:
            if component["required"]:
                raise PackageError(f"required component JAR was not supplied: {component_id}")
            continue
        if not local_path.is_file():
            raise PackageError(f"component JAR does not exist for {component_id}: {local_path}")
        data = local_path.read_bytes()
        expected_digest = component["source"]["sha256"].lower()
        actual_digest = sha256_bytes(data)
        if actual_digest != expected_digest:
            raise PackageError(
                f"component SHA-256 mismatch for {component_id}: expected {expected_digest}, got {actual_digest}"
            )
        target = component_target(component)
        if target in entries:
            raise PackageError(f"component package path collides with another entry: {target}")
        entries[target] = data

    if PROVENANCE_PATH in entries:
        raise PackageError(f"{PROVENANCE_PATH} is reserved for generated provenance")

    provenance = {
        "schema_version": 1,
        "pack_id": manifest["pack"]["id"],
        "pack_version": manifest["pack"]["version"],
        "entries": [
            {
                "path": path,
                "sha256": sha256_bytes(data),
                "size": len(data),
            }
            for path, data in sorted(entries.items())
        ],
    }
    entries[PROVENANCE_PATH] = (
        json.dumps(provenance, indent=2, sort_keys=True, ensure_ascii=True) + "\n"
    ).encode("utf-8")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    temporary = output_path.with_name(output_path.name + ".tmp")
    try:
        with zipfile.ZipFile(
            temporary,
            mode="w",
            compression=zipfile.ZIP_DEFLATED,
            compresslevel=9,
            strict_timestamps=True,
        ) as archive:
            for path, data in sorted(entries.items()):
                archive.writestr(zip_info(path), data)
        os.replace(temporary, output_path)
    finally:
        temporary.unlink(missing_ok=True)

    return sha256_bytes(output_path.read_bytes())


def parse_component_jars(values: list[str]) -> dict[str, Path]:
    parsed: dict[str, Path] = {}
    for value in values:
        component_id, separator, raw_path = value.partition("=")
        if not separator or not component_id.strip() or not raw_path.strip():
            raise PackageError("--component-jar must use COMPONENT_ID=PATH")
        component_id = component_id.strip()
        if component_id in parsed:
            raise PackageError(f"duplicate --component-jar for {component_id}")
        parsed[component_id] = Path(raw_path)
    return parsed


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--manifest", default="modpack/manifest.json")
    parser.add_argument("--core-jar", required=True)
    parser.add_argument(
        "--component-jar",
        action="append",
        default=[],
        metavar="COMPONENT_ID=PATH",
        help="Pinned runtime component JAR; repeat for multiple components",
    )
    parser.add_argument("--output", required=True)
    args = parser.parse_args()

    try:
        component_jars = parse_component_jars(args.component_jar)
        digest = build_package(
            Path(args.manifest),
            Path(args.core_jar),
            Path(args.output),
            component_jars,
        )
    except (OSError, json.JSONDecodeError, PackageError, ValueError) as error:
        print(f"ERROR: {error}")
        return 1

    print(f"OK: {args.output}")
    print(f"SHA-256: {digest}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
