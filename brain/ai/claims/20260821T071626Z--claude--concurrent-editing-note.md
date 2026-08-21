---
uid: 20260821T071626Z-claude-concurrent-editing-note
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude
tool: claude-code-opus-5
task_id: 20260821T071626Z-claude-concurrent-editing-note
created: 2026-08-21
updated: 2026-08-21
branch: codex/combat-core-standalone
worktree: primary
base_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
lease_until: 2026-08-21T15:16:26Z
targets:
  - brain/protocol/concurrent-editing.md
  - brain/protocol/ai-collaboration.md
  - brain/tools/agent_brief.py
  - AGENTS.md
  - brain/home.md
excludes:
  - combat-core/
  - mod/
  - modpack/
  - shadowslave/
  - testserver/
  - docs/
  - brain/ai/agents/
  - brain/inbox/
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
  - concurrency
---

# Claim — Concurrent editing protocol note

> Scaffold: `python brain/tools/new_record.py claim --agent <slug> --slug <short-slug>`

## Owner intent

Andrew: *"there should probably be some sort of note about the multi-AI
concurrent file usage so that the AIs don't freak out."*

An agent that meets another agent mid-task — a claim on its file, a note that
changed under it, a failed edit — currently has nothing telling it this is
normal. The risk is not data loss but wasted work: an agent stopping, escalating,
reverting someone, or asking permission it does not need.

## Exact scope

- Create `brain/protocol/concurrent-editing.md` as the reference for what is
  normal and what is not.
- Wire it where an agent will actually hit it: `agent_brief.py` output at the
  moment of confusion, `AGENTS.md`, `ai-collaboration.md`, and `home.md`.

## Acceptance criteria

- The note answers each observable situation with an action, not a rationale.
- `agent_brief.py` names it when it reports active claims, concurrent work, or
  an expired lease.
- `python brain/tools/validate_vault.py` returns 0 errors and 0 warnings.
- No other agent's record is modified.

## Target paths

Listed in `targets`.

## Explicit exclusions

Listed in `excludes`.

## Dependencies and overlaps

No active claim named any target path at `20260821T071626Z`.

`brain/home.md` was modified on disk by another agent between this agent reading
it and editing it. The targeted edit applied cleanly to a region no one else was
writing — the documented co-editing behaviour working in practice, recorded here
because it is exactly the situation the new note describes.

## Coordination notes

`brain/home.md` and the protocol notes are maintainer-owned. Edited here under
the delegation Andrew gave on 2026-08-21, and additive in both cases: one link
line in home, one pointer paragraph in the collaboration protocol.

## Closure

Closed 2026-08-21.

- Added: [[brain/protocol/concurrent-editing]]
- Wired into `agent_brief.py`, `AGENTS.md`,
  [[brain/protocol/ai-collaboration]], and [[brain/home]]
- Verified: `validate_vault.py` 0 errors 0 warnings; `test_validate_vault.py`
  30 tests; `test_new_record.py` 14 tests

Claim closure is not proof of success.
