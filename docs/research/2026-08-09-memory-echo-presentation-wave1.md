# Memory / Echo presentation wave 1 research

**Status:** implementation evidence note  
**Scope:** player-facing acquisition and inspection composition for already-resolved Memory and Echo catalogue identities  
**Authority:** `docs/LORE-SOURCE-POLICY.md`

## Primary chapter evidence checked

- **Chapter 47 — Echo:** Echoes are represented separately from Memories, can be inspected through rune-like presentation that exposes creature identity/class/rank-style information, can be summoned and commanded, and can serve combat, carrying and other practical tasks.
- **Chapter 74 — Midnight Shard:** receiving a Memory is explicitly communicated to the bearer, after which rune inspection exposes the Memory identity, Rank and Type and allows the player-facing item to be examined separately from the acquisition event.
- **Chapter 256 — True Reason:** later material describes an Echo as a copy of a Nightmare Creature, supporting creature-derived provenance while not implying that every Echo must have identical presentation metadata.
- **Chapter 1552 — Lifeboat:** a later Echo is summoned and given a practical command during non-combat work, reinforcing utility/command presentation as meaningful player information.
- **Chapter 1609 — Reclusive Saint:** later material explicitly distinguishes practical mount Echoes from combat value and identifies an artificial Echo, so the project must not fabricate Nightmare Creature Rank/Class provenance for artificial Echo content.

## Evidence boundary

### CANON

- Memory acquisition and later Memory inspection/presentation are distinct player-facing concepts.
- Memory identity includes named/ranked/type-like rune information; later systems may expose additional Memory information.
- Echoes can be inspected, summoned and commanded and may provide practical utility outside direct combat.
- Creature-derived Echoes exist, and artificial Echoes also exist.

### INFERRED

- A game UI benefits from separating a concise acquisition card from a richer inspection card.
- Catalogue-owned tactical roles, command modes, utility tags and authored enchantment hooks can be rendered as player-readable explanatory fields without making the renderer authoritative.

### DESIGN

- `MemoryEchoPresentationComposer`, its `PresentationCard` / `PresentationLine` records, field labels, title copy, field ordering, title-casing, deterministic set sorting, stable Echo subject-ID wrapper and provenance string.
- Showing authored Memory theme tags and enchantment gameplay hooks in inspection cards.
- Showing authored Echo role/command/utility/tactical-use fields in inspection cards.

### UNKNOWN

- The exact universal Spell UI, typography, audio, timing, ordering and field visibility for every Memory/Echo.
- A universal canonical Memory acquisition/drop formula or Echo acquisition probability.
- Whether every Echo exposes exactly the same metadata in every era or circumstance.
- Any canonical relationship between the project catalogue's utility tags/commands and Spell-generated rune fields.

### COMPATIBILITY

- Java catalogue/state identity remains authoritative.
- This slice does not award, mutate, summon, persist, appraise, rank, destroy or execute Memories/Echoes.
- NeoForge HUD/chat/narrator/model/entity/AI adapters may render these immutable cards but cannot become the owner of Memory/Echo identity or progression state.
- The open Spell-presentation PR can later adapt or replace surface wording without changing the underlying merged Memory/Echo content identities.

## Implementation intent

This is deliberately a bounded presentation-composition slice based directly on current `main`. It consumes the already-merged Memory and Echo catalogues and does not depend on the open broad Spell-presentation branch or Nightmare correctness stack.

Set-backed catalogue fields are sorted before rendering so equivalent Java-owned content yields stable presentation regardless of set iteration order. Artificial Echo inspection omits creature source Rank/Class rather than inventing values.

No generation, drop, appraisal or reward formula is claimed as canonical.
