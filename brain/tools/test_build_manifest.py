#!/usr/bin/env python3
"""Self-test for build_manifest.py.

Verifies deterministic JSON emission, abstract/snapshot extraction,
provenance stamping, and drift detection with --check.

    python brain/tools/test_build_manifest.py
"""

import json
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import build_manifest  # noqa: E402


class BuildManifestTests(unittest.TestCase):
    def setUp(self):
        self.tmp = Path(tempfile.mkdtemp(prefix="ss-test-manifest-"))
        self.brain = self.tmp / "brain"
        self.brain.mkdir(parents=True)
        # Initialize a throwaway git repository
        subprocess.run(["git", "init", "-b", "main", str(self.tmp)], check=True, stdout=subprocess.DEVNULL)
        subprocess.run(["git", "-C", str(self.tmp), "config", "user.name", "Test"], check=True)
        subprocess.run(["git", "-C", str(self.tmp), "config", "user.email", "test@example.com"], check=True)

    def tearDown(self):
        shutil.rmtree(self.tmp, ignore_errors=True)

    def _write_note(self, rel_path, content):
        p = self.brain / rel_path
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(content, encoding="utf-8")
        return p

    def test_collect_manifest_basic(self):
        self._write_note(
            "design/combat.md",
            """---
uid: ss-design-combat
record_kind: design
authority: project-authority
lore_class: DESIGN
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
tags:
  - combat
  - v1
---

# Combat System

> [!abstract]
> Server-authoritative action state machine for melee combat.

Details about combat.
""",
        )
        self._write_note(
            "lore/creature.md",
            """---
uid: ss-lore-creature
record_kind: lore
authority: context
lore_class: DESIGN
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
tags:
  - creature
---

# Creature Note

Creature description paragraph.
""",
        )

        manifest = build_manifest.collect_manifest(self.tmp)
        self.assertEqual(manifest["schema_version"], 1)
        self.assertEqual(manifest["note_count"], 2)
        self.assertIn("ss-design-combat", manifest["notes"])
        self.assertIn("ss-lore-creature", manifest["notes"])

        combat_note = manifest["notes"]["ss-design-combat"]
        self.assertEqual(combat_note["title"], "Combat System")
        self.assertEqual(combat_note["summary"], "Server-authoritative action state machine for melee combat.")
        self.assertEqual(combat_note["tags"], ["combat", "v1"])

    def test_sorting_determinism(self):
        self._write_note(
            "z_note.md",
            """---
uid: ss-z-note
record_kind: idea
authority: proposal
lore_class: N/A
state: proposed
owner: tester
created: 2026-08-21
updated: 2026-08-21
tags:
  - zebra
  - apple
---

# Z Note
""",
        )
        self._write_note(
            "a_note.md",
            """---
uid: ss-a-note
record_kind: idea
authority: proposal
lore_class: N/A
state: proposed
owner: tester
created: 2026-08-21
updated: 2026-08-21
tags:
  - banana
---

# A Note
""",
        )

        manifest = build_manifest.collect_manifest(self.tmp)
        keys = list(manifest["notes"].keys())
        self.assertEqual(keys, ["ss-a-note", "ss-z-note"])
        self.assertEqual(manifest["notes"]["ss-z-note"]["tags"], ["apple", "zebra"])

    def test_check_mode_detects_drift(self):
        self._write_note(
            "test.md",
            """---
uid: ss-test
record_kind: idea
authority: proposal
lore_class: N/A
state: proposed
owner: tester
created: 2026-08-21
updated: 2026-08-21
---

# Test
Initial body.
""",
        )
        # Generate initial manifest
        out_path = self.brain / "manifest.json"
        res = build_manifest.main(["--root", str(self.tmp), "--output", str(out_path)])
        self.assertEqual(res, 0)

        # Verify check mode passes
        check_res = build_manifest.main(["--root", str(self.tmp), "--output", str(out_path), "--check"])
        self.assertEqual(check_res, 0)

        # Modify note and verify check mode fails
        self._write_note(
            "test.md",
            """---
uid: ss-test
record_kind: idea
authority: proposal
lore_class: N/A
state: proposed
owner: tester
created: 2026-08-21
updated: 2026-08-21
tags:
  - newly-added-tag
---

# Test
Modified body.
""",
        )
        drift_res = build_manifest.main(["--root", str(self.tmp), "--output", str(out_path), "--check"])
        self.assertEqual(drift_res, 1)


if __name__ == "__main__":
    unittest.main()
