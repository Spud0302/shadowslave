# Nightmare SavedData durability barrier audit — 2026-08-08

## Scope

This note follows the NeoForge persistence finding raised during PR #74 review and audits the shared `NightmareService.persistRegistry(...)` checkpoint used by successful completion, technical/admin exit, explicit completion cleanup, ordinary teardown, and canonical-death registry persistence.

## Confirmed defect

The completion and technical-exit coordinators model `persistRegistry()` as a durable boundary. Their restart tests deliberately crash immediately after those calls and reconstruct from the state that the fake operations marked durable.

The production adapter did not provide the same contract. It called `server.overworld().getDataStorage().save()` and returned. The PR #74 review established for the configured NeoForge 21.1.244 path that `DimensionDataStorage.save()` schedules dirty `SavedData` writes through NeoForge's I/O worker; scheduling the write is therefore not the same boundary as waiting for that queued write to complete.

This matters most for Issue #34's recovery transactions. A process can advance from one logical phase to another while the registry phase/marker that is supposed to authorize restart recovery is still only queued. The coordinator tests remain useful for transaction ordering, but their durability assumption was stronger than the runtime adapter.

## Correction

`NightmareService.persistRegistry(...)` now delegates to the shared `SavedDataPersistence.saveAndWait(server)` helper introduced by the corrected preview-reset work. That helper:

1. calls overworld `DimensionDataStorage.save()` to schedule dirty SavedData writes;
2. calls `IOUtilities.waitUntilIOWorkerComplete()` before returning.

Every existing Nightmare registry checkpoint that routes through the shared helper therefore joins the queued write before the transaction advances to its next persistence surface or phase.

This is intentionally a shared adapter correction rather than separate fixes inside `NightmareCompletionCoordinator` and `NightmareTechnicalExitCoordinator`: those coordinators correctly define ordering and remain testable without Minecraft runtime objects, while the server adapter owns the concrete durability mechanism.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, death, Aspect, Flaw, or progression mechanic changes.
- **INFERRED:** unchanged association between one recovery transaction and its active/completed Nightmare instance.
- **DESIGN:** a Java method named/used as a durable registry checkpoint does not return until the NeoForge SavedData I/O worker has completed the queued write.
- **UNKNOWN:** a worker join is not proof against every operating-system, filesystem, storage-device, or abrupt-power-loss failure. Real process-kill fault injection at each documented boundary is still required by Issue #34.
- **COMPATIBILITY:** no save schema, phase order, or gameplay result changes. Rare completion/recovery/admin/reset checkpoints may block slightly longer while queued SavedData I/O drains.

No canon rule is invented.

## Test boundary

Existing completion and technical-exit coordinator tests continue to exercise ordering and restart replay at each logical registry boundary. `SavedDataPersistenceTest` now additionally verifies repeated durability checkpoints each complete their wait before control reaches code between checkpoints or a following checkpoint.

Hosted Gradle/JUnit, client/server smoke, and physical process-kill evidence must still be recorded for the exact PR head before they are claimed.

## Remaining risk

This correction makes the existing registry checkpoints match their intended API-level durability contract. It does **not** repair transaction ordering that is independently unsafe. In particular, `canonicalDeath(...)` still consumes active Nightmare ownership before resetting and persisting player-side Soul/identity state; that crash window remains a separate audit slice rather than being hidden by a stronger save primitive.
