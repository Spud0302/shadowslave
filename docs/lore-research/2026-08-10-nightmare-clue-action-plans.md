# Nightmare clue-to-action planning primitives — evidence note

**Date:** 2026-08-10  
**Status:** bounded player-facing DESIGN slice  
**Branch:** `gpt/nightmare-clue-action-plans-wave1`

## Repository checks

Before implementation, re-read current `main`, open PRs/issues, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and the active Nightmare social/evidence/journal branches. This slice is based directly on `main` and deliberately does not import open PR #150 or its lower evidence stack. The caller supplies an opaque already-resolved journal-entry identity.

## Primary/later chapter checks

- **Chapter 14 — Child of Shadows:** Sunny's decisive First-Nightmare action depends on bounded asymmetric knowledge; he knows a relevant creature limitation that another actor does not, and acts on that difference rather than on omniscient information.
- **Chapter 370 — Exploration Report:** accumulated observations, geography/environment, landmarks, creature information, received information, and experience can be organized into practical field knowledge. This supports follow-up investigation/planning as a useful player-facing concern without implying perfect knowledge.
- **Chapter 743 — Appraisal:** appraisal occurs after the Nightmare is already over and recounts broad deeds, including intellectual/social conflict, before the verdict. Planning content must therefore not become terminal-resolution or appraisal authority.

Freshness check: the owner-designated NovelFull access layer remains a reading-access layer rather than publication authority. Official WebNovel currently reports 3,131 chapters; no claim in this slice depends on material later than Chapter 743.

## Evidence classification

### CANON

- Nightmare action can materially depend on incomplete/asymmetric situational knowledge.
- Useful observations and received field information can be recorded and acted on without becoming omniscient world state.
- Appraisal follows an already-ended Nightmare and may narrate broad non-combat/intellectual deeds.

### INFERRED

- Revisit, compare, seek-source, route-test, evidence-protection, and deferral are useful separable player-facing follow-up concerns around already-recorded information.
- A planning layer can convert a bounded clue into a next-question/next-action prompt without deciding whether that action succeeds or whether the clue is true.

### DESIGN

- The six exact planning families: `REVISIT`, `COMPARE`, `SEEK_SOURCE`, `TEST_ROUTE`, `PROTECT_EVIDENCE`, `DEFER`.
- All 24 exact primitives, titles, situation reads, action prompts, player options, affinity tags, cues, and anti-overclaim boundaries.
- Opaque `scenarioId`, `actorContextId`, and `journalEntryId` inputs supplied by Java-owned authoritative state.
- Positive-evidence preference with magnitude deliberately ignored.
- SHA-256 deterministic compatible selection and generator version `nightmare-clue-action-plan-v1`.

### UNKNOWN

- Any canonical investigation/planning taxonomy or generation formula.
- Whether the Spell exposes a journal, evidence board, clue tracker, route planner, or equivalent UI.
- Truth, lie, forgery, guilt, confidence, certainty, source-reliability, persuasion, trust, reputation, allegiance, or probability systems.
- Success/failure rules for revisiting, comparison, witnesses, route tests, evidence preservation, waiting, or environmental change.
- Any mapping from a clue or planned action to accepted `ResolutionGraph` events, Nightmare completion, appraisal, rewards, or progression.

### COMPATIBILITY

Java remains authority for scenario identity, actor/role identity, journal/evidence identity, accepted events, world mutations, terminal resolution, appraisal inputs, progression, and future persistent knowledge/planning state. HUD, books, maps, dialogue, NPC AI, props, particles, sound, and other external adapters may render or execute already-authorized intents but cannot infer canonical success or mutate authoritative state from the displayed plan.

No canonical generation, truth, certainty, persuasion, route-safety, probability, appraisal, or reward formula is claimed.

## Content boundary

The catalogue provides 24 bounded next-action prompts, four per family. Each plan contains exactly three player options and two presentation cues. A seed may choose only within caller-authorized families. Evidence tags may prefer a compatible plan, but positive evidence magnitude is discarded (`1` and `999` are equivalent), preventing accidental difficulty/confidence/appraisal math.

Displaying **Test the Route Marker** does not make a route safe. **Seek a Second Witness** does not imply majority truth. **Protect the Original** does not certify authenticity. **Defer Until the Missing Fact Changes** does not freeze the world or guarantee that the missing fact will appear.

## Validation contract

`NightmareClueActionPlanCatalogTest` checks:

- exactly 24 unique primitives and four per family;
- exactly three player options and two cues per primitive;
- deterministic output independent of evidence-map iteration order;
- evidence-magnitude independence;
- compatible positive-evidence preference without truth inference;
- 4,096-seed preservation of caller-owned scenario, actor-context, journal-entry, and allowed-family identity;
- neutral 16,384-seed reachability of all 24 primitives and all 48 primitive/cue combinations;
- explicit anti-overclaim coverage for truth/guilt, route safety, scenario authority, and stale/current conditions;
- fail-closed blank authority IDs, empty family sets, negative evidence, and unknown primitive IDs.

This connector-only run does not claim a local Gradle/JUnit/client/server execution. Hosted validation is reported only from GitHub evidence registered for the exact PR head.
