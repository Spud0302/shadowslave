# Phase 1 "The First Nightmare" Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a vanilla Minecraft 1.21.1 datapack where sleeping pulls the player into a timed nightmare dimension, surviving it and killing the Nightmare Creature makes them Awakened, and Awakening grants one random Aspect and one random Flaw.

**Architecture:** Pure datapack — JSON for the dimension, biome and advancement trigger; `.mcfunction` for all logic. All player state lives in scoreboard objectives and tags. Entry is driven by the `minecraft:slept_in_bed` advancement trigger rather than tick polling; only players tagged as being inside the nightmare cost anything per tick.

**Tech Stack:** Minecraft Java Edition 1.21.1 vanilla datapack (`pack_format` 48). Python 3.12 for the structure validator only — it never runs inside the game.

## Global Constraints

- **Minecraft version: 1.21.1.** `pack_format` is **48** (covers 1.21–1.21.1 only).
- **Directory names are singular** — `function`, `advancement`, `loot_table`, `predicate`, `dimension`, `dimension_type`, `tags/function`. Plural names silently do nothing in 1.21.
- **`dimension_type` uses the 1.21.1 field set** — `ultrawarm`, `natural`, `bed_works`, `respawn_anchor_works`, `piglin_safe`, `effects`, `has_raids`. Do **not** use the "environment attributes" format from 25w42a+ snapshots; it does not exist in 1.21.1.
- **`/attribute modifier add` takes a namespaced `<id>`, not UUID+name** (changed 24w21b). Operations are `add_value`, `add_multiplied_base`, `add_multiplied_total`.
- **Namespace everything `shadowslave`** — functions, tags, scoreboards, bossbars, attribute modifier ids.
- **Naming: `lower_snake_case`** for all files and folders.
- **No Java, no mod loader, no external dependencies.** If a task seems to need one, stop and raise it.
- **Versioning: Pride Versioning** (`PROUD.DEFAULT.SHAME`). Phase 1 ships as `1.0.0`. **Tag first, then bump SHAME for every round of fixes** — do not hold a release back until everything is fixed, that erases the record of what it took. Everything in `ISSUES.md` is SHAME work.
- **Minecraft cannot run on this machine.** Automated verification is limited to the structure validator. Every task also carries an **In-game check** the user runs manually; do not claim a task works because the validator passed.

---

## File Structure

```
shadowslave/
  pack.mcmeta                                  # pack_format 48, description
  tools/validate.py                            # structure + JSON validator (dev only, not shipped)
  data/
    minecraft/tags/function/
      load.json                                # -> shadowslave:init
      tick.json                                # -> shadowslave:tick
    shadowslave/
      dimension_type/nightmare.json            # dark, no bed, no respawn anchor
      worldgen/biome/nightmare.json            # black fog, heavy hostile spawns
      dimension/nightmare.json                 # noise generator, fixed biome
      advancement/
        enter_nightmare.json                   # slept_in_bed trigger -> enter function
        test/                                  # impossible-trigger tree; granted by command
          root.json  chosen.json  endured.json
          slayer.json  awakened.json  cast_out.json
          aspect_live.json  flaw_live.json  bypass.json
      predicate/
        in_darkness.json                       # light level <= 7, for the Shadow Aspect
        in_sunlight.json                       # bright, sky-visible, daytime — for Shadow Slave
      function/
        init.mcfunction                        # objectives + bossbar, on load
        tick.mcfunction                        # dispatch: nightmare players, upkeep clock
        upkeep.mcfunction                      # per-Awakened Aspect/Flaw dispatch, 1/second
        soul.mcfunction                        # /trigger soul readout
        nightmare/
          enter.mcfunction                     # store return pos, teleport in, start timer
          tick_player.mcfunction               # countdown, health watch, creature watch
          spawn_creature.mcfunction            # summon the Nightmare Creature
          survive.mcfunction                   # creature dead -> awaken and return
          eject.mcfunction                     # near-death -> return, no Awakening
          leave.mcfunction                     # shared teardown + return teleport
          return.mcfunction                    # macro: $tp @s $(x) $(y) $(z) — player NBT can't be written directly
        awaken/
          roll.mcfunction                      # roll aspect + flaw, set rank
        aspect/
          shadow.mcfunction  flame.mcfunction
          bone.mcfunction    wind.mcfunction
        flaw/
          shadow_slave.mcfunction  fragile.mcfunction
          ravenous.mcfunction      weightless.mcfunction
        test/selfcheck.mcfunction              # in-game assertions
```

**Responsibility split:** `nightmare/` owns the trial lifecycle, `awaken/` owns the one-time rank/roll transition, `aspect/` and `flaw/` own recurring per-tick effects. `leave.mcfunction` exists so `survive` and `eject` share exactly one teardown path — a bug in teardown can only live in one file.

---

### Task 1: Pack scaffold, state, and the validator

**Files:**

- Create: `shadowslave/pack.mcmeta`
- Create: `shadowslave/tools/validate.py`
- Create: `shadowslave/data/minecraft/tags/function/load.json`
- Create: `shadowslave/data/minecraft/tags/function/tick.json`
- Create: `shadowslave/data/shadowslave/function/init.mcfunction`
- Create: `shadowslave/data/shadowslave/function/tick.mcfunction`

**Interfaces:**

- Consumes: nothing.
- Produces: scoreboard objectives `ss_rank`, `ss_timer`, `ss_aspect`, `ss_flaw`, `ss_roll`, `ss_clock`, `ss_health`, `ss_ret_x`, `ss_ret_y`, `ss_ret_z` (all `dummy`) and `soul` (`trigger`) — eleven in total; bossbar `shadowslave:trial`; tags `ss_in_nightmare`, `ss_creature_spawned`; entry points `shadowslave:init` and `shadowslave:tick`.

- [ ] **Step 1: Write the failing validator**

Create `shadowslave/tools/validate.py`:

```python
#!/usr/bin/env python3
"""Structure + JSON validator for the shadowslave datapack.

Minecraft cannot run on the build box, so this checks the things that are
statically checkable: pack_format, singular directory names, JSON validity,
and that every function referenced by a function tag actually exists.
"""
import json
import sys
from pathlib import Path

PACK = Path(__file__).resolve().parent.parent
DATA = PACK / "data"
REQUIRED_PACK_FORMAT = 48

# Plural names silently do nothing in 1.21 — catching them is the whole point.
PLURAL_TRAPS = {
    "functions", "advancements", "loot_tables", "predicates",
    "recipes", "structures", "item_modifiers", "dimensions", "dimension_types",
}

errors = []


def check_pack_mcmeta():
    path = PACK / "pack.mcmeta"
    if not path.is_file():
        errors.append("pack.mcmeta is missing")
        return
    meta = json.loads(path.read_text())
    fmt = meta.get("pack", {}).get("pack_format")
    if fmt != REQUIRED_PACK_FORMAT:
        errors.append(f"pack_format is {fmt!r}, expected {REQUIRED_PACK_FORMAT} (1.21.1)")


def check_no_plural_dirs():
    for d in DATA.rglob("*"):
        if d.is_dir() and d.name in PLURAL_TRAPS:
            errors.append(f"plural directory {d.relative_to(PACK)} — 1.21 requires singular")


def check_json_parses():
    for f in DATA.rglob("*.json"):
        try:
            json.loads(f.read_text())
        except json.JSONDecodeError as exc:
            errors.append(f"{f.relative_to(PACK)}: invalid JSON — {exc}")


def function_path(ref):
    """shadowslave:nightmare/enter -> data/shadowslave/function/nightmare/enter.mcfunction"""
    namespace, _, name = ref.partition(":")
    return DATA / namespace / "function" / f"{name}.mcfunction"


def check_tagged_functions_exist():
    for tag in (DATA / "minecraft" / "tags" / "function").glob("*.json"):
        try:
            values = json.loads(tag.read_text()).get("values", [])
        except json.JSONDecodeError:
            continue  # already reported by check_json_parses
        for ref in values:
            if isinstance(ref, str) and not function_path(ref).is_file():
                errors.append(f"{tag.name} references missing function {ref}")


def main():
    check_pack_mcmeta()
    check_no_plural_dirs()
    check_json_parses()
    check_tagged_functions_exist()
    if errors:
        for e in errors:
            print(f"FAIL: {e}")
        return 1
    print("OK: datapack structure valid")
    return 0


if __name__ == "__main__":
    sys.exit(main())
```

