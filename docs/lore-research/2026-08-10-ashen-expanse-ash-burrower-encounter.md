# Ashen Expanse Ash Burrower encounter — evidence boundary

**Date:** 2026-08-10

## Scope

This slice gives the already-authored `ash_burrower` Nightmare Creature profile a removable Minecraft body and places one bounded hostile encounter inside the already-authored `ashen_expanse` Dream Realm region. It adds no new creature identity, reward rule, progression rule or canonical spawn ecology.

## Primary/later checks

- **Chapter 81 — Weaver's Eye (official WebNovel cross-check):** Nightmare Creature **Rank** is a distinct hierarchy; the chapter identifies Dormant as the first rank and distinguishes the rank ladder from other creature properties.
- **Chapter 201 — Lord of the Dead (novel chapter text through the owner-designated access layer):** later material explicitly treats Nightmare Creature **Class** as qualitatively meaningful rather than merely another rank number; higher classes can change a creature's capabilities and intelligence.
- **Chapter 488 (novel chapter text through the owner-designated access layer):** later context remains compatible with Dormant beasts being dangerous physical threats to mundane humans. This is used only as a broad danger constraint, not as a stat formula.

No source establishes an Ash Burrower, its appearance, Minecraft-scale body, spawn location, statistics, burrowing algorithm or exact senses. Those remain project design/unknown rather than canon.

## Classification

### CANON

- Nightmare Creature Rank and Class are separate concepts.
- Class can correspond to qualitative differences in creature capability.
- A Dormant creature can still be a meaningful physical danger; Dormant does not mean harmless.

### INFERRED

- A Java-owned Nightmare Creature identity can be executed by a replaceable Minecraft entity without allowing that entity class/model to become the identity authority.
- A region profile's existing creature-affinity hook can constrain which physical encounter is appropriate for a bounded region slice.

### DESIGN

- `Ash Burrower`, its Dormant Beast profile, and its Ashen Expanse affinity are existing project-authored content.
- One hostile Ash Burrower is placed near the physical `ruin_metal` hook when the development Ashen Expanse slice is entered.
- The current executor uses vanilla Silverfish dimensions, renderer, movement and hostile AI as a clearly replaceable placeholder.
- Repeated development entry avoids intentionally multiplying this executor inside the bounded slice.
- The entity has an empty loot table and grants no Memory, Echo, Soul Shard, progression or region-resource state.

### UNKNOWN

- Canonical Ash Burrower appearance, animation, sound, dimensions, statistics, burrowing behavior, vibration/sc scent implementation, combat intelligence and destruction behavior.
- Canonical Dream Realm encounter density, respawn timing, ecology, territory, spawn probabilities and reward/drop behavior.
- Any formula mapping Nightmare Creature Rank/Class to Minecraft attributes.

### COMPATIBILITY

`NightmareCreatureContentCatalog` owns the creature profile and `DreamRealmRegionContentCatalog` owns region affinity. NeoForge registration, Silverfish AI/rendering, world coordinates and development spawning are removable execution surfaces. Killing or observing the physical body does not mutate Soul progression, appraisal, Nightmare resolution, Memories, Echoes, resources, settlement state or canonical region identity.
