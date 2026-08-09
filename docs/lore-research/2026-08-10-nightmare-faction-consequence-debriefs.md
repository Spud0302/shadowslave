# Nightmare faction consequence / debrief presentation — lore evidence

**Date:** 2026-08-10  
**Scope:** bounded player-facing summaries for local faction consequences that Java has already authorized  
**Implementation:** `NightmareFactionConsequenceDebriefCatalog`

## Research question

Can the project present a local consequence of a Nightmare faction interaction without turning presentation into authority over allegiance, territorial legitimacy, resources, truth, terminal scenario resolution, or appraisal?

## Sources checked

Research followed `docs/LORE-SOURCE-POLICY.md`, current `main`, open PRs/issues, `PROJECT-STATUS.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and the active faction pressure / negotiation / commitment branches before implementation.

### Chapter 14 — `Child of Shadows`

Sunny's decisive First-Nightmare action depends on bounded asymmetric information: he knows a decisive fact about the Mountain King that Hero does not. The chapter then explicitly places the end of the Nightmare before appraisal begins.

**Use here:** local knowledge and local consequences can matter without making the presentation layer omniscient, and scenario resolution must remain separate from later appraisal.

### Chapter 737 — `Self-Reflection`

Sunny treats Mordret as an ally only "in theory" and explicitly prepares for the possibility that their interests will diverge. Conversation, misleading questions, and incomplete knowledge become strategically consequential.

**Use here:** one act of cooperation, access, refusal, or a broken term cannot safely be generalized into durable trust, allegiance, hostility, or future intent.

### Chapter 743 — `Appraisal`

The Spell announces that the Nightmare is already over before appraisal. It recounts broad deeds, including Sunny's battle of wits with Mordret, and only afterward gives the appraisal. Sunny then develops a theory about divergence from fate; that is character interpretation, not a universal generation or scoring rule for this project.

**Use here:** local faction consequence presentation must not decide terminal resolution, appraisal, rewards, or progression.

## Freshness / later-material check

The owner-designated NovelFull listing was checked at the start of this work and currently exposes material through Chapter 3116. Official WebNovel catalogue/title snapshots available during the same check expose newer chapters, although the snapshots are not perfectly synchronized with each other. No implementation claim in this slice depends on material later than Chapter 743.

## Evidence boundary

### CANON

- Bounded/asymmetric information and social manipulation can materially affect Nightmare action.
- Nominal cooperation inside a Nightmare does not necessarily settle future intent.
- Nightmare resolution precedes appraisal.
- Appraisal can recount broad non-combat or intellectual/social deeds after the Nightmare has ended.

### INFERRED

- A local consequence is a useful presentation concern separate from deeper faction relationship state, territorial legitimacy, resource accounting, hidden truth, world mutation, terminal scenario resolution, and appraisal.
- Access changes, closed obligations, resource-state changes, and deliberately unresolved relationship questions can be surfaced to the player when Java supplies those facts without the content layer inventing the state transition.
- Preserving an unresolved relationship question is preferable to inventing trust, hostility, betrayal, or allegiance when authoritative state does not establish one.

### DESIGN

- The four exact `ConsequenceKind` values: `ACCESS_CHANGED`, `OBLIGATION_CLOSED`, `RESOURCE_STATE_CHANGED`, and `RELATIONSHIP_UNRESOLVED`.
- All 16 exact debrief primitives, titles, consequence reads, carry-forward prompts, player responses, affinity tags, presentation cues, and anti-overclaim boundaries.
- Opaque mixed-case/namespaced caller-owned scenario/faction/agreement/consequence IDs.
- Positive-evidence preference with evidence magnitude deliberately ignored.
- Deterministic SHA-256 selection and generator version `nightmare-faction-consequence-debrief-v1`.

### UNKNOWN

- Any canonical faction-consequence taxonomy or procedural generator.
- Persuasion, trust, reputation, allegiance, hostility, intimidation, leverage, or betrayal probability systems.
- Resource quantity, scarcity, price, debt, valuation, ownership, or economic formulas.
- Territorial legitimacy, permission enforcement, route safety, access duration, or travel probability rules.
- Truth, authenticity, guilt, blame, motive, bad-faith, or deception-detection rules.
- Future faction behavior probabilities or AI transitions.
- Any formula connecting a faction consequence to accepted `ResolutionGraph` events, terminal Nightmare resolution, appraisal, rewards, or progression.

### COMPATIBILITY

Java remains authority for scenario, faction, agreement, and consequence identity; the exact consequence kind; access and territorial state; resources and transfers; relationship/allegiance/reputation state; actor/NPC/world/combat state; accepted `ResolutionGraph` events; terminal resolution; challenger outcome; appraisal; rewards; progression; and persistence.

Dialogue, HUD, NPC, map, structure, prop, audio, animation, and other external adapters may render an already-authorized debrief. They must not derive canonical state or mutate authoritative gameplay merely because a particular line or cue was selected.

## Deliberate non-claims

This slice does **not** claim a canonical faction, relationship, reputation, resource, territory, negotiation, consequence, probability, appraisal, reward, or progression formula. The taxonomy and authored prose are project DESIGN content intended to make Java-owned outcomes readable without silently becoming state authority.