- [ ] **Step 2: Run it to verify it fails**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: FAIL — `pack.mcmeta is missing`, exit code 1.

- [ ] **Step 3: Create the pack manifest and function tags**

`shadowslave/pack.mcmeta`:

```json
{
  "pack": {
    "pack_format": 48,
    "description": "Shadow Slave — the Nightmare Spell. Sleep, and be Chosen."
  }
}
```

`shadowslave/data/minecraft/tags/function/load.json`:

```json
{
  "values": ["shadowslave:init"]
}
```

`shadowslave/data/minecraft/tags/function/tick.json`:

```json
{
  "values": ["shadowslave:tick"]
}
```

- [ ] **Step 4: Write init.mcfunction**

`shadowslave/data/shadowslave/function/init.mcfunction`:

```mcfunction
# Shadow Slave — one-time setup, runs on every /reload and world load.

# Rank: 0 = Sleeper, 1 = Awakened
scoreboard objectives add ss_rank dummy "Soul Rank"
# Nightmare countdown, in ticks
scoreboard objectives add ss_timer dummy "Nightmare Timer"
# Rolled Aspect and Flaw, 1-4 each
scoreboard objectives add ss_aspect dummy "Aspect"
scoreboard objectives add ss_flaw dummy "Flaw"
# Scratch space for /random rolls
scoreboard objectives add ss_roll dummy "Roll"
# Global once-a-second counter, held on the fake player $ss_clock
scoreboard objectives add ss_clock dummy "Clock"
# Health sampled each tick while in the nightmare
scoreboard objectives add ss_health dummy "Health"
# Consecutive ticks the creature has been absent — guards against a rejoin, where the
# entity's chunk has not deserialized yet, being misread as "the creature died".
scoreboard objectives add ss_gone dummy "Creature Absent"
# Overworld return position
scoreboard objectives add ss_ret_x dummy "Return X"
scoreboard objectives add ss_ret_y dummy "Return Y"
scoreboard objectives add ss_ret_z dummy "Return Z"
# Player-facing readout, enabled per-player in tick
scoreboard objectives add soul trigger "Soul"

# The trial bossbar. Global, which is fine for singleplayer.
# ponytail: one shared bossbar — per-player bars need macro-generated ids, add in Phase 6 multiplayer
bossbar add shadowslave:trial {"text":"The Nightmare Spell","color":"dark_purple"}
bossbar set shadowslave:trial color purple
bossbar set shadowslave:trial style notched_10
# Bossbars survive /reload. tick_player.mcfunction re-shows this for anyone
# still mid-trial, since a server restart fires this before any player joins.
bossbar set shadowslave:trial visible false

tellraw @a {"text":"[Shadow Slave] The Spell stirs.","color":"dark_gray","italic":true}
```

- [ ] **Step 5: Write tick.mcfunction**

`shadowslave/data/shadowslave/function/tick.mcfunction`:

```mcfunction
# Shadow Slave — per-tick dispatch. Keep this file cheap: it runs 20x/second.

# Let every player use /trigger soul again this tick.
scoreboard players enable @a soul
execute as @a[scores={soul=1..}] run function shadowslave:soul

# Only players actually inside a nightmare cost anything.
execute as @a[tag=ss_in_nightmare] at @s run function shadowslave:nightmare/tick_player
```

- [ ] **Step 6: Run the validator to verify it passes**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: `OK: datapack structure valid`, exit code 0.

- [ ] **Step 7: In-game check**

Copy `shadowslave/` into a test world's `datapacks/` folder, then:

```
/reload
/scoreboard objectives list
```

Expected: the chat line "The Spell stirs." appears, and all eleven objectives are listed (ten `dummy` plus `soul`).

- [ ] **Step 8: Commit**

```bash
cd /project/src/shadowslave
git add shadowslave/pack.mcmeta shadowslave/tools/validate.py shadowslave/data
git commit -m "feat: datapack scaffold, state objectives, and structure validator"
```

---

### Task 2: The nightmare dimension

**Files:**

- Create: `shadowslave/data/shadowslave/dimension_type/nightmare.json`
- Create: `shadowslave/data/shadowslave/worldgen/biome/nightmare.json`
- Create: `shadowslave/data/shadowslave/dimension/nightmare.json`

**Interfaces:**

- Consumes: nothing.
- Produces: the dimension id `shadowslave:nightmare`, usable as `execute in shadowslave:nightmare`.

- [ ] **Step 1: Write the dimension type**

`shadowslave/data/shadowslave/dimension_type/nightmare.json`. These are the **1.21.1** fields — the current wiki documents a newer "environment attributes" format that does not exist in 1.21.1:

```json
{
  "ultrawarm": false,
  "natural": false,
  "piglin_safe": false,
  "respawn_anchor_works": false,
  "bed_works": false,
  "has_raids": false,
  "has_skylight": false,
  "has_ceiling": false,
  "coordinate_scale": 1.0,
  "ambient_light": 0.0,
  "fixed_time": 18000,
  "min_y": -64,
  "height": 256,
  "logical_height": 256,
  "infiniburn": "#minecraft:infiniburn_overworld",
  "effects": "minecraft:the_nether",
  "monster_spawn_block_light_limit": 15,
  "monster_spawn_light_level": {
    "type": "minecraft:uniform",
    "value": {
      "min_inclusive": 0,
      "max_inclusive": 15
    }
  }
}
```

`fixed_time: 18000` is midnight. `has_skylight: false` plus `ambient_light: 0.0` means true darkness. `monster_spawn_block_light_limit: 15` lets hostiles spawn at any light level — the nightmare should never feel safe.

- [ ] **Step 2: Write the biome**

`shadowslave/data/shadowslave/worldgen/biome/nightmare.json`:

```json
{
  "temperature": 0.0,
  "downfall": 0.0,
  "has_precipitation": false,
  "effects": {
    "sky_color": 0,
    "fog_color": 1310740,
    "water_color": 1183506,
    "water_fog_color": 655370,
    "grass_color": 4079166,
    "mood_sound": {
      "sound": "minecraft:ambient.cave",
      "tick_delay": 3000,
      "block_search_extent": 8,
      "offset": 2.0
    }
  },
  "spawners": {
    "monster": [
      {
        "type": "minecraft:zombie",
        "weight": 40,
        "minCount": 2,
        "maxCount": 4
      },
      {
        "type": "minecraft:skeleton",
        "weight": 30,
        "minCount": 1,
        "maxCount": 3
      },
      {
        "type": "minecraft:spider",
        "weight": 20,
        "minCount": 1,
        "maxCount": 2
      },
      {
        "type": "minecraft:phantom",
        "weight": 10,
        "minCount": 1,
        "maxCount": 1
      }
    ],
    "creature": [],
    "ambient": [],
    "axolotls": [],
    "underground_water_creature": [],
    "water_creature": [],
    "water_ambient": [],
    "misc": []
  },
  "spawn_costs": {},
  "carvers": {},
  "features": []
}
```

- [ ] **Step 3: Write the dimension**

`shadowslave/data/shadowslave/dimension/nightmare.json`:

```json
{
  "type": "shadowslave:nightmare",
  "generator": {
    "type": "minecraft:noise",
    "settings": "minecraft:overworld",
    "biome_source": {
      "type": "minecraft:fixed",
      "biome": "shadowslave:nightmare"
    }
  }
}
```

Reusing `minecraft:overworld` noise settings gives real, walkable terrain for free. Bespoke noise settings are a Phase 2+ concern.

- [ ] **Step 4: Run the validator to verify it passes**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: `OK: datapack structure valid`.

- [ ] **Step 5: In-game check**

```
/reload
/execute in shadowslave:nightmare run tp @s 0 100 0
```

