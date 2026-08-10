# Nightmare faction current-intent checks — lore evidence

**Date:** 2026-08-10  
**Scope:** player-facing questions and boundary checks after a Nightmare faction re-encounter context has already been resolved by Java.  
**Implementation:** `NightmareFactionCurrentIntentCheckCatalog`.

## Repository context checked

Before implementation, this slice re-read current `main`, open pull requests and issues, `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and adjacent Nightmare NPC, evidence, investigation, faction-pressure, negotiation, commitment, consequence, history, and re-encounter work.

The adjacent re-encounter catalogue on PR #165 already presents bounded `KNOWN_HISTORY`, `CHANGED_CIRCUMSTANCE`, `OPEN_BUSINESS`, and `NO_CURRENT_COMMITMENT` context. This slice does not duplicate or import that branch. Instead, it accepts an opaque already-resolved re-encounter-context identity plus an interaction identity and caller-approved check families. It is based directly on current `main` so it can review and rebase independently while the faction stack remains open.

## Primary and later chapter checks

### Chapter 14 — *Child of Shadows*

The decisive First-Nightmare sequence depends on asymmetric situational knowledge and deliberate manipulation of what another actor does and does not know. The chapter supports treating information available in a social encounter as consequential without making every statement omniscient truth. The Nightmare ends before appraisal begins.

### Chapter 737 — *Self-Reflection*

Much later Nightmare material again preserves uncertainty around another participant's intentions despite nominal alliance. Strategic questioning and misleading information are part of the conflict. This is strong negative evidence against presentation silently converting current cooperation, a stated goal, or one answer into durable trust, allegiance, or verified motive.

### Chapter 743 — *Appraisal*

The Spell states that the Nightmare is over before beginning appraisal, then recounts broad deeds including a battle of wits before passing its verdict. This supports the architecture boundary that a current-intent question may be meaningful player-facing content while remaining separate from terminal resolution and later appraisal. Sunny's subsequent theory about fate and appraisal remains character interpretation and is not used as a generation or scoring formula here.

The owner-designated NovelFull index was also checked for freshness during this run; it currently lists through Chapter 3116. Official WebNovel was used to confirm Chapter 743 identity. No implementation claim in this slice depends on material later than Chapter 743.

## Evidence classification

### CANON

- Nightmare action can materially depend on incomplete or asymmetric situational information.
- Strategic conversation, misleading questions, and uncertain intent can remain consequential inside a Nightmare.
- Nominal cooperation does not necessarily reveal another participant's complete future intent.
- Nightmare resolution precedes appraisal; appraisal can later recount broad social or intellectual deeds.

### INFERRED

- Present goal, current access, one inherited open matter, and a player's refusal of renewed terms are useful separable presentation concerns around an already-resolved re-encounter.
- Prior history should not silently become current intent, current access, current commitment, or durable relationship state without new authoritative state.
- A player can ask for scope, evidence, or present position while leaving hidden motive and truth unresolved.
- A refusal can remain scoped to the proposed terms rather than automatically defining the whole relationship.

These are implementation-oriented syntheses from the chapter evidence, not claims that the Spell exposes a canonical faction dialogue system.

### DESIGN

- The four check families `CLARIFY_PRESENT_GOAL`, `VERIFY_CURRENT_ACCESS`, `REVISIT_OPEN_MATTER`, and `DECLINE_RENEWED_TERMS`.
- All 16 exact authored primitives, titles, situation reads, player asks, follow-up options, affinity tags, presentation cues, and anti-overclaim boundaries.
- Caller-supplied allowed-family restriction.
- Opaque preservation of scenario, faction, re-encounter-context, and interaction identities.
- Positive evidence-tag preference with evidence magnitude deliberately discarded.
- Deterministic SHA-256 selection and generator version `nightmare-faction-current-intent-check-v1`.

### UNKNOWN

The novel evidence checked here does **not** establish:

- a canonical faction re-encounter/current-intent UI, taxonomy, generator, or frequency;
- persuasion, leverage, trust, reputation, allegiance, hostility, friendship, intimidation, or relationship scores;
- truth, lie, bluff, motive, guilt, bad-faith, or deception-detection rules;
- territorial legitimacy, ownership, permission, access-enforcement, route-safety, escort, or gatekeeping formulas;
- resource valuation, debt, obligation, contract, consent, coercion, or expiry systems;
- faction AI or future-behavior probabilities;
- a mapping from dialogue questions or refusals to accepted `ResolutionGraph` events, terminal resolution, appraisal, rewards, or progression.

No canonical social, faction, relationship, access, truth, probability, appraisal, reward, or generation formula is claimed.

### COMPATIBILITY

Java remains authority for:

- scenario and faction identity;
- the already-resolved re-encounter context and current interaction identity;
- which check families are legal in the current state;
- current faction stance, motive, relationship, trust/reputation/allegiance if such systems are later added;
- access, territory, permission, route, resources, obligations, terms, world state, NPC state, and combat state;
- accepted scenario events and every `ResolutionGraph` transition;
- terminal Nightmare resolution, per-challenger outcomes, appraisal, rewards, progression, and persistence.

Dialogue, HUD, journal, NPC, map, structure, prop, audio, animation, and other adapters may render or execute already-authorized presentation. They must not infer or mutate canonical state from the selected question, answer framing, or presentation seed.

## Implementation limits

The catalogue cannot decide what the faction wants, whether an answer is true, whether access exists, whether an old matter remains legally or socially binding, or what happens after the player declines terms. Java supplies the identities and allowed check families; the catalogue selects only compatible authored presentation.

Changing the seed or positive evidence magnitude cannot change authority. Evidence tags are presentation affinity only. A selected `VERIFY_CURRENT_ACCESS` primitive, for example, can ask whether prior passage still applies but cannot grant passage, unlock terrain, certify safety, or establish territorial ownership.
