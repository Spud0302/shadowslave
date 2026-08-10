# Drowned Listener GeckoLib presentation boundary — 2026-08-11

## Scope

This slice replaces the vanilla Drowned renderer for the already-authored Java-owned `drowned_listener` Nightmare Creature with a project-owned GeckoLib geometry/animation executor. It does not alter creature identity, Rank/Class, scenario ownership, rewards, progression, Nightmare lifecycle, persistence, sensing rules, or combat authority.

## Sources checked

Repository policy and architecture re-read before implementation:

- `docs/LORE-SOURCE-POLICY.md`
- `docs/JAVA-LORE-ALIGNMENT.md`
- `docs/NIGHTMARE-SEED-ROADMAP.md`
- `docs/THIRD-PARTY-DEPENDENCY-POLICY.md`
- prior entity evidence note `docs/lore-research/2026-08-10-drowned-listener-physical-entity.md`

Primary novel evidence rechecked through the owner-designated NovelFull access layer and official WebNovel chapter identity/publication cross-check:

- Chapter 370, `Exploration Report`: Nightmare Creatures can have creature-specific powers, behavior and weaknesses worth documenting and learning.
- Later/current publication listing was checked to ensure the research access context is not being treated as a frozen endpoint; no later text located for this slice establishes the exact appearance of this project-authored Drowned Listener.

No copied novel text or third-party content-mod art is added.

## Evidence classification

- **CANON:** Nightmare Creatures can differ meaningfully in powers, behavior and weaknesses; those distinctions can matter to survival and preparation.
- **INFERRED:** a physical Minecraft executor benefits from a recognizable creature-specific silhouette instead of an unrelated vanilla model when Java already owns a distinct creature identity.
- **DESIGN:** `drowned_listener` itself; the narrow aquatic humanoid silhouette; lateral listener fins; throat fan; exact proportions; animation timing; Drowned-derived navigation/combat; and GeckoLib renderer/model bindings.
- **UNKNOWN:** canonical Drowned Listener anatomy, color/material palette, whether any hearing/vibration organ is externally visible, exact locomotion, final attack animation, bespoke sound design, and the implementation of SOUND/VIBRATION sensing.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains creature identity/descriptor authority. GeckoLib and the entity renderer are removable presentation infrastructure. Replacing them must not require canonical player-state migration or alter rewards/progression/Nightmare resolution.

## Placeholder boundary

The geometry is project-authored, but it intentionally maps Minecraft's vanilla Drowned texture until a bespoke project texture can be authored and reviewed. Vanilla Drowned water/ground navigation, hostile targeting, melee behavior and sounds also remain temporary execution behavior. The visible fin/throat shapes are presentation DESIGN only; they must not be cited later as evidence that canon defines those organs.

## Dependency boundary

No dependency is added by this slice. It reuses the already-audited GeckoLib `4.9.2` / Minecraft `1.21.1` / NeoForge lane from the active presentation stack. GeckoLib remains presentation-only and owns no canonical state.
