# Dream Realm era-linked world generation

## Goal

Combat and exploration are primary gameplay pillars. The Dream Realm must therefore become a large, seeded, discoverable world rather than a collection of fixed preview rooms. Nightmares must also stop feeling like disconnected challenge dimensions: they reconstruct conflicts from the ancient history of the same Dream Realm geography the player can later explore.

This document defines the architectural direction. Exact terrain formulas, placement rates, site histories, structure palettes and appraisal thresholds remain replaceable project DESIGN unless separately supported by primary lore evidence.

## Lore / authority boundary

### CANON

- Nightmares reconstruct conflicts/events from the ancient history of the Dream Realm.
- Challengers inhabit historical roles inside those reconstructed events.
- The Nightmare Spell appraises what the challenger actually did.
- Chapter 743 strongly supports deviation from the original flow of fate as a major appraisal factor: reproducing the original past can receive a lower appraisal than a run that forces history onto a substantially different course.

### INFERRED

- A Nightmare's historical location can have a later Dream Realm state that remains geographically recognisable even after centuries/ages of destruction and transformation.
- Reusing a stable site identity across historical Nightmare execution and present Dream Realm exploration produces a more lore-coherent game than unrelated procedural arenas.

### DESIGN

- Exact region/site IDs and coordinates.
- Original-history fate axes and their weights.
- Structure pools, ruin transforms, placement spacing and encounter budgets.
- The Drowned Bell -> Storm Lantern Coast site mapping and all exact future ruins.
- Any numeric divergence score or later mapping from that score to Spell appraisal labels.

### UNKNOWN

- The exact Nightmare Spell appraisal formula and grade thresholds.
- Whether every Nightmare site remains physically identifiable in the later Dream Realm.
- Exact elapsed eras, geographic drift, destruction processes and reconstruction fidelity.
- Whether changing a reconstruction has any metaphysical effect beyond the challenger and appraisal. The project must not assume normal time-travel causality.

### COMPATIBILITY

Java owns scenario identity, site identity, original-history baseline, accepted resolution history, divergence evidence and progression. Minecraft/NeoForge worldgen owns physical terrain, structures and encounter placement only. TerraBlender or another worldgen helper may later replace parts of physical generation without migrating Java authority.

## Core invariant: one place, multiple eras

Every authored Nightmare that represents Dream Realm history should eventually reference a stable `historicalSiteId`.

A site contains:

- Nightmare scenario ID;
- Dream Realm region ID;
- historical landmark identities;
- later/future landmark identities;
- the original historical outcome represented as weighted fate axes;
- future-state presentation cues.

The Nightmare and present Dream Realm are two executions of the same site identity:

```text
Dream Realm site identity
        |
        +-- historical era -> Nightmare reconstruction
        |       bell tower intact
        |       village inhabited
        |       sea gate functional
        |       conflict in progress
        |
        +-- later era -> explorable Dream Realm
                belfry ruined
                harbour drowned
                sea gate broken
                quarry collapsed
                creatures / salvage / later settlers possible
```

The challenger's Nightmare choices do **not** regenerate or rewrite the shared later-era site. The later Dream Realm reflects the original historical stream. The player's altered trial exists as their resolved Nightmare history and appraisal evidence.

## First proof: The Drowned Bell

`the_drowned_bell` is linked to `storm_lantern_coast` through site `drowned_bell_cliff_settlement`.

Recognisable era pairs:

| Historical Nightmare | Later Dream Realm |
| --- | --- |
| Bell Tower | storm-battered belfry ruin |
| Sea Gate | broken / buried sea-gate works |
| Old Quarry Tunnels | collapsed quarry cut |
| Lower Village | drowned harbour terraces |

The exact original-history baseline is project DESIGN and starts with five explicit fate axes:

- warning bell: silent;
- quarry route: sealed;
- sea gate: failed;
- lower village: inundated;
- Drowned Listener: survived.

A completed Nightmare resolves some or all of those axes. Changing an axis contributes positive weighted divergence; reproducing it contributes none; an unresolved/unproven axis grants no assumed credit.

The first implementation deliberately produces a **divergence score**, not hard-coded canonical Spell grade thresholds.

## Appraisal architecture

Appraisal needs two separate inputs.

### 1. Divergence: how much fate changed

Compare resolved fate axes with the original history.

Required invariant:

> If run B changes every fate axis changed by run A and changes additional original-history axes, B cannot receive a lower divergence score.

This provides monotonicity without pretending the hidden Spell formula is known.

### 2. Deed/evidence character: how the player changed it

The existing evidence system remains useful for determining the character of the result:

- preservation;
- warning;
- guidance;
- water;
- retaliation;
- precision;
- adaptation;
- etc.

Divergence should influence **appraisal quality**. Deed evidence should influence **what kind of Aspect/Attribute/Flaw identity is generated**. These concepts should not be collapsed into one opaque weight map.

## World generation rework

