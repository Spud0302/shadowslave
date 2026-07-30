# GPT handoff — living checkpoint

> **Read first in a new GPT session.** Canon authority is `docs/lore-research/`; engineering
> authority is `docs/ENGINEERING-NOTES.md`; workflow authority is `docs/COLLABORATION.md`.

## Repository state

- Repository: `Spud0302/shadowslave`
- Default/release branch: `main`
- Current release on main: **v1.4.9**
- Main baseline used here: `a470b914f3e0710d3dfee63adc29b8e6e50d4599`
- Active gameplay branch: **`gpt/aspect-flaw-rework`**
- Active gameplay PR: **#2 — Rework generated Aspects and earned Flaws**
- Separate release-hardening PR: **#1 — Datapack release-completion hardening**

Latest checks when this handoff was written:

- PR #2: **mergeable**, 0 commits behind main;
- PR #1: graph is 0 behind main, but GitHub's connector still reports `mergeable: false` despite the
  clean ancestry and no status checks. Treat that as unresolved merge-gate metadata until Claude/GitHub
  identifies the exact gate; do not invent a file conflict.

GPT does not write directly to `main`. Claude reviews/merges GPT branches, owns live Minecraft
verification and version stamping, and ships releases.

## Owner goal

Finish the **datapack completely and release-ready before moving to Java**.

That work is deliberately split:

1. **PR #1 — release hardening:** fail-closed harness, three-way version validation, reproducible
   release ZIP, player-facing advancement presentation and current release docs.
2. **PR #2 — Q2 gameplay/canon rework:** replace the fixed four-class Aspect presentation and
   independently random Flaw model.

The remaining global Nightmare ownership, broad death-item sweep, ravager stand-in, Overworld-noise
terrain, shallow rank storage, GUI/data-model limits and bespoke AI remain honest Java-boundary
ceilings. Do not build them in commands merely to postpone the Java transition.

## Collaboration rules currently in force

Read `docs/COLLABORATION.md`; it supersedes older handoff wording.

GPT's natural area includes lore-derived Aspect/Flaw content, `prototype/roll_aspect_flaw`,
player-facing copy and canon/progression semantics. Claude's natural area includes Nightmare state
machine, harness, validator, dimension/storage plumbing, version stamping and release.

The lists are not hard fences, but cross-column edits must be explicit and Claude must scrutinize
them. Two rules never move:

1. Claude runs validator + harness before merge.
2. `ENGINEERING-NOTES.md` invariants bind both agents.

## PR #2 design answer

Current-main `docs/OPEN-QUESTIONS.md` Q2 asked how to replace the fixed four Aspects/four randomized
Flaws while staying inside vanilla limits.

### Aspect = composed identity over a finite Dormant expression

New Aspect identity is generated from **two independent vocabularies**:

- nature/root: `Veiled / Ashen / Pale / Restless`;
- archetype: `Witness / Bearer / Warden / Wanderer`.

`ss_aspect` encodes `nature * 10 + archetype`, producing 16 combinations from two axes rather than
choosing one whole name from a fixed class list.

The nature maps to exactly one existing compatibility/mechanics tag:

- Veiled -> `ss_aspect_shadow`
- Ashen -> `ss_aspect_flame`
- Pale -> `ss_aspect_bone`
- Restless -> `ss_aspect_wind`

This keeps the vanilla ceiling honest: identity can be generated; arbitrary new executable abilities
cannot be invented at runtime.

### Flaw = observed trial classification, randomness on top

The successful First Nightmare records:

- `ss_trial_bloodied` — health reaches 5..8 while the creature is active;
- `ss_trial_hungry` — FoodLevel falls at least 6 below the entry baseline;
- `ss_trial_fled` — creature reaches 40+ blocks before the 48-block leash.

Family precedence:

```text
fled > hungry > bloodied > baseline
```

Mechanical burden:

- baseline/no strong deviation -> family 1, daylight burden;
- bloodied -> family 2, reduced max health;
- hungry -> family 3, hunger burden;
- fled -> family 4, fall/footing burden.

