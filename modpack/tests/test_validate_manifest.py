from __future__ import annotations

import copy
import json
import unittest
from pathlib import Path

from modpack.tools.validate_manifest import ManifestError, validate_manifest


ROOT = Path(__file__).resolve().parents[2]


class ManifestValidationTest(unittest.TestCase):
    def setUp(self) -> None:
        with (ROOT / "modpack" / "manifest.json").open("r", encoding="utf-8") as handle:
            self.manifest = json.load(handle)

    def component(self) -> dict:
        return {
            "id": "example-provider",
            "mod_id": "example",
            "version": "1.0.0",
            "role": "execution_provider",
            "side": "both",
            "required": False,
            "owns_canonical_state": False,
            "source": {
                "type": "modrinth",
                "project": "example-project",
                "file": "example-file",
                "sha256": "a" * 64,
            },
            "license": "Example-License",
            "removal_behavior": "Canonical state remains readable and the provider-backed ability becomes unavailable.",
        }

    def test_repository_manifest_is_valid(self) -> None:
        validate_manifest(self.manifest)

    def test_provider_requires_hash_license_and_removal_contract(self) -> None:
        for field_path in ("sha256", "license", "removal_behavior"):
            with self.subTest(field=field_path):
                candidate = copy.deepcopy(self.manifest)
                component = self.component()
                if field_path == "sha256":
                    component["source"][field_path] = "not-a-hash"
                else:
                    component[field_path] = ""
                candidate["components"] = [component]
                with self.assertRaises(ManifestError):
                    validate_manifest(candidate)

    def test_external_component_cannot_claim_canonical_state(self) -> None:
        candidate = copy.deepcopy(self.manifest)
        component = self.component()
        component["owns_canonical_state"] = True
        candidate["components"] = [component]
        with self.assertRaisesRegex(ManifestError, "owns_canonical_state"):
            validate_manifest(candidate)

    def test_duplicate_component_ids_are_rejected(self) -> None:
        candidate = copy.deepcopy(self.manifest)
        candidate["components"] = [self.component(), self.component()]
        with self.assertRaisesRegex(ManifestError, "duplicate component id"):
            validate_manifest(candidate)

    def test_packaging_file_list_is_deterministic(self) -> None:
        candidate = copy.deepcopy(self.manifest)
        candidate["packaging"]["include"] = ["manifest.json", "README.md", "README.md"]
        with self.assertRaisesRegex(ManifestError, "unique and lexicographically sorted"):
            validate_manifest(candidate)


if __name__ == "__main__":
    unittest.main()
