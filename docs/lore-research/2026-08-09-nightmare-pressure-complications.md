# Nightmare pressure / complication primitives — evidence note

**Status:** bounded player-facing DESIGN slice for review  
**Implementation:** `NightmarePressureComplicationCatalog`  
**Generator version:** `nightmare-pressure-v1`

## Scope

This slice adds reusable authored pressure cards for a Nightmare whose identity has already been resolved by the Java core. It does **not** add a Nightmare-generation algorithm, difficulty curve, random-event scheduler, appraisal formula, persistence layer, or world mutation system.

The catalogue contains six DESIGN families with four primitives each:

- environmental deterioration;
- misinformation;
- divided obligation;
- resource loss;
- time-sensitive route change;
- delayed consequence.

A caller explicitly supplies the already-resolved scenario ID and allowed families. Positive evidence tags may prefer a compatible authored primitive, but evidence magnitude is deliberately ignored. A deterministic seed selects only among those bounded primitives and their two presentation cues.

## Primary chapter evidence checked

### Chapter 2 — *Slave Caravan*

**CANON:** Sunny's First Nightmare places him into a reconstructed historical situation with a substantive inherited body/identity and adverse starting circumstances. The scenario is not presented as a neutral arena whose only meaningful variable is enemy strength.

**Constraint used here:** reusable pressure content may describe circumstances and obligations around a resolved scenario, but must not claim that these six families are categories used by the Spell.

### Chapter 14 — *Child of Shadows*

**CANON:** Sunny acts on situational knowledge, environmental context, deception, movement, and a creature weakness during the decisive part of the First Nightmare. The trial ends after the reconstructed conflict reaches its decisive outcome; the text does not establish a universal UI objective or universal boss-template algorithm.

**Constraint used here:** complications may create information, route, environmental, or obligation decisions. They must not independently decide terminal resolution; that remains the scenario's Java `ResolutionGraph` concern.

### Chapter 15 — *Shadow Slave*

**CANON:** the First Nightmare is already over before the Spell completes its appraisal and progression sequence.

**Constraint used here:** pressure selection must not calculate appraisal, Aspect/Flaw outcomes, rewards, or whether the Nightmare has ended.

### Chapter 743 — *Appraisal*

**CANON:** a later Nightmare appraisal recounts a broad range of deeds and consequences before giving a verdict. The chapter supports deeds beyond one narrow combat statistic being relevant to the Spell's narration.

**UNKNOWN:** Sunny's later theory about divergence from the original course of fate is character interpretation in this chapter, not a verified universal scoring formula.

**Constraint used here:** pressure cards can expose different kinds of player decisions but must not attach canonical appraisal weights or infer a verdict.

### Chapter 2029 — *Fortune Telling* (later clarification)

**CANON:** Sunny explicitly attributes some of his First-Nightmare Attributes and his initial Aspect to the historical temple slave whose body he inhabited. This is later confirmation that the inherited Nightmare body can carry substantive identity rather than being cosmetic staging.

**Constraint used here:** scenario/role authority remains upstream of this catalogue. Complication composition may not reroll or reinterpret the challenger's historical role.

## Evidence classifications

### CANON

- Nightmares can place challengers into substantive reconstructed historical circumstances and bodies/roles.
- Situational information, environmental conditions, movement, deception, and creature-specific knowledge can materially matter to what a challenger does.
- Nightmare resolution precedes appraisal.
- Appraisal can narrate broad deeds and consequences rather than only one combat statistic.
- Later text confirms that a First-Nightmare historical body can contribute substantive identity traits.

### INFERRED

- For implementation, scenario identity, scenario resolution, local pressure presentation, and later appraisal can be represented as separable concerns.
- A reusable complication may offer player-facing choices without itself changing authoritative conflict state.
- Scenario authors can explicitly allow only pressure families coherent with their authored conflict.

These are architecture/content inferences, not claims about internal Spell data structures.

### DESIGN

- the six pressure families and all 24 primitive IDs, names, prompts, response hooks, affinity tags, and presentation cues;
- selecting one primitive from caller-authorized families;
- positive-tag matching as a bounded preference mechanism;
- ignoring evidence magnitude so `1` and `999` do not become an invented difficulty/appraisal formula;
- SHA-256-based deterministic tie selection;
- `nightmare-pressure-v1` and all anti-overclaim text.

### UNKNOWN

The novel research performed for this slice does not establish:

- a canonical complication-generation or random-event formula;
- complication frequencies, probabilities, counts, or difficulty weights;
- a universal taxonomy corresponding to these six families;
- canonical timers, flood rates, weather curves, resource-loss rates, durability, NPC reliability, credibility scores, route-finding odds, or structural simulation;
- a universal relationship between particular pressures and Rank, Class, role, fate, appraisal, Aspect, Flaw, rewards, or success;
- whether a given authored complication actually occurs in any specific Nightmare unless that scenario's Java-owned content/runtime accepts it.

### COMPATIBILITY

- Java owns the resolved scenario ID, accepted scenario events, `ResolutionGraph`, terminal resolution, challenger outcomes, appraisal inputs, and any persistent complication state.
- This catalogue is immutable Java content and pure deterministic presentation composition.
- External mods or adapters may render sounds, weather, structures, NPC behaviour, particles, messages, route markers, damaged items, or other execution/presentation corresponding to an accepted complication, but removing an adapter must not remove or rewrite canonical Java progression state.
- A presentation adapter must never infer that a pressure prompt itself is an accepted scenario event or a terminal resolution.

## Validation target

Focused tests require:

1. exactly 24 unique primitives, four per family;
2. three response hooks, affinity tags, and two presentation cues per primitive;
3. deterministic same-input composition;
4. evidence-magnitude independence;
5. evidence-compatible preference without truth inference;
6. preservation of caller-owned scenario identity across a 4,096-seed sweep;
7. no escape from caller-authorized families;
8. reachability of all 24 primitives and all 48 primitive/cue pairs in a deterministic sweep;
9. fail-closed blank scenario IDs, empty family sets, and negative evidence;
10. explicit anti-overclaim language around probability, difficulty, appraisal, and Java-owned resolution authority.

## Limitations

This slice does not wire complications into active Nightmare instances. That is intentional: the content is reviewable and mergeable independently of the large persistence/recovery stack. A future Java-owned scenario instance may persist a resolved complication ID/seed if exact replay or delayed execution needs it, but the authoritative acceptance of resulting events must remain in the scenario engine.
