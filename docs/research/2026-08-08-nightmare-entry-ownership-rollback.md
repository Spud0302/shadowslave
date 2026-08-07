# Nightmare failed-entry ownership rollback audit

**Date:** 2026-08-08

## Finding

`NightmareService.tryEnter(...)` creates and registers a pre-preparation `NightmareInstance`, then prepares world state and calls `NightmareRegistryData.update(prepared)`.

The registry intentionally requires exact authoritative snapshots for teardown. If preparation succeeds but `update(prepared)` throws, the registry still owns the original pre-preparation snapshot while the local `prepared` value already contains layout and pursuer state. The old catch path called ordinary teardown with `prepared`, so `NightmareRegistryData.remove(prepared)` rejected that stale/modified snapshot and left active ownership behind.

That failure also prevented the catch block from reaching its intended Soul rollback because teardown itself threw.

The failed-entry path now separates world cleanup authority from registry ownership authority:

- scenario/entity cleanup still uses the locally prepared snapshot because it carries the created pursuer identity;
- registry removal re-reads the current active snapshot for that player and removes it only when its instance UUID matches the failed entry;
- the exact authoritative snapshot returned by the registry is passed back into `remove(...)`, preserving the exact-snapshot mutation contract from PRs #62/#65;
- a different/newer instance UUID for the same player is never consumed.

## Evidence boundary

- **CANON:** unchanged from Issue #34 / PR #39; this slice does not change Nightmare ending, appraisal, progression, failure, or return mechanics.
- **INFERRED:** unchanged; one active First Nightmare owns its technical scenario state while that instance is active.
- **DESIGN:** a failed entry transaction must consume the exact active ownership record for the failed instance even when the local prepared copy and registry copy differ because `update(...)` failed.
- **UNKNOWN:** physical NeoForge fault injection at the `registry.update(...)` boundary and process-crash behavior during entry remain unproven without a dedicated integration harness.
- **COMPATIBILITY:** successful entry and ordinary exact-snapshot teardown are unchanged; stale failed-entry cleanup cannot consume another instance for the same player.

No canon rule is introduced.

## Regression coverage

`NightmareEntryRollbackOwnershipTest` proves:

1. ordinary exact-snapshot teardown rejects a prepared copy when the registry still owns the pre-update copy, reproducing the mismatch;
2. failed-entry ownership cleanup re-reads and consumes that exact authoritative pre-update owner;
3. the same helper consumes the prepared snapshot when the registry update did succeed;
4. a different active instance UUID for the same player survives stale failed-entry cleanup.

## Deliberate limits

This slice does not make every rollback step infallible. A world/entity cleanup exception can still interrupt the failed-entry catch before Soul restoration, and a real process crash can occur between any two operations. Those are separate recovery-policy questions and should not be solved by consuming ownership blindly after failed world cleanup, because doing so could discard the only recovery handle for orphaned world state.
