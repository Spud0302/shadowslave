# Java handoff contract — datapack `1.0.0` baseline

**Written against:** `0.5.0` main plus the stacked `gpt/v0.6-experience` and `gpt/v0.7-hardening` completion branches.

**Purpose:** this is not a Java implementation plan. It is the behavioural and persistence contract the Java mod must preserve when the datapack is frozen at `1.0.0`.

The port is not a line-by-line translation of mcfunctions. Preserve the contracts; replace the machinery.

## 1. Behavioural contracts that survive the port

### Progression

```text
Mundane / untouched
  -> first ordinary sleep
Carrier / marked
  -> later sleep or deliberate bed interaction
First Nightmare
  -> successful central conflict
Sleeper / Dreamer, Soul Rank Dormant
```

First-Nightmare completion is **not Awakening**. Actual Awakening belongs after the first Dream Realm journey and successful return through a Gateway.

### Nightmare lifecycle

Datapack seams map cleanly to Java responsibilities:

| Datapack | Java contract |
| --- | --- |
| `nightmare/enter` | `NightmareService.tryEnter(player, source)` — every eligibility invariant in one choke point |
| `nightmare/objective_tick` | scenario/objective implementation; boss kill is one Phase-1 objective, not the definition of every Nightmare |
| `nightmare/leave` | `NightmareService.exit(instance, player, ExitReason)` — one teardown path |
| `nightmare/eject` | exit reason + consequences/presentation, not separate cleanup |
| `nightmare/survive` | objective completion -> exit -> progression |
| `prototype/observe_trial` | behaviour/evidence collection owned by the active Nightmare instance |

The Java implementation must add explicit Nightmare instance ownership. Do **not** port the datapack's global creature selector, shared bossbar or shared command storage as architecture.

## 2. Persistent datapack state to import

Only state that represents player identity/progression belongs in the importer. Scratch/runtime values do not.

### Spell/progression state

- `ss_carrier` tag and no completed rank -> `spellState = CARRIER`.
- `ss_rank >= 1` -> `spellState = DREAMER` and `soulRank = DORMANT` under the completed datapack contract.
- neither -> `spellState = UNTOUCHED`.

The historical prototype once *called* `ss_rank=1` Awakened, but current runtime semantics intentionally reinterpret that same stored value as Sleeper/Dormant. There is no reliable version marker on old player state that can reconstruct which label a player saw years earlier; the Java importer follows the frozen datapack behaviour rather than resurrecting the superseded label.

### Aspect identity

Current generated `ss_aspect` values are two digits:

- tens digit / nature + mechanical root:
  - `1x` = Veiled -> `ss_aspect_shadow`
  - `2x` = Ashen -> `ss_aspect_flame`
  - `3x` = Pale -> `ss_aspect_bone`
  - `4x` = Restless -> `ss_aspect_wind`
- ones digit / archetype:
  - `x1` = Witness
  - `x2` = Bearer
  - `x3` = Warden
  - `x4` = Wanderer

Import into an `AspectInstance`, not a registry enum. Suggested fields:

```text
AspectInstance
  instanceId
  name
  rank = DORMANT
  nature/root
  archetype
  legacyMechanicalRoot
  generationSeed?       # absent for datapack imports
  abilities[]
  evolutionHistory[]
  importedFromDatapack = true
```

Legacy datapack scores `1..4` may still exist. Preserve them as explicit legacy prototype identities using the matching mechanics tag; do not silently reroll them during import.

### Flaw identity

Current generated `ss_flaw` values are also two digits:

- tens digit / earned mechanical family:
  - `1x` = daylight burden -> internal compatibility tag `ss_flaw_shadow_slave`
  - `2x` = reduced-health burden -> `ss_flaw_fragile`
  - `3x` = hunger burden -> `ss_flaw_ravenous`
  - `4x` = unsafe-footing/fall burden -> `ss_flaw_weightless`
- ones digit selects the personal formal name within that family.

Import into a `FlawInstance` with the **player-facing generated name**, family and parameters. Never expose the internal compatibility id `shadow_slave` as the Flaw name; Shadow Slave is canonically an Aspect.

Suggested fields:

```text
FlawInstance
  instanceId
  formalName
  family
  parameters
  causalEvidence[]
  importedFromDatapack = true
```

The datapack does not retain full causal evidence after generation. For imported characters, record the family as known but mark detailed evidence as unavailable/legacy rather than inventing history.

## 3. State that must NOT become persistent Java architecture

These values are runtime/scratch machinery:

- `ss_timer`
- `ss_gone`
- `ss_health`
- `ss_roll`
- `ss_scratch_a`, `ss_scratch_b`
- `ss_ret_x`, `ss_ret_y`, `ss_ret_z`
- `ss_cooldown`
- `ss_in_nightmare`
- `ss_creature_spawned`
- `ss_test_bypass`
- `ss_trial_bloodied`, `ss_trial_hungry`, `ss_trial_fled`

Java may have analogous runtime fields, but they belong to typed player/session/Nightmare-instance objects rather than being imported as permanent soul identity.

## 4. Soul data boundary

The first Java implementation should separate concepts the datapack deliberately keeps shallow:

```text
SoulData
  schemaVersion
  spellState
  soulRank
  coreState
  aspect
  flaw
  attributes[]
  trueName?
  memories[]
  echoes[]
  dreamAnchor?
  corruption
  behaviorProfile
  progressionHistory
```

Do not turn `ss_rank` into a larger ordinal ladder. Spell state, Soul Rank, Aspect Rank, core saturation and exceptional core count/Class are separate concepts.

## 5. Import safety

Recommended one-time import order:

1. detect datapack identity/progression state;
2. construct Java `SoulData` in memory;
3. validate every translated field;
4. persist Java data with a migration/schema version;
5. mark the player imported;
6. only then remove/ignore legacy datapack values.

Never delete datapack tags/objectives first and hope construction succeeds afterward.

Migration should occur while no player is inside an active datapack Nightmare. If legacy `ss_in_nightmare` state is detected, fail safe: recover/teardown or defer import rather than constructing half an instance from global prototype state.

## 6. Resources to keep data-driven

The Java mod should continue shipping datapack-style resources where Minecraft already has a good data format:

- dimensions and dimension types;
- biomes/worldgen;
- predicates and tags;
- advancements;
- recipes/loot/structures when later phases add them.

Java owns state, behaviour, generation, networking, GUI, custom entities/AI and real Nightmare ownership. Do not rewrite JSON resources into Java merely because a mod loader exists.

## 7. Definition of translation success

The initial Java build is behaviourally compatible when a fresh player can perform the same `1.0.0` vertical slice and an imported datapack player retains their progression, generated Aspect identity, Flaw identity and effective mechanics.

Only after that compatibility baseline passes should Java-only Phase 2+ systems expand the game.
