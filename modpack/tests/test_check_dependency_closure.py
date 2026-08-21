from __future__ import annotations

import json
import tempfile
import unittest
from pathlib import Path

from modpack.tools.check_dependency_closure import (
    ClosureError,
    check,
    parse_required_dependencies,
)


ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / "modpack" / "manifest.json"
MODS_TOML = ROOT / "mod" / "src" / "main" / "templates" / "META-INF" / "neoforge.mods.toml"

TOML = """
[[mods]]
modId="${mod_id}"

[[dependencies.${mod_id}]]
modId="neoforge"
type="required"
versionRange="[21.1.244,)"

[[dependencies.${mod_id}]]
modId="combat_core"
type="required"
versionRange="[${combat_core_version}]"

[[dependencies.${mod_id}]]
modId="geckolib"
type="required"
versionRange="[4.9.2]"

[[dependencies.${mod_id}]]
modId="someoptional"
type="optional"
versionRange="[1.0]"
"""


def manifest_with(mod_ids):
    return {
        "canonical_state_owner": {"mod_id": "shadowslave"},
        "components": [{"id": "%s-c" % m, "mod_id": m} for m in mod_ids],
    }


class DependencyClosureTest(unittest.TestCase):
    def write(self, root, toml_text, manifest_data):
        toml_path = root / "mods.toml"
        toml_path.write_text(toml_text, encoding="utf-8")
        manifest_path = root / "manifest.json"
        manifest_path.write_text(json.dumps(manifest_data), encoding="utf-8")
        return toml_path, manifest_path

    # --- parsing a Gradle template that is not yet valid TOML

    def test_parses_required_dependencies_only(self):
        found = dict(parse_required_dependencies(TOML))
        self.assertEqual(set(found), {"neoforge", "combat_core", "geckolib"})
        self.assertNotIn("someoptional", found)

    def test_preserves_unexpanded_version_range(self):
        found = dict(parse_required_dependencies(TOML))
        self.assertEqual(found["combat_core"], "[${combat_core_version}]")

    # --- the defect this tool exists to catch

    def test_missing_required_dependency_is_reported(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            toml_path, manifest_path = self.write(
                root, TOML, manifest_with(["geckolib"]))
            _, _, missing = check(str(toml_path), str(manifest_path))
            self.assertEqual([m for m, _ in missing], ["combat_core"])

    def test_covered_dependency_passes(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            toml_path, manifest_path = self.write(
                root, TOML, manifest_with(["geckolib", "combat_core"]))
            _, _, missing = check(str(toml_path), str(manifest_path))
            self.assertEqual(missing, [])

    def test_platform_dependencies_need_no_component(self):
        """neoforge and minecraft come from pack.loader, not a shipped JAR."""
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            toml_path, manifest_path = self.write(
                root, TOML, manifest_with(["geckolib", "combat_core"]))
            _, satisfied, _ = check(str(toml_path), str(manifest_path))
            by = {m: source for m, _, source in satisfied}
            self.assertEqual(by["neoforge"], "pack platform")

    def test_canonical_owner_counts_as_covered(self):
        toml = TOML + """
[[dependencies.${mod_id}]]
modId="shadowslave"
type="required"
versionRange="[1.0]"
"""
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            toml_path, manifest_path = self.write(
                root, toml, manifest_with(["geckolib", "combat_core"]))
            _, _, missing = check(str(toml_path), str(manifest_path))
            self.assertEqual(missing, [])

    # --- fail closed rather than passing vacuously

    def test_toml_without_required_dependencies_is_an_error(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            toml_path, manifest_path = self.write(
                root, "[[mods]]\nmodId=\"x\"\n", manifest_with([]))
            with self.assertRaisesRegex(ClosureError, "no required dependencies"):
                check(str(toml_path), str(manifest_path))

    def test_unreadable_input_is_an_error(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            _, manifest_path = self.write(root, TOML, manifest_with([]))
            with self.assertRaisesRegex(ClosureError, "cannot read"):
                check(str(root / "absent.toml"), str(manifest_path))

    # --- the real repository must stay closed

    def test_repository_manifest_covers_the_real_mods_toml(self):
        _, _, missing = check(str(MODS_TOML), str(MANIFEST))
        self.assertEqual(
            missing, [],
            "modpack/manifest.json does not ship every required dependency: %s"
            % ", ".join(m for m, _ in missing))


if __name__ == "__main__":
    unittest.main()
