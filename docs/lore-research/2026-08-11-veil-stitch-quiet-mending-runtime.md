# Veil-Stitch Case quiet-mending runtime evidence

## Scope

This slice executes the existing `shadowslave:memory/veil_stitch_case` / `shadowslave:memory_enchantment/quiet_mending` catalogue hook. It does not add a new Memory, enchantment family, repair economy, essence rule, or progression mechanic.

## Sources checked

Repository authority was re-read first: `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md`.

Primary novel material was then rechecked through the owner-designated full-chapter access layer, with official WebNovel used for chapter identity/publication cross-checking:

- Chapter 74, **Midnight Shard**: Memories have ranks/tiers/types and individual supernatural properties; they can be transferred between people.
- Chapter 694, **Key Piece**: Memories share universal summon/dismiss, self-repair-while-dismissed, and soul-connection traits, while their individual purpose/enchantment remains distinct.

Neither chapter establishes Veil-Stitch Case, equipment-repair enchantments, repair quantities, Minecraft durability, combat-clearance rules, or the `quiet_mending` / `dull_seam` names.

## Evidence classification

- **CANON:** Memories are soul-associated supernatural objects with individual qualities/enchantments; summon/dismiss and self-repair while dismissed are general Memory traits.
- **INFERRED:** an existing authored equipment-repair enchantment should execute only from an owned Memory identity and may use server-authoritative world state to decide whether its authored condition is currently satisfied.
- **DESIGN:** Veil-Stitch Case, `quiet_mending`, using a damaged off-hand equipment stack as the repair target, sixteen Minecraft durability points per invocation, an eight-block living-hostile clearance as the bounded interpretation of being out of combat, a five-second cooldown, command names/messages, and a vanilla string placeholder item model.
- **UNKNOWN:** canonical Veil-Stitch existence, whether any Memory repairs unrelated equipment, what qualifies as combat/rest, repair speed/amount/cost, essence use, material limits, cross-realm rules, death retention, and all `dull_seam` behavior.
- **COMPATIBILITY:** `MemoryOwnershipData` remains ownership authority. Minecraft inventory, durability, nearby-hostile queries, item presentation and chat are replaceable execution/presentation only and cannot grant ownership, progression or canonical Memory identity.

## Runtime boundary

The player must already own Veil-Stitch Case in Java state before `/shadowslave_memory summon veil_stitch_case` can materialize the physical executor. Inventory possession never grants ownership.

Using the manifested Case repairs only a damaged, damageable off-hand stack. The invocation fails closed while a living vanilla `Monster` is within eight blocks. No persistent combat model or supernatural threat classification is introduced; the nearby-hostile query is only a bounded DESIGN gate for this one repair action.

Preview reset removes the physical manifestation before clearing Memory ownership, preventing an inert item from surviving the development reset boundary.

`dull_seam` remains definition-only because suppressing shine, clatter and visible damage would require separate presentation/stealth semantics not established by current runtime authority.
