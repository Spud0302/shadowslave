# Phase 1 Aspect / Flaw rework

**Baseline:** `main@a470b914f3e0710d3dfee63adc29b8e6e50d4599` (`v1.4.9`)

**Branch:** `gpt/aspect-flaw-rework`

**Owner intent:** finish the datapack before moving the architecture boundary to Java. The current
four fixed Aspects / four independently random Flaws are the remaining explicit pre-Java gameplay
prototype called out by `docs/OPEN-QUESTIONS.md` Q2.

## Intent

Make the First Nightmare reward feel **personal rather than like choosing one of eight classes**,
while staying honest about what vanilla commands can actually generate.

Canon gives constraints, examples and themes, but **not an Aspect-generation formula**. Therefore:

- Aspect identity is composed from **two independently rolled vocabularies**, not selected as one
  whole item from a larger fixed-name list.
- The first component expresses the finite Dormant mechanical nature; the second is an independent
  archetype.
- The Dormant mechanical expression remains one of four finite command implementations. Vanilla can
  compose identity, but it cannot invent a brand-new executable ability at runtime.
- Every Flaw **mechanical family** is derived from the successful trial classification.
- Randomness happens **after** family selection, varying the personal Flaw identity inside the earned
  burden so identical play does not necessarily produce an identical formal Flaw.

This is a Phase 1 endpoint, not the future Java Soul model.

## Canon constraints behind the design

From `docs/lore-research/section-a-aspects-and-flaws.md`:

- an Aspect belongs to the person; the Spell reveals/unseals rather than metaphysically creating it;
- innate affinities, choices, Nightmare role/history and what happens in the trial can all matter,
  but no weighting/algorithm is established;
- Aspect names describe broad supernatural nature more plausibly than one literal combat move;
- formal Aspect names are too sparse to infer a universal English grammar;
- Flaws are personal burdens and often mirror, twist or attack a person's nature/power/circumstances;
- `Shadow Slave` is Sunny's **Aspect**, not his Flaw. The old player-facing Flaw name must go.

The generated names and game-side selection rules here are **fan-created content**, not claimed canon
names or a canonical Nightmare Spell algorithm.

## Datapack ceiling

Do not fake arbitrary procedural mechanics.

```text
composed personal identity
        ↓
finite Dormant mechanical root
        ↓
future Java implementation can replace the root with data-driven/custom behavior
```

Existing internal Aspect tags remain the four mechanical roots for save/test compatibility:

- `ss_aspect_shadow`
- `ss_aspect_flame`
- `ss_aspect_bone`
- `ss_aspect_wind`

The four historical Flaw tags remain internal burden-family ids. Player-facing identity no longer
uses those raw labels.

## Aspect generation

`ss_aspect` becomes an encoded identity score rather than `1..4`.

The generator rolls two independent axes:

1. **nature/root** (`1..4`) — chooses the finite Dormant mechanic and the first word;
2. **archetype** (`1..4`) — independently chooses the second word.

The final score is `nature * 10 + archetype`, producing `11..44`.

### Nature vocabulary / mechanical roots

| Nature | First word | Internal mechanic | Dormant expression |
| --- | --- | --- | --- |
| 1 | **Veiled** | `ss_aspect_shadow` | darkness lends sight and a little speed |
| 2 | **Ashen** | `ss_aspect_flame` | fire recoils; blows can carry embers |
| 3 | **Pale** | `ss_aspect_bone` | the body hardens beneath the skin |
| 4 | **Restless** | `ss_aspect_wind` | movement becomes unnaturally light and quick |

### Independent archetype vocabulary

| Archetype | Second word |
| --- | --- |
| 1 | **Witness** |
| 2 | **Bearer** |
| 3 | **Warden** |
| 4 | **Wanderer** |

This is a Cartesian product, not sixteen separately authored whole names:

