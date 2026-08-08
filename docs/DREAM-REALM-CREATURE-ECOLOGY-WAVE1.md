# Dream Realm creature ecology — wave 1

**Status:** player-facing DESIGN content built only from already-merged region and Nightmare Creature primitives.  
**Architecture:** Java owns resolved region/creature identity. Presentation/execution adapters may consume a resolved ecology profile but cannot choose canonical state.  
**Generator version:** `dream-realm-creature-ecology-v1`

## Scope

This wave adds one ecology/presentation bridge for every region-to-creature affinity already present in `DreamRealmRegionContentCatalog` on `main` (22 pairs across 10 regions and 12 creatures).

Each profile combines only primitives that already exist on its two source catalogues:

- one authored region hazard;
- one authored region traversal mode;
- source-creature counterplay tags;
- two local approach cues;
- a habitat read, pressure read, practical counterplay read and explicit anti-overclaim boundary.

`compose(seed, regionId, creatureId)` receives the already-resolved region and creature. The seed may vary only which authored approach cue is surfaced. It cannot select a different creature, alter Rank/Class, create a spawn, determine occurrence frequency, award a reward, or persist state.

## Source-policy check

Research followed `docs/LORE-SOURCE-POLICY.md` before implementation.

At research time the owner-designated NovelFull listing exposed through **Chapter 3116 — Princess of the Underworld**, while official WebNovel reported **3,131 chapters**. The third-party access layer is therefore behind publication and is treated only as a reading-access layer, not publication authority. The decisive ecology evidence for this slice is much earlier and was checked directly in chapter text.

## Primary and later chapter evidence

### Chapter 370 — Exploration Report

Sunny's Dream Realm report records geography, environment and landmarks together with individual Nightmare Creatures' powers, behavior and weaknesses. This is strong primary evidence that environment and creature-specific behavior are both actionable field information rather than interchangeable background flavor.

Working access: NovelFull Chapter 370. Official WebNovel Chapter 370 was also checked.

### Chapter 380 — Above and Below

The Chained Isles combine region-specific environmental danger with islands that have different Nightmare Creature populations and threat levels. Movement is constrained by the environment itself, including changing altitude/pressure conditions. This supports treating environmental traversal and creature danger as interacting encounter concerns without implying a universal ecology formula.

Working access: NovelFull Chapter 380. Official WebNovel also exposes the chapter.

### Chapter 1461 — Encore

Later material explicitly uses known creature hunting grounds, routes and habits as strategic information. Sunny manipulates those expectations so several threats move and interact, changing which route is safer. This is strong later evidence that creature location/behavior and local geography can be tactically related.

Working access: NovelFull Chapter 1461. Official WebNovel Chapter 1461 was also checked.

### Chapter 1608 — Death Zone

Later Godgrave material again couples environmental lethality, timing/movement choices and Nightmare Creature encounters. The environment can itself determine when movement is viable even for powerful cohorts.

Official WebNovel Chapter 1608 was checked as the later clarification source.

## Evidence boundary

### CANON

- Dream Realm geography, environment and landmarks are meaningful survival/exploration information.
- Nightmare Creatures can have distinct powers, behavior and weaknesses that are learned and recorded.
- Regions can contain materially different creature populations and environmental dangers.
- Known creature hunting grounds, routes and habits can matter strategically.
- Environmental danger and creature encounters can interact with movement/timing decisions.

### INFERRED

- Stable region identity, stable creature identity and a local encounter/ecology presentation are useful separable game-content concerns.
- A player-facing encounter can combine a known regional hazard/traversal pressure with known creature counterplay without asserting that the combination is universal or inevitable.

### DESIGN

- All 22 exact region/creature ecology profiles.
- Every habitat read, pressure read, approach cue, counterplay read and anti-overclaim boundary.
- Selecting exactly one source-region hazard and traversal pressure per profile.
- Seeded approach-cue variation and generator version `dream-realm-creature-ecology-v1`.
- Existing region creature-affinity IDs and all exact project creature behaviors remain project-authored DESIGN content unless separately supported.

### UNKNOWN

- Any canonical Nightmare Creature spawn or encounter-generation algorithm.
- Spawn frequency, population density, migration, territory size, aggro/pathfinding, respawn or despawn rules.
- Any universal mapping from Rank/Class to habitat, pressure, AI, stats or ecological role.
- Whether the Nightmare Spell or Dream Realm uses a procedural ecology system resembling this catalogue.
- Region-specific reward/drop probability, Soul Shard yield policy, appraisal consequence or encounter difficulty scaling.

No canonical generation, ecology, spawn, reward or probability formula is claimed.

### COMPATIBILITY

- `DreamRealmRegionContentCatalog` remains the source of stable region IDs, hazards, traversal modes and region creature-affinity IDs.
- `NightmareCreatureContentCatalog` remains the source of stable creature IDs, Rank/Class and authored creature counterplay.
- This catalogue cannot create or reroll either identity and fails closed for a pair not already present in the merged region affinity set.
- A future Java-owned encounter instance may persist a resolved ecology profile ID/seed if exact presentation must survive restart.
- Entity AI, models, particles, sound, structures, biome placement, HUD and dialogue remain removable adapters and may not own progression, creature identity, Rank/Class or persistence authority.

## Validation contract

`DreamRealmCreatureEcologyCatalogTest` requires:

1. exact coverage of every current region/creature affinity and no extra pairs;
2. all profile hazards/traversal modes to belong to the source region;
3. all profile counterplay tags to belong to the source creature;
4. at least two authored approach cues plus substantive habitat/pressure/counterplay/boundary copy per pair;
5. deterministic same-seed composition;
6. a 256-seed sweep that cannot change caller-supplied region, creature, profile, hazard or traversal identity;
7. seeded cue variation to be reachable;
8. incompatible or unknown region/creature pairs to fail closed;
9. no catalogue-level spawn-chance, drop-chance or guaranteed-reward authority.

## Integration limit

This wave does not depend on the open Nightmare Creature encounter-role or presentation PRs. It can merge directly on `main` and later be combined with those layers by a Java-owned encounter definition. The compatibility profile describes local pressure for an already-resolved pair; it never decides which creature exists in the world.