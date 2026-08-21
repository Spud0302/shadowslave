#!/usr/bin/env python3
"""Self-test for validate_vault.py.

A validator that has never been observed failing is not evidence. Each test
builds a throwaway vault containing exactly one violation and asserts the
matching code is reported, so "0 errors" on the real vault means something.

    python brain/tools/test_validate_vault.py
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
import validate_vault  # noqa: E402

GOOD = """---
uid: ss-fixture-good
record_kind: design
authority: context
lore_class: DESIGN
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
---

# Good note
"""


def note(**overrides):
    """Render frontmatter from the known-good baseline with fields overridden."""
    fields = {
        "uid": "ss-fixture", "record_kind": "design", "authority": "context",
        "lore_class": "DESIGN", "state": "active", "owner": "Andrew",
        "created": "2026-08-21", "updated": "2026-08-21",
    }
    fields.update(overrides)
    body = overrides.pop("_body", "# Fixture\n")
    lines = ["---"]
    for key, value in fields.items():
        if key.startswith("_") or value is None:
            continue
        lines.append("%s: %s" % (key, value))
    lines.append("---")
    lines.append("")
    lines.append(body)
    return "\n".join(lines)


class ValidatorTest(unittest.TestCase):
    def setUp(self):
        self.root = Path(tempfile.mkdtemp(prefix="vaulttest-"))
        (self.root / "brain" / "protocol").mkdir(parents=True)
        (self.root / "brain" / "protocol" / "note-schema.md").write_text(GOOD, encoding="utf-8")
        for sub in ("ai/claims", "ai/handoffs", "ai/logs", "evidence", "decisions", "design"):
            (self.root / "brain" / sub).mkdir(parents=True, exist_ok=True)

    def tearDown(self):
        shutil.rmtree(self.root, ignore_errors=True)

    def write(self, relpath, text):
        path = self.root / relpath
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")
        return path

    def run_validator(self):
        buf = io.StringIO()
        with contextlib.redirect_stdout(buf):
            code = validate_vault.main(["--root", str(self.root), "--format", "json"])
        return code, json.loads(buf.getvalue())

    def codes(self):
        _, report = self.run_validator()
        return [f["code"] for f in report["findings"]]

    def assertFinds(self, code):
        found = self.codes()
        self.assertIn(code, found, "expected %s, got %s" % (code, found))

    # --- the baseline must be clean, or every other assertion is meaningless

    def test_clean_vault_passes(self):
        self.write("brain/design/fine.md", note(uid="ss-fine"))
        code, report = self.run_validator()
        self.assertEqual(report["errors"], 0, report["findings"])
        self.assertEqual(code, 0)

    # --- frontmatter parsing

    def test_block_sequence_under_bare_key_is_parsed(self):
        """A regression: 'targets:' + block items must not read as empty.

        This silently broke the claim collision check, which answered "clear"
        for a path an active claim held.
        """
        text = ("---\nuid: ss-blk\nrecord_kind: claim\nauthority: context\n"
                "lore_class: \"N/A\"\nstate: active\nowner: claude\n"
                "created: 2026-08-21\nupdated: 2026-08-21\ntask_id: t\n"
                "branch: b\nbase_commit: abc\nlease_until: 2099-01-01T00:00:00Z\n"
                "targets:\n  - mod/src\n  - combat-core/src\n---\n\n# Claim\n")
        meta = validate_vault.parse_frontmatter(text)[0]
        self.assertEqual(meta["targets"], ["mod/src", "combat-core/src"])

    def test_explicit_empty_list_stays_empty(self):
        meta = validate_vault.parse_frontmatter(
            "---\nuid: x\ntargets: []\n---\n\nbody\n")[0]
        self.assertEqual(meta["targets"], [])

    # --- structural

    def test_missing_frontmatter(self):
        self.write("brain/design/bare.md", "# No frontmatter\n")
        self.assertFinds("FRONTMATTER")

    def test_unclosed_frontmatter(self):
        self.write("brain/design/open.md", "---\nuid: x\n\n# body\n")
        self.assertFinds("FRONTMATTER")

    def test_missing_required_property(self):
        text = note(uid="ss-x").replace("owner: Andrew\n", "")
        self.write("brain/design/noowner.md", text)
        self.assertFinds("REQUIRED_MISSING")

    # --- values

    def test_invalid_enum(self):
        self.write("brain/design/enum.md", note(uid="ss-e", authority="made-up"))
        self.assertFinds("ENUM_INVALID")

    def test_invalid_date(self):
        self.write("brain/design/date.md", note(uid="ss-d", created="21-08-2026"))
        self.assertFinds("DATE_INVALID")

    def test_updated_before_created(self):
        self.write("brain/design/order.md",
                   note(uid="ss-o", created="2026-08-21", updated="2026-08-01"))
        self.assertFinds("DATE_ORDER")

    def test_duplicate_uid(self):
        self.write("brain/design/one.md", note(uid="ss-same"))
        self.write("brain/design/two.md", note(uid="ss-same"))
        self.assertFinds("UID_DUPLICATE")

    # --- naming and placement

    def test_claim_filename_must_be_timestamped(self):
        self.write("brain/ai/claims/my-claim.md",
                   note(uid="ss-c", record_kind="claim", task_id="t", branch="b",
                        base_commit="deadbeef", lease_until="2099-01-01T00:00:00Z"))
        self.assertFinds("FILENAME")

    def test_claim_outside_claims_folder(self):
        self.write("brain/design/20260821T000000Z--claude--stray.md",
                   note(uid="ss-s", record_kind="claim", task_id="t", branch="b",
                        base_commit="deadbeef", lease_until="2099-01-01T00:00:00Z"))
        self.assertFinds("PLACEMENT")

    def test_decision_filename(self):
        self.write("brain/decisions/some-decision.md",
                   note(uid="ss-dec", record_kind="decision", state="accepted",
                        authority="project-authority"))
        self.assertFinds("FILENAME")

    def test_standing_register_keeps_stable_name(self):
        """Evidence without task_id is a living register, not a task record."""
        self.write("brain/design/drift-register.md",
                   note(uid="ss-reg", record_kind="evidence", authority="evidence"))
        self.assertNotIn("FILENAME", self.codes())

    def test_task_scoped_evidence_must_be_timestamped(self):
        self.write("brain/evidence/loose-evidence.md",
                   note(uid="ss-ev", record_kind="evidence", authority="evidence", task_id="t1"))
        self.assertFinds("FILENAME")

    # --- claims are locks

    def test_claim_without_lease(self):
        self.write("brain/ai/claims/20260821T000000Z--claude--nolease.md",
                   note(uid="ss-nl", record_kind="claim", task_id="t", branch="b",
                        base_commit="deadbeef"))
        self.assertFinds("LEASE_MISSING")

    def test_expired_lease_still_active(self):
        self.write("brain/ai/claims/20260821T000000Z--claude--stale.md",
                   note(uid="ss-sl", record_kind="claim", task_id="t", branch="b",
                        base_commit="deadbeef", lease_until="2000-01-01T00:00:00Z"))
        self.assertFinds("LEASE_EXPIRED")

    def test_expired_lease_marked_expired_is_fine(self):
        self.write("brain/ai/claims/20260821T000000Z--claude--closed.md",
                   note(uid="ss-cl", record_kind="claim", state="closed", task_id="t",
                        branch="b", base_commit="deadbeef",
                        lease_until="2000-01-01T00:00:00Z"))
        self.assertNotIn("LEASE_EXPIRED", self.codes())

    def test_claim_missing_task_id(self):
        self.write("brain/ai/claims/20260821T000000Z--claude--notask.md",
                   note(uid="ss-nt", record_kind="claim", branch="b",
                        base_commit="deadbeef", lease_until="2099-01-01T00:00:00Z"))
        self.assertFinds("REQUIRED_MISSING")

    def test_unsubstituted_placeholder_is_an_error(self):
        self.write("brain/design/scaffold.md", note(uid="ss-sc", owner="replace-agent"))
        self.assertFinds("PLACEHOLDER")

    def test_obsidian_placeholder_is_an_error_outside_templates(self):
        self.write("brain/design/raw.md", note(uid="ss-raw", created="{{date}}"))
        self.assertFinds("PLACEHOLDER")

    def test_agent_profile_kind_is_accepted(self):
        self.write("brain/ai/agents/someagent.md",
                   note(uid="ss-ai-agent-x", record_kind="agent-profile"))
        _, report = self.run_validator()
        self.assertEqual(report["errors"], 0, report["findings"])

    def test_agent_card_kind_is_not_policed(self):
        """Cards may use context or agent-profile; the vault does not pick one."""
        self.write("brain/ai/agents/legacy.md",
                   note(uid="ss-ai-agent-legacy", record_kind="context"))
        _, report = self.run_validator()
        self.assertEqual(report["errors"], 0, report["findings"])

    def test_closed_evidence_is_not_stale_when_head_moves(self):
        """captured_commit on a closed record is history, not staleness.

        Regression: after the first vault commit, every immutable evidence
        record reported SNAPSHOT_STALE simply because HEAD had advanced.
        """
        subprocess.run(["git", "init", "-q"], cwd=str(self.root),
                       capture_output=True, check=True)
        subprocess.run(["git", "-c", "user.email=t@t", "-c", "user.name=t",
                        "commit", "-q", "--allow-empty", "-m", "one"],
                       cwd=str(self.root), capture_output=True, check=True)
        old = subprocess.run(["git", "rev-parse", "HEAD"], cwd=str(self.root),
                             capture_output=True, text=True, check=True).stdout.strip()
        subprocess.run(["git", "-c", "user.email=t@t", "-c", "user.name=t",
                        "commit", "-q", "--allow-empty", "-m", "two"],
                       cwd=str(self.root), capture_output=True, check=True)

        self.write("brain/evidence/20260821T000000Z--claude--done.md",
                   note(uid="ss-ev-closed", record_kind="evidence", authority="evidence",
                        state="closed", task_id="t1", captured_commit=old))
        self.assertNotIn("SNAPSHOT_STALE", self.codes())

        self.write("brain/design/living.md",
                   note(uid="ss-living", state="active", captured_commit=old))
        self.assertFinds("SNAPSHOT_STALE")

    # --- cross references

    def test_unresolved_supersedes(self):
        self.write("brain/design/sup.md", note(uid="ss-sup", supersedes="ss-does-not-exist"))
        self.assertFinds("SUPERSEDES_UNRESOLVED")

    def test_resolved_supersedes_passes(self):
        self.write("brain/design/old.md", note(uid="ss-old"))
        self.write("brain/design/new.md", note(uid="ss-new", supersedes="ss-old"))
        self.assertNotIn("SUPERSEDES_UNRESOLVED", self.codes())

    def test_broken_wikilink(self):
        self.write("brain/design/link.md",
                   note(uid="ss-lk", _body="See [[brain/design/nowhere]] for detail.\n"))
        self.assertFinds("LINK_BROKEN")

    def test_valid_wikilink_with_table_escaped_alias(self):
        """Inside a Markdown table the alias pipe must be escaped as \\|."""
        self.write("brain/design/target.md", note(uid="ss-tg2"))
        self.write("brain/design/table.md", note(
            uid="ss-tb",
            _body="| Note |\n| --- |\n| [[brain/design/target\\|Target]] |\n"))
        self.assertNotIn("LINK_BROKEN", self.codes())

    def test_valid_wikilink_with_alias(self):
        self.write("brain/design/target.md", note(uid="ss-tg"))
        self.write("brain/design/src.md",
                   note(uid="ss-sr", _body="See [[brain/design/target|the target]].\n"))
        self.assertNotIn("LINK_BROKEN", self.codes())

    # --- templates carry placeholders and must not be graded as real notes

    def test_template_placeholders_are_not_errors(self):
        (self.root / "brain" / "templates").mkdir(parents=True, exist_ok=True)
        self.write("brain/templates/thing.md", note(
            uid='"{{date}}-replace-slug"', created='"{{date}}"', updated='"{{date}}"',
            record_kind="claim", owner="replace-agent"))
        _, report = self.run_validator()
        self.assertEqual(report["errors"], 0, report["findings"])

    # --- exit codes

    def test_error_exits_nonzero(self):
        self.write("brain/design/bad.md", note(uid="ss-b", state="not-a-state"))
        code, _ = self.run_validator()
        self.assertEqual(code, 1)


if __name__ == "__main__":
    unittest.main(verbosity=2)
