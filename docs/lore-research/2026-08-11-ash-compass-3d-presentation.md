# Ash Compass 3D item presentation — 2026-08-11

## Scope

This slice changes only the physical Minecraft presentation of the already-owned `shadowslave:memory/ash_compass` Memory. It does not change Memory identity, ownership, acquisition provenance, `ember_north` behavior, cooldowns, Nightmare rewards, progression, persistence, or appraisal.

The current Java runtime already keeps `MemoryOwnershipData` authoritative and treats `AshCompassMemoryItem` as a removable execution adapter. This presentation slice preserves that boundary.

## Source-policy check

`docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and `docs/THIRD-PARTY-DEPENDENCY-POLICY.md` were re-read before implementation.

The existing Ash Compass runtime evidence in PR #185 checked Chapter 74 (`Midnight Shard`) and later Chapter 694 (`Key Piece`) for the underlying Memory ownership/summon/dismiss boundary. This slice introduces no new lore-facing Memory mechanic and does not require a new canonical appearance claim.

## Evidence classification

- **CANON:** Memories can be meaningful soul-associated owned objects and can be summoned/dismissed; no canonical Ash Compass appearance is asserted here.
- **INFERRED:** a physically manifested Memory should have a stable recognizable presentation that remains subordinate to its Java-owned identity.
- **DESIGN:** Ash Compass itself; the exact squat compass/case silhouette; recessed face; physical needle/crown geometry; all model transforms; current blackstone/copper/magma material treatment.
- **UNKNOWN:** canonical Ash Compass existence/appearance, materials, proportions, color, needle motion, summoning VFX/audio, and whether its final presentation should animate.
- **COMPATIBILITY:** `MemoryOwnershipData` and the existing Java gameplay hook remain authority. The item model and Minecraft textures are presentation only and may be replaced without save migration or canonical-state changes.

## Dependency / asset boundary

No dependency is added. A static compass-shaped item does not yet justify using GeckoLib merely because GeckoLib is approved elsewhere in the presentation stack. The model uses Minecraft's bundled polished-blackstone, oxidized-copper, and magma textures as explicit material placeholders.

The cuboid geometry and transforms in `ash_compass_memory.json` are project-authored. No model, texture, sound, structure, or other asset was copied from a third-party content mod or novel/adaptation source.

## Player-visible result

The Ash Compass no longer appears as a flat vanilla Echo Shard icon. In inventory, first person, third person, dropped/fixed contexts it now has a dedicated three-dimensional compass-like body with a recessed ember face and raised needle/crown silhouette.

## Remaining placeholder work

- vanilla block textures remain temporary material placeholders;
- no custom project PNG is added because the current GitHub connector write surface safely supports UTF-8 text resources, not binary PNG authoring;
- no needle animation, summon/dismiss animation, audio, particles, custom emissive treatment, or `warm_needle` behavior is added;
- final art direction remains replaceable DESIGN.
