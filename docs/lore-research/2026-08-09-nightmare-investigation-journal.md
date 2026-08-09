# Nightmare investigation journal — evidence note

**Date:** 2026-08-09  
**Scope:** player-facing journal composition around already-resolved Nightmare evidence-link and verification-exchange identities.

## Repository sources checked

Before implementation, rechecked current `main`, all open PRs/issues, `PROJECT-STATUS.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, merged Nightmare content, and current social/evidence work including PRs #141, #142, #144, #147 and #149. This slice is based directly on `main` and does not import those open branches. The caller supplies opaque already-resolved evidence-link and verification-exchange IDs so future Java-owned integration can bind them after those branches merge without making this catalogue evidence authority.

## Primary and later chapter checks

### Chapter 14 — `Child of Shadows`

Sunny's decisive First-Nightmare action depends on bounded situational knowledge that another actor does not possess. The scene supports the proposition that incomplete/asymmetric information can materially affect Nightmare action. It does not define a universal journal, certainty, evidence, truth, interrogation, or investigation mechanic.

### Chapter 370 — `Exploration Report`

Sunny deliberately records geography, environment, landmarks, creature powers/behavior/weaknesses, received information, and personal experience in a report intended to help a less-informed explorer survive. This supports recording bounded observations and separating useful field knowledge from omniscient world state. The chapter does not establish a Spell-managed investigation journal or a universal confidence system.

### Chapter 737 — `Self-Reflection`

Later Nightmare material again preserves uncertain intentions and information asymmetry even among nominal allies. This supports keeping testimony, refusal, contradictions, and incomplete knowledge explicit rather than allowing presentation to infer hidden truth or allegiance.

### Chapter 743 — `Appraisal`

The Nightmare is already over before appraisal, and the Spell recounts broad deeds including a battle of wits before passing a verdict. This supports allowing information handling and investigation to remain consequential player-facing content while keeping terminal resolution and later appraisal separate. Sunny's subsequent theory about what the Spell values remains character interpretation rather than a project formula.

## Freshness / publication check

The owner-designated NovelFull access layer was checked during this task and exposes the relevant chapter pages. Official WebNovel currently reports volatile catalogue snapshots around Chapter 3126–3131. No claim in this slice depends on material later than Chapter 743.

## Evidence boundary

### CANON

- Nightmare action can materially depend on incomplete or asymmetric situational knowledge.
- Dream Realm observations, geography, environment, landmarks, creature behavior/weaknesses, and received information can be recorded into a useful report.
- Interpersonal intentions can remain uncertain inside a Nightmare even where nominal alliance exists.
- Social/intellectual conflict can matter among the deeds later narrated by appraisal.
- Nightmare resolution precedes appraisal.

### INFERRED

- Observed, contradicted, unresolved, and preserved are useful separable journal states for presenting already-resolved scenario-local evidence without adjudicating hidden truth.
- A journal can preserve an original observation, contradiction, unanswered question, or source limitation without automatically changing certainty or scenario state.
- Evidence-link identity, verification-exchange identity, journal presentation, accepted scenario events, and appraisal are useful separate Java concerns.

### DESIGN

- The four-state taxonomy: `OBSERVED`, `CONTRADICTED`, `UNRESOLVED`, `PRESERVED`.
- All 20 exact journal primitives, titles, reads, questions, player actions, affinity tags, presentation cues, and anti-overclaim boundaries.
- Opaque caller-supplied `scenarioId`, `actorContextId`, `evidenceLinkId`, and `verificationExchangeId`.
- Positive-evidence compatibility preference with magnitude collapsed to presence/absence.
- SHA-256 deterministic selection and generator version `nightmare-investigation-journal-v1`.

### UNKNOWN

- Any canonical Nightmare investigation-journal, quest-log, evidence-board, or note-taking system.
- Any canonical certainty/confidence scale, threshold, percentage, probability, weighting, truth model, lie/forgery/guilt detector, or source-reliability formula.
- Whether contradictions or corroboration have universal canonical evidentiary value.
- Persuasion, trust, reputation, allegiance, betrayal, testimony-memory, chain-of-custody, or ownership rules.
- Any evidence-to-`ResolutionGraph` event, terminal resolution, appraisal, reward, or progression formula.
- Any universal rule for how observations persist, update, spread between challengers, or become shared knowledge.

### COMPATIBILITY

Java remains authority for scenario identity, actor/role identity, evidence-link identity, verification-exchange identity, accepted `ResolutionGraph` events, terminal resolution, appraisal inputs, progression, and any future persistent knowledge/evidence state. Dialogue, books, maps, HUD, signs, NPC AI, models, audio, particles, and world/object adapters may render already-authorized journal content but must not convert presentation into truth, authenticity, guilt, allegiance, persuasion success, certainty progression, scenario completion, appraisal, rewards, or persistence authority.

## Deliberate non-claims

This catalogue does **not** claim a canonical generation formula, truth model, confidence score, investigation algorithm, evidence weighting system, or Spell journal UI. Evidence magnitude is deliberately ignored so a convenient content selector cannot silently become a certainty, difficulty, or appraisal formula.
