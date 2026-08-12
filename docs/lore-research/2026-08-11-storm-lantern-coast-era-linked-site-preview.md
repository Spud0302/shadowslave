# Storm Lantern Coast era-linked site preview — evidence note

## Scope

This slice physically executes the first later-era Dream Realm location linked to an authored Nightmare historical site. It does not add a new canonical mechanic, rewrite the player's Nightmare result into world state, or replace the fixed Ashen Expanse regression fixture.

## Primary / official-source recheck

Official WebNovel Chapter 743 (`Appraisal`) was rechecked on 2026-08-11. Sunny explicitly compares his First and Second Nightmare appraisals and reasons that most of his Second Nightmare achievements reproduced the original past, while his First Nightmare diverged much more strongly from the current of fate. He asks whether changing fate is what the Spell truly values. The exact formula remains unknown.

The official WebNovel Nightmare Spell compendium was also rechecked on 2026-08-11. It describes Nightmares as conflicts believed to be reconstructions of events from the ancient history of the Dream Realm and the Dream Realm itself as a vast ruined magical world.

## Classification

### CANON

- Nightmares reconstruct conflicts/events believed to come from the ancient history of the Dream Realm.
- The Dream Realm is a vast ruined world.
- Chapter 743 strongly supports deviation from the original flow of fate as a major appraisal factor, without exposing a numeric formula.

### INFERRED

- A historically reconstructed place can remain geographically/silhouettically recognizable in a later ruined Dream Realm state.
- Reusing landmark identity across eras is a lore-coherent exploration affordance.

### DESIGN

- `drowned_bell_cliff_settlement` is located in Storm Lantern Coast.
- The later site contains a ruined belfry, broken sea gate, collapsed quarry cut and drowned harbour terraces.
- Exact coordinates, block palettes, terrain height, sea line, modular variants, rotations, lantern/shelter/wreckage pieces and command name.
- Historical anchors keep stable relative coordinates while their ruin variants/rotations are derived deterministically from the world seed.

### UNKNOWN

- Whether every Nightmare location remains physically identifiable in the later Dream Realm.
- Exact elapsed time, erosion, geographic drift and destruction details.
- Exact Spell appraisal formula/thresholds.
- Any metaphysical effect of altered reconstructed events on shared Dream Realm history.

### COMPATIBILITY

- `NightmareHistoricalSiteCatalog` remains Java authority for scenario/site/region/era identity.
- `StormLanternCoastSitePlan` only derives deterministic physical piece selection from that identity plus the world seed.
- `StormLanternCoastPreviewService` is a removable NeoForge/Minecraft executor. Blocks, coordinates and geometry do not own progression, appraisal, Nightmare resolution or persistence.
- A player's altered Nightmare result is deliberately not an input to later-site generation.

## Player-visible result

`/shadowslave_dreamrealm enter_storm_lantern` builds and enters a separate Storm Lantern Coast development area in the Dream Realm dimension. The player arrives on a high route and must explore to identify the later forms of the Bell Tower, Sea Gate, Quarry Tunnels and Lower Village. Surrounding cliff lanterns, a collapsed shelter and storm wreckage vary from the deterministic site plan.

## Migration boundary

This is still a development executor, not mature chunk worldgen. It proves the stable-anchor + modular-piece seam before introducing biome/structure registration or TerraBlender. The fixed Ashen Expanse executor remains untouched for regression coverage.
