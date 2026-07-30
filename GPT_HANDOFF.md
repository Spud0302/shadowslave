
# GPT handoff — living checkpoint

**Read first in a new GPT session.**  
**Repository:** `Spud0302/shadowslave`  
**Current main baseline used here:** `e0850193d52c85b4f81e1115f908f9dbdb67d419`  
**Current GPT branch:** `gpt/admin-docs-current-state`

## Project state

- Datapack `datapack-v1.0.0`: released, packaged and frozen.
- Java core: `0.1.0-alpha.4` on `main`.
- Java automated status: compile/tests/JAR/client/server CI green.
- Java human/agent status: **not yet Claude-verified**.
- Blocking gate: GitHub Issue #16.
- Modpack track: documented only; no manifest or dependencies committed.
- No active Java feature PR should merge before #16 closes.

Claude subsequently committed documentation reconciliation for the retired Weightless mechanic,
raised Q5 about the missing Java Nightmare lifecycle map, and removed an accidentally committed
root `server.log`. This branch starts after those commits.

## What is implemented

1. NeoForge 1.21.1 / Java 21 workspace and wrapper.
2. Persistent schema-v2 `SoulData` attachment.
3. Lore-aligned Uninfected -> Carrier -> Aspirant -> Dreamer stages.
4. Independent Soul Rank and Aspect Rank.
5. Server-owned mutation service and client snapshot contract.
6. Read-only O-key Soul screen.
7. Schema-1-to-2 migration.
8. Pure fail-safe datapack translation for untouched, Carrier, generated and legacy completed identities.
9. Unit, client-startup, server-startup and JAR CI gates.

## What is not implemented

- live scoreboard/tag reader;
- verified persistence/read-back writer for imported Aspect/Flaw records;
- legacy cleanup;
- natural infection;
- persistent Nightmare registry/instances;
- playable Java First Nightmare;
- appraisal service, abilities or Dream Realm systems;
- modpack manifest/integrations.

## Blocking verification

Issue #16 requires Claude to independently run:

```bash
./mod/gradlew -p mod build
./mod/gradlew -p mod runClientSmoke --no-daemon
./mod/gradlew -p mod runServerSmoke --no-daemon
```

and test in a real client:

```text
O opens Soul screen
fresh -> Uninfected / no Rank
infect -> Carrier / no Rank
begin_first_nightmare_test -> Aspirant / Dormant
complete_first_nightmare_test -> Dreamer / Dormant + Aspect Rank + Flaw
reset -> Uninfected / no Rank
relog/restart -> persisted state
```

Do not claim this interaction passed until Claude records evidence.

## Lore rules now binding Java

- Mundane is descriptive, not Soul Rank zero.
- Carrier, Aspirant and Dreamer are separate states.
- Aspirant has a Dormant Soul Core but no permanent Aspect/Flaw before appraisal.
- Aspect Rank is independent of Soul Rank.
- a First Nightmare owns a historical role, situation, central conflict and valid resolutions;
- ordinary First-Nightmare failure is lethal; crash/admin recovery is technical;
- Aspect/Flaw appraisal algorithms are project design where canon gives no formula;
- novel mechanics outrank adaptation wording; manhwa visuals are secondary reference.

## Q5 answer

The Nightmare lifecycle mapping was dropped accidentally while restructuring the Java handoff. It
remains a live contract and is restored by this branch:

- one eligibility choke point;
- one teardown path for every exit reason;
- scenario-specific objectives behind an abstraction;
- evidence owned by the active Nightmare instance.

## Next action after #16

Build the live datapack migration reader/writer in a new `gpt/*` branch:

1. read immutable legacy evidence;
2. call the accepted pure translator;
3. persist Java Soul/Aspect/Flaw records;
4. read them back and verify identity;
5. mark import complete;
6. retain legacy values until all verification succeeds.

Then implement `NightmareRegistryData` and explicit per-player instance ownership.

## Workflow reminders

- GPT does not write directly to or merge into `main`.
- Every GPT commit includes `Co-Authored-By: ChatGPT <gpt@openai.com>`.
- Claude independently reviews and tests before merge.
- CI is deliberately throttled; do not create per-commit workflow noise.
- Historical docs are annotated, not silently rewritten as though earlier beliefs never existed.
