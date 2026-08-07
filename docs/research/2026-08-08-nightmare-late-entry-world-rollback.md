# Nightmare late-entry world rollback audit

**Date:** 2026-08-08

## Finding

PR #67 made `LastSignalScenario.prepare(...)` transactional when preparation itself fails, and PR #68 made failed-entry registry ownership removal authoritative when `registry.update(prepared)` fails.

A separate late-entry gap remained: if preparation succeeded and a later step in `NightmareService.tryEnter(...)` threw, the catch path discarded the prepared pursuer and removed matching registry ownership but did not clear the slot geometry that preparation had already built. That could leave an ownerless Last Signal structure in the shared Nightmare dimension.

The failed-entry world rollback now removes the attempted pursuer and clears the allocated slot volume before registry ownership rollback. It derives the physical rollback origin from the immutable allocated `slot`, not the snapshot's persisted `origin`, because `registry.update(prepared)` may be the failing operation and the authoritative registry snapshot may still contain the pre-preparation `BlockPos.ZERO` layout.

Normal successful/technical/admin/death teardown semantics are deliberately unchanged by this slice; only failed entry gets the destructive slot clear.

## Evidence boundary

- **CANON:** unchanged from Issue #34 / PR #39. This slice changes no Nightmare ending, appraisal, progression, failure, or return mechanic.
- **INFERRED:** unchanged one-instance ownership of technical scenario state during an active First Nightmare.
- **DESIGN:** a failed entry transaction must not leave ownerless scenario geometry; rollback addresses the physical namespace through the immutable allocated slot.
- **UNKNOWN:** physical NeoForge fault injection after successful preparation and process-crash behavior during entry remain unproven without a dedicated integration harness.
- **COMPATIBILITY:** successful entry and all ordinary teardown paths keep their previous geometry behavior; PR #67 preparation-failure cleanup remains idempotently compatible with the additional failed-entry clear.

No canon rule is introduced.

## Regression coverage

`NightmareLateEntryWorldRollbackTest` proves that rollback derives the physical slot origin from the immutable allocated slot even when the pre-update snapshot still has the placeholder zero layout. Existing `NightmarePreparationTransactionTest` proves that multi-step rollback continues to the slot clear even if an earlier cleanup step throws.

## Deliberate limits

This does not make the full entry catch infallible. If failed-entry world rollback itself ultimately throws, later ownership and Soul rollback can still be interrupted; PR #68 records why blindly consuming ownership after failed world cleanup could discard the only recovery handle. This slice also does not reclaim `nextSlot`, restore arbitrary pre-existing blocks, or claim process-crash atomicity.
