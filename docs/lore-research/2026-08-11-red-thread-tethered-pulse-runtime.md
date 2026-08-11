# Red Thread Bracelet `tethered_pulse` runtime evidence

## Scope

This slice executes one already-authored `MemoryContentCatalog` hook: `shadowslave:memory/red_thread_bracelet` / `shadowslave:memory_enchantment/tethered_pulse`. It does not add a Memory, enchantment family, reward, acquisition rule, progression rule, or danger model. The second authored hook, `strain_warning`, remains definition-only.

## Primary evidence checked

Research followed `docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md`.

- Chapter 74 (`Midnight Shard`) was rechecked for Memory rank/tier context.
- Chapter 694 (`Key Piece`) was rechecked through the owner-designated full-chapter access layer and official WebNovel publication for general Memory behavior. It distinguishes universal Memory properties such as soul association and summon/dismiss behavior from the individual enchantments/purposes of particular Memories.

Neither chapter establishes a Red Thread Bracelet, companion-marking Memory, canonical range, directional pulse, or danger-warning formula.

## Evidence classification

- **CANON:** Memories are soul-associated supernatural objects with differentiated individual enchantments/purposes and can be summoned/dismissed.
- **INFERRED:** an already-authored stateful Memory executor should retain its selected target in server-authoritative Java state rather than on a disposable Minecraft item or client presentation.
- **DESIGN:** Red Thread Bracelet, `tethered_pulse`, direct right-click marking of another online player, one marked companion at a time, replacement on re-mark, persistent UUID target, 128-block same-realm read range, eight-direction text, approximate distance, cooldown, command syntax, and vanilla-redstone placeholder item presentation.
- **UNKNOWN:** whether a canonical Memory resembles this item; canonical marking method or consent semantics; target capacity; range; inter-realm behavior; direction precision; essence cost; death retention; offline behavior; obstruction rules; and all `strain_warning` danger semantics.
- **COMPATIBILITY:** `MemoryOwnershipData` remains Memory ownership authority and `RedThreadCompanionData` owns only the authored effect target. NeoForge inventory items, player interaction, online-player lookup, distance calculation, text and placeholder model are replaceable execution/presentation.

## Runtime boundary

An inventory stack never grants ownership. Manifestation requires Java `MemoryOwnershipData`. Marking writes only the companion UUID to a codec-backed Java attachment. Using the bracelet reads that Java target and presents a bounded same-realm direction only when the companion is online and within range. Preview reset clears both the physical manifestation and the Java target.

The target attachment deliberately does not use `copyOnDeath()`: persistence through player death is UNKNOWN and is not invented by this slice.

## Deliberate limits

- `strain_warning` remains definition-only;
- no health/damage/danger observation is converted into supernatural warning state;
- no arbitrary entity, offline-location, cross-dimension or global player tracking;
- no client-owned target, item NBT authority, HUD marker, particles, custom model/texture, essence drain, rewards, appraisal, Nightmare event or progression mutation.
