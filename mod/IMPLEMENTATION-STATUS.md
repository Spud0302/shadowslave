# Java core implementation status

## Package identity

- Minecraft: `1.21.1`
- NeoForge: `21.1.244`
- Java: **JDK** `21`
- ModDevGradle: `2.0.143`
- Gradle wrapper: `9.2.1`
- Stable main: `0.1.0-alpha.4`, Claude-verified
- Active preview candidate: `0.1.0-preview.2` on PR #19
- Runtime source: `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`
- Public Java release: none

## Verification state

GitHub Actions `Java core` run 34 / ID `30686670446` passed:

- Gradle wrapper validation;
- compilation and expanded unit tests;
- physical-client startup marker;
- dedicated-server ready marker;
- JAR packaging;
- artifact upload.

Artifact ID `8814240590`; checksums are in `docs/PLAYABLE-PREVIEW-PROVENANCE.md`.

This is corrected **Java automated evidence**, not a completed Claude/player verdict. The frozen-datapack Mineflayer regression is a separate pending gate.

## Corrected persistence invariants

Issues #22–#25 are addressed in the candidate:

- `SoulData.CODEC` decodes through `StoredSoulData` so invalid stored combinations become `DataResult.error` rather than raw exceptions;
- negative, zero, unsupported, and future schema versions reject;
- schema 1 migrates explicitly to schema 2;
- schema 2 is validated as stored and does not receive legacy repairs;
- Nightmare-Spell states at or beyond Dreamer require paired Aspect, Aspect Rank, and Flaw identity;
- completed frozen-datapack imports require the retained Carrier tag;
- generated two-digit identities require their matching mechanics tags.

## Implemented in the preview

### Soul and identity

- codec-backed schema-v2 `SoulData` player attachment copied across death;
- Uninfected, Carrier, Aspirant, and Dreamer/Sleeper stages;
- Nightmare Spell versus natural-awakening path field;
- optional Soul Rank before the First Nightmare;
- independent Aspect Rank;
- persistent paired `SoulIdentityData` records;
- persistent imported-identity compatibility metadata;
- server-owned mutation and synchronization boundaries;
- bounded client snapshots and expanded O-key Soul screen.

### Live datapack migration

- direct scoreboard/tag reader over frozen datapack evidence;
- deliberate absent-score sentinel handling;
- rejection of explicit zero, unsupported, inconsistent, or active-Nightmare state;
- pure translation through exact mappings;
- provisional Java writes;
- exact Soul and identity read-back;
- migration marker only after verification;
- rollback of Java attachments on failure;
- no deletion of legacy scores or tags;
- operator command `/shadowslave migrate_datapack`.

### Nightmare lifecycle

- persistent Overworld `NightmareRegistryData`;
- one active instance per owner UUID;
- separate scenario slots;
- stored return position/dimension, role, scenario, altar, owned pursuer, and recovery data;
- one entry choke point in `NightmareService.tryEnter`;
- owned-entity and registry teardown shared by lifecycle outcomes;
- bundled Nightmare dimension and biome;
- reconnect resume or explicit technical recovery;
- distinction between ordinary death and technical/admin recovery.

### Playable vertical slice

- DESIGN scenario **The Last Signal**;
- DESIGN role **last watchkeeper**;
- optional combat pressure from one owned vanilla Husk placeholder;
- central conflict resolved by restoring the signal fire;
- fixed DESIGN appraisal;
- persistent Aspect **Last Light**, Awakened Aspect Rank;
- server-owned ability **Kindle** and cooldown;
- persistent Flaw **Cold Ash**, applying Weakness in water/rain/bubbles;
- player onboarding, inspection, recovery, and reset commands.

## Current commands

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

Operator-only commands:

```text
/shadowslave migrate_datapack
/shadowslave infect
/shadowslave begin_first_nightmare_test
/shadowslave complete_first_nightmare_test
/shadowslave reset
```

## Deliberately not implemented

- natural infection and exhaustion/sleep progression;
- complete historical body, inventory, or provisional role-power replacement;
- custom Nightmare Creature AI;
- corpse Gate consequences;
- procedural or canon-claimed appraisal;
- multiple terminal resolutions or a `ResolutionGraph`;
- complete mechanics for every imported Aspect/Flaw;
- later Seeds, Dream Realm systems, Memories, Echoes, Gates, or later ranks;
- modpack adapters and manifest;
- public `mod-v0.1.0` release.

## Future architecture

`docs/NIGHTMARE-SEED-ROADMAP.md` is binding. The current campfire click is a development event, not the final completion model. Future work separates:

1. central-conflict terminal resolution;
2. per-challenger survival/eligibility and outcome;
3. teardown and return;
4. appraisal/progression;
5. Seed post-resolution lifecycle.

Exact Seed behaviour requires renewed primary-novel verification before implementation.

## Local commands

```bash
./mod/gradlew -p mod build
mod/verify-smoke.sh
./mod/gradlew -p mod runClient
./mod/gradlew -p mod runServer --no-daemon
```

Do not use bare `runClientSmoke`/`runServerSmoke` exit codes as a gate. Use `mod/verify-smoke.sh`, which checks the same readiness markers as CI.