The current Ashen Expanse preview is a bounded fixed `49 x 49` cleared platform with hard-coded coordinates. It proved the execution layer but should not be the long-term world system.

### Layer 1: seeded macro-regions

Generate large contiguous region territories from the world seed and Java region profiles.

Region profiles already define:

- hazards;
- traversal modes;
- opportunities;
- creature affinities;
- landmark hooks;
- resource hooks;
- arrival/travel rules.

Worldgen should consume these descriptors rather than hard-code one flat terrain algorithm.

Initial desired regions for physical implementation:

1. Ashen Expanse;
2. Storm Lantern Coast;
3. Chainfall Reach or Blackwater Steps.

### Layer 2: stable authored site anchors

Historically meaningful sites are not throwaway random buildings.

Their coordinates are deterministically derived from:

- world seed;
- region identity;
- stable historical site ID.

This means the same world always places the same Nightmare-linked site in the same Dream Realm location.

Sites should have spacing/exclusion rules so important locations are discoverable expeditions rather than structures every few chunks.

### Layer 3: procedural landmark and ruin pools

Generic exploration content can be much more variable.

Instead of one hard-coded `buildWatchtower`, use pools such as:

- ruined watch structures;
- collapsed roads/causeways;
- abandoned camps;
- creature dens;
- shrines/unknown monuments;
- caves and tunnel mouths;
- resource sites;
- traversal hazards;
- small settlement remnants;
- defensive refuges.

Each landmark identity should have several geometry variants, rotations and damage states. Structure/Jigsaw-style generation or equivalent data-driven templates are preferred over giant Java block-placement loops once the first generator seam exists.

### Layer 4: era transforms

Nightmare-linked sites need at least two structure states:

- `HISTORICAL`: inhabited/functional state used by the Nightmare;
- `LATER`: ruined/transformed state used in the Dream Realm.

The two should share recognisable silhouettes and anchor points while allowing major destruction, flooding, burial, overgrowth or later occupation.

Do not generate the later form by replaying the player's Nightmare outcome. It is authored/deterministic from the original history.

### Layer 5: encounter ecology

Exploration should create uncertainty and danger rather than placing one guaranteed creature at one fixed coordinate.

Each generated area gets an encounter budget based on:

- region creature affinities;
- local landmark type;
- exposure/shelter;
- time/environment conditions where relevant;
- nearby resource value;
- distance from safe settlements;
- creature Rank/Class compatibility with the intended progression band.

Use deterministic seed streams so encounters can be reproduced/debugged, while still feeling unpredictable to a player who does not know the seed.

Avoid generic vanilla spawn-table ownership for important Nightmare Creature identity/reward logic. Java content remains authority; physical spawn systems only execute approved encounters.

### Layer 6: exploration information

Exploration should reward observation and memory.

Potential later systems:

- discovered landmark journal/map state;
- settlement rumours and route knowledge;
- Memory-assisted navigation;
- danger observations rather than omniscient minimap markers;
- recognising a later-era site after completing its Nightmare;
- finding environmental evidence of the site's original history.

The game should favour 'I know this tower' over automatically drawing a quest arrow to it.

## Random building generation principles

Random does not mean arbitrary.

Every generated structure must answer at least one gameplay question:

- Why would a player explore it?
- What traversal choice does it create?
- What danger can inhabit it?
- What resource/information/shelter can it provide?
- How does it communicate its region?

Prefer **families of authored modular pieces selected procedurally** over unconstrained block soup. A ruined tower can have twenty combinations from a small high-quality kit while retaining readable architecture and collision.

## External worldgen dependencies

Do not add TerraBlender merely to satisfy this document. First implement the stable site/region generator seam with native NeoForge worldgen/data-driven structures. Re-evaluate TerraBlender when biome placement or cross-region terrain blending becomes the actual bottleneck it is designed to solve.

## Migration from the current preview

1. Keep the existing Ashen Expanse preview as a development fixture until the new generator can reproduce all currently testable interactions.
2. Introduce stable site identity + divergence appraisal independently of terrain generation.
3. Build Storm Lantern Coast as the first true era-linked region because The Drowned Bell already supplies the historical half.
4. Build one later-era Drowned Bell ruin from reusable structure pieces.
5. Build the historical Nightmare version from the same anchor/silhouette vocabulary.
6. Move Ashen Expanse fixed landmarks into seeded landmark pools.
7. Add encounter-budget spawning instead of fixed one-creature placement.
8. Only then retire the fixed preview builder.

## Acceptance target

A successful vertical slice should allow a player to:

1. enter The Drowned Bell Nightmare and experience the cliff settlement in its historical state;
2. resolve it on a path that differs measurably from the authored original history;
3. receive divergence-based appraisal evidence plus deed-specific identity evidence;
4. later explore Storm Lantern Coast in the Dream Realm;
5. discover the same settlement's later ruins through ordinary exploration;
6. recognise several major landmarks without the Nightmare having rewritten those ruins;
7. encounter variable region-appropriate danger/resources on the journey rather than following one fixed coordinate script.
