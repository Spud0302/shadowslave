# Bellglass Token held-note runtime

## Scope

This slice executes the already-authored Bellglass Token `held_note` hook without adding another Memory, enchantment, sound catalogue, reward, progression rule, Nightmare event, or external dependency.

The physical executor is deliberately bounded to Minecraft Note Blocks: using an owned manifested Bellglass Token on a Note Block captures that block's current instrument identity and note index into persistent Java-owned player state. Sneak-using the Token later releases the stored note once and clears that payload. Normal use remains the separate `clear_warning` executor from the parent slice.

## Primary evidence

Primary Chapters 74 (`Midnight Shard`) and 694 (`Key Piece`) were rechecked under `docs/LORE-SOURCE-POLICY.md`. Chapter 74 establishes ranked/tiered Memories with distinct qualities. Chapter 694 explicitly separates universal Memory traits such as soul association and summon/dismiss behavior from the individual enchantments and purposes of particular Memories.

Those chapters do not establish Bellglass Token, an audio-recording Memory, Minecraft Note Blocks, or any canonical sound-storage formula. The repository's existing `held_note` gameplay hook is project-authored content, so this implementation does not promote it to canon.

## Classification

- **CANON:** Memories are soul-associated supernatural objects that can be summoned/dismissed and possess individual qualities/enchantments.
- **INFERRED:** an already-authored stateful Memory enchantment should retain its effect payload in Java-owned persistent state rather than in a disposable client/item presentation object.
- **DESIGN:** Bellglass Token, `held_note`, Note Blocks as the bounded capture source, retaining exact vanilla Note Block instrument identity plus note index, one-note capacity, replacement-on-recapture, sneak-use release, one-shot clearing, messages, cooldown, pitch mapping, and all Minecraft sound presentation.
- **UNKNOWN:** canonical Bellglass existence, whether any canonical Memory records sound, capture range/source categories, maximum duration, capacity, essence cost, whether stored sound survives dismissal/death, fidelity, volume, cross-realm behavior, and whether release consumes stored sound.
- **COMPATIBILITY:** `MemoryOwnershipData` remains Memory ownership authority. `BellglassHeldNoteData` owns only this authored enchantment payload. NeoForge items, Note Block inspection and sound playback are removable execution/presentation adapters and cannot create Memory ownership or progression.

## Persistence boundary

The held note is a separate codec-backed player attachment with an empty default. Existing player saves therefore load without a payload, while a captured note can survive item dismissal and server save/reload independently from the physical manifested stack. Invalid half-populated payloads and note indices outside vanilla's 0-24 range fail closed.

## Deliberate limits

This is not a generic microphone, voice recorder or event-level audio capture system. It does not listen to arbitrary client sounds, mobs, chat, music, other mods or network packets. That avoids making client presentation authoritative and keeps the slice reviewable while turning the existing `held_note` definition into something a player can physically capture and use.
