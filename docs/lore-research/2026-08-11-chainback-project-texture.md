# Chainback project texture — 2026-08-11

## Scope

Retire the vanilla Spider texture from the existing Chainback GeckoLib physical executor without changing its Java-owned creature identity, Rank/Class, AI, rewards, Nightmare lifecycle, persistence, or scenario authority.

## Sources checked

Repository status/policy and active gameplay/correctness/world branches were re-read before implementation, including `docs/THIRD-PARTY-DEPENDENCY-POLICY.md`. Primary Chapter 370 (`Exploration Report`) was rechecked through the owner-designated NovelFull access layer and official WebNovel publication. It supports the general proposition that individual Nightmare Creatures can have distinct powers, behavior and weaknesses. It does not establish this project's Chainback anatomy, palette, literal chain placement, skin/material treatment, or Minecraft animation/render implementation.

## Evidence classification

- **CANON:** individual Nightmare Creatures can have meaningful creature-specific powers, behavior and weaknesses.
- **INFERRED:** a distinct Java-owned creature identity benefits from a stable, recognizable physical presentation rather than an unrelated vanilla mob skin.
- **DESIGN:** Chainback itself, the existing hunched chained geometry, this exact soot-brown/scarred surface treatment, iron-chain atlas treatment, abrasion bands and all exact colors/UV choices.
- **UNKNOWN:** canonical Chainback anatomy, skin/material/color, chain placement/material, markings, wounds, gait, sounds, VFX and mature displacement behavior.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains creature identity/descriptor authority. The PNG, GeckoLib model/renderer/animations and Spider-derived navigation/melee remain removable execution/presentation infrastructure and cannot own progression, rewards, Nightmare resolution or persistence.

## Asset provenance

`assets/shadowslave/textures/entity/chainback.png` is an original Shadow Slave project asset created specifically for this repository's existing 64x64 Chainback UV atlas. No texture, model, sound, structure or other asset was copied or adapted from a third-party content mod or novel publication.

The texture intentionally stays simple: soot-brown body surfaces with scar/abrasion bands and a separate cold-iron/rust treatment for the UV area used by the hanging chain bones. It is project DESIGN and may be replaced as art direction improves.

## Dependency boundary

No dependency is added. The slice reuses the already-admitted GeckoLib 4.9.2 presentation lane for Minecraft 1.21.1 / NeoForge. SmartBrainLib, Curios, Veil and TerraBlender remain unadopted.
