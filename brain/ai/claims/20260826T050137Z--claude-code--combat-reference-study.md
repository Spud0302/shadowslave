---
uid: 20260826T050137Z-claude-code-combat-reference-study
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code
task_id: 20260826T050137Z-claude-code-combat-reference-study
created: 2026-08-26
updated: 2026-08-26
branch: claude/shadow-slave-github-connect-x6nqra
worktree: primary
base_commit: b4de24b6be17d244d0bf73c24fcbcec7afa75e6c
lease_until: 2026-08-26T09:01:37Z
targets:
  - brain/inbox/combat-reference-study.md
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Reference study: mods to learn combat feel from

## Owner intent

Owner request, 2026-08-26: record the code-reuse/licensing discussion and
research which mods are worth *reading* to inform our own combat implementation.

Owner clarification, same session: "we aren't copying the code or rewriting it,
we are looking at how they have done something then writing our own."

## Exact scope

One inbox idea note. No code, no manifest change, no dependency added.

## Target paths

- `brain/inbox/combat-reference-study.md`

## Explicit exclusions

- `modpack/manifest.json` and `validate_manifest.py` — the license-allowlist
  proposal is recorded, not implemented. Owner decision first.
- Adding any third-party mod as a dependency.
- `combat-core/**`.

## Acceptance criteria

- Licenses stated in the note are verified against each project's own
  repository, not against aggregator or search-result summaries.
- Anything unverified is marked as such.

## Coordination notes

Licenses were read from GitHub's licence API or each repo's own README.
`api.modrinth.com` and `modrinth.com` are blocked by this sandbox's egress
proxy, so no Modrinth metadata was used. One search result asserting a license
was checked and found wrong (see the note), which is why the acceptance
criterion above exists.

## Dependencies and overlaps

None.

## Closure

Closed 2026-08-26 by claude-code on filing the note. Research only; authorizes
nothing.
