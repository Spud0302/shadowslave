# Modpack roadmap research

**Research date:** 2026-08-06  
**Repository baseline:** `main@37287c4ccdf3709440be034f2ce5b71eb3d275cc`  
**Authoring status:** research and recommendation, not an approved implementation contract

## Purpose

This document records a repository-wide assessment of the Shadow Slave project and a proposed sequence for building the Nightmare Spell modpack. It is intended to be visible to future GPT, Claude and Codex sessions without replacing the current authorities in `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `mod/IMPLEMENTATION-STATUS.md`, `docs/MOD-TRANSITION-PLAN.md` or `docs/NIGHTMARE-SEED-ROADMAP.md`.

The recommendations below must be reconciled with current repository state before implementation. Lore-sensitive work still requires primary-text research under `docs/LORE-SOURCE-POLICY.md`.

## Executive conclusion

The project is currently a capable custom Shadow Slave Java core surrounded by an unbuilt modpack layer.

It has three distinct products:

1. a completed and frozen vanilla datapack used as a behavioural and migration reference;
2. a NeoForge Java core with persistent Soul identity, migration, networking, UI, per-player Nightmare ownership and one playable scenario;
3. a planned Nightmare Spell modpack whose directory currently contains design documentation only—there is no manifest, dependency lock, adapter implementation, packaged client or server distribution.

The strongest long-term architecture is:

> The custom Java core owns Shadow Slave identity, progression and lifecycle; curated dependencies supply generic execution, content and presentation through optional adapters.

The modpack should not become a collection of quest scripts that merely resembles Shadow Slave. Soul state, progression, Aspects, Flaws, Nightmare ownership, outcomes, appraisal and migration must remain authoritative in the custom core.

The immediate priority is not adding more mods. It is closing the durable Nightmare-completion gap and establishing interfaces through which dependencies can safely provide abilities, equipment, creatures, structures, loot and presentation.

## Current project audit

| Area | Current state | Roadmap assessment |
| --- | --- | --- |
| Vanilla datapack | Released and frozen; one active First Nightmare at a time | Keep for migration, regression testing and behavioural comparison |
| Soul foundation | Persistent schema-v2 Soul data, identity, ranks, migration metadata and synchronization | Strong foundation; extend rather than replace |
| Migration | Transactional datapack read, translation, write-back verification and rollback | Mature enough to retain; needs a backed-up real-world migration trial |
| Nightmare ownership | Persistent registry, separate player slots, exact entity ownership and recovery | Structurally sound; physical multiplayer proof remains incomplete |
| Nightmare completion | One handcrafted scenario with direct campfire completion and fixed appraisal | Prototype wiring, not a reusable scenario engine |
| Powers | One custom ability, Kindle, and one Flaw, Cold Ash | Needs a general provider and execution contract |
| Progression | Uninfected -> Carrier -> Aspirant -> Dreamer/Sleeper | Natural infection, Awakening, Dream Realm and later ranks are absent |
| Modpack | Design-only | Must be bootstrapped as an independently buildable product |
| Public Java release | None | Correct decision; lifecycle and comparison gates are not yet satisfied |

The playable core already contains essential seams: attachments, command registration, networking, Nightmare event handling and preview power execution.

Several status surfaces describe different historical stages. Before feature work, reconcile current truth so new sessions do not follow old PR-specific instructions.

## Target architecture

```text
Shadow Slave Core
├── Soul domain and persistence
├── Aspect, Flaw and Attribute identities
├── Progression and appraisal
├── Nightmare and Seed lifecycle
├── Server-authoritative ability contracts
├── Migration
├── Networking
└── Soul UI

Integration Layer
├── AbilityProvider
├── EquipmentProvider
├── CreatureProvider
├── StructureProvider
├── LootProvider
└── Optional quest/guide bridge

