---
uid: ss-implementation-branch-archive
record_kind: implementation
authority: project-authority
lore_class: "N/A"
state: active
owner: Andrew
recorded_by: Claude
created: 2026-08-21
updated: 2026-08-21
sources:
  - PROJECT-STATUS.md
  - brain/evidence/20260821T081506Z--claude--branch-archive-pass.md
tags:
  - git
  - branches
  - archive
---

# Archived branches — how to find and restore them

> [!important] The branches were not deleted. They were moved.
> On 2026-08-21 the remote went from **285 branches to 6**. The other 189 were
> pushed to `refs/archive/<name>` before their `refs/heads/<name>` was removed.
> Every commit is still reachable and restorable. Nothing was discarded.

## Why they are not in the branch list

A `refs/archive/*` ref keeps its commits alive permanently and is fetchable, but
GitHub does not display it in the branch UI and `git clone` does not fetch it by
default. That is the point: the clutter is gone, the history is not.

This mattered here because **this repository contains only one pull request.**
The PR numbers throughout the project documents — #239, #242, #275–#286 — belong
to a different repository whose history was pushed in. There is no
`refs/pull/N/head` to recover a deleted branch from, so an ordinary branch
deletion here would have been permanent.

## List the archive

```bash
git ls-remote origin 'refs/archive/*'
```

## Fetch every archived ref locally

```bash
git fetch origin 'refs/archive/*:refs/archive/*'
git log --oneline refs/archive/gpt/ash-burrower-ambush-dance
```

## Restore one as a working branch

```bash
git push origin refs/archive/gpt/ash-burrower-ambush-dance:refs/heads/gpt/ash-burrower-ambush-dance
```

One command, no history rewriting, and the archived ref stays in place.

## What was kept as a live branch

| Branch | Why |
| --- | --- |
| `main` | trunk |
| `gpt/combat-core-mvp` | active lane #282, Combat Core |
| `gpt/storm-lantern-region-wip-extraction` | active lane #283, provider extraction |
| `gpt/base-entry-durability-current-main` | active lane #284, entry durability |
| `vault/multi-ai-brain` | project brain and tooling |
| `packaging/combat-core-closure` | modpack dependency closure |

## Why archiving rather than merging

The archived branches are **superseded, not pending**. They appear unmerged
because their work reached `main` by a different route — squashed, rebased, or
reimplemented — not because the functionality is missing. `AshCompassMemoryItem`,
`AshBurrowerEntity`, `GlassRoadMemoryItem`, `EchoOwnershipService`, and
`MemoryManifestationService` all exist in `main` today, and
[[PROJECT-STATUS]] records the merged lineage as already including the Memory,
Echo, Ash Compass, Ash Burrower, and Glass Road slices.

Merging them would re-apply superseded implementations over the current ones and
reopen exactly the base-breadth that
[[docs/design/MODULAR-JAR-BOUNDARIES]] exists to prevent.

## Why this satisfies the retention rule

`PROJECT-STATUS.md` sets the bar for retiring a branch: supersede or retire only
after ancestry or equivalence evidence proves the successor contains the needed
behaviour.

That evidence was **not** produced for all 189 — doing so properly would mean
189 code reviews. Archiving makes it unnecessary. Nothing was discarded, so no
equivalence proof is required, and any branch can be restored the moment someone
wants to do that review.

The retention policy's intent — "useful implementation/history remains available
for provider/WIP ports" — is fully preserved. Only the storage location changed.
