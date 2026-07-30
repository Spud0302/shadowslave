# Shadow Slave — two-track mod transition plan

**Owner decision:** after the datapack is frozen as its completed `1.0.0` behavioural reference, test two Java-era paths against the same playable slice:

1. **Nightmare Spell modpack** — reuse mature mods for generic content and mechanics, with a small custom Shadow Slave integration mod filling the gaps;
2. **standalone Shadow Slave mod** — implement the important systems directly in Java, preserving the datapack's proven contracts while replacing command machinery.

This is a controlled comparison, not a commitment to maintain two permanent products feature-for-feature.

## 1. Why both paths are worth testing

The two paths answer different questions.

The modpack path tests how much high-quality gameplay can be assembled quickly from existing ecosystems. It can prove atmosphere, content density, progression pacing, spell feel, equipment and exploration before we build every generic system ourselves.

The standalone path tests whether owning the architecture produces a meaningfully better Shadow Slave experience: personal Soul state, generated Aspects and Flaws, Nightmare ownership, progression, networking, GUI and multiplayer behaviour without dependency constraints.

The likely long-term answer is a **hybrid**:

- Shadow Slave Java code owns all identity, progression and authoritative state;
- carefully selected dependencies provide generic capabilities where they are genuinely better and stable;
- integrations remain optional where practical;
- no third-party mod becomes the canonical storage authority for a player's Soul.

## 2. Shared technical baseline

Both prototypes target:

- Minecraft Java Edition `1.21.1`;
- NeoForge `21.1.x`;
- Java `21`;
- Gradle;
- dedicated-server compatibility from the beginning;
- server-authoritative gameplay state;
- data-driven resources wherever Minecraft already has a strong JSON/resource system.

Do not start with a multi-loader abstraction. One functioning NeoForge implementation is more valuable than three empty platform modules.

Official NeoForge references:

- <https://docs.neoforged.net/docs/1.21.1/gettingstarted/>
- <https://docs.neoforged.net/docs/1.21.1/networking/>
- <https://docs.neoforged.net/docs/1.21.1/datastorage/saveddata/>

## 3. Frozen datapack dependency

The Java work must begin from a real frozen datapack release, not from whichever pre-release commit happens to be visible locally.

Required baseline:

- completed datapack runtime version: `1.0.0`;
- Git tag: **`datapack-v1.0.0`**;
- release asset: `shadowslave-v1.0.0.zip`;
- checksum file;
- final `CHANGELOG.md`, `README.md`, `TESTING.md`, `ISSUES.md` and `docs/JAVA-HANDOFF.md` in agreement;
- automated and human release gates recorded as passed.

The namespaced Git tag is required because historical prototype tags already occupy ordinary `v1.0.0` through `v1.4.9` and must remain untouched.

If Claude has completed the final datapack work locally but GitHub still shows an older `main`, push/merge/tag that work before declaring the Java migration baseline final.

## 4. Shared vertical-slice acceptance test

Both Path A and Path B must implement the same first comparison slice:

```text
Mundane
  -> first sleep
Carrier
  -> later sleep / deliberate bed interaction
First Nightmare instance
  -> central conflict
Sleeper / Dormant
  -> generated Aspect identity
  -> behaviour-derived Flaw identity
  -> Soul screen
```

The comparison is invalid if one path contains substantially more game than the other. Extra content can be disabled while measuring the shared slice.

### Required behaviours

1. first ordinary sleep marks the player but does not immediately consume the whole progression;
2. every Nightmare has explicit instance ownership and a single lifecycle service;
3. the First Nightmare can be exited by victory, ejection, death, disconnect recovery or administrative teardown without leaving state behind;
4. Aspect and Flaw identities persist across logout, death and server restart;
5. trial observations influence the Flaw family;
6. the client receives only the Soul state it needs to render;
7. the server validates every progression or ability request;
8. two players can be in separate Nightmare instances without sharing boss state, mobs or return locations;
9. the datapack importer preserves a completed datapack player's identity without rerolling it;
10. a dedicated server can complete the slice without client-only code crashing it.

## 5. Path A — Nightmare Spell modpack

Path A is a curated modpack plus a small custom mod, provisionally called `nightmare_spell_core`.

### The custom glue mod must own

- Spell state: untouched, Carrier, Dreamer/Sleeper and later progression;
- persistent `SoulData`;
- Nightmare selection, creation, entry, exit and recovery;
- behaviour evidence collection;
- Aspect and Flaw assignment;
- canonical identity IDs and migration;
- Soul UI data and networking;
- compatibility adapters for chosen content mods;
- server-side permission and validity checks.

