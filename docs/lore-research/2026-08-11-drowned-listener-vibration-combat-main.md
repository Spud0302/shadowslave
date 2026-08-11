# Drowned Listener vibration combat — current-main integration

**Date:** 2026-08-11  
**Implementation branch:** `gpt/drowned-listener-vibration-combat-main`  
**Scope:** port the already-authored VIBRATION / `dry_ground` counterplay into the current-main physical executor and make successful disengagement create a short readable recovery window.

## Primary evidence checked

Primary Chapter 370, **Exploration Report**, was rechecked through the repository-designated chapter access layer and official WebNovel publication. Sunny records Dream Realm geography/environment/landmarks and individual Nightmare Creatures' powers, behavior and weaknesses as practically useful exploration knowledge.

This supports the project requirement that creature-specific behavior and weaknesses can matter materially to survival. It does **not** establish the project-created Drowned Listener, a literal vibration sensor, water transmission values, crouch rules, pursuit duration or recovery timing.

## Classification

- **CANON:** individual Nightmare Creatures can have materially distinct powers, behavior and weaknesses; learning them can be practically valuable exploration/combat knowledge.
- **INFERRED:** an already-authored creature sense/counterplay identity should produce observable physical gameplay rather than being bypassed by generic nearest-player targeting.
- **DESIGN:** Drowned Listener identity; VIBRATION implementation by sampled displacement; 14-block water range; 6-block dry-ground range; 2.5-block proximity override; crouch suppression; four-tick sampling; 80-tick pursuit memory; 1.2 pursuit speed; 16-tick post-loss listening recovery; movement/LOOK/JUMP reservation during recovery.
- **UNKNOWN:** canonical Drowned Listener existence/anatomy; exact SOUND/VIBRATION mechanism; occlusion; intensity; water/ground propagation; target priorities; attack pattern; exact counterplay; whether loss of a supernatural sense canonically produces any recovery opening.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains authority for `drowned_listener`, its senses, locomotion, pressures and `dry_ground` tag. NeoForge targeting/navigation/tick sampling and recovery timing are replaceable execution only and cannot own Rank/Class, rewards, progression, Nightmare resolution or appraisal.

## Combat-dance result

The executor no longer acquires players simply through inherited Drowned nearest-target AI. A player can reduce detection by moving on dry ground or crouching at range. Once an already-acquired vibration pursuit expires and the player has genuinely broken proximity, the Listener drops the vibration-owned target and must spend a short bounded listening recovery before reacquiring.

This creates the intended interaction:

```text
make noise / get detected
-> Listener pursues
-> use dry ground, crouching and spacing to break the signal
-> pursuit memory expires
-> Listener stops to listen
-> player earns a short reposition / punish window
-> sensing resumes
```

Retaliation remains available, so attacking the creature can still provoke combat independently of vibration-owned acquisition.

## Deliberate limits

This slice does not implement the broader generic combat/movement framework, universal dodge/parry, SOUND-event sensing, through-block acoustics, bespoke Listener attacks, damage formulas, Aspect interaction, Memory counters, spawning/ecology, rewards, persistence, appraisal or historical-site state.

No external dependency is added. Vanilla Drowned locomotion/melee remain temporary execution; SmartBrainLib is not justified by this bounded sensing/recovery state machine.
