# Drowned Bell native worldgen anchor — migration evidence

**Date:** 2026-08-11  
**Scope:** first native Minecraft world-generation execution of an already-authored era-linked Drowned Bell landmark.

## Why this slice

The active Storm Lantern Coast stack proves stable Java-owned site identity, deterministic later-era pieces, historical/later landmark correspondence, encounter budgets, environmental clues and local ecology context. Its physical execution is still a bounded command fixture that clears and rebuilds an area through `ServerLevel#setBlock`.

This slice opens the smallest native worldgen seam without attempting to migrate the entire coast at once:

- consume `StormLanternCoastSitePlan.drownedBellLater(worldSeed)` unchanged;
- derive one reserved native macro-area from the existing site seed;
- make the existing `storm_belfry` historical anchor define the site's native global coordinate frame;
- execute only that ruined belfry through a registered Minecraft `Feature` during biome decoration;
- keep every other coast/site/encounter/clue executor on the command fixture for now;
- keep the fixed Ashen Expanse preview as a regression/development fixture.

The native site is deliberately reserved east of the bounded preview origins so chunk-generated evidence and command-built regression geometry do not overwrite each other.

## Evidence classification

### CANON

No new canonical Shadow Slave proposition is introduced by this migration. Existing era-linked work retains the already-recorded canon boundary that Nightmares reconstruct Dream Realm history and the Dream Realm contains later ruined geography.

### INFERRED

No new lore inference is required. Reusing the same Java site/landmark plan when moving from a command executor to native Minecraft worldgen is an engineering compatibility decision.

### DESIGN

- the reserved 32 x 32 chunk native macro-area;
- its current chunk-coordinate range;
- using `storm_belfry` as the first native anchor;
- projecting the site's global origin from that belfry;
- placing the belfry at the selected chunk centre and current world surface;
- the exact feature/configured-feature/placed-feature IDs;
- biome decoration at the surface-structure step;
- the belfry block palette, dimensions and broken-side presentation;
- continuing to expose the entire Dream Realm through the temporary fixed `ashen_expanse` biome while region generation is unfinished.

### UNKNOWN

- final Storm Lantern Coast biome/macro-region terrain generation;
- final historical-site spacing rules at world scale;
- whether site coordinates should remain fixed across future terrain-generator revisions;
- final belfry architecture/materials/destruction state;
- mature native placement for the sea gate, quarry, terraces, generic landmarks, encounters and clues;
- terrain blending between future Dream Realm macro-regions;
- final discovery/navigation UX for locating this native site without a development command.

### COMPATIBILITY

`NightmareHistoricalSiteCatalog`, `StormLanternCoastSitePlan`, region content and related Java records remain identity/placement-plan authority. The custom Minecraft feature owns only physical block execution at an approved deterministic anchor. It cannot alter original-history baselines, a player's altered Nightmare result, appraisal/divergence evidence, Soul/progression, Aspect/Flaw/Memory/Echo identity, rewards, scenario resolution or persistence.

The player's altered Drowned Bell reconstruction still cannot rewrite this later Dream Realm landmark. The native feature consumes the later site plan derived from the original-history stream only.

## Dependency decision

No dependency is added.

Minecraft/NeoForge configured features, placed features and biome generation already provide the exact infrastructure needed for this first native-placement seam. TerraBlender remains deferred because this work has still not reached the cross-biome or cross-region terrain-blending problem TerraBlender is designed to solve. GeckoLib and SmartBrainLib are unrelated to static landmark placement.

## Validation target

Deterministic unit coverage must prove:

- same-seed native anchor identity;
- anchor confinement to the reserved macro-area;
- seed variability across that area;
- exact projection of all existing local site coordinates into one native global frame;
- the belfry remains the same Java-owned historical anchor selected by `StormLanternCoastSitePlan`.

Static resource coverage must prove:

- feature registration is hooked into the mod event bus;
- configured/placed feature resources reference the registered type;
- the Dream Realm's current fixed biome includes the placed feature;
- the native feature consumes the Java placement plan and does not call command-preview clear/teleport code.

Fresh exact-head hosted compile/all JUnit/package, datapack/resource validation and physical NeoForge client/server gates are required before the slice is called green. A data-pack registry decode failure or Minecraft 1.21.1 Feature API mismatch is a real blocker to fix rather than evidence that native worldgen should be bypassed.

## Next migration step

Once this first native anchor is green, migrate the remaining three historical later-era anchors as chunk-safe native pieces from the same global site frame. Only after all major site geometry is native should the command-built coast itself begin retiring. Encounter/clue planners can then attach to generated landmark positions rather than preview coordinates.
