# Drowned Bell native collapsed-quarry migration

**Date:** 2026-08-11  
**Scope:** migrate the existing later-era `collapsed_quarry_cut` landmark into the native Minecraft world-generation seam established by PR #272 and extended by PR #273.

## Repository position

This slice is stacked on PR #273 exact head `128229d32b4daa10ca4e09c1c9f35ef497640d17`. That head passed Preview Gates run `31490799952` before this branch was created.

The branch does not create a second Feature, site seed, coordinate authority, historical identity, biome pipeline, encounter planner or preview arena. It extends the existing `DrownedBellLaterAnchorFeature` and `StormLanternCoastNativePlacementPlan` only.

## Player-visible purpose

The later Drowned Bell site now has a third recognizable historical landmark eligible for normal native generation:

1. storm-battered belfry;
2. broken Sea Gate;
3. collapsed quarry cut.

The quarry executor is deliberately more than a marker block. It creates a narrow gravel/coarse-dirt approach between broken retaining shoulders, a fractured worked arch and a small rubble choke. The result provides traversal and environmental-information value while retaining the ruined-silhouette role of the original quarry approach.

## Stable geography and chunk boundary

The existing Java site plan places:

- `storm_belfry` at local site `(-20, -17)`;
- `collapsed_quarry_cut` at `(24, -20)`.

Therefore the quarry remains exactly `(+44 X, -3 Z)` from the belfry for every world seed.

The native projection established in #272 puts the belfry at local chunk coordinate `(8,8)`. The quarry consequently lands at local chunk coordinate `(4,5)`. Its executor is bounded to X `[-3,+3]` and Z `[-1,+5]` relative to that anchor, so the complete footprint stays inside its generated chunk. This avoids cross-chunk writes while Minecraft is generating neighbouring chunks independently.

## Lore / authority classification

- **CANON:** no new Shadow Slave lore proposition is introduced by this migration.
- **INFERRED:** unchanged accepted era-recognition inference from the Drowned Bell worldgen design: a historical site can remain recognizable in later ruined Dream Realm geography.
- **DESIGN:** exact quarry coordinates, compact cut geometry, retaining walls, palette, rubble state, surface placement, migration order and one-chunk footprint.
- **UNKNOWN:** canonical Drowned Bell existence, exact quarry architecture/materials, final Storm Lantern Coast terrain/flooding, how deeply a mature quarry should carve into terrain, and final ruin damage state.
- **COMPATIBILITY:** `NightmareHistoricalSiteCatalog` / `StormLanternCoastSitePlan` remain identity and geography authority. Minecraft native worldgen owns only physical blocks. The player's altered Nightmare reconstruction does not rewrite this later shared site.

No new primary-novel claim was required, so this slice reuses the already-reviewed lore boundary rather than introducing unsupported quarry lore.

## Dependency decision

No dependency is added.

Native Minecraft/NeoForge Feature placement remains sufficient for this chunk-safe static landmark. GeckoLib and SmartBrainLib do not reduce this structure-placement work. Curios and Veil remain unrelated. TerraBlender remains deferred because cross-region biome/terrain blending is still not the demonstrated bottleneck.

No third-party content-mod assets are copied. The executor uses bundled vanilla block materials as replaceable physical presentation.

## Validation contract

Focused deterministic tests pin:

- the quarry is the same existing Java historical piece;
- exact `(+44,-3)` belfry-relative geography across seed sweeps;
- stable local chunk coordinate `(4,5)` under the current projection;
- quarry, belfry and Sea Gate occupy distinct generated chunks;
- the complete quarry executor footprint remains within its owned chunk;
- native admission includes belfry + Sea Gate + quarry while drowned terraces remain explicitly non-native;
- the Feature still consumes Java placement authority and contains no Soul/Memory/appraisal ownership.

Because this changes physical worldgen, fresh exact-head Preview Gates are required: compile/all JUnit/package, datapack/resource validation, physical NeoForge client boot and dedicated-server gates.

## Still placeholder

- the Dream Realm dimension still uses the temporary fixed `ashen_expanse` biome globally;
- drowned harbour terraces remain command-fixture-only;
- coast terrain, shelter/wreckage, encounter ecology and environmental clues remain on migration fixtures;
- the quarry is a compact surface-cut silhouette rather than a mature modular underground structure pool;
- no true large macro-region terrain partition or persistent discovery journal exists yet.

## Next highest-impact slice

If this exact head is green, migrate `drowned_harbour_terraces` as the fourth and final stable historical later-era anchor through the same native frame. After all four anchor landmarks are native, bind encounter/clue planning to those generated positions before retiring more of the command-built Storm Lantern fixture.
