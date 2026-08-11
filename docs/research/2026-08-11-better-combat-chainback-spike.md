# Better Combat + Chainback vertical-slice spike

**Date:** 2026-08-11  
**Branch:** `gpt/better-combat-chainback-spike`  
**Base:** `gpt/chainback-evade-punish-refresh` / PR #256  
**Status:** development-runtime spike; not yet an admitted modpack dependency.

## Purpose

Test the smallest credible player-side combat plumbing against the existing Chainback evade/punish exchange before implementing a bespoke generic combat framework.

This branch deliberately stacks on PR #256 so Chainback has one authority/execution lineage. It does not create a second Chainback action system.

Target physical loop:

```text
Chainback telegraphs displacement
    -> player moves/uses spacing and can earn the existing miss recovery
    -> player attacks during the opening with Better Combat's ordinary sword moveset
    -> Better Combat owns mundane attack input/timing/swing hitbox/player attack animation
    -> Minecraft/Shadow Slave remain free to own later damage consequences and stability rules
    -> both sides recover/reposition
```

## Dependency decision

### Better Combat — SPIKE

Pinned development runtime:

- Better Combat `2.4.0+1.21.1-neoforge` / Modrinth version ID `VhIOvcXP`
- Minecraft `1.21-1.21.1`
- NeoForge
- upstream project license: All Rights Reserved

Current upstream integration guidance says Better Combat owns/changes the exact mundane plumbing this spike is trying not to reinvent: weapon combos, attack animation, upswing, accurate swing calculation/hitbox targeting, synchronized weapon attributes, movement slowdown while attacking, and attack input. It also explicitly requires integrators to remove/disable semantically conflicting player animation, attack range, attack timing/cooldown, attack-key and similar logic.

Therefore this spike adopts a strict boundary:

> When Better Combat is enabled for an ordinary weapon, Better Combat owns mundane player attack input/timing/range/swing hitbox/player attack animation. Shadow Slave must not run a competing mundane action timer for that same swing.

Better Combat `2.4.0` also exposes a per-player disable path (`CombatFlags` / `bettercombat_disabled` entity tag), leaving a future escape hatch for exceptional Shadow Slave actions if the experiment is retained.

### playerAnimator — TRANSITIVE RUNTIME REQUIREMENT

Pinned `2.0.4+1.21.1-forge` / Modrinth version ID `HJZB6bmA`.

The published file is marked compatible with NeoForge 1.21-1.21.1 and is MIT licensed. Better Combat currently lists playerAnimator as a required dependency.

### Cloth Config — TRANSITIVE RUNTIME REQUIREMENT

Pinned `15.0.140+neoforge` / Modrinth version ID `izKINKFg`.

Published for NeoForge Minecraft 1.21-1.21.1; LGPL-3.0-only. Better Combat currently lists Cloth Config as a required dependency.

### Player Animation Library — DEFERRED FOR THIS SPIKE

PAL has a current NeoForge 1.21.1 release (`1.1.5+1.21.1-NeoForge`) and is MIT licensed, but Better Combat's 1.21.1 line currently requires playerAnimator and already owns player attack animation in this experiment. Loading a second player animation framework would add dependency surface without proving the core combat question.

Reconsider PAL only if Better Combat is rejected or if a later non-Better-Combat player animation need is demonstrated.

## Playable development fixture

Run:

```text
/shadowslave_combat chainback_slice
```

The command first asks NeoForge's mod list whether the `bettercombat` mod is actually loaded. If it is absent, setup refuses to spawn/equip the slice. This prevents a vanilla-only iron-sword swing from being mistaken for evidence in favor of the dependency. The check uses only NeoForge loader metadata; it does not import or call Better Combat's Java API.

When Better Combat is loaded, the command equips one ordinary iron sword and spawns one tagged existing Chainback six blocks in front of the player. The tag exists only so the development diagnostic can identify the intended test target.

Run this while the intended punish window is open:

```text
/shadowslave_combat status
```

The first `status` call made while Chainback's existing displacement recovery is `OPEN` arms a transient in-memory baseline for that exact tagged Chainback and reports its authoritative health. A later `status` call reports the health delta from that baseline. The baseline is keyed to the player + Chainback UUID, is cleared by `chainback_slice` and `reset`, and is never written to `SavedData`, entity NBT, player persistence, or a damage-event mirror.

This keeps the proof path deliberately below combat authority: the command only samples health that the server already owns. It does not listen to or modify damage, does not know how Better Combat selected the target, and cannot change hit timing, consequences, recovery, Rank, Memories, Aspects, Soul state, or progression.

Earlier spike heads attempted to count post-damage events directly. That observer seam failed twice against NeoForge 1.21.1 API differences (`LivingDamageEvent.Post` accessor churn), so the spike deliberately stopped retrying it. The transient OPEN-baseline path is the smaller credible alternative: no event listener, no persistent telemetry, and no second damage source of truth.

