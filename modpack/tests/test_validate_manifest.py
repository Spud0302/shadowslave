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

    def local_component(self) -> dict:
        """A first-party component built from this repository.

        It pins no digest, because the JAR does not exist until it is built;
        provenance records what actually shipped instead.
        """
        component = self.component()
        component["id"] = "example-local"
        component["mod_id"] = "example_local"
        component["source"] = {
            "type": "local_gradle_build",
            "project": "dev.spud.example:example",
            "artifact_glob": "example/build/libs/*.jar",
            "package_path": "mods/example-local.jar",
        }
        return component

    def with_local(self) -> dict:
        candidate = copy.deepcopy(self.manifest)
        candidate["components"].append(self.local_component())
        return candidate

    def test_repository_manifest_is_valid(self) -> None:
        validate_manifest(self.manifest)

    def test_repository_manifest_ships_combat_core(self) -> None:
        """The P0 this schema extension exists to close."""
        mod_ids = {c["mod_id"] for c in self.manifest["components"]}
        self.assertIn("combat_core", mod_ids)

    def test_local_build_component_is_accepted(self) -> None:
        validate_manifest(self.with_local())

    def test_local_build_component_requires_glob_and_package_path(self) -> None:
        for field in ("artifact_glob", "package_path"):
            with self.subTest(field=field):
                candidate = self.with_local()
                del candidate["components"][-1]["source"][field]
                with self.assertRaises(ManifestError):
                    validate_manifest(candidate)

    def test_local_build_component_must_not_pin_a_digest(self) -> None:
        """A digest that is never checked reads as a guarantee the build cannot make."""
        for field, value in (("sha256", "a" * 64), ("file", "example.jar")):
            with self.subTest(field=field):
                candidate = self.with_local()
                candidate["components"][-1]["source"][field] = value
                with self.assertRaisesRegex(ManifestError, "not valid for a local_gradle_build"):
                    validate_manifest(candidate)

    def test_local_package_path_must_be_a_jar_under_mods(self) -> None:
        for bad in ("example-local.jar", "mods/example-local.txt", "../escape.jar"):
            with self.subTest(path=bad):
                candidate = self.with_local()
                candidate["components"][-1]["source"]["package_path"] = bad
                with self.assertRaises(ManifestError):
                    validate_manifest(candidate)

    def test_local_package_path_must_not_collide_with_canonical_owner(self) -> None:
        candidate = self.with_local()
        candidate["components"][-1]["source"]["package_path"] = \
            candidate["canonical_state_owner"]["package_path"]
        with self.assertRaisesRegex(ManifestError, "collides with the canonical state owner"):
            validate_manifest(candidate)

    def test_two_local_components_must_not_share_a_package_path(self) -> None:
        candidate = self.with_local()
        second = self.local_component()
        second["id"] = "example-local-2"
        second["mod_id"] = "example_local_2"
        candidate["components"].append(second)
        with self.assertRaisesRegex(ManifestError, "duplicate component package path"):
            validate_manifest(candidate)

    def test_remote_component_still_requires_its_digest(self) -> None:
        candidate = copy.deepcopy(self.manifest)
        component = self.component()
        del component["source"]["sha256"]
        candidate["components"].append(component)
        with self.assertRaises(ManifestError):
            validate_manifest(candidate)

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
