---
uid: 20260821T073015Z-claude-freshness-and-quality-checks-handoff
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude
task_id: 20260821T072638Z-claude-freshness-and-quality-checks
created: 2026-08-21
updated: 2026-08-21
branch: vault/multi-ai-brain
base_commit: acf4ed5fda81
head_commit: acf4ed5fda81
worktree_dirty: true
sources:
  - brain/evidence/20260821T073015Z--claude--freshness-and-quality-checks.md
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Freshness and quality checks

## Owner intent and scope

Andrew: *"start working on the vault optimisations, I'll have another agent
running the same task alongside."*

This claim took the mechanical enforcement lane only — items 11, 16, 18, and 19 —
and declared the remaining backlog as available in its lane table. The other
agent independently took items 1 and 3, adding `build_manifest.py` and
`query_vault.py`. No coordination beyond the claim was needed.

## Outcome

Four backlog items are now enforced by `validate_vault.py`, each with tests
proving it fires on a violation rather than only passing.

## Files changed

- `brain/tools/validate_vault.py` — `derived_from` freshness, handoff
  verification, packet budget, and a parser that errors instead of guessing
- `brain/tools/test_validate_vault.py` — 30 to 42 tests
- `brain/tools/agent_brief.py` — stale-notes section aligned with the validator
- `brain/tools/README.md` — documented the above, alongside the other agent's
  concurrent additions to the same file
- `brain/protocol/note-schema.md` — `derived_from` and an invalidation section

## Acceptance criteria

Met. Every new check has a positive and a negative test; the vault reports 0
errors; all new checks are warnings rather than errors.

## Verification performed

```
python brain/tools/validate_vault.py      -> 75 notes, 0 errors, 3 warnings
python brain/tools/test_validate_vault.py -> 42 tests, OK
python brain/tools/test_new_record.py     -> 14 tests, OK
```

## Evidence and artifacts

[[brain/evidence/20260821T073015Z--claude--freshness-and-quality-checks]].

## Unperformed checks

- No runtime, build, GameTest, or packaging work.
- `derived_from` is not yet set on any real note; only tested synthetically.
- Not run against a vault with nested-YAML frontmatter, which the parser now
  rejects by design.

## Known risks

- The parser is stricter than before. A note using nested YAML that previously
  mis-parsed silently will now fail outright. That is the intent, but it is a
  behaviour change for any writer relying on the old leniency.
- `VERIFICATION_UNPROVEN` checks that a command is named, not that it ran. It
  cannot detect a fabricated result and should not be read as proof.
- Freshness compares `updated` dates, so a source edited without bumping
  `updated` stays invisible.

## Lore classifications

N/A. Vault tooling only; no claim about *Shadow Slave* canon or design.

## Explicitly deferred

Left in the backlog and explicitly not taken, per the claim's lane table: the
manifest and query CLI (in progress by the other agent), executive snapshots,
new context packets, agent capability routing, the retrieval evaluation suite,
the security hygiene scan, Obsidian core-plugin views, and code-to-brain markers.

The security scan (item 12) is worth someone's attention, with one caution
recorded during this work: the vault legitimately stores absolute machine paths
in claim `worktree` fields, so a naive credential/path scan will be noisy on
valid content.

## Next safe action

Two notes remain correctly flagged stale and are not this agent's to refresh:
`current-snapshot.md` and `authority-drift-register.md` are Codex-owned,
`state: active`, and pinned to `7223e140`. Refreshing them means re-verifying
their claims about the build, which requires running gates no agent here has run.

One real defect is outstanding and belongs to its author:
`20260821T072645Z--antigravity--vault-optimizations.md` was filed with its
verification section unfilled. The warning names it; antigravity should complete
or correct it.
