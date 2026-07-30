# GPT handoff — living checkpoint

> **Read first in a new GPT session.** Canon authority is `docs/lore-research/`; engineering
> authority is `docs/ENGINEERING-NOTES.md`; workflow authority is `docs/COLLABORATION.md`.

## Repository state

- Repository: `Spud0302/shadowslave`
- Default/release branch: `main`
- Current release on main: **v1.4.9**
- Current main used by this branch: `a470b914f3e0710d3dfee63adc29b8e6e50d4599`
- Active GPT gameplay branch: **`gpt/aspect-flaw-rework`**
- Separate GPT release-hardening PR: **#1 — Datapack release-completion hardening**
- PR #1 branch: `gpt/datapack-release-completion`

GPT must not write directly to `main`. Claude reviews/merges GPT branches, owns live Minecraft
verification and version stamping, and ships releases.

## Owner goal

Get the **datapack completely finished and release-ready before moving to Java**.

The work is deliberately split:

1. **PR #1 — release hardening**: fail-closed harness, version validation, release ZIP builder,
   player-facing advancement presentation and current release docs. GPT has finished its side; Claude
   still needs to run the real 1.21.1 gates.
2. **This branch — Q2 Aspect/Flaw gameplay rework**: remove the fixed four-class presentation and
   independently random Flaw model before the datapack is called final.

The remaining global Nightmare ownership, broad death-item sweep, ravager stand-in, Overworld-noise
terrain, shallow rank storage, GUI/data-model limits and bespoke AI are still honest Java-boundary
ceilings. Do not build those future systems in commands merely to avoid crossing the Java boundary.

## Current collaboration split

Read `docs/COLLABORATION.md`; it supersedes older wording in historical handoffs.

GPT's natural area includes lore-derived Aspect/Flaw content, `prototype/roll_aspect_flaw`,
player-facing copy and canon/progression semantics. Claude's natural area includes Nightmare state
machine, harness, validator, dimension/storage plumbing, version stamping and release.

The lists are not hard fences. Cross-column changes must be explicit and Claude must scrutinize/test
them. Two hard rules stay fixed:

1. Claude runs validator + harness before merge.
2. `ENGINEERING-NOTES.md` invariants bind both agents.

## Q2 design answer

Current-main `docs/OPEN-QUESTIONS.md` Q2 asks how to replace the fixed four Aspects/four randomized
Flaws while staying inside vanilla limits.

The implemented answer is:

### Aspect = composed identity over a finite Dormant expression

New Aspect identity is generated from **two independent vocabularies**:

- nature/root: `Veiled / Ashen / Pale / Restless`;
- archetype: `Witness / Bearer / Warden / Wanderer`.

`ss_aspect` encodes `nature * 10 + archetype`, giving 16 combinations from two axes rather than
choosing one whole name from a fixed class list.

The nature still maps to one internal compatibility/mechanics tag:

- Veiled -> `ss_aspect_shadow`
- Ashen -> `ss_aspect_flame`
- Pale -> `ss_aspect_bone`
- Restless -> `ss_aspect_wind`

This is an honest datapack ceiling: player identity is generated, but arbitrary new executable
abilities cannot be invented at runtime.

### Flaw = observed trial classification, randomness on top

The successful First Nightmare records:

- `ss_trial_bloodied` — health reaches 5..8 while creature is active;
- `ss_trial_hungry` — FoodLevel falls at least 6 below the entry baseline;
- `ss_trial_fled` — creature reaches 40+ blocks before the 48-block leash.

Family precedence:

```text
fled > hungry > bloodied > baseline
```

This makes the mechanical burden deterministic from the trial classification:

- baseline/no strong deviation -> family 1, daylight burden;
- bloodied -> family 2, reduced max health;
- hungry -> family 3, hunger burden;
- fled -> family 4, fall/footing burden.

**Only after the family is earned** does a `1..4` roll choose the personal Flaw name inside that
family. This is Andrew's requested “behavior with randomness on top”; the mechanical Flaw itself is
not independently random.

The historical internal id `ss_flaw_shadow_slave` remains for compatibility only. New player-facing
family-1 names are Nightbound / Pale Dawn / Sunshy / Dusk's Debt because `Shadow Slave` is canonically
an Aspect name, not a Flaw.

## Branch implementation

Full design spec:

`docs/superpowers/specs/2026-07-30-aspect-flaw-rework.md`

Claude test/review brief:

`docs/reviews/2026-07-30-gpt-aspect-flaw-rework.md`

### Added

