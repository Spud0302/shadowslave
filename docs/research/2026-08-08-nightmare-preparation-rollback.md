# Nightmare preparation rollback audit

**Date:** 2026-08-08

## Finding

`NightmareService.tryEnter(...)` allocates and registers a new `NightmareInstance` before `LastSignalScenario.prepare(...)` mutates the Nightmare dimension. The service catch path can remove that pre-prepare registry record, but the pre-prepare snapshot still has zero layout coordinates and no pursuer UUID.

`LastSignalScenario.prepare(...)` clears and rebuilds the slot region before pursuer creation/addition can fail. Before this change, a failure at either pursuer boundary left the newly built geometry behind after registry teardown. The monotonically consumed slot prevented a later supported instance from overlapping that geometry, but the world mutation itself had no owner and no authoritative cleanup handle.

The preparation boundary now runs as a small fail-closed transaction. If preparation throws, the created pursuer (when any) is discarded and the allocated slot volume is cleared before the original failure is rethrown to `NightmareService`, which then performs its existing registry/Soul rollback.

## Evidence boundary

- **CANON:** unchanged from Issue #34 / PR #39; this slice does not alter Nightmare resolution, appraisal, progression or return rules.
- **INFERRED:** unchanged; one active First Nightmare owns its technical scenario state while it exists.
- **DESIGN:** scenario preparation side effects are transactional for the supported Last Signal preview; failed preparation must not leave ownerless geometry/entities. A failed slot remains consumed rather than being reused.
- **UNKNOWN:** physical failure injection inside NeoForge world mutation and process-crash behavior during preparation remain unproven without a dedicated integration harness.
- **COMPATIBILITY:** successful preparation produces the same layout, pursuer and returned `NightmareInstance`; existing service-level Soul/registry rollback remains unchanged.

No canon rule is introduced.

## Why the slot is not reclaimed

`nextSlot` remains monotonic. Reusing a failed slot would require proving every possible world-side mutation was completely reverted, including future scenario additions and crash boundaries. Leaving the slot consumed is the safer isolation rule and is only a coordinate-allocation cost.

## Regression coverage

`NightmarePreparationTransactionTest` proves that the preparation transaction:

1. does not run rollback after success;
2. runs rollback through the failure path and rethrows the original preparation failure;
3. preserves the original preparation failure if rollback itself also fails, attaching the rollback error as suppressed evidence.

The unit seam does not claim a physical NeoForge world rollback test. That remains an integration evidence gap.
