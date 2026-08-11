# Chainback displacement evade-punish exchange

**Date:** 2026-08-11  
**Scope:** current-main physical combat execution only  
**Implementation branch:** `gpt/chainback-evade-punish-current-main`

## Why this slice

The live Chainback already has a distinct Java-owned identity, `PURSUIT + DISPLACEMENT` pressure, GeckoLib presentation, and authored counterplay tags. Current `main` still executes ordinary Spider melee beneath that presentation, while stale PRs #235/#238 developed a bounded displacement pull and readable warning on older ancestry.

This current-main slice ports that bounded displacement exchange and adds one missing combat-loop consequence: a player who escapes the warning should earn a short punish opportunity instead of merely resetting the creature to ordinary pursuit.

This does **not** start the generic combat/movement framework discussed in `docs/design/COMBAT-MOVEMENT-DISCUSSION.md`; that document remains explicitly implementation-not-started. This is a creature-local NeoForge executor using existing Minecraft goals.

## Primary lore check

Official WebNovel Chapter 370, **Exploration Report**, was rechecked on 2026-08-11. Sunny's report treats Nightmare Creatures as worth documenting by their individual powers, behavior, weaknesses, and the environments in which they are encountered. The chapter supports creature-specific readable danger/counterplay as a setting constraint, but does not establish this project's Chainback or any exact timing formula.

## Classification

- **CANON:** Nightmare Creatures can have materially distinct powers, behavior, and weaknesses; learning those properties is practically important.
- **INFERRED:** a creature-specific dangerous action should expose observable counterplay in the physical game rather than being an invisible instantaneous stat check.
- **DESIGN:** `chainback`; its literal displacement pull; four-block horizontal range; 1.5-block vertical band; 12-tick warning; three-tick warning pulse; 0.55 horizontal pull; 0.10 lift; 50-tick cooldown; eight-tick recovery after a connected pull; eighteen-tick recovery after a clean evade; chain warning/break audio; CRIT warning particles; and using a priority-zero recovery goal to reserve movement/look/jump controls.
- **UNKNOWN:** canonical Chainback existence, anatomy, Rank/Class, literal chains, exact displacement mechanics, telegraph, recovery, stagger, anchor/sever rules, damage model, and final combat timing.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains authority for creature identity/pressure/counterplay. The timing, particles, motion and goal control in `ChainbackEntity` are replaceable Minecraft execution only and cannot own progression, rewards, Nightmare resolution, appraisal, or persistence.

## Combat-dance contract

The local exchange is:

```text
Chainback enters displacement range
-> 12-tick readable commitment
-> player can break range or line of sight
-> evade: displacement is cancelled and Chainback enters 18-tick recovery
-> connected pull: target is displaced and Chainback enters 8-tick recovery
-> recovery blocks inherited Spider movement/leap/melee goals
-> ordinary pursuit resumes after recovery, while the 50-tick displacement cooldown prevents immediate repetition
```

The longer evade recovery is intentional DESIGN: successful defense earns initiative. The shorter connected recovery still prevents the special attack from flowing directly into uninterrupted Spider pressure.

## Dependencies

No new dependency. GeckoLib remains presentation-only. SmartBrainLib is not needed for this one local timed action because current-main Chainback already has replaceable vanilla pursuit/climbing and this slice can reserve conflicting goal controls directly.

## Evidence target

Focused unit coverage pins bounded pull eligibility, warning cadence, bounded pull vector, and the invariant that evasion earns a longer recovery than taking the pull. Hosted Preview Gates must still compile/package the full mod and boot a physical NeoForge client/server before this head is called green.
