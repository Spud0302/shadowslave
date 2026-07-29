# Shadow Slave

A Minecraft datapack based on the webnovel _Shadow Slave_ by Guiltythree.

Sleep, and something notices you. Sleep again and it takes you — into a dark world with a
countdown and something hunting you. Survive, kill what comes, and wake **Awakened** with an
Aspect that grants you power and a Flaw that charges you for it.

**Minecraft 1.21.1 · vanilla datapack · server-side only**

---

## Install

Drop the `.zip` into your world's `datapacks/` folder and `/reload`.

```
<world>/datapacks/shadowslave-<version>.zip
```

`pack.mcmeta` sits at the root of the archive, which is what Minecraft expects — if a pack
never appears in `/datapack list`, a wrapper folder is usually why.

**No client install.** Datapacks are server-side, so anyone can join a server running this
with a completely vanilla client and everything works: the dimension, the trial, Aspects,
advancements. That is a deliberate reason to stay a datapack for as long as possible.

**Requirements:** Minecraft **1.21.1** exactly (`pack_format` 48), difficulty **Easy or
higher** (Peaceful strips hostile mobs and the trial is empty), and **cheats on** if you want
the testing commands.

---

## Playing

You start **Mundane** — untouched. Nothing happens to you.

1. **Sleep once.** The Spell notices you. You wake normally, but you are a **Carrier** now,
   and every 30 seconds you feel it pulling.
2. **Sleep again, or crouch on a bed at any hour.** It takes you. Vanilla only lets you sleep
   at night; the Spell does not care what time it is.
3. **Survive the countdown.** The nightmare is dark and full of things.
4. **Kill the Nightmare Creature** when it comes. Drop too low and you are cast out instead,
   with your gear, to try again once you have recovered.
5. **Wake Awakened**, with an Aspect and a Flaw.

`/trigger soul` shows where you stand.

|             |                                             |
| ----------- | ------------------------------------------- |
| **Aspects** | Shadow, Flame, Bone, Wind                   |
| **Flaws**   | Shadow Slave, Fragile, Ravenous, Weightless |

Rolled independently, so Shadow + Shadow Slave is possible — and thematically apt.

> These eight are **placeholders**. The novel is explicit that every Aspect is unique and that
> Flaws are never random, so the plan is to generate Aspects and derive Flaws from how you
> actually survived the trial. See the design spec.

---

## Testing commands

```
/function shadowslave:test/help
```

| Command          | Does                                |
| ---------------- | ----------------------------------- |
| `test/selfcheck` | Asserts the pack loaded correctly   |
| `test/infect`    | Become a Carrier now                |
| `test/cure`      | Back to untouched                   |
| `test/nightmare` | Enter the trial immediately, no bed |
| `test/awaken`    | Skip the trial, roll an Aspect      |
| `test/reset`     | Wipe everything and start over      |

Skip the countdown mid-trial with `/scoreboard players set @s ss_timer 1`.

**Testing commands bypass the systems they test.** `test/nightmare` walks past the ejection
cooldown and the weakness gate via a single-use `ss_test_bypass` tag that entry consumes —
otherwise the command for entering the trial refuses to enter the trial. The rank gate has no
bypass on purpose: an Awakened in a First Nightmare is a state nothing handles. Use
`test/reset`.

There is also a **Shadow Slave — Verification** advancement tab. Each entry is granted at the
exact line a mechanic executes, so it records what actually ran rather than what you think
ran. An incomplete branch tells you where the loop stopped.

---

## Development

### Structure

```
shadowslave/            the datapack itself
  data/shadowslave/
    function/           all logic, grouped by responsibility
      nightmare/          the trial lifecycle
      aspect/  flaw/      recurring per-second effects
      awaken/             the one-time rank transition
      test/               testing commands
    dimension_type/  dimension/  worldgen/biome/
    advancement/  predicate/
  tools/validate.py     offline structure and reference checker
testserver/             local 1.21.1 server + mineflayer harness
docs/superpowers/       design spec and implementation plan
```

### Checks before shipping

```bash
python3 shadowslave/tools/validate.py      # static: structure, references, schema
cd testserver && node harness.mjs          # live: 22 assertions against a real server
```

**`validate.py`** catches what fails _silently_ in Minecraft — plural directory names, a
misspelled objective, a tag tested but never applied, an unresolvable function or predicate,
an attribute modifier added without a paired remove, a `dimension_type` that does not cover
its noise settings, and version drift between the manifest and the load message.

**`harness.mjs`** runs a mineflayer bot against a local server, executing commands and reading
the replies back to assert on game state. It covers the mechanical half — state transitions,
guards, thresholds, teardown — and prints a **"needs a human"** list for everything requiring
judgement.

Both exist because this project has repeatedly shipped code that was valid, well-formed, and
wrong. Almost every way a datapack breaks is silent.

### Conventions

- **Namespace everything** `shadowslave`; objectives and tags carry an `ss_` prefix.
- **Singular directory names** — plurals silently do nothing in 1.21.
- **Comments explain why, not what.** A command says what it does; a comment should say what
  it protects against, or what was tried first and failed.
- **`ponytail:` comments** mark deliberate shortcuts, naming the ceiling and the upgrade path.
- **`remove` before every `add`** on attribute modifiers. The upkeep runs once a second
  forever; without it modifiers stack until the player is unkillable.
- **Never guard on a score with `matches 0`.** An absent score fails `matches` outright, and
  nothing writes 0 — use `unless ... matches 1..`. This has caused two separate bugs.
- **Player NBT is read-only.** `data merge|modify entity <player>` and
  `execute store ... entity <player>` are refused. Dynamic teleports go through command
  storage and a macro.

### Versioning

[Pride Versioning](https://pridever.org/) — `PROUD.DEFAULT.SHAME`. Tag first, then bump SHAME
for each round of fixes; holding a release back until it is clean makes the number lie.

---

## Documents

|                              |                                                                     |
| ---------------------------- | ------------------------------------------------------------------- |
| [CHANGELOG.md](CHANGELOG.md) | Every version, and which issue it fixed                             |
| [ISSUES.md](ISSUES.md)       | Known issues, deliberate limitations, and what is confirmed working |
| [TESTING.md](TESTING.md)     | Full in-game test plans                                             |
| `docs/superpowers/specs/`    | Design spec, including the direction for generated Aspects          |

## Credit

_Shadow Slave_ is by **Guiltythree**. This is an unofficial fan project, not affiliated with
the author or publisher. Lore is drawn from the community wiki; the vault note is the
project's source of truth for it.
