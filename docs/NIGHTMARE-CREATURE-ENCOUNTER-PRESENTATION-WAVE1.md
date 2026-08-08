# Nightmare Creature encounter presentation — wave 1

**Status:** bounded player-facing DESIGN content for the existing Java-owned Nightmare Creature catalogue.  
**Code:** `NightmareCreatureEncounterPresentationCatalog`  
**Presentation version:** `nightmare-creature-encounter-presentation-v1`

## Scope

`NightmareCreatureContentCatalog` already owns twelve authored creature identities with Rank, Class, senses, locomotion, pressures, environment tags, counterplay tags, appraisal evidence and an encounter cue. This slice does not create another creature generator. It turns an already-resolved creature profile into three readable encounter surfaces:

1. **first sign** — the existing authored sensory cue;
2. **threat read** — a concise explanation of what pressure the player should understand;
3. **counterplay hint** — one deterministic hint selected only from that creature's already-authored counterplay set.

The seed may vary which hint is surfaced first. It cannot change creature identity, Rank, Class, source counterplay, spawn state, rewards or persistence.

## Player-facing content

All twelve current creatures gain encounter-readable threat/counterplay presentation:

- **Ash Burrower:** read moving ash as a vibration-driven ambush and answer with firm ground, false vibration or elevation;
- **Bell-Eater:** understand ringing as pursuit pressure and use silence, false echoes or softer movement surfaces;
- **Chainback:** read trailing iron as a snag/displacement threat and exploit gaps, anchors or vertical separation;
- **Drowned Listener:** recognize stilling water as a sound-targeting warning and use decoys, dry ground or route collapse;
- **Glasswing:** read reflected light as an approach tell and break glare/sightlines;
- **Gutter Choir:** treat stolen voices as an information threat requiring source verification;
- **Hollow Mimic:** treat familiar speech without breathing as a verification problem rather than automatic truth;
- **Mire Runner:** read reed wakes as coordinated pursuit and disrupt route, scent or pack movement;
- **Pale Ferryman:** treat the crossing as a choice/displacement encounter where refusal and alternate routes are valid;
- **Stone Maw:** read circular cracking as an underground area-denial tell and exploit timing/surface choice;
- **Thorn Matron:** understand growing briar corridors as escalating terrain control and reopen only the routes needed;
- **Veil Stalker:** read displaced mist as the warning and use shelter, crosswind and shared observation.

Every profile includes an explicit boundary preventing the exact DESIGN mechanic from being generalized into a canonical Rank/Class rule.

## Lore research

Research followed `docs/LORE-SOURCE-POLICY.md`. The decisive claims were checked against chapter text, with official WebNovel used where available.

### Chapter 370 — Exploration Report

Sunny's report explicitly records Dream Realm geography/environment together with Nightmare Creature powers, behavior and weaknesses, using direct experience where possible. This is strong primary evidence that creature-specific behavior and learned counterplay are meaningful in-world knowledge rather than merely hidden combat statistics.

### Chapter 256 — True Reason

Later discussion identifies an Echo as a copy of a Nightmare Creature and illustrates how bizarre a creature's actual nature can be. This reinforces the project's existing rule that creature identity should not be reduced to a generic Minecraft mob silhouette. It does not establish this slice's encounter phases or counterplay taxonomy.

### Chapter 468 — Desecrated Grove

The Chained Isles material was rechecked as a route/geography example. The chapter itself is not used to claim a universal creature rule; it reinforces the broader setting context in which routes, islands and known dangers shape decisions.

### Chapter 1608 — Death Zone

Much later Godgrave material again shows that surviving Nightmare Creatures cannot be separated from regional environmental danger and cohort tactics. The exact authored interactions in this wave remain DESIGN.

## Evidence boundary

### CANON

- Nightmare Creatures have distinct powers, behavior and weaknesses that can be learned and recorded.
- Dream Realm geography/environment is relevant to understanding encounters.
- Nightmare Creatures can have bizarre forms and capabilities not reducible to ordinary animal templates.
- Rank and Class are established creature identity vocabulary; this slice preserves the already-authored values and does not calculate them.

### INFERRED

- A player-facing encounter benefits from separating the first observable sign, the interpreted threat, and practical learned counterplay.
- Presenting creature-specific counterplay can make exploration knowledge actionable without exposing a universal numeric weakness table.

### DESIGN

- All twelve exact creature profiles remain project-authored DESIGN.
- `FIRST_SIGN`, `THREAT_READ` and `COUNTERPLAY` are presentation phases, not canonical categories.
- Every threat explanation, counterplay sentence, deterministic seed rule and anti-overclaim boundary is DESIGN.
- The source catalogue's senses, locomotion, pressure, environment, counterplay and appraisal-evidence tags remain DESIGN taxonomies.

### UNKNOWN

- There is no verified canonical formula generating Nightmare Creature powers, behavior, senses or weaknesses.
- There is no verified universal mapping from Rank/Class to Minecraft statistics, AI complexity, detection range, damage, health or weakness count.
- There is no canonical rule that every creature exposes a readable warning tell or exactly three practical counters.
- There is no canonical encounter-director, spawn, regional-frequency or reward formula here.

### COMPATIBILITY

- `NightmareCreatureContentCatalog` remains the Java authority for stable creature content identity.
- The composer receives an already-resolved creature ID and may vary only presentation among that creature's authored hints.
- Future entity AI, model, sound, particles, HUD or dialogue adapters may render/execute removable presentation behavior, but cannot select or persist canonical creature identity, Rank/Class, progression or rewards.
- A later Java-owned encounter instance may persist the resolved presentation seed/hint if exact wording must survive restart; presentation must never reconstruct authoritative creature state.

## Validation contract

`NightmareCreatureEncounterPresentationCatalogTest` requires:

1. exact coverage of all twelve current source creature IDs;
2. exact preservation of source display name, Rank, Class, first-sign cue and counterplay tags;
3. deterministic same-seed output;
4. a 256-seed sweep cannot alter creature identity or Rank/Class;
5. a 512-seed sweep can surface every authored counterplay option for every creature;
6. high-risk evocative profiles retain explicit anti-overclaim boundaries;
7. unknown creature IDs fail closed.

No canonical creature-generation, encounter, AI, weakness, spawn or reward formula is claimed.