# GPT handoff — living checkpoint

**Read first in a new GPT session.**  
**Repository:** `Spud0302/shadowslave`  
**Current main baseline used here:** `6a87991353480035f4fe6da08c775cd87d0e81df`  
**Current GPT branch:** `gpt/admin-docs-current-state`

## Project state

- Datapack `datapack-v1.0.0`: released, packaged and frozen.
- Java core: `0.1.0-alpha.4` on `main`.
- Java automated status: compile/tests/JAR/client/server CI green.
- Java human/agent status: **Claude-verified**; Issue #16 closed 2026-07-30.
- No blocking gate. Human tests are deferred evidence per **D2**, not merge gates.
- Modpack track: documented only; no manifest or dependencies committed.

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

## Verification status — alpha.4 verified

Issue #16 is **closed**. Claude ran, locally rather than trusting workflow status:

```bash
./mod/gradlew -p mod build      # BUILD SUCCESSFUL, 14 tests, 0 failures
mod/verify-smoke.sh             # both smokes PASS on CI's log markers
python3 shadowslave/tools/validate.py
```

**Use `mod/verify-smoke.sh`, not the bare Gradle smoke tasks.** Those tasks are not pass/fail gates:
the dedicated server failed to start three times during this verification — a port clash with the
Mineflayer harness on 25565, then a stale `world/session.lock` — and Gradle reported `BUILD SUCCESSFUL`
with exit `0` every time. A JDK is required, not just a JRE; a JRE fails in NeoForm's recompile with the
misleading `error: release version 21 not supported`.

The real-client walkthrough below is **deferred, not performed** (**D2** — it judges presentation and
feel):

```text
O opens Soul screen
fresh -> Uninfected / no Rank
infect -> Carrier / no Rank
begin_first_nightmare_test -> Aspirant / Dormant
complete_first_nightmare_test -> Dreamer / Dormant + Aspect Rank + Flaw
reset -> Uninfected / no Rank
relog/restart -> persisted state
```

Nobody has run this. Deferred is not passed — do not let any document imply otherwise. It stays here
because it is still worth doing, and because step 8 (state surviving a relog) is the one item that is
genuinely end-to-end rather than cosmetic: its mechanism is unit-tested by `codecRoundTripsImportedIdentity`,
but the full round trip is not. `mod/build.gradle` already declares `gameTestServer`, so a NeoForge
GameTest could cover it without a human.

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
