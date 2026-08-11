# Mirewalker Boots sure-foot runtime evidence

## Scope

This slice executes the existing `shadowslave:memory/mirewalker_boots` / `shadowslave:memory_enchantment/sure_foot` catalogue hook. It does not add a new Memory, enchantment family, terrain taxonomy, essence rule, progression mechanic, or stealth system.

## Sources checked

Repository authority was re-read first: `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md`, plus current open PR/issue ownership.

Primary novel material was rechecked through official WebNovel chapter text/publication:

- Chapter 74, **Midnight Shard**: Memories have differentiated rank/tier/type and individual supernatural qualities.
- Chapter 694, **Key Piece**: Memories share soul association and summon/dismiss/self-repair traits while individual enchantments/purposes remain distinct.

Neither chapter establishes Mirewalker Boots, terrain-loss mitigation, Minecraft mud/water/soul-sand behavior, movement-effect strength/duration, or the `sure_foot` / `light_trace` names.

## Evidence classification

- **CANON:** Memories are soul-associated supernatural objects with differentiated identities and individual enchantments; summon/dismiss is a general Memory trait.
- **INFERRED:** an already-authored mobility enchantment should execute from owned Java Memory identity while server-authoritative Minecraft terrain may be used as a replaceable physical input.
- **DESIGN:** Mirewalker Boots, `sure_foot`, manual invocation, water/mud/soul-sand as the current dragging-terrain set, eight seconds of Movement Speed I, a five-second cooldown, exact messages, and a vanilla leather-boots item placeholder.
- **UNKNOWN:** canonical Mirewalker existence, whether any Memory mitigates terrain drag this way, passive-vs-active operation, exact terrain classes, magnitude, duration, essence cost, death/cross-realm semantics, and all `light_trace` behavior.
- **COMPATIBILITY:** `MemoryOwnershipData` remains ownership authority. NeoForge item use, block/fluid sampling, potion-effect execution, inventory and presentation are removable adapters and cannot grant Memory identity, progression, rewards or Nightmare state.

## Runtime boundary

The player must already own Mirewalker Boots in Java state before `/shadowslave_memory summon mirewalker_boots` can materialize the item. Inventory possession cannot establish ownership.

Using the manifested Boots while in water, standing in mud, or standing on soul sand applies one bounded movement-speed effect intended to offset part of the local movement loss. Ordinary stable terrain fails closed rather than granting a generic speed buff.

Preview reset removes the physical manifestation before Memory ownership is cleared.

`light_trace` remains definition-only because track suppression requires a separate authoritative notion of tracks/observation that the current runtime does not own.
