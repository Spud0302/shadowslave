# Nightmare faction answer follow-up evidence — 2026-08-10

## Scope

This note supports `NightmareFactionAnswerFollowupCatalog`, a player-facing presentation catalogue for an **already-authorized faction answer**. The slice supplies bounded `RECORD`, `COMPARE`, `VERIFY`, and `DEFER` follow-up presentation. It does not generate the answer, decide truth, mutate faction relationships or world state, accept scenario events, resolve a Nightmare, or appraise the challenger.

Research followed `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, `PROJECT-STATUS.md`, current `main`, open PRs/issues, and adjacent social/evidence work before implementation. In particular, open PR #147 already owns generic Nightmare evidence-link primitives, while #169 owns current faction answer framing; this slice is narrower and consumes an opaque already-authorized `answerId` instead of duplicating either catalogue.

## Primary and later chapter checks

### Chapter 14 — `Child of Shadows`

Sunny's First Nightmare turns on bounded asymmetric situational knowledge: he knows a decisive fact about the Mountain King that Hero does not and acts on that difference. The chapter ends the Nightmare before the Spell begins appraisal.

**Use here:** information available to a challenger can be incomplete yet materially actionable. Presentation should preserve what is known, stated, or missing rather than manufacture omniscient truth.

### Chapter 370 — `Exploration Report`

Sunny compiles geography, environment, landmarks, creature knowledge, information heard from others, direct Spell information, and his own experience into a practical report. The report is explicitly written from the information available to him and for practical usefulness.

**Use here:** bounded observations and received information can be recorded and compared as useful knowledge without turning the record into universal world-state authority.

Official WebNovel independently exposes Chapter 370 under the same title and content context.

### Chapter 737 — `Self-Reflection`

During the Second Nightmare, Sunny treats a nominal ally's future intent as uncertain and deliberately uses conversation, misleading questions, and information asymmetry as part of a consequential plan.

**Use here:** a statement or conversation can matter without settling sincerity, hidden motive, future allegiance, or truth. Follow-up presentation therefore keeps those concerns separate.

### Chapter 743 — `Appraisal`

The Spell states that the Nightmare is already over before appraisal begins and then narrates a broad range of deeds.

**Use here:** recording, comparing, or verifying a faction answer must not become terminal-resolution or appraisal authority.

## Freshness check

At research time, the owner-designated NovelFull listing exposes Chapter **3116 — `Princess of the Underworld`** as its latest listed chapter. Official WebNovel snapshots are currently not perfectly synchronized: the title page reports **3,131 chapters**, while an indexed catalogue snapshot reports **3,127** and another title-page snapshot lists Chapter **3130 — `Bleak Days`** as the latest release. This discrepancy is publication/index metadata, not lore evidence. No implementation claim in this slice depends on material later than Chapter 743.

## Evidence boundary

### CANON

- Nightmare action can materially depend on incomplete or asymmetric situational information.
- Received information, direct observations, geography/environment, landmarks, and creature knowledge can be retained as useful practical records.
- Nominal cooperation does not guarantee settled future intent; strategic conversation and information asymmetry can materially affect a Nightmare conflict.
- Nightmare resolution precedes appraisal.

### INFERRED

- An already-authorized faction answer can be usefully retained separately from whether the statement is true, sincere, complete, enforceable, or predictive.
- `RECORD`, `COMPARE`, `VERIFY`, and `DEFER` are useful bounded follow-up concerns when Java supplies the answer and exact legal follow-up family.
- Comparing a statement with another account, a physical record, or current observation can expose disagreement without adjudicating hidden truth.

### DESIGN

- The four-family taxonomy `RECORD / COMPARE / VERIFY / DEFER`.
- All 16 exact follow-up primitives, titles, reads, prompts, actions, affinity tags, presentation cues, and anti-overclaim boundaries.
- Opaque scenario/faction/answer/follow-up authority IDs.
- Positive-evidence preference with magnitude independence.
- Immutable authored collections.
- Deterministic SHA-256 presentation selection and generator version `nightmare-faction-answer-followup-v1`.

### UNKNOWN

- Any canonical Nightmare dialogue follow-up, evidence-board, journal, or verification UI/taxonomy.
- Truth, sincerity, deception, guilt, hidden motive, source-reliability, confidence, certainty, or probability formulas.
- Persuasion, intimidation, leverage, trust, reputation, allegiance, hostility, relationship, or future-behavior formulas.
- Territorial legitimacy, ownership, access enforcement, route safety, resource exchange, contract, or obligation formulas.
- Any answer/follow-up-to-`ResolutionGraph`, terminal-resolution, appraisal, reward, progression, or fate formula.

No canonical generation, dialogue, truth, evidence weighting, relationship, access, probability, appraisal, reward, or progression formula is claimed.

### COMPATIBILITY

Java remains canonical authority for scenario identity, faction identity, already-authorized answer identity/content, legal follow-up identity/family, source/evidence identity and provenance, current world/access/resource state, NPC/faction state, relationships/motive, accepted `ResolutionGraph` events, terminal resolution, challenger outcomes, appraisal, rewards, progression, and persistence.

External dialogue, HUD, journal, book, map, NPC, world, audio, or animation mods may render or execute removable presentation around an already-authorized follow-up. They must not promote the selected primitive into canonical state.

## Validation intent

Focused tests require:

- exactly 16 unique primitives and four per family;
- three player actions and two presentation cues per primitive;
- immutable authored tags/actions/cues and selected matched-evidence tags;
- no backend terminology in player-facing fields;
- preservation of mixed-case/namespaced opaque scenario/faction/answer/follow-up IDs across 4,096 seeds per family;
- evidence-map-order and positive-evidence-magnitude independence;
- compatible evidence preference without authority mutation;
- neutral 16,384-seed reachability of all primitives and all primitive/cue pairs;
- fail-closed blank authority IDs, null family, negative evidence, and unknown catalogue IDs;
- explicit anti-overclaim coverage for truth, motive, access, ownership, route safety, trust, allegiance, reputation, future behavior, and scenario authority.

## Integration limitation

This content is independently reviewable from current `main`. It does not import open #147 or #169. After the lower social/evidence stack lands, a Java-owned interaction/evidence composer may validate the opaque `answerId` and `followupId`, constrain legal follow-up families, and optionally persist the selected primitive/seed for exact replay.