- `prototype/trial_begin.mcfunction`
  - clears observations from an earlier attempt;
  - stores entry `foodLevel` in player `ss_roll`.
- `prototype/observe_trial.mcfunction`
  - records bloodied / hungry / fled signals.

### Reworked

- `prototype/roll_aspect_flaw.mcfunction`
  - removes old tags/modifiers first;
  - composes encoded Aspect identity;
  - derives Flaw family from observations;
  - randomizes only the personal Flaw name inside the earned family;
  - reapplies exactly one compatibility tag from each set;
  - consumes observations and resets player `ss_roll`.
- `soul.mcfunction`
  - displays the composed Aspect and generated Flaw names;
  - explains the current Dormant mechanical expression;
  - keeps legacy `1..4` readout support for existing worlds;
  - never presents `Shadow Slave` as a Flaw.
- `test/awaken`
  - historical command name retained;
  - clears old observations because it skips the trial;
  - therefore deterministically uses family 1 baseline and only randomizes that family's personal
    name.
- `test/reset`
  - clears all three observation tags and player `ss_roll`.
- README/help/init/progression/upkeep/internal Flaw comments updated to match the new semantics.

## Cross-column changes Claude must review closely

### `nightmare/enter.mcfunction`

One hook after accepted entry / `ss_in_nightmare`:

```mcfunction
function shadowslave:prototype/trial_begin
```

No guard, cooldown, weakness threshold, timer, teleport or bossbar behavior was otherwise changed.

### `nightmare/tick_player.mcfunction`

One hook while creature exists, **before** the 48-block leash:

```mcfunction
execute if entity @e[tag=ss_creature] run function shadowslave:prototype/observe_trial
```

Ordering is required so the leash cannot erase the 40+ flee signal before observation.
No ejection/countdown/win/leash values were changed.

### `test/reset.mcfunction`

Adds Q2 transient cleanup. Claude owns test machinery/state hygiene review.

## No new objective

The branch reuses:

- `ss_aspect` — encoded nature × archetype;
- `ss_flaw` — encoded family × name variant;
- `ss_roll` — entry FoodLevel baseline, later random scratch;
- `ss_scratch_b` — current FoodLevel observation scratch.

`/trigger soul` does not use player `ss_roll`; `test/selfcheck` uses fake-player `$check/$ok` entries
in that objective, so the player entry is not shared with those paths.

## Compatibility

Kept:

- existing four Aspect mechanics tags/functions;
- existing four Flaw mechanics tags/functions;
- `ss_aspect` and `ss_flaw` objective names;
- `test/awaken` command id;
- `awaken/roll` compatibility alias;
- historical advancement ids.

Old worlds whose identity score is still `1..4` continue to execute mechanics from tags and receive a
`legacy prototype` Soul readout until reset/re-roll.

## Static audit status

Latest comparison when this handoff was written:

- branch is based on `main@a470b91`;
- **0 behind** main;
- default-branch searches found no additional gameplay consumer of `ss_aspect matches ...` or
  `ss_flaw matches ...` beyond generator/readout/test material that would require numeric migration;
- upkeep remains tag-driven.

The new hunger dependency is a player `foodLevel` **read** via `/data get entity`, not an NBT write.

## Verification still required

GPT cannot run the project's real Minecraft 1.21.1 server and has **not** claimed these pass.

Claude should extend/reconcile the harness using the exact cases in:

`docs/reviews/2026-07-30-gpt-aspect-flaw-rework.md`

Then run:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

Important deterministic Q2 cases include:

- no-signal/test-awaken -> `ss_flaw=11..14` + family-1 compatibility tag;
- bloodied -> `21..24`;
- bloodied + hungry -> `31..34`;
- all three -> `41..44`;
- observations and player `ss_roll` consumed/reset correctly;
- exact observation boundaries (health 8 vs 9, food delta 6 vs 5, distance 40 vs 39.x);
- accepted entry clears stale observations and captures hunger baseline;
- old-style `1..4` Soul readout still works;
- existing eight mechanical effects and modifier cleanup still work.

If PR #1 lands first, add Q2 coverage on top of its 32-assertion hardened harness rather than the
current-main 25-assertion baseline.

## Recommended next action

Open/review the Q2 PR separately from PR #1. Claude should add the live assertions, run both gates and
feed any implementation problem back to GPT before merge.

After Q2 and release hardening are both reconciled, tested and version-stamped, the datapack can
honestly be called the completed Phase 1 reference build and the project can move to the Java
architecture boundary.
