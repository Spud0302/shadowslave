# GPT review — generated Aspect / earned Flaw rework

**Branch:** `gpt/aspect-flaw-rework`

**Baseline:** `main@a470b914f3e0710d3dfee63adc29b8e6e50d4599` (`v1.4.9`)

**Related open question:** `docs/OPEN-QUESTIONS.md` Q2

## Review verdict

From repository-level reasoning, the Q2 implementation is ready for Claude's **implementation review
and live Minecraft verification**. GPT has not run Minecraft 1.21.1 and does not claim the branch is
release-approved.

The branch deliberately changes gameplay semantics while preserving the existing Phase 1 mechanics
as compatibility roots:

- Aspect formal identity is generated compositionally from **nature × archetype**;
- four existing internal Aspect tags remain the finite Dormant mechanics;
- Flaw family is biased by strong observed trial behavior, with randomness inside the family;
- the old player-facing `Shadow Slave` Flaw is removed because `Shadow Slave` is canonically an
  Aspect name;
- old `ss_aspect=1..4` / `ss_flaw=1..4` worlds retain a legacy `/trigger soul` fallback;
- no new scoreboard objective, rank, dimension, death path, ownership model or version stamp was
  introduced.

Full design rationale and acceptance criteria:

`docs/superpowers/specs/2026-07-30-aspect-flaw-rework.md`

## Static compatibility findings

### Existing effects continue to run by tags

`upkeep.mcfunction` does not dispatch Aspect/Flaw effects from `ss_aspect` or `ss_flaw` numeric
values. It dispatches from:

- `ss_aspect_shadow`
- `ss_aspect_flame`
- `ss_aspect_bone`
- `ss_aspect_wind`
- `ss_flaw_shadow_slave`
- `ss_flaw_fragile`
- `ss_flaw_ravenous`
- `ss_flaw_weightless`

The generator still assigns exactly one tag from each set before it encodes the richer score value.
This is why the score migration can be done without rewriting all eight effect functions.

### Historical test alias reaches the new generator

`test/awaken` still routes through `awaken/roll`, and that compatibility alias routes directly to
`progression/become_sleeper`, which calls `prototype/roll_aspect_flaw`.

The helper now explicitly clears trial-observation tags first, because a command that skips the trial
must exercise the no-trial/random-family path rather than inherit behavior from an earlier failed
attempt.

### No new objective

The branch reuses existing objectives:

- `ss_aspect` — encoded nature × archetype;
- `ss_flaw` — encoded family × personal variant;
- `ss_roll` — trial entry FoodLevel baseline, then generation scratch;
- `ss_scratch_b` — current FoodLevel temporary during observation.

The `ss_roll` reuse is safe against the current runtime paths inspected in the repository:

- `/trigger soul` uses `ss_scratch_a` / `ss_scratch_b`, not `ss_roll`;
- `test/selfcheck` uses fake-player `$check` / `$ok` entries in `ss_roll`, not the player's `@s`
  entry;
- generation occurs only after the trial is over, at which point overwriting the hunger baseline is
  intentional.

### Player hunger is read, not written

The new observer depends on the player's `foodLevel` NBT field through `data get entity`. That stays
inside the project's established rule: player NBT reads are allowed; writes are not.

## Cross-column changes Claude must scrutinize

Under the current file split, three changes touch Claude's natural area. They are intentionally small.

### `nightmare/enter.mcfunction`

After every guard has accepted the player and after `ss_in_nightmare` is set:

```mcfunction
function shadowslave:prototype/trial_begin
```

Purpose: clear observations from an earlier failed attempt and capture entry hunger before the trial
starts.

No guard, cooldown, weakness threshold, timer, teleport or bossbar command was otherwise changed.

### `nightmare/tick_player.mcfunction`

While the creature exists, immediately **before** the existing 48-block leash:

```mcfunction
execute if entity @e[tag=ss_creature] run function shadowslave:prototype/observe_trial
```

The ordering is intentional. If observation ran after the leash, a `40+` flee signal could be erased
by the teleport before it was recorded.

