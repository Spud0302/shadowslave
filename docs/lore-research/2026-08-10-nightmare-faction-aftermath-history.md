# Nightmare faction aftermath / relationship-history evidence — 2026-08-10

## Scope

This slice adds player-facing history presentation for **already-resolved local Nightmare faction interactions**. It does not add a faction simulator, relationship progression, reputation, allegiance, persuasion, truth adjudication, future-behavior prediction, accepted scenario events, terminal resolution, appraisal, rewards, or progression.

The catalogue exposes four authored history kinds supplied by Java authority: `COOPERATED`, `DISPUTED`, `UNRESOLVED`, and `SEPARATED`. These are project presentation categories, not claimed Shadow Slave terminology or a canonical generation formula.

Research began by re-reading `docs/LORE-SOURCE-POLICY.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, current `main`, open PRs/issues, `PROJECT-STATUS.md`, and the active Nightmare NPC/investigation/faction content branches. The branch is based directly on `main@e9f676a9b2ef58cf1e0cc18d45d1420b9e62c2f4` and deliberately does not import the unmerged faction stack.

## Primary chapter checks

### Chapter 2 — `Slave Caravan`

The First Nightmare reconstructs an older social situation with a substantive assigned body/role, slaves, soldiers, hierarchy, danger, and local interpersonal conflict. This supports treating reconstructed social circumstances as consequential scenario content rather than decorative dialogue.

### Chapter 14 — `Child of Shadows`

Sunny's decisive First-Nightmare action depends on bounded asymmetric situational knowledge: he knows a fact about the Mountain King that Auro does not and uses that information in a desperate plan. The chapter then explicitly places the end of the Nightmare before appraisal. This supports preserving what was actually known/done without giving presentation omniscient truth authority.

### Chapter 737 — `Self-Reflection`

Inside a later Nightmare, Sunny continues to treat Mordret as only theoretically an ally and does not assume shared participation means aligned future intent. This is strong later evidence against deriving durable trust, allegiance, or future cooperation from one bounded cooperative interaction.

### Chapter 743 — `Appraisal`

The Spell again begins appraisal only after the Nightmare has ended. Its retrospective narration includes broad deeds and a battle of wits, showing that social/intellectual actions can be consequential while appraisal remains a later, separate concern. Sunny's subsequent theory about changing fate is character interpretation and is not promoted into a project scoring formula.

## Evidence classification

### CANON

- First Nightmares can reconstruct substantive historical/social circumstances and place a challenger into a role within them.
- Bounded information asymmetry and social manipulation can materially affect Nightmare action.
- Nominal cooperation inside a Nightmare does not necessarily establish aligned future intent.
- Nightmare resolution precedes appraisal; appraisal may retrospectively recount broad social/intellectual deeds.

### INFERRED

- It is useful to preserve a bounded interaction history separately from deeper truth, allegiance, trust, reputation, resources, access legality, future behavior, terminal scenario resolution, and appraisal.
- A past cooperative act can be recorded as an event without becoming a durable relationship verdict.
- A dispute can be retained without converting contradiction into deception or blame.
- Unresolved intent/claims/relationship questions should remain unresolved when the supplied state does not settle them.
- Separation can be recorded without forcing permanent hostility, peace, or future behavior.

### DESIGN

- The exact `COOPERATED / DISPUTED / UNRESOLVED / SEPARATED` history taxonomy.
- All 16 exact authored primitives, titles, reads, carry-forward prompts, reflections, tags, cues, and anti-overclaim boundaries.
- Opaque preservation of Java-supplied scenario/faction/interaction/history-entry IDs.
- Positive evidence tags as presentation preference only; evidence magnitude is ignored.
- Deterministic SHA-256 selection and generator version `nightmare-faction-aftermath-history-v1`.
- Treating history entries as reusable presentation primitives rather than a fixed complete story catalogue.

### UNKNOWN

- Any canonical faction-relationship history UI, taxonomy, generator, or frequency distribution.
- Trust, reputation, allegiance, friendship, hostility, betrayal probability, intimidation, persuasion, leverage, or social-AI formulas.
- Whether or how one interaction changes later faction behavior.
- Truthfulness, lie detection, blame, guilt, authenticity, ownership, territorial legitimacy, debt, resource valuation, prices, or scarcity rules.
- Any formula connecting interaction history to accepted `ResolutionGraph` events, terminal Nightmare resolution, challenger outcome, appraisal, rewards, fate, or progression.

### COMPATIBILITY

Java remains authority for scenario/faction/interaction/history identities and exact history kind; current access/resource/world/NPC state; relationship/allegiance/reputation state if such systems are later added; accepted scenario events; `ResolutionGraph` transitions; terminal resolution; challenger outcome; appraisal; rewards; progression; and persistence.

Dialogue, HUD, books, maps, NPC AI, structures, props, audio, particles, and animation may render an already-authorized history entry but cannot derive canonical relationship or scenario state from its prose.

## Anti-overclaim rule

Displaying `COOPERATED` means only that the supplied bounded interaction included cooperation. It does not mean trust, friendship, allegiance, faction-wide agreement, or future help. `DISPUTED` does not prove deception, guilt, hostility, or blame. `UNRESOLVED` preserves missing relationship/intent/truth information instead of guessing it. `SEPARATED` records that contact ended without deciding permanent hostility, peace, abandonment, or future willingness.

No canonical generation, relationship, reputation, allegiance, truth, persuasion, probability, appraisal, reward, or progression formula is claimed.

## Validation intent

`NightmareFactionAftermathHistoryCatalogTest` validates:

- exactly 16 unique primitives / four per history kind;
- exactly three player reflections and two presentation cues per primitive;
- no backend `Java`, `caller-owned`, `authoritative`, or `ResolutionGraph` language in actual player-facing fields;
- 4,096-seed preservation per history kind of mixed-case/namespaced opaque scenario/faction/interaction/history IDs and exact supplied kind;
- evidence map-order and positive-magnitude independence (`1` and `999` are equivalent presentation context);
- compatible evidence preference without authority mutation;
- neutral 16,384-seed reachability of all 16 primitives and all 32 primitive/cue pairs;
- fail-closed blank authority IDs, null kind, negative evidence, and unknown catalogue IDs;
- explicit anti-overclaim coverage for trust, allegiance, reputation, truth, ownership, and future behavior.

No local Gradle/JUnit/client/server execution is claimed from the GitHub connector environment. Hosted CI is evidence only when GitHub registers checks for the exact branch head.

## Integration boundary

This slice has no runtime dependency for review or merge. After the lower faction stack lands, a Java-owned scenario/faction interaction record may validate the opaque interaction/history identity and optionally persist the selected history primitive/seed for exact replay.

The next independent content slice should avoid implementing a relationship meter. A better bounded follow-up is a **Nightmare faction re-encounter context catalogue** that presents already-authorized `KNOWN_HISTORY / CHANGED_CIRCUMSTANCE / OPEN_BUSINESS / NO_CURRENT_COMMITMENT` context when the player meets a faction again, while all actual stance, trust, reputation, allegiance, current terms, accepted events, and appraisal remain Java-owned/UNKNOWN.
