# Nightmare completion recovery

**Status:** implementation record for Issue #34.  
**Scope:** successful completion of the playable Java First Nightmare preview.  
**Classification:** canonical presentation order plus Minecraft recovery **DESIGN**.

## Lore evidence checked

The implementation was checked against primary chapter material before changing the lifecycle:

- **CANON — Chapter 15:** after the First Nightmare ends, the challenger enters the Spell's appraisal space; the Spell appraises the completed trial, changes the Aspirant to Dreamer, and reveals/evolves the resulting identity before waking-world continuation.
- **CANON — Chapters 742–744:** the Second Nightmare reaches its terminal outcome, collapses, presents appraisal in the space between dream and reality, and then performs Ascension/return.
- **CANON — Chapter 1581:** a later exceptional interruption explicitly lacks the normal appraisal space, reinforcing that appraisal normally belongs between Nightmare collapse and return.
- **DESIGN:** Minecraft save files do not provide one atomic transaction spanning overworld `SavedData`, player attachments, position and entity cleanup. The durable receipt and replay coordinator are technical implementation choices, not mechanics attributed to the Nightmare Spell.

The source text was used for mechanics and ordering only. No novel passages are committed here.

## Previous failure window

The old preview path performed:

```text
return player
-> remove entities and consume active Nightmare ownership
-> write appraisal identity and Dreamer progression
```

If active ownership reached disk before player appraisal, restart recovery had no surviving record from which to finish success. The player could remain an Aspirant outside the Nightmare with neither active ownership nor an appraisal.

## Implemented transaction

The new path is:

```text
record TERMINAL_RESOLUTION_RECORDED
-> synchronously save overworld Nightmare SavedData
-> reconcile/apply appraisal
-> synchronously save player data
-> record APPRAISAL_COMMITTED and save SavedData
-> return player and synchronously save player data
-> record RETURN_COMMITTED and save SavedData
-> remove exact owned entities and active ownership
-> record TEARDOWN_COMMITTED and save SavedData
```

The completion receipt retains the full `NightmareInstance` snapshot and remains stored after teardown. This is intentional: the player file and overworld SavedData can reach disk in either order. A retained receipt lets login recovery compare the phase with actual observed state instead of assuming which file is newer.

## Recovery rules

Observed state determines which idempotent action still needs to run:

- appraisal is replayed only when the exact expected Dreamer Soul and fixed preview identity are absent;
- return is replayed only while the player is still in the Nightmare dimension;
- teardown is replayed only while the exact active ownership remains;
- persisted reconstruction rejects a player whose active Nightmare instance differs from the instance named by that player's retained successful-completion receipt;
- persisted reconstruction also rejects one Nightmare instance UUID being assigned to different players across active ownership and retained completion state;
- persisted reconstruction treats each allocated scenario slot as belonging to one instance UUID across active and retained completion state;
- persisted reconstruction requires an active instance and its retained completion snapshot to agree on the persisted scenario `origin` and `altar` when both records exist;
- an active instance cannot change its allocated slot through `update`;
- phase markers advance monotonically and cannot skip a milestone;
- unrelated Soul or identity state is rejected rather than overwritten;
- `preview_reset` explicitly clears the retained receipt before publishing the reset snapshot.

The cross-instance reconstruction check is deliberately fail-closed. Without it, corrupted SavedData could retain completion X while also restoring a newer active Nightmare Y for the same player. Completion recovery would correctly refuse to tear down Y, but could still return the player using X's recorded return location and then suppress normal active-instance login handling. The supported transaction never creates that pairing, so recovery must reject it instead of guessing which instance owns the player.

Instance UUIDs are likewise global persisted identities, not merely keys local to one map. If active ownership for Alice and a retained completion receipt for Bob claim the same instance UUID, exact-instance recovery and audit history become contradictory. The supported transaction cannot create that state, so reconstruction rejects it in either load order rather than choosing one owner.

Slots are persistent physical namespaces in the current preview: `LastSignalScenario.originForSlot` maps a slot directly to a separated region of the shared Nightmare dimension. The allocator is monotonic and retained completion receipts keep the original slot, so two different instance UUIDs claiming one slot is not a supported historical state. Allowing it could make restart reconstruction accept overlapping scenario geometry or make later ownership/history disagree about which instance owns that region. Reconstruction therefore rejects duplicate slot ownership across active instances and retained receipts, while still allowing one active instance and its own receipt to carry the same slot during normal completion recovery.

The completion receipt is a snapshot of the same active instance at terminal resolution, so its persisted layout cannot legitimately diverge from the still-active record before teardown. If `origin` or `altar` differs for the same player, instance UUID and slot, recovery no longer has one unambiguous physical scenario location: interaction checks, entity cleanup and future geometry reconstruction could target different places depending on which record is read. Reconstruction therefore rejects that split state in either load order rather than choosing one layout. This is a persistence **DESIGN** invariant; it does not assert a lore rule about Nightmare geometry.

`PreviewAppraisalService` accepts the exact already-appraised state and repairs only the two safe split states:

1. expected identity present while the Soul is still Aspirant;
2. expected Dreamer Soul present while identity is empty.

It does not replace unrelated progression or identity.

## Automated evidence

`NightmareRegistryDataTest` and `NightmareRegistryLayoutRecoveryTest` now prove:

- completion receipts round-trip at every phase;
- active ownership remains available until teardown commit;
- the receipt survives after active ownership is consumed;
- phase progression is ordered and replay-idempotent;
- duplicate owners, instance IDs and receipts fail closed;
- a retained completion receipt cannot coexist with a different active instance for the same player, regardless of reconstruction order;
- one instance UUID cannot belong to different players across active and completed state, regardless of reconstruction order;
- one physical slot cannot belong to different instance UUIDs across active or retained completion state;
- the same active/completed instance cannot disagree on persisted `origin` or `altar`, regardless of reconstruction order;
- registered active instances cannot move to another allocated slot through the update path.

`NightmareCompletionRecoveryPlanTest` proves that replay actions follow actual durable player/registry state rather than phase alone.

`NightmareCompletionCoordinatorTest` uses deterministic simulated crashes immediately after every persistence boundary:

- appraisal player save;
- appraisal phase save;
- return player save;
- return phase save;
- teardown phase save.

After each simulated restart, only durable state is reconstructed and the coordinator is rerun. The final committed state contains one appraisal application, one return and one teardown.

These are unit/storage integration results. A physical dedicated-server restart matrix is still required before claiming real process-restart evidence.

## Physical verification still required

For each boundary, a dedicated test build should expose a one-shot test hook that stops progression immediately after the selected synchronous save. The operator then stops/restarts the same world and verifies:

- the player becomes Dreamer with Dormant Soul Rank;
- Last Light, Awakened Aspect Rank and Cold Ash are present once;
- the player is outside the Nightmare dimension;
- exact active ownership and pursuer are absent;
- the retained receipt is `TEARDOWN_COMMITTED`;
- another player's active instance is unchanged.

Do not claim this physical evidence from the coordinator unit test or ordinary CI smoke boot.

## Retention and future work

The preview retains one successful receipt per player until explicit `preview_reset`. A future general appraisal/history system should migrate these receipts into permanent Nightmare history rather than deleting them immediately.
