# Cinder Rest / Ashen Expanse world-story integration — 2026-08-10

## Scope

This slice combines existing Java-owned Dream Realm content with existing physical execution work. It adds no new settlement, faction, NPC archetype, region, landmark, resource, progression or reward catalogue.

`DreamRealmWorldStoryIntegration` binds the authored `ashen_watch` module (Cinder Rest / Grey Lanterns / `watch_captain`) to the authored `ashen_expanse` region. Entering the development Dream Realm builds the bounded Ashen Expanse slice and ensures one tagged Watch Captain placeholder exists inside the refuge. Right-click presentation resolves back through the Java-owned story module; vanilla Villager trading remains suppressed.

## Primary/later evidence rechecked

Research followed `docs/LORE-SOURCE-POLICY.md`.

- Chapter 468, **Desecrated Grove**: Dream Realm travel around a Citadel uses established routes, with dangerous wilderness outside them and organized patrol activity.
- Chapter 2263, **Beginning of the End**: later material explicitly describes human settlements paired with Citadels and distinguishes safer immediate Citadel areas from broader Nightmare-Creature-infested territory.
- Chapter 2273, **Shadow Clan**: a Dream-Realm-based organization operates from a Citadel and divides work across information gathering, threat removal, logistics and construction.

No exact Cinder Rest, Grey Lanterns, Ashen Expanse geography, Villager body or service set is claimed to come from the novel.

## Classification

- **CANON:** humans and organizations can maintain Dream Realm routes, Citadels, settlements and practical organized work; dangerous wilderness remains relevant outside controlled areas.
- **INFERRED:** a bounded refuge, local NPC body and interaction surface can physically execute already-authored Java settlement/faction identity without becoming authority for progression or relationships.
- **DESIGN:** Ashen Expanse, Cinder Rest, Grey Lanterns, Watch Captain, exact 49x49-ish terrain slice, block palette, NPC location, Villager body, invulnerability/no-AI state, development commands and interaction copy.
- **UNKNOWN:** canonical procedural geography, settlement placement/population, NPC appearance/AI/dialogue, economy, reputation, service refresh, creature ecology, resource mechanics and final art/audio/weather.
- **COMPATIBILITY:** Java owns region/module/faction/settlement/archetype/service identity. The dimension, blocks, Villager, tags, chat and commands are removable execution/presentation only. They do not mutate Soul, Aspect, Flaw, Attribute, Memory, Echo, Nightmare resolution, appraisal or rewards.

## Validation boundary

Focused pure-Java tests verify that the world slice consumes all authored Ashen Expanse landmark/resource hooks, the Watch Captain resolves from the authored Ashen Watch module, and Cinder Rest can only integrate when both resolve to the same region ID.

Hosted Preview Gates must still prove exact-head Java compilation/unit/package, physical NeoForge client boot, dedicated-server boot, development JAR packaging and frozen-datapack deployed harness compatibility.
