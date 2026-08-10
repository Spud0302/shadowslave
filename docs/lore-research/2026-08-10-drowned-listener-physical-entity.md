# Drowned Listener physical entity evidence note

**Scope:** one replaceable Minecraft execution adapter for the already-authored `drowned_listener` Nightmare Creature profile.

## Repository authority checked

This slice was selected after re-reading current `main`, open PRs/issues, `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md`.

It deliberately stacks on the Chainback entity seam rather than duplicating it. The active Drowned Bell runtime work already uses a vanilla Drowned as an explicit placeholder for the Drowned Listener, so this branch adds only the reusable registered creature execution layer and does not modify scenario resolution or Drowned Bell geometry.

## Primary novel check

Research followed `docs/LORE-SOURCE-POLICY.md`. The owner-designated NovelFull chapter access layer and official WebNovel were checked for the general creature rules this adapter must not contradict.

- **Chapter 74 — Midnight Shard:** Nightmare Creature Class is tied to the creature class/tier axis separately from Rank in the surrounding Memory discussion.
- **Chapter 81 — Weaver's Eye** (official WebNovel cross-check): Nightmare Creatures use the Dormant-to-Unholy Rank ladder while Class is a separate core-count/power axis.
- **Chapter 201 — Lord of the Dead:** later material explicitly describes higher Classes as carrying qualitative capability distinctions, not merely a flat increase in raw might.

No chapter establishes a creature called Drowned Listener or a canonical Minecraft-equivalent sound-detection, water-navigation, stat, spawn, loot, model, animation, or combat formula.

## Evidence classification

- **CANON:** Nightmare Creature Rank and Class are separate concepts; Nightmare Creatures can possess qualitatively different capabilities rather than being reducible to one numerical stat ladder.
- **INFERRED:** a replaceable physical entity can execute an already-resolved Java-owned creature identity while leaving Rank/Class/content authority in the Java catalogue.
- **DESIGN:** the `Drowned Listener` identity and authored profile; registry ID `shadowslave:drowned_listener`; vanilla Drowned-backed water/ground locomotion, navigation, hostility, dimensions, renderer, and explicit summon seam; empty loot table; no natural spawning.
- **UNKNOWN:** canonical occurrence/spawn rules; exact Rank/Class-to-stat or AI mapping; a real sound/vibration sensing algorithm; final appearance, animation, audio, equipment/combat style, drops/rewards, and how any encounter should affect appraisal or resolution.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains authoritative for stable creature identity and authored descriptors. NeoForge EntityType, vanilla AI, renderer, loot JSON, structures, and scenario adapters may execute or present that state but cannot own progression, rewards, `ResolutionGraph` acceptance, or terminal Nightmare outcome.

## Physical behavior boundary

The entity subclasses vanilla `Drowned` only to obtain a tested water/ground hostile-mob execution baseline. That does **not** mean the authored `SOUND` or `VIBRATION` senses are mechanically implemented yet. No hearing radius, vibration listener, decoy response probability, aggro rule, rank-to-health rule, equipment rule, or reward formula is inferred from the catalogue tags.

The empty loot table is intentional: this physical placeholder cannot silently manufacture Soul Shards, Memories, Echoes, equipment, or progression rewards.

## Integration boundary

This branch does not edit `NightmareInstance`, `ResolutionGraph`, appraisal, Soul state, Drowned Bell interaction stations, terminal resolutions, or the correctness/recovery stack. After this entity slice and the Drowned Bell runtime slice are independently green, a small follow-up can replace Drowned Bell's vanilla Drowned placeholder with `shadowslave:drowned_listener` while keeping encounter spawning/cleanup and event acceptance Java-owned.
