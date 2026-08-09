# Nightmare faction pressure primitives — evidence and design boundary

**Date:** 2026-08-10  
**Branch:** `gpt/nightmare-faction-pressure-primitives-wave1`  
**Scope:** player-facing faction interest/pressure primitives for already-resolved Nightmare scenario factions. This note does not define faction simulation, persuasion, reputation, scenario resolution, appraisal, or a canonical content-generation formula.

## Repository authority checked first

- `docs/LORE-SOURCE-POLICY.md` — chapter text is authoritative; later clarification is required; repository/design claims must retain CANON / INFERRED / DESIGN / UNKNOWN boundaries.
- `docs/NIGHTMARE-SEED-ROADMAP.md` — Nightmare definitions own historical situation, roles, factions, central conflict and resolution graph; world actors/factions may contribute to resolution, but accepted events and terminal resolution remain Java-owned.
- `PROJECT-STATUS.md` and `docs/lore-research/minecraft-implementation-brainstorm.md` — the playable preview is not the canonical template, and historical-scenario/faction concepts are design scaffolding rather than a licence to invent lore mechanics.
- Open PRs and issues were checked before branching. No active PR adds this Nightmare faction-pressure catalogue; the nearest work is NPC motive/stance/conversation content and Dream Realm faction/story presentation, so this slice remains main-based and independently reviewable.

## Primary chapter evidence

### Chapter 2 — `Slave Caravan`

**CANON:** Sunny's First Nightmare reconstructs an old social situation rather than an abstract arena. He inhabits a temple slave in a caravan containing slaves and armed imperial soldiers, with status, authority, scarcity, religious history and interpersonal treatment already embedded in the scenario.

**What this constrains:** Nightmare content can legitimately expose social obligations, access conflicts and group interests as part of the reconstructed situation. It does **not** establish a universal faction taxonomy or negotiation system.

### Chapter 14 — `Child of Shadows`

**CANON:** Sunny's decisive First-Nightmare actions depend on bounded situational knowledge and deliberate manipulation rather than a direct fair fight. The Nightmare ends before appraisal begins.

**What this constrains:** presentation may give players bounded information and negotiation/action hooks, but it must not convert a displayed pressure into authoritative truth, success, or appraisal.

### Chapter 737 — `Self-Reflection`

**CANON:** during the Second Nightmare, people who are nominal allies can still have conflicting intentions. Sunny deliberately stalls and misleads Mordret so another participant can act elsewhere.

**What this constrains:** a current cooperative relationship cannot be flattened into guaranteed alignment, truthfulness or permanent allegiance. Faction-pressure presentation must stay separate from relationship state and accepted scenario events.

### Chapter 743 — `Appraisal`

**CANON:** the Spell appraises Sunny only after the Second Nightmare is already over and recounts a broad set of deeds, including an important battle of wits. Sunny then forms his own theory about why one Nightmare was appraised differently from another.

**What this constrains:** local social/faction actions may matter to the story, but this chapter does not provide a canonical pressure score, negotiation score, fate-divergence formula, or appraisal formula. Sunny's interpretation must not be promoted to project canon.

### Later clarification: Chapter 3006 — `Boons of a Nightmare`

The repository policy requires later checks. The owner-designated NovelFull index was checked and currently lists through Chapter 3116, but web retrieval did not surface a direct Chapter 3006 NovelFull page in this run. Official WebNovel confirms Chapter 3006's identity/title and exposes its opening, which identifies Tamar's Nightmare as taking place in historical Mictlan/Godgrave. The Fandom cross-reference identifies this chapter as the recap of Tamar's cohort Second Nightmare. A secondary full-chapter access result was used only to cross-check the continuation: the cohort faced competing historical sides and members preferred different sides for different reasons, including survival.

**CANON-supported later boundary:** later material is compatible with challengers confronting competing group interests/sides inside a reconstructed Nightmare, rather than every participant sharing one faction objective.

**Source-access limitation:** because the owner-designated archive did not expose the direct Chapter 3006 page through this run's search interface, no implementation rule depends on fine-grained wording from that secondary mirror. The exact six-family taxonomy below remains DESIGN even though the broader factional-conflict shape is later-canon-compatible.

## Freshness check