| Score | Generated identity | Score | Generated identity |
| --- | --- | --- | --- |
| 11 | Veiled Witness | 21 | Ashen Witness |
| 12 | Veiled Bearer | 22 | Ashen Bearer |
| 13 | Veiled Warden | 23 | Ashen Warden |
| 14 | Veiled Wanderer | 24 | Ashen Wanderer |
| 31 | Pale Witness | 41 | Restless Witness |
| 32 | Pale Bearer | 42 | Restless Bearer |
| 33 | Pale Warden | 43 | Restless Warden |
| 34 | Pale Wanderer | 44 | Restless Wanderer |

The command implementation renders the pair with score-specific tellraw lines because that is simple
per-player persistence in vanilla. Conceptually the identity is generated from two independent
components: adding a new archetype expands every nature rather than adding another whole class.

### Deliberately not claimed

- these words are not canon terminology;
- the two-roll system is not the canonical Spell algorithm;
- four mechanics are not the complete space of possible Aspects;
- sharing a mechanical root does not mean two generated identities are canonically the same Aspect.

## Flaw observation

A First Nightmare records three **strong, directly observable** behavior signals. They reset at every
accepted entry so an earlier failed attempt cannot contaminate the successful run.

### `ss_trial_bloodied`

Set after the creature exists if sampled health reaches **5..8 HP**.

- normal entry refuses `..9`, so reaching 8 or less proves health fell during this trial;
- `..4` triggers ejection/death teardown, so 5..8 is the survivable near-collapse window.

### `ss_trial_hungry`

At entry, store the player's FoodLevel in existing `ss_roll`. During the fight, compare current
FoodLevel with that baseline. Set the tag after a **drop of at least 6 food points**.

Absolute hunger is not used: somebody who entered hungry should not be credited with behavior that
happened before the Nightmare.

### `ss_trial_fled`

Set if the Nightmare Creature reaches **40+ blocks** from the player before the existing 48-block
leash teleports it back.

The observer must run before the leash so the safety mechanic does not erase the evidence.

## Flaw family selection

Every successful run gets a behavior classification. The absence of a strong deviation is itself the
baseline class; the pack does **not** randomize the mechanical burden.

Priority when multiple observations occurred:

1. `ss_trial_fled` -> family 4;
2. `ss_trial_hungry` -> family 3;
3. `ss_trial_bloodied` -> family 2;
4. no strong signal -> family 1 baseline.

The priority favors the most specific signal. Being bloodied can occur in many close fights; burning
six food points or opening a forty-block gap is more distinctive.

Only after the family is fixed does the generator roll `1..4` for a personal identity variant and
encode `family * 10 + variant` in `ss_flaw`.

That is the intended **“behavior, with randomness on top”** model: identical play earns the same
burden family but can still produce a different formal Flaw identity.

### Family 1 — baseline / night-daylight burden

Internal compatibility tag/function: `ss_flaw_shadow_slave` / `flaw/shadow_slave`.
The internal id stays for compatibility; **Shadow Slave is never the player-facing Flaw name**.

| Score | Generated Flaw identity |
| --- | --- |
| 11 | Nightbound |
| 12 | Pale Dawn |
| 13 | Sunshy |
| 14 | Dusk's Debt |

Burden: direct sunlight hurts and weakens the player.

### Family 2 — bloodied / fragile-body burden

Internal tag: `ss_flaw_fragile`.

| Score | Generated Flaw identity |
| --- | --- |
| 21 | Brittle Vessel |
| 22 | Cracked Heart |
| 23 | Borrowed Blood |
| 24 | Thin Thread |

Burden: reduced maximum health.

### Family 3 — hunger burden

Internal tag: `ss_flaw_ravenous`.

| Score | Generated Flaw identity |
| --- | --- |
| 31 | Hollow Maw |
| 32 | Bottomless Hunger |
| 33 | Empty Feast |
| 34 | Gnawing Soul |

Burden: hunger drains faster.

### Family 4 — flee / footing-fall burden

