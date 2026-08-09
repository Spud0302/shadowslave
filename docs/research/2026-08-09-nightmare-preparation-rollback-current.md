# Current-lineage Nightmare preparation rollback audit

**Date:** 2026-08-09

## Finding

The green #133 lineage still entered the Last Signal scenario through `NightmareService.tryEnter(...)`, which registers a new `NightmareInstance` before `LastSignalScenario.prepare(...)` mutates the Nightmare dimension. The service catch path can remove registry ownership and restore Soul state, but the pre-prepare snapshot has no authoritative layout or pursuer identity.

`LastSignalScenario.prepare(...)` cleared and rebuilt the allocated slot before pursuer creation/addition could fail. A failure at either pursuer boundary could therefore leave ownerless geometry after the service rolled back the registry record. Monotonic slot allocation prevents supported future instances from overlapping that same slot, but it does not make the leaked world mutation correct.

Historical PR #67 previously audited this exact defect. This current-lineage port adds only that bounded preparation transaction rather than importing later failed-entry/teleport fixes in the same review unit.

## Current correction

Scenario preparation now runs through a small fail-closed transaction. If preparation throws:

1. a created pursuer is discarded when present;
2. the allocated slot volume is cleared;
3. all rollback steps are attempted even if an earlier rollback step fails;
4. the original preparation failure remains the primary exception and rollback failures are attached as suppressed evidence;
5. `NightmareService.tryEnter(...)` then continues its existing registry/Soul rollback.

Successful preparation is unchanged and returns the same layout/pursuer-bearing `NightmareInstance`.

## Evidence classification

- **CANON:** unchanged. This slice does not alter Nightmare role, resolution, appraisal, progression, death, return, Aspect, Flaw, Seed, or Dream Realm rules.
- **INFERRED:** unchanged one-instance ownership of technical scenario state while a First Nightmare is active.
- **DESIGN:** Last Signal world preparation is transactional; failed preparation must not leave ownerless scenario geometry or an attempted pursuer. Failed slots remain consumed rather than being reused.
- **UNKNOWN:** live NeoForge failure injection inside block/entity mutation, real process-crash behavior during preparation, and whether later entry steps need additional independent rollback transactions.
- **COMPATIBILITY:** successful Last Signal preparation and current service-level Soul/registry rollback remain unchanged. No save schema changes.

No canon rule is introduced or generalized.

## Why slot reuse remains out of scope

`nextSlot` remains monotonic. Reusing a failed slot would require proving every possible world-side mutation was completely reverted, including future scenario additions and process-crash boundaries. Leaving a failed slot consumed is the safer isolation rule and costs only coordinate space.

## Regression coverage

`NightmarePreparationTransactionTest` proves:

1. successful preparation does not invoke rollback;
2. failed preparation invokes rollback and rethrows the original failure;
3. rollback failure is suppressed rather than replacing the preparation failure;
4. multi-step rollback continues after an earlier cleanup failure and preserves later failures as suppressed evidence.

This is process-free transaction coverage. It does not claim a physical Minecraft world rollback test.

## Next independent entry slices

Historical PRs #68, #69 and #70 cover separate later entry boundaries: authoritative registry rollback after a successful prepare but failed registry update, clearing successfully prepared world state on a later entry failure, and committing entry only after the player is observed in the Nightmare dimension. Those should remain separate reviewable slices and be ported only after this exact head is green.
