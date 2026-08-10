# Ash Compass Memory runtime integration evidence

**Scope:** authoritative Memory ownership/acquisition plus the first physical summon/use/dismiss adapter.

Research followed `docs/LORE-SOURCE-POLICY.md`. The implementation reuses the already-authored `shadowslave:memory/ash_compass` identity rather than adding another Memory catalogue primitive.

## Primary material checked

- **Chapter 74 — Midnight Shard:** a Memory changes hands between people, the recipient receives explicit Memory acquisition feedback, its identity/rank/type can be inspected, and the acquired weapon can then be physically summoned for use.
- **Chapter 694 — Key Piece:** later text describes summoning and dismissing as shared Memory behavior, while dismissed Memories remain connected to the wielder's soul and can recover from damage.
- Official WebNovel was cross-checked for the Chapter 74 identity/publication entry.

No source text is reproduced here.

## Evidence boundary

**CANON:** Memories can be owned/transferred; acquisition and current ownership are meaningful state; a Memory can manifest physically through summoning and be dismissed; Memories are connected to the wielder's soul rather than being ordinary inventory ownership.

**INFERRED:** Java should persist Memory identity/provenance independently from the temporary Minecraft `ItemStack` that executes a manifested form. An inventory stack must therefore be unable to establish canonical ownership by itself.

**DESIGN:** Ash Compass is project-authored content; awarding it after the playable First-Nightmare appraisal is a preview reward choice, not a claimed universal Spell reward rule. The exact commands, Minecraft item ID, placeholder model, respawn-point refuge anchor, direction/distance wording, cooldown, and inventory-capacity behavior are Minecraft implementation choices.

**UNKNOWN:** universal Memory drop/reward probabilities; whether every First Nightmare awards a Memory; the canonical implementation of Ash Compass because Ash Compass itself is project content; exact summon/dismiss timing, visual effects, essence costs, repair timing, and transfer UI.

**COMPATIBILITY:** `MemoryOwnershipData` is Java authority. `shadowslave:ash_compass_memory`, command handlers and item-use code only manifest/execute an already-owned identity. Giving the item stack through Minecraft commands or another adapter does not grant ownership and cannot activate the Memory effect.

## Bounded gameplay contract

1. successful preview appraisal persists Ash Compass ownership with the exact Nightmare instance/resolution provenance;
2. `/shadowslave_memory summon ash_compass` manifests one physical stack only for its owner;
3. right-clicking the manifested Memory reports the direction/distance to the player's current same-dimension respawn refuge, or reports that the refuge is unanchored/beyond the current realm;
4. `/shadowslave_memory dismiss ash_compass` removes manifested stacks while retaining soul ownership;
5. preview reset clears the Java-owned Memory state;
6. later Memory slices may implement more existing authored Memories, transfer, repair and stronger enchantment execution without changing this ownership boundary.