Internal tag: `ss_flaw_weightless`.

| Score | Generated Flaw identity |
| --- | --- |
| 41 | Rootless |
| 42 | False Wings |
| 43 | Unsteady Ground |
| 44 | Falling Debt |

Burden: safe falling distance is reduced.

## Required implementation shape

GPT-side content:

- replace `prototype/roll_aspect_flaw.mcfunction`;
- add `prototype/trial_begin.mcfunction`;
- add `prototype/observe_trial.mcfunction`;
- update `soul.mcfunction` and player-facing docs;
- keep historical mechanic ids internal.

Cross-column hooks that Claude must scrutinize:

- `nightmare/enter.mcfunction`: after accepted entry, call `prototype/trial_begin`;
- `nightmare/tick_player.mcfunction`: while the creature exists, call `prototype/observe_trial`
  **before** the 48-block leash;
- `test/reset.mcfunction`: clear the three observation tags and player's `ss_roll`.

No new objective is required:

- `ss_aspect` = encoded Aspect identity;
- `ss_flaw` = encoded Flaw identity;
- `ss_roll` = entry-hunger baseline, then random scratch;
- `ss_scratch_b` = current-food temporary during observation.

## State hygiene

At generation/reset:

- remove all four internal Aspect tags;
- remove all four internal Flaw tags;
- remove every persistent modifier before applying a new identity;
- clear all three observation tags after generation;
- reset player's `ss_roll` when it is no longer needed.

The existing remove-before-add modifier invariant remains absolute.

## Compatibility

Keep:

- historical internal tags/function paths for all eight finite mechanics;
- `test/awaken` as the historical command name;
- advancement ids under `shadowslave:test/*`;
- `ss_aspect` / `ss_flaw` objective names.

Existing worlds with old `ss_aspect=1..4` or `ss_flaw=1..4` plus current tags continue receiving
mechanics through the tags. `/trigger soul` includes legacy fallback display until reset/re-roll.

`test/awaken` clears old observation tags first because it skips the trial; its deterministic burden
family is therefore family 1, with only the personal family-1 name randomized.

## Acceptance criteria

Claude should add/adjust live assertions as needed. Observable contract:

1. Aspect identity recombines independent **nature + archetype** axes while exactly one internal
   Aspect mechanic tag is assigned;
2. new `ss_aspect` is in `11..14`, `21..24`, `31..34`, or `41..44`;
3. exactly one internal Flaw tag is assigned;
4. new `ss_flaw` is in the same encoded bands;
5. `test/awaken` / no strong signal deterministically selects family 1 (`ss_flaw=11..14`);
6. `ss_trial_bloodied` selects family 2 (`21..24`);
7. `ss_trial_hungry` selects family 3 (`31..34`) over bloodied;
8. `ss_trial_fled` selects family 4 (`41..44`) over hungry/bloodied;
9. identity variant still varies `1..4` inside the selected family;
10. accepted First-Nightmare entry clears observations from an earlier failed attempt;
11. `test/reset` clears observations and player's `ss_roll`;
12. `/trigger soul` shows composed/generated names, never the old player-facing fixed labels for a
    new roll;
13. existing Aspect/Flaw effects still function through compatibility tags;
14. re-rolling cannot leave an old attribute modifier behind;
15. normal entry, teardown, cooldown, death recovery and Sleeper progression are unchanged except for
    the two observation hooks.

## What must NOT change

- no rank beyond Sleeper/Dormant;
- no Dream Realm / Awakening implementation;
- no player NBT writes;
- no new Nightmare ownership/multiplayer architecture;
- no death-sweep redesign;
- no dimension/worldgen changes;
- no version stamp;
- no claim that these names or selection rules are canonical.

## Release relationship

This branch is independent of PR #1 (`gpt/datapack-release-completion`) and starts from current
`main`. PR #1 hardens tests/release packaging; this branch changes gameplay semantics. Claude can
review them separately and reconcile whichever lands second.