- Owner-designated NovelFull access layer: latest indexed chapter visible in this run is **3116 — `Princess of the Underworld`**.
- Official WebNovel catalog checked on 2026-08-10 reports **3,149 chapters** and a latest listed release of **Chapter 3148 — `Division of Power`**.

NovelFull is therefore a project-designated reading access layer, not publication authority, and is currently behind official publication. No catalogue primitive below depends on material later than Chapter 3006.

## Evidence ledger

### CANON

- Nightmares can reconstruct substantive historical/social circumstances rather than neutral combat arenas.
- Role and social position can matter inside a Nightmare.
- Bounded knowledge, deception, competing intentions and non-combat interaction can materially affect what challengers do.
- Later Nightmare material is compatible with challengers confronting competing historical sides/interests.
- Nightmare resolution precedes appraisal.

### INFERRED

- It is useful to author faction **pressures/interests** separately from faction identity, allegiance, truth, resources, relationship state and terminal scenario resolution.
- A player-facing pressure can offer negotiation hooks while Java independently decides whether any resulting action is legal, succeeds, changes state, or becomes a `ResolutionGraph` event.
- Scenario authors can constrain which pressure families are appropriate for a resolved faction without requiring a universal procedural faction simulator.

### DESIGN

Wave one defines six reusable pressure families, four primitives each:

- `DUTY` — watch, order, promise, protected charge;
- `RESOURCE` — stores, water, tools, shelter;
- `SECRECY` — hidden route, protected witness, concealed failure, withheld detail;
- `TERRITORIAL` — crossing, boundary, vantage, refuge;
- `RESCUE` — missing people, passage, delaying action, injured movement;
- `SURVIVAL` — withdrawal, division of burden, passage bargain, preservation after loss.

The exact 24 primitives, names, prose, player levers, affinity tags, presentation cues, deterministic SHA-256 selector, positive-evidence preference, evidence-magnitude independence and generator version `nightmare-faction-pressure-v1` are all Minecraft/project DESIGN.

Caller-owned `scenarioId` and `factionId` are opaque nonblank Java identities and are preserved verbatim. Java also supplies the allowed pressure families. Only catalogue IDs/tags are normalized.

### UNKNOWN

Canon does not establish, and this slice does not invent:

- a universal Nightmare faction taxonomy;
- a canonical faction-interest or pressure-generation algorithm;
- pressure frequency, probability, weighting, severity or escalation curves;
- faction resource math, ownership law, territorial legitimacy or scarcity formulas;
- persuasion thresholds, trust/reputation scores, bargaining success, truthfulness or lie detection;
- automatic allegiance changes or relationship transitions;
- canonical sacrifice values, rescue probabilities, route safety or environmental forecasts;
- any mapping from a local faction interaction to `ResolutionGraph` acceptance, terminal Nightmare resolution, appraisal, rewards or progression.

### COMPATIBILITY

Java remains authoritative for:

- scenario and faction identity;
- faction membership/allegiance and any persistent relationship state;
- authoritative resources, locations, actors, casualties and world mutations;
- whether negotiation/action attempts are legal or succeed;
- accepted scenario events and `ResolutionGraph` transitions;
- terminal Nightmare resolution and per-challenger outcome;
- appraisal inputs, rewards, progression and persistence.

External adapters may render dialogue, HUD cards, NPC behavior cues, maps, props, audio or other presentation for the already-selected primitive. Removing an adapter must not remove or rewrite canonical Java state.

## Validation target

`NightmareFactionPressureCatalogTest` is intended to prove:

- exactly 24 unique primitives / four per family;
- three player levers and two presentation cues per primitive;
- deterministic selection independent of input set/map iteration order;
- evidence magnitude cannot become an implicit score;
- compatible positive evidence may prefer presentation without changing Java authority;
- 4,096-seed preservation of mixed-case/namespaced scenario/faction IDs and caller-owned family restrictions;
- neutral reachability of all 24 primitives and all 48 primitive/cue pairs;
- fail-closed malformed authority/evidence inputs;
- explicit anti-overclaim boundaries for allegiance, reputation, scenario acceptance and appraisal.

Independent reproduction of the exact selector arithmetic reaches all 24 primitives and all 48 primitive/cue pairs in the 16,384-seed neutral sweep, with complete pair coverage by seed **245**.
