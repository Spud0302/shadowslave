# Nightmare faction negotiation-response evidence — 2026-08-10

## Scope

This note supports `NightmareFactionNegotiationResponseCatalog`, a bounded player-facing content layer for a Nightmare faction whose scenario identity, faction identity, pressure identity, interaction-state identity, and allowed response families have already been resolved by Java-owned scenario logic.

The catalogue does **not** decide whether a negotiation succeeds, what a faction truly believes, whether access is actually granted, whether resources move, whether allegiance/reputation changes, or whether a `ResolutionGraph` event is accepted. It only supplies authored presentation inside authority supplied by the Java core.

This branch is based directly on `main@e9f676a9b2ef58cf1e0cc18d45d1420b9e62c2f4`. It deliberately does not import open PR #156. The caller may later bind its opaque `pressureId` to #156 after that catalogue lands, but this slice remains independently reviewable and rebaseable.

## Repository policy and architecture checked

Before implementation, re-read:

- `docs/LORE-SOURCE-POLICY.md`;
- `docs/NIGHTMARE-SEED-ROADMAP.md`;
- current `main` and open PRs/issues;
- current Nightmare NPC, evidence/investigation, scenario, and faction-pressure content work.

The roadmap requires scenario state, accepted events, terminal resolution, challenger outcome, and appraisal to remain distinct. A negotiation response therefore cannot be treated as an accepted event merely because presentation says that a faction cooperates, refuses, warns, bargains, or offers conditional access.

## Chapter evidence checked

### Chapter 14 — `Child of Shadows`

Rechecked through the owner-designated chapter access layer. Sunny's First Nightmare decisive sequence depends on bounded situational information, what other actors know or do not know, and manipulation of the reconstructed conflict. The Nightmare ends before appraisal begins.

**Use here:** supports treating local information and social choices as potentially consequential while refusing to make presentation omniscient or appraisal-authoritative.

### Chapter 737 — `Self-Reflection`

Rechecked as a later Nightmare clarification. The chapter preserves uncertainty around another participant's intentions despite nominal cooperation and depicts strategic social/information conflict inside a Nightmare.

**Use here:** supports the compatibility of cooperation, refusal, warning, and bargaining-like social pressure with deeper intentions remaining unresolved. It does not establish a negotiation taxonomy or persuasion system.

### Chapter 743 — `Appraisal`

Rechecked as the later appraisal boundary. The Nightmare is already over when appraisal begins, and the recount can include broad intellectual/social deeds.

**Use here:** keeps negotiation presentation and any scenario-local consequences separate from appraisal. No universal social-action score or response formula is supplied.

### Later competing-side clarification — Chapter 3006, `Boons of a Nightmare`

A later Mictlan Nightmare chapter was checked as corroborating context for reconstructed historical factions/sides. During this run, the owner-designated NovelFull search interface did not surface the chapter body directly. A secondary full-text access result and a community chapter locator were used only to confirm that the chapter is associated with participants operating amid competing historical sides. No exact primitive, rule, probability, or implementation decision depends on wording from the secondary mirror or community summary.

This limitation is deliberately retained rather than promoting an access-layer cross-check into primary canon authority.

## Source freshness

At research time the owner-designated NovelFull listing exposed material through Chapter 3116. The official WebNovel catalogue snapshot retrieved during the same run reported 3,131 chapters. The numbers are access/publication metadata, not lore evidence, and may be volatile. No claim in this slice depends on material later than Chapter 3006; the decisive implementation constraints come from the directly checked earlier chapter material above.

## Evidence boundary

### CANON

- Nightmares can contain substantive reconstructed circumstances in which bounded knowledge and social/information conflict materially affect action.
- Participants who appear to be cooperating can retain uncertain or conflicting intentions inside a Nightmare.
- Nightmare resolution precedes appraisal; appraisal is not the mechanism that decides whether the local interaction happened.
- Later Nightmare material is compatible with reconstructed conflicts containing competing historical sides.

### INFERRED

- An immediate faction response is a useful authoring concern separate from deeper pressure, allegiance, truth, resources, relationship state, world state, and terminal resolution.
- A caller-owned interaction state can safely expose bounded cooperation, counteroffer, refusal, warning, or conditional-access presentation without making that presentation authoritative for the hidden outcome.
- A resolved faction pressure may later be used by Java to constrain allowed response families while still leaving the response catalogue presentation-only.

### DESIGN

- The exact five response families: `COOPERATION`, `COUNTEROFFER`, `REFUSAL`, `WARNING`, and `CONDITIONAL_ACCESS`.
- All 20 exact response primitives, their faction lines, player options, affinity tags, presentation cues, and anti-overclaim boundaries.
- Caller-supplied opaque scenario/faction/pressure/interaction-state IDs.
- Caller-supplied allowed response families.
- Positive-evidence tag preference while discarding evidence magnitude.
- Deterministic SHA-256 selection and generator version `nightmare-faction-negotiation-response-v1`.

These are gameplay/content authoring choices. They are not claimed to describe how the Nightmare Spell generates personalities or negotiations.

### UNKNOWN

The novel evidence checked does not establish:

- a canonical faction-negotiation taxonomy or response generator;
- probabilities/frequencies for cooperation, refusal, bargaining, warning, or access;
- persuasion, leverage, charisma, trust, reputation, intimidation, morale, or relationship equations;
- faction-resource valuation, scarcity, debt, exchange-rate, or ownership formulas;
- truthfulness, lie detection, bluff detection, guilt, legitimacy, sovereignty, or territorial-right rules;
- exact reconstructed historical psychology/dialogue;
- warning accuracy or future-event probability;
- universal access/escort/verification rules;
- allegiance-transition, hostility/aggro, NPC-AI, or combat-escalation rules;
- a pressure-to-response formula;
- any response-to-`ResolutionGraph`, appraisal, reward, or progression formula.

These remain `UNKNOWN`; the implementation must not fill them in because deterministic content composition is convenient.

### COMPATIBILITY

Java remains the authority for:

- scenario, faction, pressure, and interaction-state identity;
- allowed response families;
- membership, allegiance, relationship/reputation, and resource ownership;
- actual resource transfer or access legality;
- NPC/world state, movement, combat/aggro, and escalation;
- accepted `ResolutionGraph` events and transitions;
- terminal Nightmare resolution and per-challenger outcome;
- appraisal inputs/verdict, rewards, progression, and persistent state.

Dialogue, HUD, NPC AI, maps, structures, props, audio, animation, and other external adapters may render or execute already-authorized presentation. They must not derive canonical state from a response primitive.

## Implementation consequence

The catalogue consumes authoritative IDs instead of generating them. Seed and evidence may choose only among the caller's allowed families. Positive evidence is reduced to a set of tags, so magnitude `1` and `999` are intentionally equivalent. A displayed response is never evidence that the associated pressure is true or that its requested outcome occurred.

No canonical generation, persuasion, reputation, allegiance, bargaining, resource, access, probability, appraisal, reward, or progression formula is claimed.
