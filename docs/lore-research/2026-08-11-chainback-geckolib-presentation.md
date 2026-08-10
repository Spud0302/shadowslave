# Chainback GeckoLib presentation boundary

Date: 2026-08-11

This note records the evidence boundary for replacing the Chainback's vanilla Spider renderer with project-owned GeckoLib geometry and animation. It does not establish a canonical Chainback appearance.

## Primary/later checks

- Chapter 201, `Lord of the Dead`: later Nightmare Creature discussion explicitly separates Class from a purely quantitative ladder and describes qualitative capability changes between Classes.
- Chapter 370, `Exploration Report`: Nightmare Creatures are worth documenting individually by their powers, behavior, and weaknesses; Dream Realm creature identity is therefore not well represented by one interchangeable vanilla mob visual.
- Official WebNovel Chapter 370 was cross-checked for chapter identity/publication wording.

No checked chapter establishes the anatomy, color, proportions, chains, gait, attack animation, or sound of this project's authored `chainback` creature. Those details remain non-canonical project presentation.

## Classification

- **CANON:** Nightmare Creatures can differ meaningfully in powers, behavior, weaknesses, and qualitative capability; creature Rank/Class remain separate concepts.
- **INFERRED:** a physical Minecraft executor benefits from a recognizable creature-specific presentation instead of retaining an unrelated vanilla Spider visual.
- **DESIGN:** the authored Chainback profile, tall hunched chained silhouette, jaw/limb proportions, hanging-chain geometry, idle/walk/strike clips, renderer scale/shadow choices, and continued Spider-derived execution behavior.
- **UNKNOWN:** canonical Chainback anatomy, exact materials/colors, whether literal external chains are appropriate, movement gait, combat choreography, sound set, displacement mechanic, and all occurrence/reward rules.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains creature identity authority. GeckoLib is removable presentation infrastructure; Spider-derived movement/climbing/hostility remain replaceable execution behavior and cannot own Rank/Class, progression, rewards, Nightmare state, or persistence.

## Dependency boundary

This slice reuses the already-admitted GeckoLib 4.9.2 dependency from the Ash Burrower presentation stack. It adds no new runtime dependency, packaging component, or third-party content asset. The temporary texture remains Minecraft's vanilla Spider texture; the geometry and animation JSON are new project assets.
