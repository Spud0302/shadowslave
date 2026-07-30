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
<world>/datapacks/dist-shadowslave-<version>.zip
```

`pack.mcmeta` sits at the root of the archive, which is what Minecraft expects — if a pack
never appears in `/datapack list`, a wrapper folder is usually why.

**No client install.** Datapacks are server-side, so players can join a server running this with a
completely vanilla client. Phase 1 is not a fully multiplayer-instanced system, however: it uses one
shared Nightmare bossbar/creature set, so **simultaneous active First Nightmares are not supported**.
That ceiling is intentionally deferred to the Java instance system rather than being papered over with
a fragile command-only ownership framework.

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

|             |                                           |
| ----------- | ----------------------------------------- |
| **Aspects** | Shadow, Flame, Bone, Wind                 |
| **Flaws**   | Sunscorched, Fragile, Ravenous, Weightless |

These eight are **Phase 1 placeholders**. They are rolled independently for the prototype. The novel
is explicit that every Aspect is unique and that Flaws are personal rather than random; the Java-era
system will replace this fixed generator rather than expanding the catalogue. `Sunscorched` is an
invented placeholder name — the legacy internal id still says `shadow_slave` for save/test
compatibility, but **Shadow Slave is Sunny's Aspect, not a Flaw**.

The **Shadow Slave** advancement tab tracks the major milestones of the Phase 1 loop. Internally the
same advancement IDs double as regression markers for the automated harness, but the release-facing
text is gameplay rather than QA instrumentation.

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
| `test/awaken`    | Compatibility name: skip to Sleeper + roll   |
| `test/reset`     | Wipe player test state and start over        |

Skip the countdown mid-trial with `/scoreboard players set @s ss_timer 1`.

**Testing commands bypass the systems they test.** `test/nightmare` walks past the ejection
cooldown and the weakness gate via a single-use `ss_test_bypass` tag that entry consumes —
otherwise the command for entering the trial refuses to enter the trial. The rank gate has no
bypass on purpose: a Sleeper in a First Nightmare is a state nothing handles. Use `test/reset`.

---

## Development

### Structure

```
shadowslave/            the datapack itself
  data/shadowslave/
    function/           all logic, grouped by responsibility
      nightmare/          the trial lifecycle
      progression/        one-time Phase 1 progression transitions
      prototype/          temporary four-Aspect/four-Flaw generator
      aspect/  flaw/      recurring powers and penalties
      test/               testing commands
    dimension_type/  dimension/  worldgen/biome/
    advancement/  predicate/
  tools/
    validate.py         offline structure/reference checker
    build.py            validated deterministic release ZIP builder
testserver/             local 1.21.1 server + Mineflayer harness
docs/lore-research/     canon evidence base + implementation brainstorm
docs/reviews/           cross-agent code/release reviews
docs/superpowers/       historical design spec and implementation plan
```

### Checks before shipping

```bash
python3 shadowslave/tools/validate.py      # static: structure, references, schema
cd testserver && node harness.mjs          # live: behavioral assertions against a real server
cd .. && python3 shadowslave/tools/build.py # validated deterministic installable ZIP
```

**`validate.py`** catches what fails _silently_ in Minecraft — plural directory names, a
misspelled objective, a tag tested but never applied, an unresolvable function or predicate,
an attribute modifier added without a paired remove, a `dimension_type` that does not cover
its noise settings, and version drift between the manifest and load message.

**`harness.mjs`** runs a Mineflayer bot against a local server, executing commands and reading
the replies back to assert on game state. It covers the mechanical half — state transitions,
guards, thresholds, teardown — and prints a **"needs a human"** list for everything requiring
judgement.

**`build.py`** refuses to package a validation failure, derives the archive name from
`pack.mcmeta`, normalizes ZIP metadata for reproducible output, and includes only the player-facing
pack (`pack.mcmeta`, optional `pack.png`, and `data/`) at archive root.

All three layers exist because this project has repeatedly shipped code that was valid, well-formed,
and wrong. Almost every way a datapack breaks is silent.

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

---

## Documents

| | |
| --- | --- |
| [docs/RELEASE-CHECKLIST.md](docs/RELEASE-CHECKLIST.md) | **Live release blockers, acceptance test and frozen-Phase-1 boundary** |
| [CHANGELOG.md](CHANGELOG.md) | Every version, and which issue it fixed |
| [ISSUES.md](ISSUES.md) | Historical issue/fix record; some old sections intentionally describe their original release state |
| [TESTING.md](TESTING.md) | Historical playtest sweeps; use the release checklist above for the current acceptance pass |
| [docs/ENGINEERING-NOTES.md](docs/ENGINEERING-NOTES.md) | Why the code is shaped this way — each convention with the bug that caused it |
| [docs/COLLABORATION.md](docs/COLLABORATION.md) | How Claude and GPT coordinate through git |
| [docs/lore-research/](docs/lore-research/) | Canon research and confidence-labelled evidence |
| [docs/reviews/](docs/reviews/) | Durable code/release reviews and agent replies |

## Credit

_Shadow Slave_ is by **Guiltythree**. This is an unofficial fan project, not affiliated with the
author or publisher. Canon research used for the project is documented under `docs/lore-research/`,
with claims separated by confidence and checked against the novel/official release and community
reference sources according to that directory's source policy.
