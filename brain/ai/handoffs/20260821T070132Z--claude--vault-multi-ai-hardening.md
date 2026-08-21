---
uid: 20260821T070132Z-claude-vault-multi-ai-hardening
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude
task_id: 20260821T063734Z-claude-vault-multi-ai-hardening
created: 2026-08-21
updated: 2026-08-21
branch: codex/combat-core-standalone
base_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
head_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
worktree_dirty: true
sources:
  - brain/evidence/20260821T070131Z--claude--vault-multi-ai-hardening.md
  - brain/decisions/ADR-20260821-template-placeholders.md
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Vault hardening for multi-AI workflows

## Owner intent and scope

Andrew directed work to solidify the vault for multi-AI workflows, asked this
agent to announce itself, and then widened scope: *"since this vault will be
mainly for AI's optimise it how you see fit."* That wording is the explicit
delegation used to promote the template ADR and to edit maintainer-owned
protocol and entry-point files.

## Outcome

The vault moved from convention-only to mechanically enforced, and the cost of
starting a task correctly dropped from roughly eight file reads plus manual
directory inspection to one command.

## Files changed

New:

- `brain/tools/validate_vault.py`, `new_record.py`, `agent_brief.py`
- `brain/tools/test_validate_vault.py`, `test_new_record.py`
- `brain/tools/README.md`
- `brain/ai/agents/README.md`, `brain/ai/agents/claude-code.md`
- `brain/decisions/ADR-20260821-template-placeholders.md`
- this task's claim, log, evidence, and handoff

Modified:

- all nine templates in `brain/templates` (ADR C1–C4)
- `AGENTS.md` — now the single dense operational protocol
- `CLAUDE.md`, `.github/copilot-instructions.md` — reduced to pointers
- `brain/protocol/ai-collaboration.md` — tool-backed pre-write steps, a claim
  lease section, narrowest-claim guidance

Nothing under `combat-core/`, `mod/`, `modpack/`, `shadowslave/`, `testserver/`,
or `docs/` was touched. The pre-existing dirty-worktree work is intact.

## Acceptance criteria

Met: the validator runs on stock Python 3.9 with no packages, exits non-zero on
real violations, reports rather than repairs, and no accepted decision,
historical evidence, or other agent's record was rewritten.

## Verification performed

- `python brain/tools/validate_vault.py` → 54 notes, 0 errors, 0 warnings, exit 0
- `python brain/tools/test_validate_vault.py` → 28 tests, OK
- `python brain/tools/test_new_record.py` → 14 tests, OK
- `python brain/tools/agent_brief.py --paths <held path>` → collision, exit 1

## Evidence and artifacts

[[brain/evidence/20260821T070131Z--claude--vault-multi-ai-hardening]], which
also records the three defects found and fixed during the work.

## Unperformed checks

- No runtime, build, GameTest, packaging, client, or dedicated-server work.
- No Obsidian UI render check of the migrated templates.
- Tools exercised on one OS, one Python version, one commit.
- No CI job runs any of this; enforcement is local by design.

## Known risks

- **The `agent_brief.py` collision check sees paths, not concepts.** Two agents
  can still collide on meaning while touching different files.
- Local enforcement can be skipped by an agent that does not run it. `AGENTS.md`
  instructs it; nothing compels it.
- `AGENTS.md` is now load-bearing for three tools. Its accuracy matters more
  than before, and it can drift from `brain/protocol/` if edited carelessly.
- The minimal frontmatter parser handles the vault's flat scalar and list subset
  only. Nested YAML would parse wrongly rather than error — the block-sequence
  defect proved this class of failure is silent.

## Lore classifications

N/A throughout. This work concerns vault tooling and made no claim about
*Shadow Slave* canon, mechanics, or game design.

## Explicitly deferred

- The P0 findings in [[brain/implementation/authority-drift-register]]: modpack
  dependency closure omitting Combat Core, and modpack CI booting a fixture
  rather than the assembled pack.
- A `vault-operations` context packet; Andrew deselected it and `AGENTS.md` now
  covers the same ground more cheaply.
- Making the agent registry table generated rather than hand-edited. It is a
  shared append-only file and therefore a contention point — two agents wrote it
  during this session. It resolved cleanly, so the change was not forced through
  while another claim was live.
- Any CI gate for `brain/`.

## Next safe action

Run `python brain/tools/agent_brief.py`. For substantive project work, the
highest-value open item remains the release-QA P0 pair, using the
[[brain/ai/context/release-qa|release-qa packet]] — not more vault work.

An unresolved question worth Andrew's attention: **Codex has no agent card.** Its
slug is reserved for attribution, but only Codex should write that card, and it
authored the vault bootstrap before the registry existed.
