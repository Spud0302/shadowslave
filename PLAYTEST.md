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

## Dev menu (testing shortcuts)

Added by claude-code, 2026-10-31.

Press **F7** (rebindable under Controls, Shadow Slave) to open the dev menu. It
needs operator rights: turn cheats on when you create a single-player world, or
`/op` yourself on a server. Without them the key only shows a short message.
Every button sends a `/shadowslave` command, so the server still checks
permission, and you can type the same commands by hand.

- **Nature**: click any of the 28 Natures to become it on the spot, without the
  Dream Candle or the reveal. An Awakened soul stays Awakened and gets that
  Nature's first Awakened ability (`/shadowslave dev nature <nature>`).
- **First key**: put any Nature's Dormant ability on R
  (`/shadowslave dev ability dormant <ability>`). Your Nature, innate rule and
  Flaw stay the same, so you can try one ability against another kit.
- **Second key**: put any of the 56 Awakened abilities on V, two pages of
  Natures, or clear it (`/shadowslave dev ability awakened <ability|none>`). You
  do not need to be Awakened.
- **Progress**: set the soul to Dormant or Awakened (`/shadowslave dev rank`),
  empty, half-fill or fill the core, restore health, food and essence
  (`/shadowslave dev restore`), toggle free casting so abilities cost no essence
  (`/shadowslave dev free_casting`), take 16 soul shards of any rank
  (`/shadowslave dev shards <rank> [count]`), reroll a random Aspect, or reset the
  soul.
- **World**: enter or wake from the Dream Realm, open a Category 1 or 2 Gate or
  close it, set day or night, clear the weather, switch creative or survival,
  skip 60 seconds or abort inside a Nightmare, and start any Nightmare on your
  own.

Hover a button to read what it does. The footer shows what is on each key now.

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
entirely. The roll picks one of twenty-four Natures (Ember, Mist, Stone, Tide, Warden,
Seer, Reaper, Wellspring, Forge, Delve, Mason, Verdant, Wayfarer, Lull, Gloam,
Rime, Fury, Threshold, Anchor, Hourglass, Herald, Keeper, Fortune, Cantor), nudged slightly by how you played beforehand (lighting
and smelting, sneaking, mining, swimming nudge the first four). Operators can
pick a Nature outright with `/shadowslave aspect set <nature>`. Press R to use
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
Endure, Return, Hunt, Foresee, Devour, Give, Shape, Dig, Build, Grow, Wander, Rest, Darken, Chill, Rage, Keep, Weigh, Linger, Rally or Guard) and press Offer: one of the item in your hand is taken if the
Spell cares for it (Ember: coal, torches, blaze powder; Mist: feathers, phantom
membrane, ender pearls; Stone: cobblestone, iron, amethyst, diamond; Tide: kelp,
fish, prismarine, a water bucket; Warden: bows, arrows, a lead, a rabbit's foot;
Seer: a spyglass, an ender eye, a compass, a map; Reaper: rotten flesh, spider
eyes, soul sand, an echo shard; Wellspring: golden apples, honey, glistering
melon, bread; Forge: gold or copper ingots, an anvil, a furnace; Delve: a
pickaxe, raw copper, redstone, lapis; Mason: bricks, stone bricks, planks,
scaffolding, a stonecutter; Verdant: seeds, wheat, bone meal, saplings, carrots,
potatoes, moss; Wayfarer: leather boots, a saddle, a boat, a cartography
table, rails, a minecart; Lull: a bed, blue wool, dandelions, a milk bucket, a
note block, a jukebox, a bell; Gloam: black dye, black wool, a black candle,
tinted glass, blackstone; Rime: snowballs, snow, ice, packed or blue ice, a
powder snow bucket; Fury: an iron sword or axe, a stone axe, a goat horn, TNT,
gunpowder, red dye; Threshold: a chest, barrel, ender chest, shulker shell,
bundle, an oak or iron door; Anchor: a lodestone, chain, heavy core, iron bars,
pointed dripstone, slime balls; Hourglass: sand, red sand, glass, a repeater,
comparator, daylight detector; Herald: a white, red, blue or yellow banner, a
golden helmet, an emerald, a book and quill; Keeper: a shield, a respawn anchor,
crying obsidian, an iron chestplate, a cobblestone wall, an iron trapdoor;
Fortune: a gold nugget, gold block, raw gold, golden carrot, emerald block,
sunflower; Cantor: a name tag, book, paper, oak sign, the "cat" music disc, a
sculk sensor; the
full lists are the `data/shadowslave/tags/item/intention/` tags). The Soul Sea
shows the odds under each longing: offering and longing for the same Nature
takes it from about 4% to 9%, never certain. Then sleep beside the candle; the roll uses the intention once.

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
| Warden | Snare Sigil (35) | A sigil on the ground where you look; the first hostile to step on it is rooted for 4 s |
| Warden | Relentless Hunt (30) | Leap to your marked prey (within 32 blocks) and gain Haste II and Speed |
| Seer | Fate Thread (35) | Snap back to where you stood 3 s ago |
| Seer | Shared Sight (40) | For 60 s, players within 32 blocks get your Forewarnings and hostiles you see glow |
| Reaper | Harvest (40) | Up to 8 hostiles within 8 blocks take 4 damage; 6 essence back for each |
| Reaper | Reaping Arc (30) | A 120 degree sweep for 6 damage; anything left at 30% health or less (100 max health or less) is reaped outright for 10 essence |
| Wellspring | Soul Ward (35) | Absorption II for 30 s on the ally you look at, or yourself |
| Wellspring | Overflow (50) | A pool of 8 blocks for 20 s: Regeneration and 3 essence a second to everyone in it |
| Forge | Masterwork (60) | For 60 s the next billet you fuse may reach one Tier above your Forging level, with a wider hammer mark and one grade higher |
| Forge | Molten Edge (35) | For 15 s each melee hit deals 3 more damage and sets the target alight |
| Delve | Vein Pull (40) | Mines the whole vein (up to 32 blocks) of the ore you look at, with the right tool, into your pack |
| Delve | Stonewalk (30) | Step through up to 8 blocks of wall ahead to the first open space |
| Mason | Blueprint (50) | First press learns the 5x5x5 shape ahead (free); press within 60 s to build it where you look from your pack |
| Mason | Scaffold (25) | For 10 s, planks appear under you when you step over a drop; they fall away after 8 s |
| Verdant | Living Wall (35) | A 5x2 flowering hedge for 15 s; hostiles touching it take 2 damage and Slowness II |
| Verdant | Wild Call (30) | Tames the animal you look at (12 blocks); your pets within 16 get Regeneration II and Strength for 20 s |
| Wayfarer | Anchor Recall (40) | A 3 s channel home: your Dream Anchor in the Dream Realm, your bed elsewhere (same world). A hit breaks it, and a broken channel costs nothing |
| Wayfarer | Trailblaze (30) | For 15 s you run faster and your sprint lays a glowing trail; allies who step on it get Speed II |
| Lull | Dreamfall (45) | Every hostile within 6 blocks no higher in rank than you sleeps for 4 s |
| Lull | Sleepwalker (30) | The sleeping creature you look at walks where you look for 10 s, still asleep |
| Gloam | Shade Step (20) | Step to the spot you look at (24 blocks), if it is dim or dark there (light 7 or less) |
| Gloam | Snuff (35) | Torches and lanterns within 10 blocks go out and candles, campfires and furnaces dim for 30 s; hostiles nearby lose you and are blinded for 4 s |
| Rime | Encase (40) | The creature you look at (16 blocks, no higher in rank) is frozen in a shell of real ice for 4 s |
| Rime | Rimepath (25) | For 20 s still water freezes under your feet, and you get Speed II on ice |
| Fury | Ground Slam (35) | Hostiles within 5 take 4 damage plus up to 6 more with rage and are thrown up; spends half your rage |
| Fury | Second Wind (30) | Burn all your rage (20 at least): heal a point per 10 rage and Speed II for 5 s |
| Threshold | Doorway (40) | First press opens a door where you stand (free); press again within 60 s, at least 3 blocks away, to open its twin. For 30 s you and your allies walk into one door and out of the other |
| Threshold | Banish (45) | The creature you look at (16 blocks, no higher in rank) drops out of the world for 5 s, then comes back where it was |
| Anchor | Crush (35) | For 10 s, landing from 3 blocks or more sends out a shockwave that grows with the fall (half again while Heavy, 14 max) and the fall cannot hurt you |
| Anchor | Gravity Well (40) | A well opens where you look (24 blocks): for 4 s hostiles within 8 blocks of it are dragged in and pinned |
| Hourglass | Rewind (35) | Snap back to where you stood 3 s ago, with the health you had then if it was more |
| Hourglass | Still Moment (60) | Every hostile within 8 blocks, no higher in rank, is held out of time for 2 s |
| Herald | Rally Call (40) | Every other player within 256 blocks in your world hears the call; those who sneak within 10 s are drawn to your side. Your pets within 64 come at once |
| Herald | Oath of Arms (35) | Bind the ally you look at (16 blocks) for 20 s: half of every blow on them falls on you instead |
| Keeper | Bulwark (50) | A 7-block dome for 10 s: you and your allies inside take half harm, and arrows and other projectiles from outside fly back at the edge. You can fight out of it |
| Keeper | Gateway Step (50) | Standing by a lit Dream Candle, step straight to the Gateway of your clan's nearest Citadel. From the waking world it is a crossing: your body sleeps where you stood |
| Fortune | Twist Fate (35) | For 20 s, the next attack aimed at you has an even chance to miss. That one attack spends the ward, hit or miss |
| Fortune | Borrowed Luck (30) | Mark the creature you look at (20 blocks). If it dies within 30 s, by anyone's hand, it drops twice as much and gives twice the experience |
| Cantor | Silencing Word (35) | The Nightmare Creature you look at (20 blocks) cannot use its special abilities for 5 s (no slam, chain pull, burrow, borrowed voice, lunge or summons). It still walks and bites |
| Cantor | Rallying Name (30) | Clear every harmful effect (and fire) from the ally you look at (24 blocks) and give them Speed II for 8 s |

