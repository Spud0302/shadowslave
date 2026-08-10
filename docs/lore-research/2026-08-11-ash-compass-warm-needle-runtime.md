# Ash Compass warm-needle runtime integration

**Date:** 2026-08-11

## Why this slice exists

The existing Java Memory catalogue already authors Ash Compass with two enchantments. `ember_north` already has a physical item executor and active PR #220 corrects its target to Java-owned Cinder Rest. The second authored enchantment, `warm_needle`, still had no runtime consequence despite its existing hook: the needle warms when hostile essence-bearing life closes in.

This slice executes that existing hook against the three hostile Nightmare Creature identities that already have physical Java/NeoForge executors: Ash Burrower, Chainback, and Drowned Listener. It does not create another Memory, enchantment, creature, threat catalogue, reward, or progression rule.

## Primary material checked

Research follows `docs/LORE-SOURCE-POLICY.md`.

- **Chapter 74 — `Midnight Shard`:** rechecked against official WebNovel. It establishes Memories as individually ranked/tiered supernatural objects whose qualities can differ substantially. It does not establish Ash Compass, `warm_needle`, or an essence-proximity formula.
- **Chapter 694 — `Key Piece`:** rechecked against official WebNovel and the owner-designated full-chapter access layer. It explicitly distinguishes universal Memory qualities from a Memory's individual purpose/enchantment. That supports keeping the authored warning effect attached to the Java-owned Ash Compass identity rather than generic Minecraft item state.
- **Chapter 370 — `Exploration Report`:** rechecked against official WebNovel and the owner-designated access layer. It supports Nightmare Creatures having individually meaningful powers, behavior, and weaknesses. It does not establish that every Nightmare Creature can be detected through essence, nor any canonical range or warning intensity.

No novel text is copied into runtime content.

## Evidence classification

- **CANON:** Memories are distinct supernatural objects with individual qualities/enchantments; Nightmare Creatures can have creature-specific powers, behavior, and weaknesses.
- **INFERRED:** an already-authored detection enchantment should consume stable Java-owned Memory/creature identities rather than infer hostile supernatural identity from generic vanilla mob classes.
- **DESIGN:** Ash Compass itself; `warm_needle`; the twelve-block horizontal/vertical AABB search; one warm/no-warm threshold; warning text/color; and the exact current physical threat set of Ash Burrower, Chainback, and Drowned Listener.
- **UNKNOWN:** whether a canonical Memory behaves like this; canonical essence-detection range, occlusion, intensity, target classes, false positives/negatives, essence cost, cooldown, inter-realm behavior, and whether Echoes or humans should count.
- **COMPATIBILITY:** `MemoryContentCatalog` remains Memory/enchantment identity authority and the existing creature execution bindings remain threat identity authority. `MemoryOwnershipData` still decides whether the player owns the Memory. NeoForge entity proximity and chat output are replaceable execution/presentation only. Vanilla mobs and the owned Ash Burrower Echo cannot manufacture a `warm_needle` warning.

## Runtime boundary

The physical item scans only while an owned Ash Compass is actively used. The warning accepts only the three currently registered hostile project creature executor classes, then validates those identities against the integration binding. Generic `Monster`, vanilla Zombie/Drowned/Spider/Silverfish, players, villagers, arbitrary modded mobs, and `ash_burrower_echo` are excluded.

The current twelve-block range is intentionally one bounded DESIGN constant rather than a claim about essence perception. Later Java-owned creature or faction semantics can replace or extend the threat binding without migrating Memory ownership.
