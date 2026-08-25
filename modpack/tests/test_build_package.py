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


def fixture_filename(component: dict, data: bytes) -> str:
    """Name the on-disk fixture JAR, pinning a digest only where one belongs.

    A remote component is verified against `source.sha256`, so the fixture
    rewrites that digest to match its payload. A `local_gradle_build` component
    is built from this repository and pins no digest; setting one is rejected by
    the validator, and its archive path comes from `source.package_path`.
    """
    source = component["source"]
    if source.get("type") == "local_gradle_build":
        return source["package_path"].rsplit("/", 1)[-1]
    source["sha256"] = hashlib.sha256(data).hexdigest()
    return source["file"]


def fixture_manifest(root: Path) -> tuple[Path, dict[str, Path]]:
    modpack = root / "modpack"
    modpack.mkdir()
    (modpack / "README.md").write_text("fixture", encoding="utf-8")
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    components: dict[str, Path] = {}
    # See fixture_filename: a locally built component pins no digest, so the
    # fixture must not invent one.
    for component in manifest["components"]:
        component_id = component["id"]
        data = f"deterministic-{component_id}-fixture".encode()
        local = root / fixture_filename(component, data)
        local.write_bytes(data)
        components[component_id] = local

    manifest_path = modpack / "manifest.json"
    manifest_path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    return manifest_path, components


class DeterministicPackageTest(unittest.TestCase):
    def test_same_inputs_produce_identical_archive_and_digest(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest, components = fixture_manifest(root)
            core = root / "core.jar"
            core.write_bytes(b"deterministic-core-fixture")
            first = root / "first.zip"
            second = root / "second.zip"

            first_digest = build_package(manifest, core, first, components)
            second_digest = build_package(manifest, core, second, components)

            self.assertEqual(first.read_bytes(), second.read_bytes())
            self.assertEqual(first_digest, second_digest)
            self.assertEqual(hashlib.sha256(first.read_bytes()).hexdigest(), first_digest)

    def test_archive_has_sorted_fixed_metadata_and_verified_provenance(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest, components = fixture_manifest(root)
            core = root / "core.jar"
            core.write_bytes(b"core-content")
            output = root / "pack.zip"
            build_package(manifest, core, output, components)

            component_entries = [f"mods/{path.name}" for path in components.values()]
            expected_entries = sorted([
                "README.md",
                "manifest.json",
                *component_entries,
                "mods/shadowslave-core.jar",
                "provenance.json",
            ])

            with zipfile.ZipFile(output) as archive:
                names = archive.namelist()
                self.assertEqual(expected_entries, names)
                for info in archive.infolist():
                    self.assertEqual(FIXED_ZIP_TIME, info.date_time)
                    self.assertEqual(0o100644, info.external_attr >> 16)

                provenance = json.loads(archive.read("provenance.json"))
                self.assertEqual("shadowslave-nightmare-spell", provenance["pack_id"])
                self.assertEqual("0.0.0-dev", provenance["pack_version"])
                recorded = {entry["path"]: entry for entry in provenance["entries"]}
                self.assertNotIn("provenance.json", recorded)
                for path in expected_entries:
                    if path == "provenance.json":
                        continue
                    data = archive.read(path)
                    self.assertEqual(hashlib.sha256(data).hexdigest(), recorded[path]["sha256"])
                    self.assertEqual(len(data), recorded[path]["size"])

    def test_required_component_must_be_supplied(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest, components = fixture_manifest(root)
            core = root / "core.jar"
            core.write_bytes(b"core")
            components.pop(next(iter(components)))
            with self.assertRaisesRegex(PackageError, "required component JAR was not supplied"):
                build_package(manifest, core, root / "pack.zip", components)

    def test_component_digest_mismatch_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest, components = fixture_manifest(root)
            component = components["geckolib-4"]
            component.write_bytes(b"tampered")
            core = root / "core.jar"
            core.write_bytes(b"core")
            with self.assertRaisesRegex(PackageError, "component SHA-256 mismatch"):
                build_package(manifest, core, root / "pack.zip", components)

    def test_component_filename_with_backslash_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path, components = fixture_manifest(root)
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            # Must target a remote component: only those carry source.file, and
            # the manifest now leads with a local_gradle_build component.
            remote = next(c for c in manifest["components"]
                          if c["source"].get("type") != "local_gradle_build")
            remote["source"]["file"] = "bad\\name.jar"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            core = root / "core.jar"
            core.write_bytes(b"core")
            with self.assertRaisesRegex(PackageError, "source.file must be a JAR filename"):
                build_package(manifest_path, core, root / "pack.zip", components)

    def test_missing_core_jar_is_rejected_without_partial_output(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest, components = fixture_manifest(root)
            output = root / "pack.zip"
            with self.assertRaisesRegex(PackageError, "core JAR does not exist"):
                build_package(manifest, root / "missing.jar", output, components)
            self.assertFalse(output.exists())
            self.assertFalse((root / "pack.zip.tmp").exists())

    def test_include_path_traversal_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            manifest_path, components = fixture_manifest(root)
            manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
            manifest["packaging"]["include"] = ["../outside.txt", "README.md", "manifest.json"]
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            core = root / "core.jar"
            core.write_bytes(b"core")
            with self.assertRaisesRegex(ValueError, "safe relative path"):
                build_package(manifest_path, core, root / "pack.zip", components)


if __name__ == "__main__":
    unittest.main()
