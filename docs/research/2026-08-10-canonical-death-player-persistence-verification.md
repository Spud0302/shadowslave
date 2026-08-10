# Canonical-death player persistence verification — 2026-08-10

## Scope

Audit the player-side commit immediately before canonical First-Nightmare death consumes active Nightmare ownership and then clears its dedicated death-recovery marker. This follows the verified initial death-intent persistence boundary from PR #164 and the analogous technical-exit player-side audit in PR #173. It does not revisit PR #158's blocked prepared-world durability question.

## Finding

`NightmareDeathCoordinator` already retained a durably trusted canonical-death marker while it cleared any successful-completion receipt and reset the player's permanent identity and Soul state. The final player-side order was:

1. reset Soul and permanent identity in memory;
2. call `PlayerList.saveAll()`;
3. consume exact active Nightmare ownership;
4. persist the Nightmare registry;
5. clear the canonical-death marker;
6. persist the death registry.

That sequence relied on normal return from `PlayerList.saveAll()` as sufficient proof that the expected player file contained the death-reset state. If the player write were absent or stale while the later registry/death-marker writes became durable, restart could observe an Aspirant or otherwise stale player attachment state with neither active Nightmare ownership nor a canonical-death marker left to repair it.

This is a distinct final-authority boundary: after the marker is eventually cleared there is no older death-specific replay token remaining.

## Implemented contract

After `PlayerList.saveAll()` and before `teardownActiveInstance()`, production now opens the exact `playerdata/<uuid>.dat` image and verifies the canonical-death reset result itself:

- `shadowslave:soul` must decode through the repository codec to `SoulData.uninfected()`;
- `shadowslave:identity` must decode through its repository codec to `SoulIdentityData.empty()` when present;
- an absent identity attachment is accepted because the registered attachment default is the same empty identity;
- missing player data, missing Soul attachment, malformed attachment payloads, Carrier state, stale Aspirant state, or non-empty identity fail closed.

The coordinator exposes this as an explicit `verifyPlayerPersisted()` boundary after `persistPlayer()` and before active-ownership teardown so ordering and failure behavior are directly regression-tested.

Direct value verification is deliberate rather than a changed-file digest requirement. A legitimate restart immediately after an already-successful player save can begin with the exact uninfected/empty-identity bytes already on disk. Those bytes are sufficient player-side recovery evidence; demanding a second byte change could reject a valid replay. This differs from the first death-intent checkpoint, where a new recovery authority must be shown to have changed its dedicated persistence image.

## Evidence classification

- **CANON:** unchanged. Ordinary First-Nightmare death remains the existing canonical failure/death path; no new death, Gate, creature-release, appraisal, progression, return, Aspect, Flaw, Seed, or Dream Realm rule is introduced.
- **INFERRED:** none added.
- **DESIGN:** exact persisted player-side death-reset state must be readable before Java consumes the final canonical-death recovery authority.
- **UNKNOWN:** physical power-loss/fsync guarantees below a readable compressed player file; storage corruption or replacement failure after the read; mature corpse/Gate behavior and exact Nightmare Creature consequence details; physical process-kill convergence at this exact boundary.
- **COMPATIBILITY:** no save-schema change. Already-correct persisted death-reset state is accepted on replay. Technical/admin recovery remains distinct and still converges to Carrier + empty identity rather than this death path's Uninfected + empty identity result.

No new novel proposition is introduced. Existing canonical-death lore evidence and `docs/JAVA-LORE-ALIGNMENT.md` remain the governing boundary; this change is persistence/recovery DESIGN.

## Tests

`PersistedCanonicalDeathPlayerVerifierTest` covers:

- exact Uninfected Soul plus empty identity;
- absent identity reconstructing to the registered empty default;
- Carrier rejection, preserving the semantic distinction from technical recovery;
- stale Aspirant rejection;
- missing Soul, malformed identity, and missing player-file rejection.

`NightmareDeathCoordinatorTest` additionally proves:

- ownership is consumed only after player persistence verification succeeds;
- verification failure retains the durable death marker and active ownership and cannot reach teardown;
- the existing restart-after-every-durable-boundary convergence matrix remains intact.

## Remaining limits / next audit

This does not replace physical process-kill testing and does not claim filesystem durability below the readable player-file observation. It does not add checkpoints mechanically to later writes when an older verified replay authority remains sufficient.

Prepared Nightmare world/chunk persistence remains blocked under PR #158's recorded resume conditions and was not retried. After this slice is green and review-clean, continue with a fresh audit for another independent recovery authority only when complete loss of that authority can be shown to create an unrecoverable persisted split.
