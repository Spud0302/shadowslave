# Nightmare Creature content wave 1

**Status:** authored player-facing content foundation.  
**Runtime status:** data only; not yet wired to entity spawning or scenario selection.  
**Classification:** Minecraft **DESIGN** constrained by verified Shadow Slave creature lore.

## Purpose

Future Nightmares should not require one bespoke Java class for every encounter. This slice adds a reusable authored catalogue describing how a creature pressures a scenario: what it notices, how it moves, which environments suit it, what counterplay the scenario can expose, and which appraisal-evidence themes an encounter can record.

The catalogue does **not** generate canonical Nightmare Creatures and does not claim that the Nightmare Spell uses these fields.

## Player-facing content

Wave 1 contains twelve authored creatures:

- Ash Burrower
- Bell-Eater
- Chainback
- Drowned Listener
- Glasswing
- Gutter Choir
- Hollow Mimic
- Mire Runner
- Pale Ferryman
- Stone Maw
- Thorn Matron
- Veil Stalker

The content spans Dormant, Awakened and Fallen ranks and Beast, Monster, Demon and Devil classes. Encounter pressure includes ambush, pursuit, area denial, pack coordination, deception, attrition and displacement. Locomotion includes ground movement, climbing, burrowing, swimming and gliding.

Every profile carries authored counterplay that is more specific than "deal enough damage". Examples include exploiting solid floors against a burrower, using false echoes against a sound hunter, verification phrases against a mimic, alternative crossings against a river predator, controlled burns against overgrowth and shared watch against a mist ambusher.

## Lore evidence checked

Research followed `docs/LORE-SOURCE-POLICY.md` and used chapter text as authority.

- **Chapter 74:** confirms the seven creature Classes from Beast through Titan as a distinct scale from Rank and connects higher Class to increasingly special results.
- **Chapter 201:** explicitly describes Class as more than raw might; higher Classes gain qualitative characteristics, with intelligence appearing by Demon and Tyrants exercising authority through lesser creatures.
- **Chapter 380:** demonstrates multiple creature Ranks coexisting in one region and being treated as a separate threat axis from Class.
- **Chapter 1609:** shows that creatures of similar broad power can have very different useful body plans and roles, including rideable forms selected for speed rather than direct combat value.
- **Chapter 1652:** shows a deliberately mixed collection of Nightmare Creatures differing in size, nature and environmental resistance, reinforcing that encounter ecology cannot be reduced to one stat template.

### Evidence labels

**CANON**

- Nightmare Creatures have separate Rank and Class measures.
- Beast through Titan are established Classes.
- Class can change a creature qualitatively, not merely increase raw power.
- Nightmare Creatures vary greatly in form, powers, movement and environmental interaction.

**INFERRED**

- A reusable Minecraft content model should represent encounter-relevant differences independently rather than encode every creature as one health/damage progression.
- Scenario authors benefit from explicit counterplay and environmental hooks because canon repeatedly presents creatures as problems whose nature matters.

**DESIGN**

- all twelve creature names;
- exact Rank/Class assignments for these invented creatures;
- `Sense`, `Locomotion` and `Pressure` enums;
- environment, counterplay and appraisal-evidence tags;
- presentation cues and all implied Minecraft encounter mechanics.

**UNKNOWN**

- no canonical procedural creature-generation formula is known;
- no universal mapping from Rank/Class to body plan, sense, locomotion or encounter behavior is claimed;
- this slice does not define universal First-Nightmare creature-selection limits.

**COMPATIBILITY**

- the Java core remains the sole owner of canonical Nightmare instance and progression state;
- external entity/animation/AI providers may later render these profiles but must not become canonical state owners;
- `drowned_listener` deliberately matches PR #78's authored Dormant Monster concept so that scenario can later consume a reusable profile instead of duplicating creature identity.

## Validation

`NightmareCreatureContentCatalogTest` checks:

- twelve unique creature IDs;
- coverage across all declared Wave-1 Ranks, Classes, senses, locomotion modes and pressure modes;
- non-empty counterplay, appraisal evidence and presentation cues for every creature;
- at least thirty distinct counterplay tags across the catalogue;
- reusable Drowned Listener identity;
- meaningful representation of deception, area-denial and displacement encounters rather than only direct pursuit.

## Integration boundary

This PR does not register Minecraft entities, AI goals, loot, Memories, spawn tables or scenario state. A later adapter can map stable creature profile IDs to removable presentation/execution implementations while scenario and progression authority remains in Java.

The best next content step is a **Memory content foundation** with authored weapon/armor/tool/charm concepts, Rank/Tier identity, enchantment-role descriptors and acquisition provenance, while keeping exact Memory-generation and drop formulas explicitly UNKNOWN.
