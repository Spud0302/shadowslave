# Storm Lantern Coast ecology-context slice

**Date:** 2026-08-11  
**Status:** bounded physical worldgen/exploration implementation note  
**Parent:** `gpt/storm-lantern-encounter-clues` / PR #261

## Goal

Advance the encounter-ecology layer without adding arbitrary creatures or pretending the command-built Storm Lantern Coast fixture is mature world generation.

The existing Storm Lantern encounter budget already responds to region affinity and broad landmark pressure. This slice adds deterministic local context for optional pressure so encounter placement can respond to why a place matters to exploration:

- high-exposure cliff routes;
- valuable salvage/wreckage;
- the approach margin around shelter.

The mandatory flooded-harbour Drowned Listener and historic-ruin Chainback remain unchanged in role.

## Authority and lore classification

- **CANON:** no new Shadow Slave lore claim is introduced by this slice.
- **INFERRED:** no new lore inference is required. The implementation follows the accepted project direction that exploration danger can depend on geography and useful locations.
- **DESIGN:** local ecology contexts, candidate weights, optional selection without replacement, the eight-block shelter margin, and the exact encounter-placement formulas are Minecraft gameplay design.
- **UNKNOWN:** mature Dream Realm population ecology, canonical creature territory behavior, exact resource attraction rules, settlement-distance effects, progression-band formulas, weather/time effects, persistence, migration, and respawn ecology.
- **COMPATIBILITY:** Java region/creature content remains authority for which creatures may exist. The ecology context is deterministic execution metadata only; it cannot grant rewards, change Rank/Class, alter Soul/progression, resolve a Nightmare, or rewrite historical-site state.

## Planner behavior

The encounter seed remains a separate deterministic stream derived from the site seed.

The 2-4 total encounter budget is preserved:

1. mandatory `drowned_listener` pressure at the drowned harbour terraces (`FLOOD_MARGIN`);
2. mandatory `chainback` pressure at either the belfry or collapsed quarry (`HISTORIC_RUIN`);
3. zero to two optional pressures selected without replacement from weighted candidates.

Optional candidates:

| Anchor | Context | Weight | Intent |
| --- | --- | ---: | --- |
| `coast_watch_0` | `HIGH_EXPOSURE` | 4 | exposed cliff route can carry roaming pressure |
| `coast_watch_1` | `HIGH_EXPOSURE` | 4 | second exposed-route alternative |
| `salvage_ledge` | `RESOURCE_EDGE` | 5 | useful wreckage can be dangerous to approach |
| `storm_shelter` | `SHELTER_MARGIN` | 1 | shelter remains comparatively valuable; occasional danger is kept on its approach margin |

Weights are **DESIGN**, not simulation truth. They intentionally make the resource ledge and exposed routes more likely optional pressure than the shelter margin.

For `SHELTER_MARGIN`, the hostile is offset at least eight blocks on X from the shelter anchor before small Z jitter is applied. The current command-built geometry is too coarse to claim exact indoor/outdoor navigation, but this prevents the planner from selecting the shelter anchor itself as the danger point.

## Physical executor

Spawned Storm Lantern encounter entities retain the existing encounter tag and pressure tag and now also receive:

`shadowslave_ecology_<context>`

This is execution/debug metadata. It is deliberately derived from the authoritative plan and is not persisted as canonical world identity.

## Regression found in parent PR #261

PR #261's exact-head Preview Gates run `31478724313` compiled successfully but failed one JUnit assertion in `StormLanternCoastDiscoveryPlanTest.clueMovementIsBoundedTowardArrival`.

The production helper `toward(2, 0, 5)` correctly returns `0`: when the target is only two blocks away and the allowed standoff movement is five blocks, the bounded movement reaches the target. The test incorrectly expected `2`.

This successor branch corrects the expectation to `0`. The datapack job in that parent run passed; client/server gates were skipped after the Java unit-test failure.

## Tests added/extended

Deterministic tests now pin:

- same-seed encounter equality;
- 2-4 budget and region-authorized physical creature identities;
- mandatory flood-margin and historic-ruin contexts;
- optional high-exposure, resource-edge and shelter-margin contexts appearing across a seed sweep;
- resource-edge pressure using only `salvage_ledge`;
- shelter-margin pressure using only `storm_shelter` and remaining outside its immediate anchor;
- optional candidate selection without replacement;
- seed-to-seed variability;
- independent site/encounter seed streams;
- corrected clue movement semantics from the parent PR.

Fresh exact-head Preview Gates remain required before this slice is called green.

## Still placeholder

This does not create true chunk-generated geography, persistent populations, dynamic territory, weather-aware spawning, resource depletion, settlement-distance weighting, progression-band gating, or ecology migration. The current Storm Lantern Coast remains a command-built development/migration fixture, and the Ashen Expanse remains the fixed regression fixture.

## Next highest-impact slice

Move the shared Drowned Bell site + encounter + discovery plans into native structure/chunk placement. If that remains too broad for one reviewable change, settlement-distance/progression-band inputs should be introduced as pure deterministic planner inputs before adding more creature variety.
