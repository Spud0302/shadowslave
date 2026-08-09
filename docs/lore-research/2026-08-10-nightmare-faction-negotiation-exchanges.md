# Nightmare faction negotiation exchange primitives — lore evidence

**Date:** 2026-08-10  
**Scope:** bounded player-facing exchange beats after Java has already resolved the Nightmare scenario, faction, negotiation response, interaction state, and allowed exchange families.  
**Generator version:** `nightmare-faction-negotiation-exchange-v1`

## Source-policy check

Research followed `docs/LORE-SOURCE-POLICY.md`, current `main`, open PRs/issues, `PROJECT-STATUS.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, `docs/lore-research/minecraft-implementation-brainstorm.md`, and the active faction pressure / negotiation-response work before implementation.

The owner-designated NovelFull access layer was checked at research time. Its listing currently exposes through Chapter 3116. Official WebNovel was also checked for publication freshness; its fetched pages were not perfectly synchronized, with the title page reporting 3,131 chapters while one catalogue snapshot reported 3,127. No claim in this slice depends on material later than Chapter 743, so that publication-index drift does not affect the design evidence below.

## Primary and later chapter checks

### Chapter 14 — `Child of Shadows`

Sunny's decisive First-Nightmare action depends on an information asymmetry: he knows a relevant property of the Mountain King that Hero does not, stays quiet, creates a misleading situation, and uses that bounded knowledge to change the confrontation. The chapter then explicitly ends the Nightmare before appraisal begins.

**Use here:** bounded knowledge, disclosure, questioning, verification and deliberate non-disclosure can matter to a Nightmare conflict. This does **not** establish a universal dialogue, persuasion, negotiation, truth or faction mechanic.

### Chapter 737 — `Self-Reflection`

Inside a later Nightmare, Sunny treats a nominal ally as a possible adversary, deliberately uses extended conversation and misleading questions to occupy Mordret's attention, and coordinates that social/information action with Kai's separate move.

**Use here:** an exchange can be strategically meaningful without being reducible to combat, and apparent cooperation does not imply settled trust or aligned intent. This does **not** establish a universal negotiation-state machine or success probability.

### Chapter 743 — `Appraisal`

The Spell states that the Nightmare is already over before appraisal. Its retrospective account includes broad deeds and explicitly includes Sunny's battle of wits among meaningful achievements. Sunny then develops a theory about divergence from fate, but that is his interpretation rather than an explicit universal formula supplied by the Spell.

**Use here:** local negotiation/social beats must remain separate from terminal resolution and later appraisal. The project must not infer an appraisal score, persuasion weight, or fate-divergence equation from a displayed exchange.

## Evidence boundary

### CANON

- Nightmare action can materially depend on bounded, asymmetric situational information rather than only direct combat.
- Conversation, deception and a battle of wits can be consequential inside a Nightmare.
- Nominal cooperation does not guarantee aligned intent or future loyalty.
- Nightmare resolution precedes appraisal; appraisal can later recount broad intellectual/social deeds.

### INFERRED

- An immediate exchange beat is a useful authoring concern separable from the already-resolved faction response, deeper faction pressure, allegiance, truth, resources, access legality, world state and terminal resolution.
- `accept`, `clarify`, `counter`, `verify` and `disengage` are useful bounded interaction concerns when Java supplies which families are valid for the current state.
- A player-facing exchange can retain scope, provenance and uncertainty without deciding whether either side is truthful or whether a proposed action succeeds.

### DESIGN

- The exact five-family taxonomy: `ACCEPT`, `CLARIFY`, `COUNTER`, `VERIFY`, `DISENGAGE`.
- All 20 exact exchange primitives, titles, reads, faction lines, player options, affinity tags, presentation cues and anti-overclaim boundaries.
- Opaque caller-owned `scenarioId`, `factionId`, `responseId` and `interactionStateId` handling.
- Positive-evidence preference, evidence-magnitude independence, deterministic SHA-256 selection and generator version `nightmare-faction-negotiation-exchange-v1`.
- The rule that deterministic generation may vary only among caller-authorized exchange families and authored presentation, never canonical state.

### UNKNOWN

- Any canonical negotiation/dialogue generation taxonomy, algorithm, frequency or probability.
- Persuasion, leverage, trust, reputation, intimidation, morale, bluff or deception-detection mechanics.
- Truthfulness, guilt, legitimacy, authority, faction hierarchy or reconstructed historical psychology beyond explicitly authored scenario facts.
- Resource valuation, barter fairness, debt, inventory reservation or transfer rules.
- Access legality, territorial ownership, escort authority, warning accuracy, hostility/aggro transitions or future-offer persistence.
- Any formula mapping faction pressure -> response -> exchange -> accepted event, terminal resolution, appraisal, rewards or progression.

### COMPATIBILITY

Java remains authoritative for:

- scenario, faction, already-resolved negotiation-response and interaction-state identity;
- which exchange families are legal in the current interaction;
- faction membership, allegiance, reputation, trust or relationship state if those are added later;
- resources, transfers, inventory, debts and ownership;
- access legality, routes, NPC movement, AI/aggro/combat and world mutation;
- accepted `ResolutionGraph` events and state transitions;
- terminal Nightmare resolution, per-challenger outcomes, appraisal inputs/results, rewards, progression and persistence.

Dialogue, HUD, subtitles, books, maps, NPC models/AI presentation, audio, animation and other external adapters may render or execute already-authorized presentation intents. They must not infer canonical state from exchange text or presentation cues.

No canonical generation, persuasion, reputation, allegiance, bargaining, resource, access, probability, appraisal, reward or progression formula is claimed by this slice.
