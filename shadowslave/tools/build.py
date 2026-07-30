#!/usr/bin/env python3
"""Validate and build the installable Shadow Slave datapack ZIP.

The release archive contains the datapack *contents* at its root:

    pack.mcmeta
    data/...

not a wrapper ``shadowslave/`` directory. Minecraft silently ignores a pack
with the wrong archive root, so packaging is treated as part of correctness.

The archive is deterministic for identical source bytes: paths are sorted and
ZIP timestamps/permissions are normalized instead of inheriting local mtimes.
"""
from __future__ import annotations

import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path

PACK = Path(__file__).resolve().parent.parent
REPO = PACK.parent
VALIDATOR = Path(__file__).with_name("validate.py")
VERSION_RE = re.compile(r"\bShadow Slave v(\d+\.\d+\.\d+)\b")
FIXED_ZIP_TIME = (1980, 1, 1, 0, 0, 0)


def read_version() -> str:
    """Read the release version from the pack description.

    Version stamping belongs to Claude at merge time. The builder deliberately
    derives its filename from pack.mcmeta so there is no fourth version literal
    for humans or agents to keep synchronized.
    """
    meta_path = PACK / "pack.mcmeta"
    try:
        meta = json.loads(meta_path.read_text(encoding="utf-8"))
        description = meta["pack"]["description"]
    except (OSError, json.JSONDecodeError, KeyError, TypeError) as exc:
        raise RuntimeError(f"cannot read version from {meta_path}: {exc}") from exc

    if not isinstance(description, str):
        raise RuntimeError("pack.mcmeta pack.description must be a string")

    match = VERSION_RE.search(description)
    if not match:
        raise RuntimeError(
            "pack.mcmeta description must contain 'Shadow Slave vX.Y.Z' so the release filename "
            "can be derived without another version source"
        )
    return match.group(1)


def source_files() -> list[Path]:
    """Return exactly the files that belong in the player-facing datapack."""
    files = [PACK / "pack.mcmeta"]
    pack_icon = PACK / "pack.png"
    if pack_icon.is_file():
        files.append(pack_icon)

    data_dir = PACK / "data"
    if not data_dir.is_dir():
        raise RuntimeError(f"missing datapack data directory: {data_dir}")
    files.extend(path for path in data_dir.rglob("*") if path.is_file())
    return sorted(files, key=lambda path: path.relative_to(PACK).as_posix())


def validate() -> None:
    """Run the same static validator required by the collaboration protocol."""
    result = subprocess.run([sys.executable, str(VALIDATOR)], cwd=REPO, check=False)
    if result.returncode != 0:
        raise RuntimeError("validator failed; refusing to build a release archive")


def write_member(archive: zipfile.ZipFile, path: Path) -> None:
    """Write one normalized member so identical sources produce identical ZIP bytes."""
    relative = path.relative_to(PACK).as_posix()
    info = zipfile.ZipInfo(relative, FIXED_ZIP_TIME)
    info.compress_type = zipfile.ZIP_DEFLATED
    # Regular file, owner-readable/writable and world-readable (0644).
    info.external_attr = 0o100644 << 16
    archive.writestr(info, path.read_bytes(), compress_type=zipfile.ZIP_DEFLATED, compresslevel=9)


def build() -> Path:
    validate()
    version = read_version()
    output = REPO / f"dist-shadowslave-{version}.zip"

    try:
        with zipfile.ZipFile(output, "w") as archive:
            for path in source_files():
                write_member(archive, path)
    except Exception:
        output.unlink(missing_ok=True)
        raise

    # Packaging regression guard: the manifest must be at archive root and the
    # development tools must not leak into the player-facing artifact.
    with zipfile.ZipFile(output) as archive:
        names = archive.namelist()
    if "pack.mcmeta" not in names:
        output.unlink(missing_ok=True)
        raise RuntimeError("built archive is invalid: pack.mcmeta is not at ZIP root")
    if any(name.startswith("shadowslave/") for name in names):
        output.unlink(missing_ok=True)
        raise RuntimeError("built archive has an extra shadowslave/ wrapper directory")
    if any(name.startswith("tools/") for name in names):
        output.unlink(missing_ok=True)
        raise RuntimeError("development tools leaked into the player-facing archive")

    return output


def main() -> int:
    try:
        output = build()
    except RuntimeError as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        return 1
    print(f"Built {output.relative_to(REPO)}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
