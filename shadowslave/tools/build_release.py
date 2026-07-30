#!/usr/bin/env python3
"""Build the public Shadow Slave datapack ZIP.

The archive deliberately contains only Minecraft-consumed pack content:
`pack.mcmeta`, optional `pack.png`, and `data/**`. Development tools, tests and
repository docs stay outside the release artifact.

By default the static validator runs first. The Mineflayer harness still needs
to be run separately against the real 1.21.1 test server before release.
"""

from __future__ import annotations

import hashlib
import json
import re
import subprocess
import sys
import zipfile
from pathlib import Path

PACK = Path(__file__).resolve().parent.parent
REPO = PACK.parent
VALIDATOR = PACK / "tools" / "validate.py"


def pack_version() -> str:
    meta = json.loads((PACK / "pack.mcmeta").read_text(encoding="utf-8"))
    description = meta.get("pack", {}).get("description", "")
    match = re.search(r"v(\d+\.\d+\.\d+)", description if isinstance(description, str) else "")
    if not match:
        raise SystemExit("pack.mcmeta description carries no vX.Y.Z version")
    return match.group(1)


def release_files() -> list[Path]:
    files = [PACK / "pack.mcmeta"]
    pack_icon = PACK / "pack.png"
    if pack_icon.is_file():
        files.append(pack_icon)
    files.extend(path for path in (PACK / "data").rglob("*") if path.is_file())
    return sorted(files, key=lambda path: path.relative_to(PACK).as_posix())


def build_zip(destination: Path) -> None:
    # Normalized timestamps make two builds from identical source byte-for-byte stable.
    epoch = (1980, 1, 1, 0, 0, 0)
    with zipfile.ZipFile(destination, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for source in release_files():
            relative = source.relative_to(PACK).as_posix()
            info = zipfile.ZipInfo(relative, date_time=epoch)
            info.compress_type = zipfile.ZIP_DEFLATED
            info.external_attr = 0o644 << 16
            archive.writestr(info, source.read_bytes(), compresslevel=9)


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def main() -> int:
    subprocess.run([sys.executable, str(VALIDATOR)], cwd=REPO, check=True)

    version = pack_version()
    destination = REPO / f"shadowslave-v{version}.zip"
    build_zip(destination)

    print(f"Built {destination.name}")
    print(f"SHA-256 {sha256(destination)}")
    print("Live release gate still required: cd testserver && node harness.mjs")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