Expected: you are teleported into a pitch-dark world with overworld-shaped terrain. Confirm `/time set day` does **not** brighten it (fixed time) and that placing and using a bed fails.

Then return: `/execute in minecraft:overworld run tp @s 0 100 0`

- [ ] **Step 6: Commit**

```bash
cd /project/src/shadowslave
git add shadowslave/data/shadowslave/dimension_type shadowslave/data/shadowslave/worldgen shadowslave/data/shadowslave/dimension
git commit -m "feat: add the nightmare dimension, biome and dimension type"
```

---

### Task 3: Entering the nightmare

**Files:**

- Create: `shadowslave/data/shadowslave/advancement/enter_nightmare.json`
- Create: `shadowslave/data/shadowslave/function/nightmare/enter.mcfunction`

**Interfaces:**

- Consumes: objectives and tags from Task 1; dimension `shadowslave:nightmare` from Task 2.
- Produces: tag `ss_in_nightmare` on the player, `ss_timer` set to 6000, `ss_ret_x/y/z` holding the Overworld return position.

- [ ] **Step 1: Write the advancement trigger**

The `Sleeping` NBT was removed from the data model in 1.14, so sleep is detected via the advancement trigger. This also costs nothing per tick, unlike polling.

`shadowslave/data/shadowslave/advancement/enter_nightmare.json`:

```json
{
  "criteria": {
    "slept": {
      "trigger": "minecraft:slept_in_bed"
    }
  },
  "requirements": [["slept"]],
  "rewards": {
    "function": "shadowslave:nightmare/enter"
  }
}
```

There is no `display` block, so the advancement is hidden — it is a trigger, not something the player earns.

- [ ] **Step 2: Write the enter function**

`shadowslave/data/shadowslave/function/nightmare/enter.mcfunction`:

```mcfunction
# Runs as and at the player, from the slept_in_bed advancement reward.

# Revoke immediately so the trigger can fire again on the next sleep.
advancement revoke @s only shadowslave:enter_nightmare

# Only Sleepers are Chosen. Awakened players sleep normally.
execute if score @s ss_rank matches 1.. run return 0
# Guard against re-entry if something fires twice.
execute if entity @s[tag=ss_in_nightmare] run return 0

# Remember where to put them back.
execute store result score @s ss_ret_x run data get entity @s Pos[0]
execute store result score @s ss_ret_y run data get entity @s Pos[1]
execute store result score @s ss_ret_z run data get entity @s Pos[2]

tag @s add ss_in_nightmare
scoreboard players set @s ss_timer 6000
scoreboard players set @s ss_gone 0

# Pull them in. Teleporting wakes the player out of the bed.
# `execute in <dimension>` scopes ONLY the command chained to its `run` — it does not
# persist to the next line. Every line that must act inside the nightmare needs its own
# `execute in`, and the final offset needs `at @s` so `~` resolves against the player's
# post-spread position rather than the bed they left.
execute in shadowslave:nightmare run tp @s 0 120 0
execute in shadowslave:nightmare run spreadplayers 0 0 200 400 false @s
# The Spell takes you whole. Also prevents an entry loop for players who slept while hurt.
effect give @s minecraft:instant_health 1 5 true

bossbar set shadowslave:trial max 6000
bossbar set shadowslave:trial value 6000
bossbar set shadowslave:trial name {"text":"The Nightmare Spell","color":"light_purple"}
bossbar set shadowslave:trial color purple
bossbar set shadowslave:trial visible true
bossbar set shadowslave:trial players @s

execute at @s run playsound minecraft:ambient.cave ambient @s ~ ~ ~ 1 0.5
title @s times 20 60 20
title @s subtitle {"text":"Survive.","color":"gray"}
title @s title {"text":"The Nightmare Spell","color":"dark_purple","bold":true}
```

> **In-game verification needed:** teleporting a player mid-sleep is the one behaviour here I could not confirm from documentation. If the player stays stuck in the bed, the fallback is to wrap the teleport in `schedule function shadowslave:nightmare/enter_delayed 1t` so it runs the tick after the bed animation starts.

- [ ] **Step 3: Run the validator**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: `OK: datapack structure valid`.

- [ ] **Step 4: In-game check**

```
/reload
/scoreboard players set @s ss_rank 0
```

Place a bed, wait for night, and sleep.

Expected: you are pulled into the dark dimension, a purple bossbar reads "The Nightmare Spell", and a title card appears. Then confirm the Awakened path is skipped:

```
/scoreboard players set @s ss_rank 1
```

Sleep again — expected: you sleep normally and are **not** pulled in.

- [ ] **Step 5: Commit**

```bash
cd /project/src/shadowslave
git add shadowslave/data/shadowslave/advancement shadowslave/data/shadowslave/function/nightmare
git commit -m "feat: pull sleeping Sleepers into the nightmare via slept_in_bed trigger"
```

---

### Task 4: The trial timer and near-death ejection

**Files:**

- Create: `shadowslave/data/shadowslave/function/nightmare/tick_player.mcfunction`
- Create: `shadowslave/data/shadowslave/function/nightmare/leave.mcfunction`
- Create: `shadowslave/data/shadowslave/function/nightmare/eject.mcfunction`

**Interfaces:**

- Consumes: tag `ss_in_nightmare`, objectives `ss_timer`, `ss_health`, `ss_ret_x/y/z`.
- Produces: `shadowslave:nightmare/leave` as the single shared teardown path; calls `shadowslave:nightmare/spawn_creature` (Task 5) when the timer expires.

- [ ] **Step 1: Write the per-player tick**

`shadowslave/data/shadowslave/function/nightmare/tick_player.mcfunction`:

```mcfunction
# Runs as and at each player inside a nightmare, every tick.

# Sample health so we can eject before a real death drops their gear.
# ponytail: NBT reads on players are expensive, but this only runs for players
# ponytail: actually inside a nightmare — a handful at most
execute store result score @s ss_health run data get entity @s Health
execute if score @s ss_health matches ..8 run function shadowslave:nightmare/eject
execute if entity @s[tag=!ss_in_nightmare] run return 0

# Count down.
scoreboard players remove @s ss_timer 1
execute store result bossbar shadowslave:trial value run scoreboard players get @s ss_timer
# Self-healing after a /reload or restart, which hides the bar via init.
bossbar set shadowslave:trial visible true

# Timer expired and no creature yet: summon it.
execute if score @s ss_timer matches ..0 unless entity @s[tag=ss_creature_spawned] run function shadowslave:nightmare/spawn_creature

# You cannot outrun the Nightmare. Leashing the creature also makes the absence test below
# a genuine "it died" signal rather than "I walked away".
execute if entity @s[tag=ss_creature_spawned] run tp @e[tag=ss_creature,distance=48..] ~ ~ ~

# Creature was summoned and is now gone: the trial is won. Require the absence to
# hold for two seconds so a rejoin (chunk not yet deserialized) can't be misread
# as a kill.
execute if entity @e[tag=ss_creature] run scoreboard players set @s ss_gone 0
execute if entity @s[tag=ss_creature_spawned] unless entity @e[tag=ss_creature] run scoreboard players add @s ss_gone 1
execute if score @s ss_gone matches 40.. run function shadowslave:nightmare/survive
```

- [ ] **Step 2: Write the shared teardown**

`shadowslave/data/shadowslave/function/nightmare/leave.mcfunction`:

```mcfunction
# Single teardown path shared by survive and eject. Runs as the player.

tag @s remove ss_in_nightmare
tag @s remove ss_creature_spawned
scoreboard players set @s ss_timer 0
scoreboard players set @s ss_gone 0

# Kills every creature in the nightmare, not just this player's. Phase 1 is single-player
# at a time — same limitation as the shared bossbar. Per-player creature ownership would
# need owner tags; deferred with it.
execute in shadowslave:nightmare run kill @e[tag=ss_creature]

bossbar set shadowslave:trial visible false
bossbar set shadowslave:trial players

# Put them back where they slept.
# Players cannot have their NBT written, so the return position goes through storage
# and a macro function — the only way to feed dynamic coordinates to /tp.
execute store result storage shadowslave:ret x int 1 run scoreboard players get @s ss_ret_x
execute store result storage shadowslave:ret y int 1 run scoreboard players get @s ss_ret_y
execute store result storage shadowslave:ret z int 1 run scoreboard players get @s ss_ret_z
execute in minecraft:overworld run function shadowslave:nightmare/return with storage shadowslave:ret
```

