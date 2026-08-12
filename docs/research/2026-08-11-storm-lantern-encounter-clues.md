# Storm Lantern Coast encounter clues

**Date:** 2026-08-11  
**Scope:** exploration-information execution layered on the deterministic Storm Lantern Coast encounter budget.  
**Base:** PR #258 / `gpt/storm-lantern-encounter-ecology`.

## Why this slice

The era-linked worldgen design requires exploration information that rewards observation and memory without omniscient quest arrows. PR #258 establishes deterministic region/landmark encounter pressure, but its development arrival message still disclosed the exact encounter count and the physical world did not warn the player before contact.

This slice adds one bounded discovery layer: each approved physical encounter derives a nearby environmental clue from its pressure class. The clue is placed toward the approach route and explicitly not on the hostile coordinate. It communicates danger category through world geometry rather than exposing a marker, nameplate, minimap icon, or exact count.

## Player-facing design

- flood-edge pressure produces a disturbed mud/clay/gravel patch;
- ruin-guard pressure produces cracked masonry with abandoned chainwork;
- exposed-route pressure produces fractured masonry/gravel damage;
- the arrival message no longer reports the number of seeded encounters;
- the same encounter plan deterministically produces the same clue plan;
- clues remain physical presentation only and grant no discovery state, reward, progression, appraisal, or creature knowledge automatically.

## Evidence classification

- **CANON:** no new Shadow Slave lore claim is introduced by this slice.
- **INFERRED:** no new lore inference is required. The implementation follows the repository's accepted exploration direction that observation should matter.
- **DESIGN:** all clue families, materials, five-block standoff, approach direction, exact wording, and the one-clue-per-encounter mapping.
- **UNKNOWN:** canonical visual traces left by any project creature, how long traces persist, whether particular creatures leave reliable tracks, and mature discovery/journal semantics.
- **COMPATIBILITY:** `StormLanternCoastEncounterPlan` remains the approved deterministic encounter authority for this bounded preview. The clue planner/executor is removable Minecraft presentation and cannot create creatures, change Rank/Class, award knowledge/rewards, alter Nightmare history, or mutate progression.

Because the slice introduces no new canonical proposition, no additional novel claim was needed beyond the lore source/alignment policies already re-read at run start. Existing creature identities and the Storm Lantern Coast ecology remain explicitly project DESIGN as recorded in PR #258's evidence note.

## Dependency decision

No dependency added. Native Minecraft blocks are sufficient for this small physical information layer. GeckoLib, SmartBrainLib, Curios, Veil, and TerraBlender do not replace meaningful work here. TerraBlender remains deferred because biome/terrain blending is still not the demonstrated bottleneck.

## Validation target

Focused tests require:

- one clue per approved encounter;
- deterministic clue derivation;
- pressure-to-clue-family mapping;
- bounded movement toward the arrival route;
- no clue occupying the exact hostile X/Z coordinate;
- malformed negative standoff input failing closed through the pure helper.

Fresh exact-head Preview Gates are required before this branch is called green. Physical client/server gates remain necessary because the execution uses real Minecraft block placement.

## Still placeholder

This does not implement persistent discovered-landmark journals, track aging, weather erasure, creature-specific canonical spoor, route rumours, map state, true chunk-generated clues, resource-value ecology, settlement-distance pressure, or progression-band encounter gating. The Storm Lantern Coast remains a command-built migration fixture until the site/encounter planners move into actual generated geography.
