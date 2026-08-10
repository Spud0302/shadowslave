# Drowned Bell Listener runtime wiring evidence

## Scope

This slice does not add a new Shadow Slave mechanic. It replaces the Drowned Bell scenario's explicitly tagged vanilla Drowned execution placeholder with the already-authored and already-registered `drowned_listener` Nightmare Creature entity while preserving the scenario-owned entity UUID.

## Primary evidence rechecked

- **Chapter 14 — Child Of Shadows:** First-Nightmare action is driven by situated knowledge and the reconstructed conflict; defeating a powerful creature occurs within that conflict rather than establishing a universal boss-kill contract.
- **Chapter 743 — Appraisal:** later Nightmare material again places appraisal after the trial has already ended and recounts many different deeds, preserving the repository boundary between physical combat execution, terminal scenario resolution and later appraisal.
- Official WebNovel was cross-checked for Chapter 743 identity/publication placement.

No Drowned Bell, Drowned Listener, sensing algorithm, spawn probability or Minecraft body is claimed to be canonical novel content.

## Classification

- **CANON:** Nightmare completion and appraisal are distinct; combat can be part of a Nightmare without defining a universal completion rule.
- **INFERRED:** a replaceable physical Minecraft entity may execute an already-resolved Java-owned Nightmare Creature identity while the scenario instance continues to own lifecycle/cleanup state.
- **DESIGN:** the `drowned_listener` authored creature, the Drowned Bell authored scenario, vanilla-Drowned-backed movement/rendering/combat, the legacy placeholder tag, deferred entity-join replacement and UUID preservation.
- **UNKNOWN:** canonical Drowned Listener existence/appearance/behavior (the creature is project-authored), mature sound/vibration sensing, encounter probability, rewards, bespoke model/animation/audio and any creature-death-to-resolution relationship.
- **COMPATIBILITY:** `NightmareInstance`, `ResolutionGraph` and Java content identities remain authoritative. NeoForge entity join hooks, registered EntityTypes, AI/rendering and assets are removable execution/presentation adapters only.

## Execution boundary

The adapter recognizes only a vanilla `minecraft:drowned` carrying `shadowslave_drowned_listener_placeholder`. It defers replacement onto the server task queue, preserves UUID/location/rotation/motion/target, discards the vanilla body and adds `shadowslave:drowned_listener`. Already-converted Listeners and unrelated vanilla mobs are ignored.

Preserving the UUID is required because Drowned Bell persists that UUID in `NightmareInstance` as the owned-entity cleanup handle. The adapter therefore changes physical body/type without changing canonical scenario ownership.
