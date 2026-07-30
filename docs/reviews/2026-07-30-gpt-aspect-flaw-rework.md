# GPT review — generated Aspect / earned Flaw rework

**Branch:** `gpt/aspect-flaw-rework`

**Baseline:** `main@a470b914f3e0710d3dfee63adc29b8e6e50d4599` (`v1.4.9`)

**Related open question:** `docs/OPEN-QUESTIONS.md` Q2

## Review verdict

From repository-level reasoning, the Q2 implementation is ready for Claude's **implementation review
and live Minecraft verification**. GPT has not run Minecraft 1.21.1 and does not claim the branch is
release-approved.

The branch deliberately changes gameplay semantics while preserving the current Phase 1 mechanics as
compatibility roots:

- Aspect identity is generated compositionally from **nature × archetype**;
- four existing internal Aspect tags remain the finite Dormant mechanics;
- every Flaw mechanical family comes from the successful trial classification;
- randomness is applied only to the personal Flaw name **after** the family is earned;
- the old player-facing `Shadow Slave` Flaw is removed because `Shadow Slave` is canonically an
  Aspect name;
- old `ss_aspect=1..4` / `ss_flaw=1..4` worlds retain a legacy `/trigger soul` fallback;
- no new scoreboard objective, rank, dimension, death path, ownership model or version stamp was
  introduced.

Full rationale and acceptance criteria:

`docs/superpowers/specs/2026-07-30-aspect-flaw-rework.md`

## Static compatibility findings

### Existing effects continue to run by tags

`upkeep.mcfunction` dispatches effects from the existing compatibility tags, not the numeric identity
scores:

- `ss_aspect_shadow`
- `ss_aspect_flame`
- `ss_aspect_bone`
- `ss_aspect_wind`
- `ss_flaw_shadow_slave`
- `ss_flaw_fragile`
- `ss_flaw_ravenous`
- `ss_flaw_weightless`

The generator still assigns exactly one tag from each set before it encodes the richer score value.
This is why the score migration does not require rewriting all eight effect functions.

### Historical test alias reaches the new generator

`test/awaken` still routes through `awaken/roll` -> `progression/become_sleeper` ->
`prototype/roll_aspect_flaw`.

Because `test/awaken` explicitly skips the trial, it clears old observation tags first. With no
strong trial signal it deterministically uses family 1's baseline burden and randomizes only the
personal name inside that family.

### No new objective

The branch reuses:

- `ss_aspect` — encoded nature × archetype;
- `ss_flaw` — encoded family × personal variant;
- `ss_roll` — trial entry FoodLevel baseline, then generation scratch;
- `ss_scratch_b` — current FoodLevel temporary during observation.

The `ss_roll` reuse is compatible with current runtime paths inspected in the repository:

- `/trigger soul` uses `ss_scratch_a` / `ss_scratch_b`, not player `ss_roll`;
- `test/selfcheck` uses fake-player `$check` / `$ok` entries in `ss_roll`, not the player's `@s`
  entry;
- generation occurs only after the trial is over, when overwriting the hunger baseline is intended.

### Player hunger is read, not written

The observer reads the player's `foodLevel` through `data get entity`; no player NBT write was added.

## Cross-column changes Claude must scrutinize

Under the current collaboration split, three changes touch Claude's natural area.

### `nightmare/enter.mcfunction`

After every entry guard has accepted the player and `ss_in_nightmare` is set:

```mcfunction
function shadowslave:prototype/trial_begin
```

Purpose: clear observations from an earlier failed attempt and capture entry hunger before the trial.
No guard, cooldown, weakness threshold, timer, teleport or bossbar behavior was otherwise changed.

### `nightmare/tick_player.mcfunction`

While the creature exists, immediately **before** the existing 48-block leash:

```mcfunction
execute if entity @e[tag=ss_creature] run function shadowslave:prototype/observe_trial
```

The ordering is intentional: after the leash, a 40+ flee signal can be erased by the teleport.
No ejection threshold, countdown, win/absence logic or leash distance was changed.

### `test/reset.mcfunction`

Adds cleanup for:

- `ss_trial_bloodied`
- `ss_trial_hungry`
- `ss_trial_fled`
- player's `ss_roll`

Reset must remain genuinely fresh state.

## Generated Aspect contract

Aspect score = `nature * 10 + archetype`.

Nature / first word / internal mechanic:

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

The two axes matter architecturally: adding an archetype expands every nature instead of adding
another whole class name.

## Earned Flaw contract

Observation tags:

- `ss_trial_bloodied` — sampled health reaches 5..8 HP while creature is active;
- `ss_trial_hungry` — FoodLevel falls at least 6 below the entry baseline;
- `ss_trial_fled` — creature reaches 40+ blocks before the 48-block leash.

Family precedence:

```text
fled > hungry > bloodied > baseline
```

Family / score / compatibility tag:

