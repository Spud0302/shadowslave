# Playtest: launch Minecraft with the current build

One command builds the mod from whatever branch you have checked out and opens
Minecraft 1.21.1 with it loaded. NeoForge, GeckoLib, Lodestone and
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

**Intention offering.** Before your first roll you can meditate at a lit Dream
Candle (sneak and use it) to offer an intention. Name a longing (Burn, Hide,
Endure or Return) and press Offer: one of the item in your hand is taken if the
Spell cares for it (Ember: coal, torches, blaze powder; Mist: feathers, phantom
membrane, ender pearls; Stone: cobblestone, iron, amethyst, diamond; Tide: kelp,
fish, prismarine, a water bucket; the full lists are the
`data/shadowslave/tags/item/intention/` tags). The Soul Sea shows the odds:
offering and longing for the same Nature takes it from 25% to about 44%, never
certain. Then sleep beside the candle; the roll uses the intention once.

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

## Nightmare Creatures

Four creatures, each with its own model, animations and AI. Spawn them from the
Spawn Eggs creative tab. Every creature has a **Rank** (Dormant, Awakened, ...)
that sets its health, damage and armour and the rank of its shards, and a
**Class** that sets how many shards it leaves (one per soul core) and how it
fights. Killing one always drops its shards and the Spell tells you what you
slew ("You have slain an Awakened Monster, Chainback.").

| Creature | Rank and Class | How it hunts | How to beat it |
| --- | --- | --- | --- |
| Ash Burrower | Dormant Beast (1 shard) | Sinks into dirt, sand or gravel and follows footsteps, then bursts out under you. Calls its pack. Bolts from fire and torches. | Sneak (it hunts by vibration), stand on stone (it cannot burrow there and is dazed if it runs onto it), hit it to force it up; arrows miss it while it is under ground |
| Chainback | Awakened Monster (2 shards) | Climbs walls, waits for you to look away and lunges, drags you toward it with a chain from 4-12 blocks. Retreats to heal when badly hurt, then comes back. | Break line of sight while the chain rattles; it is too wide for one-block gaps |
| Hollow Mimic | Awakened Demon (3 shards) | In the dark it speaks to you in chat with another player's name, from where it waits. Feints, picks the weakest player, opens doors, walks round fire and lava. | Light (block light 10+ weakens it and silences it), stay in pairs (it will not close in unless struck), and notice its lines never have punctuation |
| Ashen Tyrant | Awakened Tyrant (5 shards), the Gate Guardian | Slams the ground (ring of damage and knock-up, screen shake), spawns small copies of itself, drives the creatures around it. Boss bar. | Watch for the raised fists and step out of the ring; kill the small copies early |

**Shards from ordinary mobs** are now *faint*: worth a quarter of a creature
shard, and nothing at all to an Ascended core or higher.

**Block damage.** `/gamerule shadowslaveNightmareBlockDamage false` stops
Nightmare creatures and Gates from breaking blocks, like `mobGriefing`. It is
on by default.

## Nightmare Gates

Gates don't open on their own yet: you call one when you are ready.

1. Craft a **Gate Lure**: a Dormant soul shard on top, amethyst shard, crying
   obsidian, amethyst shard in the middle row, three obsidian along the bottom.
   It is also in the Functional Blocks creative tab. Mine it with an iron
   pickaxe or better; a broken Lure keeps what it was fed.
2. Place it and right-click it with soul shards to feed it. Each shard adds
   pull: Dormant 1, Awakened 4, Fallen 16, Corrupted 64, and a faint shard a
   quarter of its rank. Its runes light up as the pull grows, and the action
   bar shows the **Category forecast** (right-click empty-handed to read it
   again). Four Dormant shards are the least that calls a Gate (Category 1);
   about 8 pull forecasts Category 2, 24 Category 3, 64 Category 4 and 160
   Category 5. It holds 32 shards.
3. **Sneak and right-click it empty-handed** to light it. The Lure burns away
   and a Gate begins to open where it stood. Everyone within 96 blocks gets
   the warning in chat and on screen, with the odds in the canon form
   ("Category 2 (89%), 1 (8%), 3 (3%), higher (undefined)"), and a boss bar
   counts down 30 seconds.
4. The Gate opens at a Category rolled from those odds, then sends out waves
   of Nightmare creatures (more, and of higher Rank, at higher Categories).
   When the waves are nearly cleared, the **Gate Guardian** emerges: an
   Ashen Tyrant, Dormant at Category 1 up to Corrupted at Category 5.
5. Kill the Guardian and the Gate closes. Creatures it let out that are still
   alive crumble to ash and drop nothing.

A Gate only runs while a player is within 160 blocks, and two Gates can't
open within 64 blocks of each other. With block damage on, the opening blast
breaks the ground around the rift. Operators can use `/shadowslave gate open
<1-5>` (a certain Category where you stand, no Lure needed), `/shadowslave gate
status` and `/shadowslave gate close`.
