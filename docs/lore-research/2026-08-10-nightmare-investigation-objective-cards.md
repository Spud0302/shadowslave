# Nightmare investigation objective-card evidence — 2026-08-10

## Scope

This note supports a bounded player-facing objective-card presentation catalogue for already-resolved Nightmare investigation/planning state. It does not define canonical investigation mechanics, objective completion rules, truth/certainty systems, or appraisal scoring.

Repository state checked before implementation:

- current `main` at `e9f676a9b2ef58cf1e0cc18d45d1420b9e62c2f4`;
- all open PRs and issues, including the separate correctness stack and the unmerged Nightmare content chain through #151;
- `PROJECT-STATUS.md` (noting that its root status text is stale relative to current PR activity);
- `docs/LORE-SOURCE-POLICY.md`;
- `docs/NIGHTMARE-SEED-ROADMAP.md`;
- active evidence/journal/planning work #147, #149, #150 and #151.

This slice is based directly on `main`. It consumes `planId` as an opaque caller-supplied Java identity and therefore does not import or duplicate #151.

## Primary/later chapter checks

### Chapter 14 — *Child of Shadows*

Sunny's decisive First-Nightmare actions depend on bounded asymmetric situational knowledge: he knows the Mountain King is blind while Hero does not, and uses that informational difference rather than possessing omniscient state. The chapter also ends the Nightmare before appraisal begins.

Relevant constraint: player-facing investigation guidance may expose a bounded next check or retained uncertainty, but presentation should not become truth authority or a universal completion trigger.

### Chapter 370 — *Exploration Report*

Sunny records geography, environment, landmarks, creature powers/behavior/weaknesses, information received from other sources, personal observations and theories into a practical report. He also deliberately omits information. The report is useful precisely because structured knowledge can guide later action without being exhaustive or omniscient.

Relevant constraint: objective presentation may summarize already-owned observations, gaps, route checks and preservation work while keeping provenance, omissions and uncertainty explicit.

### Chapter 743 — *Appraisal*

The Nightmare is already over before appraisal. The Spell recounts a broad range of deeds, including intellectual conflict, before giving its verdict. Sunny then speculates about fate divergence; that speculation is not promoted here into a canonical formula.

Relevant constraint: a completed investigation objective must not imply Nightmare completion, appraisal result, reward, or progression.

## Freshness check

At research time, the owner-designated NovelFull access layer lists through Chapter 3116, while official WebNovel reports 3,131 chapters. NovelFull is therefore treated only as the repository-designated reading-access layer, not as current-publication authority. No claim in this slice depends on material later than Chapter 743.

## Evidence classification

### CANON

- Nightmare action can materially depend on incomplete/asymmetric situational knowledge rather than omniscience.
- Useful geography, environment, landmarks, creature knowledge, observations, received information and theories can be organized into practical field knowledge.
- Such reporting can be incomplete or intentionally omit information.
- Nightmare resolution precedes appraisal; appraisal can narrate broad deeds after the trial is already over.

### INFERRED

- `ACTIVE`, `DEFERRED`, `BLOCKED` and `COMPLETED` are useful player-facing presentation states for investigation objectives when the authoritative state is supplied by Java rather than inferred by UI text.
- An objective card can expose a next bounded check, a resume condition, a current blocker, or a completed local action while preserving uncertainty and provenance.
- A completed local investigation step can remain inspectable without implying the central Nightmare conflict has ended.

### DESIGN

- The exact four-state objective-card taxonomy.
- All 16 authored card primitives, four per state.
- Every title, status read, prompt, player option, affinity tag, presentation cue and anti-overclaim boundary.
- Opaque caller-supplied `scenarioId`, `actorContextId`, `planId` and `objectiveId`.
- Positive-evidence preference with evidence magnitude deliberately ignored.
- SHA-256 deterministic selection and generator version `nightmare-investigation-objective-card-v1`.

### UNKNOWN

- Any canonical Nightmare objective/journal/task UI or state taxonomy.
- Canonical rules for when an investigation objective becomes active, deferred, blocked or completed.
- Hidden-prerequisite discovery, blocker duration, route safety, truth/lie/forgery/guilt detection, certainty/confidence thresholds, source reliability, persuasion/trust/reputation/allegiance, environmental forecasts, and automatic sharing.
- Any formula mapping investigation objectives, evidence, planning, completion state or presentation to `ResolutionGraph` acceptance, Nightmare completion, appraisal, rewards or progression.

### COMPATIBILITY

- Java remains authority for scenario identity, actor/role context, plan identity, objective identity, objective state, accepted events, world mutation, terminal resolution, appraisal inputs, progression and any future persistent investigation state.
- The catalogue can only render an already-resolved objective state. Seed/evidence cannot change `ACTIVE` to `COMPLETED`, remove a blocker, create access, certify truth, or accept a scenario event.
- HUD, book, map, dialogue, NPC, sound, particle and world adapters may present already-authorized cards but cannot own or infer canonical state from the displayed text.

## Validation intent

`NightmareInvestigationObjectiveCardCatalogTest` checks:

- exactly 16 unique primitives and exactly four per Java-owned state;
- exactly three player options and two presentation cues per primitive;
- substantive reads/prompts/boundaries;
- a 4,096-seed sweep preserving caller-supplied scenario, actor, plan, objective and state authority;
- neutral 16,384-seed reachability of all 16 primitives and all 32 primitive/cue pairs;
- evidence-map-order and evidence-magnitude independence;
- positive evidence may prefer a compatible card without changing caller-owned state;
- anti-overclaim coverage for route guarantees, world-freezing deferral, `ResolutionGraph` authority, scenario completion and appraisal;
- malformed IDs, null state, negative evidence and unknown card IDs fail closed.

No local Gradle/JUnit/client/server execution is claimed from the connector-only authoring environment. Hosted CI is evidence only when GitHub registers a workflow/status for the exact PR head.
