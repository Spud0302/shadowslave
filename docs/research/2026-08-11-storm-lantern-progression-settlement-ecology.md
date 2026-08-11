# Storm Lantern Coast progression + settlement ecology

**Date:** 2026-08-11  
**Scope:** bounded follow-up to the Storm Lantern Coast encounter ecology stack.  
**Status:** implementation evidence/design note; the Ashen Expanse/Cinder Rest coordinates remain a development fixture.

## Why this slice exists

`docs/design/DREAM-REALM-ERA-LINKED-WORLDGEN.md` calls for encounter budgets to respond to region, landmark, exposure, resource value, safe-settlement distance and intended progression band. The active Storm Lantern stack already consumes region affinity, landmark purpose, exposure, shelter and resource value. This slice adds the two missing bounded inputs without introducing another creature identity or pretending the command-built preview is mature world generation.

## Primary lore re-check

Official WebNovel material was rechecked on 2026-08-11:

- Auxiliary chapter **The Nightmare Spell** records the seven human Soul Ranks and the separate Nightmare Creature Rank/Class axes.
- Chapter **9 — Wishful Thinking** describes Soul Core Rank as a major basis of power and treats preparation before entering the Dream Realm as materially important.
- The official early appraisal text also emphasizes that gaps between Ranks are consequential.

These sources establish that Rank is a meaningful power axis. They do **not** disclose a spawn-budget formula, settlement safety radius, or rule that same-Rank creatures must populate a region around a particular human.

## Classification

### CANON

- Soul Rank is a meaningful quality/power axis.
- Nightmare Creature Rank and Class are distinct power axes.
- Dormant and Awakened are established Rank terms.

### INFERRED

- None required for the numeric ecology rule. The implementation deliberately does not claim that the Spell or Dream Realm dynamically scales populations to a player.

### DESIGN

- `UNRANKED`, `DORMANT`, and `AWAKENED_OR_HIGHER` execution bands.
- `NEAR <= 96`, `FRONTIER <= 192`, and `REMOTE > 192` settlement-distance bands.
- optional-pressure capacity derived from progression band plus one near/remote modifier.
- optional Dormant/unranked pressure selecting only physically implemented Dormant-affinity creatures; Awakened-or-higher allowing the existing Dormant and Awakened executors.
- preserving one authored Chainback ruin guard even when it is above the entering player's band, so Rank disparity remains an exploration/avoidance problem rather than making the world silently level-scale every landmark.
- using the current horizontal Cinder Rest -> Storm Lantern preview-origin distance (224 blocks) as a temporary physical settlement-distance input.
- executor entity tags exposing the derived progression and settlement bands for debugging/future AI/discovery integration.

### UNKNOWN

- canonical Nightmare Creature population density or territory rules;
- whether safe human settlements suppress nearby creatures, and at what distance;
- mature Rank/Class distribution around Citadels, settlements, Gateways, resources or routes;
- final intended progression bands for each Dream Realm region;
- whether the eventual game should use the strongest party member, weakest party member, average party capability, route rating, world era, or another value for multiplayer encounter planning;
- any exact relationship between human Rank and a creature's practical encounter difficulty.

### COMPATIBILITY

- `SoulService` / `SoulData` remain authority for the player's actual Soul Rank. The world planner receives only a coarse derived execution band and cannot mutate progression.
- `NightmareCreatureContentCatalog` remains authority for the project creature Rank/Class identities; the planner looks up those authored profiles rather than duplicating a second rank table.
- `DreamRealmRegionContentCatalog` remains authority for Storm Lantern Coast creature affinities.
- settlement distance is execution/world context, not permanent Soul, Nightmare, appraisal or reward state.
- the 224-block Cinder Rest distance is explicitly tied to the fixed regression fixture. Native generated settlement anchors should replace it once that world seam exists.
- a player's altered Drowned Bell Nightmare reconstruction still cannot rewrite the shared later Storm Lantern Coast geography or ecology history.

## Current bounded rule

Every Storm Lantern plan retains two authored landmark pressures:

1. Drowned Listener at the flooded harbour margin;
2. Chainback at one major historical ruin.

Only the **optional** 0-2 pressures are scaled by context. The capacity begins at 0 / 1 / 2 for unranked / Dormant / Awakened-or-higher, then gains +1 when remote from a safe settlement or -1 when near one, clamped to 0-2.

This avoids turning the preview into universal level scaling. The important ruin can still be too dangerous for the player; lower-band context mainly prevents additional optional Awakened pressure from being layered on top of that authored danger.

## Migration boundary

This is still not native chunk generation. The reusable seam is the `EncounterContext`: future worldgen can supply actual nearest-settlement distance and a chosen party/progression policy without changing creature identity, site identity, or deterministic site seeds. The fixed Ashen Expanse/Cinder Rest origin must not become the long-term geography model.
