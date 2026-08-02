# Java core implementation status

## Package identity

- Minecraft: `1.21.1`
- NeoForge: `21.1.244`
- Java: **JDK** `21`
- ModDevGradle: `2.0.143`
- Gradle wrapper: `9.2.1`
- Current main: `c3ffcd9c3f6139817fe84ef3c81d94ceafdda4e3`
- Current build: `0.1.0-preview.2`
- Runtime source: `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`
- Public Java release: none

## Verification state

Claude independently verified the merge result:

- validator clean;
- frozen-datapack lifecycle harness 32/32;
- Flaw harness 39/39;
- disconnect/reconnect trial-lock regression PASS with exit 0, repeated twice;
- cleanup restored the global lock to 0 and left no stray trial creature;
- Java clean build with 35 tests and 0 failures;
- physical-client and dedicated-server smokes passed through `mod/verify-smoke.sh`.

Artifact:

```text
shadowslave-0.1.0-preview.2.jar
SHA-256 48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

The remaining evidence boundary is Andrew's real-client play/feel pass, not machine verification.

## Implemented

### Soul and identity

- codec-backed schema-v2 `SoulData` player attachment copied across death;
- explicit schema-1 migration and fail-closed invalid/future schema rejection;
- invalid Soul combinations returned as codec errors rather than thrown load exceptions;
- Uninfected, Carrier, Aspirant, and Dreamer/Sleeper states;
- Nightmare Spell versus natural-awakening path field;
- optional Soul Rank before the First Nightmare;
- independent Aspect Rank;
- persistent paired `SoulIdentityData` records;
- persistent imported-identity compatibility metadata;
- post-First-Nightmare Spell states retain permanent Aspect/Flaw identity;
- server-owned mutation and synchronization boundaries;
- bounded client snapshots and expanded O-key Soul screen.

### Live datapack migration

- direct scoreboard/tag reader over frozen datapack evidence;
- deliberate absent-score sentinel handling;
- four reader tests around absence versus explicit zero;
- rejection of explicit zero, unsupported, inconsistent, or active-Nightmare state;
- completed players require retained Carrier evidence;
- generated identities require matching mechanics tags;
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
- exact-UUID entity ownership and shared lifecycle teardown;
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

## Open engineering issues

- **#20:** the frozen datapack's supported one-slot path is verified, but its command-era objective still uses global `ss_creature` selectors. A manually introduced unrelated tagged entity can affect it. Java does not share this limitation.
- **#29:** `PreviewPowerData` must adopt a non-throwing persisted-codec construction path, followed by malformed-input regression coverage across every registered persisted attachment.

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

`docs/NIGHTMARE-SEED-ROADMAP.md` is binding. The current campfire interaction is a development event, not the final completion model. Future work separates:

1. central-conflict terminal resolution;
2. per-challenger survival/eligibility and outcome;
3. teardown and return;
4. appraisal/progression;
5. Seed post-resolution lifecycle.

Exact Seed behaviour requires renewed primary-novel verification before implementation.

## Local commands

```bash
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
./mod/gradlew -p mod runClient
./mod/gradlew -p mod runServer --no-daemon
```

Do not use bare `runClientSmoke`/`runServerSmoke` exit codes as a gate. Use `mod/verify-smoke.sh`, which checks the accepted readiness markers.
