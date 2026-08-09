# Successful-completion receipt persistence verification — 2026-08-10

## Scope

This note audits only the **first durable successful-completion receipt** on the current combined #152/#153 entry-durability lineage. It does not redesign later completion phases, prepared Nightmare chunk persistence, canonical death, technical/admin exit, preview reset, appraisal rules, or scenario content.

## Concrete defect

`NightmareSuccessfulCompletionActivation` previously treated a normal return from `SavedDataPersistence.saveAndWait(server)` as proof that `NightmareRegistryData.beginSuccessfulCompletion(...)` had reached `world/data/shadowslave_nightmares.dat`.

The entry-durability work already established a stronger repository-local persistence contract: the configured save path can return after scheduling/joining without proving that the expected target file changed, so a recovery-critical checkpoint must compare settled file images across the mutation. `PersistenceFileCheckpoint.capture(...)` drains pending NeoForge I/O before the baseline read; `requireChanged(...)` drains it again before verification.

For successful completion the first receipt is uniquely important. If it is absent after restart, there is no completion authority from which to replay appraisal/return/teardown, recreating Issue #34's zero-appraisal failure shape. By contrast, once this initial receipt is genuinely durable, a later phase write that fails to advance durably can replay from an older retained receipt through the existing idempotent completion coordinator. This slice therefore verifies only the initial receipt rather than broadening every completion persistence call at once.

## Corrected order

1. validate the terminal-resolution trigger;
2. capture a settled baseline image of `world/data/shadowslave_nightmares.dat`;
3. create the successful-completion receipt in `NightmareRegistryData`;
4. schedule/join the registry save;
5. require the settled registry file image to have changed;
6. only then expose `AFTER_TERMINAL_REGISTRY_SAVE` as a durable fault boundary;
7. only then apply world-resolution presentation and resume the completion coordinator.

If verification fails, the failure is surfaced before the durable fault marker, altar ignition, appraisal, return, or teardown begins.

## Evidence classification

- **CANON:** unchanged. No Nightmare resolution, survival, appraisal, progression, return, death, Aspect, Flaw, Seed, or Dream Realm rule changes.
- **INFERRED:** unchanged association between one retained completion receipt and the exact resolved Nightmare instance.
- **DESIGN:** a recovery-critical Java completion receipt counts as persisted only when its expected registry file has an observably changed settled image across the mutation.
- **UNKNOWN:** power-loss/fsync guarantees below the observable file image; physical storage-device failure; live process loss exactly inside the file-replacement path; whether later phase checkpoints also deserve direct file-image verification despite being replayable from the retained receipt.
- **COMPATIBILITY:** completion schema, terminal-resolution validation, world presentation, appraisal, return, teardown, canonical-death, technical/admin recovery, preview reset, and entry behavior are unchanged when persistence succeeds.

No new lore proposition is introduced. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, current Issue #34, current main authority files, and the active persistence/recovery PRs were re-read before implementation.

## Tests

`NightmareSuccessfulCompletionActivationTest` owns the production ordering seam. It proves that receipt verification occurs before the named durable fault boundary, world presentation, and completion replay, and that a verification failure stops all three later steps while propagating the original failure.

`PersistenceFileCheckpointTest` on the parent lineage already covers missing-to-created, changed, unchanged, missing-target, and the older-queued-write baseline-drain hazard.

## Limits / next audit

This is not a physical process-kill proof and does not replace Issue #34's same-world recovery matrix. It also does not automatically wrap every later completion phase save in another file digest checkpoint: an older durable receipt remains recovery authority for those later phases, so broadening them requires separate evidence that replay is insufficient.

After this slice is green, the next independent correctness audit should examine another persistence authority that can disappear entirely, not retry the prepared-world durability blocker and not add speculative phase barriers without a demonstrated failure model.
