from __future__ import annotations

import hashlib
import json
import tempfile
import unittest
from pathlib import Path
import zipfile

from modpack.tools.build_package import FIXED_ZIP_TIME, build_package, zip_info
from modpack.tools.verify_package import VerificationError, verify_package


ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / "modpack" / "manifest.json"


class PackageVerificationTest(unittest.TestCase):
    def build_fixture(self, root: Path) -> Path:
        modpack = root / "modpack"
        modpack.mkdir()
        (modpack / "README.md").write_text("fixture", encoding="utf-8")
        manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
        components: dict[str, Path] = {}
        for component in manifest["components"]:
            data = f"verified-{component['id']}-fixture".encode()
            source = component["source"]
            # A local_gradle_build component pins no digest -- the validator
            # rejects one -- and states its archive path outright.
            if source.get("type") == "local_gradle_build":
                local = root / source["package_path"].rsplit("/", 1)[-1]
            else:
                source["sha256"] = hashlib.sha256(data).hexdigest()
                local = root / source["file"]
            local.write_bytes(data)
            components[component["id"]] = local

        manifest_path = modpack / "manifest.json"
        manifest_path.write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")

        core = root / "core.jar"
        core.write_bytes(b"verified-core-fixture")
        archive = root / "pack.zip"
        build_package(manifest_path, core, archive, components)
        return archive

    def rewrite(self, source: Path, destination: Path, transform) -> None:
        with zipfile.ZipFile(source) as original, zipfile.ZipFile(destination, "w") as changed:
            for info in original.infolist():
                new_info, data = transform(info, original.read(info.filename))
                changed.writestr(new_info, data)

    def test_built_archive_verifies(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            archive = self.build_fixture(Path(directory))
            digest = verify_package(archive)
            self.assertEqual(64, len(digest))

    def test_payload_tampering_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            archive = self.build_fixture(root)
            tampered = root / "tampered.zip"

            def transform(info: zipfile.ZipInfo, data: bytes):
                replacement = zip_info(info.filename)
                if info.filename == "mods/shadowslave-core.jar":
                    data += b"tampered"
                return replacement, data

            self.rewrite(archive, tampered, transform)
            with self.assertRaisesRegex(VerificationError, "size mismatch"):
                verify_package(tampered)

    def test_unrecorded_payload_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            archive = self.build_fixture(root)
            changed = root / "extra.zip"
            with zipfile.ZipFile(archive) as original, zipfile.ZipFile(changed, "w") as output:
                for info in original.infolist():
                    output.writestr(zip_info(info.filename), original.read(info.filename))
                output.writestr(zip_info("unexpected.txt"), b"not recorded")

            with self.assertRaisesRegex(VerificationError, "entry set does not match"):
                verify_package(changed)

    def test_duplicate_archive_name_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            archive = self.build_fixture(root)
            changed = root / "duplicate.zip"
            with zipfile.ZipFile(archive) as original, zipfile.ZipFile(changed, "w") as output:
                for info in original.infolist():
                    output.writestr(zip_info(info.filename), original.read(info.filename))
                output.writestr(zip_info("README.md"), b"duplicate")

            with self.assertRaisesRegex(VerificationError, "duplicate entry names"):
                verify_package(changed)

    def test_non_deterministic_metadata_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            archive = self.build_fixture(root)
            changed = root / "metadata.zip"

            def transform(info: zipfile.ZipInfo, data: bytes):
                replacement = zip_info(info.filename)
                if info.filename == "README.md":
                    replacement.date_time = (2026, 8, 7, 0, 0, 0)
                return replacement, data

            self.rewrite(archive, changed, transform)
            with self.assertRaisesRegex(VerificationError, "non-deterministic timestamp"):
                verify_package(changed)

    def test_malformed_provenance_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            archive = self.build_fixture(root)
            changed = root / "bad-provenance.zip"

            def transform(info: zipfile.ZipInfo, data: bytes):
                replacement = zip_info(info.filename)
                if info.filename == "provenance.json":
                    provenance = json.loads(data)
                    provenance["entries"][0]["sha256"] = "not-a-digest"
                    data = (json.dumps(provenance, sort_keys=True) + "\n").encode()
                return replacement, data

            self.rewrite(archive, changed, transform)
            with self.assertRaisesRegex(VerificationError, "64-character hexadecimal digest"):
                verify_package(changed)

    def test_unsafe_entry_name_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            changed = root / "unsafe.zip"
            with zipfile.ZipFile(changed, "w") as output:
                unsafe = zip_info("../escape.txt")
                output.writestr(unsafe, b"escape")
                output.writestr(zip_info("provenance.json"), b"{}")

            with self.assertRaisesRegex(VerificationError, "unsafe or non-canonical"):
                verify_package(changed)


if __name__ == "__main__":
    unittest.main()
