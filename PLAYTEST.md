# Playtest: launch Minecraft with the current build

One command builds the mod from whatever branch you have checked out and opens
Minecraft 1.21.1 with it loaded. NeoForge, GeckoLib, SmartBrainLib and
PlayerAnimationLib are pulled in automatically; you don't install anything into
a launcher.

## One-time setup

1. Install **JDK 21** ([Temurin 21](https://adoptium.net/temurin/releases/?version=21)).
2. Install **Git** and clone the repo:

   ```bash
   git clone https://github.com/Spud0302/shadowslave
   ```

The first launch downloads Minecraft and the libraries, so it takes several
minutes. Later launches only rebuild what changed.

## Launch

| OS | How |
| --- | --- |
| Windows | Double-click `play.bat`, or run `.\play.bat` in PowerShell |
| macOS | Double-click `play.command`, or run `./play.sh` |
| Linux | `./play.sh` |

Options (same on every OS):

| Option | What it does |
| --- | --- |
| `--pull` | Pull the latest commits for the current branch first |
| `--branch NAME` | Fetch and switch to branch `NAME` first (for example a PR branch) |
| `--world NAME` | Skip the menus and load the singleplayer world `NAME` |

Examples:

```bash
./play.sh --pull                      # latest main, then play
./play.sh --branch claude/some-pr     # try a PR's build
./play.sh --world Visuals             # jump straight into the "Visuals" world
```

`--pull` and `--branch` refuse to run if you have uncommitted changes, so they
never overwrite your work.

## Where things live

The game folder is `mod/run/` (ignored by git). Worlds are in
`mod/run/saves/`, and they persist between launches, so you can keep one test
world for checking visuals. For `--world`, create the world once in-game and use
its folder name.

You play offline as `Dev`. Multiplayer servers that need a Microsoft account
won't accept this client.

## Rolling an Aspect (Dream Candle)

Added by claude-code, 2026-10-22.

1. Craft a **Dream Candle**: any candle plus an amethyst shard (shapeless). It is
   also in the Functional Blocks creative tab.
2. Place it within two blocks of your bed and light it (flint and steel).
3. Sleep. When you wake, the candle burns down and the Spell types out your
   Aspect, Innate, Ability and Flaw over a dark veil. Click (or Space) once to
   show it all, and again to open your Soul screen; it also moves on by itself.
   Without a lit Dream Candle, sleep is plain vanilla.

You can only roll once. Operators can reroll with `/shadowslave aspect reroll`
(optionally followed by player names), and `/shadowslave reset` clears the Soul
entirely. The roll picks Ember, Mist, Stone or Tide, nudged slightly by how you
played beforehand (lighting and smelting, sneaking, mining, swimming). Press R to use
your Aspect's ability. Abilities have no cooldowns: each one spends **essence**
from a single pool that refills in about a minute (100 essence at Dormant). The
slot sits left of the hotbar with the essence bar beside it; the tick on the bar
marks what the ability costs. The slot shades while the pool is short, glows
while Returning Tide's mark is set, and flashes (with a soft chime) when you can
afford it again. Press O for the Soul screen, with tabs for your Aspect,
Abilities, Flaw and Soul (click a tab or press 1-4). Each ability has a casting
pose that other players see too.

**Soul shards and your core.** Once your Aspect is named you have a soul core.
Hostile mobs killed by a player sometimes drop soul shards graded by creature
rank: most mobs drop Dormant shards (about 1 in 8), armoured mobs, illagers and
most Nether mobs drop Awakened shards, wither skeletons, evokers and Elder
Guardians drop Fallen shards, and the Wither, Ender Dragon and Warden always
drop a Corrupted shard. Right-click a shard to absorb it (sneak to absorb the
stack). The Soul tab shows the core filling; it adds up to two hearts as it
fills, and once **saturated** it takes no more. Killing another player tears a
quarter of their core out as a shard you can absorb; turn that off with
`/gamerule shadowslavePvpSoulShards false`. Operators can use
`/shadowslave core fill` or `/shadowslave core set <n>` to test.

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Ember | Fire and lava hurt half as much | Kindle: night vision, speed, nearby hostiles catch fire (35 essence) | Cold Ash: weak in water or rain |
| Mist | Sneaking sets off no sculk or wardens | Mist Passage: blink up to 8 blocks and turn invisible for 3 s (25 essence) | Borrowed Breath: each blink costs hunger, air runs out faster |
| Stone | No knockback while standing still | Holdfast: Resistance II and no knockback for 6 s, no sprinting (35 essence) | Stone Sleep: slow after sleeping, you sink in water |
| Tide | Breathe underwater | Returning Tide: press to mark, press again within 10 s (or wait) to be pulled back (30 essence) | Tidal Debt: weak if a day passes without touching water or rain |
