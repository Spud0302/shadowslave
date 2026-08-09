# Dream Realm field-report content — wave 1

**Status:** player-facing DESIGN content proposal  
**Scope:** post-exploration field notes for already-resolved Dream Realm regions  
**Generator:** `dream-realm-field-report-v1`

## Purpose

This slice adds a Java-owned presentation/content layer for information a player has already encountered during exploration. It deliberately distinguishes three useful knowledge states:

- **OBSERVED** — a current local condition the player can directly notice;
- **VERIFIED** — a local landmark/reference checked through more than one physical observation;
- **PROVISIONAL** — a plausible interpretation of indirect evidence that remains explicitly unconfirmed.

These states are player-facing knowledge labels, not a new authority for world truth. They do not discover regions, reveal maps, spawn creatures, calculate encounter probability, forecast weather, award progression, or make a suspected creature present merely because a report mentions it.

## Content model

Wave one contains **30 knowledge anchors**: exactly one OBSERVED, one VERIFIED, and one PROVISIONAL anchor for each of the ten regions already merged in `DreamRealmRegionContentCatalog`.

The anchor subjects are constrained to source-region primitives already on `main`:

- OBSERVED anchors use one existing regional hazard;
- VERIFIED anchors use one existing regional landmark hook;
- PROVISIONAL anchors use one existing regional creature affinity.

The catalogue also contains **9 reusable report framings**, three for each knowledge state. Composition therefore produces **90 authored-anchor/framing combinations** without maintaining a fixed list of 90 finished report cards. The deterministic seed may choose only among framings compatible with the caller-supplied state; it cannot change region, certainty, subject, evidence basis, or limitation.

Examples include:

- Ashen Expanse — current open-flat exposure; a cross-checked Buried Watchtower; an unconfirmed seam under ash compatible with Ash Burrower pressure;
- Glassmere Flats — presently felt resonance; a locally aligned Mirror Ridge; a broken resonance pattern that might indicate Bell-Eater pressure but is not proof;
- Blackwater Steps — a rising local waterline; Rope Harbour confirmed from water and high ground; sound deadening that might indicate a Drowned Listener but does not confirm one;
- Mistwound Pass — current visibility loss; a Weather Cairn cross-check that is explicitly not a weather oracle; a familiar voice without a visible source kept provisional rather than converted into automatic lie detection;
- Storm Lantern Coast — current water crossing a lower-path marker; Storm Belfry fixed against cliff references without becoming a forecast device; a missing bell note that may indicate Bell-Eater pressure but is not an encounter detector;
- Red Canopy — fresh vines entering a route; Giant Root re-identified from two vertical layers; layered voices that may fit Gutter Choir pressure without proving presence or number.

## Lore research

Research followed `docs/LORE-SOURCE-POLICY.md` before implementation. Current `main`, open PRs/issues, `docs/NIGHTMARE-SEED-ROADMAP.md`, the merged Dream Realm region catalogue, and adjacent open Dream Realm content PRs were reviewed first to avoid duplicating active work.

### Chapter 370 — Exploration Report

This is the decisive primary source for the field-report concept. Sunny compiles information gathered through exploration into a report intended to help someone survive the Forgotten Shore. The report covers geography and environment, landmarks, Nightmare Creature powers/behavior/weaknesses, Spell-provided Memory/Echo information where available, historical observations, and Sunny's own theories. The chapter also makes an important epistemic distinction: some material is direct observation or received information, while some is theory, and Sunny can deliberately omit information.

Project consequence: useful field knowledge can be recorded and communicated, but the project should not flatten observation, verified reference points, theory, hidden information, and omniscient world truth into one state.

### Chapter 2278 — Field Reports

Later material continues to use reports/status updates as operational information rather than omniscient truth. The chapter includes concise updates about current conditions, travel/logistics, limitations, research, and risks. It supports the continued relevance of bounded, source-attributed operational knowledge without establishing a universal supernatural reporting system.

Project consequence: later material is compatible with reports carrying current observations and uncertainty, but does not supply the exact OBSERVED / VERIFIED / PROVISIONAL taxonomy in this catalogue.

### Publication freshness

Checked 2026-08-09 before implementation:

- the owner-designated NovelFull access layer currently lists through **Chapter 3116 — Princess of the Underworld**;
- official WebNovel currently reports **3,131 chapters**.

