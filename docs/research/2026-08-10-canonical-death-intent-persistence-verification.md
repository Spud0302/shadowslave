# Canonical death intent persistence verification

## Scope

This slice hardens the first recovery authority in the restart-replayable canonical First-Nightmare death transaction. It does not change what canonical death means, add corpse/Gate consequences, or change Minecraft respawn behavior.

## Concrete persistence gap

The canonical-death transaction records an exact `NightmareInstance` in `shadowslave_nightmare_deaths.dat` before clearing completion state, resetting player state, or consuming active ownership. Before this correction, a normal return from `SavedDataPersistence.saveAndWait(server)` was treated as proof that the new marker existed durably.

The entry/completion persistence work established a stronger boundary: NeoForge's save path can finish its joined I/O flow without the caller independently observing that the expected file image changed. If the first death-marker write is silently rejected and no durable marker exists, later player/registry mutations can create the same class of zero-authority restart state that the death transaction was introduced to prevent.

## Correction

For a newly recorded canonical-death intent:

1. drain pending NeoForge I/O and capture the settled image of `world/data/shadowslave_nightmare_deaths.dat`;
2. record the exact death intent;
3. run the existing SavedData persistence/join;
4. require the settled death-marker file image to exist and differ from the captured image;
5. only then allow completion-receipt clearing, player reset, and ownership teardown.

If persistence or verification fails, the newly recorded in-memory marker is quarantined before the failure propagates. A quarantine failure is attached as suppressed evidence and does not replace the original persistence failure.

An already durable death marker found during restart replay skips the new-marker checkpoint. It is existing recovery authority and must not be rejected merely because replay does not rewrite the marker file before continuing.

## Primary lore evidence rechecked

Research followed `docs/LORE-SOURCE-POLICY.md`.

- **Chapter 1, `Nightmare Begins`: CANON.** First-Nightmare survival is required; death is failure and can release a Nightmare Creature into the waking world. This supports preserving canonical death as a terminal outcome rather than reinterpreting it as technical recovery.
- **Chapter 887, `Lapse of Judgment`: CANON, later confirmation.** A First-Nightmare challenger dies and a Nightmare Creature is released into the waking world. Official WebNovel exposes the same chapter/event.
- A current later-material spot check found no basis for changing ordinary First-Nightmare death into a safe Spell ejection.

No novel text is committed.

## Evidence classification

- **CANON:** ordinary First-Nightmare death is real failure/death, not normal safe Spell ejection; First-Nightmare death can release a Nightmare Creature into the waking world.
- **INFERRED:** none added by this persistence correction.
- **DESIGN:** a newly created Java death-recovery marker is not trusted until its expected persistence file has an observably changed settled image; rejected live authority is quarantined before later transaction phases.
- **UNKNOWN:** physical power-loss/fsync guarantees below the observed file image, whether a lower-level failing write nevertheless leaves valid bytes on disk, mature corpse/Gate behavior, exact creature consequence timing/location, and real process-kill convergence at this new verification boundary.
- **COMPATIBILITY:** existing durable death markers remain replayable without requiring an initial rewrite; successful-completion, technical/admin recovery, entry recovery, and persistence schemas are unchanged.

## Tests

`NightmareDeathCoordinatorTest` now additionally requires:

- initial death-intent persistence failure quarantines live authority and stops before later death-side mutation;
- initial death-intent verification failure does the same;
- quarantine failure is suppressed behind the original persistence error;
- an already durable death intent replays without demanding a new initial file change.

The existing restart-after-each-durable-boundary convergence matrix remains in place.

## Limitations and follow-up

This is an observable-file checkpoint, not a claim of power-loss atomicity. If storage returns contradictory outcomes, restart must still obey whatever valid marker image actually survived. No equivalent checkpoint should be added mechanically to later replayable phases unless a concrete zero-authority failure is demonstrated.