`shadowslave/data/shadowslave/function/nightmare/return.mcfunction`:

```mcfunction
$tp @s $(x) $(y) $(z)
```

- [ ] **Step 3: Write the ejection path**

`shadowslave/data/shadowslave/function/nightmare/eject.mcfunction`:

```mcfunction
# Near-death: the Spell spits them out. Gear intact, no Awakening.

function shadowslave:nightmare/leave

# Shaken.
effect give @s minecraft:blindness 5 0 true
effect give @s minecraft:nausea 8 0 true

# `leave` already teleported the player, but the ambient context here is still the stale
# nightmare position — a function call does not re-derive it. `at @s` re-reads the player's
# current position and dimension, without which the sound plays at coordinates they left.
execute at @s run playsound minecraft:entity.wither.spawn master @s ~ ~ ~ 0.4 0.5
title @s times 10 50 20
title @s subtitle {"text":"You were not ready.","color":"dark_gray"}
title @s title {"text":"Cast Out","color":"dark_red","bold":true}
tellraw @s {"text":"The Nightmare rejected you. Sleep again to face it.","color":"gray","italic":true}
```

- [ ] **Step 4: Run the validator**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: `OK: datapack structure valid`.

- [ ] **Step 5: In-game check**

```
/reload
/scoreboard players set @s ss_rank 0
```

Sleep to enter, then confirm the countdown: the bossbar should drain steadily. Then force ejection:

```
/effect give @s minecraft:instant_damage 1 3
```

Expected: you are returned to your bed's position at half a heart with blindness, your gear still in your inventory, the bossbar hidden, and the "Cast Out" title shown.

- [ ] **Step 6: Commit**

```bash
cd /project/src/shadowslave
git add shadowslave/data/shadowslave/function/nightmare
git commit -m "feat: trial countdown and near-death ejection with shared teardown"
```

---

### Task 5: The Nightmare Creature

**Files:**

- Create: `shadowslave/data/shadowslave/function/nightmare/spawn_creature.mcfunction`

**Interfaces:**

- Consumes: tag `ss_in_nightmare`, bossbar `shadowslave:trial`.
- Produces: an entity tagged `ss_creature`; tag `ss_creature_spawned` on the player. Task 4's tick watches for the creature's absence.

- [ ] **Step 1: Write the spawn function**

