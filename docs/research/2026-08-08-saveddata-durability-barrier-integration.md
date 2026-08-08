# SavedData durability barrier integration — 2026-08-08

## Scope

This note records the bounded durability dependency needed before the active successful-completion coordinator can be wired to NeoForge runtime persistence. It ports only the shared SavedData save-and-join helper and its pure ordering tests from the historical correctness stack onto the current #98 lineage.

## Correctness finding

The current consolidation branch has a restart-replayable coordinator, durable completion receipts, and idempotent appraisal reconciliation, but it does not yet contain the historical `SavedDataPersistence` helper. A runtime `NightmareCompletionCoordinator.Operations.persistRegistry()` implementation must not use raw `DimensionDataStorage.save()` as a durability checkpoint: NeoForge queues SavedData writes through its I/O worker, so returning from `save()` alone is not the transaction boundary assumed by the coordinator's restart model.

The historical #74/#75 correction already established the project contract: schedule the overworld SavedData save, then wait for NeoForge's I/O worker to complete before advancing the recovery transaction. Reusing that helper avoids reintroducing a known persistence defect while keeping the runtime adapter itself small.

## Change

`SavedDataPersistence.saveAndWait(server)` performs:

1. `server.overworld().getDataStorage().save()` to schedule dirty SavedData;
2. `IOUtilities.waitUntilIOWorkerComplete()` before returning.

The package-private overload accepts two `Runnable`s so unit tests can prove ordering without constructing a Minecraft server or touching actual I/O.

## Evidence classification

- **CANON:** unchanged. This slice changes no Nightmare, appraisal, death, progression, return, Aspect, Flaw, Attribute, Memory, Echo, or Seed rule.
- **INFERRED:** none added.
- **DESIGN:** joining NeoForge's queued SavedData worker before a Java recovery transaction advances is a persistence implementation choice.
- **UNKNOWN:** worker completion is not proof against every operating-system, filesystem, storage-device, abrupt-power-loss, or physical process-kill failure. Issue #34's real same-world restart matrix remains required.
- **COMPATIBILITY:** no save schema or gameplay state changes. The helper is additive and unused by current runtime paths until the completion runtime adapter is integrated.

No primary-novel research was required because no lore-sensitive mechanic changed. `docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` remain controlling.

## Tests

`SavedDataPersistenceTest` proves that one checkpoint schedules before waiting and that repeated checkpoints each finish their wait before intervening/later transaction work begins.

## Next integration boundary

With this dependency available, the next bounded slice can safely implement `NightmareCompletionCoordinator.Operations` against the current `NightmareRegistryData`, `PreviewAppraisalService`, player save path, return/teardown operations, and this durability barrier. Terminal-event/login routing should remain separate if possible so the adapter can be reviewed independently.
