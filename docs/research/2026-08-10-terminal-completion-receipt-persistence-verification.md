# Successful-completion receipt persistence verification — 2026-08-10

## Scope

This note audits only the **first durable successful-completion receipt** on the current combined #152/#153 entry-durability lineage. It does not redesign later completion phases, prepared Nightmare chunk persistence, canonical death, technical/admin exit, preview reset, appraisal rules, or scenario content.

## Concrete defect

`NightmareSuccessfulCompletionActivation` previously treated a normal return from `SavedDataPersistence.saveAndWait(server)` as proof that `NightmareRegistryData.beginSuccessfulCompletion(...)` had reached `world/data/shadowslave_nightmares.dat`.

The entry-durability work already established a stronger repository-local persistence contract: the configured save path can return after scheduling/joining without proving that the expected target file changed, so a recovery-critical checkpoint must compare settled file images across the mutation. `PersistenceFileCheckpoint.capture(...)` drains pending NeoForge I/O before the baseline read; `requireChanged(...)` drains it again before verification.

For successful completion the first receipt is uniquely important. If it is absent after restart, there is no completion authority from which to replay appraisal/return/teardown, recreating Issue #34's zero-appraisal failure shape. By contrast, once this initial receipt is genuinely durable, a later phase write that fails to advance durably can replay from an older retained receipt through the existing idempotent completion coordinator. This slice therefore verifies only the initial receipt rather than broadening every completion persistence call at once.

## Review correction: unverified in-memory receipt

The initial #161 head `80900879cde29e71e55f23e464650e042d48103b` passed Preview Gates #121, but review identified a P1 recovery-authority gap. A save/verification failure can leave the newly-created completion receipt in the live `NightmareRegistryData` object even though the checkpoint has just refused to treat it as durable. Because later same-process recovery reads that in-memory receipt, a relog could otherwise resume appraisal/return/teardown from authority that the persistence checkpoint explicitly rejected.

The corrected activation path therefore treats registry persistence and file-image verification as one guarded pre-durable phase. If either throws, it clears the exact newly-recorded completion receipt from the live registry before propagating the original persistence failure. Clearing marks the registry dirty, so later saves/retries cannot silently keep trusting the rejected in-memory receipt. If cleanup itself fails, that cleanup failure is attached as suppressed evidence and the original persistence failure remains primary.

This is intentionally a quarantine of **unverified live authority**, not a claim that storage is known to contain no receipt. A lower-level write may have partially or fully reached disk before an exception. Retaining exact active Nightmare ownership while refusing same-process completion recovery is therefore safer than progressing from an authority that failed its verification contract; restart behavior remains governed by whatever image actually survived on disk.

## Corrected order

1. validate the terminal-resolution trigger;
2. capture a settled baseline image of `world/data/shadowslave_nightmares.dat`;
3. create the successful-completion receipt in `NightmareRegistryData`;
4. schedule/join the registry save;
5. require the settled registry file image to have changed;
6. if steps 4-5 fail, clear/quarantine the exact in-memory receipt and propagate the original failure;
7. only after verified success expose `AFTER_TERMINAL_REGISTRY_SAVE` as a durable fault boundary;
8. only then apply world-resolution presentation and resume the completion coordinator.

## Evidence classification

- **CANON:** unchanged. No Nightmare resolution, survival, appraisal, progression, return, death, Aspect, Flaw, Seed, or Dream Realm rule changes.
- **INFERRED:** unchanged association between one retained completion receipt and the exact resolved Nightmare instance.
- **DESIGN:** a recovery-critical Java completion receipt counts as persisted only when its expected registry file has an observably changed settled image; a receipt that fails that check is not trusted by same-process recovery and is removed from live authority before the failure returns.
- **UNKNOWN:** power-loss/fsync guarantees below the observable file image; whether a failed lower-level write nevertheless left a valid receipt on disk; physical storage-device failure; live process loss exactly inside the file-replacement path; whether later phase checkpoints also deserve direct file-image verification despite being replayable from the retained receipt.
- **COMPATIBILITY:** completion schema, terminal-resolution validation, world presentation, appraisal, return, teardown, canonical-death, technical/admin recovery, preview reset, and entry behavior are unchanged when persistence succeeds. Failed verification now leaves active ownership but no trusted in-memory completion receipt.

No new lore proposition is introduced. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, current Issue #34, current main authority files, and the active persistence/recovery PRs were re-read before implementation.

## Tests

`NightmareSuccessfulCompletionActivationTest` owns the production ordering seam. It proves that receipt verification occurs before the named durable fault boundary, world presentation, and completion replay. It also proves that both persistence failure and verification failure quarantine the receipt before returning, that later presentation/replay never runs, and that a quarantine failure is suppressed without replacing the original persistence failure.

`PersistenceFileCheckpointTest` on the parent lineage already covers missing-to-created, changed, unchanged, missing-target, and the older-queued-write baseline-drain hazard.

## Limits / next audit

This is not a physical process-kill proof and does not replace Issue #34's same-world recovery matrix. It also does not automatically wrap every later completion phase save in another file digest checkpoint: an older durable receipt remains recovery authority for those later phases, so broadening them requires separate evidence that replay is insufficient.

After the corrected exact head is green, the next independent correctness audit should examine another persistence authority that can disappear entirely, not retry the prepared-world durability blocker and not add speculative phase barriers without a demonstrated failure model.