### The newer Natures: Warden, Seer, Reaper, Wellspring

Added by claude-code, 2026-10-23. Quick start: `/shadowslave aspect set warden`
(or seer, reaper, wellspring), then `/shadowslave core fill` and
`/shadowslave awaken <name>` for the Awakened abilities above. Gates and
creatures (see below) make the best targets.

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Warden | Gate Sense: a faint trail of light (only you see it) points to the nearest open Gate; looking at a Nightmare Creature shows its rank, class and health under the crosshair | Mark Prey: the creature you look at (32 blocks) glows for 30 s and takes 50% more damage from everyone (20 essence) | Quarry Lock: while a mark is up, you deal half damage to anything else |
| Seer | Forewarning: a violet edge and a marker pointing at the attacker, with a bell, just before a big attack (Ashen Tyrant slam, Chainback chain pull, a Tyrant calling its brood, a creeper hissing) | Foresight: for 30 s hostiles within 24 glow; a Gate within 128 blocks that is still warning settles its Category now and its waves are read out in chat (30 essence) | Clouded Eyes: thick fog beyond about 36 blocks, lifted while Foresight is up |
| Reaper | Soul Feast: kills restore essence (8 for vanilla hostiles, more for higher ranked creatures, up to 60) | Soul Siphon: drain the creature you look at for 3 s, 2 damage and 6 essence every half second (15 essence) | Hunger: essence only refills while meditating at a Dream Candle; kills are your main source |
| Wellspring | Deep Well: essence refills 3x as fast while meditating, 2x while sneaking and standing still | Essence Gift: give 40 essence to the player you look at, or heal 6 if there is none (heals you when you are alone) (30 essence) | Brimming Vessel: a hit of 3 hearts or more spills a quarter of your essence |

Things to try:

1. Warden: open a Gate (`/shadowslave gate open 2` or a Gate Lure), walk away and
   follow the trail back. Mark a creature, then compare your damage on it and on
   something else.
2. Seer: stand near an Ashen Tyrant or Chainback and watch for the violet edge
   before the slam or chain. Use Foresight while a Gate is still counting down;
   the Category it shows is the one that opens. Note the fog until you cast.
3. Reaper: watch the essence bar stay still while you walk around, then fill it
   with kills and Soul Siphon. Reaping Arc a group of weakened mobs.
4. Wellspring: sneak still to see the bar refill faster; take a big hit and
   watch essence spill. With a second player, gift essence and ward them.

### The utility Natures: Forge, Delve, Mason, Verdant

Added by claude-code, 2026-10-23. These four are for crafting, mining, building
and farming rather than fighting. Quick start: `/shadowslave aspect set forge`
(or delve, mason, verdant), then `/shadowslave core fill` and
`/shadowslave awaken <name>` for the Awakened abilities above.

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Forge | Thrifty Hands: 20% of smelted items come out doubled; Smithing earns 1.5x Forging XP; your billets cool 30% slower | Quench: holding a shaped blade, quench it perfectly on the spot; otherwise smelt up to 8 of the held stack in your hands; with nothing smeltable, a heat burst throws back and ignites hostiles within 4 (25 essence) | Cold Hearth: slow and weak in powder snow or anywhere cold enough to snow |
| Delve | Ore Sense: sneak to see ores within 8 blocks outlined through stone, coloured by ore | Tunnel Strike: break a 3x3 tunnel 3 deep in the direction you face (up or down when you look steeply) (20 essence) | Open Sky: weak under the open sky, and essence refills at half speed there |
| Mason | Long Reach: place and break blocks 2 blocks further away | Raise Wall: a 3x3 wall two blocks ahead from the block in your hand, one per block placed (20 essence) | Far From Home: abilities cost 50% more when you are over 32 blocks from anything you have placed |
| Verdant | Green Thumb: crops and saplings within 6 blocks grow faster | Bloom: crops and saplings within 4 blocks grow two stages, animals within 6 heal 4 (25 essence) | Rootbound: essence only refills while you stand on grass, dirt, farmland, moss or leaves |

Things to try:

1. Forge: hold raw iron and press R, then press R empty-handed next to a few
   zombies. Smelt a stack in a furnace and count the extras. Awaken with
   Masterwork, then fuse a billet one Tier above your Forging level at a Forge
   Hearth (the diamond sword at level 1). Stand in
   a snowy biome and watch the Flaw kick in.
2. Delve: dig into a hillside, sneak, and look for the coloured outlines. Use
   Tunnel Strike looking ahead, then looking straight down. Step outside and
   check the essence bar slows. Vein Pull an iron vein; Stonewalk through a wall.
3. Mason: place blocks from further away than usual. Hold bricks and Raise Wall.
   Build something, walk 40 blocks away and compare ability costs. Blueprint a
   small arch, look elsewhere and press again; Scaffold off a cliff.
4. Verdant: stand in a wheat field and watch it grow; Bloom it. Stand on stone
   and see the essence bar stop. Living Wall in front of a zombie; Wild Call a
   wolf or horse, then fight beside it.

### The Dream Realm Natures: Wayfarer, Lull

