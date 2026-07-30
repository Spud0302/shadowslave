# Phase 1 Aspect / Flaw rework

**Baseline:** `main@a470b914f3e0710d3dfee63adc29b8e6e50d4599` (`v1.4.9`)

**Branch:** `gpt/aspect-flaw-rework`

**Owner intent:** finish the datapack before moving the architecture boundary to Java. The current
four fixed Aspects / four independently random Flaws are the remaining explicit pre-Java gameplay
prototype called out by `docs/OPEN-QUESTIONS.md` Q2.

## Intent

Make the First Nightmare reward feel **personal rather than like choosing one of eight classes**,
while staying honest about what a vanilla datapack can actually generate.

Canon gives constraints, examples and themes, but **not an Aspect-generation formula**. Therefore the
pack must not present a made-up deterministic algorithm as lore. It should use a game system that is
canon-compatible rather than canon-claimed:

- Aspect identity is **composed from two independently rolled vocabularies**, not selected as one
  whole item from a larger fixed-name list.
- The first component expresses the finite Dormant mechanical nature; the second is an independent
  archetype. Their Cartesian product produces the player-facing identity.
- The Dormant mechanical expression remains one of four finite command implementations. Vanilla can
  compose identity, but it cannot invent a brand-new executable ability at runtime.
- Flaw **family** is derived from observable behavior in the successful First Nightmare when there is
  a strong signal.
- Randomness varies the personal Flaw identity inside that earned family, so two players who behave
  the same do not necessarily receive the same formal name.
- If no tracked behavior produced a strong signal, the family is random rather than pretending the
  game observed personality data that it did not.

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

The generated names in this spec are **fan-created game content**, not claimed canon names.

## Datapack ceiling

Do not fake arbitrary procedural mechanics.

A command pack can choose and compose values, names and presentation, but every distinct executable
effect still has to exist ahead of time. Phase 1 therefore uses:

```text
composed personal identity
        ↓
finite Dormant mechanical root
        ↓
future Java implementation can replace the root with data-driven/custom behavior
```

The existing internal tags remain the mechanical roots for save/test compatibility:

- `ss_aspect_shadow`
- `ss_aspect_flame`
- `ss_aspect_bone`
- `ss_aspect_wind`

The four historical Flaw tags remain internal burden-family ids. Player-facing identity no longer
uses those old raw labels.

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

This produces a Cartesian product rather than sixteen separately authored whole names:

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

The command implementation still renders the resulting pair with score-specific tellraw lines because
that is the simplest per-player persistence available in vanilla. Conceptually and mechanically,
however, the identity is generated from two independent components; adding a fifth archetype later
expands every nature rather than requiring a new hand-authored class.

### What is deliberately NOT claimed

- These words are not claimed canon terminology.
- The two-roll system is not claimed to be the Nightmare Spell's canonical algorithm.
- The four mechanics are not claimed to be the complete space of possible Aspects.
- A generated identity can share a mechanical root with another identity; that is the visible
  datapack ceiling the Java port later removes.

## Flaw observation

A First Nightmare run records three **strong, directly observable** behavior signals. They are reset
at every accepted entry so an earlier failed attempt cannot contaminate the next attempt.

### `ss_trial_bloodied`

Set after the creature exists if sampled health reaches **5..8 HP**.

Why this range:

- normal entry refuses `..9`, so reaching 8 or less proves health fell during the trial;
- `..4` triggers ejection/death teardown, so 5..8 is the survivable near-collapse window.

### `ss_trial_hungry`

At entry, store the player's FoodLevel in the existing `ss_roll` scratch objective. During the fight,
compare current FoodLevel with that baseline. Set the tag after a **drop of at least 6 food points**.

This is better than checking absolute hunger: entering the Nightmare already hungry must not be
misread as behavior performed during the trial.

### `ss_trial_fled`

Set if the Nightmare Creature is ever **40+ blocks** from the player before the existing 48-block
leash teleports it back.

This captures deliberate disengagement without changing the leash or trial outcome.

### No invented fourth signal

Do not manufacture a personality judgement for an ordinary fight. If none of the three strong
signals occurred, Flaw family is random. `UNKNOWN`/unobserved is better than pretending the pack
measured courage, mercy or aggression when it did not.

## Flaw family selection

Priority when multiple strong signals occurred:

1. fled;
2. hungry;
3. bloodied;
4. otherwise random family `1..4`.

The priority intentionally favors the most behaviorally specific signal. Being bloodied can happen
in almost every close fight; creating 40 blocks of separation or burning six food points is more
distinctive.

After family selection, roll `1..4` for a personal identity variant and encode
`family * 10 + variant` in `ss_flaw`.

### Family 1 — night / daylight burden

Internal compatibility tag/function: `ss_flaw_shadow_slave` / `flaw/shadow_slave`.
The old player-facing name **Shadow Slave** is removed because that is canonically an Aspect name.