Nightmare Spell Pack
├── manifest and locked dependencies
├── configs and defaultconfigs
├── pack tuning scripts
├── resource and data packs
├── provider-specific adapters
├── onboarding/guide content
├── client performance layer
└── server package and test evidence
```

### Non-negotiable ownership rules

The save should store internal identities such as:

```text
shadowslave:aspect/last_light
shadowslave:ability/kindle
shadowslave:flaw/cold_ash
```

It must not store only an external spell, quest or accessory identifier.

When an optional integration is absent:

- Soul identity still loads;
- the Aspect and Flaw remain intact;
- the ability reports that its execution provider is unavailable;
- no state is silently rerolled or deleted;
- reinstalling the provider can restore execution without rewriting permanent identity.

This should become an automated contract before the first dependency adapter is accepted.

# Prioritized milestone roadmap

## Milestone 0 — Establish one truthful baseline

### Work

- reconcile `PROJECT-STATUS.md`, `CURRENT-PREVIEW-SUMMARY.md`, `ISSUES.md`, `GPT_HANDOFF.md`, `mod/IMPLEMENTATION-STATUS.md` and the transition plan;
- remove stale descriptions of merged work as an active preview PR;
- clearly distinguish unit, storage/network integration, physical-client, dedicated-server and manual evidence;
- decide the next development version after `0.1.0-preview.2`;
- create one issue/milestone structure grouped by core, scenario, modpack, integration, content, testing and lore;
- keep Issue #20 framed as a frozen-datapack limitation rather than a Java ownership defect.

### Exit gate

One current-status document is authoritative and all active administrative documents agree with it.

## Milestone 1 — Make Nightmare completion crash-recoverable

This is the highest-priority correctness task.

The current success sequence can consume the active Nightmare instance before permanent appraisal state is durably recoverable. A crash in that interval can leave an Aspirant with neither an active instance nor an appraisal recovery record.

### Recommended model

Add a persisted lifecycle transaction, outbox or equivalent recovery record:

```text
ACTIVE
-> RESOLUTION_RECORDED
-> OUTCOME_COMMITTED
-> APPRAISAL_PENDING
-> APPRAISAL_COMMITTED
-> TEARDOWN_COMMITTED
```

Every phase must be:

- persisted;
- idempotent;
- keyed by Nightmare instance and player;
- safe to replay after restart;
- isolated from every other player.

The exact order may differ, but the runtime must always retain enough durable evidence to finish or safely recover the operation.

### Required tests

- deterministic fault injection after every durable phase;
- restart during appraisal;
- restart during teardown;
- duplicate event delivery;
- stale transaction replay;
- a newer instance owned by the same player;
- two players completing near-simultaneously;
- exactly one appraisal and exactly one teardown per success;
- zero appraisal for death, technical recovery and admin abort.

### Exit gate

Issue #34 is closed with deterministic fault-injection evidence, not a timing-based process-kill test.

## Milestone 2 — Build the reusable Nightmare completion engine

Future completion must separate:

1. global scenario resolution;
2. each challenger’s outcome;
3. teardown;
4. appraisal;
5. Seed consequences.

### Domain objects

Implement or formalize:

- `NightmareDefinition`;
- `ScenarioEvent`;
- `ResolutionGraph`;
- `TerminalResolution`;
- `ChallengerOutcome`;
- `AppraisalRecord`;
- persisted lifecycle phase;
- scenario evidence ledger.

### Refactor The Last Signal

Turn the existing campfire interaction into one event rather than direct success.

Suggested event chain:

```text
REACHED_WATCH
-> DISCOVERED_SIGNAL_PURPOSE
-> RECOVERED_FUEL
-> PURSUER_CONFRONTED / EVADED
-> SIGNAL_CHOICE_MADE
-> TERMINAL_RESOLUTION
```

Create at least two materially different DESIGN endings, for example:

- transmit the warning;
- suppress or falsify the signal;
- abandon the post with recovered knowledge.

Scenario resolution determines what happened globally. `ChallengerOutcome` independently determines whether each participating player survived, remained eligible and completed the Nightmare.

### Exit gate

The automated suite proves:

- a creature can die without ending the scenario;
- a non-combat event can end it;
- missing prerequisites reject a terminal interaction;
- two paths reach different named endings;
- appraisal happens only after completion;
- restart does not duplicate resolution, appraisal or teardown;
- separate players can receive different outcomes from one shared resolution.

## Milestone 3 — Bootstrap the actual modpack product

Create the first real `modpack/` implementation:

```text
modpack/
├── pack.toml
├── index.toml
├── mods/
├── config/
├── defaultconfigs/
├── kubejs/
│   ├── startup_scripts/
│   ├── server_scripts/
│   └── client_scripts/
├── resourcepacks/
├── server-overrides/
├── dependency-review/
├── test-results/
└── README.md
```

Use a source-controlled manifest rather than committing downloaded dependency JARs by default.

### CI outputs

```text
nightmare-spell-modpack-<version>.mrpack
nightmare-spell-server-<version>.zip
dependency-report.json
SHA256SUMS.txt
```

### Build matrix

Every pack commit should test:

- clean client installation;
- clean dedicated-server installation;
- server boot without client-only classes;
- client joining the server;
- world creation;
- Shadow Slave core registration;
- exact dependency manifest;
- configuration loading;
- restart using the same world;
- optional integration removed;
- no duplicate mods or mixed versions.

### Exit gate

A clean machine can install the pack, start the server package, join it, open the Soul screen and complete the existing preview loop.

## Milestone 4 — Add a minimal dependency layer

Do not begin with a large content pack. Begin with dependencies that provide clear infrastructure value.

Suggested categories for review:

- pack scripting and recipe/tag tuning;
- loot adaptation;
- accessory/equipment execution;
- recipe browsing;
- guide/codex presentation;
- profiling and performance diagnostics.

Each dependency must receive a review covering:

- exact Minecraft and NeoForge version;
- client/server side requirements;
- direct and transitive dependencies;
- licence and distribution constraints;
- source and issue tracker availability;
- active maintenance signal;
- persistent save data written;
- removal behaviour;
- dedicated-server result;
- performance impact;
- configuration and scripting surfaces;
- API versus reflection/mixin integration;
- replacement plan if abandoned.

## Milestone 5 — Prove the external ability-provider architecture

A large spell mod should be an experiment, not an initial baseline assumption.

### First adapter spike

Map exactly one internal ability:

```text
shadowslave:ability/kindle
        ↓
