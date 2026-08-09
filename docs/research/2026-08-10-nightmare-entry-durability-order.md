# Nightmare entry durability ordering — current lineage

**Date:** 2026-08-10  
**Base:** corrected PR #148 exact head `e4a93ee2e02e2cc4020ea3ff2417b480d6822217`  
**Scope:** successful First-Nightmare entry persistence ordering only

## Demonstrated gap

On the #148 lineage, `NightmareService.tryEnter(...)` created and updated active `NightmareRegistryData`, then mutated the player's Soul to Aspirant and teleported the player into the Nightmare, but did not force either persistence surface before returning.

`NightmareRegistryData` is overworld `SavedData`; Soul attachments and player dimension/location are player data. Those surfaces are saved independently. Without an explicit order, a player save can become durable before the dirty Nightmare registry. A restart from that split can therefore reconstruct an Aspirant/player location with no active Nightmare ownership. `NightmareLoginRecoveryPolicy` has no recovery authority in the no-ownership case, so this is a correctness/persistence blocker rather than a presentation concern.

The inverse split is recoverable: if active ownership is durable while the old player file still places the player outside the Nightmare, login selects the existing technical-recovery path because ownership is retained.

## Initial bounded correction

PR #152 introduced `NightmareEntryDurabilityCoordinator` to enforce:

1. after scenario preparation and `registry.update(prepared)`, call `SavedDataPersistence.saveAndWait(server)`;
2. only afterward mutate the player to Aspirant and attempt the cross-dimension teleport;
3. require the existing authoritative observed-dimension commit boundary;
4. after committed entry, call `PlayerList.saveAll()` before returning success.

That ordering is necessary, but review found that the first implementation overclaimed what normal return from those save APIs proves.

## Review correction: observable file-image checkpoints

Codex review of #152 identified two concrete persistence API hazards on the configured Minecraft/NeoForge line:

- a `SavedData` write failure can be logged/swallowed by the asynchronous save path, allowing the I/O-worker join to return normally even though the expected file image was not updated;
- a player-data save failure can likewise be logged/swallowed, allowing `PlayerList.saveAll()` to return normally without proving that the player's file changed.

The first #153 head therefore captured the target file image immediately before each transaction checkpoint and required a changed image immediately afterward. Exact head `b63b6543cb14421f345d71f3fa255157272f3655` passed Preview Gates run #116, but review then found a stronger P1 baseline hazard: an older SavedData write may already be queued when the registry baseline is captured. If that older write lands during `saveAndWait(...)` while the new ownership write fails, the old write alone can change the digest and falsely satisfy the checkpoint.

The corrected checkpoint primitive now drains NeoForge's pending I/O worker before reading either the baseline or verification image. Entry therefore uses this order:

1. drain pending NeoForge I/O, then capture `world/data/shadowslave_nightmares.dat`;
2. schedule and join the prepared-ownership SavedData save;
3. drain pending I/O again and require the Nightmare registry file to exist with a different SHA-256 image before any player mutation begins;
4. drain pending I/O and capture `world/playerdata/<uuid>.dat` immediately before Aspirant/teleport mutation;
5. perform the existing observed-dimension entry boundary;
6. call `PlayerList.saveAll()`;
7. drain pending I/O and require the target player file to exist with a different SHA-256 image before publishing entry success.

Draining before the baseline ensures an older queued write cannot masquerade as evidence for the transaction's new ownership write. A swallowed registry write failure therefore leaves the settled baseline unchanged and fails closed before player mutation. A swallowed player write failure still surfaces after authoritative teleport commit; the existing #140/#148 exceptional path preserves active ownership rather than falsely publishing success.

This is an observable-write check, not a claim of disk-controller or power-loss atomicity. Reading a changed settled file image proves the configured save path produced new repository-visible bytes across the checkpoint; it does not prove storage hardware has survived arbitrary sudden power loss.

## Focused tests

`NightmareEntryDurabilityCoordinatorTest` proves:

- prepared ownership persistence precedes player mutation and committed-player persistence;
- ownership persistence failure prevents all player mutation;
- player-entry failure prevents publishing a committed player save;
- committed-player persistence failure is surfaced rather than treated as success.

`PersistenceFileCheckpointTest` proves:

- a newly created persistence file satisfies the checkpoint;
- a changed file image satisfies the checkpoint;
- an unchanged image fails closed;
- a missing file fails closed;
- pending writes are drained before the baseline image is read, so an older queued write cannot by itself satisfy the subsequent changed-image check.

These remain process-free tests. Hosted Gradle/JUnit plus physical client/server gates are required separately, and no physical process-kill result is claimed here.

## Evidence classification

- **CANON:** unchanged. No Nightmare role, completion, failure, appraisal, progression, death, return, Aspect, Flaw, Seed, or Dream Realm rule changes.
- **INFERRED:** unchanged one-instance ownership of technical scenario state while an active First Nightmare exists.
- **DESIGN:** prepared Java ownership must have an observably changed, settled persistence-file image before player-side entry state may mutate; a normally committed player entry must likewise have an observably changed settled player file before success is published.
- **UNKNOWN:** physical power/process loss at these exact boundaries; filesystem/storage guarantees below the observed settled file image; world/chunk durability for prepared Last Signal geometry; process failure inside teleport after the server-level switch; crash convergence after failed-entry rollback itself; exact position/orientation durability beyond the player file; administrator UX for a persistence device that repeatedly rejects writes.
- **COMPATIBILITY:** successful entry semantics, #136 preparation rollback, #139 ownership rollback, corrected #140 observed-dimension commit semantics, #145/#146 world cleanup behavior, #148 fail-closed rollback ordering, save schemas, and completion/death/technical/reset transactions are unchanged.

No new lore proposition is introduced, so no canon rule is invented or generalized.

## Deliberate limit / next audit

This correction closes the review-proven false-success cases currently demonstrated for the entry checkpoints, including the stale queued-write baseline case. It does not claim full transaction atomicity for scenario chunks or exceptional teleport process loss. Further entry persistence work should require a newly demonstrated split, fault evidence, or a concrete process-free reconstruction model rather than another speculative intent schema.
