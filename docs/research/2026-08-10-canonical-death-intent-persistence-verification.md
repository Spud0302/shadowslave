# Canonical death intent persistence verification

## Scope

This slice hardens the first recovery authority in the restart-replayable canonical First-Nightmare death transaction. It does not change what canonical death means, add corpse/Gate consequences, or change Minecraft respawn behavior.

## Concrete persistence gap

The canonical-death transaction records an exact `NightmareInstance` in `shadowslave_nightmare_deaths.dat` before clearing completion state, resetting player state, or consuming active ownership. Before this correction, a normal return from `SavedDataPersistence.saveAndWait(server)` was treated as proof that the new marker existed durably.

The entry/completion persistence work established a stronger boundary: NeoForge's save path can finish its joined I/O flow without the caller independently observing that the expected file image changed. If the first death-marker write is silently rejected and no durable marker exists, later player/registry mutations can create the same class of zero-authority restart state that the death transaction was introduced to prevent.

## Correction

For a newly recorded canonical-death intent:

1. drain pending NeoForge I/O and capture the settled image of `world/data/shadowslave_nightmare_deaths.dat`;
2. record the exact death intent and advance a persisted `persistence_revision` proof value;
3. run the existing SavedData persistence/join;
4. require the settled death-marker file image to exist and differ from the captured image;
5. mark the exact live marker durably trusted only after verification succeeds;
6. only then allow completion-receipt clearing, player reset, and ownership teardown.

A P1 review of the initial implementation correctly identified that deleting an unverified live marker was unsafe. A save can throw after the marker actually reached disk, or verification can fail while reading a successful write. Removing that marker from memory and dirtying the marker-less registry could later overwrite valid on-disk recovery authority.

The corrected failure behavior is fail-closed without destructive quarantine: persistence or verification failure retains the exact live marker as *unverified* and stops before later death-side mutation. If the same server process retries, `begin(...)` advances `persistence_revision` again and dirties the registry, guaranteeing that a genuinely successful retry has a distinguishable settled file image even when an earlier ambiguous write already produced the same marker payload. Once verification succeeds, the transient unverified state is cleared. After restart, markers loaded from the persistence file are trusted recovery authority because they were reconstructed from disk.

The `persistence_revision` field is a compatible technical proof field. Older marker files omit it and load as revision zero; no Nightmare identity or gameplay semantics are derived from it.

## Primary lore evidence rechecked

Research followed `docs/LORE-SOURCE-POLICY.md`.

- **Chapter 1, `Nightmare Begins`: CANON.** First-Nightmare survival is required; death is failure and can release a Nightmare Creature into the waking world. This supports preserving canonical death as a terminal outcome rather than reinterpreting it as technical recovery.
- **Chapter 887, `Lapse of Judgment`: CANON, later confirmation.** A First-Nightmare challenger dies and a Nightmare Creature is released into the waking world.
- No implementation rule in this correction depends on later material beyond that confirmation.

No novel text is committed.

## Evidence classification

- **CANON:** ordinary First-Nightmare death is real failure/death, not normal safe Spell ejection; First-Nightmare death can release a Nightmare Creature into the waking world.
- **INFERRED:** none added by this persistence correction.
- **DESIGN:** a newly created Java death-recovery marker is not trusted until its expected persistence file has an observably changed settled image; ambiguous live authority is retained but cannot authorize later transaction phases; a persisted proof revision makes same-process retries distinguishable.
- **UNKNOWN:** physical power-loss/fsync guarantees below the observed file image, whether a lower-level failing write nevertheless leaves valid bytes on disk, mature corpse/Gate behavior, exact creature consequence timing/location, and real process-kill convergence at this verification boundary.
- **COMPATIBILITY:** existing persisted death-marker files load with proof revision zero and remain replayable; the added field does not alter Nightmare identity or terminal semantics; successful-completion, technical/admin recovery, and entry recovery are unchanged.

## Tests

`NightmareDeathCoordinatorTest` requires:

- initial death-intent persistence failure retains exact live authority but does not promote it or allow later death-side mutation;
- initial death-intent verification failure does the same;
- successful verification promotes authority before later mutation;
- an already durable death intent replays without demanding another initial file change;
- restart after every later durable boundary still converges to the canonical-death outcome.

`NightmareDeathRegistryDataTest` additionally requires:

- a newly begun marker is not durably trusted until promoted after verification;
- retrying an unverified marker advances the persisted proof revision, creating a distinguishable file image for another checkpoint attempt;
- promoting the marker stops further proof revisions for normal replay;
- a marker loaded from persisted data is trusted restart authority;
- stale snapshots and malformed/duplicate marker data retain their existing fail-closed behavior.

## Limitations and follow-up

This remains an observable-file checkpoint, not a claim of power-loss atomicity. A process can still die below the observation boundary, and storage can still produce contradictory lower-level outcomes. No equivalent checkpoint should be added mechanically to later replayable phases unless a concrete zero-authority failure is demonstrated.
