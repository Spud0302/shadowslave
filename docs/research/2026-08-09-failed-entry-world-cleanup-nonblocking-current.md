# Failed-entry world cleanup must not block authoritative rollback — current lineage

**Date:** 2026-08-09

## Context

PR #145 added destructive cleanup of successfully prepared Last Signal world state on the verified pre-entry rollback side. Its own research note deliberately recorded one remaining correctness question: `LastSignalScenario.rollbackFailedEntryWorld(...)` called `NightmarePreparationTransaction.rollbackAll(...)`, which rethrows the first cleanup failure after attempting the later cleanup step. `NightmareService.rollbackFailedEntry(...)` performs authoritative registry ownership cleanup only after that world rollback returns, and the caller restores the pre-entry Carrier `SoulData` only after `rollbackFailedEntry(...)` returns.

Therefore a block/entity cleanup exception could still prevent both authoritative ownership cleanup and Soul restoration even though the player had never committed entry. This is a concrete control-flow defect, not a speculative persistence feature.

PR #145 exact head `13d98461df8cbed52edc5580b7a66c752d4f4183` passed Preview Gates run #108 / ID `31309891354` before this follow-up started.

## Current correction

`NightmarePreparationTransaction` now exposes `rollbackAllBestEffort(...)`. It preserves the existing cleanup aggregation semantics—every step is attempted, the first failure remains the primary cleanup failure, and later cleanup failures are attached as suppressed evidence—but returns that aggregate rather than throwing it.

`LastSignalScenario.rollbackFailedEntryWorld(...)` uses the best-effort variant. If entity discard or slot clearing fails, it logs the aggregate failure with the Nightmare instance UUID and returns normally. That allows the existing #139 authoritative ownership rollback and subsequent pre-entry Soul restoration to continue.

The original `rollbackAll(...)` contract remains unchanged for preparation-time rollback, where `NightmarePreparationTransaction.run(...)` still attaches rollback failure evidence to the original preparation exception.

## Evidence classification

- **CANON:** unchanged. No Nightmare role, scenario resolution, survival, appraisal, progression, death, return, Aspect, Flaw, Seed, or Dream Realm rule changes.
- **INFERRED:** unchanged one-instance ownership of technical scenario state during an active First Nightmare.
- **DESIGN:** physical world cleanup is subordinate to authoritative registry/Soul rollback on a failed pre-entry Java transaction; incomplete cleanup must remain observable in logs without preventing those later rollback surfaces.
- **UNKNOWN:** live NeoForge block/entity cleanup fault injection; process-crash atomicity during entry; whether a registry mutation failure should likewise permit attempted Soul restoration; behavior if `SoulService.replace(...)` itself fails; restoration of arbitrary pre-existing blocks in a preview slot.
- **COMPATIBILITY:** successful entry, successful cleanup, preparation-time rollback exception aggregation, #139 exact ownership rollback, #140 entry commit semantics, persistence formats, and completion/death/recovery transactions are unchanged.

No lore proposition is introduced or generalized.

## Regression coverage

`NightmarePreparationTransactionTest.bestEffortRollbackReturnsFailureWithoutThrowingAndRunsEveryStep` proves that:

1. every cleanup step is attempted;
2. the best-effort API returns rather than throws;
3. the first cleanup failure is retained;
4. later cleanup failure is attached as suppressed evidence.

Existing tests continue to prove that ordinary `rollbackAll(...)` still throws its aggregate and that preparation failure retains rollback failures as suppressed evidence.

## Deliberate limits / next audit

This slice specifically removes world-cleanup failure as a control-flow barrier to registry/Soul rollback. It does not claim that every later rollback surface is infallible. A separate audit may evaluate whether authoritative registry rollback failure should still allow attempted Soul restoration, but that should be handled as its own bounded correctness decision because inconsistent ownership/Soul combinations have different recovery implications.

Issue #34's successful-completion physical restart matrix remains separately blocked on real-player execution and is not retried here.
