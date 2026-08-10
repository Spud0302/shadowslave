# Technical-exit player persistence verification — 2026-08-10

## Scope

Audit the player-side commit immediately before technical/admin recovery consumes its final restart authority. This follows PR #166's direct verification of the first technical-exit registry marker and does not revisit PR #158's blocked prepared-world durability question.

## Finding

`NightmareTechnicalExitCoordinator` deliberately retains both the active `NightmareInstance` and the technical-exit marker while it clears any successful-completion receipt and resets the player to the existing recovered Carrier state. The final order was:

1. reset Soul/permanent identity in memory;
2. call `PlayerList.saveAll()`;
3. consume exact active ownership plus the technical-exit marker;
4. persist the registry.

That order assumes the player save is durable before the only remaining recovery authority is consumed. The entry-durability work already established that a normal save-method return is not, by itself, independent evidence that the expected player file image contains the new state. If the player write were absent while the final registry teardown became durable, restart could observe stale Aspirant/identity state with neither active ownership nor a technical-exit marker left to repair it.

This is a distinct final-authority boundary. It is not another checkpoint on a phase that retains an older replay token after the checkpoint.

## Implemented contract

After `PlayerList.saveAll()` and before `teardownActiveInstance()`, production now reads the exact `playerdata/<uuid>.dat` image and verifies the technical-exit recovery result itself:

- `shadowslave:soul` must decode to the existing recovered Carrier state (`SoulTransitions.infect(SoulData.uninfected())`);
- `shadowslave:identity` must decode to `SoulIdentityData.empty()` when present;
- an absent identity attachment is accepted because the registered attachment default is the same empty identity;
- missing player data, missing Soul attachment, invalid attachment payloads, stale Aspirant Soul state, or non-empty identity fail closed.

The verifier reads NeoForge's persisted `neoforge:attachments` container. NeoForge documents persistent entity attachments as serializer-backed data attachments and reserves `neoforge:attachments` for attachment persistence. The repository's attachment registration uses codecs for both `shadowslave:soul` and `shadowslave:identity`.

Direct value verification is deliberate rather than a changed-file digest requirement. A legitimate restart after the prior player-save boundary already has the recovered Carrier bytes on disk. Replaying technical exit must accept that already-correct persisted state and proceed to teardown; requiring a second byte change could deadlock an otherwise recoverable transaction.

## Evidence classification

- **CANON:** unchanged. Technical recovery and administrator abort remain technical operations, not ordinary Nightmare Spell mercy.
- **INFERRED:** none added.
- **DESIGN:** exact persisted player-side recovery state must be readable before Java consumes the final technical-exit recovery authority.
- **UNKNOWN:** physical power-loss/fsync guarantees below a readable compressed player file; behavior under storage-device corruption during replacement; whether a future attachment-storage format change needs a compatibility adapter rather than direct NBT inspection.
- **COMPATIBILITY:** no save-schema change. A restart after an already successful player save is accepted because the verifier checks the persisted target values rather than requiring another file change. Successful technical recovery/admin abort still converges to the same Carrier + empty permanent-identity result.

No new novel proposition is introduced. `docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` already separate crash/restart/admin recovery from in-world Spell mechanics.

## Tests

`PersistedTechnicalExitPlayerVerifierTest` covers:

- exact recovered Carrier plus empty identity;
- missing identity attachment reconstructing to the registered empty default;
- stale Aspirant Soul rejection;
- missing Soul and malformed identity rejection;
- missing player file rejection.

`NightmareTechnicalExitCoordinatorTest` additionally proves that a player-persistence proof failure leaves the durable technical-exit marker and live active ownership in place and does not reach teardown.

The existing restart-after-player-save reconstruction case remains important compatibility coverage: because exact persisted values are accepted, replay can finish teardown without demanding an artificial second file-image change.

## Remaining limits / next audit

This does not replace physical process-kill testing. It does not add verification to every later registry write where an older verified replay authority remains sufficient. Prepared Nightmare world/chunk persistence remains blocked under PR #158's recorded resume conditions.

After this boundary is green and review-clean, continue by auditing another independently owned recovery transaction only if complete loss of its remaining authority can be demonstrated to leave an unrecoverable persisted split. Do not add persistence checks mechanically.
