# The Drowned Bell — First Nightmare content module

**Status:** authored content slice, stacked on the reusable resolution engine.  
**Runtime status:** pure content definition; not yet wired into world generation or player entry.  
**Scenario ID:** `the_drowned_bell`  
**Historical role:** `bell_keepers_apprentice`

## Player-facing premise

The challenger inhabits the role of a young bell-keeper's apprentice in an isolated cliff settlement during an advancing storm tide. The village warning bell is damaged, the old quarry tunnels are disputed and partially sealed, the harbour floodgate can redirect the surge only at a cost, and one creature is following strong vibration through the flooded caves.

The scenario is designed around imperfect information and limited time rather than a mandatory boss fight. The central conflict is preserving lives through the storm while surviving the Drowned Listener and deciding which part of the settlement can realistically be saved.

## Authored content

Locations:

- Bell Tower — repairable warning bell and exposed final hold point;
- Old Quarry Tunnels — high-ground evacuation route and potential collapse trap;
- Sea Gate — flood-control structure able to redirect the worst surge;
- Lower Village — homes, stores and civilians most exposed to the tide.

Named scenario characters:

- Mara — injured bell-keeper who knows the warning code;
- Oren — quarry foreman who knows the unsafe escape passages;
- Vesh — harbour reeve prioritising stores and flood control;
- Nemi — child witness who noticed the creature follows resonance rather than sight.

Nightmare Creature:

- **Drowned Listener** — authored **DESIGN**, classified here as a Dormant Monster. It tracks strong vibration and resonance. It is deliberately the scenario's only Nightmare Creature; the remaining danger is environmental or human.

## Resolution paths

The reusable `ResolutionGraph` accepts four distinct terminal solutions:

1. **Last Bell Standing** — repair and ring the bell, holding the warning tower long enough for the village to scatter;
2. **Road Above the Tide** — open the quarry route and personally guide civilians to high ground;
3. **Break the Water** — reach the sea gate and redirect the surge toward the abandoned cut;
4. **Silence Below Stone** — repair the bell, use it to lure the Drowned Listener into the quarry approach, then collapse the entrance over it.

No path is defined as the singular canonical answer. The graph rejects events that do not make sense in the current conflict state, so the final action requires the relevant preparation.

Each terminal resolution carries a distinct set of positive evidence weights. These are future inputs to procedural appraisal, not a canonical Spell formula. For example, evacuation emphasises guidance/preservation, flood diversion emphasises water/precision, and the quarry trap emphasises sound/precision/retaliation.

## Lore evidence

Research followed `docs/LORE-SOURCE-POLICY.md` and used chapter text rather than wiki summaries as authority.

### CANON

- **Chapter 2:** the First Nightmare places Sunny into a concrete historical role and surrounding circumstances, and explicitly contrasts his unusually poor starting position with more typical First-Nightmare roles that can provide meaningful agency.
- **Chapter 14:** a First Nightmare ends when the decisive scenario conflict is resolved; Sunny's victory does not require following an obvious prescribed combat script.
- **Chapter 15:** after the Nightmare ends, the Spell appraises the challenger and recounts significant actions and achievements rather than reducing the trial to a binary survival flag.
- **Chapter 743:** Sunny later reasons that appraisal can distinguish how strongly a challenger diverged from the original course of events. This supports preserving meaningful resolution history while not claiming a known numerical appraisal formula.

### INFERRED

- A reusable First-Nightmare scenario can offer several coherent ways to change or resolve its central conflict rather than one quest-marker solution.
- Recording distinct resolution evidence is appropriate input for a later appraisal system because the Spell demonstrably considers what the challenger did, not only whether they lived.
- A First-Nightmare role should be embedded in the reconstructed event and constrain what the player initially knows and can influence.

### DESIGN

Everything specific to The Drowned Bell is Minecraft project design: its settlement, historical role, characters, Drowned Listener, creature classification, locations, storm timing, choices, state graph, terminal names and appraisal evidence weights.

The graph implementation and all event IDs are also DESIGN. Canon does not provide this data model.

### UNKNOWN

- The exact algorithm by which the Spell chooses a First-Nightmare role or event is unknown.
- The exact formula used for appraisal is unknown.
- Canon does not establish that every First Nightmare permits four resolutions, uses a settlement-scale conflict, or contains a Dormant Monster.
- The project does not assume that changing fate is the sole universal appraisal variable; Chapter 743 presents Sunny's reasoning, not a complete specification of the Spell.

### COMPATIBILITY

- canonical state remains owned by the Java core;
- this slice does not write player data, Nightmare registry data, Soul state or appraisal state;
- it depends only on PR #40's pure resolution engine;
- later world/presentation providers must remain removable and cannot become authoritative owners of scenario completion;
- eventual appraisal integration should convert a committed terminal resolution into persisted evidence before permanent progression, using the restart-safe completion stack rather than rerunning content logic after a crash.

## Validation

`DrownedBellScenarioDefinitionTest` covers:

- complete content counts and one declared Nightmare Creature;
- all authored choices are reachable through at least one accepted graph state;
- all four terminal resolutions are reachable through explicit event sequences;
- unavailable terminal actions fail closed without mutating state;
- each ending provides non-empty positive and distinct appraisal-evidence weights.

## Integration boundary

This PR deliberately does not build blocks, spawn NPCs, start the scenario, persist graph state, award appraisal results or add a scenario selector. Those are later integration slices and should reuse existing state ownership instead of embedding canonical state in a content/provider layer.

A useful next content slice is a modular creature/encounter catalogue shared by multiple authored Nightmares, or a third scenario with a social/deception central conflict so scenario variety is not dominated by environmental survival.
