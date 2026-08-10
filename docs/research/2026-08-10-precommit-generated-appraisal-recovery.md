# Pre-commit generated appraisal recovery boundary — 2026-08-10

## Scope

This slice is the next bounded prerequisite for Issue #34 after merged PR #194. It does not yet claim that successful Nightmare completion is crash-atomic.

Current `main` still tears down the active Nightmare before calling `PreviewAppraisalService.appraiseWithRewards(...)`. PR #194 made the exact generated result serializable, but the recovery snapshot could only be created from a `CommittedAppraisal`, after player attachments and rewards had already been mutated. A durable completion receipt cannot safely store an exact award *before* destructive teardown if the exact award does not exist until after that teardown.

## Change

`PreviewAppraisalService` now separates generated appraisal into two boundaries:

1. `prepareWithRewards(...)` resolves the generator result and constructs the exact persistent Aspect/Flaw identity, Attribute, Memory and Echo records without requiring or mutating a `ServerPlayer`;
2. `commitPrepared(...)` applies those already-resolved records to the player and performs the Dreamer transition without rerunning the generator.

The existing `appraiseWithRewards(...)` API delegates through prepare then commit, preserving the normal runtime call surface and current successful behavior.

`GeneratedAppraisalRecoverySnapshot.fromPrepared(...)` can therefore serialize the exact award before any player attachment mutation. The future Issue #34 transaction can persist this snapshot while active Nightmare ownership is still present, then consume ownership only after durable recovery authority exists.

## Why this is separate from the receipt transaction

The older successful-completion recovery lineage was built around a fixed preview appraisal. Current `main` now has generated Aspect/Flaw/Attribute rewards plus Memory and Echo rewards, as well as newer scenario/world integration. Porting the whole older transaction while also changing where generation happens would combine two independently reviewable correctness changes.

This slice pins the required pure preparation seam first. It is useful only as a prerequisite: **the zero-appraisal crash window remains open on this branch** because `NightmareService.completePreview(...)` still performs normal exit/teardown before appraisal. The next recovery PR must persist `GeneratedAppraisalRecoverySnapshot.fromPrepared(...)` before active ownership can be consumed and replay that stored snapshot after restart rather than invoking generation again.

## Evidence classification

- **CANON:** unchanged. No Nightmare completion, appraisal, Aspect, Flaw, Attribute, Memory, Echo, progression, return or Dream Realm rule changes.
- **INFERRED:** once one appraisal result is resolved for a completed Nightmare, recovery should preserve that exact resolved result rather than resolving again against a later generator/catalogue state.
- **DESIGN:** splitting appraisal into pure preparation and player mutation is a Java transaction boundary for durable recovery.
- **UNKNOWN:** final completion-receipt schema/migration on current `main`; exact cross-file persistence ordering; physical fsync/power-loss guarantees; physical process-kill evidence at the eventual receipt boundaries.
- **COMPATIBILITY:** no player attachment or SavedData schema changes in this slice. Existing `appraise(...)` and `appraiseWithRewards(...)` callers retain their behavior by delegating through the new preparation seam.

No new lore-sensitive proposition is introduced. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, current Issue #34, current `main`, and the merged #194 recovery-snapshot note were re-read. Primary novel research is not used to invent a rule for this technical persistence refactor.

## Tests

`GeneratedAppraisalRecoverySnapshotTest` now proves that a prepared appraisal yields the complete exact recovery snapshot before any player commit and that the pre-commit snapshot is model-equivalent to the corresponding committed shape. Existing round-trip, changed-resolution stability, and malformed-payload fail-closed coverage remain.

## Resume condition / next slice

Once this branch is exact-head green and review-clean, resume Issue #34 by adding the durable successful-completion receipt on the current gameplay ancestry. The receipt must contain the prepared snapshot and complete `NightmareInstance` authority before active ownership is consumed; login replay must apply the stored records without rerunning `FirstNightmareAppraisalResolver`. Prepared-world durability #158 remains blocked under its existing recorded resume conditions and should not be retried automatically.
