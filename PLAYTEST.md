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

**Meditation and Awakening.** Sneak and use a lit Dream Candle (with anything
in hand) to meditate: the **Soul Sea** opens, showing your Soul Rank, your core
and what the next rank asks. For now Awakening asks for a saturated core. Pick one
of your Nature's two Awakened abilities and press Awaken while you stay beside
the candle. Awakening raises your Soul and Aspect to Awakened, puts the new
ability on the V key, doubles the essence pool (200) and starts a larger core
(600). Operators can skip the candle with `/shadowslave awaken <name>` (the core
must still be saturated; `/shadowslave core fill` helps) or test any ability with
`/shadowslave ability grant <name>`.

**Refinement.** The Soul Sea has a second page, Refine (press 2 or click the
tab). You get one refinement point when the Spell names your Aspect and three
more at Awakening. Place them while meditating: **Body** gives a heart, 3% speed
and half a point of melee damage each; **Soul** gives 15 essence and a 10%
faster refill each; **Aspect** makes every ability 6% cheaper each (up to 30%).
**Respec** takes every point back but loses one for good.

**Channeling (Awakened).** Press Z (Channel Essence) to burst forward and up
for 20 essence; a channeled leap forgives about its own height of fall. Sneak
while pressing Z to load your next melee blow instead (25 essence): it hits
1.75 times as hard and throws the target back. The essence bar glows amber while
a blow is loaded, and the charge fades after 6 seconds.

| Nature | Ability | What it does |
| --- | --- | --- |
| Ember | Carry the Flame (40) | Regeneration II and fire resistance to the player or tamed pet you look at, or yourself |
| Ember | Ember Ward (45) | A ring of fire for 8 s; hostile mobs that touch it are thrown back burning |
| Mist | Through the Haze (30) | Every creature and player within 32 blocks glows for 10 s |
| Mist | Veil (45) | A dome of mist for 8 s; hostile mobs inside are blinded and lose their target |
| Stone | Root in Stone (35) | A 3x3 stone wall two blocks ahead that crumbles after 12 s; it only fills empty space |
| Stone | Quake (45) | Stomp: hostile mobs within 5 blocks take damage, are thrown up and slowed |
| Tide | Low Tide (30) | Surge forward on a wave that shoves mobs aside |
| Tide | Undertow (40) | Drags mobs within 8 blocks of where you look toward that spot and slows them |