No ejection threshold, countdown, creature-absence/win logic or leash distance was changed.

### `test/reset.mcfunction`

Adds cleanup for:

- `ss_trial_bloodied`
- `ss_trial_hungry`
- `ss_trial_fled`
- player's `ss_roll`

Reset must remain a genuinely fresh state for tests.

## New generated Aspect contract

Aspect score = `nature * 10 + archetype`.

Nature / first word / internal mechanical tag:

| digit | word | tag |
| --- | --- | --- |
| 1 | Veiled | `ss_aspect_shadow` |
| 2 | Ashen | `ss_aspect_flame` |
| 3 | Pale | `ss_aspect_bone` |
| 4 | Restless | `ss_aspect_wind` |

Independent archetype / second word:

| digit | word |
| --- | --- |
| 1 | Witness |
| 2 | Bearer |
| 3 | Warden |
| 4 | Wanderer |

Examples:

- `13` = **Veiled Warden** + shadow-root mechanic;
- `24` = **Ashen Wanderer** + ember-root mechanic;
- `31` = **Pale Witness** + body-root mechanic;
- `42` = **Restless Bearer** + motion-root mechanic.

The point of the two axes is architectural, not just cosmetic: adding another archetype expands every
nature instead of hand-authoring another entire class list.

## New Flaw contract

Observation tags:

- `ss_trial_bloodied` — sampled health reaches 5..8 HP while creature is active;
- `ss_trial_hungry` — FoodLevel falls at least 6 below the entry baseline;
- `ss_trial_fled` — creature reaches 40+ blocks before the 48-block leash.

Family precedence:

```text
fled > hungry > bloodied > random fallback
```

Family / encoded score range / compatibility tag:

| family | scores | tag | burden |
| --- | --- | --- | --- |
| 1 | 11..14 | `ss_flaw_shadow_slave` | daylight damage/weakness |
| 2 | 21..24 | `ss_flaw_fragile` | reduced max health |
| 3 | 31..34 | `ss_flaw_ravenous` | faster hunger drain |
| 4 | 41..44 | `ss_flaw_weightless` | reduced safe fall distance |

The family-1 **internal id** remains `ss_flaw_shadow_slave` only for compatibility. New player-facing
names are Nightbound / Pale Dawn / Sunshy / Dusk's Debt.

## Suggested Claude harness additions

The existing harness should remain responsible for live mechanics. The following tests are designed
to be deterministic where possible; do not use probabilistic distribution as a release gate.

### A. Encoded identity shape + exactly one mechanical tag

After `test/reset` + `test/awaken`:

- assert `ss_aspect` matches one of the four encoded bands (`11..14`, `21..24`, `31..34`, `41..44`);
- assert exactly one `ss_aspect_*` compatibility tag is present;
- assert `ss_flaw` matches one encoded band;
- assert exactly one `ss_flaw_*` compatibility tag is present;
- `/trigger soul` must contain the expected composed name for the observed score.

Do **not** require all 16 random combinations to appear in one CI run. That would make the gate
probabilistic. Mapping can be checked deterministically by setting each encoded score temporarily and
calling `soul` if desired.

### B. Generator precedence — deterministic direct forcing

The generator consumes observation tags directly, so precedence can be tested without conducting a
fight. Use `test/reset`, add the relevant observation tags, then call
`shadowslave:prototype/roll_aspect_flaw` directly.

**Bloodied only**

```mcfunction
function shadowslave:test/reset
tag @s add ss_trial_bloodied
function shadowslave:prototype/roll_aspect_flaw
```

Expect `ss_flaw=21..24` and only `ss_flaw_fragile`.

**Bloodied + hungry**

```mcfunction
function shadowslave:test/reset
tag @s add ss_trial_bloodied
tag @s add ss_trial_hungry
function shadowslave:prototype/roll_aspect_flaw
```

Expect `ss_flaw=31..34` and only `ss_flaw_ravenous`.

**All three**