Added by claude-code, 2026-10-23. These two lean on the Dream Realm (see below).
Quick start: `/shadowslave aspect set wayfarer` (or lull), then
`/shadowslave core fill` and `/shadowslave awaken <name>` for the Awakened
abilities above.

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Wayfarer | Pathfinder: every couple of seconds a faint gold pull (only you see it) points home, and in the Dream Realm a red one points to the nearest Seed within 512 blocks | Waymark: press to set a mark at your feet (up to 3, seen through walls; yours gold, other Wayfarers' blue); sneak and press to snap back to the newest within 64 blocks (20 essence) | Wanderlust: stay within 24 blocks for over 2 minutes and essence refills slower, down to a quarter after 7 minutes; moving on resets it |
| Lull | Drowsy Aura: every fourth blow a hostile aims at you fizzles as it nods off | Lullaby: the creature you look at (16 blocks) sleeps for 5 s: it stands still, drops its target and cannot hurt anyone; any damage wakes it. Creatures above your rank shrug it off (25 essence) | Insomnia: you cannot sleep in a bed unless a lit Dream Candle burns beside it |

Things to try:

1. Wayfarer: set three Waymarks while walking, go behind a wall and look for
   the pillars. Sneak and press to snap back. Set your bed, walk away and
   follow the gold pull. Stand still for a few minutes and watch the essence
   bar slow. Awaken with Anchor Recall and let a zombie hit you mid-channel;
   Trailblaze and sprint, with a second player running behind you.
2. Lull: Lullaby a zombie and stand next to it; hit it and it wakes. Try it on
   a wither skeleton while Dormant (it should fail). Let a few zombies hit you
   and count the blows that fizzle. Dreamfall a crowd, then Sleepwalker one
   into lava. Try sleeping without a Dream Candle.

### The Awakening-inspired Natures: Gloam, Rime, Fury

Added by claude-code, 2026-10-23. Each one is built on one signature rule that
changes how you play. Quick start: `/shadowslave aspect set gloam` (or rime,
fury), then `/shadowslave core fill` and `/shadowslave awaken <name>` for the
Awakened abilities above.

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Gloam | Deepening Dark: in the dark (light 4 or less, night counts) your melee hits deal 2 more and essence refills 1.5x as fast | Gloam Edge: for 20 s melee hits deal 2 more in bright light, 4 in the dim, 6 in the dark (30 essence) | Sunstruck: no essence refill at all in open daylight (sky above you, daytime, no rain) |
| Rime | Frost Mark: each melee hit leaves a mark (up to 5); every mark slows the creature and bites for half a point a second, and one melts off every 3 s | Ice Lance: the creature you look at (24 blocks) takes 5 damage and 2 Frost Marks (25 essence) | Thin Blood: within about 6 blocks of fire, lava, magma or a lit campfire or furnace your essence stops and your marks melt twice as fast; within 2 blocks you take light burn damage |
| Fury | Rage: a meter (red bar beside the essence bar) fills when you are hit and a little when you hit; it makes you up to a quarter bigger, with 1.5 more reach and 3 more melee damage, and cools 5 s after the fight | Provoke: every hostile within 12 turns on you, you get Resistance I for 6 s and 20 rage (20 essence) | Red Haze: at full rage you cannot eat or open your inventory or any chest until the rage cools to 60 |

Things to try:

1. Gloam: play a night, then a day. Compare essence refill in the open at noon
   (none), under a roof (normal) and in a dark cave (faster). Gloam Edge a
   zombie in a lit room, then in the dark. Shade Step into a dark corner, then
   try a torch-lit spot. Snuff a torch-lit room.
2. Rime: hit a zombie a few times and watch the frost and slowness build;
   Ice Lance something across a field. Stand next to a furnace or campfire and
   watch the essence bar stop and the burn tick in. Encase a skeleton, then
   Rimepath across a lake.
3. Fury: let a few zombies hit you and watch the red bar fill and your
   character grow; Provoke a crowd. Fill it to the top and try to eat or open a
   chest. Ground Slam with a full bar and with an empty one; Second Wind when
   low on health.

### Pocket, weight and time: Threshold, Anchor, Hourglass

Added by claude-code, 2026-10-23. The second batch of Awakening-inspired
Natures. Quick start: `/shadowslave aspect set threshold` (or anchor,
hourglass), then `/shadowslave core fill` and `/shadowslave awaken <name>` for
the Awakened abilities above.

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Threshold | Hollow Pocket: nine slots of space only you can reach; what you keep there stays with you through death | Pocket: open the Hollow Pocket anywhere (5 essence) | Unmoored: every door into elsewhere (each Pocket, each Doorway) takes half a heart of max health, up to three hearts, until you next sleep the night through |
| Anchor | Heft: you choose your weight. Light: higher, floatier jumps and soft landings, a little faster. Heavy: no knockback, you sink in water and walk the bottom, and you mine underwater as fast as on land, but you are slower | Shift Weight: step Normal, Heavy, Light and round again (10 essence) | Leaden: at every weight, boats and horses will not carry you and ladders will not hold you |
| Hourglass | Banked Blows: hits on a creature you hold out of time do nothing yet; when time catches up they all land at once, a quarter harder | Stasis: the creature you look at (16 blocks, no higher in rank) is held out of time for 4 s; it cannot move or strike (30 essence) | Borrowed Time: every second you hold something out of time is paid back after; your essence will not refill for as long |

Things to try:

1. Threshold: put something in your Pocket, die, and check it is still there.
   Open the Pocket a few times and watch your max health drop half a heart
   each time; sleep the night and it comes back. Open a Doorway, walk 20
   blocks, open its twin and walk through both ways (bring a friend or a pet).
   Banish a zombie and watch it come back five seconds later.
2. Anchor: Shift Weight to Heavy and jump in a lake: you should sink and walk
   the bottom, and mine there at full speed. Get hit by a skeleton (no
   knockback). Shift to Light and sprint-jump; jump off a 10 block drop. At
   any weight try a boat, a horse and a ladder. Awakened: Crush from a tower
   into a crowd (Heavy hits harder); Gravity Well a group of zombies.
3. Hourglass: Stasis a zombie and hit it four or five times; nothing happens
   until the hold ends, then it all lands together. Watch the essence bar stay
   still afterwards (Borrowed Time). Rewind after walking into lava or a bad
   fall; Still Moment a crowd.

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

## The Dream Realm and natural Seeds

Added by claude-code, 2026-10-23. First slice: one region, the Ashen Expanse.
Quick start: `/shadowslave aspect set ember`, `/shadowslave core fill`,
`/shadowslave awaken <name>`, then sleep in a bed with a lit Dream Candle
beside it, at any hour (2026-10-31: there is no night-only rule; monsters
nearby still keep you up).

1. **Crossing.** Once you have Awakened, sleeping beside a lit Dream Candle
   takes you to the Dream Realm after about three seconds instead of skipping
   the night. Dormant players still sleep normally. The first time, you
   arrive over your bed at 1:8 scale and a lit Dream Candle appears beside
   you: that is your **Dream Anchor**, and every later crossing arrives there.
   Light another Dream Candle in the Dream Realm to move it.
2. **Waking.** Use a lit Dream Candle in the Dream Realm with an empty hand
   (sneaking still meditates), or sneak-use your clan's Citadel Gateway with an
   empty hand. Either only lets you through after a wait from when you arrived
   (5 minutes; `/gamerule shadowslaveDreamWakeWait <seconds>`, after ch. 398),
   and says how long is left. `/shadowslave dream` shows it too. You wake in
   your bed. Dying there also ends the dream; you respawn as usual. Logging
   out wakes you on the next login.
3. **Your sleeping body.** Your body stays asleep in the bed (Awakened only;
   Ascended cross whole, body and all). Hostile mobs nearby walk over to it. If
   it is hurt you are pulled back awake at once with its wounds. Its chunk stays
   loaded while you dream.
4. **Lost** (2026-10-31, `/gamerule shadowslaveLostDreamers`, on). If your
   sleeping body is killed, you become one of the **Lost** (ch. 360, 435): a
   red title says so and you stay in the Dream Realm. Candles and Gateways will
   not wake you, logging out and in keeps you there, and dying brings you back
   at your Dream Anchor. Conquer your Second Nightmare and rise to Ascended and
   your body is remade ("Found"); from then on you cross whole and wake as
   usual. Rising to Ascended is being built in its own thread; until then an
   operator frees a Lost player with `/shadowslave dream wake <player>`. With
   the rule off, the old rule holds: you die where you stand in the Dream Realm.
5. **Nightmare Seeds.** While an Awakened player walks the Dream Realm, Seeds
   take root 24 to 64 blocks away now and then (the action bar says so). Use
   one to read it: region, Category forecast, growth stage (1 to 4) and time
   to bloom (3 in-game days at normal speed).
6. **Cleansing.** Sneak-use a Seed to challenge it: a Gate opens at the Seed
   in the Dream Realm. Kill its Guardian and the Seed is cleansed and drops
   its region's loot, rolled 3 times at stage 1, 2 times at stages 2 and 3,
   once at stage 4.
6. **Blooming.** A Seed left alone blooms into a Gate in the Overworld at the
   matching spot (Seed position x 8). A lit Dream Candle within 48 blocks of
   that spot (at any height) wards it: the Seed withers instead.

Server options (game rules): `shadowslaveDreamSeeds` (on; off stops new Seeds and freezes the rest), `shadowslaveDreamSeedsOptIn`
(off; when on, players choose with `/shadowslave dream seeds on|off`),
`shadowslaveDreamSeedGrowth` (100 percent; 0 freezes Seeds),
`shadowslaveDreamBlockDamage` (on), `shadowslaveSleepingBody` (on) and
`shadowslaveDreamRatio` (8). Anyone can run `/shadowslave dream status`.
Operators: `/shadowslave dream enter|wake [player]`, `/shadowslave dream seed
plant` (in front of you), `/shadowslave dream seed grow <days>` and
`/shadowslave dream seed bloom` (the nearest Seed).

Regions and Seed tuning are data: `data/shadowslave/dream_realm/region/*.json`
and `data/shadowslave/dream_realm/seeds.json`; cleanse loot is a normal loot
table.

## Nightmares and Seed trials

Added by claude-code, 2026-10-29. First slice: echo sites, the trial framework
and one hand-made Category 2 Nightmare, The Last Vigil, at the Ember Watch echo
site in the Ashen Expanse. Quick start: Awaken (see above), cross into the
Dream Realm, then `/shadowslave trial site shadowslave:ember_watch visit` and
`/shadowslave trial site shadowslave:ember_watch grow`.

1. **Echo sites.** Some Dream Realm places are echoes of a past. Walk near one
   and its ruin rises out of the ash; now and then a Seed grows at it. Reading
   that Seed names the site, its Category (set by the site's weight, so it is
   certain) and a line of lore.
2. **Challenging.** Sneak-use the Seed. The Spell gathers a cohort for 15
   seconds: anyone Awakened within 12 blocks is drawn in with you, party or
   stranger. A Seed that already judged you refuses with a line.
3. **Inside.** You wake at the Ember Watch as it stood, with a role the Spell
   deals (Lamplighter, Watch Slave, Sworn Guard, Ash Pilgrim) and its kit.
   Names above heads are hidden and chat and the tab list show your role.
   Kit items are bound to the Nightmare and stay behind when you leave.
4. **Endings.** The goals are hidden. The Last Vigil has three: light the
   three watch lamps after the second wave, slay the Ash Herald that comes at
   dusk, or simply last until dawn. Each scores differently and a role whose
   fate matches the ending scores more.
5. **Appraisal.** On a clear the Spell lists your deeds and kills, gives a
   grade with its comments (see "The Spell's memory, judgement and voice"
   below), and boons from a loot table per grade. Click once to finish the
   typing and again to wake. You keep what you won inside.
6. **Death.** Dying inside puts you back where you were, exactly as you were
   when you entered: items, experience and effects gained inside are lost,
   and that Seed will never let you in again. Logging out or your sleeping
   body being hurt also reverts you, without the Seed's judgement.
7. **Rank-up.** Ascension now needs a full core and a cleared Category 2
   Nightmare (the rise to Ascended itself is not built yet; the Soul screen
   shows the requirement).

Server options (game rules, all off): `shadowslaveNightmareScar` (a death
inside leaves a Nightmare Scar, see "Long Nightmares" below),
`shadowslaveNightmareTrueDeath` (a death inside costs a rank; today that means
Awakened back to Dormant), `shadowslaveNightmareHardcore` (a death inside drops
you all the way back to Dormant) and `shadowslaveFirstNightmare` (Awakening also needs
your First Nightmare conquered, see "The First Nightmare" below). PvP inside follows the server's PvP setting.
Anyone can run `/shadowslave trial status`. Operators: `/shadowslave trial
sites`, `site <id> visit|grow`, `start <nightmare> [category] [players]`,
`first [player]`, `skip <seconds>`, `end <ending>`, `abort` and `forget <player>`.

Content is data: Nightmares in `data/shadowslave/nightmare_trial/nightmare/`,
echo sites in `data/shadowslave/dream_realm/echo_site/`, tuning (weights to
Category, gather time, kill points, grades) in
`data/shadowslave/nightmare_trial/settings.json`, boons as `gift` loot tables.

### Long Nightmares, the Thornwake Basin and the Nightmare Scar

Run E adds a deeper region, a Category 3 Nightmare that lasts for days, and the
Scar. Quick start: Awaken, cross into the Dream Realm, then
`/shadowslave trial site shadowslave:briar_cloister visit` and `... grow`, or
`/shadowslave trial start shadowslave:long_thorn 3` to skip the walk.

1. **The Thornwake Basin.** About 1000 blocks from the Dream Realm's centre
   the ash gives way to a sunken basin of mud and black briars. Its border
   wobbles a little, so it is not a perfect circle. Its Seeds are mostly
   Category 3 and things of the Fallen rank walk there: Ash Burrowers,
   Chainbacks, Hollow Mimics and now and then a Thorn Matron roam in from the
   dark. A Warding Candle keeps roamers from appearing near it, and
   `/gamerule shadowslaveDreamCreatures false` turns roaming off. The Ashen
   Expanse stays as quiet as it was. Worlds made before this keep the chunks
   they already generated; the basin appears only in new ground.
2. **Black briars.** Walking through them hurts. Axes and swords cut them
   fast, fire burns them, and they drop nothing.
3. **The Thorn Matron.** A Fallen Devil. When she sees you she raises her arms
   and a ring of briars grows around you two blocks high (those wither away
   after about 15 seconds). She takes double damage from fire and her lash
   slows you.
4. **The Briar Cloister and The Long Thorn.** The cloister ruin sits in the
   basin (around x 900, z 640) with its Seed in the cleared heart of the old
   garden. Its Nightmare is The Long Thorn: the cloister in the season the
   briars came. Roles: Cloister Novice, Thornwarden, Brazier Keeper, Penitent
   Captive. Endings: light the three braziers (from day 2), slay the Thorn
   Mother in the garden, or outlast three days.
5. **Long Nightmares.** The Long Thorn is a Long Nightmare. Its days are ten
   minutes each, with a "Day N" title at each dawn, and it fades after five
   days without an ending. Things to try:
   - Log out inside. A figure of you, named with your role, stays where you
     stood. Log back in and you wake in its place, at its health.
   - Have a friend kill your figure while you are away (or let a creature do
     it). When you log in, the Spell tells you that you died in your sleep and
     you are reverted and judged as if you had died.
   - While nobody is awake inside, the Nightmare's clock stops.
   - Let a second group use the same Seed while the first is still inside. They
     join the same run on whatever day it is. Someone who left and comes back
     keeps their old role.
   - Finish it while a cohort-mate is logged out. They are appraised when
     they next log in.
   - Restart the server mid-run. The run, its clock and its sleepers survive.
6. **The Nightmare Scar** (still off by default: `/gamerule
   shadowslaveNightmareScar true`). A death inside a Nightmare leaves a scar:
   minus one heart and 5% damage per scar, up to three, closing after 30
   minutes. Conquering a Nightmare closes one early.

Tuning lives in `data/shadowslave/nightmare_trial/settings.json` (`scar` and
`long_nightmares` blocks), the pack's own `long` block (day length, fade,
dawn line), `data/shadowslave/dream_realm/roaming.json` and each region's
`creatures` list.

