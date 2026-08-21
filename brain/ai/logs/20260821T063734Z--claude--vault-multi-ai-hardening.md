---
uid: 20260821T063734Z-claude-vault-hardening-log
record_kind: log
authority: context
lore_class: "N/A"
state: active
owner: Claude
task_id: 20260821T063734Z-claude-vault-multi-ai-hardening
created: 2026-08-21
updated: 2026-08-21
branch: codex/combat-core-standalone
base_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
tags:
  - multi-ai
  - log
  - vault
---

# Run log — Vault hardening for multi-AI workflows

Append entries in chronological order. Only the owning agent edits this file
while active.

## 2026-08-21 06:37Z — audit

- Goal: find where the vault is soft for multi-AI use before writing anything.
- Action: read the protocol, template, index, and context notes; inspected
  `.obsidian`, `.gitignore`, and `.github/workflows`.
- Observation: nine gaps recorded, all in enforcement and lifecycle rather than
  protocol design. No workflow references `brain/` at all — the single `brain`
  match in CI was the substring inside `SmartBrainLib`.
- Correction: I first reported that `.gitignore` had no `.obsidian` coverage.
  That was wrong. `.obsidian/.gitignore` exists and correctly ignores
  `workspace.json`, caches, plugins, themes, and snippets, so
  [[brain/protocol/vault-setup]] is accurate as written. Cause: I ran `ls`
  without `-a` and never saw the dotfile.
- Next: get owner direction on sequencing before writing.

## 2026-08-21 06:37Z — owner direction

- Andrew selected: validator first then protocol gaps; local agent-invoked
  enforcement rather than a CI gate; Python.
- Assumption recorded: enforcement is local because `PROJECT-STATUS.md` reports
  hosted runners were unavailable for recent Preview Gates runs, so a CI-only
  gate could sit unexecuted. This does not preclude adding a CI job later.
- Andrew separately asked this agent to announce itself in the vault. Folded in,
  since it is the same gap as the missing agent registry.
- Action: filed
  [[brain/ai/claims/20260821T063734Z--claude--vault-multi-ai-hardening|the claim]].
  No other claim was active; the claims folder held only its README.

## 2026-08-21 06:40Z — validator

- Action: wrote `brain/tools/validate_vault.py`, zero third-party dependencies,
  Python 3.9.13 as installed on this workstation.
- Checks: required properties, enum values, ISO dates, date ordering, uid
  uniqueness, filename conventions, record placement, claim lease presence and
  expiry, `supersedes` resolution, wikilink integrity, commit resolution,
  snapshot staleness against HEAD, and Canvas node file references.
- Observation: first run reported one error against
  [[brain/implementation/authority-drift-register]] for not using the
  `YYYYMMDDTHHMMSSZ--agent--slug.md` name.
- Finding: the rule was wrong, not the note. `record_kind: evidence` covers two
  different things — a task-scoped immutable observation, which carries
  `task_id`, and a standing register with a stable name. `task_id` is the
  discriminator. Relaxed the rule rather than rename a file that three notes
  link to and that another agent owns.

## 2026-08-21 06:42Z — proving the validator

- Rationale: a validator never observed failing is not evidence.
- Action: wrote `brain/tools/test_validate_vault.py`, 24 tests, each building a
  throwaway vault with exactly one violation.
- Result: `python brain/tools/test_validate_vault.py` → 24 passed, exit 0.
- Result: `python brain/tools/validate_vault.py` → 41 notes, 0 errors,
  0 warnings, exit 0.

## 2026-08-21 06:43Z — agent registry

- Action: created `brain/ai/agents/README.md` and
  [[brain/ai/agents/claude-code|this agent's card]], covering strengths,
  weaknesses, what the agent cannot verify at all, and instruction-source
  handling.
- Note: the `codex` row reserves the slug for attribution only. That card is
  Codex's to write; this agent did not write it on its behalf.
- Observation: the validator flagged `LINK_BROKEN` on the registry table. Cause
  was a real subtlety — a wikilink alias inside a Markdown table must escape the
  pipe as `\|`, which left a trailing backslash on the captured target. The
  vault content was correct; fixed the regex and added a regression test.

## 2026-08-21 06:43Z — open protocol question

- Blocker recorded, not resolved. The claim lists
  `brain/protocol/ai-collaboration.md` and `brain/protocol/note-schema.md` as
  targets, but [[brain/protocol/ai-collaboration]] states that protocol notes are
  maintainer-owned. Listing them as targets was premature.
- Position taken: do not edit maintainer-owned protocol notes unilaterally. The
  remaining protocol gaps — lease expiry procedure, claims index, entry-point
  parity, template placeholder handling, `supersedes` on templates — will be
  raised with Andrew as proposals before any edit.
- Next: owner direction on whether to propose via ADR or apply directly under
  explicit delegation.

## 2026-08-21 06:47Z — owner direction, scope narrowed

- Andrew selected: propose via ADR rather than take delegated write access to
  maintainer-owned files.
- Andrew narrowed remaining scope to template placeholder handling only. Lease
  expiry procedure, entry-point parity, and the vault-operations context packet
  were **not** selected and are out of scope for this claim. They remain recorded
  in the session audit if he wants them later.

## 2026-08-21 06:48Z — templates, and a larger defect underneath

- Action: read the five templates not yet examined.
- Finding, larger than the placeholder issue: **every template's uid pattern
  disagrees with the vault's actual convention.** `idea.md` produces
  `2026-08-21-replace-idea-slug` where real notes use `ss-idea-<slug>`;
  `decision.md` produces `adr-2026-08-21-<slug>` where the accepted ADR uses
  `ss-adr-20260821-project-brain`. An agent following the templates faithfully
  emits a non-conforming uid every time.
- Design decision: rather than wait on template edits, `brain/tools/new_record.py`
  **derives** uid, filename, dates, and git context itself instead of trusting
  the template. It therefore emits conforming records against the templates
  exactly as they stand today, and the ADR becomes a correctness and usability
  fix rather than a blocker.

## 2026-08-21 06:48Z — scaffolder and a gap it exposed

- Action: wrote `brain/tools/new_record.py` and `test_new_record.py`.
- Observation: one test failed, and the failure was the test's fault — the
  temporary vault had no git repository, so `replace-branch` and
  `replace-full-sha` fallbacks legitimately survived. Fixed by making the
  fixture a real git repository, which tests the path that actually matters.
- Gap it exposed, worth more than the failing test: **the validator did not
  catch unsubstituted placeholders in a filed record.** A claim carrying
  `base_commit: replace-full-sha` names no commit but passed every check. Added
  a `PLACEHOLDER` error over frontmatter values, plus two regression tests.
- Confirmed: the em dash mangling seen in console output was a Windows console
  display artifact only. `test_utf8_is_preserved_in_written_file` proves the
  written file is correct UTF-8.

## 2026-08-21 06:50Z — state at pause

- Evidence: `test_validate_vault.py` 26 tests exit 0; `test_new_record.py`
  14 tests exit 0; `validate_vault.py` 44 notes, 0 errors, 0 warnings, exit 0.
- Filed [[brain/decisions/ADR-20260821-template-placeholders|the template ADR]]
  as `authority: proposal`, `state: proposed`. No template was edited.
- Nothing outside `brain/` was modified. The 63 pre-existing dirty-worktree
  entries for Combat Core and Chainback are untouched.
- Claim remains active; lease runs to 2026-08-21T14:37:34Z. Closing it requires
  an evidence record and an immutable handoff, neither of which is written yet.
