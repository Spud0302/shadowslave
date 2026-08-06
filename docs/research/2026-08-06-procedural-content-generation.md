# Procedural content generation direction

**Research date:** 2026-08-06  
**Status:** owner-requested design direction and research note; not yet a binding implementation contract  
**Applies to:** shared Java core, standalone mod and Nightmare Spell modpack

## 1. Owner direction

The project should investigate procedural generation so that important Shadow Slave systems are not limited to a finite catalogue of fully predefined results.

The primary targets are:

- Aspects;
- Flaws;
- Attributes;
- Nightmare scenarios and historical roles;
- Nightmare Creatures;
- Memories;
- Echo snapshots;
- appraisal narratives;
- Nightmare Seeds, Gates and crises;
- Dream Realm regions;
- later Aspect Legacies and Domains.

This does **not** mean unrestricted randomness. The intended direction is constrained, lore-aware, deterministic generation from curated components, compatibility rules and recorded player history.

## 2. Core principle

Use this shape:

```text
player evidence
+ Nightmare role and reconstructed history
+ meaningful choices
+ conflict resolution
+ affinities and prior identity
+ weighted rarity
+ deterministic generation seed
    -> generated candidates
    -> coherence and balance validation
    -> final persistent instance
```

Randomness should create content **inside established lore rules**, not randomise the rules themselves.

## 3. Persistence and reproducibility requirements

Every generated result must be saved as a complete resolved instance. It must not reroll because:

- the server restarted;
- the player reconnected;
- a dependency was removed;
- generator weights changed;
- a newer generator version was installed.

Persist at least:

```text
generation_seed
generator_version
selected component IDs
recorded evidence and provenance
final resolved definition
external execution-provider bindings, if any
```

A generator upgrade may affect future generation, but existing identities, Nightmares, Memories and Echoes must retain their resolved data unless an explicit migration is performed.

## 4. Aspects

Aspects are the strongest first candidate for constrained procedural generation.

Do not generate a bag of unrelated abilities. Generate a coherent supernatural nature and derive its expressions from that nature.

Possible component layers:

```text
core nature
archetype
expression families
secondary motifs
constraints and tensions
Nightmare-role influence
player-choice evidence
rarity and Aspect Rank
future evolution directions
```

Example primitives:

- **nature:** shadow, flame, distance, silence, memory, hunger, growth, reflection;
- **archetype:** guardian, hunter, witness, servant, wanderer, creator, sovereign;
- **expression:** control, perception, movement, reinforcement, transformation, creation;
- **tension:** preparation, exposure, dependence, range, sacrifice, vulnerability.

A generated Aspect instance should support:

```text
instance ID
formal name or unrevealed name state
Aspect Rank
core nature
Innate Ability, when applicable
abilities by Soul Rank
ability evolution paths
potential Aspect Legacy direction
provenance and appraisal evidence
generation metadata
```

Formal names may be assembled from curated semantic vocabulary, but the model must also support an Aspect whose existence and abilities are known while its formal name remains unrevealed.

## 5. Flaws

Flaws should be generated from personal and metaphysical tensions, not from one table of negative potion effects and not by simply choosing the opposite of the Aspect.

Supported families should eventually include:

- compulsions and obligations;
- perception and attention effects;
- social or communication constraints;
- resource recovery or consumption tensions;
- attachment-based vulnerabilities;
- identity, memory or emotional consequences;
- bodily transformations and sensitivities;
- conditional limitations;
- long-horizon consequences;
- paradoxical effects that can be useful while remaining personally cruel.

A generated Flaw should record:

```text
instance ID
formal name or unrevealed name state
effect family
server-authoritative rule
conditions and scope
knowledge/revelation state
connection to identity, choices and Aspect
provenance and generation metadata
```

Behavioral Flaws should be implemented as meaningful constraints or pressures where feasible, rather than flattened into numerical debuffs.

## 6. Attributes

Attributes can be generated from:

- innate traits;
- lineage;
- assigned Nightmare role;
- divine or supernatural contact;
- exceptional actions;
- exposure to places, beings or phenomena;
- long-term transformations.

An Attribute definition should support:

```text
name
origin
semantic description
mechanical tags and effects
hidden interactions
evolution conditions
visibility state
provenance
```

Attributes may evolve or combine when the lore and design justify it. They must not become generic Strength, Dexterity or Vitality statistics presented as canon.

## 7. Nightmare scenarios

Nightmares should use authored modules plus procedural assembly.

A reusable scenario definition may combine:

```text
historical setting
roles
central conflict
factions
hidden truths
relationships
time or environmental pressure
possible resolutions
participant-specific outcomes
consequences and appraisal evidence
```

Procedural assembly must be validated before activation. A generated Nightmare must guarantee:

- at least one reachable terminal resolution;
- reachable critical objectives;
- required clues and entities exist;
- participant roles are valid;
- every ending records recoverable outcome data;
- restart and disconnect recovery are possible;
- appraisal evidence can be reconstructed deterministically.

The first implementation should extend The Last Signal with multiple authored resolutions before attempting broad scenario synthesis.

## 8. Historical roles

Roles can be generated or matched from:

```text
occupation and social status
relationships
physical condition
existing enemies and obligations
knowledge and secrets
temporary Attributes or provisional powers
personal objective
faction alignment
```

Role assignment may use similarities of circumstance, affinity or fate as design weights. The repository must not call an exact matching algorithm canonical because canon does not provide one.

Later multiplayer Nightmares should support participants occupying different and potentially opposing roles.

