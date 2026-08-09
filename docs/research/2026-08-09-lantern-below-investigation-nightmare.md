# The Lantern Below — lore and design evidence

**Date:** 2026-08-09  
**Scope:** bounded authored First-Nightmare-style investigation/search-and-rescue content  
**Implementation:** `LanternBelowScenarioDefinition`

## Repository boundary checked

Before authoring this slice, current `main`, open PRs/issues, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, the merged `ResolutionGraph`, `DrownedBellScenarioDefinition`, and active authored-scenario PRs were reviewed.

The overlap check intentionally keeps this scenario separate from:

- **The Drowned Bell** — storm/flood evacuation plus one creature drawn by resonance;
- **The Hollow Treaty** — testimony, translation, forged evidence and inter-faction diplomacy;
- **The Falling Span** — evacuation, bridge denial, negotiation and delayed world consequences.

`The Lantern Below` instead focuses on evidence verification, underground route reconstruction, missing-person search, bounded rescue, containment and preserving/exposing records under environmental pressure.

## Primary chapter evidence checked

### Chapter 2 — Slave Caravan

Sunny's First Nightmare visibly reconstructs an older situation and places him inside a substantive historical body/role. His physical condition, social position, local people and circumstances constrain what he can do. The chapter also describes ordinary First-Nightmare starts as scenarios with materially different amounts of agency, but that observation does **not** establish a project generation formula.

### Chapter 14 — Child of Shadows

The decisive First-Nightmare sequence depends on situational knowledge and manipulation of the reconstructed conflict rather than an announced universal boss-objective contract. The Nightmare ends after the reconstructed situation reaches its decisive outcome. This is compatible with, but does not prove, broader non-combat resolution structures.

### Chapter 15 — appraisal sequence

The Spell states that the trial is over and then performs appraisal, recounting concrete deeds before passing a final appraisal. This supports keeping scenario resolution separate from later appraisal/evidence presentation.

### Chapter 743 — Appraisal

A later Nightmare is again already over before appraisal. The Spell recounts a broad history of actions and consequences, including endurance, allies, sorcery, large-scale consequences and a battle of wits, before giving a verdict. Sunny's subsequent explanation about divergence from fate remains his theory and is not used as a project scoring rule.

### Chapter 2029 — Fortune Telling

Much later material explicitly says that some First-Nightmare Attributes and Sunny's initial Temple Slave Aspect came from the historical body he inhabited. This reinforces that a First-Nightmare role/body can be substantive rather than cosmetic presentation.

## Freshness check

At research time, the owner-designated NovelFull access layer listed through **Chapter 3116 — Princess of the Underworld**. Official WebNovel reported **3,131 chapters** in its current catalogue snapshot, with a latest-release snapshot around Chapter 3130. The third-party host is therefore treated only as the project-designated full-chapter reading access layer, not as publication authority.

No claim in this slice depends on material later than Chapter 2029.

## Evidence classification

### CANON

- A First Nightmare can reconstruct an older situation and place the challenger in another person's substantive historical body/role.
- That inherited role/body can carry meaningful physical and social circumstances rather than being a cosmetic class label.
- Nightmare resolution and appraisal are distinct stages.
- Appraisal can narrate broad deeds and consequences rather than only combat kills.
- The primary First-Nightmare text is compatible with decisive situational knowledge/manipulation rather than a universal announced boss objective.

### INFERRED

- Investigation, route knowledge, witness comparison, rescue, containment and evidence preservation are useful separable authoring concerns for representing a reconstructed conflict.
- A historical role can provide bounded local knowledge without granting automatic authority, omniscience or perfect truth detection.
- Scenario terminal meaning should belong to the `ResolutionGraph` state and accepted event path, not to a single interaction in isolation.

### DESIGN

Everything specific to **The Lantern Below** is project-authored DESIGN:

- the scenario, title and premise;
- the Survey Clerk's Assistant role;
- all locations, characters, pressures, route marks, altered ledger and collapse details;
- all twelve event/choice IDs;
- all five terminal resolutions;
- the exact transition graph;
- appraisal-evidence tags and positive weights;
- the decision to make this scenario contain no declared Nightmare Creature or boss objective.

The five endings are intentionally morally and practically distinct: full rescue, bounded injured-person rescue, containment with unresolved loss, public exposure of the altered order, and preservation of evidence while leaving the deeper rescue unfinished.

### UNKNOWN

The repository does **not** claim to know:

- the Nightmare Spell's actual scenario-generation or role-selection algorithm;
- whether every First Nightmare must contain a Nightmare Creature;
- whether every First Nightmare exposes multiple terminal resolutions;
- any universal investigation, rescue, cave-in, testimony or evidence mechanic;
- the canonical number or type of participants, NPCs, locations or endings;
- the relationship among historical fate, difficulty, role, choices, evidence and final appraisal;
- whether divergence from historical fate is actually the Spell's appraisal principle;
- any canonical appraisal-scoring formula or meaning for the DESIGN evidence weights.

The absence of a declared Nightmare Creature in this authored module is therefore **DESIGN**, not a canon claim that creature-less First Nightmares are known to exist.

### COMPATIBILITY

- `ResolutionGraph` remains the Java authority for accepted events and terminal resolution identity.
- This definition owns immutable content only; it does not own active Nightmare persistence, challenger outcomes, appraisal verdicts, rewards, Soul/progression state or world mutation.
- Future structures, cave-ins, particles, sound, NPC AI/dialogue, route markers and HUD adapters may render or execute already-authored event intents, but cannot decide canonical completion or progression.
- A future Java-owned `NightmareInstance` may persist the stable scenario/event/resolution identities; adapters must not reconstruct authoritative state from flavor text.

## Validation boundary

`LanternBelowScenarioDefinitionTest` is intended to prove:

- stable content counts and resolution IDs;
- every declared choice is accepted in at least one reachable state;
- all five terminal resolutions are explicitly reachable;
- no declared choice is a kill/slay/boss objective;
- full rescue cannot be rushed from the initial state and requires route/air/signal preparation;
- investigation can terminally resolve the authored conflict without completing the deep rescue;
- every ending exposes a distinct, positive appraisal-evidence shape while no verdict is calculated.

No local Gradle/JUnit/client/server execution is claimed from the GitHub connector environment. Hosted CI may be claimed only for an exact head that GitHub actually registers and runs.
