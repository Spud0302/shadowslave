# Ash Compass -> Cinder Rest runtime integration

**Date:** 2026-08-11

## Why this slice exists

The existing Ash Compass Memory is already soul-owned, summonable/dismissable, and physically usable. Its runtime direction effect nevertheless targeted the player's vanilla respawn position. That position is not Java-owned Dream Realm settlement state and can be changed for unrelated Minecraft reasons, so it could make the Memory report a false "refuge" while the actual authored Cinder Rest integration remained elsewhere.

This slice removes that authority leak. The compass now reads the physical anchor produced from `DreamRealmWorldStoryIntegration.cinderRest()` inside the bundled Dream Realm and reports that Cinder Rest lies beyond the current realm elsewhere.

No new Memory, settlement, enchantment, resource, reward, progression rule, or catalogue is added.

## Primary material checked

Research follows `docs/LORE-SOURCE-POLICY.md`.

- **Chapter 74 — `Midnight Shard`:** official WebNovel chapter identity/publication was rechecked. This early Memory material supports Memories being distinct supernatural objects with their own qualities; it does not establish this project's Ash Compass or a refuge-finding enchantment.
- **Chapter 694 — `Key Piece`:** chapter text was rechecked through the owner-designated full-chapter access layer. It explicitly treats summoning/dismissing, soul connection and distinct Memory enchantment/purpose as separable qualities of Memories. It does not establish Cinder Rest, `ember_north`, Minecraft coordinates, compass directions, or respawn semantics.

Only those broad Memory constraints are used here. No novel wording is copied into runtime content.

## Evidence classification

- **CANON:** Memories are distinct soul-associated supernatural objects; they can be summoned/dismissed and can possess individual enchantments/purposes.
- **INFERRED:** a project-authored Memory effect should resolve from the authoritative state of the thing it is meant to indicate rather than from unrelated client/vanilla convenience state.
- **DESIGN:** Ash Compass, Cinder Rest, `ember_north`, the refuge-finding function, eight-direction text, approximate block distance, one-second cooldown, exact physical settlement coordinate and the rule that the reading only resolves inside the bundled Dream Realm.
- **UNKNOWN:** whether any canonical Memory behaves like a compass to a refuge; canonical targeting/range/inter-realm semantics; whether a comparable enchantment would update continuously, consume essence, show visual direction, or point through Gate/realm boundaries.
- **COMPATIBILITY:** Java-owned `DreamRealmWorldStoryIntegration` remains settlement/location authority and `MemoryOwnershipData` remains Memory ownership authority. The Minecraft item only reads those states and presents the result. Vanilla respawn state, item model, UI text and future rendering cannot manufacture or move canonical Dream Realm state.

## Runtime boundary

The physical Cinder Rest anchor is derived from the same `DreamRealmWorldStoryIntegration.cinderRest()` coordinates used to place the Watch Captain. The Ash Compass no longer calls `getRespawnPosition()` or `getRespawnDimension()` to infer its target.

This keeps the implementation replaceable: future Dream Realm world generation can move the settlement by changing Java-owned integration state, and the Memory effect will follow that state instead of requiring a second hard-coded target or player respawn mutation.
