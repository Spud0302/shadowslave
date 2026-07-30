# Shadow Slave — two-track mod transition plan

**Owner decision:** test two Java-era paths against the same lore-aligned playable slice:

1. **Nightmare Spell modpack** — reuse mature mods for generic content and mechanics, with the Shadow Slave core filling the gaps;
2. **standalone Shadow Slave mod** — implement the important systems directly in Java.

This is a controlled comparison, not a commitment to maintain two permanent products feature-for-feature.

<!-- transition-current-status -->
## Current implementation status

- datapack `datapack-v1.0.0`: released and frozen;
- shared/standalone Java core: `0.1.0-alpha.4` on `main`;
- implemented: Soul persistence, lore schema, networking/UI, schema migration and pure datapack translation;
- blocking gate: Claude verification Issue #16;
- not implemented: live import, Nightmare SavedData/instances, playable conflict/appraisal;
- modpack track: design only.


`docs/JAVA-LORE-ALIGNMENT.md` is the architecture gate for both paths. The frozen datapack is a migration and regression reference, not a limit on what Java may model.

## 1. Why both paths are worth testing

The modpack path tests how quickly strong atmosphere, spell presentation, creatures, equipment, structures and exploration can be assembled from existing ecosystems.

The standalone path tests whether owning the architecture produces a meaningfully better Shadow Slave experience: personal Soul identity, lore-correct progression, Aspect/Flaw revelation, Nightmare ownership, networking, GUI and multiplayer behaviour without dependency constraints.

The expected long-term answer is probably a **hybrid**:

- Shadow Slave Java code owns identity, progression, appraisal and authoritative state;
- selected dependencies provide generic capabilities where they are genuinely better and stable;
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

Do not start with a multi-loader abstraction. One functioning NeoForge implementation is more valuable than several empty platform modules.

Official NeoForge references:

- <https://docs.neoforged.net/docs/1.21.1/gettingstarted/>
- <https://docs.neoforged.net/docs/1.21.1/networking/>
- <https://docs.neoforged.net/docs/1.21.1/datastorage/saveddata/>

## 3. Frozen datapack dependency

The Java work begins from the published datapack baseline:

- runtime version `1.0.0`;
- Git tag `datapack-v1.0.0`;
- release asset `shadowslave-v1.0.0.zip`;
- checksum and recorded test evidence.

The datapack contributes:

- migration fixtures;
- tested identity persistence;
- server-authoritative drawback lessons;
- release/install expectations;
- the broad uninfected -> Carrier -> First Nightmare -> Sleeper loop.

Java deliberately replaces datapack constraints such as score bands, four complete identity families, a shared boss, safe retry/ejection and command-specific mechanics.

## 4. Shared lore-aligned comparison slice

Both Path A and Path B implement:

```text
Uninfected / Mundane description
  -> infection event
Carrier
  -> supernatural exhaustion / forced Nightmare trigger
Aspirant with Dormant Soul Core
  -> individually owned First Nightmare
  -> assigned historical role/body
  -> reconstructed situation and central conflict
  -> meaningful choices and evidence
  -> conflict resolution and appraisal
Dreamer (Sleeper) with Dormant Soul Rank
  -> permanent Aspect identity + independent Aspect Rank
  -> permanent Flaw identity/effect
  -> server-synchronised Soul screen
```

The comparison is invalid if one path contains substantially more game than the other. Extra content can be disabled while measuring the shared slice.

Required behaviours are defined in `shared-test-spec/VERTICAL-SLICE.md`.

## 5. Shared canonical Java core

Both paths use one domain implementation.

```text
SoulData
  schemaVersion
  spellState/status
  awakeningPath
  soulRank?
  aspect?
  flaw?
  attributes[]
  memories[]
  echoes[]
  trueName?
  dreamAnchor?
  corruption
  progressionHistory

AspectInstance
  instanceId
  formalName?              # unknown remains unknown
  aspectRank               # independent from Soul Rank
  nature/source
  traits[]
  abilities[]
  evolutionHistory[]
  legacy/import metadata

FlawInstance
  instanceId
  formalName?              # effect label is not automatically a formal name
  effectDefinition
  parameters
  revelationEvidence[]
  legacy/import metadata

NightmareInstance
  instanceId
  scenarioId
  participants
  historicalRoles
  provisionalTrialContext
  lifecycleState
  centralConflictState
  possibleResolutions
  return/recovery records
  evidenceByPlayer
  ownedEntities
  createdAt
  technicalRecoveryPolicy
```

