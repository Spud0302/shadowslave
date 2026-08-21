#!/usr/bin/env python3
"""Self-test for query_vault.py.

Verifies filtering by kind, state, authority, lore_class, tags, active claims,
and substring searches.

    python brain/tools/test_query_vault.py
"""

import argparse
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import query_vault  # noqa: E402


class QueryVaultTests(unittest.TestCase):
    def setUp(self):
        self.sample_notes = {
            "ss-design-combat-v1": {
                "uid": "ss-design-combat-v1",
                "path": "brain/design/combat-v1.md",
                "record_kind": "design",
                "authority": "project-authority",
                "lore_class": "DESIGN",
                "state": "active",
                "owner": "Andrew",
                "title": "Combat v1",
                "summary": "Chainback-first vertical slice.",
                "tags": ["design", "combat-v1", "chainback"],
            },
            "ss-lore-chainback": {
                "uid": "ss-lore-chainback",
                "path": "brain/lore/chainback.md",
                "record_kind": "lore",
                "authority": "context",
                "lore_class": "DESIGN",
                "state": "active",
                "owner": "Andrew",
                "title": "Chainback",
                "summary": "Creature lore and combat dance definition.",
                "tags": ["lore", "creature", "chainback"],
            },
            "20260821T070000Z-test-claim": {
                "uid": "20260821T070000Z-test-claim",
                "path": "brain/ai/claims/20260821T070000Z--test--claim.md",
                "record_kind": "claim",
                "authority": "context",
                "lore_class": "N/A",
                "state": "active",
                "owner": "test-agent",
                "title": "Claim — Test task",
                "summary": "Active claim fixture.",
                "tags": ["claim"],
            },
            "20260821T060000Z-old-claim": {
                "uid": "20260821T060000Z-old-claim",
                "path": "brain/ai/claims/20260821T060000Z--old--claim.md",
                "record_kind": "claim",
                "authority": "context",
                "lore_class": "N/A",
                "state": "closed",
                "owner": "old-agent",
                "title": "Claim — Closed task",
                "summary": "Closed claim fixture.",
                "tags": ["claim"],
            },
        }

    def test_filter_by_kind(self):
        args = argparse.Namespace(
            kind="design", authority=None, lore_class=None, state=None,
            owner=None, tag=None, search=None, active_claims=False
        )
        res = query_vault.filter_notes(self.sample_notes, args)
        self.assertEqual(len(res), 1)
        self.assertEqual(res[0]["uid"], "ss-design-combat-v1")

    def test_filter_by_tag(self):
        args = argparse.Namespace(
            kind=None, authority=None, lore_class=None, state=None,
            owner=None, tag="chainback", search=None, active_claims=False
        )
        res = query_vault.filter_notes(self.sample_notes, args)
        self.assertEqual(len(res), 2)
        uids = {r["uid"] for r in res}
        self.assertEqual(uids, {"ss-design-combat-v1", "ss-lore-chainback"})

    def test_filter_active_claims(self):
        args = argparse.Namespace(
            kind=None, authority=None, lore_class=None, state=None,
            owner=None, tag=None, search=None, active_claims=True
        )
        res = query_vault.filter_notes(self.sample_notes, args)
        self.assertEqual(len(res), 1)
        self.assertEqual(res[0]["uid"], "20260821T070000Z-test-claim")

    def test_substring_search(self):
        args = argparse.Namespace(
            kind=None, authority=None, lore_class=None, state=None,
            owner=None, tag=None, search="vertical slice", active_claims=False
        )
        res = query_vault.filter_notes(self.sample_notes, args)
        self.assertEqual(len(res), 1)
        self.assertEqual(res[0]["uid"], "ss-design-combat-v1")


if __name__ == "__main__":
    unittest.main()