Lore-accurate: a First Nightmare is tailored to one Aspirant, so exactly one creature appears, and never above Devil class. This is a Monster-class creature (2 soul cores' worth of threat).

`shadowslave/data/shadowslave/function/nightmare/spawn_creature.mcfunction`:

```mcfunction
# Runs as and at the player. The timer has expired; the Nightmare shows its face.

# Attribute format is 1.20.5+: lowercase `attributes`, with `id` and `base`.
summon minecraft:ravager ^ ^1 ^12 {Tags:["ss_creature"],CustomName:'{"text":"Nightmare Creature","color":"dark_purple","bold":true}',CustomNameVisible:1b,PersistenceRequired:1b,attributes:[{id:"minecraft:generic.max_health",base:160},{id:"minecraft:generic.attack_damage",base:4},{id:"minecraft:generic.movement_speed",base:0.32},{id:"minecraft:generic.knockback_resistance",base:0.8},{id:"minecraft:generic.follow_range",base:64}],Health:160f,ActiveEffects:[{id:"minecraft:fire_resistance",amplifier:0b,duration:-1,show_particles:0b}]}

tag @s add ss_creature_spawned

bossbar set shadowslave:trial name {"text":"Nightmare Creature","color":"dark_red"}
bossbar set shadowslave:trial color red
bossbar set shadowslave:trial max 160
bossbar set shadowslave:trial value 160

playsound minecraft:entity.ravager.roar hostile @s ~ ~ ~ 2 0.6
title @s times 10 50 20
title @s subtitle {"text":"Kill it, or be killed.","color":"dark_gray"}
title @s title {"text":"It Has Found You","color":"dark_red","bold":true}
```

- [ ] **Step 2: Track the creature's health on the bossbar**

Append to `shadowslave/data/shadowslave/function/nightmare/tick_player.mcfunction`, immediately after the countdown block:

```mcfunction
# Once the creature exists, the bar tracks its health instead of the timer.
execute if entity @s[tag=ss_creature_spawned] store result bossbar shadowslave:trial value run data get entity @e[tag=ss_creature,limit=1] Health
```

This replaces the timer-driven `bossbar set ... value` once the creature is up. Both lines run, but the creature line executes second and wins.

- [ ] **Step 3: Run the validator**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: `OK: datapack structure valid`.

- [ ] **Step 4: In-game check**

```
/reload
/scoreboard players set @s ss_rank 0
```

Sleep in, then skip the wait:

```
/scoreboard players set @s ss_timer 1
```

Expected: a named "Nightmare Creature" ravager spawns ahead of you, the bossbar turns red and reads its name, and the bar tracks its health as you damage it.

- [ ] **Step 5: Commit**

```bash
cd /project/src/shadowslave
git add shadowslave/data/shadowslave/function/nightmare
git commit -m "feat: summon the Nightmare Creature and track it on the trial bossbar"
```

---

### Task 6: Surviving, and the Awakening roll

**Files:**

- Create: `shadowslave/data/shadowslave/function/nightmare/survive.mcfunction`
- Create: `shadowslave/data/shadowslave/function/awaken/roll.mcfunction`

**Interfaces:**

- Consumes: `shadowslave:nightmare/leave` (Task 4).
- Produces: `ss_rank` set to 1, `ss_aspect` and `ss_flaw` each set to 1–4, tags `ss_aspect_shadow`/`ss_aspect_flame`/`ss_aspect_bone`/`ss_aspect_wind` and `ss_flaw_shadow_slave`/`ss_flaw_fragile`/`ss_flaw_ravenous`/`ss_flaw_weightless`.

- [ ] **Step 1: Write the survive function**

`shadowslave/data/shadowslave/function/nightmare/survive.mcfunction`:

```mcfunction
# The creature is dead. Runs as the player.

function shadowslave:nightmare/leave
function shadowslave:awaken/roll

# Same stale-context trap as eject: `leave` teleported the player, so `at @s` is required.
execute at @s run playsound minecraft:ui.toast.challenge_complete master @s ~ ~ ~ 1 1
title @s times 20 80 30
title @s subtitle {"text":"You are Awakened.","color":"gray"}
title @s title {"text":"The Nightmare Ends","color":"light_purple","bold":true}
```

- [ ] **Step 2: Write the roll function**

`/random value` was added in 1.20.2 and is the correct source of randomness here.

`shadowslave/data/shadowslave/function/awaken/roll.mcfunction`:

```mcfunction
# One-time transition: Sleeper -> Awakened, with an Aspect and a Flaw.

scoreboard players set @s ss_rank 1

# Aspect and Flaw roll independently, so Shadow + Shadow Slave can occur.
execute store result score @s ss_aspect run random value 1..4
execute store result score @s ss_flaw run random value 1..4

execute if score @s ss_aspect matches 1 run tag @s add ss_aspect_shadow
execute if score @s ss_aspect matches 2 run tag @s add ss_aspect_flame
execute if score @s ss_aspect matches 3 run tag @s add ss_aspect_bone
execute if score @s ss_aspect matches 4 run tag @s add ss_aspect_wind

execute if score @s ss_flaw matches 1 run tag @s add ss_flaw_shadow_slave
execute if score @s ss_flaw matches 2 run tag @s add ss_flaw_fragile
execute if score @s ss_flaw matches 3 run tag @s add ss_flaw_ravenous
execute if score @s ss_flaw matches 4 run tag @s add ss_flaw_weightless

tellraw @s [{"text":"\n","color":"white"},{"text":"You have Awakened.","color":"light_purple","bold":true},{"text":"\nRun ","color":"gray"},{"text":"/trigger soul","color":"aqua"},{"text":" to read your soul.\n","color":"gray"}]
```

- [ ] **Step 3: Run the validator**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: `OK: datapack structure valid`.

- [ ] **Step 4: In-game check**

Enter a nightmare, skip the timer, and kill the creature:

```
/scoreboard players set @s ss_timer 1
/kill @e[tag=ss_creature]
```

Expected: you are returned to your bed position, the "The Nightmare Ends" title appears, and:

```
/scoreboard players get @s ss_rank
/scoreboard players get @s ss_aspect
/scoreboard players get @s ss_flaw
/tag @s list
```

shows rank 1, aspect and flaw each between 1 and 4, and exactly one `ss_aspect_*` and one `ss_flaw_*` tag.

- [ ] **Step 5: Commit**

```bash
cd /project/src/shadowslave
git add shadowslave/data/shadowslave/function/nightmare shadowslave/data/shadowslave/function/awaken
git commit -m "feat: Awakening on survival, rolling one Aspect and one Flaw"
```

---

### Task 7: Aspects

**Files:**

- Create: `shadowslave/data/shadowslave/function/aspect/shadow.mcfunction`
- Create: `shadowslave/data/shadowslave/function/aspect/flame.mcfunction`
- Create: `shadowslave/data/shadowslave/function/aspect/bone.mcfunction`
- Create: `shadowslave/data/shadowslave/function/aspect/wind.mcfunction`
- Create: `shadowslave/data/shadowslave/function/upkeep.mcfunction`
- Create: `shadowslave/data/shadowslave/predicate/in_darkness.json`
- Modify: `shadowslave/data/shadowslave/function/tick.mcfunction`

**Interfaces:**

- Consumes: tags `ss_aspect_*` from Task 6.
- Produces: recurring effects and attribute modifiers under ids `shadowslave:aspect_bone_armor` and `shadowslave:aspect_wind_speed`.

- [ ] **Step 1: Write the four Aspect functions**

Effects are re-applied with a short duration each pass so they lapse naturally if the Aspect is ever removed. Applied every 20 ticks, not every tick.

`shadowslave/data/shadowslave/function/aspect/shadow.mcfunction`:

```mcfunction
# Shadow — sees in the dark, and moves faster within it.
effect give @s minecraft:night_vision 15 0 true
execute if predicate shadowslave:in_darkness run effect give @s minecraft:speed 2 1 true
```

`shadowslave/data/shadowslave/function/aspect/flame.mcfunction`:

```mcfunction
# Flame — immune to fire, and strikes burn.
effect give @s minecraft:fire_resistance 15 0 true
```

`shadowslave/data/shadowslave/function/aspect/bone.mcfunction`:

```mcfunction
# Bone — the body hardens.
attribute @s minecraft:generic.armor modifier remove shadowslave:aspect_bone_armor
attribute @s minecraft:generic.armor modifier add shadowslave:aspect_bone_armor 6 add_value
```

`shadowslave/data/shadowslave/function/aspect/wind.mcfunction`:

```mcfunction
# Wind — light on the feet.
attribute @s minecraft:generic.movement_speed modifier remove shadowslave:aspect_wind_speed
attribute @s minecraft:generic.movement_speed modifier add shadowslave:aspect_wind_speed 0.15 add_multiplied_base
effect give @s minecraft:jump_boost 15 0 true
```

The `remove` before every `add` makes these idempotent — without it, repeated application stacks modifiers until the player is unplayably fast.

- [ ] **Step 2: Write the darkness predicate**

Create `shadowslave/data/shadowslave/predicate/in_darkness.json`:

```json
{
  "condition": "minecraft:location_check",
  "predicate": {
    "light": {
      "light": {
        "max": 7
      }
    }
  }
}
```

- [ ] **Step 3: Add Aspect upkeep to the tick dispatch**

Append to `shadowslave/data/shadowslave/function/tick.mcfunction`:

```mcfunction
# Aspect and Flaw upkeep — once a second, not every tick.
scoreboard players add $ss_clock ss_clock 1
execute if score $ss_clock ss_clock matches 20.. run scoreboard players set $ss_clock ss_clock 0
execute if score $ss_clock ss_clock matches 0 as @a[scores={ss_rank=1..}] at @s run function shadowslave:upkeep
```

`$ss_clock` is a fake player — a scoreboard entry not attached to any entity — which is the standard way to hold a global counter. Order matters: increment, wrap, then dispatch. Dispatching before the wrap fires upkeep every tick instead of once a second.

- [ ] **Step 4: Write the upkeep dispatcher**

Create `shadowslave/data/shadowslave/function/upkeep.mcfunction`:

```mcfunction
# Runs as and at each Awakened player, once per second.

execute if entity @s[tag=ss_aspect_shadow] run function shadowslave:aspect/shadow
execute if entity @s[tag=ss_aspect_flame] run function shadowslave:aspect/flame
execute if entity @s[tag=ss_aspect_bone] run function shadowslave:aspect/bone
execute if entity @s[tag=ss_aspect_wind] run function shadowslave:aspect/wind
```

- [ ] **Step 5: Run the validator**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: `OK: datapack structure valid`.

- [ ] **Step 6: In-game check**

For each Aspect in turn:

```
/reload
/tag @s remove ss_aspect_shadow
/tag @s remove ss_aspect_flame
/tag @s remove ss_aspect_bone
/tag @s remove ss_aspect_wind
/scoreboard players set @s ss_rank 1
/tag @s add ss_aspect_bone
```

Expected within one second: `/attribute @s minecraft:generic.armor get` reports 6 higher than base. Wait 30 seconds and check again — it must still be exactly 6 higher, not stacking. Repeat for `ss_aspect_shadow` (night vision icon), `ss_aspect_flame` (stand in fire, take no damage), and `ss_aspect_wind` (visibly faster, higher jumps).

- [ ] **Step 7: Commit**

```bash
cd /project/src/shadowslave
git add shadowslave/data/shadowslave/function shadowslave/data/shadowslave/predicate
git commit -m "feat: four Aspects with idempotent effect and attribute upkeep"
```

---

### Task 8: Flaws

**Files:**

- Create: `shadowslave/data/shadowslave/function/flaw/shadow_slave.mcfunction`
- Create: `shadowslave/data/shadowslave/function/flaw/fragile.mcfunction`
- Create: `shadowslave/data/shadowslave/function/flaw/ravenous.mcfunction`
- Create: `shadowslave/data/shadowslave/function/flaw/weightless.mcfunction`
- Create: `shadowslave/data/shadowslave/predicate/in_sunlight.json`
- Modify: `shadowslave/data/shadowslave/function/upkeep.mcfunction`

**Interfaces:**

- Consumes: tags `ss_flaw_*` from Task 6.
- Produces: attribute modifiers `shadowslave:flaw_fragile_health` and `shadowslave:flaw_weightless_fall`.

- [ ] **Step 1: Write the sunlight predicate**

`shadowslave/data/shadowslave/predicate/in_sunlight.json`:

```json
{
  "condition": "minecraft:all_of",
  "terms": [
    {
      "condition": "minecraft:location_check",
      "predicate": {
        "light": {
          "light": {
            "min": 14
          }
        },
        "can_see_sky": true
      }
    },
    {
      "condition": "minecraft:time_check",
      "value": {
        "min": 0,
        "max": 12000
      },
      "period": 24000
    }
  ]
}
```

- [ ] **Step 2: Write the four Flaw functions**

`shadowslave/data/shadowslave/function/flaw/shadow_slave.mcfunction`:

```mcfunction
# Shadow Slave — the sun is an enemy.
execute if predicate shadowslave:in_sunlight run damage @s 1 minecraft:on_fire
execute if predicate shadowslave:in_sunlight run effect give @s minecraft:weakness 2 0 true
```

`shadowslave/data/shadowslave/function/flaw/fragile.mcfunction`:

```mcfunction
# Fragile — the body never fully recovered.
attribute @s minecraft:generic.max_health modifier remove shadowslave:flaw_fragile_health
attribute @s minecraft:generic.max_health modifier add shadowslave:flaw_fragile_health -6 add_value
```

`shadowslave/data/shadowslave/function/flaw/ravenous.mcfunction`:

```mcfunction
# Ravenous — the soul burns through the body.
effect give @s minecraft:hunger 2 0 true
```

`shadowslave/data/shadowslave/function/flaw/weightless.mcfunction`:

```mcfunction
# Weightless — the ground is unkind.
attribute @s minecraft:generic.safe_fall_distance modifier remove shadowslave:flaw_weightless_fall
attribute @s minecraft:generic.safe_fall_distance modifier add shadowslave:flaw_weightless_fall -2 add_value
```

- [ ] **Step 3: Add Flaws to the upkeep dispatcher**

Append to `shadowslave/data/shadowslave/function/upkeep.mcfunction`:

```mcfunction
execute if entity @s[tag=ss_flaw_shadow_slave] run function shadowslave:flaw/shadow_slave
execute if entity @s[tag=ss_flaw_fragile] run function shadowslave:flaw/fragile
execute if entity @s[tag=ss_flaw_ravenous] run function shadowslave:flaw/ravenous
execute if entity @s[tag=ss_flaw_weightless] run function shadowslave:flaw/weightless
```

- [ ] **Step 4: Run the validator**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: `OK: datapack structure valid`.

- [ ] **Step 5: In-game check**

```
/reload
/scoreboard players set @s ss_rank 1
/tag @s add ss_flaw_fragile
```

Expected: max health drops to 14 (7 hearts) within a second, and stays at 14 after a minute rather than sinking further. Then:

```
/tag @s remove ss_flaw_fragile
/tag @s add ss_flaw_shadow_slave
/time set day
```

Stand outside in the open. Expected: you take steady damage; step into shade or a cave and it stops.

- [ ] **Step 6: Commit**

```bash
cd /project/src/shadowslave
git add shadowslave/data/shadowslave/function shadowslave/data/shadowslave/predicate
git commit -m "feat: four Flaws with sunlight predicate and idempotent modifiers"
```

---

### Task 9: The Soul readout and the self-check

**Files:**

- Create: `shadowslave/data/shadowslave/function/soul.mcfunction`
- Create: `shadowslave/data/shadowslave/function/test/selfcheck.mcfunction`

**Interfaces:**

- Consumes: everything above.
- Produces: `/trigger soul` as the player-facing status readout; `shadowslave:test/selfcheck` as the in-game assertion function.

- [ ] **Step 1: Write the Soul readout**

`shadowslave/data/shadowslave/function/soul.mcfunction`:

```mcfunction
# /trigger soul — the Spell's status page, such as it is without a GUI.

scoreboard players set @s soul 0

tellraw @s [{"text":"\n"},{"text":"— Soul —","color":"light_purple","bold":true}]

# `matches 0` would fail for a player with NO score entry, and nothing ever writes 0 —
# a fresh player simply has no row. `unless ... matches 1..` covers both absent and zero.
execute unless score @s ss_rank matches 1.. run tellraw @s [{"text":"Rank: ","color":"gray"},{"text":"Sleeper","color":"dark_gray"}]
execute if score @s ss_rank matches 1 run tellraw @s [{"text":"Rank: ","color":"gray"},{"text":"Awakened","color":"aqua"}]

execute if entity @s[tag=ss_aspect_shadow] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Shadow","color":"dark_purple"},{"text":" — sight in darkness, speed within it","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_flame] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Flame","color":"gold"},{"text":" — fire cannot touch you","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_bone] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Bone","color":"white"},{"text":" — the body hardens","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_aspect_wind] run tellraw @s [{"text":"Aspect: ","color":"gray"},{"text":"Wind","color":"green"},{"text":" — light on the feet","color":"dark_gray","italic":true}]

execute if entity @s[tag=ss_flaw_shadow_slave] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Shadow Slave","color":"red"},{"text":" — the sun burns you","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_fragile] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Fragile","color":"red"},{"text":" — three hearts were never returned","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_ravenous] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Ravenous","color":"red"},{"text":" — the soul burns through the body","color":"dark_gray","italic":true}]
execute if entity @s[tag=ss_flaw_weightless] run tellraw @s [{"text":"Flaw: ","color":"gray"},{"text":"Weightless","color":"red"},{"text":" — the ground is unkind","color":"dark_gray","italic":true}]

execute unless score @s ss_rank matches 1.. run tellraw @s {"text":"The Spell has not yet tested you. Sleep.","color":"dark_gray","italic":true}
tellraw @s {"text":""}
```

- [ ] **Step 2: Write the self-check**

`shadowslave/data/shadowslave/function/test/selfcheck.mcfunction`:

```mcfunction
# Run with /function shadowslave:test/selfcheck — asserts the pack loaded correctly.

tellraw @s [{"text":"— Shadow Slave self-check —","color":"light_purple","bold":true}]

# Objectives exist: setting a value on a missing objective fails the command.
execute store success score $check ss_roll run scoreboard players set $probe ss_rank 0
execute if score $check ss_roll matches 1 run tellraw @s {"text":"PASS objectives registered","color":"green"}
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL objectives missing — did init run?","color":"red"}

# Dimension is registered.
execute store success score $check ss_roll run execute in shadowslave:nightmare run time query daytime
execute if score $check ss_roll matches 1 run tellraw @s {"text":"PASS dimension shadowslave:nightmare registered","color":"green"}
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL dimension missing","color":"red"}

# Bossbar exists.
execute store success score $check ss_roll run bossbar get shadowslave:trial max
execute if score $check ss_roll matches 1 run tellraw @s {"text":"PASS trial bossbar registered","color":"green"}
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL trial bossbar missing","color":"red"}

# Every Aspect and Flaw function resolves.
execute store success score $check ss_roll run function shadowslave:aspect/shadow
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL aspect/shadow missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:aspect/flame
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL aspect/flame missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:aspect/bone
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL aspect/bone missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:aspect/wind
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL aspect/wind missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:flaw/shadow_slave
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL flaw/shadow_slave missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:flaw/fragile
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL flaw/fragile missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:flaw/ravenous
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL flaw/ravenous missing","color":"red"}
execute store success score $check ss_roll run function shadowslave:flaw/weightless
execute if score $check ss_roll matches 0 run tellraw @s {"text":"FAIL flaw/weightless missing","color":"red"}

# Clean up the effects the probe calls just applied.
attribute @s minecraft:generic.armor modifier remove shadowslave:aspect_bone_armor
attribute @s minecraft:generic.movement_speed modifier remove shadowslave:aspect_wind_speed
attribute @s minecraft:generic.max_health modifier remove shadowslave:flaw_fragile_health
attribute @s minecraft:generic.safe_fall_distance modifier remove shadowslave:flaw_weightless_fall
effect clear @s

tellraw @s {"text":"— self-check complete —","color":"light_purple"}
```

- [ ] **Step 3: Run the validator**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: `OK: datapack structure valid`.

- [ ] **Step 4: In-game check**

```
/reload
/function shadowslave:test/selfcheck
/trigger soul
```

Expected: every self-check line reads PASS, and the Soul readout prints your rank, and your Aspect and Flaw if Awakened.

- [ ] **Step 5: Full loop verification**

In a fresh world with the datapack installed:

1. `/function shadowslave:test/selfcheck` — all PASS
2. `/trigger soul` — shows Sleeper
3. Build a bed, sleep at night — pulled into the nightmare with the purple bar
4. Survive the countdown — the Nightmare Creature spawns, bar turns red
5. Kill it — returned to the bed, "The Nightmare Ends"
6. `/trigger soul` — shows Awakened with one Aspect and one Flaw
7. Sleep again — you sleep normally, not pulled in
8. In a second run, take heavy damage instead — ejected at half a heart with gear intact, still a Sleeper

- [ ] **Step 6: Commit and tag the release**

```bash
cd /project/src/shadowslave
git add shadowslave/data/shadowslave/function
git commit -m "feat: Soul readout and in-game self-check"
git tag -a v1.0.0 -m "Phase 1: The First Nightmare"
```

Version `1.0.0` under Pride Versioning — the first release worth being proud of.

---

### Task 10: The in-game verification tree

**Files:**

- Create: `shadowslave/data/shadowslave/advancement/test/root.json`
- Create: `shadowslave/data/shadowslave/advancement/test/chosen.json`
- Create: `shadowslave/data/shadowslave/advancement/test/endured.json`
- Create: `shadowslave/data/shadowslave/advancement/test/slayer.json`
- Create: `shadowslave/data/shadowslave/advancement/test/awakened.json`
- Create: `shadowslave/data/shadowslave/advancement/test/cast_out.json`
- Create: `shadowslave/data/shadowslave/advancement/test/aspect_live.json`
- Create: `shadowslave/data/shadowslave/advancement/test/flaw_live.json`
- Create: `shadowslave/data/shadowslave/advancement/test/bypass.json`
- Create: `shadowslave/data/shadowslave/function/test/reset.mcfunction`
- Modify: `shadowslave/data/shadowslave/function/nightmare/enter.mcfunction`
- Modify: `shadowslave/data/shadowslave/function/nightmare/spawn_creature.mcfunction`
- Modify: `shadowslave/data/shadowslave/function/nightmare/survive.mcfunction`
- Modify: `shadowslave/data/shadowslave/function/nightmare/eject.mcfunction`
- Modify: `shadowslave/data/shadowslave/function/awaken/roll.mcfunction`
- Modify: `shadowslave/data/shadowslave/function/upkeep.mcfunction`

**Interfaces:**

- Consumes: every function from Tasks 3–8.
- Produces: the advancement tab "Shadow Slave — Verification", and `shadowslave:test/reset` to clear it for a fresh run.

**Why this exists:** the manual in-game checklists in Tasks 1–9 record what the tester _believes_ happened. These advancements record that a specific line of code actually ran. When a mechanic silently fails, the missing advancement says exactly which one, and how far the loop got before it stopped.

- [ ] **Step 1: Write the root advancement**

`minecraft:impossible` can never be satisfied by play, so these are grantable only by command — the standard pattern for command-driven advancements.

`shadowslave/data/shadowslave/advancement/test/root.json`:

```json
{
  "display": {
    "icon": {
      "id": "minecraft:black_bed"
    },
    "title": {
      "text": "Shadow Slave — Verification",
      "color": "light_purple"
    },
    "description": {
      "text": "Each of these is granted the moment a mechanic actually fires."
    },
    "background": "minecraft:textures/block/deepslate.png",
    "frame": "task",
    "show_toast": false,
    "announce_to_chat": false
  },
  "criteria": {
    "granted": {
      "trigger": "minecraft:impossible"
    }
  }
}
```

- [ ] **Step 2: Write the eight child advancements**

All eight share a shape: `parent`, a `display` block, and the impossible criterion. Only icon, title and description differ.

`chosen.json`:

```json
{
  "parent": "shadowslave:test/root",
  "display": {
    "icon": { "id": "minecraft:red_bed" },
    "title": { "text": "Chosen", "color": "dark_purple" },
    "description": {
      "text": "Sleeping pulled a Sleeper into the nightmare dimension."
    },
    "frame": "task",
    "show_toast": true,
    "announce_to_chat": false
  },
  "criteria": { "granted": { "trigger": "minecraft:impossible" } }
}
```

`endured.json`:

```json
{
  "parent": "shadowslave:test/chosen",
  "display": {
    "icon": { "id": "minecraft:clock" },
    "title": { "text": "Endured", "color": "dark_purple" },
    "description": {
      "text": "The countdown expired and the Nightmare Creature spawned."
    },
    "frame": "task",
    "show_toast": true,
    "announce_to_chat": false
  },
  "criteria": { "granted": { "trigger": "minecraft:impossible" } }
}
```

`slayer.json`:

```json
{
  "parent": "shadowslave:test/endured",
  "display": {
    "icon": { "id": "minecraft:iron_sword" },
    "title": { "text": "Slayer", "color": "dark_purple" },
    "description": {
      "text": "The creature died and the trial resolved as a win."
    },
    "frame": "goal",
    "show_toast": true,
    "announce_to_chat": false
  },
  "criteria": { "granted": { "trigger": "minecraft:impossible" } }
}
```

`awakened.json`:

```json
{
  "parent": "shadowslave:test/slayer",
  "display": {
    "icon": { "id": "minecraft:nether_star" },
    "title": { "text": "Awakened", "color": "aqua" },
    "description": {
      "text": "Rank set to Awakened with one Aspect and one Flaw rolled."
    },
    "frame": "challenge",
    "show_toast": true,
    "announce_to_chat": false
  },
  "criteria": { "granted": { "trigger": "minecraft:impossible" } }
}
```

`cast_out.json`:

```json
{
  "parent": "shadowslave:test/chosen",
  "display": {
    "icon": { "id": "minecraft:wither_rose" },
    "title": { "text": "Cast Out", "color": "dark_red" },
    "description": {
      "text": "Near-death ejection returned you with gear intact and no Awakening."
    },
    "frame": "task",
    "show_toast": true,
    "announce_to_chat": false
  },
  "criteria": { "granted": { "trigger": "minecraft:impossible" } }
}
```

`aspect_live.json`:

```json
{
  "parent": "shadowslave:test/awakened",
  "display": {
    "icon": { "id": "minecraft:ender_eye" },
    "title": { "text": "Aspect Holds", "color": "aqua" },
    "description": { "text": "Aspect upkeep ran on a live Awakened player." },
    "frame": "task",
    "show_toast": true,
    "announce_to_chat": false
  },
  "criteria": { "granted": { "trigger": "minecraft:impossible" } }
}
```

`flaw_live.json`:

```json
{
  "parent": "shadowslave:test/awakened",
  "display": {
    "icon": { "id": "minecraft:cracked_stone_bricks" },
    "title": { "text": "Flaw Bites", "color": "red" },
    "description": { "text": "Flaw upkeep ran on a live Awakened player." },
    "frame": "task",
    "show_toast": true,
    "announce_to_chat": false
  },
  "criteria": { "granted": { "trigger": "minecraft:impossible" } }
}
```

`bypass.json`:

```json
{
  "parent": "shadowslave:test/awakened",
  "display": {
    "icon": { "id": "minecraft:white_bed" },
    "title": { "text": "Sleep Undisturbed", "color": "gray" },
    "description": {
      "text": "An Awakened player slept without being pulled in — the rank gate works."
    },
    "frame": "task",
    "show_toast": true,
    "announce_to_chat": false
  },
  "criteria": { "granted": { "trigger": "minecraft:impossible" } }
}
```

- [ ] **Step 3: Grant them at the exact points the behaviour occurs**

In `nightmare/enter.mcfunction`, change the rank gate line so the bypass is recorded before returning:

```mcfunction
# Only Sleepers are Chosen. Awakened players sleep normally.
execute if score @s ss_rank matches 1.. run advancement grant @s only shadowslave:test/bypass
execute if score @s ss_rank matches 1.. run return 0
```

Then append to the end of the same file:

```mcfunction
advancement grant @s only shadowslave:test/chosen
```

Append to `nightmare/spawn_creature.mcfunction`:

```mcfunction
advancement grant @s only shadowslave:test/endured
```

Append to `nightmare/survive.mcfunction`:

```mcfunction
advancement grant @s only shadowslave:test/slayer
```

Append to `awaken/roll.mcfunction`:

```mcfunction
advancement grant @s only shadowslave:test/awakened
```

Append to `nightmare/eject.mcfunction`:

```mcfunction
advancement grant @s only shadowslave:test/cast_out
```

Append to `upkeep.mcfunction`:

```mcfunction
execute if entity @s[tag=ss_aspect_shadow] run advancement grant @s only shadowslave:test/aspect_live
execute if entity @s[tag=ss_aspect_flame] run advancement grant @s only shadowslave:test/aspect_live
execute if entity @s[tag=ss_aspect_bone] run advancement grant @s only shadowslave:test/aspect_live
execute if entity @s[tag=ss_aspect_wind] run advancement grant @s only shadowslave:test/aspect_live
execute if entity @s[tag=ss_flaw_shadow_slave] run advancement grant @s only shadowslave:test/flaw_live
execute if entity @s[tag=ss_flaw_fragile] run advancement grant @s only shadowslave:test/flaw_live
execute if entity @s[tag=ss_flaw_ravenous] run advancement grant @s only shadowslave:test/flaw_live
execute if entity @s[tag=ss_flaw_weightless] run advancement grant @s only shadowslave:test/flaw_live
```

Granting an advancement a player already holds is a no-op, so running these every second is harmless.

- [ ] **Step 4: Write the reset function**

`shadowslave/data/shadowslave/function/test/reset.mcfunction`:

```mcfunction
# Wipe verification state for a clean test run. Does not touch Aspect or Flaw tags.

advancement revoke @s from shadowslave:test/root
advancement revoke @s only shadowslave:enter_nightmare

scoreboard players set @s ss_rank 0
scoreboard players set @s ss_timer 0
scoreboard players set @s ss_aspect 0
scoreboard players set @s ss_flaw 0
scoreboard players reset @s ss_gone

tag @s remove ss_in_nightmare
tag @s remove ss_creature_spawned
tag @s remove ss_aspect_shadow
tag @s remove ss_aspect_flame
tag @s remove ss_aspect_bone
tag @s remove ss_aspect_wind
tag @s remove ss_flaw_shadow_slave
tag @s remove ss_flaw_fragile
tag @s remove ss_flaw_ravenous
tag @s remove ss_flaw_weightless

attribute @s minecraft:generic.armor modifier remove shadowslave:aspect_bone_armor
attribute @s minecraft:generic.movement_speed modifier remove shadowslave:aspect_wind_speed
attribute @s minecraft:generic.max_health modifier remove shadowslave:flaw_fragile_health
attribute @s minecraft:generic.safe_fall_distance modifier remove shadowslave:flaw_weightless_fall

execute in shadowslave:nightmare run kill @e[tag=ss_creature]
bossbar set shadowslave:trial visible false
bossbar set shadowslave:trial players

tellraw @s {"text":"[Shadow Slave] Verification state reset. You are a Sleeper again.","color":"gray","italic":true}
```

- [ ] **Step 5: Extend the validator to check advancement wiring**

Add to `shadowslave/tools/validate.py`, and call it from `main()`:

```python
def check_advancement_parents():
    """Every `parent` must point at an advancement file that exists."""
    root = DATA / "shadowslave" / "advancement"
    for f in root.rglob("*.json"):
        try:
            data = json.loads(f.read_text())
        except json.JSONDecodeError:
            continue  # already reported
        parent = data.get("parent")
        if parent is None:
            continue
        namespace, _, name = parent.partition(":")
        target = DATA / namespace / "advancement" / f"{name}.json"
        if not target.is_file():
            errors.append(f"{f.relative_to(PACK)}: parent {parent} does not exist")


def check_granted_advancements_exist():
    """Every `advancement grant ... only <id>` must name a real advancement."""
    import re
    pattern = re.compile(r"advancement (?:grant|revoke) \S+ (?:only|from) (\S+)")
    for f in DATA.rglob("*.mcfunction"):
        for line in f.read_text().splitlines():
            for ref in pattern.findall(line):
                namespace, _, name = ref.partition(":")
                target = DATA / namespace / "advancement" / f"{name}.json"
                if not target.is_file():
                    errors.append(f"{f.relative_to(PACK)}: grants missing advancement {ref}")
```

Update `main()` to call both:

```python
def main():
    check_pack_mcmeta()
    check_no_plural_dirs()
    check_json_parses()
    check_tagged_functions_exist()
    check_advancement_parents()
    check_granted_advancements_exist()
    if errors:
        for e in errors:
            print(f"FAIL: {e}")
        return 1
    print("OK: datapack structure valid")
    return 0
```

- [ ] **Step 6: Run the validator to verify it passes**

Run: `python3 /project/src/shadowslave/shadowslave/tools/validate.py`
Expected: `OK: datapack structure valid`.

Then prove the new check works — temporarily append a bad grant to `soul.mcfunction`:

```mcfunction
advancement grant @s only shadowslave:test/does_not_exist
```

Run the validator again.
Expected: `FAIL: ... soul.mcfunction: grants missing advancement shadowslave:test/does_not_exist`.
Remove that line and confirm it returns to OK.

- [ ] **Step 7: In-game check — the full loop, read off the tree**

```
/reload
/function shadowslave:test/reset
```

Open the advancements screen and find the "Shadow Slave — Verification" tab. Everything should be locked. Then play the loop: sleep, endure, kill the creature. Each toast fires as its mechanic executes.

A complete Phase 1 leaves all of these earned except one — **Cast Out** and **Slayer** are mutually exclusive in a single run, since you either survive or you don't. Reset and take a deliberate beating to collect **Cast Out** on a second run.

Expected after two runs: all nine granted.

- [ ] **Step 8: Commit**

```bash
cd /project/src/shadowslave
git add shadowslave/data/shadowslave/advancement shadowslave/data/shadowslave/function shadowslave/tools/validate.py
git commit -m "feat: in-game verification advancement tree and reset function"
```

---

## Known limitations

Deliberate, and recorded so they are not mistaken for bugs:

- **One global bossbar.** Two players in nightmares simultaneously will fight over it. Per-player bars need macro-generated bossbar ids; deferred to Phase 6.
- **Ejection triggers at 2 hearts, not at death.** Intercepting actual death without losing the player's gear is not reliably possible in a datapack, and gear retention matters more than the exact threshold.
- **The Nightmare Creature is a reskinned ravager.** A bespoke creature needs custom models and AI, which means Java. Phase 2+.
- **Terrain is Overworld noise.** Only the lighting, sky and spawns are nightmarish. Bespoke worldgen is deferred.
- **Health is polled via NBT each tick** for players inside a nightmare. Expensive per best practice, but bounded to players actually in the trial.
- **The verification tab ships with the pack.** Anyone installing `1.0.0` sees a "Shadow Slave — Verification" advancement tab. Removing `advancement/test/` and the `advancement grant` lines strips it, but that is a release chore every version. The cheaper long-term move is converting the tree into real player-facing advancements in Phase 2 — the trigger points are already in exactly the right places.
- **Phase 1 is single-player at a time.** One player leaving the nightmare kills every creature in it, so a second player mid-trial gets a free Awakening. Same root cause as the shared bossbar; per-player creature ownership is deferred with it.

### Player NBT cannot be written directly

Minecraft refuses all player NBT writes (`data merge/modify entity <player>`, `execute store … entity <player>`) — reads are fine. Any dynamic player teleport must go through command storage and a macro function (see `nightmare/return.mcfunction`). This caused three separate defects in Phase 1.

## Verify in-game before trusting

1. **Teleporting a sleeping player.** If the player stays in the bed, wrap the teleport in `schedule function shadowslave:nightmare/enter_delayed 1t`.
2. **Advancement `icon` format.** The plan uses the 1.20.5+ item-stack form `{"id": "minecraft:red_bed"}`. If icons fail to load, the older form is `{"item": "minecraft:red_bed"}`. A wrong icon breaks the whole advancement file, so check the log on first `/reload`.
3. **`summon` NBT for attributes.** The `attributes`/`id`/`base` casing is 1.20.5+; confirm the ravager spawns at 160 health rather than default.
4. **`minecraft:generic.safe_fall_distance`** exists as an attribute in 1.21.1 — confirm before relying on the Weightless flaw.
5. **`damage @s 1 minecraft:on_fire`** — confirm the damage type id resolves; the fallback is `minecraft:magic`.
6. **The ravager's roar damage figure.** Assumed ~6 when setting the ejection threshold to `..8` — confirm the actual figure in-game and adjust the threshold if it's higher.
7. **`minecraft:generic.max_health` and the other `generic.`-prefixed attribute ids** are correct for **1.21.1 specifically** — they were renamed (the `generic.` prefix dropped) in 1.21.2. Confirm before bumping the target version.
