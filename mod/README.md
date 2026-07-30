# Standalone/shared Shadow Slave Java core

This directory contains the canonical Java core used by the standalone track and, where practical,
by the Nightmare Spell modpack track.

## Current state — `0.1.0-alpha.4`

Implemented:

- NeoForge 1.21.1 / Java 21 workspace and committed wrapper;
- persistent schema-v2 Soul attachment;
- lore-aligned Uninfected -> Carrier -> Aspirant -> Dreamer progression model;
- separate Soul Rank and Aspect Rank;
- server-authoritative mutation service;
- limited server-to-client snapshots;
- read-only O-key Soul screen;
- alpha-schema compatibility;
- pure fail-safe datapack migration translator and fixtures;
- unit, packaging, client-startup and dedicated-server gates.

**Status:** CI-green, but not yet independently verified by Claude. Issue #16 is the blocking gate.
This is not a public release and does not yet include a playable Java Nightmare.

## Lore model

```text
Uninfected — no Soul Rank
  -> Carrier — no Soul Rank
  -> Aspirant — Dormant Soul Rank, temporary role belongs to NightmareInstance
  -> Dreamer/Sleeper — Dormant Soul Rank, permanent Aspect + Aspect Rank + Flaw
```

Mundane is descriptive rather than Rank zero. Aspect Rank is independent of Soul Rank. Project
appraisal algorithms are design unless canon explicitly defines a rule.

## Smoke commands

```text
/shadowslave soul
/shadowslave soul_screen
/shadowslave infect
/shadowslave begin_first_nightmare_test
/shadowslave complete_first_nightmare_test
/shadowslave reset
```

These bypass natural gameplay and exist only to test architecture.

## Authority rules

- server owns every permanent mutation;
- client sends intent, never rank/identity values;
- UI is a snapshot, not save authority;
- commands, scenarios and integrations call services;
- one Nightmare service owns eligibility and teardown;
- external mods never become canonical identity storage;
- missing optional integrations cannot make Soul data undecodable.

## Persistence ownership

- player attachment: progression, path, rank/core, Aspect, Flaw, Attributes and personal history;
- Overworld/server SavedData: Nightmare registry, instance ownership, role/conflict state, return and recovery records;
- data components: generated item/Memory identity and charges;
- data resources/codecs: roles, scenarios, conflicts and compatible content definitions.

## First public target — `mod-v0.1.0`

The first public JAR requires:

1. Claude-verified current foundation;
2. safe live datapack import with read-back verification;
3. persistent per-player Nightmare instance ownership;
4. one historical role and central conflict;
5. one valid completion path and canonical death outcome;
6. appraisal revealing Aspect and Flaw;
7. real client and dedicated-server evidence;
8. no mandatory external content dependency.

## Next order

1. close Issue #16 with evidence/fixes;
2. live migration reader/writer;
3. Nightmare registry and lifecycle;
4. data-driven role/conflict prototype;
5. appraisal boundary;
6. shared vertical-slice verification;
7. release only when the slice is genuinely playable.

## Build

```bash
./mod/gradlew -p mod build
./mod/gradlew -p mod runClientSmoke --no-daemon
./mod/gradlew -p mod runServerSmoke --no-daemon
```
