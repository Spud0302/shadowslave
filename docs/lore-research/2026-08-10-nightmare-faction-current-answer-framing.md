# Nightmare faction current-answer framing — lore evidence

**Date:** 2026-08-10  
**Scope:** player-facing framing for an answer whose identity and frame have already been resolved by Java.  
**Implementation:** `NightmareFactionCurrentAnswerFramingCatalog`.

## Repository context checked

Before implementation, this slice re-read current `main`, open pull requests and issues, `PROJECT-STATUS.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and adjacent Nightmare NPC, evidence, investigation, faction-pressure, negotiation, commitment, consequence, history, re-encounter, and current-intent work.

PR #167 already provides bounded current-intent questions. This slice does not import or duplicate that branch. It is based directly on current `main` and accepts opaque already-resolved question/answer identities plus the exact answer frame, keeping the content independently reviewable and rebaseable while the faction stack remains open.

A fresh Codex review on #167 also identified a reusable content-definition defect: affinity-tag sets were exposed mutably. This slice freezes authored affinity tags in the record constructor so a selected primitive cannot be mutated after validation.

## Primary and later chapter checks

### Chapter 14 — *Child of Shadows*

The decisive First-Nightmare sequence depends on asymmetric situational knowledge and deliberate manipulation of what another actor does and does not know. Sunny acts on a fact Hero does not possess, while neither participant has omniscient information. The Nightmare ends before appraisal begins.

This supports an important negative boundary: a statement or answer can be consequential without becoming omniscient truth authority.

### Chapter 737 — *Self-Reflection*

In a later Nightmare, Sunny remains uncertain whether a nominal ally will become an adversary. He deliberately uses conversation and misleading questions to create a false impression and constrain another participant's action. The scene supports treating what was said, what was withheld, and what another actor inferred as distinct from verified motive or durable allegiance.

### Chapter 743 — *Appraisal*

The Spell first states that the Nightmare is over and only then begins appraisal. Its retrospective narration includes broad deeds and a battle of wits. This supports keeping answer presentation separate from terminal resolution and later appraisal. Sunny's subsequent theory about fate and appraisal is character interpretation and is not used as a canonical generation or scoring formula.

## Source freshness

The owner-designated NovelFull access layer currently lists through Chapter 3116. Official WebNovel snapshots checked during this run are not perfectly synchronized, reporting roughly 3,127–3,131 chapters and latest indexed releases around Chapter 3126–3130. No implementation claim in this slice depends on material later than Chapter 743.

## Evidence classification

### CANON

- Nightmare action can materially depend on incomplete or asymmetric situational information.
- Strategic conversation, misleading questions, and uncertain intent can remain consequential inside a Nightmare.
- Nominal cooperation does not necessarily reveal complete future intent or durable allegiance.
- Nightmare resolution precedes appraisal; appraisal can later recount broad social or intellectual deeds.

### INFERRED

- What a faction says in response to a bounded question is a useful concern separate from whether the statement is true, complete, persuasive, enforceable, or predictive.
- A direct answer can record exactly what was said without proving hidden motive.
- A limited answer can preserve an explicit information gap rather than filling it by inference.
- A conditional answer can preserve a stated condition without deciding whether that condition is fulfilled.
- A refusal can preserve the absence of an answer without becoming evidence of guilt, hostility, deception, or closed access.

These are implementation-oriented syntheses, not claims that the Spell exposes a canonical faction dialogue system.

### DESIGN

- The four exact answer frames `DIRECT`, `LIMITED`, `CONDITIONAL`, and `REFUSED`.
- All 16 authored primitives, titles, answer reads, faction lines, player responses, affinity tags, presentation cues, and anti-overclaim boundaries.
- Opaque preservation of scenario, faction, question, and answer identities.
- Positive evidence-tag preference with evidence magnitude deliberately discarded.
- Deterministic SHA-256 selection and generator version `nightmare-faction-current-answer-framing-v1`.
- Immutable authored affinity-tag sets.

### UNKNOWN

The checked novel evidence does **not** establish:

- a canonical faction answer taxonomy, UI, generator, frequency, or probability;
- whether any given answer is truthful, complete, deceptive, coerced, sincere, or strategically misleading;
- persuasion, leverage, trust, reputation, allegiance, hostility, friendship, intimidation, or relationship scores;
- motive, guilt, bad-faith, deception-detection, source-reliability, or confidence formulas;
- territorial legitimacy, ownership, permission, access enforcement, route safety, or escort rules;
- resource valuation, debt, obligation, contract, consent, coercion, or exchange systems;
- future faction behavior or AI probabilities;
- any mapping from an answer frame to accepted `ResolutionGraph` events, terminal resolution, appraisal, rewards, or progression.

No canonical social, faction, answer-generation, truth, relationship, access, probability, appraisal, reward, or progression formula is claimed.

### COMPATIBILITY

Java remains authority for:

- scenario and faction identity;
- the already-resolved question and answer identities;
- the exact answer frame;
- what statement was actually authorized for the encounter;
- current faction motive, relationship, trust/reputation/allegiance if such systems are later added;
- access, territory, permission, resources, obligations, world state, NPC state, and combat state;
- accepted scenario events and every `ResolutionGraph` transition;
- terminal Nightmare resolution, per-challenger outcomes, appraisal, rewards, progression, and persistence.

Dialogue, HUD, journal, NPC, map, structure, prop, audio, animation, and other adapters may render already-authorized presentation. They must not infer or mutate canonical state from answer framing, affinity tags, or presentation seed.

## Implementation limits

The catalogue cannot choose whether an answer is direct, limited, conditional, or refused. It cannot decide whether the statement is true, who has legitimate authority, whether an access condition is satisfied, whether an exchange occurred, or what relationship consequence follows. Java supplies all four opaque authority identities plus the exact answer frame; the catalogue only chooses compatible authored wording and presentation cues.
