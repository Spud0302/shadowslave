---
uid: ss-adr-20260821-template-placeholders
record_kind: decision
authority: project-authority
lore_class: "N/A"
state: accepted
owner: Claude
decision_owner: Andrew
task_id: 20260821T063734Z-claude-vault-multi-ai-hardening
created: 2026-08-21
updated: 2026-08-21
sources:
  - brain/templates/task-claim.md
  - brain/templates/handoff.md
  - brain/templates/evidence.md
  - brain/templates/decision.md
  - brain/protocol/note-schema.md
  - .obsidian/templates.json
supersedes: []
tags:
  - decision
  - multi-ai
  - templates
---

# ADR — Template tokens and uid conventions for direct-writing agents

## Context

The templates under `brain/templates` were written for creation through
Obsidian's Templates plugin. Agents write files directly, and three concrete
defects follow.

**1. Obsidian placeholders do not expand for direct writers.** `{{date}}`,
`{{time}}`, and `{{title}}` are substituted by the Templates plugin at creation
time. An agent copying the template writes the literal text, putting
`created: {{date}}` into frontmatter — not a date.

**2. The uid patterns in every template disagree with the vault.** This is the
larger defect and was not visible until the templates were compared against real
notes:

| Template | uid it produces | uid the vault actually uses |
| --- | --- | --- |
| `idea.md` | `2026-08-21-replace-idea-slug` | `ss-idea-<slug>` |
| `lore-entity.md` | `2026-08-21-replace-lore-slug` | `ss-lore-chainback` |
| `feature.md` | `2026-08-21-replace-feature-slug` | `ss-design-combat-v1` |
| `ai-context-packet.md` | `2026-08-21-replace-context-slug` | `ss-context-combat-v1` |
| `decision.md` | `adr-2026-08-21-<slug>` | `ss-adr-20260821-project-brain` |
| `task-claim.md` | `{{date}}-replace-agent-task-slug` | `20260821T062438Z-codex-vault-bootstrap-handoff` |

An agent following the templates faithfully produces non-conforming uids in
every case.

**3. `.obsidian/templates.json` cannot produce agent-record stamps.** Its
`dateFormat` is `YYYY-MM-DD`, so `{{date}}` can never yield the
`YYYYMMDDTHHMMSSZ` that [[brain/protocol/note-schema|the note schema]] requires
for claim, log, handoff, and task-scoped evidence filenames. Raising the format
is not a fix either, because the same token is also used for `created:` and
`updated:`, which must stay plain dates.

Separately, `handoff.md` and `evidence.md` have no `supersedes` field, although
[[brain/protocol/ai-collaboration|the collaboration protocol]] makes supersede
chains the only sanctioned way to correct an immutable record.

## Options considered

**A. Keep Obsidian tokens, add agent instructions to each template.** Rejected.
It leaves two substitution styles in one file, which is the present defect, and
it does not touch the uid mismatch.

**B. Raise `dateFormat` to a full timestamp.** Rejected. It corrupts `created:`
and `updated:`, which the schema defines as ISO dates.

**C. Unify on the `replace-*` convention the templates already use, and fix the
uid patterns.** Proposed below. The templates already use `replace-agent`,
`replace-branch`, `replace-full-sha`, and `replace-iso-timestamp`; the Obsidian
tokens are the inconsistent minority.

## Proposed decision

**C1.** In all nine templates, replace `{{date}}` with `replace-iso-date`,
`{{time}}` with `replace-iso-time`, and `{{title}}` with `replace-title`.

**C2.** Correct each template's uid to the convention the vault already follows:

- concept notes — `ss-<record_kind>-replace-slug`
- decisions — `ss-adr-replace-compact-date-replace-slug`
- agent records — `replace-stamp-replace-agent-replace-slug`

**C3.** Add `supersedes: []` to `handoff.md` and `evidence.md`.

**C4.** Add one line to each template pointing at `brain/tools/new_record.py`.

## Already implemented, and not requiring this decision

These landed under the same claim and touch no maintainer-owned file:

- `brain/tools/new_record.py` scaffolds records with correct uid, filename,
  dates, git context, and lease. It **derives these itself rather than trusting
  the template**, so it already emits conforming records against the templates
  exactly as they stand today. This ADR is therefore a correctness and
  human-usability fix, not a blocker.
- `brain/tools/validate_vault.py` now rejects unsubstituted `{{...}}` and
  `replace-*` scaffolding in any filed record, so defect 1 cannot reach a handoff
  undetected whether or not this ADR is accepted.
- `brain/tools/test_new_record.py` asserts that every generated record kind
  passes validation, which will catch a template drifting out of conformance.

## Consequences

- Obsidian users lose automatic date and title fill in these templates. The
  effect is small: six of the eight required properties already needed manual
  entry, and the scaffolder is the recommended path for both audiences.
- One substitution convention across every template, so an agent has one rule.
- Generated uids match existing notes, so `ss-`-prefixed search and Obsidian
  graph grouping stay coherent.
- The `supersedes` field becomes visible at authoring time rather than something
  a writer must remember during a correction.

## Evidence

- `python brain/tools/test_validate_vault.py` → 26 tests, exit 0.
- `python brain/tools/test_new_record.py` → 14 tests, exit 0.
- `python brain/tools/validate_vault.py` → 43 notes, 0 errors, 0 warnings.
- Captured at branch `codex/combat-core-standalone`, commit `7223e140d625`,
  worktree dirty with unrelated Combat Core and Chainback work.

## Lore classification

N/A. This decision concerns vault tooling only and makes no claim about
*Shadow Slave* canon or game design.

## Migration or supersession

No existing note changes. The corrected uid patterns are already what current
notes use, so nothing needs renaming, and no accepted decision, evidence record,
handoff, or another agent's claim is rewritten.

## Owner response

**Accepted 2026-08-21 under explicit owner delegation.** Andrew directed, in
session: *"since this vault will be mainly for AI's optimise it how you see
fit."* That is the delegation
[[brain/protocol/authority-model|the authority model]] requires for a maintainer
other than Andrew to promote a proposal.

The promotion is recorded here rather than made as a quiet frontmatter edit, so
it stays auditable and reversible. Andrew may reverse it with a superseding
record; the templates carry no data and reverting them is a content change only.

Applied the same day: C1 through C4 across all nine templates in
`brain/templates`. Verified by `python brain/tools/test_new_record.py`
(14 tests, exit 0), whose `test_every_kind_passes_validation` scaffolds every
record kind from the migrated templates and validates the result.
