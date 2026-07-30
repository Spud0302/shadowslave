# Standalone Shadow Slave Java mod

This directory is reserved for Path B: the standalone NeoForge implementation.

## Baseline

- Minecraft `1.21.1`
- NeoForge `21.1.x`
- Java `21`
- Gradle wrapper committed to the repository
- dedicated-server boot required from the first build

Use the official NeoForge 1.21.1 ModDevGradle/MDK template when scaffolding the workspace. Do not hand-copy a newer Minecraft template and attempt to downgrade mappings afterward.

## First release target — `mod-v0.1.0`

The first JAR proves architecture and migration, not content quantity.

Required:

1. main mod entry point and registration layout;
2. persistent, versioned `SoulData` attached to players;
3. global `NightmareRegistryData` stored as server/Overworld SavedData;
4. server-authoritative infection and Carrier transition;
5. basic client Soul screen populated by a server payload;
6. safe datapack import fixtures for Mundane, Carrier and Sleeper states;
7. one per-player test Nightmare instance with entry and teardown;
8. dedicated server run configuration and smoke test;
9. unit tests for codecs/migration and GameTests or integration tests for lifecycle boundaries;
10. no external magic/content dependency required to boot.

## Intended package structure

```text
src/main/java/dev/spud/shadowslave/
  ShadowSlaveMod.java
  api/
    soul/
    nightmare/
    ability/
  attachment/
  client/
    screen/
    state/
  command/
  compat/
  config/
  content/
  data/
  migration/
  network/
  nightmare/
    instance/
    objective/
    scenario/
  progression/
  soul/
    aspect/
    flaw/
    history/

src/main/resources/
  META-INF/neoforge.mods.toml
  assets/shadowslave/
  data/shadowslave/
```

## Authority rules

- server owns every Soul mutation;
- client payloads express intent, never rank or identity values;
- UI state is a snapshot, not the save authority;
- commands call services rather than modifying attachments directly;
- scenario objectives call `NightmareService.exit`, not their own cleanup;
- integrations adapt external mechanics behind internal IDs;
- missing optional mods must not make Soul data undecodable.

## Persistence split

### Player data attachment

Use for:

- Spell state;
- Soul Rank and core state;
- Aspect and Flaw instances;
- personal progression/history;
- player-specific resources and cooldowns that must persist.

### Server SavedData

Use for:

- active/recoverable Nightmare instance registry;
- instance IDs;
- scenario ownership and return records;
- recovery after restart;
- cross-dimensional state not owned by one loaded entity.

### Data components

Use for custom item state such as Memories, charges, binding or generated item identity when those systems begin.

## First implementation order

1. scaffold NeoForge project and verify `runClient`, `runServer`, `build`;
2. add codecs and immutable/safely mutable domain records;
3. register persistent player attachment;
4. implement repository/service access around the attachment;
5. add network version and Soul snapshot payload;
6. add basic Soul screen;
7. implement datapack migration reader and idempotency marker;
8. implement Nightmare registry and one test scenario;
9. run the shared vertical-slice tests that are applicable to `0.1.0`;
10. publish `shadowslave-mod-v0.1.0.jar` under tag `mod-v0.1.0`.

## Deferred until the foundation passes

- elaborate procedural Aspect generation;
- dozens of abilities;
- Memories and Echoes;
- Dream Realm progression and true Awakening;
- custom creature renderer/AI library selection;
- multi-loader support;
- public API stability guarantees.
