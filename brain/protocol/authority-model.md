---
uid: ss-protocol-authority
record_kind: protocol
authority: project-authority
lore_class: "N/A"
state: accepted
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - docs/LORE-SOURCE-POLICY.md
  - docs/COLLABORATION.md
  - GPT_HANDOFF.md
tags:
  - protocol
  - authority
  - multi-ai
---

# Authority model

## Project authority order

When two records disagree, use this order:

1. The project owner's latest explicit direction.
2. Accepted decision records and binding owner directives that have not been superseded.
3. Current code and reproducible evidence tied to an exact commit or dirty-worktree snapshot.
4. Current authority documents such as [[PROJECT-STATUS]], [[docs/CURRENT-PREVIEW-SUMMARY]], and [[GPT_HANDOFF]].
5. Current implementation and test documentation.
6. Brain summaries and context packets.
7. Proposals, inbox notes, historical reviews, and archived plans.

Presence in the vault does not grant authority. A summary may be useful and still be stale.

## Lore authority

Lore-sensitive work must follow [[docs/LORE-SOURCE-POLICY]]. Important claims use one of:

- CANON — directly supported by verified chapter text.
- INFERRED — a reasoned synthesis of identified canon evidence.
- DESIGN — a Minecraft or project choice not supplied by canon.
- UNKNOWN — deliberately unresolved.
- COMPATIBILITY — a preservation or migration constraint rather than a lore claim.

Do not copy long novel passages into the vault. Record chapter references and concise paraphrases.

## Evidence is not a decision

An observation can prove that code behaves a certain way at a particular commit. It does not by itself authorize a new design, merge, release, canon classification, or scope expansion.

Every important evidence note should record:

- exact branch and commit;
- whether the worktree was dirty;
- environment and command or research method;
- observed result;
- artifact or log location;
- limitations and unperformed checks.

## Promotion rule

Agents create proposals by default. Only Andrew or a specifically delegated maintainer may change a proposal to project-authority or accept a decision. Acceptance should be a new decision record or an explicit owner-authored update, not a quiet frontmatter edit.

Accepted decisions and historical evidence are immutable. Corrections create a new record with a supersedes link.

## Staleness rule

Derived notes must carry updated, sources, and—where implementation state matters—source_commit or captured_commit. If current repository state differs, refresh the derived note or mark it stale before relying on it.

