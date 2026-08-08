from __future__ import annotations

import hashlib
import json
import tempfile
import unittest
from pathlib import Path
import zipfile

from modpack.tools.build_package import FIXED_ZIP_TIME, PackageError, build_package


ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / "modpack" / "manifest.json"


class DeterministicPackageTest(unittest.TestCase):
    def test_same_inputs_produce_identical_archive_and_digest(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            core = root / "core.jar"
            core.write_bytes(b"deterministic-core-fixture")
            first = root / "first.zip"
            second = root / "second.zip"

            first_digest = build_package(MANIFEST, core, first)
            second_digest = build_package(MANIFEST, core, second)

            self.assertEqual(first.read_bytes(), second.read_bytes())
            self.assertEqual(first_digest, second_digest)
            self.assertEqual(hashlib.sha256(first.read_bytes()).hexdigest(), first_digest)

    def test_archive_has_sorted_fixed_metadata_and_verified_provenance(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            core = root / "core.jar"
            core.write_bytes(b"core-content")
            output = root / "pack.zip"
            build_package(MANIFEST, core, output)

            with zipfile.ZipFile(output) as archive:
                names = archive.namelist()
                self.assertEqual(sorted(names), names)
                self.assertEqual(
                    ["README.md", "manifest.json", "mods/shadowslave-core.jar", "provenance.json"],
                    names,
                )
                for info in archive.infolist():
                    self.assertEqual(FIXED_ZIP_TIME, info.date_time)
                    self.assertEqual(0o100644, info.external_attr >> 16)

                provenance = json.loads(archive.read("provenance.json"))
                self.assertEqual("shadowslave-nightmare-spell", provenance["pack_id"])
                self.assertEqual("0.0.0-dev", provenance["pack_version"])
                recorded = {entry["path"]: entry for entry in provenance["entries"]}
                self.assertNotIn("provenance.json", recorded)
                for path in ("README.md", "manifest.json", "mods/shadowslave-core.jar"):
                    data = archive.read(path)
                    self.assertEqual(hashlib.sha256(data).hexdigest(), recorded[path]["sha256"])
                    self.assertEqual(len(data), recorded[path]["size"])

    def test_missing_core_jar_is_rejected_without_partial_output(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            output = root / "pack.zip"
            with self.assertRaisesRegex(PackageError, "core JAR does not exist"):
                build_package(MANIFEST, root / "missing.jar", output)
            self.assertFalse(output.exists())
            self.assertFalse((root / "pack.zip.tmp").exists())

    def test_include_path_traversal_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            modpack = root / "modpack"
            modpack.mkdir()
            (modpack / "README.md").write_text("fixture", encoding="utf-8")
            manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
            manifest["packaging"]["include"] = ["../outside.txt", "README.md", "manifest.json"]
            manifest_path = modpack / "manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            core = root / "core.jar"
            core.write_bytes(b"core")
            with self.assertRaisesRegex(ValueError, "safe relative path"):
                build_package(manifest_path, core, root / "pack.zip")


if __name__ == "__main__":
    unittest.main()
