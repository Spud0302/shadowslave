# Blackwater Hook undertow-line runtime boundary

**Date:** 2026-08-11  
**Branch:** `gpt/blackwater-undertow-runtime`

## Repository / integration position

Current live `main` was rechecked before implementation and already contains the broad Java Nightmare/appraisal/Memory/Echo/creature/Dream-Realm loop plus player gameplay keybinds from #245. The root `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, and `ISSUES.md` remain materially older preview-era snapshots, so live GitHub PR/issue ownership controls overlap decisions.

Active work already owns successful-completion restart correctness, Ash Compass runtime consolidation, Ash Burrower guard integration, Chainback displacement, Drowned Listener sensing, Cinder Rest presentation, and several older stacked Memory runtimes. This slice is based directly on current `main` and uses a distinct already-authored Memory identity instead of importing those stale stacks.

`MemoryContentCatalog.waveOne()` already defines `shadowslave:memory/blackwater_hook` with:

- `undertow_line` — anchors an essence line to terrain or a struck target;
- `river_grip` — resists forced movement while the line is fixed.

This slice implements only the **terrain-anchor** half of `undertow_line`. Struck-target anchoring and all `river_grip` behavior remain explicitly unimplemented.

## Primary evidence rechecked

Research followed `docs/LORE-SOURCE-POLICY.md`.

- **Chapter 74 — Midnight Shard:** exposes Memory Rank and Type and discusses Tier separately; it also shows ordinary Memory transfer between people. This supports differentiated persistent Memory identity but does not establish Blackwater Hook.
- **Chapter 694 — Key Piece:** later clarification identifies summon/dismiss, self-repair while dismissed, and connection to the wielder's soul as universal Memory traits, while a Memory's unique purpose/enchantment remains distinct.

The owner-designated NovelFull access layer was used for full-chapter research and official WebNovel was used to cross-check chapter identity/wording. No novel text is committed here.

## Evidence classification

- **CANON:** Memories have differentiated supernatural identities/properties; Memory Rank/Type/Tier are distinct presentation concepts; summon/dismiss and soul connection are general Memory traits.
- **INFERRED:** when an already-authored Memory enchantment has state that must survive replacement of its physical Minecraft manifestation, that effect payload should remain server-authoritative Java state rather than item/client state.
- **DESIGN:** Blackwater Hook itself; `undertow_line`; terrain-click anchoring; one fixed block anchor; exact dimension association; right-click pull; sneak-use release; 16-block maximum line distance; 2-block arrival threshold; 0.85 pull strength; 0.35 vertical cap; cooldown/messages; vanilla tripwire-hook presentation.
- **UNKNOWN:** whether Blackwater Hook exists in canon; canonical line material/mechanism; target anchoring; range, force and cadence; whether the line can cross dimensions; essence costs; death retention; obstruction/severing rules; equipment-slot semantics; and all `river_grip` behavior.
- **COMPATIBILITY:** `MemoryOwnershipData` remains Memory ownership authority and `BlackwaterHookAnchorData` owns only the persistent authored-effect anchor. Minecraft item interaction, block coordinates, velocity, cooldown, messages and model are replaceable execution/presentation only and cannot grant ownership or mutate appraisal/progression/Nightmare resolution.

## Player-visible bounded behavior

An owner may manifest Blackwater Hook with the existing Memory command surface. Right-clicking terrain fixes the Java-owned undertow line to that exact block and dimension. Normal use within the bounded line distance pulls the owner toward the stored anchor; sneak-use releases the anchor. Dismissal removes only the physical item, so the Java anchor is independent of manifestation. Preview reset removes both manifestation and anchor before ownership is cleared.

## Deliberate limits

- no struck-entity anchor;
- no `river_grip` forced-movement resistance;
- no rope/chain entity or collision;
- no block obstruction, severing, durability or essence expenditure;
- no cross-dimension pull;
- no automatic grappling target selection;
- no custom model, texture, audio or VFX;
- no reward, appraisal, Nightmare event or progression change.

These remain separate gameplay/presentation slices only after current behavior has exact-head compile/runtime evidence.
