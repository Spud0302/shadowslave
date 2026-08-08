# Nightmare Creature encounter roles — wave 1

**Status:** authored player-facing content layer.  
**Runtime status:** data/composition only; no spawning, AI, rewards, progression, or persistence integration.  
**Classification:** Minecraft **DESIGN** constrained by verified Shadow Slave creature lore.

## Purpose

The merged `NightmareCreatureContentCatalog` defines twelve reusable creature identities. This slice adds a separate authoring concern: **what an already-resolved creature is doing in a particular encounter**.

A creature can pressure a scenario as a hunter, obstacle, environmental pressure, deceptive contact, territorial threat, or avoidable hazard without requiring the project to create another bespoke creature class or to force every encounter into a boss fight.

The caller supplies the stable creature ID first. The composer can only select among authored role modules for that exact creature. It never chooses a creature, Rank, Class, spawn, reward, appraisal result, Nightmare ending, or persistent state.

## Player-facing content

Wave one contains **24 modules: exactly two for each of the twelve merged creatures**.

Role families:

- **HUNTER** — active pursuit/ambush pressure where route, observation, or coordination matters;
- **OBSTACLE** — the creature complicates passage or access without requiring its death;
- **ENVIRONMENTAL_PRESSURE** — the creature makes local terrain/infrastructure more dangerous;
- **DECEPTIVE_CONTACT** — information and verification are central to the encounter;
- **TERRITORIAL_THREAT** — remaining or repeatedly travelling through an occupied area increases pressure;
- **AVOIDABLE_HAZARD** — recognition can make refusal, delay, or an alternate route a valid authored response.

Examples include:

- Ash Burrower as a vibration-following hunter or unstable-route obstacle;
- Bell-Eater as a resonance hunter or pressure on noisy signal infrastructure;
- Drowned Listener as a flooded-route pursuer or entirely avoidable water hazard;
- Gutter Choir as deceptive voices or a district-scale acoustic pressure;
- Hollow Mimic as an information-verification contact or suspicious route to refuse;
- Pale Ferryman as a deceptive crossing contact or an obstacle whose shortest route need not be accepted;
- Thorn Matron as escalating overgrowth pressure or a territorial route-control problem;
- Veil Stalker as a shared-observation hunter or uncertainty/deception encounter.

Every module includes an encounter frame, escalation, counterplay frame, evidence tags, and an explicit anti-overclaim boundary.

## Lore research

Research followed `docs/LORE-SOURCE-POLICY.md`. The owner-designated NovelFull access layer was checked at the start of the task and listed through **Chapter 3131, Secret Weapon**. Official WebNovel also exposed **3,131 chapters** during this run, so no publication-gap workaround was needed for the evidence below.

Primary and later material checked:

- **Chapter 201 — Lord of the Dead:** Class differences can be qualitative rather than a simple raw-power ladder; Demons and higher can be intelligent, while Tyrants introduce a distinct authority/minion capability. This is useful negative evidence against treating every creature as the same combat AI with larger numbers.
- **Chapter 370 — Exploration Report:** Sunny records Nightmare Creature powers, behavior, and weaknesses alongside Dream Realm geography/environment as practical learned information. This is the strongest basis for player-readable encounter differences and counterplay.
- **Chapter 380 — Above and Below:** the Chained Isles combine varied Nightmare Creature populations with lethal regional environmental cycles. Travel decisions, local conditions, and creature danger materially interact.
- **Chapter 969 — Gifts of the Shore:** a vast moving horde can function as a strategic route obstacle; fighting through it is considered one option rather than automatically the required objective.
- **Chapter 1461 — Encore:** Sunny deliberately manipulates known creature hunting grounds/routes so different threats interact and clear another route, showing that creature ecology can be used as encounter strategy rather than only confronted head-on.
- **Chapter 1608 — Death Zone:** later Godgrave material again couples lethal environment, waiting/movement choices, Nightmare Creatures, and cohort tactics.

No exact invented creature in this repository is claimed to appear in the novel.

## Evidence labels

### CANON

- Nightmare Creatures can differ qualitatively in powers, behavior, intelligence, and weaknesses rather than only raw combat numbers.
- Geography and environmental conditions materially affect Dream Realm travel and creature encounters.
- Creature locations, routes, behavior, and weaknesses can be learned and used strategically.
- A dangerous mass or creature presence can be treated as a route/strategic problem rather than a universal mandatory boss objective.

### INFERRED

- For Minecraft authoring, **creature identity** and **encounter role** are useful separable concerns: the same resolved creature can plausibly be framed as pursuit, passage pressure, local hazard, deception, or territory depending on authored circumstances.
- Explicit counterplay and optional bypasses are useful where the source profile supports them, because canon repeatedly rewards understanding the nature and context of a threat.

### DESIGN

- all six `EncounterRole` values;
- all 24 exact role modules, frames, escalations, counterplay text, affinity tags, and evidence tags;
- exactly two modules per current creature;
- evidence matching by count of positive affinity tags;
- deterministic seed tie-breaking;
- ignoring positive evidence magnitude;
- every anti-overclaim boundary;
- generator version `nightmare-creature-encounter-role-v1`.

### UNKNOWN

- any canonical Nightmare Creature generation or encounter-director formula;
- universal creature-role taxonomy;
- spawn frequency, territory size, aggro/detection radius, pathfinding, or AI state mapping;
- universal rule for whether an encounter can be bypassed;
- any Rank/Class-to-role probability or Rank/Class-to-behavior formula;
- whether the Nightmare Spell itself composes encounters from reusable roles;
- reward, Memory/Echo drop, appraisal, or scenario-completion consequences of these roles.

No deterministic rule in this slice is claimed to reproduce the Nightmare Spell or Dream Realm ecology.

### COMPATIBILITY

- `NightmareCreatureContentCatalog` remains the Java authority for the stable resolved creature profile used by this layer.
- The composer receives the creature ID from Java-owned state and cannot substitute another creature.
- Future entity AI, models, sounds, particles, structures, dialogue, HUD, or quest adapters may execute/render a resolved module, but they cannot own stable creature identity, Rank/Class, Nightmare progression, rewards, or persistent encounter authority.
- If exact role presentation must survive restart, a later Java-owned encounter instance should persist the resolved module ID/seed instead of recomposing it from flavor state.

## Validation contract

`NightmareCreatureEncounterRoleCatalogTest` checks:

- exactly 24 unique modules and exactly two for each of the twelve merged creatures;
- all six encounter-role families are represented;
- every module's required pressure is already present on its source creature profile;
- all frames, escalations, counterplay, evidence, and anti-overclaim text are substantive;
- a 256-seed sweep for every creature can never change caller-supplied creature identity;
- evidence magnitude `1` versus `999` cannot silently become an invented canonical scoring formula;
- positive local evidence can prefer a compatible authored role while preserving creature identity;
- neutral evidence reaches both Drowned Listener modules over a 512-seed sweep;
- high-risk deception/avoidability profiles retain explicit negative boundaries;
- unknown creatures/modules and negative evidence fail closed.

## Integration boundary

This slice does **not** spawn creatures, run AI, mutate world state, award Memories/Echoes, compute appraisal, resolve Nightmares, or persist encounter instances. It is independently reviewable from PR #118's encounter-presentation layer: #118 explains what the player reads about a resolved creature, while this slice describes the authored function that creature can serve in a larger encounter.

A later composition layer may combine an already-resolved region/scenario, creature, and encounter-role module, then persist the resolved Java definition. That future layer must keep selection probabilities and canonical generation rules **UNKNOWN** unless primary text establishes them.
