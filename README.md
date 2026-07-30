# Shadow Slave for Minecraft

An unofficial fan project inspired by _Shadow Slave_ by Guiltythree.

The repository now contains three related products:

- a completed vanilla Minecraft 1.21.1 datapack;
- a shared/standalone NeoForge Java core in active alpha development;
- a planned Nightmare Spell modpack prototype using the same canonical Java state.

See [PROJECT-STATUS.md](PROJECT-STATUS.md) for the exact current gate and next action.

## Datapack v1.0.0

The first public product is complete and frozen as `datapack-v1.0.0`.

Install the release ZIP without extracting it:

```text
<world>/datapacks/shadowslave-v1.0.0.zip
```

Reopen the world or run `/reload`. It requires Minecraft Java Edition 1.21.1 and difficulty
Easy or higher. A multiplayer server installs it once; joining players use ordinary vanilla
1.21.1 clients.

The datapack proves the playable reference loop:

```text
untouched person -> Carrier -> First Nightmare prototype -> Sleeper/Dreamer
                 -> persistent Aspect identity + persistent Flaw identity
```

Its first-sleep trigger, safe ejection, global Nightmare ownership, finite mechanics catalogue
and timer/creature scenario are prototype behaviour—not universal canon or permanent Java design.

## Java core

The Java project lives under [`mod/`](mod/). Current development version: `0.1.0-alpha.4`.

Implemented:

- persistent lore-aligned Soul state;
- Uninfected, Carrier, Aspirant and Dreamer stages;
- separate Soul Rank and Aspect Rank;
- server-authoritative mutations and read-only client snapshots;
- an O-key Soul screen;
- alpha-schema migration;
- pure datapack translation fixtures;
- client, server, unit-test and JAR CI gates.

Alpha.4 is **CI-green and Claude-verified** — [Issue #16](https://github.com/Spud0302/shadowslave/issues/16)
is closed, with the build, both startup smokes and the validator re-run locally rather than taken from
workflow status. The real-client walkthrough is **deferred, not performed** (owner decision **D2**):
it judges presentation and feel, so it no longer gates anything. This is not a public mod release and
does not yet contain a playable Java Nightmare.

### Development commands

```text
/shadowslave soul
/shadowslave soul_screen
/shadowslave infect
/shadowslave begin_first_nightmare_test
/shadowslave complete_first_nightmare_test
/shadowslave reset
```

These are architecture smoke commands, not final natural progression.

### Build

Java 21 is required. From the repository root:

```bash
./mod/gradlew -p mod build
./mod/gradlew -p mod runClientSmoke --no-daemon
./mod/gradlew -p mod runServerSmoke --no-daemon
```

## Nightmare Spell modpack

[`modpack/`](modpack/) records Path A of the architecture comparison. Existing mods may provide
generic spells, creatures, equipment, loot or presentation. The Shadow Slave core still owns
Soul identity, progression, Aspect/Flaw identity, Nightmare lifecycle and migration.

No dependency manifest or public modpack exists yet.

## Lore and design

- Novel mechanics and terminology are primary authority.
- The manhwa is a secondary visual/staging reference.
- Fan translations help access material but do not override the novel.
- Project appraisal and generation algorithms are explicitly game design where canon provides no
  deterministic formula.
- Java preserves earned/imported identity while replacing datapack machinery.

Start with [`docs/JAVA-LORE-ALIGNMENT.md`](docs/JAVA-LORE-ALIGNMENT.md) and
[`docs/JAVA-HANDOFF.md`](docs/JAVA-HANDOFF.md).

## Repository map

```text
shadowslave/          frozen datapack source
testserver/           datapack deployment and Mineflayer harnesses
mod/                  shared/standalone NeoForge Java core
modpack/              dependency-led prototype plan
shared-test-spec/     implementation-neutral acceptance scenarios
docs/                 lore, architecture, workflow and historical records
```

## Administrative authority

| Need | Read |
| --- | --- |
| exact current state | [PROJECT-STATUS.md](PROJECT-STATUS.md) |
| agent workflow and merge gates | [docs/COLLABORATION.md](docs/COLLABORATION.md) |
| active defects and limitations | [ISSUES.md](ISSUES.md) |
| current test gate and historical test records | [TESTING.md](TESTING.md) |
| shipped history | [CHANGELOG.md](CHANGELOG.md) |
| GPT session checkpoint | [GPT_HANDOFF.md](GPT_HANDOFF.md) |
| Java implementation status | [mod/IMPLEMENTATION-STATUS.md](mod/IMPLEMENTATION-STATUS.md) |
| questions between agents | [docs/OPEN-QUESTIONS.md](docs/OPEN-QUESTIONS.md) |

## Collaboration

GPT works on `gpt/*` branches and does not merge its own feature work. Claude independently
reviews/tests GPT work, records evidence, fixes or rejects defects, stamps versions and merges.
GitHub CI is evidence, not a replacement for Claude's verification or Andrew's playtest judgement.

## Credit

_Shadow Slave_ is by Guiltythree. This project is unofficial and is not affiliated with the
author or publisher. It does not copy novel prose or official artwork.
