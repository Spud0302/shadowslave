# Nightmare evidence-verification exchanges — evidence note

**Date:** 2026-08-09  
**Scope:** player-facing compare / corroborate / preserve-uncertainty / preserve-evidence / disengage exchanges around an already-resolved Nightmare evidence-link identity.

## Repository sources checked

Before implementation, rechecked current `main`, open PRs/issues, `PROJECT-STATUS.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and current Nightmare social/evidence content work including PRs #141, #142, #144 and #147. This slice is based directly on `main` and does not import those open branches. The caller supplies an opaque already-resolved evidence-link ID, so future integration can bind it to #147 after merge without making this catalogue an authority for evidence identity.

## Primary and later chapter checks

### Chapter 14 — `Child of Shadows`

Sunny's decisive First-Nightmare action depends on bounded situational knowledge that another actor does not possess. The chapter supports the general proposition that incomplete/asymmetric information can materially affect Nightmare action. It does not supply a universal evidence, interrogation, verification, certainty, or dialogue system.

### Chapter 737 — `Self-Reflection`

Later Nightmare material again presents uncertain intentions, strategic questioning, and information asymmetry as meaningful parts of conflict. Nominal alliance is not equivalent to perfect knowledge or guaranteed trust. This supports keeping verification/social presentation bounded rather than treating agreement, refusal, or apparent motive as hidden-truth authority.

### Chapter 743 — `Appraisal`

The Nightmare is already over before appraisal, and the Spell recounts broad deeds including a battle of wits before passing a verdict. This supports keeping information/social actions representable as consequential deeds while keeping scenario resolution and later appraisal distinct. Sunny's subsequent theory about what the Spell values remains character interpretation, not a project formula.

## Freshness / publication check

The owner-designated NovelFull access layer was checked again during this task and exposes the relevant chapter pages. Official WebNovel currently reports 3,131 chapters in one current catalogue snapshot, while another indexed catalogue snapshot reports 3,127/Chapter 3126. This difference is treated as volatile publication/index metadata only. No claim in this slice depends on material later than Chapter 743.

## Evidence boundary

### CANON

- Nightmare action can materially depend on incomplete or asymmetric situational knowledge.
- Interpersonal intentions and nominal alliances can remain uncertain inside a Nightmare.
- Social/intellectual conflict, including a battle of wits, can be part of the deeds later narrated in appraisal.
- Nightmare resolution precedes appraisal.

### INFERRED

- Compare, corroborate, preserve uncertainty, preserve evidence, and disengage are useful separable authoring concerns around already-resolved scenario/evidence identity.
- A player-facing verification exchange can narrow a claim or preserve a contradiction without adjudicating hidden truth.
- Evidence handling/presentation should remain distinct from the Java systems that may later persist knowledge, accept scenario events, or calculate appraisal.

### DESIGN

- The five-family taxonomy: `COMPARE`, `CORROBORATE`, `PRESERVE_UNCERTAINTY`, `PRESERVE_EVIDENCE`, `DISENGAGE`.
- All 20 exact exchange primitives, titles, prompts, player responses, tags, presentation cues, and anti-overclaim boundaries.
- Opaque caller-supplied `evidenceLinkId` so the catalogue remains main-based while #147 is open.
- Positive-evidence compatibility preference with magnitude collapsed to presence/absence.
- SHA-256 deterministic selection and generator version `nightmare-evidence-verification-exchange-v1`.

### UNKNOWN

- Any canonical evidence-verification/interrogation/dialogue-generation algorithm or taxonomy.
- Truthfulness, lie detection, forgery/authenticity determination, culprit/guilt detection, coercion, consent, persuasion, trust, reputation, allegiance, or betrayal mechanics.
- Confidence percentages, certainty progression thresholds, evidence weights, probabilities, or any evidence-to-resolution/appraisal/reward formula.
- Whether repeated testimony or physical corroboration has any universal canonical evidentiary value.
- Any universal rule for preserving samples, records, chain-of-custody, or investigation UI inside the Spell.

### COMPATIBILITY

Java remains authority for scenario identity, actor/role identity, evidence-link identity, accepted `ResolutionGraph` events, terminal resolution, appraisal inputs, progression, and any future persistent knowledge/evidence state. Dialogue, HUD, books, signs, NPC AI, models, sound, particles, and world/object adapters may render or execute already-authorized presentation. They must not treat a displayed verification exchange as proof of truth, authenticity, guilt, allegiance, persuasion success, certainty progression, scenario completion, appraisal, or reward.

## Deliberate non-claims

This catalogue does **not** claim a canonical generation formula, truth model, confidence score, persuasion formula, evidence weighting system, or universal investigation mechanic. Evidence magnitude is deliberately ignored so a convenient content selector cannot silently become one.
