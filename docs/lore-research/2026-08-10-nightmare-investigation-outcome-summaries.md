# Nightmare investigation outcome summaries — lore evidence

**Date:** 2026-08-10  
**Scope:** player-facing summaries for already-resolved investigation outcomes; no terminal Nightmare resolution, appraisal, reward, truth adjudication, or persistence mechanics.

## Research workflow

Read before implementation:

- `docs/LORE-SOURCE-POLICY.md`;
- `docs/NIGHTMARE-SEED-ROADMAP.md`;
- current `main` and open issues/PRs;
- current investigation/evidence content PRs, especially #147, #149, #150, #151 and #154;
- current correctness stack only to avoid coupling to it.

The owner-designated NovelFull access layer currently lists through **Chapter 3116 — Princess of the Underworld**. Official WebNovel currently reports **3,131 chapters**. NovelFull is therefore used only as the repository-designated chapter-reading access layer, not publication authority. No claim in this slice depends on material later than Chapter 743.

## Primary and later chapter checks

### Chapter 14 — `Child of Shadows`

Sunny's decisive First-Nightmare action depends on bounded information that he possesses and another actor does not, and on exploiting a specific situational fact rather than omniscient knowledge. The Nightmare is declared over before appraisal begins.

Relevant constraint: an investigation summary may distinguish what was actually established from what remains unknown, but presentation cannot manufacture hidden truth or appraisal authority.

### Chapter 370 — `Exploration Report`

Sunny compiles a practical report from geography, environment, landmarks, creature powers/behavior/weaknesses, received information, and his own experience. The report is useful precisely as recorded field knowledge rather than omniscient world state.

Relevant constraint: a bounded outcome can preserve established observations, provenance, route facts, gaps, and limitations for later use without claiming universal certainty.

### Chapter 743 — `Appraisal`

The Nightmare is already over before appraisal begins. The Spell then recounts deeds before producing its appraisal.

Relevant constraint: a local investigation outcome, even one marked complete or found, must remain separate from terminal Nightmare resolution and appraisal.

## Evidence classification

### CANON

- Nightmare decisions can materially depend on incomplete/asymmetric situational knowledge.
- Useful field knowledge can be recorded from observations, received information, geography/environment, landmarks, and creature knowledge without becoming omniscient world state.
- Nightmare resolution precedes appraisal.

### INFERRED

- `FOUND`, `LEFT_UNRESOLVED`, `PRESERVED`, and `ABANDONED` are useful separable presentation states for already-resolved investigation outcomes when Java supplies the authoritative state.
- A player-facing summary can state what was found, what remained unresolved, what was preserved, or what was abandoned while keeping provenance, scope, and limitations visible.
- Preserving disagreement or abandonment reasons can be useful gameplay information without deciding truth, guilt, morality, or scenario success.

### DESIGN

- The exact four-state taxonomy.
- All 16 authored summary primitives, wording, reflection options, affinity tags, presentation cues, and anti-overclaim boundaries.
- Positive-evidence preference with evidence magnitude deliberately ignored.
- SHA-256 deterministic selection and generator version `nightmare-investigation-outcome-summary-v1`.
- Opaque caller-owned `scenarioId`, `actorContextId`, `investigationId`, and `outcomeId` identities preserved verbatim rather than normalized as catalogue IDs.

### UNKNOWN

- Any canonical investigation journal, evidence-board, objective, or outcome-summary UI.
- Any canonical truth, lie, forgery, guilt, confidence, certainty, source-reliability, chain-of-custody, or route-safety system.
- Whether the Spell exposes bounded investigation outcomes in any comparable taxonomy.
- Any formula connecting investigation findings, unresolved questions, preserved evidence, abandonment, fate divergence, terminal Nightmare resolution, appraisal, rewards, or progression.
- Any universal moral value assigned to abandoning one line of inquiry for another obligation.

### COMPATIBILITY

- Java remains authority for scenario identity, actor/role identity, investigation identity, outcome identity/state, accepted `ResolutionGraph` events, world mutation, challenger outcome, terminal Nightmare resolution, appraisal inputs, rewards, progression, and future persistent investigation state.
- HUD, book, map, dialogue, NPC, sound, particle, and other external adapters may render an already-resolved summary but cannot select or mutate the outcome state.
- `FOUND` means only that the caller has supplied a bounded found outcome; it does not mean the entire investigation or Nightmare is complete.
- `PRESERVED` confirms only a Java-owned preservation outcome; it does not certify authenticity or truth.
- `LEFT_UNRESOLVED` preserves uncertainty rather than resolving it by presentation.
- `ABANDONED` records a resolved stopping outcome without assigning morality, guilt, appraisal, or scenario success.

No canonical generation, investigation, truth, certainty, route, morality, appraisal, probability, reward, or progression formula is claimed.
