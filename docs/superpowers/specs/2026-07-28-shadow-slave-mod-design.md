# Shadow Slave — Minecraft Mod Design

**Date:** 2026-07-28
**Status:** Phase 1 design approved, awaiting implementation plan

## Overview

A Minecraft mod based on the webnovel _Shadow Slave_. The core loop of the novel maps
onto Minecraft unusually directly:

> Sleep → be pulled into a timed Nightmare Spell → survive it → wake up **Awakened** with
> an **Aspect** and a **Flaw** → hunt Nightmare Creatures for **Memories** and **Echoes** →
> awaken **Soul Cores** to rank up.

That is a custom dimension, an RPG progression layer, tiered soulbound loot, and summons.

> **Lore source of truth:** `/root/vault/Games/Shadow Slave/Shadow Slave - Lore Reference.md`.
> Read it before designing anything that touches the lore. Anything it does not cover must be
> checked against the wikis (`defuddle parse "<url>" -m` against `shadowslave.miraheze.org`)
> and written back into that note — never worked from recall.

## Target platform

| Choice            | Value                                        | Reasoning                                                                                                    |
| ----------------- | -------------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| Minecraft version | 1.21.1                                       | User is version-flexible (CurseForge launcher). Stable, best-supported point in the 1.21 line.               |
| Phase 1 format    | **Vanilla datapack**                         | No JDK, no build step, no loader dependency. Playable in days.                                               |
| Mod loader        | **Deliberately undecided**                   | A datapack needs no loader. Deferred to the Java port, when we will know which systems actually forced Java. |
| Distribution      | **One mod, one jar**                         | Phases are releases, not separate mods. Subpackages per system, config toggle per system.                    |
| Versioning        | **Pride Versioning** (`PROUD.DEFAULT.SHAME`) | Nothing depends on this mod programmatically, so SemVer's compatibility contract buys nothing. See below.    |
| Multiplayer       | Server-authoritative from day one            | Datapack functions run server-side inherently, so this is free. Avoids a rewrite later.                      |

## Phase roadmap

Each phase is independently playable and gets its own design pass before implementation.

| Phase | Name                    | Adds                                                                                                               |
| ----- | ----------------------- | ------------------------------------------------------------------------------------------------------------------ |
| **1** | **The First Nightmare** | Nightmare dimension, timed trial, Nightmare Creature, Awakening, Aspects & Flaws, Soul readout. **This document.** |
| 2     | Memories                | Soulbound tiered gear from the Dream Realm; Memory ranks; Soul inventory. Likely forces the Java port (real GUI).  |
| 3     | Soul Cores & Ascension  | Absorbing cores, rank progression, attribute growth.                                                               |
| 4     | Echoes                  | Bind defeated shadows, summon them to fight alongside you.                                                         |
| 5     | The waking world        | Nightmare Creatures breaching the Overworld, Gates, Second/Third Nightmares.                                       |
| 6     | Multiplayer polish      | Divine Sight on other players, party/Clan mechanics.                                                               |

## Versioning

