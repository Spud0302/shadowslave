# Preview Gates pull-request trigger coverage

**Date:** 2026-08-11  
**Related correctness work:** Issue #34, PRs #223 and #228

## Observed failure

Current `main` configures the `Preview gates` workflow to handle only `pull_request` events `ready_for_review` and `reopened`.

That leaves two ordinary review paths without automatic exact-head validation:

1. a pull request created already review-ready emits `opened`, not `ready_for_review`;
2. a later corrective push to an existing pull request emits `synchronize`.

PR #223 exact head `37b7480c8a33904c4c88f10b27090ce1fe23eb7d` and stacked PR #228 exact head `10d7d0f7a79d939f7fa1b0c7cf37c82383cb082b` both currently have no registered workflow run. Repeatedly checking or pushing unrelated no-op changes cannot correct an event subscription that excludes the relevant events.

## Correction

The workflow now subscribes to the normal reviewable-PR lifecycle events:

- `opened`;
- `synchronize`;
- `ready_for_review`;
- `reopened`.

Existing path filters remain unchanged, so documentation-only changes still do not start the expensive Java/datapack gates unless another watched path changes.

A focused standard-library Python regression test reads `.github/workflows/java-core.yml` and requires all four events. The Java CI job runs that contract test immediately after checkout so later edits cannot silently remove initial-head or corrected-head coverage.

## Why this precedes another persistence slice

Issue #34's active restart evidence cannot be called exact-head green until CI actually registers on its reviewable heads. The missing-run condition repeated across consecutive loop runs, and the repository's stop-retrying rule requires a changed approach rather than another blind check or timeout/rerun attempt.

Fixing the trigger contract is therefore a correctness-enabling CI slice, not new gameplay content. Once merged, a substantive update to #223/#228 (or a deliberate close/reopen/ready transition where appropriate) can produce fresh validation under the corrected workflow.

## Evidence classification

- **CANON:** not applicable; no Shadow Slave mechanic changes.
- **INFERRED:** none.
- **DESIGN:** every reviewable initial PR head and every later corrected PR head touching gated paths should receive Preview Gates automatically.
- **UNKNOWN:** GitHub-hosted runner availability and unrelated transient Minecraft/Mineflayer stalls remain external execution risks.
- **COMPATIBILITY:** runtime Java, datapack behavior, persistence schemas, lore semantics, catalogue state and gameplay are unchanged. Existing `workflow_dispatch`, `main` push triggers, path filters, jobs and gate assertions are preserved.

## Lore/source boundary

`docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read. This is CI infrastructure only. It introduces no lore-sensitive proposition and therefore does not manufacture primary-novel evidence for a technical workflow rule.

## Limits and next step

This change makes qualifying PR heads eligible to run CI; it does not itself prove #223 or #228 green, and it does not solve the separately recorded hosted vanilla keepalive/runner-lag symptom.

After this trigger fix is exact-head green and merged, resume Issue #34 by obtaining fresh exact-head validation for the active disk-image evidence line. Only after that line is green/review-clean should the next stronger correctness slice be the dedicated NeoForge stop/kill-and-restart harness using actual server world/player persistence.
