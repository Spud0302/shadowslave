# Claude test findings — 2026-08-01

**Baseline:** `main@5f8acf2` · **For:** GPT · **Nothing here is fixed.** Andrew's instruction was to
find and record problems first, then fix in a later pass.

Seven findings, all filed as GitHub issues. This document is the durable in-repo record — issues can
be closed and lose their reasoning, and git is the channel between us.

Both gates were green before and after this work: validator clean, **32/32 + 39/39** on a confirmed
`v1.0.0` deploy, Java build 14 tests / 0 failures. None of these findings is a regression; they are
things the gates were never looking at.

| #                                                        | Finding                                                                | Where      | Severity |
| -------------------------------------------------------- | ---------------------------------------------------------------------- | ---------- | -------- |
| [#20](https://github.com/Spud0302/shadowslave/issues/20) | Concurrent trials block each other's victory and trap the player       | datapack   | **high** |
| [#21](https://github.com/Spud0302/shadowslave/issues/21) | That limitation is documented only in a non-authoritative section      | docs       | medium   |
| [#22](https://github.com/Spud0302/shadowslave/issues/22) | `SoulData` codec throws instead of returning `DataResult.error`        | Java       | **high** |
| [#23](https://github.com/Spud0302/shadowslave/issues/23) | Stored schema version discarded on decode; its validation unreachable  | Java       | medium   |
| [#24](https://github.com/Spud0302/shadowslave/issues/24) | Aspect/Flaw invariant enforced for `DREAMER` only                      | Java       | medium   |
| [#25](https://github.com/Spud0302/shadowslave/issues/25) | Migration accepts two inconsistent states the legacy path rejects      | Java       | low      |
| [#26](https://github.com/Spud0302/shadowslave/issues/26) | `test/reset` does not restore health, disarming reset-then-enter tests | test infra | low      |

## Suggested order

1. **#22** — the only one that can stop a player loading. It also interacts with #23 and #24, since
   all three live in `SoulData`'s constructor and codec; doing them together avoids touching that
   file three times.
2. **#20** — permanent progression is at stake and a trapped player has no way out.
3. **#21** — cheap, and it is shipped documentation for a public release.
4. **#25**, **#26** — best done alongside the live datapack reader, which is what will actually
   produce the snapshots #25 is about.

## #20 in detail, because the repro is not obvious

`nightmare/objective_tick` decides victory with `@e[tag=ss_creature]`. That selector is
**dimension-scoped, not per-player**, and every concurrent trial runs in the single
`shadowslave:nightmare` dimension. So another player's creature keeps resetting your `ss_gone`
counter, and a player who has already killed their own creature can neither win nor leave — the
timer has expired, so nothing else ends the trial.

`testserver/regression_issue20.mjs` reproduces it and **currently exits 1**. It is deliberately not
in `npm test`: wiring a known-failing check into the release gate would break the gate. Wire it in
once it passes.

**Three mistakes made this look fixed when it was not.** All three are recorded in the script's
header; please keep them if you edit it.

1. `@e` is dimension-scoped — a decoy in the Overworld is invisible to `objective_tick`. Verified
   separately with two armour stands, one per dimension: each dimension-scoped query returns 1.
2. Entities in unloaded chunks are absent from `@e`. A decoy 3000 blocks out was never counted.
3. `test/reset` does not restore health (#26) and entry refuses below 14 HP, so an unhealed subject
   silently measures the refusal path.

I reported "not reproduced" twice off the back of 1 and 2 before checking the selector semantics.
The measurement was wrong, not the claim.

Three more consequences of the same root cause, found by reading rather than measured:

- the 48-block leash `tp @e[tag=ss_creature,distance=48..]` drags **every** distant creature in the
  dimension onto whoever is ticking;
- the bossbar reads `@e[tag=ss_creature,limit=1]` and can track someone else's creature;
- `spawn_creature`'s guard `execute if entity @e[tag=ss_creature] run tag @s add ss_creature_spawned`
  is satisfied by another player's creature, so a player whose own summon failed is marked as
  fighting one — the exact free win that guard's own comment was written to prevent.

**This is not a regression.** It is the known "single player at a time" ceiling, deferred to Java.
It is filed because the failure _mode_ was never written down, and because
`docs/JAVA-HANDOFF.md` §6 already requires per-instance entity ownership — the fix belongs to the
Java `NightmareService`, and the datapack may reasonably keep the ceiling if #21 is honest about it.

## Two false leads, recorded so nobody re-chases them

**A bot that would not enter the Nightmare.** Looked like a per-player entry bug and reproduced
deterministically — one bot always refused, another always admitted, identical commands. The pack
was **correct**: the failing bot was at 4 HP and entry refuses below 14. That became #26.

**An apparent free win.** A player reached `ss_rank=1` with the creature count reading 0. My own
probe had killed the creature; the pack correctly treated that as a victory. A dense solo trace
confirmed the single-player path is sound: creature spawns, tag is set, and an unarmed bot is
correctly ejected at low health.

## What I did not find

Worth stating, since absence of a finding is also information:

- the single-player loop is sound end to end — entry gates, spawn, observation, ejection, victory,
  teardown all behaved correctly under every probe;
- the migration translator fails closed on everything else probed: out-of-range scores, wrong
  compatibility tags, future ranks, active-Nightmare state, and re-migration all reject or return no
  plan, with the migration-version check correctly ordered first;
- enum decoding is well behaved — an unknown `spell_state` returns a clean `DataResult.Error`, which
  is exactly what #22 says the record invariants should do;
- `ss_scratch_a`/`ss_scratch_b` reuse across `nightmare/enter`, `observe_trial` and `soul.mcfunction`
  is **not** currently a live collision: every user resets, writes and reads within a single function
  invocation, and datapack functions run to completion. It stays fragile — this is the §1.7 shape —
  but I could not make it fail, so it is not filed as a bug.

## Method note

Findings were confirmed by measurement, not inference, except where explicitly marked "found by
reading" above. Where a probe disagreed with the code, I checked the probe first — which is what
turned two "not reproduced" results into the #20 confirmation.
