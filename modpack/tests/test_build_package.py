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
COMPONENT_ID = "geckolib-4"
COMPONENT_FILE = "geckolib-neoforge-1.21.1-4.9.2.jar"
COMPONENT_BYTES = b"deterministic-geckolib-fixture"


def fixture_manifest(root: Path) -> tuple[Path, Path]:
    modpack = root / "modpack"
    modpack.mkdir()
    (modpack / "README.md").write_text("fixture", encoding="utf-8")
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    manifest["components"][0]["source"]["sha256"] = hashlib.sha256(COMPONENT_BYTES).hexdigest()
    manifest_path = modpack / "manifest.json"
    manifest_path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    component = root / COMPONENT_FILE
    component.write_bytes(COMPONENT_BYTES)
    return manifest_path, component


class DeterministicPackageTest(unittest.TestCase):
    def test_same_inputs_produce_identical_archive_and_digest(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest, component = fixture_manifest(root)
            core = root / "core.jar"
            core.write_bytes(b"deterministic-core-fixture")
            first = root / "first.zip"
            second = root / "second.zip"
            components = {COMPONENT_ID: component}

            first_digest = build_package(manifest, core, first, components)
            second_digest = build_package(manifest, core, second, components)

            self.assertEqual(first.read_bytes(), second.read_bytes())
            self.assertEqual(first_digest, second_digest)
            self.assertEqual(hashlib.sha256(first.read_bytes()).hexdigest(), first_digest)

    def test_archive_has_sorted_fixed_metadata_and_verified_provenance(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest, component = fixture_manifest(root)
            core = root / "core.jar"
            core.write_bytes(b"core-content")
            output = root / "pack.zip"
            build_package(manifest, core, output, {COMPONENT_ID: component})

            with zipfile.ZipFile(output) as archive:
                names = archive.namelist()
                self.assertEqual(sorted(names), names)
                self.assertEqual(
                    [
                        "README.md",
                        "manifest.json",
                        f"mods/{COMPONENT_FILE}",
                        "mods/shadowslave-core.jar",
                        "provenance.json",
                    ],
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
                for path in (
                    "README.md",
                    "manifest.json",
                    f"mods/{COMPONENT_FILE}",
                    "mods/shadowslave-core.jar",
                ):
                    data = archive.read(path)
                    self.assertEqual(hashlib.sha256(data).hexdigest(), recorded[path]["sha256"])
                    self.assertEqual(len(data), recorded[path]["size"])

    def test_required_component_must_be_supplied(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest, _ = fixture_manifest(root)
            core = root / "core.jar"
            core.write_bytes(b"core")
            with self.assertRaisesRegex(PackageError, "required component JAR was not supplied"):
                build_package(manifest, core, root / "pack.zip")

    def test_component_digest_mismatch_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest, component = fixture_manifest(root)
            component.write_bytes(b"tampered")
            core = root / "core.jar"
            core.write_bytes(b"core")
            with self.assertRaisesRegex(PackageError, "component SHA-256 mismatch"):
                build_package(manifest, core, root / "pack.zip", {COMPONENT_ID: component})

    def test_component_filename_with_backslash_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path, component = fixture_manifest(root)
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            manifest["components"][0]["source"]["file"] = "bad\\name.jar"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            core = root / "core.jar"
            core.write_bytes(b"core")
            with self.assertRaisesRegex(PackageError, "source.file must be a JAR filename"):
                build_package(manifest_path, core, root / "pack.zip", {COMPONENT_ID: component})

    def test_missing_core_jar_is_rejected_without_partial_output(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest, component = fixture_manifest(root)
            output = root / "pack.zip"
            with self.assertRaisesRegex(PackageError, "core JAR does not exist"):
                build_package(manifest, root / "missing.jar", output, {COMPONENT_ID: component})
            self.assertFalse(output.exists())
            self.assertFalse((root / "pack.zip.tmp").exists())

    def test_include_path_traversal_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path, component = fixture_manifest(root)
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            manifest["packaging"]["include"] = ["../outside.txt", "README.md", "manifest.json"]
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            core = root / "core.jar"
            core.write_bytes(b"core")
            with self.assertRaisesRegex(ValueError, "safe relative path"):
                build_package(manifest_path, core, root / "pack.zip", {COMPONENT_ID: component})


if __name__ == "__main__":
    unittest.main()
