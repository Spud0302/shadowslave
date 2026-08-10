# Nightmare faction answer follow-up outcome evidence — 2026-08-10

## Scope

This note supports `NightmareFactionAnswerFollowupOutcomeCatalog`, a player-facing presentation catalogue for an **already-resolved faction answer follow-up**. The slice supplies bounded `RECORDED`, `COMPARED`, `CHECKED`, and `LEFT_OPEN` outcome summaries. It does not perform the follow-up, decide truth, mutate faction relationships or world state, accept scenario events, resolve a Nightmare, or appraise the challenger.

Research followed `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, `PROJECT-STATUS.md`, current `main`, open PRs/issues, and adjacent current content before implementation. Open PR #171 owns the follow-up actions themselves; this slice is independently reviewable from `main` and consumes opaque already-resolved `answerId`, `followupId`, and `outcomeId` values instead of importing #171.

## Primary and later chapter checks

### Chapter 14 — `Child of Shadows`

Sunny's First Nightmare turns on bounded asymmetric situational knowledge: he knows a decisive fact about the Mountain King that Hero does not and acts on that difference. The Nightmare ends before the Spell begins appraisal.

**Use here:** consequential information can remain bounded to what a participant actually knows. An outcome summary should not manufacture omniscient truth from a statement or check.

### Chapter 370 — `Exploration Report`

Sunny compiles geography, environment, landmarks, Nightmare Creature knowledge, information received from others, direct Spell information, and his own experience into a practical report. The report is explicitly assembled from the information available to him for practical usefulness.

**Use here:** useful records can preserve sourced observations and comparisons without becoming universal world-state authority. Official WebNovel independently exposes Chapter 370 under the same title and context.

### Chapter 737 — `Self-Reflection`

During the Second Nightmare, Sunny treats a nominal ally's future intent as uncertain while strategic conversation and information asymmetry remain consequential.

**Use here:** even a completed conversation or bounded check need not settle sincerity, hidden motive, future allegiance, or relationship state.

### Chapter 743 — `Appraisal`

The Spell states that the Nightmare is already over before appraisal and then recounts a broad range of deeds.

**Use here:** recording, comparison, checking, or leaving an information question open must not become terminal-resolution or appraisal authority. Sunny's later interpretation of why one appraisal differs from another is not used as a canonical formula.

## Evidence boundary

### CANON

- Nightmare action can materially depend on incomplete or asymmetric situational information.
- Received information and direct observations can be retained as useful practical records.
- Nominal cooperation does not guarantee settled future intent; strategic information conflict can remain consequential.
- Nightmare resolution precedes appraisal.

### INFERRED

- The result of an already-authorized information follow-up can be retained separately from truth, sincerity, relationship state, and world-state consequences.
- A bounded outcome can say that a statement was recorded, accounts were compared, a supplied check was performed, or a question remained open without adjudicating matters outside that result.
- Comparison results can expose agreement, disagreement, or missing information without deciding why those differences exist.

### DESIGN

- The four-kind taxonomy `RECORDED / COMPARED / CHECKED / LEFT_OPEN`.
- All 16 exact outcome primitives, titles, reads, prompts, responses, affinity tags, presentation cues, and anti-overclaim boundaries.
- Opaque scenario/faction/answer/follow-up/outcome authority IDs.
- Positive-evidence preference with magnitude independence.
- Immutable authored collections.
- Deterministic SHA-256 presentation selection and generator version `nightmare-faction-answer-followup-outcome-v1`.

### UNKNOWN

- Any canonical Nightmare dialogue outcome, evidence-board, journal, verification-result UI, or taxonomy.
- Truth, sincerity, deception, guilt, hidden motive, source reliability, confidence, certainty, or probability formulas.
- Persuasion, intimidation, leverage, trust, reputation, allegiance, hostility, relationship, or future-behavior formulas.
- Territorial legitimacy, ownership, access enforcement, route safety, resource exchange, contract, or obligation formulas.
- Any follow-up outcome to `ResolutionGraph`, terminal-resolution, appraisal, reward, progression, or fate formula.

No canonical generation, dialogue, truth, evidence-weighting, relationship, access, probability, appraisal, reward, or progression formula is claimed.

### COMPATIBILITY

Java remains canonical authority for scenario identity, faction identity, already-authorized answer identity/content, follow-up identity/action, resolved outcome identity/kind, source/evidence identity and provenance, current world/access/resource state, NPC/faction state, relationships/motive, accepted `ResolutionGraph` events, terminal resolution, challenger outcomes, appraisal, rewards, progression, and persistence.

External dialogue, HUD, journal, book, map, NPC, world, audio, or animation mods may render or execute removable presentation around an already-resolved outcome. They must not promote the selected primitive into canonical state.

## Validation intent

Focused tests require:

- exactly 16 unique primitives and four per outcome kind;
- three player responses and two presentation cues per primitive;
- immutable authored tags/responses/cues and selected matched-evidence tags;
- no backend terminology in player-facing fields;
- preservation of mixed-case/namespaced opaque scenario/faction/answer/follow-up/outcome IDs across 4,096 seeds per kind;
- evidence-map-order and positive-evidence-magnitude independence;
- compatible evidence preference without authority mutation;
- neutral 16,384-seed reachability of all primitives and all primitive/cue pairs;
- fail-closed blank authority IDs, null kind, negative evidence, and unknown catalogue IDs;
- explicit anti-overclaim coverage for truth, motive, access, ownership, route safety, trust, allegiance, reputation, future behavior, and scenario authority.

## Integration limitation

This content is independently reviewable from current `main` and does not import open #171. After the lower answer/follow-up stack lands, a Java-owned interaction/evidence composer may validate the opaque `answerId`, `followupId`, and `outcomeId`, supply the exact outcome kind, and optionally persist the selected primitive/seed for exact replay.