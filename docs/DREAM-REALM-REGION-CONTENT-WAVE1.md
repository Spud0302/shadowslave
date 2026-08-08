# Dream Realm region content — Wave 1

**Status:** authored content foundation, not wired into world generation.  
**Classification:** Minecraft **DESIGN** constrained by verified Dream Realm geography/environment evidence.  
**Canonical state:** Java core; external worldgen/presentation providers may consume stable region IDs but do not own progression or region identity.

## Player-facing content

Wave 1 defines ten reusable frontier-region profiles:

- Ashen Expanse
- Chainfall Reach
- Glassmere Flats
- Blackwater Steps
- Thornwake Basin
- Mistwound Pass
- Bonewhite March
- Hollow Causeway
- Storm Lantern Coast
- Red Canopy

Each profile carries stable identity plus:

- environmental hazard families;
- traversal modes;
- shelter/resource/exploration opportunities;
- Nightmare Creature affinity IDs;
- landmark hooks for navigation and procedural placement;
- resource hooks for later gathering/crafting content;
- an arrival presentation cue;
- an authored travel rule intended to create region-specific decision making.

The intent is to avoid a Dream Realm made from interchangeable biomes. A region should change how a player navigates, scouts, camps and chooses encounters, not only which blocks are underfoot.

## Lore evidence checked

Research followed `docs/LORE-SOURCE-POLICY.md` and checked primary chapter material including later-region examples.

- **Chapter 370 — Exploration Report:** Dream Realm field knowledge is explicitly described in terms of geography, environment, landmarks, unusual regional phenomena, Nightmare Creature behaviour and weaknesses. This supports modelling a region as more than a visual biome.
- **Chapter 380 — Above and Below:** the Chained Isles combine floating islands, enormous chains, an apparently endless Sky Below, distinct creature populations by island and a dangerous altitude effect. This is strong evidence that traversal rules can be intrinsic to a region.
- **Chapter 554 — Coming Clean:** the Crushing varies with island position and altitude, showing that environmental hazards can be conditional and route-sensitive rather than static damage fields.
- **Chapter 1608 — Death Zone:** Godgrave's exposed white sky, cloud cover, surface/hollow ecology and rapidly changing scarlet life demonstrate an environment whose safe travel window and creature ecology are coupled to a regional hazard cycle.
- **Chapter 2801 — Contagion:** the Glass Hell / Red Hill material shows long-distance trade routes, a hazardous surrounding sea, local extractive resources, settlements that provide respite, and regional mining risks. This supports pairing danger with economic and shelter opportunities.
- **Chapter 1759 — Return to the Forgotten Shore:** the Forgotten Shore later changes dramatically after major events, reinforcing that region state can evolve and should not be hard-coded as one eternal visual snapshot.

## Evidence classification

**CANON**

- Dream Realm regions can have radically different geography and environmental rules.
- Regional hazards can constrain traversal in ways unrelated to ordinary creature combat.
- Regions contain distinctive landmarks, resources, settlements/routes and creature ecologies.
- A region can materially change over time after major events.

**INFERRED**

- A Minecraft region definition should expose traversal, hazard, shelter/resource and encounter-affinity data separately so gameplay systems can compose them without reducing the region to a biome ID.
- Route planning, observation and shelter should sometimes be as important as direct combat.

**DESIGN**

- All ten Wave-1 region names.
- Every exact hazard/traversal/opportunity assignment.
- All landmark and resource hooks, arrival cues, travel rules and creature-affinity mappings.
- The Java enums used to classify authored content.

**UNKNOWN**

- There is no claimed canonical procedural world-generation formula.
- There is no claimed universal region taxonomy, fixed hazard count, resource-generation rule or creature-spawn algorithm.
- Exact geographical placement relative to canon locations is deliberately not assigned in this slice.

**COMPATIBILITY**

- The catalogue is standalone on `main` and imports no unmerged content classes.
- Creature affinities are stable string IDs so PR #80 can later be joined without forcing this PR to stack on it.
- No Minecraft biomes, dimensions, chunks, structures, loot tables, player state or persistence are changed.

## Validation

`DreamRealmRegionContentCatalogTest` checks:

- ten unique stable region IDs;
- coverage of every Wave-1 hazard, traversal and opportunity family;
- at least three hazards, two traversal options, three opportunities, three landmarks and three resource hooks per region;
- all twelve first-wave creature IDs from PR #80 appear in at least one region affinity set;
- at least thirty distinct landmark hooks and thirty resource hooks;
- distinct arrival cues and travel rules for every region.

## Integration boundary

A later world-generation adapter may translate stable region definitions into biomes, structures, weather, terrain and encounters. That adapter remains removable presentation/execution infrastructure. Java-owned content identity and any future persistent exploration/progression state remain authoritative.

The best next content slice after this region foundation is an **Echo content catalogue**: persistent creature-derived companion identities with role/behaviour descriptors and explicit separation between canon Echo identity and removable Minecraft entity/AI presentation.