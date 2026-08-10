# Ash Burrower project texture boundary — 2026-08-11

## Scope

This slice retires the vanilla Silverfish texture placeholder from the already-authored hostile
Ash Burrower and player-owned Ash Burrower Echo. Both physical executors now use one original
Shadow Slave project texture mapped onto their already-shared GeckoLib creature geometry.

It changes presentation only. It does not change creature identity, Echo ownership, Rank/Class,
combat behavior, rewards, appraisal, progression, Nightmare resolution, or persistence.

## Sources checked

Repository authority/policy re-read before implementation:

- `PROJECT-STATUS.md`
- `GPT_HANDOFF.md`
- `ISSUES.md`
- `docs/LORE-SOURCE-POLICY.md`
- `docs/JAVA-LORE-ALIGNMENT.md`
- `docs/NIGHTMARE-SEED-ROADMAP.md`
- `docs/THIRD-PARTY-DEPENDENCY-POLICY.md`
- active GeckoLib presentation PRs #207 and #210
- active world/Echo/correctness PRs #206, #209, #211, #203, and #208

Primary novel evidence was rechecked through the owner-designated NovelFull access layer and
official WebNovel chapter identity/publication cross-check:

- Chapter 370, `Exploration Report`, supports individual Nightmare Creatures having meaningful
  creature-specific powers, behavior, and weaknesses.
- Current/later publication context was checked for this presentation task; no primary text found
  establishes the exact appearance, color palette, shell material, or markings of this
  project-authored Ash Burrower.

No novel text, content-mod model, content-mod texture, or third-party art is copied into the asset.

## Evidence classification

- **CANON:** individual Nightmare Creatures can have meaningful creature-specific powers, behavior,
  and weaknesses.
- **INFERRED:** a distinct Java-owned creature identity benefits from a stable recognizable physical
  presentation; a creature-derived Echo may share that source creature's visual identity while
  ownership and command state remain separate Java authority.
- **DESIGN:** `ash_burrower`, the existing segmented geometry, and this exact charcoal/ash mineral
  palette, abrasion marks, plate bands, and restrained ember-like seams.
- **UNKNOWN:** canonical Ash Burrower anatomy, colors, materials, surface markings, glow, sexual or
  age variation, and whether an Echo should receive any separate manifestation tint/effect.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` and Echo ownership/instance data remain
  canonical authority. The PNG, GeckoLib model binding, renderer, and animations are removable
  presentation infrastructure and require no canonical-state migration.

## Asset/provenance boundary

`assets/shadowslave/textures/entity/ash_burrower.png` is an original project asset created for this
repository. It is a simple 64x32 pixel texture authored to the existing project geometry UV atlas.
No external content-mod asset is used or adapted.

Both hostile and Echo model bindings reference the same project texture. Focused tests pin that
shared identity, reject regression to the vanilla Silverfish texture path, and verify the PNG
matches the geometry's 64x32 atlas dimensions.

## Dependency boundary

No dependency is added. This reuses the active vetted GeckoLib 4.9.2 lane for Minecraft 1.21.1 /
NeoForge. GeckoLib is MIT-licensed code infrastructure and remains presentation-only. SmartBrainLib,
Curios, Veil, and TerraBlender remain unadopted.
