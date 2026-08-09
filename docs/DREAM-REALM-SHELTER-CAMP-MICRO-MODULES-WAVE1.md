# Dream Realm shelter/camp micro-modules — wave 1

**Status:** DESIGN content slice; no runtime/persistence authority.  
**Generator:** `dream-realm-shelter-camp-micro-module-v1`

## Player-facing scope

This wave adds 20 authored shelter/camp decision modules: exactly two for each of the ten Dream Realm regions already present in `DreamRealmRegionContentCatalog`.

The five reusable DESIGN families are:

- shelter evaluation;
- watch and rest;
- camp abandonment;
- information exchange;
- temporary refuge.

Each module is anchored to an already-resolved Java-owned region ID and uses one hazard plus one opportunity already authored for that source region. The seed may choose between that region's two modules and between two authored approach cues. It does not choose region identity, invent a new hazard/opportunity, calculate rest recovery, schedule encounters, award loot, or persist state.

Examples include an exposed ruin lee in the Ashen Expanse, a chain-root bivouac in Chainfall Reach, provisional route-note exchange on Glassmere Flats, a flood-aware dry terrace in Blackwater Steps, stone-perimeter refuge in Thornwake Basin, paired observation against misleading sound in Mistwound Pass, hollow-structure refuge on Bonewhite March, marked gatehouse shelter on Hollow Causeway, storm-aware high camp on Storm Lantern Coast, and an elevated root refuge in Red Canopy.

## Lore research

Research followed `docs/LORE-SOURCE-POLICY.md` before implementation.

Primary/later chapter material checked:

- **Chapter 370 — Exploration Report:** geography, environment, landmarks, creature behavior/weaknesses and accumulated field observations are practical survival knowledge. This supports camp/refuge decisions consuming known local conditions rather than arbitrary hidden rules.
- **Chapter 468 — Desecrated Grove:** detailed maps, established routes and known danger locations materially improve travel safety, while dangerous wilderness and known creature areas can often be avoided. This supports treating route/camp information as useful but contextual rather than a guaranteed encounter contract.
- **Chapter 1608 — Death Zone:** much later material again shows a cohort forced to remain still for an extended period because moving under current environmental conditions would be more dangerous. This supports waiting/rest timing as a tactical concern distinct from combat.

Freshness check on 2026-08-09: the owner-designated NovelFull listing exposed through **Chapter 3116 — Princess of the Underworld**, while official WebNovel reported **3,131 chapters**. NovelFull is therefore used as the designated chapter-reading access layer, not as current-publication authority.

## Evidence classification

**CANON**

- Dream Realm geography/environment materially affects survival and movement.
- Maps, established routes, known dangers and accumulated field information can materially improve decisions.
- Waiting or remaining still can be preferable to moving when environmental conditions are lethal.

**INFERRED**

- A resolved region and a local shelter/camp decision are useful separable game-content concerns.
- Shelter evaluation, watch/rest, camp abandonment, temporary refuge and information exchange are useful authoring dimensions for turning known environmental pressure into player choices.

**DESIGN**

- All 20 exact modules, five families, approach cues, situations, decision prompts, choices, hazard/opportunity pairings, anti-overclaim boundaries, seed mixing and generator version.
- All ten Dream Realm regions and their exact hazard/opportunity vocabularies remain the existing project-authored DESIGN content they were before this slice.

**UNKNOWN**

- Any canonical camp/shelter generation or placement formula.
- Recovery amount, rest duration, stamina restoration, healing, safe-zone guarantees, watch effectiveness, random-encounter probabilities, creature-spawn cadence, sleep interruption, respawn behavior, food/water effects, flood/storm forecasts, loot/rewards, ownership, prices or progression consequences.
- Whether the Nightmare Spell or Dream Realm uses any procedural camping system resembling this catalogue.

**COMPATIBILITY**

- `DreamRealmRegionContentCatalog` remains Java authority for region, hazard and opportunity identity.
- A future Java-owned travel/encounter instance may persist a resolved module ID/seed and any authoritative rest/encounter result if exact replay matters.
- Blocks, structures, beds, particles, sound, NPC dialogue, HUD, food/item effects and encounter execution may be removable adapters, but they cannot own canonical region/progression/reward state.

No canonical generation, safe-zone, healing, encounter-frequency, spawn, reward or recovery formula is claimed.

## Validation contract

`DreamRealmShelterCampMicroModuleCatalogTest` requires:

1. exactly 20 unique modules and exactly two per merged region;
2. every module's pressure and opportunity to belong to its source region;
3. coverage of all five DESIGN camp families;
4. two approach cues and exactly three bounded choices per module;
5. deterministic same-seed composition;
6. a 2,048-seed sweep per region that preserves caller-supplied region identity and immutable module mechanics;
7. the sweep to reach both modules and both cues for each region;
8. explicit negative boundaries around safe zones, recovery/encounter/reward authority, edibility, forecasts and misleading information;
9. unknown or malformed region/module IDs to fail closed.

This slice adds no runtime world mutation, persistence, Soul/progression change, creature spawning, reward logic or external-mod authority.