| family | scores | tag | burden |
| --- | --- | --- | --- |
| 1 | 11..14 | `ss_flaw_shadow_slave` | daylight damage/weakness |
| 2 | 21..24 | `ss_flaw_fragile` | reduced max health |
| 3 | 31..34 | `ss_flaw_ravenous` | faster hunger drain |
| 4 | 41..44 | `ss_flaw_weightless` | reduced safe fall distance |

Family 1 is the deterministic **no strong deviation observed** baseline. The family-1 internal id
remains `ss_flaw_shadow_slave` only for compatibility; new player-facing names are Nightbound,
Pale Dawn, Sunshy and Dusk's Debt.

Randomness happens only after family selection, choosing one of four names inside the family. This
is the repository handover's requested **behavior with randomness on top**, not an independently
random mechanical Flaw.

## Suggested Claude harness additions

Keep the release gate deterministic. Do not require random distributions to hit every value.

### A. Encoded identity + exactly one mechanics tag

After `test/reset` + `test/awaken`:

- `ss_aspect` must be in one encoded band (`11..14`, `21..24`, `31..34`, `41..44`);
- exactly one `ss_aspect_*` compatibility tag must be present;
- because the helper has no trial signal, `ss_flaw` must be **11..14**;
- exactly one Flaw tag must be present, specifically `ss_flaw_shadow_slave` for this baseline path;
- `/trigger soul` must render the composed Aspect name matching the observed score and one family-1
  personal Flaw name.

Do not require all 16 Aspect combinations or all four family-1 names to appear in a CI run. Mapping
can be checked deterministically by setting encoded scores and calling `soul`.

### B. Generator precedence — direct deterministic forcing

The generator consumes observation tags directly, so family precedence can be tested without a fight.

**Baseline**

```mcfunction
function shadowslave:test/reset
function shadowslave:prototype/roll_aspect_flaw
```

Expect `ss_flaw=11..14` and only `ss_flaw_shadow_slave`.

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

After every generation call, all `ss_trial_*` tags must be consumed and player `ss_roll` absent.

### C. `test/awaken` must discard an old failed-trial signal

```mcfunction
function shadowslave:test/reset
tag @s add ss_trial_fled
function shadowslave:test/awaken
```

Expect `ss_flaw=11..14`, not family 4. This is now deterministic and directly proves the helper
cleared the inherited signal before generation.

### D. Trial-entry reset/baseline integration

Enter through `test/nightmare`, then assert:

- all three observation tags are absent immediately after accepted entry;
- player's `ss_roll` equals the player's current FoodLevel read from the server.

This proves the entry hook is wired, not merely that `trial_begin` works in isolation.

### E. Observer boundaries

With an active test creature or controlled direct setup:

- `ss_health=9` -> no bloodied;
- `ss_health=8` -> bloodied;
- `ss_health=5` -> bloodied;
- `ss_health=4` belongs to normal ejection and must not become a successful-run bloodied observation
  through `tick_player`.

Hunger delta:

- drop 5 -> no hungry tag;
- drop 6 -> hungry tag.

Distance:

- creature at 39.x -> no fled tag;
- creature at 40+ -> fled tag;
- normal tick must observe fleeing before the existing 48+ leash relocates the creature.

### F. Legacy readout

Create old-style `ss_aspect=1`, `ss_flaw=1` with matching compatibility tags and call `/trigger soul`.

Expect:

- Aspect line says `legacy prototype` rather than disappearing;
- Flaw line uses **Nightbound (legacy prototype)** rather than exposing `Shadow Slave` as a Flaw.

## Human checks worth keeping human

### Naming feel

Roll `test/reset` + `test/awaken` repeatedly and read `/trigger soul`. Judge whether combinations such
as **Ashen Wanderer**, **Pale Warden** and **Restless Witness** feel like supernatural identities
rather than RPG classes.

### Flaw attribution feel

Win deliberate runs in four patterns:

1. no strong tracked deviation;
2. reach health 5..8 and recover;
3. consume at least 6 food points during creature phase;
4. open 40+ blocks of distance and still win.

Confirm the eventual burden family tracks the intended classification. The formal name should still
vary inside that earned family.

## Required release gates

GPT has **not** run:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

Claude should first add/reconcile the Q2 assertions above, then run the full suite against the real
1.21.1 server. If PR #1 lands first, its expected existing baseline is 32 assertions rather than
current-main's 25; Q2 tests go on top of whichever baseline is current.

No version stamp belongs on this GPT branch.

## Q2 answer for `OPEN-QUESTIONS.md`

**Answer:** the datapack can get meaningfully closer to canon before Java without pretending it has
infinite procedural mechanics.

Use a **composed identity / finite-expression** model for Aspects and an
**observed-trial classification -> burden family -> randomized personal name** model for Flaws.

This matches the owner's generated-identity / behavior-plus-randomness direction while keeping the
vanilla ceiling explicit. The four remaining mechanical roots become an honest Java migration seam
rather than the player's entire supernatural identity.

When Claude accepts and verifies this branch, move Q2 to **Answered** and link this review/spec.
