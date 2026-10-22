# Shadow Slave for Minecraft

An unofficial fan mod inspired by _Shadow Slave_ by Guiltythree, for Minecraft Java Edition 1.21.1
on NeoForge.

## What you can do today

The mod currently covers a player's first steps as an Awakened: rolling an Aspect, using its
abilities, and growing the soul core to the first rank-up.

- **Dream Candle ritual.** Light a Dream Candle beside your bed and sleep. The Spell names your
  Aspect, Innate rule, Ability and Flaw over a dark veil. There are four Natures (Ember, Mist,
  Stone and Tide), each with a hand-made kit, and the roll leans slightly towards how you played
  before it.
- **Intention offering.** Before the roll, meditate at the candle and offer an item and a longing to
  tilt the odds towards one Nature. It never makes the result certain.
- **Abilities and essence.** Press R for your ability. Abilities have no cooldowns; they spend
  essence from one shared pool, shown beside the ability slot left of the hotbar. Each ability has a
  casting pose that other players see.
- **Soul shards and the core.** Mobs (and players, by default) drop graded soul shards. Absorbing
  them fills your soul core, which adds health as it fills.
- **The Soul Sea and Awakening.** Meditating at a lit Dream Candle opens the Soul Sea. With a
  saturated core you can Awaken, choose one of your Nature's two Awakened abilities (bound to V),
  and double your essence pool.
- **Refinement and channeling.** Spend refinement points on Body, Soul or Aspect, respec at a loss,
  and (once Awakened) channel essence with Z into a leap or a loaded melee blow.
- **Soul screen.** Press O for tabs covering your Aspect, Abilities, Flaw and Soul.

The full player guide, with every ability and its essence cost, is [PLAYTEST.md](PLAYTEST.md).

## Play the current build

Install JDK 21 and Git, clone this repository, then run `play.bat` (Windows), `play.command`
(macOS) or `./play.sh` (Linux). It builds the mod from the checked-out branch and opens Minecraft
with it loaded. NeoForge and the libraries are fetched for you. See [PLAYTEST.md](PLAYTEST.md) for
the options.

There is no public release yet. The mod version is `0.1.0-preview.2`.

## Repository map

```text
mod/                  the NeoForge mod (Gradle root; run ./mod/gradlew -p mod build)
play.bat / play.sh    one-step build-and-play launchers
shadowslave/          the frozen vanilla datapack (datapack-v1.0.0), kept as a reference
testserver/           Mineflayer harnesses for the frozen datapack (run by hand, not in CI)
shared-test-spec/     implementation-neutral acceptance scenarios
docs/                 lore research, design notes and historical records
```

## Status and planning

| Need | Read |
| --- | --- |
| current state and what's next | [PROJECT-STATUS.md](PROJECT-STATUS.md) |
| known issues | [ISSUES.md](ISSUES.md) |
| playing and testing | [PLAYTEST.md](PLAYTEST.md) |
| rules for AI agents working here | [AGENTS.md](AGENTS.md) |
| lore source rules | [docs/LORE-SOURCE-POLICY.md](docs/LORE-SOURCE-POLICY.md) |
| older status documents | [docs/history/](docs/history/README.md) |

## Datapack v1.0.0

The original vanilla datapack is finished and frozen under `shadowslave/`. It still runs on
ordinary 1.21.1 servers, but only one player can be inside its First Nightmare at a time. It is a
reference for behaviour and data import, not the design target for the mod.

## Lore

Novel text is the primary authority for mechanics and terminology; the wiki and official
adaptations guide anything the novel leaves open. Mechanics the novel doesn't define are labelled
**DESIGN**. See [docs/LORE-SOURCE-POLICY.md](docs/LORE-SOURCE-POLICY.md).

## Credit

_Shadow Slave_ is by Guiltythree. This project is unofficial and is not affiliated with the author
or publisher. It does not copy novel prose or official artwork.
