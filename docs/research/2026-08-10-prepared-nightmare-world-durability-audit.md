# Prepared Nightmare world durability audit — 2026-08-10

## Scope

This audit follows the green combined entry-durability candidate on PR #152 (`6fa5b117619eeb1f3747201116825ba97911b37e`, Preview Gates run #120).

The question is deliberately narrower than successful-completion recovery: after `LastSignalScenario.prepare(...)` has built the scenario and created its pursuer, can the Java entry transaction prove that the prepared Nightmare **world state** is durable before it allows player-side entry state to become durable?

## Evidence checked

Current entry order on PR #152:

1. `LastSignalScenario.prepare(...)` clears/builds the allocated slot and adds the persistent pursuer entity.
2. `NightmareRegistryData.update(prepared)` records the prepared layout/entity identity in memory.
3. `PersistenceFileCheckpoint` + `SavedDataPersistence.saveAndWait(...)` prove that the overworld `shadowslave_nightmares.dat` file changed across a settled I/O boundary.
4. only then does the player become Aspirant and teleport into `shadowslave:nightmare`.
5. after observed dimension commit, the player data file must change across its own settled checkpoint before normal entry success is published.

`SavedDataPersistence.saveAndWait(...)` is intentionally scoped to the overworld `DimensionDataStorage`: it schedules `overworld().getDataStorage().save()` and joins NeoForge's I/O worker. It does not itself save or flush the Nightmare dimension's chunks/entities.

`LastSignalScenario.prepare(...)` mutates the Nightmare level directly through block writes and `addFreshEntity(...)`. No separate chunk/entity durability checkpoint currently follows those mutations before player entry.

## Demonstrated gap

The current transaction therefore has three distinct persistence surfaces:

- prepared Nightmare ownership/layout metadata in overworld SavedData;
- prepared Nightmare blocks/entities in the Nightmare dimension's chunk/entity storage;
- player Soul/dimension/location in player data.

PR #152 now orders and verifies the first and third surfaces, but it does not prove the second surface reached durable storage.

A process loss after durable ownership/player entry but before the prepared Nightmare chunks/entities have been saved can therefore plausibly reconstruct an active owner and a player in the Nightmare while the authored slot/pursuer is older or absent on disk.

This is a **technical persistence risk**, not evidence for a lore mechanic. No claim is made here that such a split has yet been reproduced with a physical process kill.

## Why this audit does not add a naive full-server save

A straightforward `MinecraftServer.saveEverything(... flush=true ...)` before player mutation would force more world state toward storage, but it changes the rollback problem as well:

- a later pre-entry failure can still clear blocks/entities and remove ownership only in memory;
- if the process then dies before those rollback mutations are durably saved, the newly forced prepared-world image can remain on disk after the command reported rollback;
- ordinary teardown intentionally does not treat arbitrary scenario geometry as a universally restorable pre-existing world snapshot.

Adding the flush without defining/replaying the matching rollback durability boundary would therefore trade one unproven split for another. This run does not make that transaction design guess.

## Evidence classification

- **CANON:** unchanged. No Nightmare role, scenario-resolution, appraisal, progression, death, return, Aspect, Flaw, Seed, or Dream Realm rule changes.
- **INFERRED:** unchanged one-instance technical ownership of an active prepared scenario.
- **DESIGN:** prepared-world/chunk durability and rollback durability are Java transaction concerns. Any future barrier/intent/receipt used to order those writes is implementation design, not Spell behavior.
- **UNKNOWN:** physical process-loss reconstruction at this boundary; exact chunk/entity save timing on the configured server; storage guarantees below Minecraft/NeoForge save APIs; whether a safe solution should flush prepared world state, persist a preparation intent, rebuild idempotently on login, or use another recovery contract; restoration of arbitrary pre-existing blocks in an allocated preview slot.
- **COMPATIBILITY:** the current #136/#139/#140/#145/#146/#148/#152 entry/rollback semantics, save schemas, successful-completion/death/technical/reset transactions, and lore-facing behavior remain unchanged by this audit.

## Blocker and exact resume condition

Do not automatically retry a world-flush implementation from this note alone.

Resume implementation only when at least one of the following appears:

1. a process-free reconstruction test demonstrates the exact persisted split and identifies a convergent recovery target;
2. a live same-world process-kill test reproduces missing/stale prepared blocks or pursuer state after otherwise committed entry;
3. primary Minecraft/NeoForge save-path evidence establishes a bounded API contract that can make both preparation and failed-entry rollback durable without introducing a new orphaned-world window;
4. an owner decision explicitly chooses a recovery policy such as authoritative idempotent scenario rebuild versus durable rollback/cleanup;
5. a dependency/code change provides a credible new transaction boundary.

Until then, retain this as a documented persistence blocker and select another unblocked correctness slice rather than repeatedly adding speculative entry transaction layers.
