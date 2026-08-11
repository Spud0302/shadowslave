# Drowned Bell native Sea Gate migration — 2026-08-11

## Scope

This note records a bounded execution migration stacked on PR #272. It does not introduce new Shadow Slave lore or a second world/site authority.

The existing Java-owned era-linked site plan already maps the historical Drowned Bell **Sea Gate** to the later Storm Lantern Coast `sea_gate` / `broken_sea_gate` landmark. This slice makes that second later-era landmark physically generate through the same native Minecraft Feature already used by the storm belfry.

## Repository state checked

Before implementation, current `main`, open PRs/issues, `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, active PR #248's `docs/design/DREAM-REALM-ERA-LINKED-WORLDGEN.md`, and active correctness/integration work were re-read.

Live GitHub state is materially newer than the root status/handoff snapshots. PR #272 is the current native worldgen edge and is therefore the parent of this change. Combat PRs #256/#260/#263/#264/#270 and correctness PR #268 are intentionally not duplicated or modified.

## Authority and lore classification

- **CANON:** no new canon proposition is introduced by this migration.
- **INFERRED:** no new inference is required. The existing reviewed era-linked site model already establishes the project inference that a reconstructed historical site can remain recognizable in later Dream Realm geography.
- **DESIGN:** the broken Sea Gate's exact Minecraft silhouette, palette, one-block-thick compact geometry, surface placement, current chunk-safe orientation, and migration order are project design.
- **UNKNOWN:** final Storm Lantern Coast terrain, exact later Sea Gate architecture, mature flooding/burial state, native placement of the quarry/terraces/encounters/clues, and final discovery/navigation UX.
- **COMPATIBILITY:** `NightmareHistoricalSiteCatalog` / `StormLanternCoastSitePlan` / `StormLanternCoastNativePlacementPlan` remain Java authority for site identity and geography. Minecraft Feature execution owns blocks only. This slice cannot alter original history, the player's resolved Nightmare fate, divergence appraisal, permanent progression, Memories, Echoes, Soul state, or the later shared site's original-history lineage.

The player's altered Nightmare reconstruction still does **not** rewrite this later Dream Realm site.

## Physical execution

The existing worldgen Feature now asks the Java placement plan which explicitly migrated historical piece, if any, belongs to the currently generated chunk.

Admitted native pieces in this slice:

1. `storm_belfry` — existing PR #272 anchor;
2. `sea_gate` — new second native historical piece.

Not admitted yet:

- `collapsed_quarry_cut`;
- `drowned_harbour_terraces`;
- generic coast pieces;
- encounter/clue planners.

The Sea Gate's global coordinate is still projected from the belfry-established site origin. Relative geography therefore remains exactly the existing authored `(+38 X, +35 Z)` from storm belfry to Sea Gate for every world seed.

The current projection places the Sea Gate at local chunk coordinate `(14, 11)`. Its compact broken-gate silhouette deliberately grows only along Z/upward so this migration does not write across chunk-generation boundaries.

## Tests added/strengthened

Focused coverage now pins:

- Sea Gate remains an existing historical anchor from the Java site plan;
- exact `(+38, +35)` relative geography to the belfry across a seed sweep;
- Sea Gate and belfry occupy distinct generated chunks;
- Sea Gate local chunk coordinate remains `(14, 11)` under the current projection;
- both explicitly migrated pieces resolve through `nativePieceForChunk`;
- quarry and drowned terraces remain non-native on this branch;
- the executor consumes Java placement authority and contains no appraisal, Memory or Soul authority;
- native placement still uses the existing configured/placed feature and surface generation path.

## Dependency decision

No dependency added. Native Minecraft/NeoForge worldgen remains sufficient for this chunk-safe site migration. TerraBlender is still deferred because biome blending is not the bottleneck demonstrated here. GeckoLib and SmartBrainLib are unrelated to static landmark placement.

## Next safe migration

After exact-head gates are green, migrate either the collapsed quarry cut or drowned harbour terraces as another explicitly admitted chunk-safe historical piece using this same global frame. Do not migrate encounters/clues onto native coordinates until the landmark positions they depend on are native and stable.