This project uses **[Pride Versioning](https://pridever.org/)** (Niki Tonsky), not SemVer.
Given a version `PROUD.DEFAULT.SHAME`, increment the:

1. **PROUD** version when you make changes you are really proud of
2. **DEFAULT** version when you make a release that's okay
3. **SHAME** version when you are fixing things that are too embarrassing to admit

Pre-release and build metadata labels work as extensions, same as SemVer.

**Why it's safe here:** SemVer's whole job is telling downstream consumers whether an upgrade
breaks them. Nothing depends on `shadowslave` programmatically — Minecraft mods gate on the
_game_ version, not the mod version — so that contract buys us nothing. Loaders parse the
version string from the mod manifest and sort it to resolve dependencies, which requires three
dot-separated integers. Pride Versioning is three dot-separated integers, so it drops straight in.

**Phase 1 ships as `1.0.0`** — the first thing worth being proud of.

## Phase 1 — The First Nightmare

### Entering

Sleeping in a bed pulls the player into `shadowslave:nightmare` instead of skipping to dawn.
Inventory and gear travel with the player. Entry point and return point are stored so the
player can be put back where they slept.

### The dimension

Defined entirely in JSON:

- `dimension_type` — permanently dark (fixed low ambient light), `bed_works: false`,
  `respawn_anchor_works: false`, no natural regeneration advantage.
- `dimension` — noise-based generator, custom biome source.
- `worldgen/biome` — black fog, dark sky colour, heavy hostile spawn weights, sparse terrain
  features. Oppressive rather than decorative.

### The trial

**Win condition: timer, then the creature.**

1. A scoreboard timer counts down, surfaced as a boss bar titled _"The Nightmare Spell"_.
2. When the timer expires, a **Nightmare Creature** spawns — for Phase 1 a heavily buffed,
   renamed vanilla mob with attribute modifiers and custom equipment. The boss bar switches
   to its health.
3. Killing it returns the player to the Overworld, Awakened.

### Death rule

**Ejected, not killed.** Dying in the nightmare returns the player to the Overworld at half a
heart with gear intact and no Awakening. Sleeping again retries the trial.

Chosen over the novel-faithful permadeath because the first build is untuned; one bad spawn
should not end a save. Permadeath is a config toggle worth adding once difficulty is tuned.

### Awakening

On victory: rank is set to Awakened, and one Aspect and one Flaw are rolled at random via
`/random value`. Both are packages of vanilla status effects and attribute modifiers,
reapplied by a tick function keyed on the player's tag.

| Aspect | Effect                                               |
| ------ | ---------------------------------------------------- |
| Shadow | Night vision; movement speed bonus while in darkness |
| Flame  | Fire immunity; burning strikes                       |
| Bone   | Bonus armour                                         |
| Wind   | Movement speed and jump boost                        |

| Flaw         | Effect                              |
| ------------ | ----------------------------------- |
| Shadow Slave | Damage over time in direct sunlight |
| Fragile      | Reduced maximum health              |
| Ravenous     | Accelerated hunger drain            |
| Weightless   | Increased fall damage               |

Aspect and Flaw are rolled independently, so any combination is possible — including the
thematically apt Shadow + Shadow Slave.

### Soul readout

`/trigger soul` prints rank, Aspect, Flaw and attributes to chat.

Deliberately not a GUI. A real Soul screen is exactly the thing datapacks cannot do, which
makes it the natural forcing function for the Java port in Phase 2.

### State

All player state lives in scoreboard objectives and tags — no custom NBT, no mod data:

| Objective / tag         | Purpose                                                  |
| ----------------------- | -------------------------------------------------------- |
| `ss_rank`               | 0 = Sleeper, 1 = Awakened                                |
| `ss_timer`              | Nightmare countdown, in ticks                            |
| `ss_ret_{x,y,z}`        | Overworld return coordinates                             |
| `ss_aspect` / `ss_flaw` | Rolled values, mirrored as tags for selector performance |
| `soul`                  | `/trigger` objective for the readout command             |

### File layout

```
data/shadowslave/
  dimension_type/nightmare.json
  dimension/nightmare.json
  worldgen/biome/nightmare.json
  function/
    init.mcfunction          # scoreboard setup, run on load
    tick.mcfunction          # timer, aspect/flaw upkeep, sleep detection
    nightmare/enter.mcfunction
    nightmare/spawn_creature.mcfunction
    nightmare/survive.mcfunction
    nightmare/die.mcfunction
    awaken/roll.mcfunction
    aspect/<name>.mcfunction
    flaw/<name>.mcfunction
    soul.mcfunction
    test/selfcheck.mcfunction
  tags/function/{load,tick}.json
  (no loot_table — the creature drops vanilla ravager loot; Memories are Phase 2)
  predicate/is_sleeping.json
```

Note: 1.21 uses singular directory names (`function`, not `functions`).

### Testing

- `shadowslave:test/selfcheck` — a runnable function asserting objectives exist, every Aspect
  and Flaw function resolves, and the dimension is registered. Prints pass/fail to chat.
  This is the one check the logic must not break.
- Manual: `/reload`, then `/function shadowslave:nightmare/enter` to skip straight into the
  trial without sleeping.

### Out of scope for Phase 1

Memories, Echoes, Soul Cores, ranks above Awakened, Overworld nightmare incursions, custom
models or textures, any GUI, custom entity AI.

## To verify at implementation time

Claims in this document that were reasoned from memory and must be confirmed against the
game or wiki before being relied on:

1. Exact sleep-detection mechanism — entity predicate flag vs NBT check vs the
   `minecraft:slept_in_bed` advancement trigger. Affects `tick.mcfunction`.
2. `pack_format` number for 1.21.1.
3. Singular datapack directory names confirmed for 1.21.1 specifically.
4. Whether boss bar handover (timer → creature health) works cleanly, or needs two bars.

Resolved since first draft:

5. ~~**Lore:** the rank ladder above Awakened.~~ **Checked against the wiki 2026-07-28 — the
   first-draft recollection was wrong.** Seven Soul Ranks, with the Spell's name and the
   colloquial human name differing: Dormant/Sleeper → Awakened/Awakened → Ascended/**Master**
   → Transcendent/**Saint** → Supreme/**Sovereign** → Sacred/Spirit → Divine/_(unnamed)_.
   Full detail in the lore reference note.

## Lore support for Phase 1 decisions

Confirmed from the wiki after the design was agreed — all three support choices already made:

- **One creature per First Nightmare.** First Nightmares are tailored individually, so canonically
  only a single Creature appears — usually a Beast or Monster, rarely a Demon, never above a
  Devil. This directly justifies the "timer, then one Nightmare Creature" win condition over
  arena waves.
- **Failed Aspirants become Gates.** A carrier who fails their First Nightmare becomes a
  miniature Gate that lets a single Nightmare Creature into the waking world — their corpse turns
  into the monster. A strong future upgrade to the death rule: ejection could spawn a hostile
  creature at the player's bed.
- **Creature Class = Soul Core count**, from Beast (1) through Monster, Demon, Devil, Tyrant,
  Terror to Titan (7). A ready-made seven-tier mob difficulty scale needing almost no adaptation.
  Phase 1 uses Beast or Monster tier only.

## Decisions log

| Decision         | Chosen                   | Rejected                     | Why                                                                                                                                                                                               |
| ---------------- | ------------------------ | ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Packaging        | One mod, one jar         | Mod per system               | Every system needs the Soul data, so splitting means hand-maintained version ranges, load-order handling and a frozen core API. Isolation comes from config toggles and per-system tests instead. |
| Modpack          | No                       | Ship as a modpack            | A modpack bundles _other people's_ mods; it is a distribution choice, available any time later, and changes nothing about the build.                                                              |
| Build path       | Datapack prototype first | Straight to Java             | Most of Phase 1 needs no Java, and the JSON carries into the mod unchanged. Not throwaway work.                                                                                                   |
| Aspect framework | Vanilla effects          | Hard-depend on Apoli/Origins | Apoli would make Aspects pure JSON, but its 1.21.1 build is pre-release (1.13.0-pre.1); inheriting its breakage and release schedule is not worth it yet. Reconsider at the Java port.            |
| Loader           | Deferred                 | Fabric or NeoForge now       | A datapack needs no loader. Deciding after Phase 1 means deciding with evidence about which systems forced Java.                                                                                  |
| Versioning       | Pride Versioning         | SemVer                       | Nothing consumes this mod programmatically, so SemVer's compatibility contract is ceremony. Pride Versioning is still three dot-separated integers, so loaders parse and sort it fine.            |
