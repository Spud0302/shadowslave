#!/usr/bin/env python3
"""Verify a deterministic Nightmare Spell modpack archive and its provenance."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path, PurePosixPath
from typing import Any
import zipfile

try:
    from modpack.tools.build_package import FIXED_ZIP_TIME, FILE_MODE, PROVENANCE_PATH
except ModuleNotFoundError:
    from build_package import FIXED_ZIP_TIME, FILE_MODE, PROVENANCE_PATH


class VerificationError(ValueError):
    pass


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def require_object(value: Any, path: str) -> dict[str, Any]:
    if not isinstance(value, dict):
        raise VerificationError(f"{path} must be an object")
    return value


def require_list(value: Any, path: str) -> list[Any]:
    if not isinstance(value, list):
        raise VerificationError(f"{path} must be an array")
    return value


def require_text(value: Any, path: str) -> str:
    if not isinstance(value, str) or not value.strip():
        raise VerificationError(f"{path} must be non-blank text")
    return value


def require_non_negative_int(value: Any, path: str) -> int:
    if not isinstance(value, int) or isinstance(value, bool) or value < 0:
        raise VerificationError(f"{path} must be a non-negative integer")
    return value


def require_sha256(value: Any, path: str) -> str:
    digest = require_text(value, path).lower()
    if len(digest) != 64 or any(character not in "0123456789abcdef" for character in digest):
        raise VerificationError(f"{path} must be a 64-character hexadecimal digest")
    return digest


def validate_entry_name(name: str) -> None:
    path = PurePosixPath(name)
    if (
        not name
        or name.endswith("/")
        or path.is_absolute()
        or ".." in path.parts
        or "\\" in name
        or path.as_posix() != name
    ):
        raise VerificationError(f"unsafe or non-canonical archive entry path: {name!r}")


def parse_provenance(data: bytes) -> dict[str, Any]:
    try:
        parsed = json.loads(data.decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError) as error:
        raise VerificationError("provenance.json must be valid UTF-8 JSON") from error

    provenance = require_object(parsed, "provenance")
    if provenance.get("schema_version") != 1:
        raise VerificationError("provenance.schema_version must equal 1")
    require_text(provenance.get("pack_id"), "provenance.pack_id")
    require_text(provenance.get("pack_version"), "provenance.pack_version")
    entries = require_list(provenance.get("entries"), "provenance.entries")

    recorded: dict[str, dict[str, Any]] = {}
    ordered_paths: list[str] = []
    for index, raw_entry in enumerate(entries):
        path = f"provenance.entries[{index}]"
        entry = require_object(raw_entry, path)
        entry_path = require_text(entry.get("path"), f"{path}.path")
        validate_entry_name(entry_path)
        if entry_path == PROVENANCE_PATH:
            raise VerificationError("provenance must not hash itself")
        if entry_path in recorded:
            raise VerificationError(f"duplicate provenance entry: {entry_path}")
        ordered_paths.append(entry_path)
        recorded[entry_path] = {
            "sha256": require_sha256(entry.get("sha256"), f"{path}.sha256"),
            "size": require_non_negative_int(entry.get("size"), f"{path}.size"),
        }

    if ordered_paths != sorted(ordered_paths):
        raise VerificationError("provenance.entries must be lexicographically sorted")
    return {"root": provenance, "entries": recorded}


def verify_package(archive_path: Path) -> str:
    if not archive_path.is_file():
        raise VerificationError(f"archive does not exist: {archive_path}")

    try:
        with zipfile.ZipFile(archive_path) as archive:
            infos = archive.infolist()
            names = [info.filename for info in infos]
            if len(names) != len(set(names)):
                raise VerificationError("archive contains duplicate entry names")
            if names != sorted(names):
                raise VerificationError("archive entries must be lexicographically sorted")
            if PROVENANCE_PATH not in names:
                raise VerificationError(f"archive is missing {PROVENANCE_PATH}")

            for info in infos:
                validate_entry_name(info.filename)
                if info.date_time != FIXED_ZIP_TIME:
                    raise VerificationError(f"archive entry has non-deterministic timestamp: {info.filename}")
                if info.create_system != 3:
                    raise VerificationError(f"archive entry has unsupported creator system: {info.filename}")
                if info.external_attr != FILE_MODE:
                    raise VerificationError(f"archive entry has non-deterministic permissions: {info.filename}")
                if info.compress_type != zipfile.ZIP_DEFLATED:
                    raise VerificationError(f"archive entry uses unsupported compression: {info.filename}")
                if info.flag_bits & 0x1:
                    raise VerificationError(f"archive entry is encrypted: {info.filename}")

            provenance_data = archive.read(PROVENANCE_PATH)
            parsed = parse_provenance(provenance_data)
            recorded = parsed["entries"]
            actual_payload_names = [name for name in names if name != PROVENANCE_PATH]
            if list(recorded) != actual_payload_names:
                missing = sorted(set(actual_payload_names) - set(recorded))
                extra = sorted(set(recorded) - set(actual_payload_names))
                raise VerificationError(
                    f"provenance entry set does not match archive payload; missing={missing}, extra={extra}"
                )

            for name in actual_payload_names:
                data = archive.read(name)
                expected = recorded[name]
                if len(data) != expected["size"]:
                    raise VerificationError(f"size mismatch for {name}")
                if sha256_bytes(data) != expected["sha256"]:
                    raise VerificationError(f"SHA-256 mismatch for {name}")
    except zipfile.BadZipFile as error:
        raise VerificationError("archive is not a valid ZIP file") from error

    return sha256_bytes(archive_path.read_bytes())


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("archive")
    args = parser.parse_args()

    try:
        digest = verify_package(Path(args.archive))
    except (OSError, VerificationError) as error:
        print(f"ERROR: {error}")
        return 1

    print(f"OK: {args.archive}")
    print(f"SHA-256: {digest}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
