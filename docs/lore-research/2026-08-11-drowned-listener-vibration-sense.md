# Drowned Listener vibration execution evidence — 2026-08-11

## Scope

This note covers one physical Minecraft executor for the existing Java-owned `drowned_listener` creature profile. It does not establish new creature content, Rank/Class, rewards, spawning, Nightmare resolution, or progression.

## Sources checked

Repository policy was re-read first: `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md`.

Primary novel Chapter 370, **Exploration Report**, was rechecked through the owner-designated NovelFull access layer and cross-checked against the official WebNovel publication. The chapter establishes that practical knowledge about individual Nightmare Creatures includes their powers, behavior, and weaknesses. It does not establish this project's Drowned Listener, vibration radius, water amplification, stealth rule, or pursuit timing.

The existing Java profile already authors `drowned_listener` as a Dormant Monster with `SOUND` and `VIBRATION` senses, `SWIM` and `GROUND` locomotion, `AMBUSH` and `PURSUIT` pressures, and `dry_ground` counterplay. This implementation consumes that profile rather than deriving creature identity from Minecraft AI.

## Classification

- **CANON:** individual Nightmare Creatures can have meaningful creature-specific powers, behavior, and weaknesses that can be learned and exploited.
- **INFERRED:** when a Java-owned creature profile already identifies a sensory capability and counterplay tag, its physical executor should expose observable player counterplay rather than generic vanilla nearest-player targeting.
- **DESIGN:** Drowned Listener itself; sampled player displacement as the vibration signal; 14-block water range; 6-block dry-ground range; 2.5-block proximity override; crouching suppressing ranged vibration detection; four-tick sampling; 80-tick pursuit; 1.2 pursuit speed; removing inherited generic nearest-target goals while retaining retaliation.
- **UNKNOWN:** canonical Drowned Listener existence, anatomy, exact SOUND/VIBRATION mechanism, whether water should amplify or transmit its sensing, canonical ranges, stealth technique, sensing through blocks, sound categories, target priorities, pursuit persistence, and mature counterplay.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains identity/descriptor authority. NeoForge target goals, sampled positions, navigation and GeckoLib animation are removable execution/presentation only and cannot own progression, rewards, Nightmare state, persistence, or creature identity.

## Dependency decision

SmartBrainLib was considered because the dependency policy allows a spike for advanced sensing. It is not adopted here: the bounded sampled-motion behavior is small, testable with a pure Java policy, and uses vanilla `GoalSelector` removal plus navigation. Adding another required runtime dependency would not materially simplify this slice.

GeckoLib 4.9.2 remains the only external runtime library used by this branch lineage, for presentation only.

## Player-visible intent

A Drowned Listener should no longer notice a player solely because vanilla Drowned targeting sees them. Movement can attract it at range, particularly while the Listener is in water. Moving onto dry ground meaningfully shortens that range, and crouching suppresses ranged motion detection. Very close proximity still provokes it, and direct attacks can still trigger vanilla retaliation.
