# Standalone Shadow Slave Java mod

This directory contains Path B and the canonical Java core shared with the Nightmare Spell modpack.

## Baseline

- Minecraft `1.21.1`
- NeoForge `21.1.244`
- Java `21`
- committed Gradle wrapper
- physical-client and dedicated-server smoke gates
- server-authoritative state
- lore architecture defined by `../docs/JAVA-LORE-ALIGNMENT.md`

## Current development state — `0.1.0-alpha.3`

Implemented:

- NeoForge workspace and checked-in wrapper;
- persistent codec-backed `SoulData` attachment;
- schema migration from the alpha-1/alpha-2 model;
- server-owned Soul mutation service;
- server-to-client Soul snapshots;
- read-only Soul screen opened with **O**;
- command-driven development progression;
- unit tests, JAR packaging, physical-client startup and dedicated-server startup.

The lore-aligned schema separates:

- uninfected description from Soul Rank;
- Carrier, Aspirant and Dreamer stages;
- Nightmare Spell and natural awakening paths;
- Soul Rank from Aspect Rank;
- temporary Nightmare roles from permanent Soul identity.

## Development smoke commands

```text
/shadowslave soul
/shadowslave soul_screen
/shadowslave infect
/shadowslave begin_first_nightmare_test
/shadowslave complete_first_nightmare_test
/shadowslave reset
```

Expected sequence:

```text
Uninfected, no Soul Rank
  -> infect
Carrier, no Soul Rank
  -> begin_first_nightmare_test
Aspirant, Dormant Soul Rank
  -> complete_first_nightmare_test
Dreamer (Sleeper), Dormant Soul Rank, permanent Aspect + Aspect Rank + Flaw
```

These commands are architecture smoke tests. They are not the final natural infection or Nightmare gameplay.

## First public mod target — `mod-v0.1.0`

The first JAR proves architecture, lore boundaries and migration rather than content quantity.

Required:

1. persistent lore-aligned Soul schema;
2. server-synchronised Soul screen;
3. safe datapack and alpha-schema import fixtures;
4. global `NightmareRegistryData` stored as server/Overworld `SavedData`;
5. one individually owned First Nightmare instance;
6. explicit Carrier -> Aspirant -> Dreamer lifecycle;
7. one data-driven historical role and provisional trial context;
8. one central conflict with at least one valid resolution;
9. one appraisal boundary revealing Aspect and Flaw identity;
10. canonical death outcome plus clearly technical crash/admin recovery;
11. dedicated-server and physical-client verification;
12. no external content dependency required to boot.

## Intended package structure

```text
src/main/java/dev/spud/shadowslave/
  api/
  appraisal/
  attachment/
  client/
  command/
  compat/
  config/
  content/
  data/
  migration/
  network/
  nightmare/
    instance/
    role/
    scenario/
    conflict/
  progression/
  soul/
    aspect/
    flaw/
    history/
```

## Authority rules

- server owns every Soul mutation;
- client payloads express intent, never rank or identity values;
- UI state is a snapshot, not save authority;
- commands and integrations call services rather than modifying attachments;
- scenario conflicts call the central Nightmare lifecycle service;
- optional mods adapt mechanics behind internal IDs;
- missing optional mods cannot make Soul identity undecodable;
- project generation/appraisal algorithms are labelled design, not canon.

## Persistence split

### Player attachment

Use for permanent player-owned state:

- progression status and awakening path;
- optional Soul Rank and core state;
- Aspect and Flaw instances;
- Attributes, history and personal resources.

### Nightmare registry SavedData

Use for:

- active/recoverable instance IDs;
- scenario and historical-role ownership;
- central-conflict state;
- participants and return/recovery records;
- restart recovery;
- instance-owned entities and temporary context.

### Data components

Use for custom item state such as Memories, charges, binding and generated item identity.

## Next implementation order

1. verify `0.1.0-alpha.3` compile/tests/client/server gates;
2. manually check the O-key screen and four-stage command sequence;
3. implement datapack and alpha-schema migration fixtures;
4. add persistent Nightmare registry and instance records;
5. add data-driven historical role and central-conflict definitions;
6. implement canonical death plus technical recovery boundaries;
7. add the first appraisal service;
8. run the shared vertical-slice cases applicable to `0.1.0`;
9. publish `shadowslave-mod-v0.1.0.jar` when the accepted slice is genuinely playable.

## Deferred until the foundation passes

- elaborate procedural Aspect/Flaw revelation;
- many abilities;
- Memories and Echoes;
- first Dream Realm journey and true Awakening;
- custom creature renderer/AI library selection;
- multi-loader support;
- public API stability guarantees.