NovelFull is therefore treated as the required working reading-access layer, not publication authority. No claim in this slice depends on material later than Chapter 2278.

## Evidence classification

### CANON

- Dream Realm geography/environment, notable landmarks, and Nightmare Creature powers/behavior/weaknesses can be valuable recorded exploration information.
- A field report can combine personally gathered observations with information obtained from other sources.
- A report can contain theory/interpretation rather than only certain fact, and information can be incomplete or deliberately omitted.
- Later operational reporting continues to communicate current conditions, logistics, limitations, and risks.

### INFERRED

- Player-facing exploration content benefits from distinguishing direct observation, locally cross-checked reference information, and an explicitly provisional hypothesis.
- A local landmark can be useful as a bounded navigation reference without granting perfect regional knowledge.
- Indirect creature signs can support caution without authoritatively asserting that a creature is present.

### DESIGN

- the exact `OBSERVED`, `VERIFIED`, and `PROVISIONAL` enum taxonomy;
- all 30 exact knowledge anchors and their prose;
- all 9 exact reusable framings;
- one hazard / one landmark / one creature-sign anchor per region;
- deterministic framing selection;
- exact `basis`, `nextAction`, and `limitation` fields;
- generator version `dream-realm-field-report-v1`.

The existing ten region identities, hazards, landmark hooks, and creature affinities are also project-authored DESIGN content. This PR does not claim that Ashen Expanse, Storm Lantern Coast, their landmarks, or their exact creature affinities are novel canon.

### UNKNOWN

- any canonical field-report format, confidence scale, or Spell knowledge UI;
- whether the Spell itself marks information as observed/verified/provisional;
- automatic map reveal, map accuracy, discovery radius, or landmark triangulation rules;
- creature-presence inference, spawn probability, territory, migration, population, or encounter timing;
- weather/flood/resonance forecasts and numerical environmental models;
- who automatically shares reports with whom, communication range, secrecy rules, prices, reputation, rewards, or progression for reporting information;
- any universal rule for how much evidence is enough to convert a hypothesis into certainty.

No canonical knowledge-generation, confidence, mapping, encounter, spawn, forecast, or reward formula is claimed.

### COMPATIBILITY

`DreamRealmRegionContentCatalog` remains the Java authority for stable region identity and its authored hazards, landmarks, and creature affinities. This field-report catalogue consumes those resolved primitives only.

A later Java-owned exploration/knowledge record may persist facts such as region ID, anchor ID, knowledge state, framing seed, who observed it, and whether later evidence changed its state. That durable record must remain authoritative. Map/HUD/book/chat/NPC/audio adapters may render the record, but cannot infer or mutate canonical world state, creature presence, progression, rewards, or persistent knowledge authority from presentation text.

## Validation contract

`DreamRealmFieldReportCatalogTest` checks:

1. exactly 30 anchors, with exactly one OBSERVED, VERIFIED, and PROVISIONAL anchor per merged region;
2. exactly nine framings, with three reusable framings per knowledge state;
3. every OBSERVED hazard belongs to its source region;
4. every VERIFIED landmark belongs to its source region;
5. every PROVISIONAL creature sign references an existing source-region creature affinity;
6. deterministic same-seed composition;
7. a 2,048-seed sweep per region/state preserving region, knowledge state, anchor, subject, basis, and limitation;
8. the same sweep reaches all three compatible framings for every region/state;
9. explicit anti-overclaim boundaries for map reveal, forecasting, supernatural landmark interpretation, lie detection, and creature confirmation;
10. unknown/malformed region IDs, unknown anchor IDs, and null certainty fail closed.

## Integration boundary

This slice does not modify world generation, travel, creature AI/spawning, maps, player persistence, progression, rewards, Soul state, Nightmare state, or external mod authority.

A future integration can let authoritative Java exploration actions advance a knowledge record from provisional to observed/verified when actual gameplay evidence warrants it. Such a transition must be an explicit Java state change; rerendering a report with another seed must never change certainty.

## Best next content slice

**Dream Realm expedition journal composition**: combine already-resolved field reports, landmark/resource micro-modules, and creature observations into a bounded per-expedition journal page with separate known/uncertain/withheld entries. Keep automatic sharing, map reveal, knowledge progression, prices/reputation, and report truth evaluation explicitly UNKNOWN, and avoid depending on unmerged presentation branches where possible.