Carrier and Aspirant are not Soul Ranks. Mundane is not Rank zero. Dreamer/Sleeper has Dormant Soul Rank. Human title, Soul Rank, Aspect Rank and creature Class are separate concepts.

Temporary historical roles, trial bodies and provisional abilities belong to `NightmareInstance`, not permanent `SoulData`.

## 6. Shared service boundary

```text
SoulService
ProgressionService
AspectService
FlawService
AppraisalService
NightmareService
NightmareScenarioRegistry
AbilityService
MigrationService
SoulSyncService
```

No public service exposes raw NBT, command score names or dependency-specific spell IDs as its main domain API.

The client requests intent. The server validates ownership, state, cooldown and targets, mutates authoritative data and sends a deliberately limited snapshot.

## 7. Path A — Nightmare Spell modpack

Path A is a curated modpack using the same Shadow Slave core JAR.

### The core must own

- status and awakening path;
- persistent `SoulData`;
- Soul Rank and Aspect Rank;
- Nightmare selection, creation, entry, conflict, appraisal, exit and recovery;
- permanent Aspect and Flaw identity;
- evidence collection without claiming a canon generation formula;
- migration and progression history;
- Soul UI data and networking;
- compatibility adapters and server-side validation.

### Existing mods may provide

- spell visuals and generic spell execution;
- equipment/accessory slots;
- structures, creatures and world-generation content;
- quest presentation;
- recipes, loot tuning and rapid pack scripting;
- animation, sound and visual libraries;
- optional party or claims integration.

### Existing mods must not own

- the canonical Soul record;
- permanent Aspect or Flaw identity;
- authoritative Soul Rank or Aspect Rank progression;
- Nightmare instance lifecycle or appraisal;
- permanent character history.

If a dependency is removed, the player's identity remains readable and migratable. A missing integration may disable an adapter; it must not erase the identity that granted it.

### Initial candidate stack

This is a shortlist to evaluate, not a locked manifest:

| Role | Candidate | Reason to evaluate |
| --- | --- | --- |
| loader | NeoForge | 1.21.1 and dedicated client/server development |
| rapid non-authoritative glue | KubeJS | recipes, pack events and fast iteration |
| loot adaptation | LootJS | KubeJS-compatible loot modification |
| generic spell execution/presentation | Iron's Spells 'n Spellbooks | mature spell visuals and content |
| accessory slots | Curios API | configurable compatible equipment slots |
| in-game documentation | Patchouli | data-driven guide presentation |

Every dependency requires compatibility, licence, redistribution and modpack-permission review. Do not vendor restricted JARs into the repository; published packs should use authorised manifests.

### Path A first deliverable

`nightmare-spell-modpack-v0.1.0` contains:

- the smallest dependency set needed for the shared slice;
- the shared Shadow Slave core as a real Java mod;
- scripts only for rapidly changing non-authoritative glue;
- one external spell adapter for one Aspect ability;
- one reliable Flaw effect;
- one mod-provided encounter adapted into a historical central conflict;
- a reproducible `.mrpack` or equivalent manifest;
- a documented dedicated-server installation path.

## 8. Path B — standalone Shadow Slave mod

Path B owns gameplay implementation while still using vanilla/NeoForge resources for dimensions, tags, recipes, structures and world generation.

### Persistence

- player Soul state: codec-backed player attachment with explicit schema migration;
- cross-dimensional Nightmare registry/recovery: server or Overworld `SavedData`;
- item-specific state: vanilla data components;
- scenario/role/conflict definitions: data-driven codecs/resources where practical;
- every mutation: validated and synchronised through a service.

### Package layout

