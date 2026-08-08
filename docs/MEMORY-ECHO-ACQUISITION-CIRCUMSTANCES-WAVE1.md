# Memory / Echo acquisition circumstances — wave 1

**Status:** player-facing content DESIGN layered on PR #107 acquisition provenance.  
**Architecture:** Java-owned authoritative acquisition source is an input; this layer may only choose compatible descriptive circumstance.  
**Source rule:** `docs/LORE-SOURCE-POLICY.md` remains controlling.

## Scope

`MemoryEchoAcquisitionCircumstanceCatalog` adds 15 authored circumstances anchored to existing Java content IDs rather than inventing a second source of truth.

Dream Realm region circumstances:

- Ashen Expanse — **Ash-Buried Find**;
- Chainfall Reach — **Guide's Exchange**;
- Glassmere Flats — **Dullglass Exchange**;
- Blackwater Steps — **Low-Water Recovery**;
- Thornwake Basin — **Briar Hunt Aftermath**;
- Mistwound Pass — **Verified Handoff**;
- Bonewhite March — **White-March Quarry**;
- Hollow Causeway — **Sealed Road Cache**;
- Storm Lantern Coast — **Cliffside Commission**;
- Red Canopy — **Flooded Root Cache** and **Canopy Hunt Aftermath**.

The Drowned Bell circumstances:

- `tower_held` — **After the Last Bell**;
- `villagers_evacuated` — **Above the Tide**;
- `flood_diverted` — **What the Flood Spared**;
- `creature_buried` — **Silence in the Rubble**.

Every circumstance supports both Memories and Echoes, but only for explicitly authored acquisition-source families. For example, Chainfall Reach can frame a known `TRANSFER`, while Bonewhite March can frame a known `SLAIN_CREATURE` acquisition. Passing the wrong source/anchor pair fails closed. Red Canopy intentionally demonstrates that one region can frame more than one already-resolved source without the circumstance layer rolling between provenance categories.

## Source freshness check

The owner-designated NovelFull chapter listing was checked at the start of this research. It currently listed through Chapter 3116. Official WebNovel currently reports 3131 chapters, so WebNovel is ahead of the designated access layer. The decisive mechanics used by this slice are much earlier than that gap, but the discrepancy is recorded rather than treating the third-party index as current publication authority.

## Primary lore evidence checked

### Chapter 370 — Exploration Report

**CANON:** Dream Realm exploration meaningfully combines geography, environments, landmarks, Nightmare Creature behavior/weaknesses, Memories and Echoes. Regional circumstances therefore matter to player experience; the chapter does not establish a reward-generation formula tied to geography.

### Chapter 380 — Above and Below

**CANON:** Dream Realm traversal conditions can materially shape a hunt and its aftermath. Sunny gathers resources from defeated creatures while responding to a region-specific environmental cycle. This supports treating local travel/hunt context as meaningful presentation without implying that a Memory or Echo must be awarded.

### Chapter 1383 — A Pile of Soul Shards

**CANON:** killing many Nightmare Creatures does not guarantee a Memory or Echo. This is strong negative evidence against turning a regional hunt circumstance into a deterministic drop rule.

### Chapter 1609 — Reclusive Saint

**CANON:** Masters deliberately hunt suitable Nightmare Creatures hoping to procure useful Echoes, while artificial Echoes also exist; official WebNovel independently confirms this chapter. The exact procurement probability remains unstated.

### Chapters 1959–1960 — In the Bag / Master Weaver

**CANON:** artificial Memory creation can be a deliberate craft process, including an entirely original Memory. This supports an artificial-creation provenance family, but not the project's exact `Cliffside Commission` circumstance or any universal crafting recipe.

### Chapter 531 — Dream Tournament

**CANON:** Memories and Echoes can be deliberately awarded as prizes by human institutions. This reinforces that a player-facing handoff circumstance need not imply a creature drop, while still not defining the project's transfer UI, prices, commission structure or reward rules.

## Evidence boundary

- **CANON:** Dream Realm geography and environmental conditions materially shape exploration and hunting; Memories/Echoes are not guaranteed by kills; people can deliberately pursue Echo acquisition through hunting; artificial Echoes and artificially created Memories exist; Memories/Echoes can be deliberately awarded by people/institutions.
- **INFERRED:** acquisition provenance and the local circumstance in which that known provenance is presented are useful separable Java concerns. A saved acquisition source should not be reconstructed from location or narrative flavor.
- **DESIGN:** all 15 exact circumstances, their titles/descriptions/evidence tags, region/source compatibility, Drowned Bell post-resolution handoffs/recoveries, deterministic selection, anchor model and generator version.
- **UNKNOWN:** all canonical Memory/Echo drop probabilities, regional reward rates, whether location influences Spell reward selection, universal crafting/commission rules, prices, settlement reward conventions, and whether a Nightmare resolution directly grants any Memory or Echo.
- **COMPATIBILITY:** Java-owned acquisition state supplies subject kind and authoritative source before circumstance composition. Region/scenario IDs are referenced from existing Java catalogues. HUD/chat/audio/NPC/loot adapters may present or execute an already-resolved result but cannot choose ownership/provenance.

No canonical generation, drop, quest-reward, regional-loot or appraisal formula is claimed.

## Drowned Bell limitation

The four Drowned Bell circumstances are explicitly **DESIGN** follow-up flavor for its already-authored terminal resolutions. Canon does not establish that completing a First Nightmare resolution grants a Memory/Echo, that locals hand one over, or that caches become available afterward. For that reason these entries accept only PR #107's already-authorized `AUTHORED_DISCOVERY` source. They cannot manufacture that source from the resolution ID.

## Validation expectations

`MemoryEchoAcquisitionCircumstanceCatalogTest` requires:

- all ten merged Dream Realm region IDs are covered;
- all four merged Drowned Bell resolution IDs are covered;
- all 15 primitives are unique, non-trivial and explicitly DESIGN;
- both current subject kinds are supported without changing their identity;
- source/anchor mismatches fail closed rather than inventing provenance;
- seed and evidence cannot change the authoritative acquisition source;
- positive evidence magnitude and map iteration order do not alter deterministic composition;
- matched evidence comes from the authored circumstance;
- Red Canopy demonstrates two different known provenance families without cross-source rolling;
- negative evidence and invalid anchors fail closed.

## Integration boundary

This branch is stacked only on PR #107 because it imports `MemoryEchoAcquisitionContextCatalog.AcquisitionSource`. It otherwise references content already present on `main`: `DreamRealmRegionContentCatalog` and `DrownedBellScenarioDefinition`.

After #107 merges, rebase this slice directly onto `main`. A later Java-owned acquisition record may persist source + circumstance ID + anchor + seed if exact narrative provenance must survive restart. It must never reroll or infer the source from the circumstance during load.
