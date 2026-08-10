# Drowned Listener project texture evidence — 2026-08-11

## Scope

Retire the vanilla Drowned texture placeholder from the existing Java-owned `drowned_listener` physical executor without changing creature identity, Rank/Class, AI, sensing, rewards, progression, persistence, or Nightmare state.

## Sources rechecked

- Primary novel text: Chapter 370, **Exploration Report**, via the owner-designated NovelFull access layer.
- Official WebNovel Chapter 370 publication/wording cross-check.
- Repository `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/THIRD-PARTY-DEPENDENCY-POLICY.md`.

Chapter 370 supports the bounded proposition already used by the Drowned Listener presentation lineage: individual Nightmare Creatures have meaningful creature-specific powers, behavior, and weaknesses that can be recorded and learned. It does not establish this project's Drowned Listener anatomy, palette, external sensory organs, or materials.

## Classification

- **CANON:** individual Nightmare Creatures can have meaningful creature-specific powers, behavior, and weaknesses.
- **INFERRED:** an already-distinct Java-owned creature identity benefits from stable recognizable presentation rather than an unrelated vanilla mob skin.
- **DESIGN:** Drowned Listener itself; the existing listener-fin/throat-fan geometry; this exact muted slate/teal aquatic palette, pale sensory marks, chest ridges, fin patterning, throat-fan treatment, and texture atlas layout.
- **UNKNOWN:** canonical Drowned Listener existence, anatomy, colors/materials, whether sensory organs are externally visible, exact swimming method, mature SOUND/VIBRATION sensing, sounds, VFX, and final art direction.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains identity/descriptor authority. The PNG, GeckoLib model/renderer/animation resources, and Drowned-derived executor are removable presentation/execution adapters and cannot own progression, rewards, Nightmare resolution, or persistence.

## Asset provenance

`assets/shadowslave/textures/entity/drowned_listener.png` is an original project asset authored for this repository's existing 64x64 Drowned Listener UV atlas. No model, texture, sound, structure, novel illustration, adaptation image, or content-mod asset was copied or adapted.

The asset is intentionally simple alpha presentation art. Its purpose is to retire the vanilla Drowned skin while keeping gameplay unblocked; it is not a claim of canonical creature appearance.

## Dependency boundary

No dependency is added. This slice reuses the already-vetted GeckoLib 4.9.2 presentation lane for Minecraft 1.21.1 / NeoForge. SmartBrainLib, Curios, Veil, and TerraBlender remain unadopted.
