# Canonical-death ownership teardown persistence verification — 2026-08-10

## Scope

Audit the final cross-SavedData handoff in canonical First-Nightmare death after PR #175 verifies the persisted player reset. This slice does not revisit prepared Nightmare world/chunk durability (#158), and it does not change ordinary Nightmare death semantics.

## Finding

Canonical death keeps its dedicated marker in `shadowslave_nightmare_deaths.dat` while it clears completion state, resets/persists the player, removes active Nightmare ownership, and finally deletes the death marker.

The remaining order was:

1. remove active ownership from `NightmareRegistryData` in memory;
2. call `SavedDataPersistence.saveAndWait(server)` for `shadowslave_nightmares.dat`;
3. delete the canonical-death marker from `NightmareDeathRegistryData`;
4. call `SavedDataPersistence.saveAndWait(server)` for the death registry.

Those are separate persistence surfaces. Normal return from the generic Nightmare-registry save did not independently prove that ownership removal reached disk before the death-specific recovery marker could be consumed.

If the ownership write remained stale while the later death-marker deletion became durable, restart could load stale active ownership but no death marker. Login recovery prioritizes a death marker only when one exists; stale active ownership outside the Nightmare dimension can instead select technical recovery. That can reinterpret an already-recorded canonical death as a technical/admin recovery path rather than replaying the death transaction.

## Implemented contract

`NightmareDeathCoordinator` now has an explicit verification boundary after active-ownership teardown persistence and before `clearDeathIntent()`.

Production opens `world/data/shadowslave_nightmares.dat` and requires a structurally readable `data.instances` list. Every persisted active-instance entry must first decode through the production `NightmareInstance.load(...)` path; only then does the verifier require that no decoded instance contains either:

- the dead player's UUID; or
- the dead Nightmare instance UUID.

A missing registry file, missing `data` compound, missing `instances` list, non-compound entry, or any entry rejected by production loading fails closed while the already-durable death marker remains available for retry/reconstruction.

Exact persisted-state verification is deliberate instead of a changed-byte digest. On a restart after ownership removal was already persisted, the target registry image can already be correct and need no further byte change. The relevant proof is that the complete persisted ownership authority is loadable and that the target ownership is absent before the separate death authority is consumed.

### Review correction

The initial #178 head validated only `player_id` and `instance_id` on unrelated persisted entries. Review identified that this could accept an entry with valid ownership IDs but another missing/invalid field such as `scenario_id` or `return_dimension`; production restart loading would then reject the registry after the death marker had already been consumed. The corrected verifier decodes every entry with `NightmareInstance.load(...)`, making the checkpoint at least as strict as the production loader whose restart behavior it is intended to protect.

## Evidence classification

- **CANON:** unchanged. Ordinary First-Nightmare death remains the existing death/failure outcome; no new death, Gate, creature-release, appraisal, progression, Aspect, Flaw, Seed, or Dream Realm mechanic is introduced.
- **INFERRED:** none added.
- **DESIGN:** canonical-death recovery authority in one SavedData surface must not be consumed until the other persisted authority surface is both production-loadable and proves active ownership is gone.
- **UNKNOWN:** physical power-loss/fsync guarantees below a readable compressed NBT image; storage corruption after verification; exact mature corpse/Gate consequences; physical process-kill convergence at this newly guarded boundary.
- **COMPATIBILITY:** no schema change. Restart replay with ownership already absent is accepted. Technical/admin recovery remains separate and is not used as a substitute for a known canonical-death marker.

No new lore proposition is introduced. `docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` remain the governing source/alignment rules; this is persistence/recovery DESIGN.

## Tests

`NightmareDeathCoordinatorTest` proves that failure of the ownership-teardown persistence proof retains the death marker and prevents its deletion, while the existing restart-at-every-durable-boundary matrix remains.

`PersistedNightmareOwnershipVerifierTest` covers:

- accepting a healthy registry with no target ownership;
- rejecting the target player under another instance ID;
- rejecting the target instance ID under another player;
- rejecting a persisted entry whose ownership IDs are present but whose remaining fields make `NightmareInstance.load(...)` fail;
- rejecting malformed active ownership metadata;
- rejecting a missing persistence file.

## Remaining limits / resume conditions

This is process-free/API-level evidence. It does not claim fsync/power-loss durability or physical process-kill proof. The prepared-world durability item remains blocked under #158's existing evidence/owner-decision resume conditions and was not retried.

The frozen-datapack hosted world-generation stall seen on #175 and #177 is also not retried here after two consecutive loop occurrences. Resume that CI item only with new stall/profile evidence, a deterministic bounded world-generation approach, a justified observation/watchdog contract, or a relevant dependency/CI change rather than another blind rerun or timeout increase.