## 9. Nightmare Creatures

Creature generation can combine:

```text
body plan
Rank
Class
origin and corruption theme
movement profile
defences
attack patterns
special abilities
intelligence package
minion ecology
weaknesses
visual mutations
```

Rank and Class remain fixed canonical systems. Class must change qualitative behaviour rather than only statistics:

- Beast: primarily instinctive;
- Monster: more adaptive;
- Demon: meaningful intelligence;
- Devil: distinct supernatural ability package;
- Tyrant: creates or controls lesser beings;
- Terror: imposes large environmental influence;
- Titan: scenario-scale calamity.

## 10. Memories

Memory generation should derive from source, Rank, Tier and event history.

Possible inputs:

```text
source creature, person or event
Rank
Tier
artifact type
materials and visual identity
enchantment themes
conditions and limitations
curse or growth rules
```

Validation must enforce:

- Rank potency;
- Tier capacity;
- enchantment compatibility;
- sensible cost and activation rules;
- persistence independent of execution provider.

Rare generators may produce growing Memories, cursed Memories, Shard Memories, Bound relics or Legacy Relics, but these must be exceptional rule sets rather than ordinary rolls.

## 11. Echoes

An Echo should be generated as a persistent snapshot of an eligible defeated entity or an artificial template:

```text
source archetype
Rank and Class
appearance variant
ability package
AI package
summoning and essence cost
provenance
generation version
```

Ordinary Echoes remain static replicas. Growth-capable Shadows, Reflections and other special summons belong to separate Aspect-specific systems.

## 12. Appraisals and Spell presentation

Appraisal content may be procedurally assembled, but only from authoritative recorded events.

Evidence may include:

```text
survival and deaths
objectives completed
relationships changed
secrets discovered
sacrifices and betrayals
protected or abandoned entities
unnecessary kills
historical deviation
terminal resolution
participant-specific outcome
```

The grade and rewards must be deterministic and testable. Generated prose must not invent events.

Spell presentation may use structured templates for:

- terse bracketed notifications;
- status/rune readouts;
- mythic appraisal summaries;
- restrained, occasionally cruel irony.

## 13. Seeds, Gates and crises

A generated Seed may own:

```text
category
Dream Realm region
historical era
scenario family
Call strength
maturation time
Gate manifestations
creature ecology
Guardian profile
environmental corruption
```

If the Seed blooms, the Gate crisis should be derived from the same persistent definition so the Nightmare and waking-world breach remain connected.

## 14. Dream Realm regions

Dream Realm region generation can combine:

```text
consumed-world origin
geography
environmental or metaphysical law
day/night danger cycle
dominant corruption
creature ecosystem
ruins and historical layers
Citadel type
travel constraints
resource profile
```

A region should be distinguished by rules and history, not only by block palette.

## 15. Aspect Legacies and Domains

Legacies and Domains may eventually use procedural support, but they should emerge from long-term identity and behaviour rather than one random reward roll.

An Aspect Legacy may derive from:

```text
Aspect nature
repeated player behaviour
skill mastery
conceptual insight
unique unlock condition
```

A Domain may derive from:

```text
Aspect source and authority
territory, people or concepts influenced
repeated expression of Will
concept embodied by the player
```

These are late systems and should not block the initial vertical slice.

## 16. Rules that should remain fixed

Do not procedurally rewrite:

- the seven Soul Ranks;
- the Nightmare Creature Rank ladder;
- the creature Class ladder;
- the distinction between status, Soul Rank and Aspect Rank;
- the distinction between First and later Nightmares;
- Memory Rank and Tier meaning;
- canonical terminology;
- technical recovery requirements;
- recorded player actions and history;
- completed persistent rewards.

## 17. Recommended technical architecture

Use four layers:

```text
1. Curated primitives
   Themes, archetypes, effects, costs, names and scenario modules.

2. Compatibility graph
   Valid and invalid component combinations, prerequisites and exclusions.

3. Evidence-weighted deterministic generator
   Produces candidates using history, affinities and a saved seed.

4. Validator and resolver
   Rejects incoherent, impossible, overpowered or technically unsafe definitions,
   then freezes the accepted result into persistent instance data.
```

External mods may execute or display generated abilities, creatures or equipment, but the Shadow Slave core must own the canonical generated definition and save data.

## 18. Recommended first implementation experiment

After crash-safe Nightmare completion is implemented:

1. record structured evidence from The Last Signal;
2. define a small curated library of Aspect natures, expressions and tensions;
3. generate several deterministic Aspect/Flaw candidate pairs;
4. validate them for semantic coherence and power budget;
5. select and persist one pair;
6. verify restart and dependency-removal stability;
7. compare the generated result with the fixed Last Light / Cold Ash preview identity.

This experiment should remain bounded. Do not attempt fully procedural terrain, creatures, Memories and identities in the same first pass.

## 19. Roadmap consequence

The roadmap should treat procedural generation as a core architectural objective, not a late content shortcut. Immediate correctness work still comes first:

```text
crash-safe resolution/appraisal transaction
-> reusable event, outcome and appraisal model
-> data-driven primitives and compatibility graph
-> bounded Aspect/Flaw generator experiment
-> procedural Memories and creatures
-> modular Nightmare assembly
-> Seeds, Gates and Dream Realm regions
-> later Legacies and Domains
```

The desired outcome is effectively unbounded variety built from finite, testable and versioned components while preserving coherent identity, deterministic saves and lore boundaries.