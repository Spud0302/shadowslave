#!/usr/bin/env python3
"""Tests for validate_animation_timing.py.

Covers tick/second agreement, named-constant resolution across files, phase
coverage rules, and the parser's refusal to be fooled by comments or by Combat
Core's own test fixtures.
"""

import json
import shutil
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

import validate_animation_timing as tool  # noqa: E402


class AnimationTimingTest(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp())
        self.java = self.tmp / "mod" / "src" / "main" / "java" / "dev" / "spud"
        self.java.mkdir(parents=True)
        self.animations = self.tmp / "mod" / "src" / "main" / "resources" / "anim"
        self.animations.mkdir(parents=True)

    def tearDown(self):
        shutil.rmtree(self.tmp, ignore_errors=True)

    # helpers -----------------------------------------------------------------

    def _entity(self, name, body):
        (self.java / f"{name}.java").write_text(
            f"package dev.spud;\npublic final class {name} {{\n{body}\n}}\n",
            encoding="utf-8",
        )

    def _action(self, name, action_id, windup, active, recovery):
        self._entity(
            name,
            "    private static final CombatActionDefinition ACTION =\n"
            f'            new CombatActionDefinition("{action_id}", '
            f"{windup}, {active}, {recovery}, 4.0D);",
        )

    def _clips(self, filename, clips):
        payload = {
            "format_version": "1.8.0",
            "animations": {
                name: {"animation_length": length, "loop": False}
                for name, length in clips.items()
            },
        }
        path = self.animations / filename
        path.write_text(json.dumps(payload), encoding="utf-8")
        return path.relative_to(self.tmp).as_posix()

    def _bindings(self, config):
        path = self.tmp / "bindings.json"
        path.write_text(json.dumps(config), encoding="utf-8")
        return path

    def _run(self, config, strict=False):
        argv = ["--root", str(self.tmp), "--bindings", str(self._bindings(config))]
        if strict:
            argv.append("--strict")
        return tool.main(argv)

    def _one_binding(self, action_id, animation_file, clips):
        return {
            "tolerance_ticks": 1,
            "bindings": [
                {
                    "action_id": action_id,
                    "animation_file": animation_file,
                    "clips": clips,
                }
            ],
        }

    # timing agreement --------------------------------------------------------

    def test_matching_timing_passes(self):
        self._action("Beast", "test:strike", 12, 1, 8)  # 21 ticks = 1.05s
        anim = self._clips("beast.animation.json", {"attack": 1.05})
        result = self._run(self._one_binding(
            "test:strike", anim,
            [{"animation": "attack", "covers": ["windup", "active", "recovery"]}],
        ))
        self.assertEqual(result, 0)

    def test_short_animation_fails(self):
        """The real Chainback bug: a 21-tick action with a 10-tick clip."""
        self._action("Beast", "test:strike", 12, 1, 8)
        anim = self._clips("beast.animation.json", {"attack": 0.5})
        result = self._run(self._one_binding(
            "test:strike", anim,
            [{"animation": "attack", "covers": ["windup", "active", "recovery"]}],
        ))
        self.assertEqual(result, 1)

    def test_drift_within_tolerance_passes(self):
        self._action("Beast", "test:strike", 12, 1, 8)  # 21 ticks
        anim = self._clips("beast.animation.json", {"attack": 1.10})  # 22 ticks
        result = self._run(self._one_binding(
            "test:strike", anim,
            [{"animation": "attack", "covers": ["windup", "active", "recovery"]}],
        ))
        self.assertEqual(result, 0)

    def test_per_phase_clips_sum(self):
        self._action("Beast", "test:strike", 12, 1, 8)  # 0.60 + 0.05 + 0.40
        anim = self._clips(
            "beast.animation.json",
            {"wind": 0.60, "hit": 0.05, "settle": 0.40},
        )
        result = self._run(self._one_binding(
            "test:strike", anim,
            [
                {"animation": "wind", "covers": ["windup"]},
                {"animation": "hit", "covers": ["active"]},
                {"animation": "settle", "covers": ["recovery"]},
            ],
        ))
        self.assertEqual(result, 0)

    # constant resolution -----------------------------------------------------

    def test_named_constants_resolve_across_files(self):
        """Glass Road's shape: constants live in a different class."""
        self._entity(
            "Item",
            "    static final int WINDUP_TICKS = 10;\n"
            "    static final int RECOVERY_TICKS = 16;",
        )
        self._entity(
            "Data",
            "    static final CombatActionDefinition ACTION =\n"
            '            new CombatActionDefinition("test:edge",\n'
            "                    Item.WINDUP_TICKS,\n"
            "                    1,\n"
            "                    Item.RECOVERY_TICKS,\n"
            "                    4.5D);",
        )
        actions = tool.parse_actions(self.tmp)
        self.assertEqual(actions["test:edge"]["windup"], 10)
        self.assertEqual(actions["test:edge"]["recovery"], 16)

    def test_unresolvable_expression_is_a_tool_error(self):
        self._entity(
            "Data",
            "    static final CombatActionDefinition ACTION =\n"
            '            new CombatActionDefinition("test:x", base * 2, 1, 8, 4.0D);',
        )
        with self.assertRaises(tool.ToolError):
            tool.parse_actions(self.tmp)

    # parser robustness -------------------------------------------------------

    def test_commented_out_definitions_are_ignored(self):
        self._entity(
            "Beast",
            '    // new CombatActionDefinition("test:ghost", 1, 1, 1, 1.0D);\n'
            '    /* new CombatActionDefinition("test:block", 2, 2, 2, 2.0D); */\n'
            "    private static final CombatActionDefinition ACTION =\n"
            '            new CombatActionDefinition("test:real", 12, 1, 8, 4.0D);',
        )
        actions = tool.parse_actions(self.tmp)
        self.assertEqual(sorted(actions), ["test:real"])

    def test_test_sources_are_not_scanned(self):
        test_root = self.tmp / "combat-core" / "src" / "test" / "java"
        test_root.mkdir(parents=True)
        (test_root / "Fixture.java").write_text(
            'class Fixture { void t() { new CombatActionDefinition("fixture", 3, 1, 4, 2.5); } }',
            encoding="utf-8",
        )
        self.assertEqual(tool.parse_actions(self.tmp), {})

    def test_length_derived_from_keyframes_when_absent(self):
        path = self.animations / "derived.animation.json"
        path.write_text(json.dumps({
            "animations": {
                "attack": {"bones": {"body": {"rotation": {"0.0": [0, 0, 0], "1.05": [0, 90, 0]}}}}
            }
        }), encoding="utf-8")
        clips = tool.load_animation_clips(path)
        self.assertAlmostEqual(tool.clip_length_seconds(clips["attack"]), 1.05)

    # coverage rules ----------------------------------------------------------

    def test_uncovered_phase_fails(self):
        self._action("Beast", "test:strike", 12, 1, 8)
        anim = self._clips("beast.animation.json", {"wind": 0.6})
        result = self._run(self._one_binding(
            "test:strike", anim, [{"animation": "wind", "covers": ["windup"]}],
        ))
        self.assertEqual(result, 1)

    def test_phase_covered_twice_fails(self):
        self._action("Beast", "test:strike", 12, 1, 8)
        anim = self._clips("beast.animation.json", {"a": 0.6, "b": 0.45})
        result = self._run(self._one_binding(
            "test:strike", anim,
            [
                {"animation": "a", "covers": ["windup", "active", "recovery"]},
                {"animation": "b", "covers": ["recovery"]},
            ],
        ))
        self.assertEqual(result, 1)

    def test_missing_clip_fails(self):
        self._action("Beast", "test:strike", 12, 1, 8)
        anim = self._clips("beast.animation.json", {"attack": 1.05})
        result = self._run(self._one_binding(
            "test:strike", anim,
            [{"animation": "absent", "covers": ["windup", "active", "recovery"]}],
        ))
        self.assertEqual(result, 1)

    # declaration rules -------------------------------------------------------

    def test_undeclared_action_fails(self):
        self._action("Beast", "test:strike", 12, 1, 8)
        self.assertEqual(self._run({"bindings": [], "unbound": []}), 1)

    def test_unbound_with_reason_passes(self):
        self._action("Beast", "test:strike", 12, 1, 8)
        config = {"bindings": [], "unbound": [
            {"action_id": "test:strike", "reason": "no clip authored yet"}
        ]}
        self.assertEqual(self._run(config), 0)

    def test_unbound_without_reason_fails(self):
        self._action("Beast", "test:strike", 12, 1, 8)
        config = {"bindings": [], "unbound": [{"action_id": "test:strike"}]}
        self.assertEqual(self._run(config), 1)

    def test_strict_rejects_unbound(self):
        self._action("Beast", "test:strike", 12, 1, 8)
        config = {"bindings": [], "unbound": [
            {"action_id": "test:strike", "reason": "no clip authored yet"}
        ]}
        self.assertEqual(self._run(config, strict=True), 1)

    def test_stale_binding_fails(self):
        self._action("Beast", "test:strike", 12, 1, 8)
        anim = self._clips("beast.animation.json", {"attack": 1.05})
        config = {
            "bindings": [
                {
                    "action_id": "test:strike",
                    "animation_file": anim,
                    "clips": [{"animation": "attack",
                               "covers": ["windup", "active", "recovery"]}],
                },
                {"action_id": "test:deleted", "animation_file": anim, "clips": []},
            ]
        }
        self.assertEqual(self._run(config), 1)

    def test_duplicate_action_id_is_a_tool_error(self):
        self._action("BeastOne", "test:same", 12, 1, 8)
        self._action("BeastTwo", "test:same", 4, 1, 6)
        with self.assertRaises(tool.ToolError):
            tool.parse_actions(self.tmp)


if __name__ == "__main__":
    unittest.main()
