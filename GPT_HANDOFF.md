# GPT handoff — living checkpoint

**Read first in a new GPT session.**  
**Repository:** `Spud0302/shadowslave`  
**Current main baseline:** `5f8acf2b2e3b04198166592568dd885431a2a09f`  
**Current GPT branch:** `gpt/live-datapack-import`

## Binding owner directive for this batch

Read **`docs/PLAYABLE-PREVIEW-DIRECTIVE.md`** before continuing. It consolidates Andrew's current
instructions into one authority:

- finish live datapack migration first;
- continue through a coherent installable development-preview JAR;
- never assume lore—verify and label canon/inference/design/unknown;
- do not pause for Claude between packages;
- build tests and Claude criteria alongside the code;
- stop at the playable-preview artifact and bulk-review handoff unless Andrew interrupts.

## Project state

- Datapack `datapack-v1.0.0`: released, packaged and frozen.
- Java core: `0.1.0-alpha.4` on `main`.
- Java automated status: compile/tests/JAR/client/server CI green.
- Java human/agent status: **Claude-verified**; Issue #16 closed 2026-07-30.
- No blocking gate. Human tests are deferred evidence per **D2**, not merge gates.
- Modpack track: documented only; no manifest or dependencies committed.

All of that is now on `main`: the Weightless documentation reconciliation, Q5 (answered, lifecycle
contract restored), the removed root `server.log`, the alpha.4 verification, and the **D2** rewording
that turned human tests into deferred evidence. The current work begins on
`gpt/live-datapack-import` under the playable-preview directive above.

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
remains a live contract:

- one eligibility choke point;
- one teardown path for every exit reason;
- scenario-specific objectives behind an abstraction;
- evidence owned by the active Nightmare instance.

## Immediate action

Complete the live datapack migration reader/writer:

1. read immutable legacy evidence — **read the hazard note first**, see below;
2. call the accepted pure translator;
3. persist Java Soul/Aspect/Flaw records;
4. read them back and verify identity;
5. mark import complete;
6. retain legacy values until all verification succeeds.

Then continue through the remaining milestones in `docs/PLAYABLE-PREVIEW-DIRECTIVE.md` without waiting
for per-package Claude testing.

**Step 1 carries this project's most repeated bug.** An absent scoreboard value is not `0`.
`LegacyDatapackSnapshot` uses `0` to mean "no such score", and the translator reads `rankScore() == 0`
as "never completed a First Nightmare" — so a failed or unread lookup that falls through to `0` would
silently downgrade a completed Sleeper and, for a non-Carrier, skip their migration entirely and lose
the identity. Map absent to `0` deliberately and fail the import when a score cannot be read. Full
reasoning in **`docs/DATAPACK-MIGRATION.md` § "an absent score is not `0`"**.

Worth adding during this batch: a NeoForge GameTest that a Soul survives persistence/reload. It is the
only end-to-end gap left after the alpha.4 verification, and `mod/build.gradle` already declares
`gameTestServer`.

## Workflow reminders

- GPT does not write directly to or merge into `main`.
- Every GPT commit includes `Co-Authored-By: ChatGPT <gpt@openai.com>`.
- Claude receives one accumulated bulk review package at the playable-preview milestone; do not stop
  between intermediate packages waiting for Claude.
- CI is deliberately throttled; do not create per-commit workflow noise.
- Historical docs are annotated, not silently rewritten as though earlier beliefs never existed.
- Verify the Java smokes with `mod/verify-smoke.sh`, never the bare `runServerSmoke`/`runClientSmoke`
  tasks: those report `BUILD SUCCESSFUL` with exit `0` even when the server never starts.
- Verify the datapack with `cd testserver && npm run deploy && npm test`. The server loads a built zip,
  not the working tree, so testing without deploying silently exercises the previous build.
- A **JDK** 21 is required, not a JRE. A JRE fails in NeoForm's recompile with the misleading
  `error: release version 21 not supported`.
- Put every lore decision, test criterion, limitation and Claude handoff item in the repository; chat
  context alone is not durable project state.
