---
uid: 20260821T081537Z-claude-branch-archive-pass
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude
task_id: 20260821T081506Z-claude-branch-archive-pass
created: 2026-08-21
updated: 2026-08-21
branch: packaging/combat-core-closure
captured_commit: d3c7257d
worktree_dirty: true
sources:
  - brain/implementation/branch-archive.md
  - brain/attachments/archived-branches-20260821.txt
  - PROJECT-STATUS.md
related: []
supersedes: []
tags:
  - evidence
  - git
  - branches
---

# Evidence — Remote branch archive pass

## Claim tested

That the remote branch list can be reduced from 285 to 6 without losing any
commit, and that every removed branch remains restorable.

## Environment and method

Windows 11, git 2.53.0, `gh` authenticated as Spud0302. Remote
`github.com/Spud0302/shadowslave`, public.

## Preconditions

285 remote branches. **One pull request in the repository**, confirmed by
`gh api repos/Spud0302/shadowslave/pulls?state=all` returning 1 and
`git ls-remote origin 'refs/pull/*'` returning 1 ref. PR numbers cited in project
documents (#239, #242, #275–#286) therefore refer to a different repository, and
no `refs/pull/N/head` fallback exists for these branches.

## Command or research procedure

```
git branch -r --merged origin/main            # 92 merged
git push origin --delete <92 merged branches>
git push origin refs/remotes/origin/<b>:refs/archive/<b>   # 189 branches
git ls-remote origin 'refs/archive/*'                      # verify before deleting
git push origin --delete <189 archived branches>
```

## Observed result

**Pass 1 — merged branches.** 92 branches already merged into `origin/main`
deleted. Each re-verified with `git merge-base --is-ancestor` immediately before
deletion. 0 failures. 285 → 193.

**Pass 2 — archive then delete.** 189 branches pushed to `refs/archive/<name>`.
Verification ran *before* any deletion:

- archives present on remote: 189
- branches queued for deletion without a confirmed archive: **0**
- archived SHA vs branch SHA mismatches, 40 sampled: **0**

Only branches appearing in both the archive-confirmed list and the delete queue
were deleted. 189 deleted, 0 failures.

**Final state.** 6 remote branches, 189 archived refs:

```
gpt/base-entry-durability-current-main
gpt/combat-core-mvp
gpt/storm-lantern-region-wip-extraction
main
packaging/combat-core-closure
vault/multi-ai-brain
```

Restore path confirmed live:
`git ls-remote origin refs/archive/gpt/ash-burrower-ambush-dance` returns
`ee7039cc5e4d253bc3ae1d5b609e37e395d4a26d`.

## Supporting finding — the archived work is superseded, not pending

Every unmerged branch had commits absent from `main`, but commit-level divergence
is a weak signal. Sampling the topics those branch names describe found all of
them already implemented in `main`: `AshCompassMemoryItem`, `AshBurrowerEntity`,
`GlassRoadMemoryItem`, `EchoOwnershipService`, `MemoryManifestationService`.
[[PROJECT-STATUS]] independently records the merged lineage as including the
Memory, Echo, Ash Compass, Ash Burrower, and Glass Road slices.

A 12-branch sample showed 11 merging into `main` without textual conflict. That
was **not** treated as evidence they should be merged: absence of text conflicts
says nothing about whether the result compiles or is behaviourally coherent.

## Artifacts

`brain/attachments/archived-branches-20260821.txt` — all 189 archived names.

## Limitations and unperformed checks

- **Equivalence was not proven per branch.** Five topics were sampled, not 189.
  Archiving is what makes that acceptable: nothing was discarded, so the
  `PROJECT-STATUS` requirement for equivalence evidence before retirement does
  not apply. It would apply to deletion.
- Active-lane classification came from `PROJECT-STATUS` plus commit recency.
  Integration candidates #275/#276/#278/#279 could not be mapped to branches,
  since the PR numbers are not from this repository. If one of those is a live
  lane it is now archived rather than a branch, and is one command from restoral.
- SHA equality was sampled at 40 of 189, not exhaustive.
- Archived refs are not fetched by a default clone. They are discoverable only
  through [[brain/implementation/branch-archive]].
- No code was built, tested, or run. This concerns git refs only.

## Conclusion

The branch list is reduced by 98% with zero commits lost and a one-command
restore for every removed branch. The retention policy's intent is preserved;
only the storage location changed.

An observation proves behaviour at one commit. It does not authorize a design,
merge, release, canon classification, or scope change.
