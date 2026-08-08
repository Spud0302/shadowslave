# Dream Realm story encounter composer — wave 1

**Status:** player-facing DESIGN composition slice.  
**Architecture:** Java resolves stable content identity; later persistence owns resolved instances; presentation/entity/quest adapters remain removable.  
**Lore rule:** `docs/LORE-SOURCE-POLICY.md` controls. This document does not claim a canonical Dream Realm encounter or quest-generation formula.

## Scope

This wave composes the already-authored Dream Realm region, settlement/faction/story, and Nightmare Creature catalogues into replayable encounter definitions.

A resolved encounter contains:

- generator version and deterministic seed;
- region and story-module identity;
- one region-compatible creature identity;
- one local hazard;
- one local NPC archetype and story hook;
- one creature-specific counterplay hook;
- one appraisal-evidence tag for later appraisal presentation;
- a combined arrival/presentation cue.

The composer does **not** spawn entities, mutate Soul/progression state, award rewards, calculate reputation, or persist itself. If a composed encounter becomes canonical player/world state, a later Java-owned persistence layer must save the complete resolved `EncounterDefinition` rather than invoking the generator again after restart or generator-version changes.

## Primary chapter evidence checked

Research followed `docs/LORE-SOURCE-POLICY.md` and rechecked primary chapter material plus later clarifications/examples:

- **Chapter 370 — Exploration Report:** Dream Realm regions are meaningfully described through geography, environment, landmarks, Nightmare Creature powers/behaviour/weaknesses, Memories and Echoes. This supports treating environment and creature counterplay as separable content inputs rather than a generic combat table.
- **Chapter 468 — Desecrated Grove:** established routes near a human Citadel can be patrolled while nearby wilderness remains materially more dangerous and creature locations affect route planning. This supports region-local encounter compatibility and travel pressure.
- **Chapter 752 — Solid Foundation:** Citadel/Gateway geography strongly constrains travel and local creature access; a safe anchor can coexist with dangerous surrounding traversal. This supports composing encounter content from location rather than treating every region as interchangeable.
- **Chapter 1608 — Death Zone:** Godgrave demonstrates an extreme region where environmental cycles, vegetation, creature ecology and safe movement are tightly coupled. This later example reinforces that the Dream Realm does not have one universal biome/encounter template.
- **Chapter 2273 — Shadow Clan:** a later Dream Realm organization explicitly performs information gathering, threat elimination, logistics, construction and remote-base work. This supports broad player-facing operational/story roles beyond fixed kill quests.

## Evidence boundary

- **CANON:** Dream Realm geography and environmental conditions materially affect travel, safety and creature ecology; Nightmare Creatures have distinct powers, behaviour and weaknesses; humans organize routes, bases and practical field work in the Dream Realm.
- **INFERRED:** a game can model region, local organization/story pressure, compatible creature pressure and explicit counterplay as separable authored concerns and then compose them into one player-facing encounter definition.
- **DESIGN:** `GENERATOR_VERSION = 1`; deterministic seed mixing; region affinity as the creature compatibility gate; selection of one hazard/NPC/hook/counterplay/evidence tag; exact encounter IDs; exact presentation concatenation; every weighting and selection rule.
- **UNKNOWN:** any canonical Dream Realm quest generator, encounter probabilities, spawn frequency, faction mission algorithm, reward formula, reputation math, appraisal weighting, or universal relationship between a region and specific creature appearances.
- **COMPATIBILITY:** existing wave-one region/story/creature IDs remain stable inputs. NeoForge structures, entities, dialogue, quest UI and other execution/presentation providers may consume resolved definitions but must not become canonical state owners.

## Determinism and persistence boundary

The generator sorts set-backed authored primitives before selection, so Java collection iteration order cannot change the result. Region identity, seed and generator version participate in resolution. The complete result records the selected component IDs.

This is deliberately a **future-generation** tool, not a persistence substitute. Existing resolved encounters must not reroll merely because:

- the server restarted;
- the player reconnected;
- catalogue ordering changed;
- weights/selection code changed in a future generator version;
- an optional presentation or execution dependency was removed.

## Validation

`DreamRealmStoryEncounterComposerTest` checks:

- every authored region composes successfully;
- every composition uses that region's story module, declared hazards and creature affinities;
- NPC/story/counterplay/evidence selections belong to their authored source definitions;
- identical seed + region produces an identical complete definition;
- a 512-seed Red Canopy sweep reaches all three local creatures, all three local hazards, all three story hooks and at least 40 distinct combinations;
- region identity changes deterministic results;
- resolved definitions carry generator metadata and complete stable component identity;
- unknown regions fail closed rather than silently inventing content.

No local Gradle/JUnit/client/server execution is claimed from the connector-only environment. Hosted CI is evidence only if a workflow registers for the exact PR head.
