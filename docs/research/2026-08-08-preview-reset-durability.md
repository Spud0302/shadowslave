# Preview reset durability audit — 2026-08-08

## Scope

This note audits the development-only compound `preview_reset` path after the restart-replayable successful-completion and technical-exit work through PR #73.

## Confirmed defect

`PreviewResetService.reset(...)` previously performed these operations without any durable reset intent:

1. abort active Nightmare ownership if present;
2. clear a retained successful-completion receipt;
3. reset Soul state;
4. clear permanent identity;
5. clear imported identity;
6. clear preview-power state;
7. publish one client snapshot.

Several of those pieces live in different persistence surfaces. In particular, Nightmare ownership/completion state is overworld `SavedData`, while the Soul/identity/imported-identity/preview-power attachments are player data. A process failure after a registry mutation but before the reset player attachments were saved could therefore restart without any durable fact saying that the compound preview reset had already been chosen.

That is distinct from PR #73's technical-exit marker. Technical recovery/admin abort intentionally converge to the existing Carrier recovery state. `preview_reset` instead clears the entire development preview state back to the uninfected baseline, including imported identity and preview-power state. Reusing the narrower technical marker would therefore encode the wrong replay semantics.

## Review corrections

Codex review identified two additional persistence-boundary defects in the first implementation.

First, NeoForge 21.1.244's `DimensionDataStorage.save()` schedules dirty `SavedData` writes through the NeoForge I/O worker and can return before the queued write has reached disk. A transaction checkpoint therefore cannot call `save()` and immediately mutate the other persistence surface while describing the marker as durable. `SavedDataPersistence.saveAndWait(...)` now schedules the overworld save and then waits for `IOUtilities.waitUntilIOWorkerComplete()` before the transaction crosses either registry checkpoint.

Second, throwing from the `SavedData` deserializer is not a fail-closed recovery strategy. `DimensionDataStorage` catches load exceptions and can subsequently create a fresh empty instance, which would erase the in-memory fact that reset recovery was uncertain. `PreviewResetRegistryData.load(...)` therefore retains malformed or duplicate persisted input as a globally blocked recovery state rather than throwing it away. Login recovery halts before technical-exit, successful-completion, or ordinary active-Nightmare recovery while that state is present, and registry mutations reject attempts to overwrite it. Because malformed data may have lost the player identity that owned the pending reset, the implementation deliberately does not guess which player is safe to recover.

## Design decision

Add a separate overworld `PreviewResetRegistryData` containing player UUIDs with pending compound preview resets, plus an explicit blocked state for undecodable persisted marker data.

The reset sequence becomes:

1. idempotently record preview-reset intent;
2. schedule the overworld `SavedData` save and wait for NeoForge's I/O worker to complete before any other reset mutation;
3. abort active Nightmare state through the existing verified-return/teardown path if needed;
4. clear any retained successful-completion receipt;
5. clear every preview player attachment in memory;
6. synchronously save player data containing the complete cleared attachment state;
7. publish the existing single final authoritative client snapshot;
8. clear the preview-reset intent;
9. schedule and join the final overworld `SavedData` write before returning.

Login recovery checks this marker before technical-exit recovery, successful-completion recovery, or ordinary active-Nightmare handling. Replay is idempotent. If the marker file is structurally invalid, recovery stops instead of treating the reset registry as empty.

## Evidence classification

- **CANON:** unchanged. This is a development reset and not a Nightmare Spell mechanic.
- **INFERRED:** none added. No novel behavior is inferred from the reset transaction.
- **DESIGN:** a durably persisted player-scoped preview-reset intent has recovery precedence until the complete reset player state is saved and the one final snapshot is published; undecodable reset metadata blocks Nightmare recovery rather than being guessed away.
- **UNKNOWN:** real process-kill behavior at each physical save boundary remains unproven until dedicated-server fault injection exists. The I/O-worker join establishes the intended API-level durability barrier but is not itself physical power-loss proof. A runtime exception without logout/relogin does not currently auto-dispatch the pending marker again; the command may be rerun or the player may relog.
- **COMPATIBILITY:** worlds with no preview-reset data file continue to construct an empty healthy registry. Valid existing PR #74 marker files round-trip unchanged. Normal successful reset still produces one final authoritative sync. Nightmare return/teardown behavior remains delegated to the existing service.

No canon rule is invented.

## Test boundary

Unit coverage checks the transaction ordering, idempotent replay, marker round-trip, corrupt-marker blocked-state behavior, mutation rejection while blocked, wrong marker type handling, and that reset intent remains present through player persistence and the final sync. `SavedDataPersistenceTest` separately checks that a scheduled save is followed by the I/O-worker wait before the durability helper returns.

Hosted Gradle/JUnit, physical client/server boot and process-kill evidence are separate gates and must not be claimed unless a workflow or manual run records them for the exact head.
