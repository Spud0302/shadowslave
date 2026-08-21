---
uid: 20260821T072638Z-claude-freshness-and-quality-checks
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude
tool: claude-code-opus-5
task_id: 20260821T072638Z-claude-freshness-and-quality-checks
created: 2026-08-21
updated: 2026-08-21
branch: vault/multi-ai-brain
worktree: primary
base_commit: acf4ed5fda81
lease_until: 2026-08-21T15:26:38Z
targets:
  - brain/tools/validate_vault.py
  - brain/tools/test_validate_vault.py
  - brain/tools/agent_brief.py
  - brain/tools/README.md
  - brain/protocol/note-schema.md
excludes:
  - brain/ai/context/
  - brain/inbox/
  - brain/ai/agents/
  - brain/home.md
  - brain/decisions/
  - AGENTS.md
  - combat-core/
  - mod/
  - modpack/
  - shadowslave/
  - testserver/
  - docs/
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
  - vault
  - tooling
---

# Claim — Freshness and quality checks in validate_vault

> Scaffold: `python brain/tools/new_record.py claim --agent <slug> --slug <short-slug>`

## Owner intent

Andrew: *"start working on the vault optimisations, I'll have another agent
running the same task alongside."*

## Lane declaration — read this if you are the other agent

This claim takes **the mechanical enforcement lane only**: checks inside
`validate_vault.py` and `agent_brief.py`, plus the schema properties they
require. It is the smallest coherent slice of the optimisation backlog that is
testable rather than editorial.

**Claimed here:**

| Item | Source | What lands |
| --- | --- | --- |
| 11 | Codex | `derived_from` dependency invalidation |
| 16 | Claude | parser fails loudly instead of mis-parsing |
| 18 | Claude | verification-command advisory check on handoffs |
| 19 | Claude | measured token budget for context packets |

**Deliberately left for you, and excluded above:**

- Item 1 / A — `manifest.json` and its provenance design.
- Item 2 / B — executive snapshot blocks.
- Item 4 — new context packets (`vault-operations`, `dream-realm-slice`,
  `echo-memory-pipeline`, `modpack-packaging`). `brain/ai/context/` is excluded.
- Item 8 — agent capability registry and task routing.
- Item 10 — retrieval evaluation suite.
- Item 12 — security and privacy hygiene scan. Genuinely valuable and
  deliberately not taken; note that the vault already stores absolute paths in
  claim `worktree` fields, so a naive scan will be noisy on legitimate content.
- Item 13 — Obsidian core-plugin operational views.
- Items 3 / D, 7 — query CLI beyond `agent_brief.py --json`, and code-to-brain
  markers.

If you need a file listed in `targets`, take it. Overlap is supported: make
targeted edits, append attributed sections, never rewrite these files wholesale,
and record the overlap in your own claim. See
[[brain/protocol/concurrent-editing]].

## Exact scope

1. `derived_from` — declare it in the schema; warn when a source note's
   `updated` is newer than the derived note's.
2. Parser hardening — `parse_frontmatter` returns an error on structures it
   cannot represent rather than guessing, since a silent mis-parse previously
   made a held path look free.
3. Handoff verification — advisory warning when a handoff's *Verification
   performed* section contains no command.
4. Context packet size — warning past a character-count proxy for the 2,000
   token ceiling.
5. Fix `agent_brief.py`'s stale-notes section, which still duplicates the
   pre-`acf4ed5f` logic and reports closed evidence records as stale.

## Acceptance criteria

- Every new check has a test proving it fires on the positive case. A checker
  that only passes is not evidence.
- `validate_vault.py` reports 0 errors on the current vault.
- New checks are warnings, not errors. A false failure trains agents to ignore
  the tool.
- No note owned by another agent is edited.

## Target paths

Listed in `targets`.

## Explicit exclusions

Listed in `excludes`, and enumerated by item in the lane declaration above.

## Dependencies and overlaps

No active claim at `20260821T072638Z`. A second agent is working the same
backlog concurrently by owner arrangement; the lane table above is the
coordination mechanism.

## Coordination notes

Two notes are correctly flagged stale and are **not** in scope here:
`current-snapshot.md` and `authority-drift-register.md` are Codex-owned, are
`state: active`, and are pinned to `7223e140`. Refreshing them means
re-verifying their claims about the build, which requires running gates this
agent has not run.

## Closure

Closed 2026-08-21.

- Evidence: [[brain/evidence/20260821T073015Z--claude--freshness-and-quality-checks]]
- Handoff: [[brain/ai/handoffs/20260821T073015Z--claude--freshness-and-quality-checks]]

The lane declaration worked without further coordination: the other agent took
items 1 and 3 and added `build_manifest.py` and `query_vault.py`. Both agents
edited `brain/tools/README.md` concurrently; the targeted edits merged cleanly
and neither contribution was lost.

Claim closure is not proof of success.
