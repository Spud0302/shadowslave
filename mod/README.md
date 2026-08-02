# Standalone/shared Shadow Slave Java core

This directory contains the canonical Java core used by the standalone track and, where practical, by the Nightmare Spell modpack track.

## Current state — `0.1.0-preview.2`

**Status:** merged into `main` and independently verified by Claude. Andrew's real-client play/feel pass remains pending. This is not a public release or a claim of feature completeness.

Verified JAR:

```text
shadowslave-0.1.0-preview.2.jar
SHA-256 48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

## Implemented

- NeoForge 1.21.1 / JDK 21 workspace and wrapper;
- persistent schema-v2 `SoulData` attachment;
- explicit schema-1 migration and fail-closed schema validation;
- Soul codec invariant failures returned as `DataResult.error`;
- post-First-Nightmare Spell states retain permanent Aspect/Flaw identity;
- persistent `SoulIdentityData`, imported identity metadata, and preview power state;
- lore-aligned Uninfected -> Carrier -> Aspirant -> Dreamer/Sleeper progression;
- separate Soul Rank and Aspect Rank;
- server-authoritative services and bounded client snapshots;
- expanded read-only Soul screen opened with **O** or `/shadowslave soul_screen`;
- live frozen-datapack score/tag reader;
- fail-closed absent-versus-explicit-zero handling and strict mechanics-tag validation;
- transactional import, exact read-back, migration marker, and rollback;
- persistent Overworld `NightmareRegistryData`;
- one active First Nightmare per player UUID and separate play-space slots;
- bundled Nightmare dimension;
- DESIGN scenario **The Last Signal** and role **last watchkeeper**;
- one shared lifecycle teardown for success, technical recovery, admin abort, and canonical death handling;
- fixed DESIGN appraisal **Last Light / Awakened Aspect Rank / Cold Ash**;
- server-owned **Kindle** cooldown and Cold Ash Weakness effect;
- unit, packaging, client-startup, dedicated-server, and frozen-datapack compatibility gates.

## Quick play

Follow [`PREVIEW-PLAY-GUIDE.md`](PREVIEW-PLAY-GUIDE.md).

```text
Press O
/shadowslave preview_begin
```

Reach and right-click the unlit soul campfire. Combat with the placeholder pursuer is optional. After appraisal, test:

```text
/shadowslave kindle
```

## Player commands

```text
/shadowslave soul
/shadowslave soul_screen
/shadowslave preview_begin
/shadowslave nightmare_enter
/shadowslave nightmare_status
/shadowslave nightmare_recover
/shadowslave kindle
/shadowslave preview_reset
```

Operator-only architecture/migration commands include `/shadowslave migrate_datapack`, `/shadowslave infect`, and the older transition test commands.

## Lore model

```text
Uninfected — no Soul Rank
  -> Carrier — no Soul Rank
  -> Aspirant — Dormant Soul Rank, temporary role on NightmareInstance
  -> Dreamer/Sleeper — Dormant Soul Rank, permanent Aspect + independent Aspect Rank + Flaw
```

Mundane is descriptive rather than Rank zero. The Last Signal, Last Light, Kindle, Cold Ash, and the fixed preview appraisal are **DESIGN**, not canonical events or formulas.

Future completion logic follows [`../docs/NIGHTMARE-SEED-ROADMAP.md`](../docs/NIGHTMARE-SEED-ROADMAP.md): scenario terminal resolution, per-challenger outcome, and appraisal are separate stages.

## Verification

Claude verified the merged result:

```text
validator                         clean
lifecycle harness                 32/32
Flaw harness                      39/39
disconnect/reconnect regression   PASS, exit 0, repeated twice
Java tests                        35, failures 0
physical-client smoke             PASS
dedicated-server smoke            PASS
```

Use [`../TESTING.md`](../TESTING.md) and [`../docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`](../docs/PLAYABLE-PREVIEW-TEST-MATRIX.md) for exact evidence and the pending manual pass.

## Authority rules

- the server owns permanent mutations, identity, cooldowns, and Nightmare ownership;
- the client sends intent, never rank or identity values;
- UI is a snapshot, not save authority;
- one service choke point owns entry and one lifecycle path owns teardown;
- temporary role/conflict state belongs to `NightmareInstance`;
- external mods never become canonical identity storage;
- project design is not presented as canon.

## Known limits

- no natural infection/exhaustion sequence;
- one handcrafted scenario and one terminal event only;
- no historical body/inventory replacement;
- vanilla Husk placeholder instead of custom Nightmare Creature AI;
- no corpse Gate;
- fixed rather than procedural appraisal;
- incomplete mechanics for imported identities;
- no later Seeds, Dream Realm, Memories, Echoes, or later-rank progression;
- no completed Andrew playthrough;
- issue #29 remains a low-severity malformed persisted-cooldown codec follow-up.

## Build and verification

```bash
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
./mod/gradlew -p mod runClient
```

Use JDK 21. Do not treat bare smoke-task exit codes as proof; use `mod/verify-smoke.sh` and its readiness markers.
