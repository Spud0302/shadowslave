# Bellglass Token hidden-movement warning

## Scope

This slice executes the already-authored `Bellglass Token` / `clear_warning` Memory hook. It adds no new Memory, enchantment, reward rule, progression rule, creature identity, or Spell formula.

## Primary evidence

- Chapter 74, `Midnight Shard`, establishes ranked/tiered Memories as summonable supernatural equipment with individual properties.
- Chapter 694, `Key Piece`, explicitly distinguishes universal Memory traits (soul connection, summon/dismiss, repair while dismissed, name) from the unique enchantment/purpose of a particular Memory.
- Later material remains consistent with Memories having distinct active/passive enchantments rather than one universal effect model.

Research followed `docs/LORE-SOURCE-POLICY.md`: chapter text is the authority; NovelFull was used as the owner-designated access layer and official WebNovel was used to cross-check chapter identity/wording where available.

## Classification

- **CANON:** Memories are soul-associated supernatural objects that can be summoned/dismissed and can possess individual enchantments/qualities.
- **INFERRED:** an authored warning enchantment should consume stable Java-owned Memory and creature identities rather than treating arbitrary Minecraft mobs as supernatural authority.
- **DESIGN:** `Bellglass Token`, `clear_warning`, ten-block range, requiring a living/moving existing hostile Nightmare Creature executor, requiring no direct player line of sight, the amethyst vibration sound, command syntax, cooldown, and warning wording.
- **UNKNOWN:** canonical Bellglass existence, warning range, whether walls/line-of-sight are the right definition of hidden movement, intensity, directionality, false positives, essence cost, continuous sensing, and which beings should qualify.
- **COMPATIBILITY:** `MemoryOwnershipData`, `MemoryContentCatalog`, and Java creature identities remain authoritative. NeoForge entity scans, line-of-sight checks, chat, sound, inventory manifestation, and the temporary amethyst-shard item model are removable execution/presentation adapters.

## Deliberate limits

`held_note` remains definition-only. This slice does not create audio recording state, generic vibration sensing, HUD indicators, progression, rewards, scenario events, or ownership from inventory state.