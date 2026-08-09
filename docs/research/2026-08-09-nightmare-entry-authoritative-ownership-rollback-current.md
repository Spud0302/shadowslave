# Current-lineage Nightmare failed-entry ownership rollback audit

**Date:** 2026-08-09

## Finding

PR #136 made `LastSignalScenario.prepare(...)` transactional and passed Preview Gates run #98, but a separate outer entry failure remained after successful preparation.

`NightmareService.tryEnter(...)` creates and registers a pre-preparation `NightmareInstance`, then prepares layout/pursuer state and calls `NightmareRegistryData.update(prepared)`.

The registry intentionally requires the exact authoritative snapshot for mutation. If preparation succeeds but `registry.update(prepared)` throws, the registry still owns the original pre-preparation snapshot while the local `prepared` variable contains layout and pursuer state. Calling ordinary teardown with `prepared` therefore asks the registry to remove a stale/modified copy, which it correctly rejects.

That failure can leave active ownership behind and can prevent the catch block from reaching the intended Soul rollback.

Historical PR #68 previously audited this exact mismatch. This current-lineage port applies only that ownership fix on top of green #136.

## Current correction

Failed-entry rollback now separates two authorities:

- world/entity cleanup uses the locally prepared snapshot because it owns the pursuer identity created during preparation;
- registry cleanup re-reads the active snapshot for the attempted player;
- ownership is removed only if the authoritative active snapshot carries the same Nightmare instance UUID as the failed entry;
- that exact registry snapshot is passed back into `NightmareRegistryData.remove(...)`;
- a different/newer instance for the same player is never consumed by stale rollback.

Successful entry and ordinary teardown remain unchanged.

## Evidence classification

- **CANON:** unchanged. This slice does not alter Nightmare role, resolution, appraisal, progression, death, return, Aspect, Flaw, Seed, or Dream Realm rules.
- **INFERRED:** unchanged one-instance ownership of technical scenario state while a First Nightmare is active.
- **DESIGN:** failed-entry rollback must consume the exact authoritative registry record for the attempted instance rather than assuming the locally prepared copy was committed.
- **UNKNOWN:** live NeoForge fault injection at `registry.update(prepared)`, real process-crash behavior during entry, rollback behavior if later world/entity cleanup itself throws, and the later teleport commit boundary.
- **COMPATIBILITY:** successful entry, exact-snapshot mutation rules, save formats, current completion/death/recovery transactions, and normal teardown are unchanged.

No canon rule is introduced or generalized.

## Regression coverage

`NightmareEntryRollbackOwnershipTest` proves:

1. ordinary exact-snapshot removal rejects the locally prepared copy when the registry still owns the pre-update snapshot;
2. failed-entry rollback re-reads and removes that exact authoritative pre-update owner;
3. the same helper removes the prepared owner when the registry update had already succeeded;
4. stale failed-entry rollback cannot consume a different active instance UUID for the same player.

## Deliberate limits

This slice does not make every failed-entry rollback step infallible. If world/entity cleanup itself throws, policy around preserving ownership versus continuing Soul restoration remains a separate correctness question. Consuming ownership blindly after uncertain world cleanup could discard the only recovery handle for orphaned state.

`nextSlot` remains monotonic and failed slots are not reclaimed.

No physical process-kill or NeoForge update-fault injection is claimed by the unit tests.

## Next independent entry slices

If this exact head passes hosted gates, historical PR #69 is the next bounded correctness slice: later entry failure after successful preparation can require cleanup of prepared world state beyond ownership reconciliation. Historical #70's observed-dimension teleport commit boundary should remain separate unless new evidence changes that ordering.