**Only after the family is earned** does a `1..4` roll choose the personal Flaw name inside that
family. This is the requested “behavior with randomness on top”; the mechanical Flaw is not
independently randomized.

The historical internal id `ss_flaw_shadow_slave` remains for compatibility only. New player-facing
family-1 names are Nightbound / Pale Dawn / Sunshy / Dusk's Debt because `Shadow Slave` is canonically
an Aspect name, not a Flaw.

## PR #2 implementation map

Full design:

`docs/superpowers/specs/2026-07-30-aspect-flaw-rework.md`

Claude review/test brief:

`docs/reviews/2026-07-30-gpt-aspect-flaw-rework.md`

### Added

- `prototype/trial_begin.mcfunction`
  - clears observations from an earlier attempt;
  - stores entry `foodLevel` in player `ss_roll`.
- `prototype/observe_trial.mcfunction`
  - records bloodied / hungry / fled signals.

### Reworked

- `prototype/roll_aspect_flaw.mcfunction`
  - strips old tags/modifiers;
  - composes encoded Aspect identity;
  - derives Flaw family from observations;
  - randomizes only the personal name inside that earned family;
  - reapplies exactly one compatibility tag from each set;
  - consumes observations and resets player `ss_roll`.
- `soul.mcfunction`
  - displays composed Aspect / generated Flaw identity;
  - keeps legacy `1..4` readout support;
  - never presents `Shadow Slave` as a Flaw.
- `test/awaken`
  - historical id retained;
  - clears old observations because it skips the trial;
  - therefore exercises family 1 baseline and randomizes only its personal name.
- `test/reset`
  - clears observation tags and player `ss_roll`.
- README/help/init/progression/upkeep/internal Flaw comments match the new semantics.

## Cross-column PR #2 changes Claude must review closely

### `nightmare/enter.mcfunction`

One hook after accepted entry:

```mcfunction
function shadowslave:prototype/trial_begin
```

No guard, cooldown, weakness threshold, timer, teleport or bossbar behavior was otherwise changed.

### `nightmare/tick_player.mcfunction`

One hook while the creature exists, **before** the 48-block leash:

```mcfunction
execute if entity @e[tag=ss_creature] run function shadowslave:prototype/observe_trial
```

Ordering is required so the leash cannot erase the 40+ flee signal. No ejection/countdown/win/leash
values changed.

### `test/reset.mcfunction`

Adds Q2 transient cleanup. Claude owns test/state-hygiene review.

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

- four Aspect mechanic tags/functions;
- four Flaw mechanic tags/functions;
- `ss_aspect` / `ss_flaw` objective names;
- `test/awaken` and `awaken/roll` compatibility ids;
- historical advancement ids.

Old worlds with score values `1..4` continue executing mechanics from tags and get a
`legacy prototype` Soul readout until reset/re-roll.

## Verification still required

GPT cannot run the project's real Minecraft 1.21.1 server and has **not** claimed this branch passes.

Claude should extend/reconcile the harness using the exact deterministic cases in the Q2 review doc,
then run:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

Minimum Q2 coverage:

- no-signal/test-awaken -> `ss_flaw=11..14` + family-1 tag;
- bloodied -> `21..24`;
- bloodied + hungry -> `31..34`;
- all three -> `41..44`;
- exactly one Aspect tag and one Flaw tag;
- stale observations cleared on accepted entry;
- entry `ss_roll` equals FoodLevel baseline;
- boundaries: health 8 vs 9, food delta 6 vs 5, distance 40 vs 39.x;
- observations/player `ss_roll` consumed after generation;
- `test/awaken` cannot inherit a prior failed-trial signal;
- legacy `1..4` Soul readout;
- all eight existing mechanics and modifier cleanup.

If PR #1 lands first, Q2 tests belong on top of its hardened 32-assertion harness rather than the
current-main 25-assertion baseline.

No GPT version stamp.

## Recommended next action

Claude reviews PRs #1 and #2 separately, adds/runs the required live coverage, and returns any failed
acceptance case to GPT before merge. Once both are reconciled, tested and version-stamped, the
Phase 1 datapack can honestly be called complete and the project can move to Java.
