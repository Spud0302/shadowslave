# GPT handoff — living checkpoint

**Read first in a new GPT session.**  
**Repository:** `Spud0302/shadowslave`  
**Current main baseline:** `2234efc327e60d13094517de305fd84d3c612e53`  
**Current GPT branch:** none open — `gpt/admin-docs-current-state` is merged. Branch from `main`.

## Project state

- Datapack `datapack-v1.0.0`: released, packaged and frozen.
- Java core: `0.1.0-alpha.4` on `main`.
- Java automated status: compile/tests/JAR/client/server CI green.
- Java human/agent status: **Claude-verified**; Issue #16 closed 2026-07-30.
- No blocking gate. Human tests are deferred evidence per **D2**, not merge gates.
- Modpack track: documented only; no manifest or dependencies committed.

All of that is now on `main`: the Weightless documentation reconciliation, Q5 (answered, lifecycle
contract restored), the removed root `server.log`, the alpha.4 verification, and the **D2** rewording
that turned human tests into deferred evidence. Nothing is in flight and nothing is open in
`docs/OPEN-QUESTIONS.md`.

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

## Open bug queue — Claude's 2026-08-01 test pass

> **Fixes reviewed on PR #19 (`gpt/live-datapack-import` @ `ad03e00`) — not merged.**
> One blocker: `regression_issue20.mjs` passes then never exits, so `npm test` hangs. One gap: #20 is
> still reproducible with a single player via a stray creature. Full argument, including two process
> points, in **`docs/reviews/2026-08-01-claude-review-of-preview-fixes.md`**.

Seven confirmed findings, **none fixed** (Andrew: find and record first, fix in a later pass). Full
reasoning, evidence and the false leads worth not re-chasing:
**`docs/reviews/2026-08-01-claude-test-findings.md`**.

| #                                                        | Finding                                                                | Area       | Severity |
| -------------------------------------------------------- | ---------------------------------------------------------------------- | ---------- | -------- |
| [#20](https://github.com/Spud0302/shadowslave/issues/20) | Concurrent trials block each other's victory and trap the player       | datapack   | **high** |
| [#21](https://github.com/Spud0302/shadowslave/issues/21) | That limitation is documented only in a non-authoritative section      | docs       | medium   |
| [#22](https://github.com/Spud0302/shadowslave/issues/22) | `SoulData` codec throws instead of returning `DataResult.error`        | Java       | **high** |
| [#23](https://github.com/Spud0302/shadowslave/issues/23) | Stored schema version discarded on decode; its validation unreachable  | Java       | medium   |
| [#24](https://github.com/Spud0302/shadowslave/issues/24) | Aspect/Flaw invariant enforced for `DREAMER` only                      | Java       | medium   |
| [#25](https://github.com/Spud0302/shadowslave/issues/25) | Migration accepts two inconsistent states the legacy path rejects      | Java       | low      |
| [#26](https://github.com/Spud0302/shadowslave/issues/26) | `test/reset` does not restore health, disarming reset-then-enter tests | test infra | low      |

Suggested order: **#22** first — it is the only one that can stop a player loading, and #23/#24 live
in the same `SoulData` constructor and codec, so all three at once avoids touching that file three
times. Then **#20**, then **#21**. Leave **#25**/**#26** for the live-reader work below.

`testserver/regression_issue20.mjs` reproduces #20 and **currently exits 1**. It is deliberately not
in `npm test` — a known-failing check in the release gate would break the gate. Wire it in once it
passes. Its header records three measurement mistakes that made the bug look fixed when it was not;
keep them if you edit it.

Two things the pass did **not** find, so they are not worth re-investigating: the single-player loop
is sound end to end, and the `ss_scratch_a`/`ss_scratch_b` reuse is fragile but not currently a live
collision.

## Next action after the bug queue

Build the live datapack migration reader/writer in a new `gpt/*` branch off `main`:

1. read immutable legacy evidence — **read the hazard note first**, see below;
2. call the accepted pure translator;
3. persist Java Soul/Aspect/Flaw records;
4. read them back and verify identity;
5. mark import complete;
6. retain legacy values until all verification succeeds.

**Step 1 carries this project's most repeated bug.** An absent scoreboard value is not `0`.
`LegacyDatapackSnapshot` uses `0` to mean "no such score", and the translator reads `rankScore() == 0`
as "never completed a First Nightmare" — so a failed or unread lookup that falls through to `0` would
silently downgrade a completed Sleeper and, for a non-Carrier, skip their migration entirely and lose
the identity. Map absent to `0` deliberately and fail the import when a score cannot be read. Full
reasoning in **`docs/DATAPACK-MIGRATION.md` § "an absent score is not `0`"**. This shape has already
cost this project §1.7, §1.10, the dead sneak-to-enter filter and the `scores={x=..0}` class the
validator now rejects.

Worth adding while you are there: a NeoForge GameTest that a Soul survives a relog. It is the only
end-to-end gap left after the alpha.4 verification, and `mod/build.gradle` already declares
`gameTestServer`.

Then implement `NightmareRegistryData` and explicit per-player instance ownership, honouring the
Nightmare lifecycle contract in `docs/JAVA-HANDOFF.md` §6 — one entry choke point, one teardown path.

## Workflow reminders

- GPT does not write directly to or merge into `main`.
- Every GPT commit includes `Co-Authored-By: ChatGPT <gpt@openai.com>`.
- Claude independently reviews and tests before merge.
- CI is deliberately throttled; do not create per-commit workflow noise.
- Historical docs are annotated, not silently rewritten as though earlier beliefs never existed.
- Verify the Java smokes with `mod/verify-smoke.sh`, never the bare `runServerSmoke`/`runClientSmoke`
  tasks: those report `BUILD SUCCESSFUL` with exit `0` even when the server never starts.
- Verify the datapack with `cd testserver && npm run deploy && npm test`. The server loads a built zip,
  not the working tree, so testing without deploying silently exercises the previous build.
- A **JDK** 21 is required, not a JRE. A JRE fails in NeoForm's recompile with the misleading
  `error: release version 21 not supported`.
- If something needs to reach Claude, put it in the repo — `docs/OPEN-QUESTIONS.md`, a commit message or
  an issue. Anything said only in chat to Andrew does not survive the handoff; that has already happened
  once, with the JDK note.
