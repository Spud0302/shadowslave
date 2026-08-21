---
uid: 20260821T073015Z-claude-freshness-and-quality-checks
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude
task_id: 20260821T072638Z-claude-freshness-and-quality-checks
created: 2026-08-21
updated: 2026-08-21
branch: vault/multi-ai-brain
captured_commit: acf4ed5fda81
worktree_dirty: true
sources:
  - brain/tools/validate_vault.py
  - brain/tools/test_validate_vault.py
  - brain/tools/agent_brief.py
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Freshness and quality checks

## Claim tested

That four backlog items from
[[brain/inbox/multi-ai-vault-optimization]] are implemented as mechanical checks,
and that each fires on a real violation rather than only passing.

## Environment and method

Windows 11, Python 3.9.13, no third-party packages. Branch
`vault/multi-ai-brain` at `acf4ed5fda81`, worktree dirty with unrelated Combat
Core and Chainback work, and a second agent writing the vault concurrently.

## Preconditions

`validate_vault.py` enforced schema, naming, leases, links, and placeholders. It
had no notion of derived-note freshness, verification quality, or packet size,
and its parser guessed rather than failing on input it could not represent.

## Command or research procedure

```
python brain/tools/validate_vault.py
python brain/tools/test_validate_vault.py
python brain/tools/test_new_record.py
```

## Observed result

- `test_validate_vault.py` — 42 tests, OK (was 30; 12 added).
- `test_new_record.py` — 14 tests, OK.
- `validate_vault.py` — 75 notes, 0 errors, 3 warnings, exit 0.

Implemented:

| Item | Check | Behaviour |
| --- | --- | --- |
| 11 | `DERIVED_STALE`, `DERIVED_UNRESOLVED` | warns when a `derived_from` source is newer, or unresolvable |
| 16 | parser errors | nested mappings and duplicate keys are rejected, not guessed |
| 18 | `VERIFICATION_UNPROVEN` | warns when a handoff's verification section names no command |
| 19 | `PACKET_OVERSIZE` | warns past ~8000 characters, a proxy for the 2000-token ceiling |

Also corrected `agent_brief.py`, whose stale-notes section still carried the
pre-`acf4ed5f` logic and reported closed evidence records as stale.

## The item-18 check found a real defect on first run

`brain/ai/handoffs/20260821T072645Z--antigravity--vault-optimizations.md` was
filed with its *Verification performed* section still containing the template's
instructional text and no command. The handoff asserts work was done; nothing in
it records what was run.

This is a true positive and the exact case the check exists for. It was **not**
corrected here: a handoff is a one-writer record belonging to antigravity, and
the warning surfaces it to its owner rather than having another agent edit it.

## Artifacts, hashes, logs, or chapter references

The tools and their tests are the artifacts. No build artifacts produced.

## Limitations and unperformed checks

- **`derived_from` has no users yet.** The property is declared, validated, and
  tested, but no existing note sets it. Adoption belongs with the owners of the
  derived notes; `brain/ai/context/` was excluded from this claim because a
  second agent was working there.
- Staleness compares `updated` dates only. A source edited without bumping
  `updated` is invisible to it.
- `VERIFICATION_UNPROVEN` tests for the *presence* of a command, not whether it
  was actually run or whether its output is real. It raises the floor; it cannot
  detect a fabricated result.
- `PACKET_OVERSIZE` counts characters, not tokens. It is a proxy.
- The parser now rejects nested mappings rather than mis-parsing them. Any note
  legitimately needing nested YAML would have to be restructured.
- No runtime, build, GameTest, or packaging work was run. Nothing here concerns
  Combat Core, Chainback, or preview.4.

## Conclusion

Four backlog items are enforced mechanically, each with a test proving it fires.
One found a real unfinished handoff on its first run against live data.

An observation proves behaviour at one commit. It does not authorize a design,
merge, release, canon classification, or scope change.
