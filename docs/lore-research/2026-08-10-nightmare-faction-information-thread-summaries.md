# Nightmare faction information-thread summaries

**Date:** 2026-08-10  
**Status:** bounded content research note for `NightmareFactionInformationThreadSummaryCatalog`  
**Generator:** `nightmare-faction-information-thread-summary-v1`

## Scope

This slice adds player-facing journal summaries for an **already-resolved Java-owned faction information thread state**. It is based directly on current `main` and deliberately does not import open PR #172 or the lower answer/follow-up chain. The caller supplies opaque `scenarioId`, `factionId`, `threadId`, `latestOutcomeId`, and exact `ThreadState`; presentation cannot create or transition those facts.

The authored states are `ACTIVE`, `CONTRADICTED`, `STALE`, and `CLOSED`. They are project presentation vocabulary, not a claimed canonical Shadow Slave information system.

## Primary and later chapter evidence

Research followed `docs/LORE-SOURCE-POLICY.md` and rechecked the designated full-chapter access layer before implementation.

- **Chapter 14 — Child Of Shadows:** Sunny's decisive First-Nightmare action depends on bounded asymmetric information: he knows a critical fact Hero does not, uses that information tactically, and the Nightmare ends before appraisal begins. This supports information being consequential without implying omniscient truth state.
- **Chapter 370 — Exploration Report:** Sunny compiles geography, environment, landmarks, Nightmare Creature knowledge, personal observations, information received from the Spell, and theories into a practical report. The chapter also preserves limits and omissions rather than making the report perfect world truth. This is the strongest direct support for retaining bounded records and distinguishing observations/information/theory.
- **Chapter 737 — Self-Reflection:** nominal allies can still have uncertain future intent. Sunny deliberately uses conversation and misleading questions to create a false impression and gain tactical advantage. This later material reinforces that statements, agreement, and current cooperation do not settle truth or durable relationship state.
- **Chapter 743 — Appraisal:** the Nightmare is already over before appraisal. The Spell recounts broad deeds, including a battle of wits, and only afterward supplies a final appraisal. Sunny's subsequent theory about fate divergence is character interpretation and is not used here as a generation or scoring formula.

Freshness check: the owner-designated NovelFull listing currently exposes through Chapter 3116. That host is used as the repository's reading-access layer, not as canonical publication authority. No implementation claim in this slice depends on material later than Chapter 743.

## Evidence boundary

### CANON

- Bounded/asymmetric information can materially affect Nightmare action.
- Practical records can combine geography, observations, received information, creature knowledge, and theory while remaining incomplete.
- Nominal cooperation does not necessarily settle another actor's future intent.
- Strategic conversation and misleading questions can materially affect a Nightmare conflict.
- Nightmare resolution precedes appraisal; broad intellectual/social deeds may be recounted during appraisal.

### INFERRED

- A bounded information thread can usefully preserve sourced claims, contradictions, stale context, and unresolved gaps separately from truth, motive, relationship state, and world state.
- `ACTIVE`, `CONTRADICTED`, `STALE`, and `CLOSED` are useful presentation concerns when the exact state is supplied by Java authority.
- Closing an information process can preserve unresolved facts rather than implying omniscience, and stale records can remain useful as historical comparison without being treated as current truth.

### DESIGN

- The exact four-state taxonomy and all 16 authored primitives.
- Titles, journal reads, next questions, three player actions, affinity tags, two presentation cues, and anti-overclaim boundaries.
- Opaque authority-ID handling and preservation of mixed-case/namespaced caller identities.
- Positive-evidence tag preference with evidence magnitude deliberately ignored.
- Immutable authored/selected collections.
- Deterministic SHA-256 selection and generator version `nightmare-faction-information-thread-summary-v1`.

### UNKNOWN

- Any canonical Nightmare journal, information-thread, quest-log, evidence-board, confidence, or certainty UI/taxonomy.
- Truth, sincerity, deception, guilt, motive, source reliability, confidence percentages, evidence weights, or contradiction-resolution formulas.
- Persuasion, trust, reputation, allegiance, hostility, relationship-history scoring, or future-behavior prediction.
- Territorial legitimacy, ownership, route safety, access enforcement, resource/contract consequences, or world mutation implied by a journal state.
- Any formula connecting information-thread state to accepted scenario events, terminal resolution, challenger outcome, appraisal, rewards, or progression.

No canonical generation, evidence-weighting, truth, confidence, relationship, probability, appraisal, reward, or progression formula is claimed.

### COMPATIBILITY

Java remains authority for scenario/faction/thread/outcome identity and exact thread state; provenance/evidence facts; relationship, access, resource, NPC, faction, and world state; accepted `ResolutionGraph` events; terminal resolution; challenger outcomes; appraisal; rewards; progression; and persistence.

External HUD, book, journal, map, dialogue, NPC, audio, animation, or other mod adapters may render the already-authorized selection but must not derive or mutate canonical state from its text.

## Content shape

Wave one contains exactly four primitives per state:

- `ACTIVE`: **Open Question in Play**, **Source Still Worth Checking**, **Condition Still Current**, **Several Leads, One Thread**.
- `CONTRADICTED`: **Accounts Conflict**, **Record and Claim Diverge**, **Condition and Observation Diverge**, **Authority Claims Conflict**.
- `STALE`: **Route Information Is Old**, **Statement Needs Renewal**, **Conditions Have Changed**, **Context No Longer Sufficient**.
- `CLOSED`: **Scoped Question Closed**, **No Further Check Authorized**, **Closed With Uncertainty Retained**, **Thread Superseded**.

The states are deliberately weaker than truth and world authority. `CONTRADICTED` preserves mismatches without proving lying; `STALE` means context is no longer current enough for the same use, not that it is false; `CLOSED` means the tracked information process has ended, not that every related fact is known or that the Nightmare resolved.

## Validation intent and limitations

Focused unit coverage validates exact catalogue distribution, bounded player copy, immutable collections, opaque authority preservation, evidence map-order/magnitude independence, compatible evidence preference, deterministic reachability, malformed-input rejection, and anti-overclaim coverage.

This connector-only content run does not claim local Gradle/JUnit, Minecraft client, or dedicated-server execution. Hosted validation is claimed only when GitHub registers workflow/status evidence for the exact branch head.
