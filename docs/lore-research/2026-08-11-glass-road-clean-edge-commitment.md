# Glass Road clean-edge commitment — evidence and design boundary

**Date:** 2026-08-11  
**Scope:** execute the already-authored `glass_road` Memory's `clean_edge` enchantment as one bounded player-side committed attack.  
**Implementation status:** runtime slice only; this does not authorize or implement the generic combat/movement framework.

## Primary evidence rechecked

Official WebNovel Chapter 74 (`Midnight Shard`) was rechecked for Memory Rank/Tier/weapon identity. It establishes that weapon Memories can have differentiated Rank/Tier and materially distinct power; higher tier is not a universal linear power formula.

Official WebNovel Chapter 694 (`Key Piece`) was rechecked for later clarification of shared Memory properties. Sunny identifies summon/dismiss, self-repair while dismissed, soul connection, and names as common Memory traits.

No chapter establishes this project's Glass Road, `clean_edge`, a ten-tick wind-up, a precision-line strike, six Minecraft damage, or a sixteen-tick recovery.

## Classification

- **CANON:** Memories are differentiated supernatural items; weapon Memories exist; Memories are connected to their wielder's soul and share summon/dismiss behavior.
- **INFERRED:** an already-authored weapon enchantment should execute only from Java-owned Memory identity/ownership, while Minecraft targeting and damage remain replaceable physical execution.
- **DESIGN:** Glass Road; `clean_edge`; right-click activation; 10-tick commitment; current-facing precision line; 4.5-block reach; 0.8-block line radius; 6.0 damage; 16-tick recovery; exact messages; vanilla iron-sword material pixels.
- **UNKNOWN:** canonical Glass Road existence/appearance; exact enchantment semantics; essence cost; whether precision is temporal, anatomical or technique-based; damage scaling; Rank disparity; interaction with armour/soul damage; death/disconnect semantics; and all `mirror_step` behavior.
- **COMPATIBILITY:** `MemoryContentCatalog` remains identity/enchantment authority and `MemoryOwnershipService` remains ownership authority. `GlassRoadCombatData` is transient action state only. NeoForge tick timing, entity geometry, Minecraft damage and the temporary item model are replaceable executors and cannot grant Memory ownership, alter appraisal, award progression, or mutate Nightmare history.

## Combat-dance rationale

This slice adds a player-side opportunity cost without creating a universal heavy-attack system:

```text
read an earned opening
-> commit Glass Road
-> 10-tick wind-up
-> current forward line resolves once
-> hit or miss
-> 16-tick recovery
-> reposition / reassess
```

A missed or mistimed use still costs recovery. This is intentional: enemy punish windows should not become free DPS windows with no player commitment.

`mirror_step` remains definition-only because implementing it credibly requires a separate parry/deflection interaction rather than treating any ordinary hit as a successful parry.

## Dependencies

No dependency added. Native NeoForge tick events, Java attachments and Minecraft entity geometry are sufficient for this bounded action. GeckoLib remains presentation-only and is not required for this temporary material executor.
