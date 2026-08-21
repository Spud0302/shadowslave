#!/usr/bin/env python3
"""Self-test for new_record.py.

The load-bearing test is test_every_kind_passes_validation: whatever the tool
emits must satisfy validate_vault.py. That closes the loop between the two
tools, so a template drifting out of conformance is caught here rather than
discovered in a filed record.

    python brain/tools/test_new_record.py
"""

import contextlib
import io
import json
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import new_record  # noqa: E402
import validate_vault  # noqa: E402

REAL_ROOT = Path(__file__).resolve().parent.parent.parent


class NewRecordTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(tempfile.mkdtemp(prefix="newrec-"))
        shutil.copytree(REAL_ROOT / "brain" / "templates", self.root / "brain" / "templates")
        (self.root / "brain" / "protocol").mkdir(parents=True)
        shutil.copy(REAL_ROOT / "brain" / "protocol" / "note-schema.md",
                    self.root / "brain" / "protocol" / "note-schema.md")
        # A real repository, because the interesting path is the one where git
        # context resolves. Without it every claim would carry a placeholder
        # base_commit and the validation test would prove nothing.
        for args in (["init", "-q"],
                     ["-c", "user.email=t@t", "-c", "user.name=t",
                      "commit", "-q", "--allow-empty", "-m", "fixture"]):
            subprocess.run(["git"] + args, cwd=str(self.root),
                           capture_output=True, check=True)

    def tearDown(self):
        shutil.rmtree(self.root, ignore_errors=True)

    def make(self, kind, slug, extra=None):
        argv = [kind, "--slug", slug, "--agent", "claude", "--root", str(self.root)]
        if kind in ("handoff", "evidence", "log"):
            argv += ["--task-id", "20260821T000000Z-claude-fixture"]
        argv += extra or []
        buf = io.StringIO()
        with contextlib.redirect_stdout(buf), contextlib.redirect_stderr(io.StringIO()):
            code = new_record.main(argv)
        return code, buf.getvalue().strip().splitlines()

    def validate(self):
        buf = io.StringIO()
        with contextlib.redirect_stdout(buf):
            validate_vault.main(["--root", str(self.root), "--format", "json"])
        return json.loads(buf.getvalue())

    # --- the loop-closing test

    def test_every_kind_passes_validation(self):
        for kind in sorted(new_record.KINDS):
            code, out = self.make(kind, "fixture-%s" % kind)
            self.assertEqual(code, 0, "%s failed to scaffold: %s" % (kind, out))
        report = self.validate()
        self.assertEqual(report["errors"], 0, report["findings"])

    # --- naming conventions

    def test_agent_record_filename_and_uid(self):
        code, out = self.make("claim", "combat-tuning")
        self.assertEqual(code, 0)
        rel = out[0]
        self.assertTrue(validate_vault.AGENT_FILENAME.match(Path(rel).name), rel)
        text = (self.root / rel).read_text(encoding="utf-8")
        uid = validate_vault.parse_frontmatter(text)[0]["uid"]
        self.assertRegex(uid, r"^\d{8}T\d{6}Z-claude-combat-tuning$")

    def test_decision_uses_adr_filename(self):
        code, out = self.make("decision", "lane-geometry")
        self.assertEqual(code, 0)
        self.assertTrue(validate_vault.ADR_FILENAME.match(Path(out[0]).name), out[0])

    def test_concept_uid_matches_vault_convention(self):
        """Concept notes use ss-<record_kind>-<slug>, not the template's {{date}} form."""
        self.make("lore", "drowned-listener")
        text = (self.root / "brain/lore/drowned-listener.md").read_text(encoding="utf-8")
        meta = validate_vault.parse_frontmatter(text)[0]
        self.assertEqual(meta["uid"], "ss-lore-drowned-listener")

    def test_feature_uid_uses_record_kind_not_cli_kind(self):
        """The feature template declares record_kind: design, so the uid follows it."""
        self.make("feature", "ash-burrower-loop")
        text = (self.root / "brain/design/ash-burrower-loop.md").read_text(encoding="utf-8")
        meta = validate_vault.parse_frontmatter(text)[0]
        self.assertEqual(meta["uid"], "ss-design-ash-burrower-loop")

    # --- placeholders must not survive

    def test_no_obsidian_placeholders_remain(self):
        for kind in sorted(new_record.KINDS):
            self.make(kind, "clean-%s" % kind)
        for path in (self.root / "brain").rglob("clean-*.md"):
            text = path.read_text(encoding="utf-8")
            self.assertNotIn("{{", text, path.name)
        for path in (self.root / "brain").rglob("*--claude--*.md"):
            text = path.read_text(encoding="utf-8")
            self.assertNotIn("{{", text, path.name)

    def test_task_id_token_not_clipped_by_shorter_token(self):
        """replace-unique-task-id must not be mangled by the shorter replace-task-id."""
        self.make("claim", "ordering")
        path = next((self.root / "brain/ai/claims").glob("*.md"))
        text = path.read_text(encoding="utf-8")
        self.assertNotIn("-unique-", text)
        self.assertNotIn("replace-task-id", text)
        meta = validate_vault.parse_frontmatter(text)[0]
        self.assertEqual(meta["task_id"], meta["uid"])

    def test_git_context_is_substituted(self):
        self.make("claim", "gitctx")
        path = next((self.root / "brain/ai/claims").glob("*.md"))
        meta = validate_vault.parse_frontmatter(path.read_text(encoding="utf-8"))[0]
        self.assertRegex(meta["base_commit"], r"^[0-9a-f]{40}$")
        self.assertNotEqual(meta["branch"], "replace-branch")

    # --- encoding

    def test_utf8_is_preserved_in_written_file(self):
        """The console may mangle an em dash on Windows; the file must not."""
        self.make("claim", "encoding")
        path = next((self.root / "brain/ai/claims").glob("*.md"))
        self.assertIn("—", path.read_text(encoding="utf-8"))

    # --- supersedes

    def test_supersedes_added_to_handoff_and_evidence(self):
        for kind, folder in (("handoff", "brain/ai/handoffs"), ("evidence", "brain/evidence")):
            self.make(kind, "sup-%s" % kind)
            path = next((self.root / folder).glob("*.md"))
            meta = validate_vault.parse_frontmatter(path.read_text(encoding="utf-8"))[0]
            self.assertIn("supersedes", meta, kind)

    # --- guard rails

    def test_refuses_to_overwrite(self):
        self.assertEqual(self.make("lore", "dup")[0], 0)
        self.assertEqual(self.make("lore", "dup")[0], 1)

    def test_handoff_requires_task_id(self):
        buf = io.StringIO()
        with contextlib.redirect_stderr(buf), contextlib.redirect_stdout(io.StringIO()):
            code = new_record.main(["handoff", "--slug", "x", "--agent", "claude",
                                    "--root", str(self.root)])
        self.assertEqual(code, 2)
        self.assertIn("--task-id", buf.getvalue())

    def test_rejects_bad_slug(self):
        buf = io.StringIO()
        with contextlib.redirect_stderr(buf), contextlib.redirect_stdout(io.StringIO()):
            code = new_record.main(["lore", "--slug", "Not A Slug", "--agent", "claude",
                                    "--root", str(self.root)])
        self.assertEqual(code, 2)

    def test_dry_run_writes_nothing(self):
        code, _ = self.make("lore", "ghost", ["--dry-run"])
        self.assertEqual(code, 0)
        self.assertFalse((self.root / "brain/lore/ghost.md").exists())


if __name__ == "__main__":
    unittest.main(verbosity=2)