```text
mod/
  src/main/java/dev/spud/shadowslave/
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
    appraisal/
    progression/
    soul/
    client/
  src/main/resources/
    assets/shadowslave/
    data/shadowslave/
```

Client-only code remains isolated and must never load on a dedicated server.

### Path B first deliverable

`shadowslave-mod-v0.1.0` should:

- boot on physical client and dedicated server;
- persist lore-aligned schema-v2 Soul state;
- display a server-synchronised Soul screen;
- import datapack Carrier and Sleeper identities;
- implement explicit Carrier -> Aspirant -> Dreamer transitions;
- create and tear down a per-player test Nightmare instance;
- assign a historical role owned by the instance;
- resolve one central conflict and perform one appraisal;
- keep Aspect/Flaw mechanics simple until persistence and lifecycle are proven.

Do not begin with dozens of powers, custom mobs or full Dream Realm progression. The first Java release proves architecture, lore boundaries, migration and lifecycle.

## 9. Shared repository layout

```text
shadowslave/              frozen datapack source
mod/                      canonical Java core and standalone path
modpack/                  Path A manifest, config and scripts
integration-mod/          split only if dependency boundaries require it
shared-test-spec/         black-box scenarios shared by both paths
docs/
```

Start with one Java module under `mod/`. The modpack depends on that same JAR, preventing two competing implementations of Soul state.

## 10. Comparison scorecard

Score each path from 1–5 after completing the shared slice.

| Category | What to measure |
| --- | --- |
| lore identity | Does it feel purpose-built and respect the accepted lore gate? |
| development speed | Hours and code needed for the same accepted slice |
| correctness | lifecycle leaks, duplication, lost state, invalid progression |
| multiplayer | simultaneous Nightmares, reconnects, dedicated-server behaviour |
| performance | tick impact, memory, load time and network traffic |
| maintainability | ownership clarity, testability and upgrade effort |
| content velocity | effort to add a role, conflict, Aspect, Flaw, creature or area |
| dependency risk | abandoned mods, version lock, API churn and incompatibilities |
| distribution | licences, permissions, manifests and server installation |
| migration | preservation of datapack and future Java saves |
| user experience | installation, UI consistency and configuration burden |

Record evidence, not preference.

## 11. Decision gate

After both prototypes pass the shared slice, choose:

- **Modpack-led** — dependencies provide most gameplay cleanly and glue stays small;
- **Standalone-led** — integration compromises identity, stability or progression ownership;
- **Hybrid** — expected default: core owns Soul/Nightmare/progression while selected dependencies provide generic capabilities;
- **Stop an experiment** — a path cannot pass a hard gate without contradicting its premise.

Do not maintain two paths merely to protect sunk effort.

## 12. Version and package naming

```text
datapack-v1.0.0
mod-v0.1.0
modpack-v0.1.0
```

```text
shadowslave-v1.0.0.zip
shadowslave-mod-v0.1.0.jar
nightmare-spell-modpack-v0.1.0.mrpack
nightmare-spell-server-v0.1.0.zip
```

The datapack's `1.0.0` freezes a reference; it does not make the Java products complete.

## 13. Current sequence

1. close Claude verification Issue #16 with evidence or fixes;
2. implement live datapack reading, Java persistence and read-back verification;
3. retain legacy state until import is confirmed and idempotent;
4. implement `NightmareRegistryData` and explicit per-player instance ownership;
5. implement one data-driven historical role, central conflict and valid resolution;
6. add appraisal revealing one Aspect and Flaw through the shared core;
7. build the same accepted slice in the modpack track;
8. score both paths and choose standalone-led, modpack-led or hybrid.

## 14. Non-goals before the comparison gate

- every canonical Aspect or Flaw;
- full Dream Realm simulation;
- all Soul Ranks and creature Classes;
- a giant public modpack;
- copied novel prose or copyrighted artwork;
- multi-loader support;
- perfect procedural generation;
- treating a design algorithm as canon;
- full parity with every selected dependency's progression system.

The experiment exists to choose architecture with evidence. It is not the final content roadmap.
