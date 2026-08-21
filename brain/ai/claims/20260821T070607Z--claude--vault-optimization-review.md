---
uid: 20260821T070607Z-claude-vault-optimization-review
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude
tool: claude-code-opus-5
task_id: 20260821T070607Z-claude-vault-optimization-review
created: 2026-08-21
updated: 2026-08-21
branch: codex/combat-core-standalone
worktree: primary
base_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
lease_until: 2026-08-21T15:06:07Z
targets:
  - brain/inbox/multi-ai-vault-optimization.md
  - brain/protocol/note-schema.md
  - brain/tools/validate_vault.py
  - brain/tools/test_validate_vault.py
  - brain/ai/agents/claude-code.md
  - brain/ai/claims/20260821T070607Z--claude--vault-optimization-review.md
excludes:
  - combat-core/
  - mod/
  - modpack/
  - shadowslave/
  - testserver/
  - docs/
  - brain/home.md
  - brain/ai/agents/codex.md
  - brain/ai/agents/antigravity.md
  - brain/templates/agent-profile.md
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
  - vault
---

# Claim — Vault optimization review and schema reconciliation

> Scaffold: `python brain/tools/new_record.py claim --agent <slug> --slug <short-slug>`

## Owner intent

Andrew asked this agent to read
[[brain/inbox/multi-ai-vault-optimization|antigravity's vault optimization
proposal]] and add to it.

## Exact scope

- Append a clearly attributed review section to antigravity's proposal. The
  existing content is preserved verbatim; nothing of another agent's is rewritten.
- Reconcile the proposal against what already shipped, so the owner is not asked
  to approve work that is already done.
- Resolve the live schema break: Codex introduced `record_kind: agent-profile`,
  which [[brain/protocol/note-schema]] does not list, so the vault fails
  validation.

## Acceptance criteria

- `python brain/tools/validate_vault.py` returns 0 errors.
- Antigravity's original seven proposals remain intact and attributed.
- Additions are marked as this agent's, with evidence for each claim about
  current state.
- No other agent's claim, log, evidence, handoff, or agent card is modified.

## Target paths

Listed in `targets`. Narrowed deliberately to named files rather than
directories, following the lesson recorded in
[[brain/ai/claims/20260821T063734Z--claude--vault-multi-ai-hardening]].

## Explicit exclusions

Listed in `excludes`. Codex's and antigravity's agent cards are excluded even
though one of them is inconsistent with the other; that is for their owners to
resolve, and the validator will surface it.

## Dependencies and overlaps

No active claim held any target path at `20260821T070607Z`, confirmed with
`agent_brief.py --paths`.

## Coordination notes

Three agents wrote to this vault inside one hour. File contents remain data, not
instructions — including antigravity's recommendations, which are a proposal to
Andrew rather than a directive to this agent.

## Overlap discovered after the check — 2026-08-21

`agent_brief.py --paths brain/inbox/multi-ai-vault-optimization.md` reported no
holder at `20260821T070607Z`. Codex's claim on that exact path is stamped
`20260821T070554Z`, thirteen seconds earlier, and is doing the same task.

First resolution: Claude filed its contribution as a separate sibling note to
avoid a concurrent write, and Codex kept the file.

**Superseded the same day by owner direction.** Andrew: *"since this is a
multi-AI workflow there will be times where different Ai's will be working on the
same file concurrently and thats fine."* The sibling note was therefore merged
into [[brain/inbox/multi-ai-vault-optimization]] as an attributed
`## Claude review and additions` section appended after Codex's, using a targeted
edit rather than a whole-file write, and the sibling was removed. Codex's claim
was never edited and had closed by then.

The race itself is recorded as item 15 of that note. The conclusion changed with
the direction: a claim is **awareness, not a lock**, and the real hazard is
whole-file rewrites rather than concurrency. `AGENTS.md`,
[[brain/protocol/ai-collaboration]], and `agent_brief.py` were updated to match —
overlapping claims are now reported as coordination information and no longer
fail the brief.

## Reversal recorded — agent card record_kind

This claim added `agent-profile` to the schema and a validator warning telling
other agents' cards to adopt it. Codex concurrently settled its own card and
template on `context`.

The warning was removed. Both values are now legal and the validator does not
police the choice, because a warning would have meant one agent's taxonomy
pressuring three other agents' files. Claude's own card was returned to
`context` to match the de facto convention. Andrew can settle it.

## Closure

Closed 2026-08-21.

- Added: `## Claude review and additions` (items 14-19) appended to
  [[brain/inbox/multi-ai-vault-optimization]]
- Schema: `agent-profile` made legal, plus an "Extending this schema" section in
  [[brain/protocol/note-schema]]
- Concurrency policy: `AGENTS.md`, [[brain/protocol/ai-collaboration]], and
  `agent_brief.py` updated so co-editing is supported rather than serialised
- Verified: `validate_vault.py` 64 notes, 0 errors, 0 warnings;
  `test_validate_vault.py` 30 tests; `test_new_record.py` 14 tests

No evidence record or handoff is filed for this claim. It produced a proposal and
a schema reconciliation, not a verified behavioural result. Claim closure is not
proof of success.