OptionalAbilityProvider
        ↓
external visual/casting execution
```

Prove that:

- the core validates whether Kindle is owned;
- cooldown is core-owned;
- external mana cannot silently become Soul Essence;
- removing the provider does not corrupt Soul data;
- the Soul screen still shows Last Light and Kindle;
- the ability reports an unavailable provider;
- reinstalling the provider restores execution without migration.

Do not adapt a broad spell catalogue until this single ability passes.

### Exit gate

The same save boots successfully with and without the integration mod.

## Milestone 6 — Complete the first comparable modpack slice

Only after the core and adapters are sound should the pack receive broader content.

### Required slice

- configurable infection event;
- Carrier onboarding and escalating symptoms;
- First Nightmare trigger;
- historical role and temporary context owned by the instance;
- multi-step Last Signal;
- at least two terminal resolutions;
- custom or adapted Nightmare creature pressure;
- per-player completion and failure;
- durable appraisal;
- permanent Aspect and Flaw;
- one integrated ability;
- one reliable permanent drawback;
- Soul screen;
- relog, restart and two-player isolation;
- legacy datapack migration;
- missing-provider recovery.

This should be judged against `shared-test-spec/VERTICAL-SLICE.md`, not against a looser “seems playable” standard.

### Content restraint

Before this gate, avoid:

- several magic systems;
- multiple large structure mods;
- broad technology progression;
- dozens of creature packs;
- a giant quest tree;
- multiple independent equipment systems;
- procedurally generated Aspects before one complete appraisal pipeline works.

The purpose of `modpack-v0.1.0` is to compare architecture and development velocity, not to become a kitchen-sink release.

## Milestone 7 — Expand Shadow Slave progression

After the comparison slice is accepted:

### Soul and identity

- persistent `CoreState`;
- Soul Essence and saturation model;
- data-driven `AspectInstance`;
- data-driven `FlawInstance`;
- supernatural Attributes distinct from Minecraft attributes;
- progression history;
- appraisal evidence and provenance.

### First Nightmare depth

- historical body and provisional inventory;
- multiple roles;
- richer NPC and faction state;
- custom Nightmare Creature AI;
- death consequences and corpse-Gate placeholder or verified implementation;
- role deviation and appraisal evidence.

### Post-First-Nightmare progression

- Sleeper survival period;
- persistent Dream Realm entry;
- Dream Anchor/Gateway model;
- first successful return;
- actual Awakening;
- conscious Essence use;
- separate Soul Rank and Aspect Rank advancement.

Do not extend the old one-integer or fixed-class prototype abstractions. Spell state, Soul Rank, Aspect Rank, core state, Attributes, Memories and Echoes require separate models.

## Milestone 8 — Dream Realm, Memories, Echoes and Seeds

This is a later product phase, not part of the first modpack release.

### Dream Realm

- persistent regions with distinct environmental rules;
- Anchor and Gateway travel;
- waking-world versus Dream-Realm body rules;
- server restart and stranded-player recovery;
- region-based progression rather than one universal dark biome.

### Memories

- internal Memory identity and provenance;
- rank/tier and abilities;
- Soul storage and summoning;
- optional accessory-backed active equipment;
- transfer and ownership rules;
- provider-independent save data.

### Echoes

- separate Echo records and summoned entity ownership;
- no generalization of Sunny-specific Shadows into the ordinary Echo system;
- persistent damage, dismissal and death rules.

### Seeds

- `SeedRecord`;
- discovery and eligibility;
- relationship to `NightmareDefinition`;
- shared multiplayer Nightmare instances;
- opposing roles;
- terminal resolution and independent challenger outcomes;
- verified post-resolution Seed lifecycle.

The Seed must not be implemented as “click or destroy a block to conquer it.” Nightmare resolution drives its outcome.

# Recommended first issue queue

## P0 — correctness and truth

1. Reconcile current status documents after the preview merge.
2. Implement persisted Nightmare completion/appraisal transaction recovery.
3. Add deterministic lifecycle fault injection.
4. Execute and record physical relog, restart and two-player procedures.
5. Test migration on a backed-up real datapack world.

## P1 — reusable architecture

6. Introduce `ScenarioEvent`.
7. Introduce `ResolutionGraph` and named terminal resolutions.
8. Separate `ChallengerOutcome` from `AppraisalRecord`.
9. Refactor The Last Signal into a multi-step conflict.
10. Add a second terminal resolution.
11. Define the optional `AbilityProvider` service interface.
12. Add missing-provider persistence tests.

## P1 — modpack bootstrap

13. Initialize a source-controlled manifest and exact dependency lock.
14. Add `.mrpack` and server-package CI.
15. Add dependency licence/removal/side review templates.
16. Establish the minimal infrastructure dependency set.
17. Add client and dedicated-server pack smoke tests.
18. Record startup time, memory and tick profiling baselines.

## P2 — integrations and content

19. Add an equipment-provider spike.
20. Add the single Kindle external-provider spike.
21. Add a prototype guide/codex.
22. Add configurable natural infection.
23. Add historical role/body/inventory model.
24. Replace the vanilla Husk placeholder with the accepted creature implementation.

# Product gates

## Core beta gate

- crash-recoverable success;
- exactly-once appraisal and teardown;
- physical relog/restart proof;
- physical two-player isolation;
- real-world migration proof;
- current documentation.

## Modpack development gate

- reproducible manifest;
- clean client and server installs;
- minimal dependency baseline;
- existing Java preview loop works unchanged;
- missing optional provider does not corrupt a save.

## Architecture comparison gate

Both standalone and modpack paths must prove the same accepted vertical slice. Score them on:

- lore identity;
- correctness;
- multiplayer;
- performance;
- maintainability;
- content development speed;
- dependency risk;
- installation;
- migration;
- user experience.

Do not compare a polished content-heavy pack against a bare standalone technical prototype.

## Public release gate

- complete accepted slice;
- no known progression-loss window;
- repeatable dedicated-server tests;
- migration backup/recovery guide;
- exact dependency and licensing record;
- performance profile;
- accessible onboarding;
- versioned client and server packages;
- owner playthrough and review evidence.

# Recommended direction

```text
Repository truth
-> crash-safe Nightmare transaction
-> reusable resolution/outcome/appraisal engine
-> modpack manifest and packaging shell
-> minimal infrastructure dependencies
-> one optional ability adapter
-> comparable vertical slice
-> architecture scorecard
-> broader content and progression
```

Do not start the next phase by installing a large spell mod and several content mods. That would create visible progress while locking the project into integrations before the core has defined how abilities, equipment, outcomes and missing providers behave.

The Shadow Slave core is already strong enough to be the platform. The roadmap should now make that platform durable and extensible, then let the modpack prove which external systems genuinely improve it.
