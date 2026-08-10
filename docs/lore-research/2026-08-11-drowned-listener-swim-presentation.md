# Drowned Listener swim presentation evidence

**Date:** 2026-08-11  
**Scope:** presentation-only amphibious locomotion for the existing Java-owned `drowned_listener` Nightmare Creature profile.

## Repository boundary

This slice stacks on the active presentation/gameplay integration lineage at PR #215 head `694730b25a617878791386eebffc98f540ac9453`. It does not change Soul/progression, Nightmare lifecycle/recovery, appraisal/rewards, Echo ownership/commands, scenario resolution, Dream Realm resources, or creature identity.

`NightmareCreatureContentCatalog` already authors Drowned Listener with `SOUND` + `VIBRATION` senses and `SWIM` + `GROUND` locomotion. The NeoForge entity and GeckoLib resources only present that already-owned locomotion distinction.

## Primary evidence checked

Under `docs/LORE-SOURCE-POLICY.md`, Chapter 370 (`Exploration Report`) was rechecked through the owner-designated full-chapter access layer and cross-checked against the official WebNovel publication identity. It supports the broad proposition that individual Nightmare Creatures have meaningful creature-specific powers, behavior and weaknesses that can be observed and recorded. It does not establish this project's Drowned Listener creature, anatomy, swimming gait, sensory organs, colors or exact locomotion presentation.

## Classification

- **CANON:** individual Nightmare Creatures can have meaningful creature-specific powers, behavior and weaknesses.
- **INFERRED:** when Java already authorizes one creature for both swimming and ground locomotion, presentation may distinguish those physical states without changing creature identity or gameplay authority.
- **DESIGN:** Drowned Listener itself; the `move.swim` clip; water/bubble + movement as the presentation trigger; the roughly horizontal body pitch; alternating arm/leg stroke; fin flare; throat-fan motion; one-second loop; controller transition timing; existing project geometry.
- **UNKNOWN:** canonical Drowned Listener existence, anatomy, colors/materials, sensory anatomy, exact swimming method, speed, attack posture, SOUND/VIBRATION mechanism and mature counterplay behavior.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains identity/descriptor authority. GeckoLib `AnimationController`, `RawAnimation`, JSON clips and Minecraft water-state checks are removable presentation adapters. Removing or replacing them requires no canonical save migration and cannot change Rank/Class, rewards, progression, Nightmare resolution or persistence.

## Asset / dependency boundary

No new dependency or third-party asset is introduced. This slice reuses the already-admitted GeckoLib 4.9.2 presentation lane.

The vanilla Drowned texture remains an explicit placeholder. A new binary project PNG was deliberately not fabricated through the current text-only repository write path, and no third-party content-mod or novel artwork was copied as a substitute. Texture retirement should resume only through a repository path that can safely add the original binary asset.

## Player-visible result

While physically moving in water or a bubble column, the existing Drowned Listener GeckoLib body now uses a dedicated swimming clip instead of the same ground walk clip. On land it retains the walk clip; while stationary it retains idle; attacks retain the existing strike animation.

This is presentation only. Drowned-derived navigation, targeting, melee, swimming mechanics and sounds remain temporary execution placeholders, and bespoke SOUND/VIBRATION sensing remains unimplemented.
