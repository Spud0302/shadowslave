# Storm Lantern Coast encounter ecology

**Date:** 2026-08-11  
**Scope:** bounded later-era Dream Realm encounter placement for the Storm Lantern Coast / Drowned Bell migration preview.

## Evidence checked

Primary Chapter 370 (`Exploration Report`) was rechecked under `docs/LORE-SOURCE-POLICY.md`. The chapter treats geography, environmental conditions, landmarks, and Nightmare Creature powers/behavior/weaknesses as practical exploration knowledge worth recording. The official Nightmare Spell compendium was also rechecked for the broad Dream Realm/Nightmare boundary: the Dream Realm is a vast ruined world inhabited by Nightmare Creatures, while Nightmares reconstruct ancient conflicts/events.

No source establishes this project's Storm Lantern Coast, Drowned Listener, Chainback, Bell Eater, exact encounter density, territorial ecology, or spawn formulas.

## Classification

- **CANON:** Dream Realm exploration involves dangerous Nightmare Creatures; geography/environment/landmarks and creature powers, behavior, and weaknesses can be materially useful practical knowledge.
- **INFERRED:** an authored Dream Realm region's creature affinities should become observable as geographically contextual encounter pressure rather than one universal fixed-coordinate spawn; terrain/landmark context can shape how a player encounters danger without becoming creature-identity authority.
- **DESIGN:** Storm Lantern Coast; all three authored affinity IDs; restricting this physical slice to the already-executable `drowned_listener` and `chainback`; the 2-4 encounter budget; mandatory flood-edge Listener pressure; one ruin-guard Chainback pressure; optional exposed-route pressures; exact anchor preferences, jitter ranges, seed mixing, cleanup tags, coordinates, messages, and persistence-required physical entities.
- **UNKNOWN:** canonical existence/appearance/Rank/Class of these project creatures; whether Bell Eater or any comparable canonical creature belongs at this site; exact creature population density, territoriality, migration, respawn, patrol, resource attraction, settlement-distance response, weather/time response, and whether a particular landmark always hosts danger.
- **COMPATIBILITY:** `DreamRealmRegionContentCatalog` remains Java authority for region identity and creature affinities, while `NightmareCreatureContentCatalog` / registered creature bindings remain identity authority for physical creature executors. The encounter planner and NeoForge spawn service only choose reproducible physical placement from already-authorized executable identities. They cannot award Soul Shards, change Rank/Class, own rewards, mutate progression, resolve a Nightmare, alter appraisal, or rewrite the historical site.

## Why Bell Eater does not spawn yet

Storm Lantern Coast already authors `bell_eater` as a region affinity, but no physical Bell Eater executor exists in the current integrated runtime. This slice therefore fails closed rather than substituting a vanilla mob and pretending the missing creature exists physically. When Bell Eater receives a real Java-owned creature definition + executor, the ecology planner can admit it through the same bounded capability check.

## Determinism and exploration

The site structure plan and encounter plan use separate deterministic seed streams. The same world seed reproduces the same encounter budget for debugging/replay, while different seeds vary budget size, placement offsets, and optional exposed-route composition. A player does not receive encounter markers; danger is discovered through traversal and creature cues.

## Dependency decision

No dependency is added.

- **SmartBrainLib 1.16.11:** already admitted for sufficiently complex creature AI, but this slice only chooses/places existing creature executors. It does not need another AI scheduler or alter those entities' behavior stacks.
- **GeckoLib 4.9.2:** existing presentation infrastructure remains unchanged.
- **TerraBlender:** remains deferred. This is still a bounded site/world migration fixture and does not demonstrate a biome-region blending bottleneck.
- **Curios / Veil:** unrelated to encounter placement.

No third-party content-mod model, texture, structure, sound, or other asset is copied.

## Deliberate limits / next step

This is the first encounter-budget seam, not mature ecology. It does not yet consume time/weather, resource value, safe-settlement distance, Rank/Class progression bands, respawn/population persistence, dens, patrol routes, or macro-region chunk generation. Those inputs should be added only as real world systems exist and can be tested without inventing canonical rules.