### Minimum physical verdict

A useful successful run is:

1. spawn the slice and confirm setup reports that Better Combat is loaded;
2. read the chain warning;
3. break range or line of sight so the displacement misses;
4. run `status` while recovery is `OPEN`; confirm the probe reports `ARMED` and note the opening ticks;
5. punish once with Better Combat's ordinary iron-sword swing;
6. run `status` again and require a positive health delta from the same OPEN baseline;
7. repeat once outside the opening to compare the rhythm rather than inventing a separate stability system;
8. confirm Chainback resumes its existing pursuit/special-action loop after recovery.

A successful dependency verdict still requires physical observation that one intended swing corresponds to one ordinary attack outcome. The health delta probe deliberately does not claim to identify internal Better Combat hit-count semantics; visible double-hit behavior, duplicate health drops, or hidden vanilla bypass still reject the spike.

If one intended Better Combat swing visibly double-hits, bypasses the expected opening rhythm, or requires Shadow Slave to take ownership of Better Combat's player timing/range/animation state, treat that as evidence against adoption rather than adapting canonical Shadow Slave state around the dependency.

## What becomes playable if physical gates pass

No new weapon item is introduced. Better Combat ships dedicated vanilla sword weapon-attribute resources, including `minecraft:iron_sword`. The spike therefore uses an ordinary iron sword as the one reference player moveset rather than adding another Shadow Slave catalogue item.

Against PR #256 Chainback, the intended physical judgement is simple:

1. read Chainback's chain warning;
2. break its range/line of sight and earn the longer recovery;
3. enter with Better Combat's sword swing rather than vanilla point-and-click melee;
4. observe whether attack commitment/movement/animation makes the punish window readable and risky enough;
5. disengage before Chainback resumes pursuit.

This does **not** yet add a Shadow Slave guard/parry or stability meter. The first question is whether Better Combat can cleanly replace mundane swing plumbing without fighting Chainback's server-owned action state.

## Authority boundary

Better Combat/playerAnimator/Cloth Config own no Shadow Slave canonical state.

They may own only replaceable execution/presentation for this experiment:

- player attack input;
- mundane attack timing/upswing;
- swing target calculation/hitbox;
- player attack animation;
- Better Combat client/server weapon configuration.

Shadow Slave remains authority for:

- Nightmare Creature identity, Rank/Class and authored counterplay;
- Chainback special-action eligibility, telegraph, pull, recovery and cooldown from PR #256;
- Soul/progression;
- Aspects/Flaws/Attributes;
- Memory/Echo ownership;
- Nightmare/appraisal/rewards;
- persistence;
- any future stability/injury/essence consequence state.

The transient fixture health baseline is diagnostic process memory only. It owns no canonical or combat state and is safe to lose on restart.

## Explicitly deferred

This spike does not implement:

- limb injury/anatomy;
- soul damage;
- Essence economy;
- slash/pierce/blunt taxonomy;
- advanced movement trees;
- broad weapon catalogues;
- generic cross-mod combat framework extraction;
- custom parry framework;
- Better Combat Java API integration;
- PAL integration;
- modpack packaging/admission.

## Admission gate

Do not promote Better Combat into `modpack/manifest.json` or required Shadow Slave metadata merely because Gradle resolves it.

Admission requires physical evidence on this exact spike that:

1. NeoForge client boots with the pinned Better Combat/playerAnimator/Cloth Config set;
2. dedicated server boots with the same required set;
3. `/shadowslave_combat chainback_slice` positively confirms Better Combat is loaded before producing the fixture;
4. ordinary iron-sword attacks execute through Better Combat;
5. PR #256 Chainback telegraph/pull/miss-recovery behavior still works;
6. the two systems do not visibly double-fire melee damage or erase Chainback's earned recovery window;
7. removal of the dependency leaves Java-owned Shadow Slave state loadable;
8. the player-side result feels closer to `observe -> respond -> opening -> commit -> recover` than vanilla hit trading.

If these fail because Better Combat must own semantics that Shadow Slave cannot surrender, reject it and test the smallest native/PAL alternative instead.

## Sources checked

- Better Combat official GitHub README/integration guide: `https://github.com/ZsoltMolnarrr/BetterCombat`
- Better Combat 2.4.0 NeoForge 1.21.1 release: `https://modrinth.com/mod/better-combat/version/2.4.0%2B1.21.1-neoforge`
- playerAnimator 2.0.4 1.21.1 file: `https://modrinth.com/mod/playeranimator/version/2.0.4%2B1.21.1-forge`
- Cloth Config 15.0.140 NeoForge file: `https://modrinth.com/mod/cloth-config/version/15.0.140%2Bneoforge`
- PAL 1.1.5 NeoForge 1.21.1 file: `https://www.curseforge.com/minecraft/mc-mods/player-animation-library/files/8454212`

No upstream source or assets are copied by this spike.
