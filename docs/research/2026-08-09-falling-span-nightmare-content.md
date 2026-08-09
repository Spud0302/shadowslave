# The Falling Span — authored Nightmare content evidence

**Status:** content/design evidence for `FallingSpanScenarioDefinition`  
**Date:** 2026-08-09  
**Source policy:** `docs/LORE-SOURCE-POLICY.md` is controlling.

## Scope

This slice adds one bounded First-Nightmare-style DESIGN scenario, **The Falling Span**, on top of the existing pure `ResolutionGraph`. It does not alter Nightmare entry, persistence, completion transactions, appraisal, Soul/progression, rewards, death, Seed lifecycle, or runtime NPC/world execution.

The content is deliberately chosen to exercise roadmap requirements that are not well represented by a universal boss-kill structure:

- an inherited historical role with real constraints but limited authority;
- evacuation and route knowledge as meaningful conflict tools;
- the same action producing different consequences depending on prior state;
- an NPC action being able to finish a negotiated terminal resolution;
- a delayed world event being able to finish a prepared terminal resolution;
- multiple morally different endings remaining valid terminal resolutions;
- appraisal evidence remaining separate from completion.

## Primary chapter evidence checked

### Chapter 2 — *Slave Caravan*

Sunny's First Nightmare reconstructs an older situation and places him into a specific historical body/role: a temple slave already embedded in a slave caravan. Physical condition, social status, local actors, available equipment and surrounding circumstances materially constrain what he can do. The narration also contrasts his unusually poor starting role with First Nightmares that can provide more agency/resources.

**Relevant constraint:** First-Nightmare content should begin from role + circumstances rather than from a generic arena or class loadout.

### Chapter 14 — *Child of Shadows*

Sunny's First Nightmare does not resolve through a declared universal boss objective. He uses knowledge of Mountain King's blindness, manipulates the confrontation between other actors, reaches the ruined temple and triggers the reconstructed situation's decisive end. The Nightmare is declared over after the resulting sequence.

**Relevant constraint:** creature death can be part of a resolution without becoming a universal `kill strongest enemy -> win` rule; indirect action and situational manipulation are compatible with the text.

### Chapter 15 — *Shadow Slave*

The Nightmare has already ended before appraisal begins. The Spell then recounts deeds/events and only afterward gives a final appraisal.

**Relevant constraint:** this scenario's terminal-resolution graph and its appraisal-evidence map remain separate concerns. The evidence weights in this project are DESIGN tags/weights, not a canonical scoring equation.

### Chapter 743 — *Appraisal*

Later appraisal again follows an already-ended Nightmare and recounts a broad history of deeds, including survival, sorcery, alliances, large consequences, combat and a battle of wits, before a final verdict. Sunny then theorises about divergence from fate.

**Relevant constraint:** appraisal-relevant deeds are not restricted to kills. Sunny's fate/divergence explanation is character interpretation and must not be promoted to a canonical formula.

### Chapter 2029 — *Fortune Telling*

Much later material explicitly says Sunny received some First-Nightmare identity traits from the nameless temple slave whose body he inhabited.

**Relevant constraint:** the historical body/role is substantive rather than cosmetic presentation. This does not reveal the Spell's role-selection algorithm.

## Publication freshness check

At research time on 2026-08-09, the owner-designated NovelFull access layer listed through Chapter 3116, while official WebNovel exposed current catalogue snapshots around Chapters 3126–3131. NovelFull is therefore treated only as the project-designated chapter-reading access layer, not current-publication authority. No claim in this slice depends on post-2029 material.

## Evidence classification

### CANON

- A First Nightmare can reconstruct a historical situation and place the challenger in another person's substantive historical body/role.
- That inherited situation can impose physical, social and informational constraints that materially shape the trial.
- Nightmare resolution and appraisal are distinct; appraisal follows the ended Nightmare.
- Appraisal narration can include a broad range of deeds and consequences rather than only combat kills.
- The First Nightmare text is compatible with indirect/situational resolution rather than a universal announced boss objective.

### INFERRED

- Historical occupation, local knowledge, relationships, obligations and limited authority are useful separable authoring dimensions for playable Nightmare roles.
- A pure resolution graph can safely model player, NPC and world events as separate sources of scenario events while leaving runtime execution elsewhere.
- The same physical action can reasonably have different scenario meaning after different preparation, because terminal meaning belongs to the reconstructed conflict state rather than to the input event alone.

### DESIGN

Everything specific to **The Falling Span** is project invention:

- scenario ID/name and `span_ward_runner` role;
- Wind Span, West Anchor House, Goat Stair, Scree Shelf and East Gate;
- Sera, Halven, Toma, Captain Rusk and Ivi;
- all five pressure primitives;
- the exact PLAYER / NPC / WORLD event-source taxonomy;
- every event, transition and prerequisite;
- the five resolutions `last_crossing`, `path_below`, `road_denied`, `mountain_decides` and `passage_bargained`;
- all evidence tags and integer weights;
- the delayed storm-driven rockfall and bounded passage negotiation;
- all exact prose and scenario balance.

### UNKNOWN

- The Nightmare Spell's actual role/scenario selection algorithm or probabilities.
- Whether all First Nightmares permit multiple terminal resolutions.
- Any universal number or type of endings.
- Any canonical PLAYER/NPC/WORLD event taxonomy.
- Any canonical relationship between role, historical fate, choices, difficulty or evidence and final appraisal.
- Whether divergence from fate is actually the Spell's appraisal principle; Chapter 743 presents Sunny's theory, not a confirmed scoring formula.
- Any universal rule for when an NPC or delayed environmental consequence can end a Nightmare.
- The exact handling of challenger death/eligibility in unusual delayed-resolution edge cases beyond the current roadmap's separately verified rules.

### COMPATIBILITY

- `ResolutionGraph` remains the Java-owned authority for accepted events and terminal resolution identity.
- This definition owns content only; it does not own persistence, active Nightmare state, progression, appraisal verdict, rewards or teardown.
- Future NPC AI, dialogue, bridge structures, weather, rockfall, crowd movement, sounds and quest/HUD surfaces may be removable adapters consuming already-resolved Java state.
- Adapters must not decide that a terminal resolution occurred merely because a bridge block broke, an NPC accepted dialogue, or a weather effect fired; the Java core must apply the corresponding authored event and own the resulting state.

## Validation contract

`FallingSpanScenarioDefinitionTest` checks:

1. stable content counts and IDs;
2. no declared event is a kill/slay/boss objective;
3. every declared event is accepted in at least one reachable graph state;
4. all five terminal resolutions are reachable by explicit paths;
5. `cut_span` produces `road_denied` when rushed but `last_crossing` after evacuation preparation;
6. `storm_breaks_cliff` is a WORLD event and cannot finish the scenario before the scree shelf is prepared;
7. `accept_passage_terms` is an NPC event and can finish the negotiated resolution without combat;
8. every resolution carries positive, distinct appraisal-evidence shapes while no appraisal verdict is calculated.

No canonical generation, role-selection, ending-count, world-event, persuasion or appraisal-scoring formula is claimed.
