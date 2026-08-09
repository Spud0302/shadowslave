# Nightmare entry durability ordering — current lineage

**Date:** 2026-08-10  
**Base:** corrected PR #148 exact head `e4a93ee2e02e2cc4020ea3ff2417b480d6822217`  
**Scope:** successful First-Nightmare entry persistence ordering only

## Demonstrated gap

On the #148 lineage, `NightmareService.tryEnter(...)` created and updated active `NightmareRegistryData`, then mutated the player's Soul to Aspirant and teleported the player into the Nightmare, but did not force either persistence surface before returning.

`NightmareRegistryData` is overworld `SavedData`; Soul attachments and player dimension/location are player data. Those surfaces are saved independently. Without an explicit order, a player save can become durable before the dirty Nightmare registry. A restart from that split can therefore reconstruct an Aspirant/player location with no active Nightmare ownership. `NightmareLoginRecoveryPolicy` has no recovery authority in the no-ownership case, so this is a correctness/persistence blocker rather than a presentation concern.

The inverse split is recoverable: if active ownership is durable while the old player file still places the player outside the Nightmare, login selects the existing technical-recovery path because ownership is retained.

## Bounded correction

Successful entry now uses `NightmareEntryDurabilityCoordinator` to enforce:

1. after scenario preparation and `registry.update(prepared)`, persist and join overworld `SavedData` through `SavedDataPersistence.saveAndWait(server)`;
2. only after that durable ownership checkpoint, mutate the player to Aspirant and attempt the cross-dimension teleport;
3. require the existing authoritative observed-dimension commit boundary;
4. after committed entry, synchronously save player data through `PlayerList.saveAll()` before returning success.

The prepared ownership record is therefore a durable recovery handle before any player-side entry mutation can become durable. If a process failure occurs after step 1 but before the committed player save, restart retains ownership and can converge through the existing active/technical login policy instead of reconstructing an ownerless player-side commit.

## Focused tests

`NightmareEntryDurabilityCoordinatorTest` proves:

- prepared ownership persistence precedes player mutation and committed-player persistence;
- ownership persistence failure prevents all player mutation;
- player-entry failure prevents publishing a committed player save.

These are process-free ordering tests. They do not claim physical process-kill coverage.

## Evidence classification

- **CANON:** unchanged. No Nightmare role, completion, failure, appraisal, progression, death, return, Aspect, Flaw, Seed, or Dream Realm rule changes.
- **INFERRED:** unchanged one-instance ownership of technical scenario state while an active First Nightmare exists.
- **DESIGN:** prepared Java ownership must become durable before player-side entry state is allowed to commit; a committed player entry is synchronously saved before success returns.
- **UNKNOWN:** physical power/process loss at these exact boundaries; filesystem guarantees below NeoForge's joined I/O worker; world/chunk durability for prepared Last Signal geometry; process failure inside teleport after the server-level switch; crash convergence after a failed-entry rollback itself; exact position/orientation durability beyond the player save.
- **COMPATIBILITY:** successful entry semantics, #136 preparation rollback, #139 ownership rollback, corrected #140 observed-dimension commit semantics, #145/#146 world cleanup behavior, #148 fail-closed rollback ordering, save schemas, and completion/death/technical/reset transactions are unchanged.

No new lore proposition is introduced, so no canon rule is invented or generalized.

## Deliberate limit / next audit

This slice removes the demonstrated **player-data-before-ownership** crash window. It does not claim full transactional atomicity for entry world chunks or for the failed-entry rollback path. Further entry persistence work should require either a newly demonstrated split, review finding, physical fault evidence, or a concrete process-free reconstruction model; do not add an entry-intent schema speculatively.
