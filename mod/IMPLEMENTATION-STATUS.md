# Java core implementation status

## Package identity

- Minecraft: `1.21.1`
- NeoForge: `21.1.244`
- Java: **JDK** `21` (a JRE cannot build: NeoForm recompiles with `javac`)
- ModDevGradle: `2.0.143`
- Gradle wrapper: `9.2.1`
- Development version: `0.1.0-alpha.4`
- Public Java release: none

## Gate status

The alpha.4 head is GitHub-CI green for compilation, unit tests, JAR packaging, physical-client
startup and dedicated-server startup, and is **Claude-verified**: Issue #16 is closed, with the build,
both startup smokes and the validator re-run locally rather than accepted from workflow status.

Nothing blocks the next Java feature merge. Real-client interaction evidence is **deferred, not
recorded** (owner decision **D2** in `docs/OPEN-QUESTIONS.md`) because it judges presentation and feel;
no document may present it as having passed.

## Implemented

- loadable common and physical-client mod entry points;
- persistent codec-backed `SoulData` player attachment copied across death;
- schema version 2 with legacy alpha decoding;
- Uninfected, Carrier, Aspirant and Dreamer stages;
- Nightmare Spell versus natural-awakening path field;
- optional Soul Rank before the First Nightmare;
- independent Aspect Rank;
- server-owned `SoulService` mutation boundary;
- server-to-client limited Soul snapshot;
- read-only Soul screen opened with O or `/shadowslave soul_screen`;
- login and mutation synchronization;
- command-driven development progression;
- pure datapack migration evidence snapshot, translator and validated plan;
- exact generated and legacy datapack Aspect/Flaw mappings;
- fail-closed rejection of active/inconsistent legacy state;
- deterministic/idempotent migration fixtures;
- wrapper validation, JUnit, JAR, client-smoke and server-smoke CI.

## Development commands

```text
/shadowslave soul
/shadowslave soul_screen
/shadowslave infect
/shadowslave begin_first_nightmare_test
/shadowslave complete_first_nightmare_test
/shadowslave reset
```

## Deliberately not implemented

- natural infection and exhaustion/sleep progression;
- live datapack scoreboard/tag reader;
- full imported `AspectInstance` and `FlawInstance` persistence/read-back;
- migration-complete marker and legacy cleanup;
- `NightmareRegistryData` / SavedData;
- active instance allocation, ownership, entry or teardown;
- historical-role/scenario/conflict resources;
- canonical death and technical recovery implementation;
- appraisal service or executable abilities;
- modpack adapters;
- public `mod-v0.1.0` release.

## Next package after verification

Add live migration without destructive cleanup:

1. read legacy evidence from a real player;
2. produce the accepted pure migration plan;
3. persist Java data;
4. read back and verify every identity field;
5. mark migration complete;
6. keep legacy values until success is proven.

Nightmare registry work begins after that persistence boundary is accepted.

## Local commands

```bash
./mod/gradlew -p mod build      # compile, unit tests, JAR
mod/verify-smoke.sh             # both startup smokes, pass/fail on CI's log markers
./mod/gradlew -p mod runClient  # interactive client
./mod/gradlew -p mod runServer --no-daemon
```

**Do not use the bare `runClientSmoke`/`runServerSmoke` tasks as a gate.** They report
`BUILD SUCCESSFUL` with exit `0` even when the server never starts — observed three times on
2026-07-30 (a port clash with the Mineflayer harness on 25565, then a stale `world/session.lock`).
`mod/verify-smoke.sh` greps for the same readiness markers CI does, resets the smoke world, sweeps
JVMs that survive its own kill, and defaults to port 25599 to avoid that clash.