```mcfunction
function shadowslave:test/reset
tag @s add ss_trial_bloodied
tag @s add ss_trial_hungry
tag @s add ss_trial_fled
function shadowslave:prototype/roll_aspect_flaw
```

Expect `ss_flaw=41..44` and only `ss_flaw_weightless`.

After each call, assert all three `ss_trial_*` tags were consumed and player `ss_roll` is absent.

### C. `test/awaken` does not inherit an old failed-trial observation

```mcfunction
function shadowslave:test/reset
tag @s add ss_trial_fled
function shadowslave:test/awaken
```

The important deterministic assertion is that `ss_trial_fled` is removed **before generation**.
Because the no-signal family is random, do not assert that the resulting Flaw is non-family-4; random
fallback is allowed to select family 4 legitimately.

If the harness needs to prove order, add a test-only instrumentation path rather than converting a
random outcome into a flaky assertion.

### D. Trial-entry reset/baseline integration

Enter through `test/nightmare`, then assert:

- all three observation tags are absent immediately after accepted entry;
- player's `ss_roll` equals the player's FoodLevel read from the server.

This proves the new `enter` hook is actually wired to accepted entry, not merely that
`trial_begin.mcfunction` works in isolation.

### E. Observer boundaries

With an active test creature or a controlled direct call:

- `ss_health=9` must **not** set bloodied;
- `ss_health=8` must set bloodied;
- `ss_health=5` must set bloodied;
- `ss_health=4` is owned by ejection and should not become the successful-run bloodied signal through
  the normal tick path.

For hunger, compare baseline/current values around the exact delta:

- drop 5 -> no `ss_trial_hungry`;
- drop 6 -> set `ss_trial_hungry`.

For fleeing:

- creature at 39.x blocks -> no flee tag;
- creature at 40+ -> set flee tag;
- normal `tick_player` must observe the tag before the existing 48+ leash relocates the creature.

### F. Legacy readout

Manually create one old-style state (`ss_aspect=1`, `ss_flaw=1`, matching compatibility tags) and
call `/trigger soul`.

Expect:

- Aspect line visibly says legacy prototype rather than disappearing;
- Flaw line uses **Nightbound (legacy prototype)** rather than re-exposing `Shadow Slave` as a Flaw.

## Manual checks worth a human

### Naming feel

Roll `test/reset` + `test/awaken` repeatedly and read `/trigger soul`.

Judge whether combinations such as **Ashen Wanderer**, **Pale Warden** and **Restless Witness** feel
like supernatural identities rather than RPG class names. This is subjective and should not be
encoded as a harness assertion.

### Flaw attribution feel

Play several real successful First Nightmares deliberately:

1. close/clean fight with no tracked strong signal;
2. allow health into 5..8 then recover and win;
3. deliberately burn at least 6 food points during the creature phase and win;
4. deliberately open 40+ blocks of distance, recover control and win.

Confirm the eventual burden feels connected to what the run actually did rather than arbitrary.
The exact Flaw **name** should still vary inside the earned burden family.

## Required release gates

GPT has not run these:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

Claude should first reconcile/add the Q2 assertions above to the harness, then run the full suite on
the real 1.21.1 test server.

If PR #1 (`gpt/datapack-release-completion`) lands first, the expected existing suite baseline becomes
32 assertions rather than the current-main 25. Q2 assertions should be added on top of whichever
baseline is current at review time.

No version stamp belongs on this GPT branch.

## Q2 answer for `OPEN-QUESTIONS.md`

**Answer:** yes, the datapack can move meaningfully closer to canon before Java without pretending to
have infinite procedural mechanics.

Use a **composed identity / finite-expression** model for Aspects, and a
**strong-observation -> burden family -> randomized personal name** model for Flaws.

This satisfies the owner's generated-identity / behavior-influenced direction while keeping the
vanilla ceiling explicit. The remaining four mechanical roots are then an honest implementation
boundary for the Java port rather than the player's entire supernatural identity.

When Claude accepts and verifies this branch, move Q2 to **Answered** and link this review/spec rather
than duplicating the whole rationale in `OPEN-QUESTIONS.md`.
