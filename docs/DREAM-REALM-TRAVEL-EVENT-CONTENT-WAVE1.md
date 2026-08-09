# Dream Realm travel-event content — wave 1

**Status:** player-facing DESIGN content built on the merged Dream Realm region catalogue.  
**Generator:** `dream-realm-travel-event-v1`  
**Architecture:** Java owns the resolved region and travel-event identity. World, HUD, audio, particle, NPC, and animation adapters may present or execute a resolved result but do not choose canonical progression, rewards, or persistence state.

## Purpose

The merged `DreamRealmRegionContentCatalog` already gives each authored frontier region hazards, traversal modes, opportunities, landmarks, resources, arrival cues, and a broad travel rule. This wave adds bounded local travel decisions without inventing a canonical encounter director or travel simulation.

Every current region receives exactly two reusable travel events. Each event combines only a hazard and traversal mode already authored for that source region, plus two presentation cues, a pressure read, a decision prompt, three choices, and an explicit anti-overclaim boundary.

The caller supplies the already-resolved region ID. Deterministic composition may vary only which of that region's two authored events is selected and which authored approach cue is surfaced. It cannot roll a different region, creature, destination, spawn, reward, travel time, or progression result.

## Player-facing content

Wave one adds 20 events across five reusable DESIGN families:

- **WEATHER_EXPOSURE** — decide whether to move now, wait, or seek a less exposed route when local conditions become relevant;
- **DETOUR** — trade a direct route for a slower line when terrain or visibility makes the shortcut difficult to verify;
- **SHELTER_REST** — decide whether to keep moving, pause under available cover, or change route rather than treating rest as a free universal action;
- **CROSSING_VERIFICATION** — test currents, altitude, landmarks, junctions, or environmental cues before committing to a crossing;
- **ROUTE_ADAPTATION** — react when floodwater, growth, unstable ground, or structural damage changes an otherwise known route.

Examples include Ashen Expanse exposed-flat timing, Chainfall Reach altitude/chain verification, Glassmere resonance lulls without magical forecasting, Blackwater flood-route checks, Thornwake growth detours, Mistwound landmark verification, Bonewhite shelter-to-shelter crossings, Hollow Causeway junction marking, Storm Lantern surge/bell-condition checks, and Red Canopy flood-versus-canopy route adaptation.

No event requires a fight or grants a reward merely for choosing a route.

## Lore research

Research followed `docs/LORE-SOURCE-POLICY.md` before implementation. Chapter text remains the authority; host metadata is only an access/publication check.

### Primary and later material checked

- **Chapter 370 — Exploration Report:** Dream Realm geography, environment, notable landmarks, creature behavior/weaknesses, and personally accumulated field information are practical survival knowledge. This supports travel content being based on learned local conditions rather than a generic random-event table.
- **Chapter 380 — Above and Below:** the Chained Isles have region-specific environmental danger independent of Nightmare Creatures; altitude and the state/sound of the chains materially affect safe movement. This supports environment-driven route decisions and waiting/verification without implying a universal mechanic.
- **Chapter 468 — Desecrated Grove:** detailed maps, established routes, known danger locations, patrols, and avoiding known Nightmare Creature areas materially make travel safer; Sunny also avoids the Crushing because the relevant islands are not rising. This supports route selection and condition-sensitive movement rather than unavoidable encounter rolls.
- **Chapter 1608 — Death Zone:** much later Godgrave material again shows lethal environmental conditions forcing a cohort to stop moving and wait for survival, reinforcing that movement timing/rest can be a meaningful tactical decision separate from creature combat.

### Publication/access freshness check

At research time on **2026-08-09**, the owner-designated NovelFull listing exposed through **Chapter 3116 — Princess of the Underworld**, while official WebNovel reported **3,131 chapters**. NovelFull is therefore treated as the designated full-chapter reading access layer, not current-publication authority. The decisive claims in this slice are from Chapters 370, 380, 468, and 1608 and were cross-checked against official WebNovel where practical.

## Evidence classification

### CANON

- Dream Realm geography and environment can be directly dangerous and materially affect travel decisions.
- Maps, established routes, known dangers, landmarks, and accumulated field knowledge can make travel safer.
- Local environmental conditions can make waiting, changing route, or avoiding an area prudent.
- Environmental travel pressure is not reducible to Nightmare Creature combat.

### INFERRED

- A useful game-content boundary separates already-resolved region identity from a local travel decision encountered while moving through it.
- Verification, waiting, detouring, shelter use, and route adaptation are useful authoring concerns for representing environmental travel pressure.
- A deterministic presentation seed can vary authored circumstances without owning the authoritative region or a later gameplay consequence.

### DESIGN

- All 20 exact travel events and their names/IDs.
- The five `TravelFamily` categories.
- Every approach cue, pressure read, decision prompt, choice, hazard/traversal pairing, and anti-overclaim boundary.
- Exactly two travel events per current authored region.
- Seed mixing, event/cue selection, and generator version `dream-realm-travel-event-v1`.
- Any eventual Minecraft-scale execution such as movement slowdown, block checks, particles, weather visuals, route markers, prompts, or shelter interactions.

### UNKNOWN

- Any canonical Dream Realm travel-event generation or encounter-director formula.
- Event frequency, probabilities, cadence, travel-time formula, stamina/exhaustion equation, rest duration, safe-window duration, or route-cost calculation.
- Exact altitude/pressure curves, flood/tide cycles, corrosion values, mist duration, rockfall probability, flora growth rates, resonance/storm prediction rules, shelter spacing, collapse chances, or navigation accuracy rules.
- Whether a travel event should spawn a Nightmare Creature, award Soul Shards, Memories, Echoes, Attributes, appraisal credit, items, or any other progression/reward.
- Any universal rule that waiting, detouring, testing, or sheltering is always available or always safe.

### COMPATIBILITY

- `DreamRealmRegionContentCatalog` remains Java authority for region identity and its hazard/traversal vocabulary.
- `DreamRealmTravelEventCatalog` is Java-owned authored content that consumes that already-resolved identity; it does not replace region generation or persistence.
- A future Java-owned travel/encounter instance may persist the chosen event ID and seed if exact replay is required.
- Datapacks, NeoForge/client code, structures, particles, sound, weather presentation, NPCs, and UI may render/execute the resolved event but must remain removable adapters and cannot become canonical reward/progression authority.

## Validation contract

`DreamRealmTravelEventCatalogTest` checks:

1. exactly 20 unique events and exactly two for each of the ten merged regions;
2. every event references only a hazard and traversal mode actually present in its source region;
3. all five DESIGN travel families are represented;
4. every event has at least two approach cues, exactly three choices, substantive decision content, and an anti-overclaim boundary;
5. same seed + region is deterministic;
6. a 2,048-seed sweep per region never changes caller-supplied region identity or any authored mechanics of the selected event;
7. the sweep reaches both authored events and all authored cues for every region;
8. evocative resonance/bell/mist/darkness cases retain explicit anti-overclaim boundaries;
9. unknown/malformed region or event IDs fail closed.

This catalogue deliberately contains no spawn chance, reward chance, travel-time formula, or progression mutation.

## Integration limit and next slice

This wave is independently reviewable from the open landmark/resource/ecology/creature-role PRs and from the Nightmare correctness stack. No runtime integration is required to merge it.

A strong next player-facing slice is **Dream Realm shelter/camp encounter content** built from existing region opportunities and travel pressure: temporary refuge evaluation, watch/rest choices, camp abandonment, and information exchange. Keep rest recovery values, random-encounter probability, safe-zone guarantees, respawn behavior, and rewards explicitly UNKNOWN.