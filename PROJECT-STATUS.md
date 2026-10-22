# Shadow Slave project status

**Status date:** 2026-10-22
**Baseline:** `main@679bf9f` (after PR #27)
**Release state:** development preview `0.1.0-preview.2`; no public release

PR and issue numbers below are this repository's GitHub numbers. Older documents in
[docs/history/](docs/history/README.md) cite numbers from the repository's previous history, which
do not match.

## Direction

In October 2026 the project was refocused on one core loop, finished with its visuals before
anything else is added: **rolling an Aspect and growing through the ranks with its abilities.** The
First Nightmare, Dream Realm regions and advanced combat are parked as later updates. The owner
chose four Natures to start (Ember, Mist, Stone, Tide), each with a hand-made kit, and an opt-in
roll at a Dream Candle instead of a forced one.

## What main contains

| Area | State | PRs |
| --- | --- | --- |
| Dream Candle ritual and the four-Nature Aspect roll, lightly weighted by playstyle | merged | #11 |
| Ability framework, the four Dormant abilities, Innate rules and Flaws | merged | #12, #14 |
| Spell reveal overlay, HUD ability slot, tabbed Soul screen | merged | #15, #16 |
| Ability icons, casting poses (Player Animation Library) and effects | merged | #6, #17 |
| Shared essence pool instead of cooldowns | merged | #19 |
| Graded soul shards, soul core saturation, optional PvP shards | merged | #20 |
| Eight Awakened abilities and the second ability key (V) | merged | #21 |
| Meditation, the Soul Sea and Awakening | merged | #22 |
| Essence channeling (Z) | merged | #23 |
| Refinement points and respec | merged | #24 |
| Intention offering before the roll | merged | #25 |
| Combat Core removed; Lodestone 1.8.2 cast effects added | merged | #27 |
| `brain/` vault retired, status docs refreshed | merged | #26 |
| One-step playtest launcher (`play.bat`, `play.sh`, `play.command`) | merged | #8 |
| CI: pinned actions, the required `all-checks` gate, auto-merge | merged | #7, #10, #13 |

[PLAYTEST.md](PLAYTEST.md) describes every player-facing feature above.

Earlier systems are still in the code but are not part of the current loop: the First Nightmare
scenario (The Last Signal), Memories and Echoes, the Ashen Expanse Dream Realm slice and the
datapack migration commands. Treat them as legacy until a later update picks them up.

## Next

- Playtest feedback on the Dormant-to-Awakened slice.
- Ideas the owner has raised: a nemesis system (mobs that absorb a killed player's shards) and
  challenge-based rank-up triggers. Each rank's trigger is kept swappable for this.

## Build and CI

- Toolchain: Minecraft 1.21.1, NeoForge 21.1.264, ModDevGradle 2.0.148, Gradle 9.8.2, JDK 21.
  Libraries: GeckoLib 4.9.3, SmartBrainLib 1.16.11, Player Animation Library 1.1.6, Lodestone 1.8.2.
- Build with JDK 21: `./mod/gradlew -p mod build`. Game tests: `./mod/gradlew -p mod runGameTestServer`.
- `main` is protected by a ruleset that requires the `all-checks` job. PRs auto-merge once it
  passes, and merged branches are deleted automatically.
- Workflows: Preview gates (`java-core.yml`: the mod build, game tests, client boot and a
  server restart), Modpack shell, Package datapack (manual or `datapack-v*` tags only), and All
  checks. The frozen datapack is no longer tested in CI.
- The `brain/` multi-agent vault (claims, leases, handoffs) was an experiment and has been
  removed with its Vault integrity workflow. Its notes remain in Git history.
