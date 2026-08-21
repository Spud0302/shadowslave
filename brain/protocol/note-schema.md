---
uid: ss-protocol-note-schema
record_kind: protocol
authority: project-authority
lore_class: "N/A"
state: accepted
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
tags:
  - protocol
  - metadata
---

# Note schema

## Required properties

- uid — stable collision-resistant identifier.
- record_kind — index, protocol, lore, design, implementation, decision, evidence, idea, context, claim, handoff, log, or agent-profile.
- authority — source-canon, project-authority, proposal, evidence, or context.
- lore_class — CANON, INFERRED, DESIGN, UNKNOWN, COMPATIBILITY, mixed, or N/A.
- state — draft, proposed, accepted, active, blocked, closed, rejected, superseded, or archived.
- owner — the human or agent responsible for this record.
- created and updated — ISO dates.

## Traceability properties

Use these when relevant:

- sources — chapter references, repository paths, issues, pull requests, or URLs.
- source_commit or captured_commit — exact Git commit used.
- worktree_dirty — true or false.
- related — other note identifiers.
- implementation_links — code and asset paths.
- test_links — tests, workflows, and evidence.
- supersedes — uid of an older immutable record.
- task_id — claim shared across claim, log, evidence, and handoff.

## Authority and lifecycle are separate

Authority answers who or what can establish truth. State answers where a record is in its lifecycle. A proposal can be active without becoming project authority. Evidence can be closed without becoming a decision.

## Extending this schema

The enumerations above are validated mechanically by
`brain/tools/validate_vault.py`. Adding a value in one place and not the other
breaks the vault for every agent.

Extend a `record_kind`, `authority`, `lore_class`, or `state` only by changing
this note and the validator's matching set **in the same claim**, with a test
covering the new value. If the change alters how records are classified rather
than just adding a name, raise an ADR first.

`agent-profile` was made legal on 2026-08-21 after Codex introduced it for
[[brain/ai/agents/README|agent cards]] and the vault failed validation. Codex
then settled its own card and template on `context` instead. Both values are
therefore accepted for agent cards and the validator does not police which one
is used — picking a winner would have meant one agent's preference rewriting
three other agents' files. Andrew can settle it; until then, either is valid.

## Filename rules

- Normal concepts use short stable lowercase names.
- Agent records use YYYYMMDDTHHMMSSZ--agent--short-slug.md.
- Decisions use ADR-YYYYMMDD-short-slug.md.
- Do not rename accepted or externally linked notes merely for tidiness.

