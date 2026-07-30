# Shadow Slave

A Minecraft datapack based on the webnovel _Shadow Slave_ by Guiltythree.

Sleep, and something notices you. Sleep again and it takes you — into a dark world with a
countdown and something hunting you. Survive, kill what comes, and wake a **Sleeper** — holding a
Dormant Aspect that grants you power and a Flaw that charges you for it.

**Minecraft 1.21.1 · vanilla datapack · server-side only**

---

## Install

Drop the release `.zip` into your world's `datapacks/` folder and `/reload`.

```
<world>/datapacks/shadowslave-v<version>.zip
```

`pack.mcmeta` sits at the root of the archive, which is what Minecraft expects — if a pack
never appears in `/datapack list`, a wrapper folder is usually why.

**No client install.** Datapacks are server-side, so anyone can join a server running this
with a completely vanilla client and everything works: the dimension, the trial, Aspects,
advancements. That is a deliberate reason to stay a datapack through Phase 1.

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
5. **Wake a Sleeper**, with a Dormant Aspect and a Flaw.

Surviving a First Nightmare makes you a **Sleeper**, not an Awakened — canon puts Awakening after a
first journey into the Dream Realm, which Phase 1 does not have. The ladder is
**Mundane → Carrier → Sleeper (Dormant) → Awakened**.

`/trigger soul` shows where you stand.

|             |                                             |
| ----------- | ------------------------------------------- |
| **Aspects** | Shadow, Flame, Bone, Wind                   |
| **Flaws**   | Shadow Slave, Fragile, Ravenous, Weightless |

Rolled independently in the current Phase 1 prototype.

> These eight are **placeholders**. The novel is explicit that every Aspect is unique and that
> Flaws are personal rather than an independent random penalty. A dedicated pre-Java datapack
> redesign is tracked in `docs/OPEN-QUESTIONS.md` Q2: generated Aspect identity and
> behaviour-influenced Flaws, within what vanilla commands can honestly support. Java later replaces
> the remaining finite/predeclared effect machinery with a proper data model.

---

## Testing commands

```
/function shadowslave:test/help
```

| Command          | Does                                         |
| ---------------- | -------------------------------------------- |
| `test/selfcheck` | Asserts the pack loaded correctly            |
| `test/infect`    | Become a Carrier now                         |
| `test/cure`      | Back to untouched                            |
| `test/nightmare` | Enter the trial immediately, no bed          |
| `test/awaken`    | Historical name: skip to the Sleeper state   |
| `test/reset`     | Wipe everything and start over               |

Skip the countdown mid-trial with `/scoreboard players set @s ss_timer 1`.

**Testing commands bypass the systems they test.** `test/nightmare` walks past the ejection
cooldown and the weakness gate via a single-use `ss_test_bypass` tag that entry consumes —
otherwise the command for entering the trial could be refused by the trial itself. The rank gate
has no bypass on purpose: a Sleeper in a First Nightmare is a state nothing handles. Use
`test/reset`.

There is also a **Shadow Slave** advancement tab. Existing internal IDs remain under
`shadowslave:test/*` for save and harness compatibility, but the displayed tree is player-facing.
Each entry is still granted at the exact line its mechanic fires, so an incomplete branch records
where the loop stopped.

---

## Development

### Structure

```
shadowslave/              the datapack itself
  data/shadowslave/
    function/             all logic, grouped by responsibility
      nightmare/          the trial lifecycle
      progression/        First Nightmare -> Sleeper transition
      prototype/          temporary Aspect/Flaw generator
      aspect/  flaw/      recurring per-second placeholder effects
      awaken/             historical compatibility alias
      test/               testing commands
    dimension_type/  dimension/  worldgen/biome/
    advancement/  predicate/
  tools/validate.py       offline structure and reference checker
  tools/build_release.py  validated reproducible release ZIP builder
testserver/               local 1.21.1 server + Mineflayer harness
docs/superpowers/         historical design/spec material
docs/lore-research/       canon evidence and Java migration research
```

### Checks before shipping

```bash
python3 shadowslave/tools/validate.py      # static: structure, references, schemas, versions
cd testserver && node harness.mjs          # live: 32 assertions against a real 1.21.1 server
cd .. && python3 shadowslave/tools/build_release.py
```

The builder creates `shadowslave-vX.Y.Z.zip` at the repository root, containing only
`pack.mcmeta`, optional `pack.png`, and `data/**`, and prints its SHA-256. It reruns the static
validator; the live harness remains a separate required release gate.

