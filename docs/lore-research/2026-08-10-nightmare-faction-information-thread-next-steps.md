# Nightmare faction information-thread next-step evidence

**Date:** 2026-08-10  
**Scope:** player-facing next-step prompts for an already-authorized Nightmare faction information thread  
**Implementation:** `NightmareFactionInformationThreadNextStepCatalog`

## Repository context checked

Before implementation, current `main`, open pull requests/issues, `PROJECT-STATUS.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and adjacent Nightmare evidence/faction work were reviewed. The slice is deliberately main-based and does not import open #171, #172, or #174. It consumes the latest thread-summary identity as opaque Java-owned context and only selects among caller-authorized next-step families.

## Primary and later chapter checks

### Chapter 14 — `Child Of Shadows`

Sunny's decisive First-Nightmare action depends on bounded asymmetric knowledge: he knows the Mountain King is blind while Hero does not, and uses that information gap rather than possessing omniscient knowledge of every actor or outcome. The Nightmare then ends before appraisal begins.

**Use here:** supports preserving bounded information gaps and follow-up actions without turning presentation into truth or terminal-resolution authority.

### Chapter 370 — `Exploration Report`

Sunny compiles geography, environmental conditions, landmarks, creature powers/behavior/weaknesses, personally gathered observations, information received from the Spell, history, and theories into a practical report. The same chapter explicitly shows that the report is incomplete because some information is omitted and because theory is kept alongside observation.

**Use here:** strongest direct support for retaining observations, sources, route information, provenance, current-vs-old context, and unresolved theory as practical records that may be revisited or compared without becoming omniscient truth.

Official WebNovel independently identifies Chapter 370 as `Exploration Report` and exposes the same core report context.

### Chapter 737 — `Self-Reflection`

Inside a later Nightmare, nominal allies can still have uncertain future intent. Sunny deliberately uses a lengthy conversation and misleading questions to create a false impression and constrain an opponent's action.

**Use here:** later clarification that social information, questioning, and uncertainty remain strategically meaningful; it also cautions against equating a recorded statement or apparent cooperation with truth, motive, or future allegiance.

### Chapter 743 — `Appraisal`

The Spell announces that the Nightmare is already over before appraisal. It then recounts broad deeds, including Sunny's battle of wits with Mordret, before giving the final appraisal.

**Use here:** keeps local information follow-up, terminal scenario resolution, and appraisal as separate concerns. Sunny's subsequent theory about fate is character interpretation and is not used as a generation or scoring formula.

Official WebNovel independently identifies Chapter 743 as `Appraisal` and confirms the resolution-before-appraisal ordering.

## Source freshness

The owner-designated NovelFull chapter listing was checked during this task rather than relying on a previously copied latest-chapter number. This implementation does not depend on any lore claim later than Chapter 743.

## Evidence classification

### CANON

- Bounded/asymmetric information can materially affect action inside a Nightmare.
- Practical reports can retain observations, received information, geography/environment, landmarks, creature knowledge, history, and theories while remaining incomplete.
- Nominal cooperation does not necessarily settle future intent, and strategic questioning/information asymmetry can matter inside a Nightmare.
- Nightmare resolution precedes appraisal; broad social/intellectual deeds may be recounted during appraisal.

### INFERRED

- Rechecking one bounded detail, seeking a named/original/independent source, comparing already-retained records, and archiving closed/stale/unresolved context are useful separable player-facing concerns around an already-authorized information thread.
- Historical context can remain useful without being treated as current truth.
- A source, record, statement, route observation, or contradiction can remain useful while truth, authenticity, causation, motive, relationship state, and downstream world consequences stay unresolved.

### DESIGN

- The exact four next-step families: `RECHECK`, `SEEK_SOURCE`, `COMPARE`, and `ARCHIVE`.
- All 16 exact authored primitives, their prose, choices, affinity tags, cues, and anti-overclaim boundaries.
- Caller-supplied allowed-family restriction.
- Opaque authority-ID preservation for scenario, faction, thread, and latest-summary IDs.
- Positive-evidence preference with evidence magnitude deliberately ignored.
- Deterministic SHA-256 selection and generator version `nightmare-faction-information-thread-next-step-v1`.
- Immutable authored and selected collections.

### UNKNOWN

- Any canonical Nightmare journal, evidence-board, investigation, or next-step UI/taxonomy.
- Any canonical algorithm for deciding when a player may recheck, seek a source, compare, or archive.
- Truth, sincerity, deception, guilt, motive, source reliability, confidence/certainty, authenticity, forgery, chain-of-custody, or causation formulas.
- Persuasion, trust, reputation, allegiance, hostility, future-behavior, ownership, territorial legitimacy, access, route safety, resource, obligation, or contract systems.
- Any formula connecting information-thread actions to accepted scenario events, terminal resolution, challenger outcome, appraisal, rewards, or progression.

No canonical generation, evidence-weighting, truth, certainty, relationship, probability, appraisal, reward, or progression formula is claimed.

### COMPATIBILITY

Java remains authority for scenario/faction/thread identity, the latest resolved thread-summary identity, which next-step families are legal, evidence/provenance, truth and source state, current relationships, access/resources/world/NPC state, accepted `ResolutionGraph` events, terminal resolution, challenger outcomes, appraisal, rewards, progression, and persistence.

This catalogue only selects authored player-facing presentation from the families supplied by Java. External HUD, journal, book, dialogue, map, NPC, audio, animation, or world adapters may render or execute removable presentation/intents, but cannot derive canonical state from the displayed text or selected primitive.

## Integration boundary

No runtime integration is required for review or merge. After the lower information-thread chain lands, a Java-owned information-action layer may validate `latestSummaryId`, determine the allowed next-step families from current state, and optionally persist the resolved primitive/seed for replay. The presentation layer still must not decide whether any follow-up succeeds or changes canonical state.