| Score | Generated Flaw identity |
| --- | --- |
| 11 | Nightbound |
| 12 | Pale Dawn |
| 13 | Sunshy |
| 14 | Dusk's Debt |

Burden: direct sunlight hurts and weakens the player.

### Family 2 — fragile-body burden

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

### Family 4 — footing/fall burden

Internal tag: `ss_flaw_weightless`.

| Score | Generated Flaw identity |
| --- | --- |
| 41 | Rootless |
| 42 | False Wings |
| 43 | Unsteady Ground |
| 44 | Falling Debt |

Burden: safe falling distance is reduced.

## Required implementation shape

### GPT-owned content files

- replace `prototype/roll_aspect_flaw.mcfunction` with the generated-identity / earned-family logic;
- add `prototype/trial_begin.mcfunction` to reset observations and capture starting hunger;
- add `prototype/observe_trial.mcfunction` to record the three behavior signals;
- update `soul.mcfunction` to show composed Aspect / generated Flaw identities and honest Dormant
  expressions;
- update the historical `flaw/shadow_slave.mcfunction` comment so internal compatibility naming is
  not mistaken for player/canon naming.

### Cross-column hooks that this branch must call out for Claude

Two one-line state-machine hooks are required:

- `nightmare/enter.mcfunction`: after entry is accepted, call `prototype/trial_begin` before the
  teleport/trial begins;
- `nightmare/tick_player.mcfunction`: while the creature exists, call `prototype/observe_trial`
  **before** the existing distance leash can erase the `40+` observation.

`test/reset.mcfunction` also clears the three trial-observation tags and the player's `ss_roll`
scratch value, because reset promises genuinely fresh state.

These are cross-column edits under the current collaboration split. They are small, named here in
advance, and Claude must scrutinize/test them.

### No new objective

Reuse:

- `ss_aspect` for encoded Aspect identity;
- `ss_flaw` for encoded Flaw identity;
- `ss_roll` as temporary starting-hunger/random scratch;
- `ss_scratch_b` as current-food temporary scratch inside trial observation.

Update the comments in `init.mcfunction`, but do not add another persistent objective merely to make
the implementation easier.

## State hygiene

At generation/reset:

- remove all four internal Aspect tags;
- remove all four internal Flaw tags;
- remove every persistent modifier before the new identity is applied;
- clear all three observation tags after generation;
- reset the player's `ss_roll` once generation no longer needs it.

The existing modifier invariant remains absolute: remove before add, every time.

## Compatibility

Keep:

- historical internal tags and function paths for the four mechanical roots;
- `test/awaken` as the historical command name;
- advancement ids under `shadowslave:test/*`;
- `ss_aspect` / `ss_flaw` objective names.

Change only their value semantics/player-facing interpretation.

Existing worlds with old `ss_aspect=1..4` or `ss_flaw=1..4` but current tags still continue to receive
mechanics through the tags. `/trigger soul` includes a small legacy fallback display for those old
score ranges until the player is reset/re-rolled.

## Acceptance criteria

Claude should add/adjust live assertions as needed, but the observable contract is:

1. repeated `test/awaken` runs demonstrate independent recombination of **nature + archetype**, not
   merely selection from the old four whole Aspect labels, while assigning exactly one internal
   Aspect mechanic tag;
2. `ss_aspect` after a new roll is one of `11..14`, `21..24`, `31..34`, `41..44`;
3. exactly one internal Flaw tag is assigned;
4. `ss_flaw` after a new roll is in the same encoded ranges;
5. an ordinary `test/awaken` with no trial observations uses a random Flaw family;
6. `ss_trial_bloodied` makes family 2 win over the random fallback;
7. `ss_trial_hungry` makes family 3 win over bloodied;
8. `ss_trial_fled` makes family 4 win over hungry/bloodied;
9. Flaw identity still varies `1..4` within the selected family;
10. entering a new First Nightmare clears observation tags from an earlier attempt;
11. `test/reset` clears observations and `ss_roll`;
12. `/trigger soul` displays composed/generated names, never the old player-facing
    `Aspect: Shadow/Flame/Bone/Wind` or `Flaw: Shadow Slave/Fragile/Ravenous/Weightless` for a new roll;
13. the existing Aspect/Flaw effects still function through the compatibility tags;
14. re-rolling cannot leave an old attribute modifier behind;
15. normal First-Nightmare entry, teardown, cooldown, death recovery and Sleeper progression remain
    unchanged except for the two observation hooks.

## What must NOT change in this branch

- no rank beyond Sleeper/Dormant;
- no Dream Realm / Awakening implementation;
- no player NBT writes;
- no new Nightmare ownership/multiplayer architecture;
- no death-sweep redesign;
- no dimension/worldgen changes;
- no version stamp;
- no claim that a generated name or this selection algorithm is canonical.

## Release relationship

This branch is intentionally independent of PR #1 (`gpt/datapack-release-completion`) and starts from
current `main`. PR #1 hardens tests/release packaging; this branch changes gameplay semantics. Claude
can review them separately and reconcile whichever lands second.