**`validate.py`** catches what fails _silently_ in Minecraft — plural directory names, a
misspelled objective, a tag tested but never applied, an unresolvable function or predicate,
an attribute modifier added without a paired remove, a `dimension_type` that does not cover
its noise settings, project-banned absent-score selector filters, and version drift between
`pack.mcmeta`, the load banner, and `test/selfcheck`.

**`harness.mjs`** runs a Mineflayer bot against a local server, executing commands and reading
the replies back to assert on game state. It covers the mechanical half — state transitions,
guards, thresholds, teardown — and prints a **"needs a human"** list for the few behaviours that
cannot be observed reliably without a real player. Expected-query timeouts are test errors rather
than negative results, so an unreadable state cannot silently count as a pass.

Both exist because this project has repeatedly shipped code that was valid, well-formed, and
wrong. Almost every way a datapack breaks is silent.

### Conventions

The rules below are the *what*. **[docs/ENGINEERING-NOTES.md](docs/ENGINEERING-NOTES.md) is the
*why*** — each convention paired with the bug that produced it. Read it before changing code;
a bare rule tends to get "improved" away by someone who cannot see what it was protecting.

- **Namespace everything** `shadowslave`; objectives and tags carry an `ss_` prefix.
- **Singular directory names** — plurals silently do nothing in 1.21.
- **Comments explain why, not what.** A command says what it does; a comment should say what
  it protects against, or what was tried first and failed.
- **`ponytail:` and `PROTOTYPE-LIMIT:` comments** mark deliberate shortcuts and placeholder
  systems. Either is fine; always name the ceiling **and** the upgrade path, never just
  "temporary".
- **Guard at the choke point, never in the callers.** An invariant about a state transition
  belongs in the function that performs it. Three bugs came from caller-side guards, most
  recently an uninfected player being able to enter a nightmare.
- **`remove` before every `add`** on attribute modifiers. The upkeep runs once a second
  forever; without it modifiers stack until the player is unkillable.
- **Never guard on a score with `matches 0`**, and never filter a selector with `scores={x=..0}`.
  An absent score fails `matches` outright, and nothing writes 0 — use `unless ... matches 1..`.
  This has caused **three** bugs, one of which killed sneak-to-enter for five releases.
- **Player NBT is read-only.** `data merge|modify entity <player>` and
  `execute store ... entity <player>` are refused. Dynamic teleports go through command
  storage and a macro.

### Versioning

[Pride Versioning](https://pridever.org/) — `PROUD.DEFAULT.SHAME`. Tag first, then bump SHAME
for each round of fixes; holding a release back until it is clean makes the number lie.

Version stamping happens on `main` after review. `pack.mcmeta`, `init.mcfunction`, and
`test/selfcheck.mcfunction` must agree; the validator enforces all three.

---

## Phase 1 datapack boundary

The datapack is the Phase 1 reference implementation, not the permanent architecture for every
future system. These known ceilings should not be papered over with increasingly fragile command
machinery:

- one active Nightmare at a time through a shared creature and bossbar;
- broad item recovery on death inside the single-player Nightmare instance;
- a shallow `ss_rank` flag rather than a full Soul data model;
- the current four fixed Aspects/four fixed Flaws **pending the dedicated Q2 datapack rework**;
- a ravager stand-in, Overworld-noise Nightmare terrain, no custom GUI and no custom AI.

The ownership/state/entity/data-model ceilings are Java migration seams. The Aspect/Flaw placeholder
catalogue is the remaining explicit pre-Java design task, kept separate from release hardening so it
can be reviewed and tested as a focused gameplay change.

---

## Documents

|                              |                                                                     |
| ---------------------------- | ------------------------------------------------------------------- |
| [CHANGELOG.md](CHANGELOG.md) | Every version, and which issue it fixed                             |
| [ISSUES.md](ISSUES.md)       | Known issues, deliberate limitations, and what is confirmed working |
| [TESTING.md](TESTING.md)     | Full in-game test plans                                             |
| [docs/ENGINEERING-NOTES.md](docs/ENGINEERING-NOTES.md) | Why the code is like this — each convention with the bug that caused it |
| [docs/COLLABORATION.md](docs/COLLABORATION.md) | How the two AI agents work through this repo |
| [docs/OPEN-QUESTIONS.md](docs/OPEN-QUESTIONS.md) | Questions between agents and the answers that settled them |
| [docs/lore-research/](docs/lore-research/) | Canon evidence and migration research |
| `docs/superpowers/specs/`    | Historical design specs; read status banners before relying on them |

## Credit

_Shadow Slave_ is by **Guiltythree**. This is an unofficial fan project, not affiliated with
the author or publisher. Canon research and confidence labels are tracked under
[`docs/lore-research/`](docs/lore-research/); the novel is treated as the primary authority.