### Existing mods may provide

- spell visuals and generic spell execution;
- equipment slots;
- structures, creatures and world-generation content;
- quest presentation;
- recipes, loot tuning and rapid pack scripting;
- animation, sound and visual libraries;
- optional party or claims integration.

### Existing mods must not own

- the canonical Soul record;
- Aspect or Flaw identity generation;
- authoritative Soul Rank progression;
- Nightmare instance lifecycle;
- datapack import completion markers;
- permanent character history.

If a dependency is removed, the player's Soul must remain readable and migratable. A missing integration may disable an ability adapter; it must not erase the identity that granted it.

### Initial candidate stack

This is a shortlist to test, not a locked manifest:

| Role | Candidate | Current reason to evaluate |
| --- | --- | --- |
| loader | NeoForge | official 1.21.1 support, Java 21, client/server run configurations |
| rapid pack scripting | KubeJS | 1.21.1 NeoForge support; recipes, items and server/world events |
| loot adaptation | LootJS | 1.21.1 NeoForge KubeJS addon for loot modification |
| generic spell content | Iron's Spells 'n Spellbooks | 1.21.1 NeoForge, client/server, strong spell presentation and content |
| accessory slots | Curios API | 1.21.1 NeoForge, configurable compatible equipment slots |
| in-game documentation | Patchouli | 1.21.1 NeoForge, data-driven guide content |

Every dependency still requires a compatibility, licence, redistribution and modpack-permission review before it enters a published manifest. In particular, do not vendor all-rights-reserved JARs into the repository. Published modpacks should reference authorised platform downloads through their manifest.

### Path A first deliverable

`nightmare-spell-modpack-v0.1.0` should contain:

- the smallest dependency set needed to exercise the comparison slice;
- `nightmare_spell_core` as a real Java mod, not only scripts;
- KubeJS only for rapidly changing pack glue that is not save-authoritative;
- one mapped Aspect ability using an external spell implementation;
- one mapped Flaw burden;
- one mod-provided creature or encounter adapted into a Nightmare objective;
- a reproducible `.mrpack` or equivalent manifest-based package;
- a server pack or documented dedicated-server installation path.

## 6. Path B — standalone Shadow Slave mod

Path B owns the gameplay implementation while continuing to use vanilla/NeoForge resource formats for dimensions, tags, recipes, structures and other data-driven content.

### Core domain model

```text
SoulData
  schemaVersion
  spellState
  soulRank
  coreState
  aspect
  flaw
  attributes[]
  memories[]
  echoes[]
  trueName?
  dreamAnchor?
  corruption
  behaviorProfile
  progressionHistory

AspectInstance
  instanceId
  formalName
  aspectRank
  nature
  archetype
  traits[]
  abilities[]
  evolutionHistory[]
  generationSeed
  importedFromDatapack

FlawInstance
  instanceId
  formalName
  family
  parameters
  causalEvidence[]
  importedFromDatapack

NightmareInstance
  instanceId
  scenarioId
  participants
  lifecycleState
  objectiveState
  returnLocations
  evidenceByPlayer
  ownedEntities
  createdAt
  recoveryPolicy
```

### Service boundary

```text
SoulService
ProgressionService
AspectService
FlawService
NightmareService
NightmareScenarioRegistry
AbilityService
MigrationService
SoulSyncService
```

No public service should expose raw NBT, command score names or dependency-specific spell IDs as its main domain API.

### Persistence

- player-specific Soul state: persisted entity/player data attachment with an explicit codec/schema version;
- global and cross-dimensional Nightmare registry/recovery state: server/Overworld `SavedData`;
- item-specific custom state: vanilla data components;
- scenario definitions and generated-content templates: data-driven codecs/resources where practical;
- every mutation goes through a service that can validate, persist and synchronise it.

### Networking

Use NeoForge payload registration and stream codecs.

- client requests intent, such as opening the Soul screen or activating an equipped ability;
- server validates state, cooldown, ownership and target;
- server mutates authoritative data;
- server sends a deliberately limited Soul snapshot to the owning client;
- never trust client-provided rank, Aspect, Flaw, cooldown or resource values.

### Package layout

```text
mod/
  build.gradle
  gradle.properties
  settings.gradle
  src/main/java/dev/spud/shadowslave/
    ShadowSlaveMod.java
    api/
    attachment/
    command/
    compat/
    config/
    content/
    data/
    migration/
    network/
    nightmare/
    progression/
    soul/
    client/
  src/main/resources/
    META-INF/neoforge.mods.toml
    assets/shadowslave/
    data/shadowslave/
  src/test/
```