### The First Nightmare (server option, off by default)

With `/gamerule shadowslaveFirstNightmare true`, a full core is no longer
enough to Awaken: the Seed the Spell planted in you has to bloom first, and you
face what grows from it alone.

1. **The Seed stirs.** Roll your Aspect and fill your Dormant core. When it
   saturates, the Spell tells you the Seed in you stirs. The Soul Sea (sneak-use
   a lit Dream Candle) now lists "Conquer your First Nightmare" under Awakening.
2. **It blooms.** Sleep in a bed with a lit Dream Candle beside it. A few
   seconds in, you leave your bed and wake inside your First Nightmare, alone,
   as Category 1. Nobody else is drawn in, even if they sleep beside you.
3. **The Chain Road.** The road to the Ember Watch, years before The Last
   Vigil, while slaves were still carrying its stones across the ash. The Spell
   makes you a **Road Slave** (most often) or an **Ash Pilgrim**. Ash Burrowers
   rise from the drifts, a Chainback waits in the pass, and after two and a half
   minutes the Grey Drover (a Dormant Tyrant) walks back up the line looking for
   you. Hidden endings: slay the Drover (a Road Slave's fate), reach the
   unfinished gate of the Watch at the north end from two minutes in (an Ash
   Pilgrim's fate), or live seven minutes.
4. **Conquered.** The Spell appraises you as for any Nightmare, then tells you
   to meditate at your Dream Candle, where you Awaken as before.
5. **Fallen.** A death reverts you to how you went to bed, like any Nightmare.
   No Seed judges you, but the one inside you goes quiet for 3 in-game days
   (`quiet_days` in the settings). Sleeping by the candle before then tells you
   how long is left; after that it blooms again the next time you sleep there.
6. **Breach** (`/gamerule shadowslaveFirstNightmareBreach true`, also off). As
   in the novel, dying in your First Nightmare lets something out: a Dormant
   Ash Burrower appears beside you when you wake.

Operators can start one at once with `/shadowslave trial first [player]`,
whatever the rule or the player's rank. The list of First Nightmares and the
Spell's lines are in the `first_nightmare` block of
`data/shadowslave/nightmare_trial/settings.json`; a pack's `breach` names what
follows a fallen carrier out. Packs can also use a new `reach` ending (stand
within `radius` blocks of a point).

### Nightmare Natures: Masque and Unbroken

Added by claude-code, 2026-10-30. Quick start: `/shadowslave aspect set masque`
(or unbroken), then `/shadowslave core fill` and `/shadowslave awaken <name>`
for the Awakened abilities. Masque's innate only shows inside a Nightmare (see
above to start The Last Vigil).

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Masque | Role-Worn: in a Nightmare your role kit comes with one more of everything, and an ending that rewards your role gives half as much again | Borrowed Face: look at a creature (24 blocks) and wear its face for a minute. Others see you as that creature, and creatures of your rank or lower ignore you until you strike or use another ability (25 essence) | Hollow Reflection: still water or glass within 2 blocks reveals you. You glow, lose the face and take a quarter more damage while revealed |
| Unbroken | Weathered: each second of a fight turns aside 2% more damage, up to 30%. A fight ends 6 s after the last blow | Last Stand: brace (30 essence). The next killing blow leaves you on 1 heart with Resistance II for 3 s, then your will rests for 3 minutes | No Retreat: sprinting away from a hostile within 12 blocks drains 3 essence a second |

Awakened choices: Masque picks **Mirror Swap** (trade places with the creature
or player you look at, 24 blocks; 30 essence) or **Stolen Voice** (throw a
creature's own call to the spot you look at; every creature of that kind
nearby forgets its target and goes there for 8 s; 30 essence). Unbroken picks
**Grit** (plant your feet for 10 s: each hit you take where you stand hardens
you 10% more against the next, up to 50%, and stepping away ends it; 30
essence) or **Defiance** (every hostile within 16 blocks must target you and
cannot turn away for 8 s, and you get Resistance I; 30 essence).

Things to try:

1. Masque: wear a zombie's face in front of a friend and check they see a
   zombie walking. Walk past zombies and skeletons (they ignore you), then hit
   one (they all turn). Step next to a pond or a glass pane while disguised.
   Mirror Swap with a skeleton across a gap, or with a friend. Stolen Voice
   at night beside a few husks and watch them walk to the spot. Inside The
   Last Vigil, count your kit and compare your appraisal with a non-Masque.
2. Unbroken: let a zombie chip at you and watch damage taken fall over a
   long fight. Brace Last Stand, let a group of zombies or a long fall kill you, and
   check you are left on 1 heart and the ability says it is mending. Sprint
   away from a creeper and watch essence drain; walk away and it doesn't.
   Grit in a crowd, then step off. Defiance with a friend nearby: the mobs
   chasing them should turn to you.

## The Spell's memory, judgement and voice

Added by claude-code, 2026-10-29 (Run A of the lore doc's run backlog). No AI
anywhere: the Spell's words come from written line tables, behind a voice
interface a later optional module can sit in front of.

1. **The Recorder.** The world keeps a log of notable events: Nightmare
   Creature kills (by rank and class; Tyrants and named creatures count as
   unique), Nightmares conquered, failed and left, Citadels claimed and lost,
   deaths, rank-ups and server firsts. Doing something nobody has done before
   (first Fallen Demon slain, first Category 2 Nightmare, first Glorious, first
   Citadel, first to Awaken) gets a gold line from the Spell.
   `/shadowslave spell record` shows the Spell's record of you and
   `/shadowslave spell firsts` the server's firsts.
2. **The Judge.** Appraisals grade Poor, Adequate, Good, Excellent and
   Glorious. A death inside is judged Failed. Since 2026-10-31 the grade
   measures how far your run strayed from the Nightmare's **fated ending**,
   which the Spell never tells you (ch. 744): reaching the fated ending is worth
   nothing by itself, changing it is worth a lot, and more when a lowly role (a
   slave) changed it. Your role's part in the ending, going alone or in a small
   cohort, cohort members lost and striking your own still count. Kills are
   listed but do not count. Before the grade the Spell may comment: "Your
   achievements are remarkable!" on Excellent, and "You have achieved the
   impossible!" on the best Glorious runs (ch. 15, 743).
3. **Commentary.** Under the grade the Spell says how you played (treachery,
   protecting others, recklessness, fighting above your rank, mercy, going
   alone, or steadfast) and what it remembers of you ("Again, you flee.",
   "Better than before.").
4. **Callings.** Now and then a band of the Spell's starry thread-web opens at
   the top of the screen and a line types itself out, with a soft chime: when
   your core is nearly full or saturated, when a Seed of the Category you need
   next grows within 48 blocks, the first time you enter a Dream Realm region,
   when an unclaimed Citadel stands within 96 blocks, and gently after 90
   minutes of play without progress. Each moment is called once.
5. **Callings page.** The Field Guide has a fourth tab, Callings, with every
   Calling you have heard, newest first. Its button sets how often they come
   (Off, Rare, Normal, Often); the same option is in the mod's config screen.

Operators: `/shadowslave spell log [count]`, `record <player>`,
`call <calling> [player]` (core_nearly_full, seed_next_category, new_region,
unclaimed_citadel, idle), `say <topic> <key>` to preview a line, and `voices`.

Content is data: lines in `data/shadowslave/spell_voice/<topic>.json` (keys
per grade, behaviour, memory and Calling; variants can require facts with a
`when` map), Callings tuning in `data/shadowslave/spell/settings.json`, and
the grade ladder and scoring in `data/shadowslave/nightmare_trial/settings.json`.

## True Names

Added by claude-code, 2026-10-30 (Run B of the lore doc's run backlog). A
True Name is a rare honorary name the Spell grants (ch. 9). Everything is
decided by game code; the voice only picks the form a name takes.

1. **Earning one.** Only a conquered Nightmare judged Glorious can grant a
   name: a 35% chance, or always when the Spell also said "You have achieved
   the impossible!". The name is built from
   word lists for how you played (treachery, protecting others, recklessness,
   fighting above your rank, mercy, going alone, steadfast). Two deeds outside
   Nightmares also grant one: being the first under the Spell to slay a Great
   (or worse) creature, and a 25% chance when you slay a Tyrant above your own
   rank with no other player within 32 blocks. No two players share a name.
   A name from an appraisal shows on the appraisal screen; a name for a deed
   opens its own reveal screen (once you are out of any Nightmare).
2. **Name page.** The Soul screen has a seventh tab, Name (key 7): your name,
   whether it shows, what it was granted for and what it does, and the names
   you know. Press H there to show or hide your name.
3. **Anchored Soul.** Wither damage, essence drains and the shard torn from
   your core by a PvP kill take half as much from you.
4. **True-Name Binding.** Sneak and use a Weaving Table with a Memory in hand
   (or `/shadowslave truename bind`) to bind it with your name for 40 essence.
   It counts as Bound and answers only to you, with no Bound pattern needed.
5. **Closer to the Spell.** Your Callings can come twice as often, reach 1.5x
   as far for Seeds and Citadels, and some lines address you by name.
6. **Secrecy.** Your name is hidden by default. Showing it puts it after your
   name in chat and the tab list, and everyone who reads it learns it
   (including players who join while it shows). Hiding it again does not make
   anyone forget. Anyone who knows your name deals 10% more damage to you, and
   once a day can speak it to sense where you are: a direction and distance
   (or "far away" in another world) with a line of soul fire, while you feel
   your name spoken as a Calling. Speak a known name from the Name page by
   clicking it, or with `/shadowslave truename sense <name>`.
7. **Passing names on.** `/shadowslave truename reveal <player>` speaks yours
   to a player within 8 blocks. Holding paper, `/shadowslave truename write`
   writes yours (or `write <name>` one you know) onto a Field Note; whoever
   uses the note learns the name, and its tooltip never shows it.

Operators: `/shadowslave truename grant <player> [deed]`, `forget <player>`
and `names`. Content is data: `data/shadowslave/true_name/settings.json`
(chances, drain resistance, damage edge, sense cooldown, Calling boost,
binding cost) and `true_name/deed/<id>.json` (trigger and word lists); the
name forms and the Spell's lines are in `spell_voice/true_name.json`.

Not yet: holding a Citadel against a raid alone (raids are Run C), Corruption
resistance, and Aspects that give power over a named bearer.

## Smithing (forge, hammer, quench)

Gear and Memories are now made by hand at a smithy. Anyone can smith,
Awakened or not. Vanilla crafting of iron and diamond gear still works; a
smithed piece is graded and can come out better.

1. Build the smithy (all three are in the Functional Blocks creative tab):
   - **Forge Hearth**: three bricks on top, brick, furnace, brick in the
     middle, three iron ingots on the bottom.
   - **Smithing Anvil**: an anvil and a smithing table, shapeless.
   - **Quench Trough**: planks in a U with a water bucket in the middle.
   - **Smith's Hammer**: three iron ingots on top, iron ingot and stick in the
     middle, a stick below.
2. Open the hearth. Lay a pattern's materials in the four crucible slots, put
   coal (or any fuel) in the fuel slot and press **Fuse**. The billet appears
   in the slot above the fuel and starts to heat. The bar on the right shows
   the fire and the billet's heat; the boxed band is the pattern's working
   heat. Sneak and use the hearth to work the bellows, which drives the fire
   up to white heat. Leave a billet too hot for too long and it burns.
3. Take the billet out and use it on the **Smithing Anvil** to lay it there.
   With the Smith's Hammer in hand, left-click the anvil. A bar shows a
   sweeping marker: strike when it crosses the gold mark. Clean hits count
   full, rough hits half, misses nothing. Each blow cools the billet; once it
   drops below the band ("Too cold") put it back in the hearth.
   Leather and hide patterns are **cold work**: no fire needed.
4. When every blow has landed, blades want a **quench**: hold use on the
   Quench Trough with the shaped billet and let go at the hiss (the bar's
   mark). Too early scores less, far too late and the blade cracks. Armour
   finishes straight off the anvil.
5. The finished piece gets a grade from its heat, hammer and quench: Crude,
   Fair, Fine, Superior or Masterwork. The grade scales its damage, armour
   and durability (85% to 130%) and sets how many threads it will hold for
   Weaving later. The tooltip says "Forged by" you.
6. Starter patterns: iron sword, pickaxe, axe, helmet, chestplate, leggings,
   boots, the Shard Knife, Sleeper's Hood and Wanderer's Boots. The diamond
   sword needs Forging level 2. Nightmare materials still unlock the
   discovered patterns (Linked Cleaver, Hollow Edge, Hollow Visage, Ashen
   Hide Vest) by trial in the crucible, and Chainback Mail on your first
   Chainback kill.
7. Smithing gives Forging XP (five times as much the first time you make a
   pattern). Levels widen the hammer's clean mark. The Forge Nature's
   **Masterwork** lets the next billet reach one Tier higher with a wider
   mark and one grade up; Forge's innate cools billets 30% slower.

Memories woven before Smithing are graded Fair on first use and keep their
"Woven by" smith.

Patterns are data files under `data/shadowslave/smithing/pattern/`, with
their heat band, blow count and quench mark, so a data pack can change them.
Operators can use `/shadowslave_weaving xp shadowslave:forging <amount>`,
`/shadowslave_weaving learn <pattern>` and `/shadowslave_weaving reset`.

## Weaving (strands)

Added by claude-code, 2026-10-28. Weaving now enchants finished gear the way
Sunny weaves: you study a weave to learn its pattern, then trace it in
essence onto a soul-shard anchor (DESIGN, after ch. 2048 and 3024).

1. **The table.** Lay a piece in the **Piece** slot, a soul shard in the
   **Anchor** slot and the strand's material in the four small slots. The
   right side shows your Weaving branch (cycle with the arrows), what the
   piece holds or teaches, and the strands you know.
2. **Study.** Anything with a weave in it teaches: a Nightmare material
   (a Chainback Link teaches Dragging Links and Rattling Guard), a woven Memory, gear that
   already holds strands, or an enchanted item or book (Sharpness teaches
   Whetted Edge, Protection teaches Warding, Efficiency teaches Quick Hands,
   Feather Falling teaches Light Fall). Hold **Study** on its row for three
   seconds. It costs 10 essence; with too little, spare shards in the anchor
   slot burn instead.
3. **Trace.** Pick a strand you know. Its pattern shows as numbered points
   around the anchor. Hold the mouse and drag through them in order; the ring
   shows how close you need to be. A clean trace lays the strand at full
   strength, a loose one at its score, and a wild one fizzles (the essence is
   spent, the anchor and materials are kept). Laying a strand uses the anchor
   shard, the materials and the essence on the cost line.
4. **Threads and Tiers.** Vanilla gear holds one strand; forged gear holds
   one to four by grade. Tier is capped by your soul (unranked and Dormant I,
   Awakened II, Ascended III), your Patterns level and the anchor shard's
   rank. Higher Tiers need a steadier hand.
5. **Tuning** (Patterns 2). Tie a strand to a condition (only at night, only
   when below half health, only out of sight of the sky) to make it half again as strong while
   the condition holds. Plain attribute strands can't be tuned.
6. **Forgery: unravel and lay.** Press **Unravel** on a strand the piece holds
   to pull it out into a **Woven Thread** in the output slot (15 essence; it
   keeps 75% of its potency, all of it at Forgery 2). Put the thread in a
   material slot with a new piece in the centre and press **Lay the thread**.
7. **The strands:** Splinter Edge (tools and weapons sometimes keep their
   durability), Quiet Dreams (helmet, harder to see), Long Road (boots, less
   sprint hunger), Cinder Skin (chest or legs, less fire damage), Dragging Links
   (weapon, pulls struck foes closer), Rattling Guard (chest, softens a heavy
   hit then rests), Borrowed Face (helmet, weak enemies hesitate), Echoing Cut
   (weapon, a second magic hit, Awakened only and studied from a Hollow Edge),
   Whetted Edge (+attack damage), Warding (+armour), Quick Hands (+mining
   speed) and Light Fall (boots, less fall damage).
8. Weaving strands gives Patterns XP; unravelling gives Forgery XP. Patterns 3
   is the deed "Lay five Tier II strands". Gear tooltips list each strand with
   its Tier, potency, tuning and who wove it.

Strands are data files under `data/shadowslave/weaving/strand/`. Operators
can use `/shadowslave_weaving study <strand>` to learn one directly.

## Memories and Bound

Added by claude-code, 2026-10-29. Weaving can turn a piece into a Memory you
carry in your soul (DESIGN, after ch. 3024: a Memory is stored in the soul,
rebuilt from essence, and mends itself).

1. **The Memory Weave.** Every Weaver knows it from the start. Trace it like
   any strand (25 essence, an ender pearl, a soul-shard anchor). It takes one
   thread, so a vanilla piece that becomes a Memory has no room for anything
   else; forged pieces have more.
2. **Dismiss and summon.** Hold the Memory and press **G** (the Memory key):
   it dissolves into your soul. Press **G** with it gone to summon it back into
   your hand (5 essence; an unranked soul pays nothing). Sneak + **G** turns
   which dismissed Memory comes next. A soul holds 3 Memories, 5 when
   Awakened, two more each Rank after. With no woven Memory involved, **G**
   still summons or dismisses the Ash Compass.
3. **Mending.** A dismissed Memory mends 2% of its durability every two
   seconds, for 1 essence each time (free for an unranked soul).
4. **Death.** Memories you carry sink into your soul instead of dropping, so
   you can summon them after you respawn. Memories can still be handed to
   another player, who can then dismiss them into their own soul.
5. **Bound** (Awakened soul). Study it from an Awakened Soul Shard. It needs a
   piece that already holds the Memory Weave, takes another thread and an
   Awakened Soul Shard. A Bound piece bonds to the first player who holds or
   wears it, so a Weaver can bind a piece for a buyer. Anyone else who picks
   it up can carry it, but it slips out of their hands or armour slots. Its
   tooltip says whose it is.
6. **Undoing.** At the table, **Unbind** cuts a bond (an Awakened Soul Shard
   or better in the anchor slot, used up), and **Undo** takes the Memory Weave
   back out once the piece is no longer Bound. Neither comes loose as a
   thread.

### Nature edges

- **Wellspring** pays 75% of Weaving's essence costs (studying, weaving,
  unravelling, laying threads, summoning).
- **Seer** studies weaves for no essence, and looking at a creature, a player
  or a dropped item shows the strands in what it holds and wears above the
  hotbar.
- **Ember** can heat a billet in the hand: use it in the air, 2 essence per
  use, 120° each time, up to 1200°.
- **Tide** quenches in any water: use a shaped, hot blade on a water source
  for a perfect quench, no trough needed.

## Weaving depth: your own strands, level 7, the Soul Sea

Added by claude-code, 2026-10-29 (Run G).

1. **Fusing a strand of your own** (Patterns 5). At the Weaving Table, press
   **Fuse** beside "Strands you know", click two strands that suit the same
   kind of piece, type a name (or keep the suggested one, such as "Dragging
   Cut") and press **Fuse** (40 essence). The new strand shows in your list
   with a ✦ and works like any other: trace it, choose its Tier, weave it. It
   takes one thread and does both parts at 70% each (85% at Patterns 7). It
   costs both parts' essence and materials and needs the higher Rank. You can
   keep 12 of your own. Anyone who studies a piece holding it learns it too.
   The tooltip shows "fused: A + B · 70% each".
2. **Seven levels.** Forging, Patterns and the new **Binding** branch (Memory
   weave and Bound XP, plus unbinding) each go to level 7. Deeds sit at levels
   3, 5 and 7 and the other levels cost points. The Soul screen's Weaving tab
   lists what each held level gives and what the next one would.
   - Forging: 3 five Tier II pieces, 5 three Superior pieces, 7 a Masterwork.
     Forging 7 makes every piece you forge hold one more thread.
   - Patterns: 5 three tuned strands (unlocks fusing), 7 three Tier II strands
     of your own (85% share).
   - Binding: more Memories held in the soul, faster mending while dismissed,
     cheaper summoning. Its deeds are three Memory weaves (3), holding five
     Memories at once (5) and three Bound pieces (7).
3. **The Soul Sea.** The Soul screen has a **Memories** tab (key 6). Your
   dismissed Memories orbit there with their durability. Hover one to read it
   and click it to summon that one into your hand.
4. **Memories don't drop.** Tossing a Memory (Q) sends it back into your soul,
   or into your inventory if the soul is full. To hand one to another player,
   hold it, sneak and use it on them. A piece Bound to someone else can't be
   handed over.
5. Two Tier 4 smithing patterns (netherite sword and chestplate) start known,
   heat 1250–1450°.

## Field Guide and map

Added by claude-code, 2026-10-23. First slice: creature pages, the map, places,
danger ratings and the Dream Realm map. Paths, Field Notes and the public
board are the second slice, below.

1. **Getting one.** Every player gets a Field Guide the first time they join.
   A lost guide can be crafted again from a book and a compass. What the
   guide knows belongs to you, not the book, so a new copy reads the same.
2. **Creature pages.** Hold a spyglass on a Nightmare Creature for about a
   second to log a sighting (one per kind every 30 seconds). The first
   sighting starts its page with its Rank and Class; more sightings and kills
   reveal its habits, weakness and drops. The page says how to open the next
   entry. Kills count too, and a Seer gets two sightings per look.
3. **Map.** Use the guide and open the **Map** tab. It shows only the ground
   you have walked near (two chunks around you); the rest is fog. Drag to pan,
   scroll or press +/- to zoom, and o to recentre. Toggle layers along the
   bottom: Pins, Gates & Seeds, Danger, Creatures (where you last met each
   one) and Seed threats.
4. **Places and pins.** Coming within 48 blocks of a Gate logs it on the map;
   it greys out once closed. Right-click the map to pin a spot with a name
   (Pin) or a red warning (Warn); right-click your own pin to remove it.
5. **Danger ratings.** Areas 64 blocks across are rated on the creature ranks
   (Dormant to Unholy) from the strongest creature you have seen or slain
   there; a death to a creature raises it one step. Hover the map to read it.
   In the Dream Realm every area starts at its region's rank.
6. **Dream Realm page.** The **Dream Realm** tab maps the Dream Realm the same
   way. Coming within 24 blocks of a Seed logs it, and its pin says where its
   Gate would open in the waking world (the 1:8 match). Those spots also show
   on the waking-world map as dashed red **Seed threats**, so you can see
   which Seeds threaten your base. Bloomed or cleansed Seeds drop off.
7. **Hold to peek.** With the guide in your off hand, a small corner map shows
   the ground around you with your pins.

Tuning is data: `data/shadowslave/field_guide/rules.json` (ranges, cooldowns,
explore radius, danger cell size, starting item on/off) and one page per
creature in `data/shadowslave/field_guide/creature/*.json`. A Dream Realm
region's baseline danger is its `danger` field.

### Paths, Field Notes and the public board

Added by claude-code, 2026-10-31. Second slice of the Field Guide.

1. **Trace.** On the **Map** tab, click **Trace: off** (top left of the map)
   to turn it on. While a Field Guide is anywhere in your inventory, the
   guide marks the ground you walk, ride or swim through as a faint dotted
   trail (4-block cells). Flying does not count. Toggle the **Paths** layer
   to hide it; the corner map shows paths too.
2. **Established paths.** Each stretch counts how often it has been walked
   (walking the same stretch again counts after a minute). Once a stretch has
   been walked 8 times it becomes an **established path**, drawn as a bright
   amber road, for everyone who has that stretch in their guide. On a server
   those 8 walks add up across everyone; one player alone can also wear a
   road. The first time a stretch you walk turns into a road the guide says so.
3. **Copying a page.** Each copy costs 1 Paper and 1 Ink Sac (free in
   creative) and gives you a **Field Note**.
   - Creatures tab: **Copy page** copies the selected creature page with its
     counts, so a fuller page teaches more. The note's tooltip says how full
     it is ("3 of 4 entries").
   - Map tabs: click **Copy area**, then click the middle of the area (the
     dashed square, 6 chunks each way). Choose **Chart** (the map there, with
     its Gates, Seeds and Citadels), **Paths**, **Danger** (danger marks and
     warnings), or **Place** if you clicked on a pin.
4. **Reading a note.** Use the note. Your guide learns whatever on it you did
   not already know, and the note stays, so you can pass it on or sell it.
   Charts uncover ground you have never visited.
5. **Field Guide Board.** Craft one (3 planks, planks + paper + planks, two
   sticks) and place it. Use a Field Note on it to pin it up (up to 12).
   Use the board with anything else to read every note on it: chat lists
   each note and who pinned it, and your guide learns them all. Sneak-use to
   take down the newest note you pinned.
6. **Lost notes.** Chests in dungeons, mineshafts, temples, shipwrecks,
   strongholds, mansions, outposts, ruined portals, igloos, ancient cities,
   cartographer houses and Dream Realm Seed caches can hold a note "copied by
   a Sleeper who did not make it": a part-filled creature page, or a chart of
   the ground around the chest with a warning pinned and a death marked.

Server options (game rules):

| Rule | Default | What it does |
|---|---|---|
| `shadowslaveSharedPaths` | true | Established paths count everyone's walks; off, each player wears their own roads |
| `shadowslavePublicBoards` | true | Field Guide Boards can be posted to and read |
| `shadowslaveLostNotes` | true | Lost notes turn up in chests |
| `shadowslaveFieldNotesWearOut` | false | Harsh: a note crumbles once read, so each copy teaches one reader |

Tuning is data: `paths` and `notes` in `data/shadowslave/field_guide/rules.json`
(cell size, walks to establish, one player's share, copy cost, chart size,
board size, lost-note odds, warnings and last lines). Which chests hold lost
notes is `data/shadowslave/loot_modifiers/lost_field_notes.json`, and how often
is `data/shadowslave/loot_table/field_guide/lost_note.json`.

## Citadels and clans

Added by claude-code, 2026-10-29. First slice: one kind of Citadel in the
Ashen Expanse. Quick start: get into the Dream Realm (see above), then
`/locate structure shadowslave:citadel`, or `/shadowslave citadel build` to
raise one in front of you (operators).

1. **Finding one.** Citadels are walled keeps that generate in the Dream
   Realm on flat enough ground, roughly one per 32 x 32 chunks. Coming within
   96 blocks logs one in your Field Guide as a keep-shaped pin.
2. **The Guardian.** An unclaimed Citadel is held by a Guardian, an Ashen
   Tyrant at the region's rank (Dormant in the Ashen Expanse). It stays inside
   the walls and never despawns. Its Gateway, on the dais under the arch, is
   **sealed** (red); using it tells you who holds it.
3. **Conquering.** Kill the Guardian: "Citadel Conquered" shows and the
   Gateway opens (violet). For the first 5 minutes only the slayer (or their
   clan) may claim it; after that anyone can.
4. **Claiming.** Use the open Gateway. If you have no clan you become a clan
   of one ("<name>'s clan"). The Gateway turns pale blue and becomes your
   Dream Anchor, so the next time you dream you arrive beside it. Lit Dream
   Candles on the dais wake you as usual.
5. **Clans.** `/clan` shows your clan, members and Citadels. `/clan create
   <name>`, `/clan rename <name>`, `/clan invite <player>`, `/clan join
   <name>` (needs an invite), `/clan leave`, `/clan kick <player>`,
   `/clan list`. Only the leader invites, kicks and renames; if the leader
   leaves, the longest-standing member leads. If the last member leaves, the
   clan ends and its Citadels fall open (claimable by anyone). If a clan of
   one joins another clan, its Citadels and board come along.
6. **Anchoring.** Any member can use a claimed Gateway to anchor to it. Leave
   the clan (or lose the Citadel) and the Gateway lets your anchor go: your
   next crossing lights a fresh Dream Anchor over your bed.
7. **Field Guide board.** When a member comes within 24 blocks of their
   clan's Citadel (or uses its Gateway), their Field Guide and the clan's
   board share what they know: creature pages, explored map, danger marks
   and logged Gates, Seeds, warnings and Citadels. Your own named places stay
   yours. The chat line says what you learned.

PvP sieges and the Sovereign Domain are not built yet.

Tuning is data: `data/shadowslave/citadel/settings.json` (Guardian, guard
radius, claim window, visit and log range, clan name length, Citadel names),
`data/shadowslave/worldgen/structure/citadel.json` (size, slope, block
palette) and `worldgen/structure_set/citadels.json` (spacing). Operators:
`/shadowslave citadel status|build|list|conquer`.

### Citadel life: upkeep, upgrades and raids

Added by claude-code, 2026-10-30 (Run C). Quick start in the Dream Realm:
`/shadowslave citadel build`, `/shadowslave citadel conquer`, use the
Gateway to claim it, then use it again.

1. **The Gateway screen.** A member who uses their clan's Gateway (with an
   empty hand) opens it over the Spell's web: the provisions meter, any raid,
   how many are anchored, the Gateway Links, and the six upgrades with what
   the next tier costs (green: you carry it; red: you don't).
2. **Upkeep.** The Gateway eats 40 provisions a day plus 20 per clan member.
   A fresh claim holds 3 days; it holds up to 14. Feed it by holding soul
   shards (10/40/160/640 by rank) or materials (gems 8, ingots 4, coal 2,
   logs 1) and using the Gateway: it eats only what it has room for.
3. **Neglect.** At no provisions the Gateway is **Hungry** (the wards drop),
   after 2 days **Starving** (the workshops go cold), after 5 days
   **Forsaken** (the Guardian's echo no longer fights raiders). The Citadel
   is never lost; one meal sets it back to Fed. The clan hears each change.
   Upgrades can only be built while it is Fed.
4. **Upgrades** (three tiers each, paid from your inventory; creative builds free):
   - *Wards*: no hostile mobs spawn within 24/36/48 blocks; a stranger
     inside rings a bell and tells the clan; at III they glow.
   - *Workshops*: raises a Forge Hearth, Smithing Anvil, Quench Trough and
     Weaving Table in the courtyard; forging on clan land scores +4/8/12,
     Weaving there costs 90/80/70% essence.
   - *Hall*: 4 members can anchor before it's built, then 8/16/32. A full
     Hall turns new anchors away.
   - *Archive*: members share with the board from anywhere in the Dream
     Realm every 10/3/1 minutes; at III even in the waking world.
   - *Lanterns*: lantern posts on a ring 18/26/34 blocks out, and the board
     charts 3/5/8 chunks around the Citadel.
   - *Gateway Links*: travel to your clan's other Citadels from the screen
     for 60/40/20 essence a trip.
5. **Raids.** A Nightmare Seed that blooms within 96 blocks of a claimed
   Citadel opens its Gate there, in the Dream Realm, at the region's
   Category, instead of in the waking world. Every member online gets a
   Calling and a chat line with the coordinates. The waves march on the
   Gateway; the Guardian's echo strikes raiders within 16 blocks every 3
   seconds while the Gateway isn't Forsaken.
   - *Held*: slay the Gate's Guardian. Each member who came within 64 blocks
     gets 2 soul shards per Category (the Citadel's rank), the Spell's record
     counts it (your next appraisal recalls it), and a lone defender has a
     30% chance at a True Name ("held a Citadel alone").
   - *Lost*: raiders stand at the Gateway for 60 raider-seconds, or nobody
     answers for 20 minutes. Two built upgrades are damaged (they work one
     tier lower) and a quarter of the provisions spoil. Repair them on the
     screen for half the tier's cost. The Citadel stays yours.

Tuning is data: `data/shadowslave/citadel/upkeep.json`, `raid.json` and
`upgrade/<id>.json`. Operators, at a claimed Citadel:
`/shadowslave citadel feed <days>`, `starve <days>`,
`upgrade <id> <tier>`, `raid <category>`, `raid lose`.

### Clan wars

Added by claude-code, 2026-10-31 (Run I, design locked with Jack). Wars are
fought between clans that hold Citadels. Quick start: two clans each claim a
Citadel, then the leader of one stands at their own Gateway.

1. **Seats.** A clan's seat is its first claimed Citadel (the Gateway screen
   heading says "Seat of ..."). The leader can move it with `/clan seat` at
   another of the clan's Gateways, only in peace and once every 7 days.
2. **Declaring.** `/clan war declare <clan>` at your own Gateway, as leader,
   costs 8 Awakened Soul Shards. The Spell calls both clans (a War Calling
   and the particulars in chat). Nothing can be struck for 24 hours.
3. **The war** has no time limit. `/clan war` shows how it stands; the
   Gateway screen shows it in a strip at the foot. Either side may:
   - *Skirmish*: fight anywhere (the server's own PvP setting applies).
   - *Raid the outskirts*: stand on an enemy Citadel's land (48 blocks from
     its Gateway) with none of them there. After 90 raider-seconds one built
     upgrade is damaged; the Citadel is spared further raids for 12 hours.
     A defender on the land drives the raid off.
   - *Cut the supply line*: Gateway Links raise a **Link Anchor** 28 blocks
     north of the Gateway, outside the walls. Stand within 3 blocks of an
     enemy anchor for 20 seconds with none of them within 16, and it is cut:
     its Gateway can't be fed, eats twice as fast, and its Links are dark. A
     member mends it by using the anchor (8 Dormant Soul Shards, 2 Ender Pearls).
   - *Assault the seat*: the leader announces it with
     `/clan war assault <hours> <clan>`, at least 12 hours and at most 7 days
     ahead. Both clans are called. In the hour that follows, attackers stand
     within 6 blocks of the seat's Gateway to channel it (each one adds a
     second; 120 needed). Defenders there alone break it down; both there
     holds it. Nobody comes, and it is taken uncontested. If the hour runs
     out, that clan waits 72 hours to announce another.
4. **Abandoned and Forsaken seats.** If no member of a clan at war logs in
   for 24 hours the Spell warns the enemy; at 72 hours the seat is
   **Abandoned**. An Abandoned seat, or one whose Gateway is **Forsaken**
   (the last stage of neglect), can be channelled at any time, no notice.
5. **The end.** Taking the seat ends the war: the winners keep the Citadel
   with everything built, the losers keep their other Citadels (the first
   becomes their seat, and they may move it at once) and a 7-day truce holds.

Every number is in `data/shadowslave/citadel/war.json`, including
`"enabled"` (sieges are on by default) and `"war_hours"`, which swaps
announced assaults for fixed daily hours (UTC) when seats may be channelled.
Operators: `/shadowslave citadel war skip <hours>` moves the war clock ahead,
`war tick` runs one second of war, `war cut` cuts the nearest claimed
Citadel's anchor, `war end <clan>` ends a clan's wars.

### War Natures: Saboteur and Standard

Added by claude-code, 2026-10-31. Quick start: `/shadowslave aspect set saboteur`
(or standard), then `/shadowslave core fill` and `/shadowslave awaken <name>`
for the Awakened choices. Both work anywhere; their war parts need two clans
at war (see Clan wars above for the operator commands that skip the notice).

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Saboteur | Line Sense: other clans' Link Anchors and Gateways within 48 blocks show to you alone as sparks (flames for an enemy's), with a line saying where | Undermine: a charge on the block you look at (6 blocks) goes after 10 s. In the wild it cracks the blocks around it (not obsidian or harder). At an enemy's Link Anchor it cuts the supply line; on an enemy Citadel's land it damages one upgrade (sharing the raid cooldown). A member of that clan within 4 blocks stamps it out; out of war the Spell holds the ground (30 essence) | Marked: after you cut an enemy's anchor or damage their upgrades you glow for 30 s |
| Standard | Under the Colours: your clan, Echoes and tamed creatures within 8 blocks of you take a tenth less damage | Plant the Standard: a purple banner for 2 minutes that wards everyone of yours near it, you included. Within 12 blocks of a Gateway being channelled it adds a second a second to your clan's channel, or takes one away from the enemy's. Anyone can strike it down (30 essence) | Oathbound: while your side fights near your banner, standing more than 12 blocks from it gives you Weakness |

Awakened choices: Saboteur picks **Cut the Lines** (always on: your strikes on
a Link Anchor count double and your charges burn in 5 s) or **False Trail**
(for a minute your steps add nothing to the shared established paths, a false
trail of footprints runs 24 blocks where you look and becomes an established
path, and creatures hunting you follow it for 8 s; 30 essence). Standard picks
**Hold the Line** (always on: no one of yours near your banner is knocked back)
or **Last Rally** (always on: when your banner falls or is furled, everyone of
yours near it heals four hearts and gains Absorption).

Things to try:

1. Saboteur: set a charge on a stone wall and watch it burn and crack. Walk
   near another clan's Citadel in the Dream Realm and look for the sparks over
   its Link Anchor. At war, undermine their anchor, with and without one of
   them standing on it, and check the Glowing. Awakened: time a strike with
   Cut the Lines, or lay a False Trail with a zombie chasing you.
2. Standard: let a tamed wolf take hits next to you and away from you. Plant
   the Standard and walk off while your wolf fights near it (Oathbound). At an
   assault, plant it by the Gateway on either side and watch the channel
   count. Awakened: get hit by a skeleton near the banner (Hold the Line), or
   break your own banner after a fight (Last Rally).

### Citadel Natures: Herald and Keeper

Added by claude-code, 2026-10-29. Quick start: `/shadowslave aspect set
herald` (or keeper), then `/shadowslave core fill` and `/shadowslave awaken
<name>` for the Awakened abilities above. "Allies" are other players and
your own pets, as for the other Natures. "Clan land" is within 48 blocks of
the Gateway of a Citadel your clan holds (`land_radius` in the Citadel
settings).

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Herald | Command Presence: allies within 8 blocks of you deal 15% more damage | War Banner: plant a red banner at your feet for 15 s; you and your allies within 8 blocks of it get Speed I and Strength I. Breaking it only furls it early, and it never drops (35 essence) | Burden of Command: when an ally within 16 blocks dies, you get Weakness I and Slowness I for 30 s |
| Keeper | Hearthhold: on clan land your abilities cost half (rounded up) | Sanctuary: a 4-block dome for 6 s; you and your allies inside take half harm, but nothing sheltering in it can hurt anything outside (35 essence) | Homebound: off clan land your essence pool holds a third less (67 of 100 Dormant, 133 of 200 Awakened) |

Things to try:

1. Herald: tame two wolves and fight a zombie with them, with and without the
   Herald nearby. Plant a War Banner and watch the effect icons on you and the
   wolves. Let a wolf die beside you. Awakened: Rally Call with a friend 100
   blocks away (they sneak to come; someone who doesn't sneak stays put), and
   with your pets sitting or roaming. Swear Oath of Arms to a friend and have
   a zombie hit them.
2. Keeper: check your essence bar in the waking world (a third short), then
   claim a Citadel and stand in its courtyard (full pool, half-price
   Sanctuary). Raise Sanctuary in a fight and try hitting a zombie outside it.
   Awakened: Bulwark near skeletons and watch the arrows turn back. Light a
   Dream Candle at home, Gateway Step to your Citadel, then wake with an empty
   hand on a lit Candle to return.

### The Spell's Natures: Fortune and Cantor

Added by claude-code, 2026-10-30. Quick start: `/shadowslave aspect set
fortune` (or cantor), then `/shadowslave core fill` and `/shadowslave awaken
<name>` for the Awakened abilities above.

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Fortune | Favoured: +1 Luck (better chest and fishing loot), each stack a creature you kill drops has a 20% chance of one more, and a cleared Nightmare has a 25% chance to grant one more boon | Coin of Fate: for 10 s your next hit lands for double damage (heads) or half (tails), even odds (20 essence) | Ill Omen: three lucky breaks within 2 minutes (a heads, a Twist Fate miss, a bonus drop or boon, a Borrowed Luck payout) bring a small mishap: a stumble, a slipping grip, or spilled essence |
| Cantor | Attuned: the Spell's Callings can come twice as often as your frequency setting allows, and each adds a second line | Command Word: speak the name of the Nightmare Creature you look at (20 blocks) and it cannot move or act for 3 s. Only works on kinds whose Field Guide page you have completed (30 essence) | Spoken Self: each ability you use shows where you stood on every other player's Field Guide map for 10 s, labelled with your name |

Things to try:

1. Fortune: flip the Coin of Fate and hit a zombie a few times to see heads
   and tails. Kill a handful of cows and compare drops with a friend who is
   not Fortune. Awakened: raise Twist Fate near a skeleton, and mark a
   creature with Borrowed Luck before killing it. Keep getting lucky and wait
   for the Ill Omen mishap.
2. Cantor: with Callings on Often, notice the extra line on each Calling. Try
   Command Word on a Chainback before and after completing its Field Guide
   page (spyglass sightings and kills). Awakened: Silence an Ashen Tyrant
   before its slam, and call a poisoned friend's name. With a friend online,
   use any ability and have them open their Field Guide map.

## Echoes

Added by claude-code, 2026-10-30. Quick start (operators):
`/shadowslave_echoes remnant zombie` gives an echo remnant, `/shadowslave_echoes
bound zombie` gives a finished Echo, `/shadowslave_echoes clear` empties your soul.

1. **Remnants.** Any creature you (or your Echo) kill can, rarely, leave an
   echo remnant: a glowing item with soul flames and the line "An echo of the X
   lingers where it fell." The chance grows with the creature's rank. Bosses,
   villagers and a few others never leave one.
2. **Binding.** Put the remnant in the Weaving Table's centre slot and a soul
   shard of the creature's rank or better in the anchor slot. The table shows
   the Echo, the Binding level and essence it needs; click **Bind the Echo**.
   Dormant needs Binding 1, Awakened 3, Fallen 5, Corrupted 7.
3. **Taking it in.** Use the bound Echo to take it into your soul. Slots by
   rank: Sleeper 0, Awakened 1, Master 2, Saint 3. The Soul screen's Echoes tab
   (key 8) lists what you hold with each Echo's health.
4. **Calling.** Press **H** to call every Echo out of your soul (essence per
   Echo, more for higher ranks) or send them home. While out they drain
   essence every second; run dry and they all return. Resting Echoes heal.
5. **Orders.** Hold **J** for the command wheel: Follow (up), Guard (right),
   Stay (down), Attack (left, whatever you look at). Point and let go, or
   click. The centre calls or dismisses. Check that Echoes never hit you or
   each other, that zombies and skeletons don't burn in daylight, and that
   your Echo's kills drop shards and loot for you.
6. **Moves, no growth.** An Echo keeps its creature's attacks and gear (a
   skeleton keeps its bow) and never levels.
7. **Death.** An Echo that dies is gone for good ("X Echo is destroyed. It
   will not return."). If you die, your summoned Echoes return to the soul.
8. **Trading.** Shift-click an Echo on the Echoes tab to draw it out as an
   item, then sneak and use another player while holding it to hand it over.
   It won't lie on the ground: tossing it keeps it in your pack.

Tuning is data: `data/shadowslave/echo/settings.json` (slots, drop chance,
Binding level, essence and upkeep by rank), `echo/creature/*.json` per
creature overrides, and the entity tag `shadowslave:echo/no_remnant`.

### Echo Natures: Shepherd and Vessel

Added by claude-code, 2026-10-30. Quick start: `/shadowslave aspect set
shepherd` (or vessel), then `/shadowslave_echoes bound zombie`, use it to take
it in, and press **H** to call it. `/shadowslave core fill` and
`/shadowslave awaken <name>` give the Awakened abilities. Both Natures can hold
one Echo even while Dormant, since a Sleeper's soul otherwise holds none.

| Nature | Innate (always on) | Ability (R) | Flaw |
| --- | --- | --- | --- |
| Shepherd | Herdbond: your Echoes cost half the upkeep, and your soul holds one Echo while Dormant | Rally: every summoned Echo and tamed pet of yours within 32 blocks gets Speed II for 8 s and heals 30% of its health (25 essence) | Lonely Soul: with no Echo summoned, your melee hits deal 20% less |
| Vessel | Borrowed Trait: while an Echo is out you carry a small trait of its kind (fire-proof kinds: Fire Resistance; swimmers: Water Breathing; fliers: Slow Falling; undead: Night Vision; quick kinds: Speed; other monsters: Strength; anything else: Haste) | Echo Merge: your Echo (within 16 blocks) folds into you for 10 s. You gain its attack, armour and some of its speed, and its trait rises a level (40 essence) | Hollowed: while merged you cannot use your own abilities. Also lets your soul hold one Echo while Dormant |

Awakened choices: Shepherd picks **Pack Tactics** (your Echoes jump to flank
the creature you look at, 24 blocks, and get Strength for 8 s; 35 essence) or
**Second Bond** (always on: one Echo slot more than your rank gives). Vessel
picks **Swap Places** (trade spots with your Echo up to 64 blocks away; 25
essence) or **Shared Wounds** (for 20 s your Echo, within 32 blocks, takes two
fifths of every hit you take; 30 essence).

Things to try:

1. Shepherd: watch the upkeep shown on the Echoes tab (key 8) next to a
   non-Shepherd friend's. Hurt your Echo and a tamed wolf, then Rally. Punch a
   pig with no Echo out and again with one out. Awakened: Pack Tactics on a
   husk with two Echoes (Second Bond) or one, and check Second Bond lets you
   take in one more Echo than your rank allows.
2. Vessel: call a blaze, a zombie, a parrot and a cow Echo in turn and check
   the effect icon changes. Merge with a zombie Echo: it vanishes, your armour
   bar fills, and your ability key says Hollowed until the merge ends. Dismiss
   the Echo mid-merge and check the merge ends cleanly. Awakened: Swap Places
   with an Echo across a ravine, or take hits from a zombie with Shared Wounds
   up and watch your Echo's health drop instead.