Client-only code remains under a client package and must never be loaded by a dedicated server.

### Path B first deliverable

`shadowslave-mod-v0.1.0` should:

- boot on client and dedicated server;
- register persistent `SoulData`;
- display a basic server-synchronised Soul screen;
- import datapack Mundane/Carrier/Sleeper state and generated identities;
- implement the infection and Carrier transitions;
- create and tear down a per-player test Nightmare instance;
- leave Aspect/Flaw mechanics as simple adapters until the persistence and lifecycle foundation is proven.

Do not begin with dozens of powers, custom mobs or Dream Realm progression. The first Java release proves architecture and migration.

## 7. Shared repository layout

Keep the comparison in one repository until the experiment is complete:

```text
shadowslave/              frozen datapack source
mod/                      Path B standalone Java mod
modpack/                  Path A manifest, config and scripts
integration-mod/          optional Path A glue module if kept separate
shared-test-spec/         black-box acceptance scenarios shared by both paths
docs/
```

Recommended simplification: start with one Java module under `mod/` that contains the canonical Soul/Nightmare core and optional compatibility packages. The modpack can depend on that same JAR. Split a separate `integration-mod/` only when dependency boundaries prove it is useful.

That prevents the experiment from accidentally creating two competing implementations of Soul state.

## 8. Comparison scorecard

Score each path from 1–5 after completing the shared slice.

| Category | What to measure |
| --- | --- |
| Shadow Slave identity | Does it feel purpose-built rather than like unrelated mods beside each other? |
| development speed | Hours and code required to reach the same accepted slice |
| correctness | lifecycle leaks, duplication, lost state, invalid progression |
| multiplayer | simultaneous Nightmares, reconnects, dedicated server behaviour |
| performance | server tick impact, memory use, load time and network traffic |
| maintainability | clarity of ownership, testability, upgrade effort |
| content velocity | effort to add a new Nightmare, Aspect, Flaw, creature or area |
| dependency risk | abandoned mods, version lock, API churn and incompatibilities |
| distribution | licences, permissions, manifest support and server installation |
| migration | ability to preserve datapack and future mod saves |
| user experience | installation complexity, UI consistency and configuration burden |

Record evidence, not preference. A score requires a build, test result or observed workflow cost.

## 9. Decision gate

After both prototypes pass the shared slice, choose one of four outcomes:

### A. Modpack-led

Use when dependencies provide most needed gameplay cleanly and the glue mod stays small.

### B. Standalone-led

Use when integration compromises identity, stability, progression ownership or multiplayer correctness.

### C. Hybrid — expected default

The Shadow Slave mod owns Soul/Nightmare/progression systems; selected dependencies provide spells, accessories, content or presentation through explicit adapters.

### D. Stop an experiment

A path can be ended early if it cannot pass a hard gate without redesigning its premise. Do not keep both alive to protect sunk effort.

## 10. Version and package naming

Use product-qualified tags because the repository has historical tag collisions:

```text
datapack-v1.0.0
mod-v0.1.0
modpack-v0.1.0
```

Release artifacts:

```text
shadowslave-v1.0.0.zip
shadowslave-mod-v0.1.0.jar
nightmare-spell-modpack-v0.1.0.mrpack
nightmare-spell-server-v0.1.0.zip
```

The Java mod and modpack each begin their own pre-1.0 Pride Versioning line. The datapack's `1.0.0` does not make the Java mod complete; it only freezes the reference behaviour.

## 11. Immediate sequence

1. push and merge Claude's final datapack work to GitHub;
2. run the final fresh-world and human gates;
3. stamp runtime `1.0.0`, tag `datapack-v1.0.0`, publish ZIP and checksum;
4. create the NeoForge Java workspace under `mod/`;
5. implement the shared Soul data and migration core once;
6. use that core in the Nightmare Spell modpack prototype;
7. implement the standalone Nightmare path behind the same domain contracts;
8. complete the shared acceptance slice in both;
9. score them and choose the long-term architecture.

## 12. Non-goals for the experiment

Do not attempt all of the following before the comparison gate:

- every canonical Aspect or Flaw;
- full Dream Realm simulation;
- all Soul Ranks and Classes;
- a giant public modpack;
- copied novel prose or copyrighted artwork;
- multi-loader support;
- perfect procedural generation;
- full parity with every selected dependency's progression system.

The experiment exists to choose architecture with evidence. It is not the final content roadmap.
