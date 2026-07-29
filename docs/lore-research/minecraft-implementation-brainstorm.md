# Minecraft implementation brainstorm — after Sections A–F

> **Status:** design brainstorm, not an approved implementation plan.
>
> **Scope:** this document is intentionally opinionated. The project owner explicitly overrode the original `lore only` restriction for this branch after Sections A–F were researched.
>
> **Rule:** whenever this document says `canon`, the claim should be traceable to the research files. Everything else is a Minecraft design proposal.

---

# 1. What the research changes about the project

The Phase 1 datapack is a good **technical prototype**, but the canon research shows that its current state model compresses several distinct systems into one.

The biggest mismatches are architectural rather than cosmetic:

1. **First Nightmare completion currently produces an Awakened.** Canon produces a Dormant **Dreamer/Sleeper** first; actual Awakening comes after the first successful Dream Realm journey.
2. **`ss_rank` currently represents too many concepts at once.** Canon distinguishes Spell/progression status, Soul Rank, Aspect Rank and — for exceptional beings — Class/core count.
3. **The current four Aspects and four Flaws are hard-coded tags.** Canon demands unique Aspects and highly personal Flaws.
4. **The current trial has one global shape: countdown → one creature → kill.** Canon Nightmares are historical scenarios whose invariant is a **central conflict**, not a boss.
5. **The current `Vitality` and `Endurance` readout uses Minecraft attributes under canon terminology that means something else.** Canon Attributes are named supernatural traits.
6. **The existing roadmap calls Memories generic `soulbound tiered gear`.** Ordinary Memories are soul-stored but transferable; true Bound/Soulbound artifacts are rare.
7. **The roadmap says `absorb Soul Cores to rank up`.** Ordinary humans absorb Soul Shards/essence to strengthen a core; Rank advancement is a separate qualitative transformation.
8. **The roadmap describes Echoes as binding defeated shadows.** Ordinary Echoes are soulless replicas; Sunny's Shadows are a separate Aspect-specific system.
9. **`Divine Sight` is not a universal player-inspection system.** Information reading is power-specific.
10. **The current Nightmare dimension is aesthetically uniform.** The Dream Realm is not one dark biome; every region has its own laws and imagery.

None of those require throwing Phase 1 away. They tell us where its prototype abstractions need to stop becoming permanent architecture.

---

# 2. Live code audit — what should survive and what should not

## Keep these ideas

### One entry choke point

`nightmare/enter.mcfunction` correctly centralizes entry guards instead of scattering them across callers.

That pattern should survive the Java port:

```text
request entry
  ↓
NightmareService.tryEnter(player, source)
  ↓
all validation in one place
  ↓
allocate / join instance
```

Do not regress to `bed code has one set of rules, command code has another`.

### One teardown path

`nightmare/leave.mcfunction` is also the right architectural instinct: successful completion, ejection, death, disconnect recovery and admin reset should converge on one teardown service.

The Java equivalent should be something like:

```text
NightmareService.exit(instance, player, ExitReason)
```

with `ExitReason` controlling consequences/presentation but **not** duplicating cleanup.

### Explicit comments around known compromises

The `ponytail:` convention is genuinely useful. Keep an equivalent in Java/docs, perhaps:

```java
// PROTOTYPE-LIMIT: ...
// CANON-DEVIATION: ...
// PERF: ...
```

A Shadow Slave project has enough intentional deviations that undocumented shortcuts will eventually be mistaken for lore.

### Static validator + behavioral tests

The repository already treats tests as part of the feature instead of optional cleanup. Keep that philosophy.

The pure-data part of the future mod should still have an offline schema/reference validator, while in-game behavior moves to GameTests and targeted integration tests.

## Stop extending these prototype abstractions

### `ss_rank` as one integer

Current:

```text
0 = Sleeper
1 = Awakened
```

This is already too small for canon, and fresh players do not even have a score, which has forced repeated `absent score` workarounds.

Do not extend this to:

```text
0 sleeper, 1 awakened, 2 master, 3 saint...
```

That would permanently mix title/stage with Soul Rank and make natural Awakening, Aspirants and exceptional soul states awkward.

### One tag per Aspect/Flaw

Current upkeep is effectively:

```text
if tag shadow -> run shadow
if tag flame -> run flame
...
```

That scales linearly with the catalogue and cannot represent truly generated instances.

The future system should store **data describing an Aspect instance**, not encode identity through one-of-N tags.

### `awaken/roll.mcfunction`

This function currently does three unrelated jobs:

- progression transition;
- Aspect generation;
- Flaw generation.

Those need to become independent services.

### Global Nightmare creature and bossbar ownership

The code already documents this as single-player-at-a-time. Do not invest heavily in per-player scoreboard/tag tricks to fix it. Java instance ownership should replace it cleanly.

---

# 3. Recommended platform direction

## Recommendation: NeoForge 1.21.1 for the Java core

**This is a recommendation, not a claim that Fabric cannot do the job.** Both ecosystems can support a large mod.

NeoForge fits this project especially cleanly on the already-selected **Minecraft 1.21.1** target because the APIs we need are available without inventing our own persistence/GUI framework:

- **Data Attachments** can persist arbitrary data on players/entities/chunks.
- vanilla/NeoForge **Data Components** can store structured persistent data on ItemStacks.
- **Menus + Screens** give server-authoritative interactive GUIs.
- custom **network payloads** can selectively synchronize private Soul information.
- **SavedData** can hold global Gate, Citadel, Nightmare-instance and faction state.
- **GameTests** can replace the Mineflayer harness for mechanics that require a modded client/server environment.

References:
- https://docs.neoforged.net/docs/1.21.1/datastorage/attachments/
- https://docs.neoforged.net/docs/1.21.1/items/datacomponents/
- https://docs.neoforged.net/docs/1.21.1/gui/menus/
- https://docs.neoforged.net/docs/1.21.1/gui/screens/
- https://docs.neoforged.net/docs/1.21.1/networking/
- https://docs.neoforged.net/docs/1.21.1/datastorage/saveddata/
- https://docs.neoforged.net/docs/1.21.1/misc/gametest/

## Do not throw away the datapack resources

A Java mod can still ship:

- dimensions;
- biomes;
- structures;
- loot tables;
- predicates;
- tags;
- advancements;
- recipes;
- JSON-driven content.

So the port should be:

```text
Java = state, behavior, generation, networking, GUI, custom entities
JSON/data = content definitions, worldgen, templates, balance/configuration
```

not:

```text
Java rewrite every JSON file by hand
```

---

# 4. Core data model — fix this before building Phase 2

The most important architectural decision is to make the soul a first-class object.

## Player `SoulData`

Suggested conceptual structure:

```text
SoulData
├─ schemaVersion
├─ spellState
├─ soulRank
├─ coreState
├─ aspect
├─ flaw
├─ attributes[]
├─ trueName?
├─ memories[]
├─ echoes[]
├─ dreamAnchor?
├─ corruption
├─ behaviorProfile
└─ progressionHistory
```

Persist this as a player Data Attachment.

### `spellState`

Do not infer progression from Soul Rank alone.

Possible states:

```text
UNTOUCHED
CARRIER
ASPIRANT
DREAMER       # human term Sleeper; after First Nightmare
AWAKENED
ASCENDED
TRANSCENDENT
SUPREME
SACRED
DIVINE
```

`ASPIRANT` is a progression/trial state, not a Soul Rank.

### `soulRank`

```text
DORMANT
AWAKENED
ASCENDED
TRANSCENDENT
SUPREME
SACRED
DIVINE
```

A Dreamer has Soul Rank Dormant.

An Aspirant may already possess a Dormant core in Spell terms while still not being a Dreamer, which is exactly why the two fields should be separate.

### `CoreState`

```text
CoreState
├─ type                # SOUL, SHADOW, special variants
├─ count               # normally 1
├─ saturation
├─ saturationMax
├─ essence
├─ essenceMax
└─ modifiers / special rules
```

`count` must **not** automatically increase for normal players.

### `AspectInstance`

```text
AspectInstance
├─ instanceId
├─ name
├─ rank
├─ archetype
├─ sourceElement / nature tags
├─ generationSeed
├─ innateAbilities[]
├─ abilitiesByRank[]
├─ evolutionHistory[]
├─ legacyState?
└─ visualMotif
```

The generated Aspect itself is an **instance**, not a registry enum.

### `FlawInstance`

```text
FlawInstance
├─ instanceId
├─ formalName
├─ description
├─ family
├─ parameters
├─ severityProfile
├─ discoveryState
└─ causalEvidence[]     # what player behavior / Aspect traits influenced generation
```

The final field is useful for debugging and possibly for an optional `why did I get this?` admin tool. The player-facing Spell does not need to expose the algorithm.

### `AttributeInstance`

```text
AttributeInstance
├─ id / generated id
├─ name
├─ description
├─ source              # INNATE, NIGHTMARE_ROLE, LINEAGE, EVENT...
├─ passiveEffects[]
└─ evolutionState
```

Do not expose Minecraft armor/max-health numbers as `Attributes`.

---

# 5. Migration from the current datapack

Do **not** silently invalidate existing saves.

## One-time importer

On first Java-mod login:

1. detect old scoreboard/tag state;
2. create `SoulData`;
3. copy current progression;
4. map current Aspect/Flaw placeholders;
5. set a `legacyPrototype` marker;
6. store migration version;
7. optionally remove old tags/objectives only after migration succeeds.

## Existing Awakened players

Do not demote a live old-world player to Sleeper just because the old prototype skipped a step.

Recommended migration:

```text
old ss_rank >= 1
→ spellState = AWAKENED
→ soulRank = AWAKENED
→ current prototype Aspect/Flaw imported as legacy placeholders
```

New players then use the corrected path.

## Existing placeholder Aspects/Flaws

Do not forcibly randomize them away.

Give migrated players/admins an explicit option later:

```text
Keep Legacy Prototype
Regenerate using new system
```

with a warning that regeneration changes gameplay permanently.

## Rename the prototype `Shadow Slave` Flaw

Even before the full generator exists, the placeholder sunlight Flaw should stop being called **Shadow Slave**, because that is the formal name of Sunny's Divine Aspect.

A temporary invented name such as `Sunscorched`, `Dayblight`, etc. would be less misleading. The exact replacement is design, not canon.

---

# 6. Corrected progression loop

## Phase 1 ending

Current:

```text
untouched
→ Carrier
→ First Nightmare
→ Awakened
```

Recommended:

```text
untouched
→ Carrier
→ Aspirant / First Nightmare
→ Dreamer / Sleeper (Dormant)
```

At First Nightmare completion the player should receive/discover:

- Aspect;
- Dormant Aspect Ability;
- Flaw;
- Attributes generated/inherited during the trial;
- possible True Name in exceptional cases;
- Spell appraisal;
- Dormant Soul state.

They should **not** yet gain conscious essence control or the Awakened Ability.

## First Dream Realm journey

Add a distinct post-First-Nightmare phase:

```text
Sleeper
→ waits for / reaches winter-solstice event
→ forcibly enters actual Dream Realm
→ survives/explores
→ finds a Gateway
→ returns to waking world
→ AWAKENING
```

That event is not a numbered Nightmare.

It can become one of the strongest pieces of the mod because it naturally turns the Dream Realm into a persistent survival/exploration world rather than another boss arena.

## Awakened sleep behavior

After Awakening, sleeping should normally move the player's consciousness into their **Dream Anchor / Gateway region**, not simply behave like vanilla sleep forever.

Later ranks change physical travel rules.

---

# 7. Infection and the Carrier stage

The current `first normal sleep = infection` rule is a good onboarding trigger but not literal canon.

Several possible implementations:

## Option A — keep first-sleep infection

Pros:
- predictable;
- easy to understand;
- no player misses the mod content.

Cons:
- infection feels like a bed recipe rather than a world event.

## Option B — random eligibility after a world-age / advancement threshold

For example, once a player has survived a few Minecraft days or reached a configurable progression milestone, each sleep has a small chance to become the infection event.

Pros:
- closer to `the Spell notices you`;
- different players become Carriers at different times.

Cons:
- random waiting can be frustrating.

## Option C — server-configurable trigger

Recommended long-term:

```text
infection_mode = first_sleep | random_sleep | advancement | command
```

The default can remain first sleep until the rest of the loop is mature.

Once infected, fatigue/calling should belong to **Carrier**, not `Sleeper` terminology.

---

# 8. First Nightmare generation

The core goal should be **generate a small story**, not `generate a boss room`.

## `NightmareScenario`

Conceptual model:

```text
NightmareScenario
├─ scenarioId
├─ historicalSetting
├─ mapTemplate / worldgen profile
├─ roles[]
├─ factions[]
├─ importantActors[]
├─ centralConflict
├─ initialState
├─ resolutionPaths[]
├─ expectedHistory
├─ notableEvents[]
└─ appraisalRules
```

### Role matching

The Spell does not choose roles randomly.

Minecraft cannot know a player's real personality, but it can build an in-game behavioral profile over time.

Potential signals:

- combat frequency;
- aggression versus avoidance;
- melee/ranged preference;
- risk-taking;
- damage accepted to protect another player/entity;
- healing/support behavior;
- exploration distance;
- building/creation activity;
- resource hoarding versus gifting;
- trading/social interactions;
- time spent underground/darkness;
- killing passive entities;
- retreating from dangerous fights;
- rescuing villagers/players;
- repeated oath/quest choices once custom systems exist.

Use these as **weights**, not deterministic moral labels.

### First Nightmare role

A scenario can contain multiple possible historical roles:

```text
prisoner
scout
heir
servant
soldier
pilgrim
outcast
apprentice
warden
traitor
healer
refugee
```

Each role has affinity tags. Match the player profile to a plausible role, then let the player's actions diverge from that role's expected history.

### Central conflict

Examples of generated conflict shapes:

- escape before a city falls;
- choose which faction gains an artifact;
- protect or assassinate a historical figure;
- break a curse/ritual;
- reach and activate a sanctuary;
- free or imprison an entity;
- survive a siege while deciding whether to betray the defenders;
- expose a hidden monster among allies;
- retrieve something at the cost of another objective;
- destroy the structure keeping a catastrophe contained.

Combat can be present in all of these without being the universal win condition.

### Creature count

Do not encode `First Nightmare = exactly one Nightmare Creature` as a rule.

Sunny's own First-Nightmare appraisal includes both the Mountain King and a Larva. The useful constraint is **personal scale**, not exact mob count.

---

# 9. Fate and appraisal

One of the most distinctive systems we can build is an **expected history**.

## Baseline history

Each Nightmare scenario stores a lightweight canonical sequence:

```text
Event A normally happens
Actor B normally dies
Faction C normally wins
Artifact D normally remains sealed
Role E normally betrays Role F
```

Players can reproduce or diverge from it.

## Fate-deviation score

Track meaningful differences, not arbitrary speedrunning points:

```text
saved someone fated to die
killed someone fated to survive
changed faction victory
resolved conflict using another route
uncovered concealed truth
destroyed/preserved key artifact
fulfilled/broke role-defining oath
```

Use that as **one major input** to appraisal.

Do not publish an exact `+12 fate XP` scoreboard unless debug mode is enabled. The Spell should feel mysterious.

## Appraisal narrator

Generate a mythic summary from event tokens.

Instead of:

```text
Killed 28 zombies
Took 34 damage
Time: 06:15
Grade: S
```

build something like:

```text
[A prisoner entered a city that had already begun to die...]
[The prisoner broke the oath meant to save them, and saved those who had condemned them instead.]
[Final appraisal: ...]
```

No runtime LLM is necessary. A deterministic template grammar can produce surprisingly strong results if the scenario emits meaningful event tags.

The Spell can occasionally add dry/cutting commentary based on contradictions in the player's actions.

---

# 10. Procedural Aspects — generate a supernatural nature, not an effect name

The old `theme × expression` idea is a good prototype, but `96 names from 12 behaviors` will eventually feel procedural in the bad sense.

A stronger generator should create a **coherent power grammar**.

## Generation inputs

Possible inputs:

```text
latent player profile
First Nightmare role
choices made
central conflict resolution
fate deviation
rewards / supernatural contact during trial
Flaw relationship
appraisal quality
```

No input should have a simple universal mapping like `used bow → bow Aspect`.

## Aspect nature layers

Generate several dimensions:

### Archetype

Broad metaphysical identity:

```text
hunter
keeper
pilgrim
oracle
devourer
vessel
warden
weaver
herald
mirror
beast
storm
grave
song
hunger
road
crown
```

These are invented design vocab, not a canon list.

### Source / motif

```text
shadow
light
ash
frost
blood
stone
tide
memory
dream
sound
gravity
reflection
bone
storm
silence
```

### Expression families

What the Aspect actually does:

- movement;
- enhancement;
- perception;
- creation;
- transformation;
- control;
- healing;
- destruction;
- summoning;
- spatial manipulation;
- information/revelation;
- resource conversion;
- environmental influence;
- social/command effects.

An Aspect can span several families as it grows.

## Generate the whole growth skeleton up front

When the Aspect is born, generate a hidden progression skeleton rather than inventing unrelated abilities each rank.

```text
Dormant expression
Awakened deepening
Ascended extension
Transformation concept
Supreme expression / Domain affinity
higher-rank potential hooks
```

Only reveal what the player has actually unlocked.

That keeps later abilities feeling like deeper manifestations of the same nature.

## Aspect Rank

Treat Aspect Rank as **potential/complexity budget**, not direct starting damage.

A Divine Aspect does not need to one-shot everything as a Sleeper. It can instead support:

- unusually deep growth;
- exceptional structural rules;
- Innate Ability;
- multiple cores in the rarest cases;
- unusually broad later expressions;
- stronger relationship with Flaw.

## Uniqueness

Store a generated Aspect signature:

```text
archetype + motif + expression graph + conditions + innate rules
```

The world can reject exact duplicate signatures.

Formal names may still share words; canon establishes unique Aspects, not a proven worldwide ban on reusing individual English words.

## Naming

Do not force every generated name into `Element + Noun`.

Allow several invented grammar families:

```text
[Ashen Pilgrim]
[Unbroken Tide]
[Keeper of Empty Roads]
[Hunger Beneath Stars]
[Last Witness]
[Glassbound]
```

The examples are design-only.

The generator should describe **identity/nature**, not simply output `[Fire Punch]` because the first ability burns enemies.

---

# 11. Aspect Ability engine

Do not write one Java class for every generated Aspect.

Build reusable **ability primitives** that JSON/data can compose.

## Suggested layers

```text
AbilityDefinition
├─ activation type
├─ target model
├─ conditions[]
├─ costs[]
├─ effects[]
├─ scaling[]
├─ visuals/sounds
└─ upgrade hooks
```

### Activation types

- passive;
- active keybind;
- toggle;
- held/channelled;
- reaction to damage;
- reaction to kill;
- contextual interaction;
- transformation.

### Targets

- self;
- looked-at entity;
- ray/line;
- block position;
- area;
- owned summon;
- marked entity;
- environmental region.

### Costs

- Soul Essence;
- health/pain;
- hunger;
- cooldown;
- stored charge;
- conditional resource;
- Flaw-specific consequence.

### Effect primitives

- attribute modifier;
- damage/heal;
- teleport/movement;
- mark/tag;
- reveal information;
- spawn construct/summon;
- block/environment change;
- projectile;
- aura;
- resource transfer;
- status application;
- temporary invulnerability/resistance;
- copy/reflect;
- transformation.

Java implements the primitives; data assembles them.

That lets 30–50 well-designed primitives create hundreds of genuinely different combinations without hundreds of hand-coded Aspect classes.

---

# 12. Flaws — build rules, not potion debuffs

The research shows Flaws occupying very different mechanical families.

## Flaw families worth supporting

### Activation price

Power use causes pain/damage/other cost.

### Conditional lock

An ability cannot be used unless a condition is met.

### Resource counterweight

One key resource regenerates poorly or drains abnormally.

### Physical curse

Touch, body growth, hunger, fragility, etc.

### Perception / social curse

Attention, truth, memory, visibility, communication.

### Behavioral compulsion / prohibition

Promises, killing, impulsiveness, retreat, etc.

### Existential maintenance

The character must continually satisfy a condition to remain functional/alive.

### Long-horizon fate/life curse

A condition whose cost shapes a campaign rather than every combat encounter.

## Event-driven Flaw hooks

Java makes these much more feasible than status effects.

Conceptual hooks:

```text
beforeAbilityUse
beforeDamageEntity
beforeKill
beforeInteract
beforeTrade
beforeConsume
beforeTeleport
afterDamageTaken
onEssenceRegen
onItemTouch
onSleep
onPromise / custom oath
onVisibilityQuery
onInformationQuery
```

Not every Flaw needs every hook.

## Generation logic

Generate Flaw from a combination of:

```text
player identity/behavior profile
First Nightmare choices
Aspect nature
what player values/protects in scenario
fate/appraisal context
```

Use randomness **inside a constrained relevant set**, not a global lottery.

## Severity

Cassie's judgement suggests a severe Flaw can imply extraordinary Aspect potential, but canon gives no formula.

Use this as a soft generation sanity check:

- high-potential Aspect can tolerate generation of broader/more severe Flaw candidates;
- low-rank Aspect should not routinely receive campaign-ending curses;
- never display `your Flaw is worth 57 balance points`.

## The owner knows; other players generally do not

The Soul Screen can reveal the owner's formal Flaw description after discovery.

Multiplayer should keep it private unless:

- player voluntarily shares it;
- another power reveals/deduces it;
- server/admin debug is enabled.

---

# 13. Named Attributes

Replace the current `Vitality / Endurance` canon labels.

## Separate internal stats from Spell Attributes

Minecraft still needs ordinary numbers:

```text
max health
armor
speed
attack
knockback resistance
```

Those can remain implementation details.

The Soul Screen should list things like:

```text
Attributes:
  [Generated Attribute Name]
  [Generated Lineage Trait]
  [Nightmare Role Inheritance]
```

Each Attribute can modify internal stats or unlock special behavior, but the name belongs to the supernatural trait rather than the number.

## Attribute sources

Support:

- innate;
- Nightmare-role inheritance;
- lineage;
- Aspect/Legacy reward;
- rare world event;
- evolved Attribute.

This creates a second axis of identity beyond Aspect.

---

# 14. Soul Screen / runes UI

Phase 2 is the right point to require a client mod.

## Suggested screen structure

```text
┌───────────────────────────────┐
│           [RUNES]             │
│ Name / True Name              │
│ Rank                           │
│ Soul Core(s)                   │
│                               │
│ Aspect                         │
│   Rank                         │
│   Abilities                    │
│   Innate Ability               │
│                               │
│ Flaw                           │
│ Attributes                     │
│                               │
│ [Memories] [Echoes] [History] │
└───────────────────────────────┘
```

Use black/dark negative space, silver text/runes and subtle star/string motifs for the **Spell interface**.

Do not apply that silver aesthetic to every Aspect.

## Keep `/trigger soul` / command fallback

Even after GUI exists, preserve a debug/accessibility command such as:

```text
/soul
```

or the current trigger wrapper.

It is useful for:

- servers;
- tests;
- accessibility;
- bug reports;
- admins;
- broken client GUI recovery.

---

# 15. Memories — data model and summon/dismiss

## Do not store dismissed Memories as ordinary inventory ItemStacks

If the Memory is in the Soul Sea, model it as a record in `SoulData`.

```text
MemoryInstance
├─ instanceId
├─ name
├─ rank
├─ tier
├─ type
├─ enchantments[]
├─ sourceCreature?
├─ sourceEvent?
├─ growthState?
├─ boundState
├─ damageState
└─ ownerUuid
```

When summoned, create a manifested ItemStack carrying a **Memory Data Component** pointing to that instance.

When dismissed, remove the manifested stack/entity and return it to soul storage.

This avoids duplication and makes the soul inventory canonical by construction.

## Rank and Tier stay separate

```text
Memory Rank = effect/essence potency
Memory Tier = weave complexity budget
```

Do not reduce both to `rarity`.

## Enchantment budget

Assign internal structural costs:

```text
minor passive       cost 1
conditional boost   cost 1–2
complex active      cost 2–4
major spatial       cost 4+
```

The exact numbers are balance design, but this lets a Tier I Memory occasionally have multiple cheap effects while a Tier IV artifact might devote most of itself to one impossible enchantment.

## Generated Memory rewards

Nightmare Creatures already have:

```text
rank
class
theme
abilities
history/scenario context
```

A Memory generator can transform selected source traits into enchantment candidates.

Example pipeline:

```text
Corrupted Tyrant with freezing aura + minions
→ Memory Rank: Transcendent
→ Tier: V
→ select one source motif: freezing aura
→ generate compatible item type
→ generate 1–N enchantments within Tier budget
→ generate name/description from creature + scenario history
```

Do not guarantee a Memory after every kill.

## Transfer

Implement deliberate transfer rather than dropped-item pickup.

Possible interaction:

1. open player interaction / Soul Arsenal;
2. choose `Offer Memory`;
3. target nearby player;
4. recipient accepts;
5. server atomically moves ownership record.

Physical proximity/contact satisfies the canon flavor without making transfer annoying.

## Bound Memories

`Bound` should be a distinct state:

```text
NONE
SOULBOUND
ASPECT_BOUND / custom
```

A truly Bound artifact may stop existing as a normal transferable Memory and become an Attribute/intrinsic item relationship.

## Growth

Do not give every Memory an XP bar.

Only Memories generated/authored with a growth rule receive one:

```text
kill counter
protect owner
consume a substance
fulfil oaths
survive specific damage
absorb source element
```

Growth can:

- raise Tier;
- add enchantment;
- transform name/form;
- unlock Bound state.

---

# 16. Dream Realm

Do **not** repurpose the existing uniform Nightmare dimension into the permanent Dream Realm.

Create a separate:

```text
shadowslave:dream_realm
```

## One realm, many regions

The research says the unifying aesthetic is **impossible history/scale**, not one color palette.

A region definition can contain:

```text
terrain generator
biome set
sky/ambient rules
regional hazard
ancient civilization palette
signature structures
creature families
Citadel candidates
world-law gimmick
```

Examples of invented region grammars:

- endless salt basin under broken moons;
- forest growing upside-down beneath floating stone;
- black-glass mountain chain around a luminous sea;
- drowned city where gravity shifts with tides;
- bone desert inside the skeleton of a giant being.

These are design examples, not canon locations.

## Persistent, not reset every trip

The Dream Realm should be a real shared dimension where:

- ruins remain looted;
- Citadels can become hubs;
- player structures persist;
- clans can occupy territory;
- Seeds grow;
- Gates relate back to waking-world crises.

Nightmares are **instances/reconstructions**; the Dream Realm is the persistent world.

---

# 17. Nightmare instance architecture

Dynamic dimensions per player are unnecessary and awkward.

Use a dedicated Nightmare dimension with isolated **instances**.

## `NightmareInstanceManager`

Global SavedData:

```text
NightmareInstance
├─ uuid
├─ nightmareNumber
├─ participants[]
├─ allocatedRegion
├─ scenarioSeed
├─ scenarioState
├─ rolesByPlayer
├─ returnLocations
├─ startTime
├─ fateHistory
└─ exitState
```

Allocate far-separated regions/chunk rectangles in one dimension.

Benefits:

- one registered dimension;
- multiplayer cohorts can share an instance;
- separate First Nightmares do not interfere;
- chunk tickets can keep active instances loaded;
- teardown can safely delete/reset allocated chunks later;
- bossbars/entities become owned by instance UUID instead of global tags.

## Instance lifecycle

```text
ALLOCATING
GENERATING
ACTIVE
RESOLVED
COLLAPSING
CLEANUP_PENDING
```

A server crash/restart can resume from persisted state instead of interpreting `entity absent` as victory.

---

# 18. First Dream Realm journey and Anchors

After the First Nightmare, each Sleeper needs a **real survival journey**.

## Winter-solstice scheduling

Minecraft does not need to wait a real calendar year.

Use configurable world-time semantics:

```text
winter_solstice_interval_days
minimum_sleeper_days
```

or a server event that sweeps eligible Sleepers together.

## Initial placement

When taken into the Dream Realm, do not spawn the Sleeper directly in a safe hub.

Place them in a region based on:

- local threat budget;
- player/cohort size;
- distance from known Gateway;
- region topology;
- possibly Aspect/Spell fate.

The goal is **find a Gateway and survive**, not complete a numbered Nightmare quest.

## Dream Anchor

After successful return/Awakening, record:

```text
dreamAnchor = Citadel/Gateway id
```

Sleeping as an Awakened returns consciousness to that anchor area.

Changing anchors can become a meaningful world action.

---

# 19. Soul Essence and core progression

## Keep four quantities separate

```text
Soul Rank
Core saturation
Current essence
Maximum essence
```

For exceptional Aspects:

```text
Core count / Class
```

is a fifth dimension.

## Soul Shards

A shard can carry:

```text
sourceRank
essenceValue
sourceSignature?
```

Absorbing one increases **core saturation/capacity**, not Soul Rank.

## Essence

Use essence for:

- advanced Aspect Abilities;
- Memory enchantments;
- Echo manifestation/maintenance where appropriate;
- bodily reinforcement at Awakened+;
- sorcery.

Dormant Sleepers can still use Spell-mediated systems such as Memories despite lacking normal conscious essence control; do not gate every magical interaction behind a generic mana bar merely because essence exists.

## Rank transitions

Spell route:

```text
First Nightmare      → Dreamer/Sleeper
Gateway return       → Awakened
Second Nightmare     → Master
Third Nightmare      → Saint
Fourth Nightmare     → Sovereign
Fifth Nightmare      → Spirit
Sixth Nightmare      → God
```

Natural route should eventually be a separate cultivation system rather than calling the same Nightmare functions without the Spell.

---

# 20. Nightmare Creatures

Build creatures from **Rank + Class + archetype**, not one entity class per combination.

## `CreatureProfile`

```text
CreatureProfile
├─ rank
├─ class
├─ archetype
├─ baseStats
├─ abilities[]
├─ intelligenceModel
├─ minionProfile?
├─ environmentalInfluence?
├─ visualVariant
└─ loot/reward signature
```

## Rank

Rank can control the broad quantitative/metaphysical ceiling:

- health/damage scale;
- armor/resistance;
- essence quality;
- ability potency;
- later Will interaction.

## Class must change behavior qualitatively

Suggested design direction:

```text
Beast
  basic instinct / predator behavior

Monster
  simple tactics, retreats, target selection, crude coordination

Demon
  intelligent encounter AI, meaningful ability combinations

Devil
  bespoke supernatural ability package / encounter gimmick

Tyrant
  creates or commands minions

Terror
  changes the battlefield / environment / perception around itself

Titan
  regional calamity; encounter becomes a world event rather than a normal mob fight
```

This is far closer to canon than `class = HP multiplier`.

---

# 21. Echoes

## Snapshot model

When a kill yields an Echo, create a fixed `EchoInstance` from the creature's profile at death:

```text
EchoInstance
├─ uuid
├─ sourceCreatureId
├─ rank
├─ class
├─ abilities[]
├─ appearance
├─ healthState
└─ ownerUuid
```

Ordinary Echoes do not level.

## Commands

Useful command modes:

```text
FOLLOW
STAY
GUARD_POSITION
GUARD_OWNER
ATTACK_TARGET
PASSIVE
DISMISS
```

## Injury/death

- partial damage persists or heals while dismissed;
- fully destroyed Echo is permanently deleted;
- dismissal is not an infinite respawn button.

## Separate growth-capable summon API

Create an interface/base abstraction that an Aspect can extend:

```text
SoulSummon
├─ Echo       # static copy
├─ Shadow     # growth capable, if an Aspect grants it
├─ Reflection
└─ future Aspect-specific summon types
```

Do not hard-code Sunny's Shadow mechanics into `Echo`.

---

# 22. Corruption

Avoid one `corruption = 0..100` bar as the entire system.

## `CorruptionState`

```text
CorruptionState
├─ sources[]
├─ depth
├─ potency
├─ mindEffect
├─ soulEffect
├─ bodyEffect
├─ knownToPlayer
└─ resistanceLog
```

### Sources can behave differently

- Seed infection;
- forbidden knowledge;
- direct contact;
- cursed artifact;
- corrupted attack;
- environmental exposure.

### Threshold manifestations

Rather than only increasing debuffs:

```text
subtle dreams / UI distortion
false perceptions
ability mutations
hostile compulsions
physical changes
loss of control
full Defilement / creature conversion
```

The exact effects should be tied to source and story context.

### Cleansing

Make general cleansing rare.

Known routes can be encoded as special events:

- death;
- natural Awakening destroying a modest Seed;
- Aspect-specific purity;
- exceptional soul surgery.

No generic milk-bucket cure.

---

# 23. Gates and Seeds

## Seed state

World SavedData:

```text
NightmareSeed
├─ uuid
├─ nightmareLevel
├─ dreamRealmPos
├─ maturity
├─ callStrength
├─ linkedGate?
└─ scenarioTemplate
```

Maturity progresses even if no player chooses to challenge it, within server-performance limits.

## Bloom

When maturity reaches threshold:

1. choose/resolve waking-world breach location;
2. create Nightmare Gate;
3. start escalating incursion event;
4. keep Seed active until conquered.

## Gate event

```text
manifestation warning
→ weaker waves
→ escalating ranks/classes
→ Gate Guardian
→ continued breach pressure
```

Killing the Guardian **does not delete the Seed**.

This creates a strong multiplayer loop:

```text
contain Gate in waking world
        +
form cohort / locate Seed in Dream Realm
        +
conquer Nightmare
        =
permanent resolution
```

## Failure consequences

Server config:

```text
first_nightmare_failure = canon | forgiving
```

`canon` can allow actual death and a Category One breach.

`forgiving` preserves the existing cast-out/retry mechanic as an explicitly non-canon accessibility mode.

Do not pretend the safety mode is Spell lore.

---

# 24. Information, secrecy and multiplayer

This is where the false `Divine Sight` assumption would have done real damage.

## Server owns all Soul information

The client should only receive:

- the player's own full SoulData;
- public information about others;
- fields explicitly revealed by a power/action.

Do **not** sync every player's Flaw/Aspect to every client and merely hide it in the GUI. That makes cheating trivial.

## Public versus private fields

Potential defaults:

```text
PUBLIC
  displayed name
  obvious transformation
  publicly declared title
  perhaps broad Rank if socially known

PRIVATE
  formal Aspect name
  Aspect rank
  exact abilities
  Flaw
  True Name
  soul core details
  Memory inventory
```

## Information Aspects

Implement an `InsightQuery` system.

A revelation/perception ability can request fields such as:

```text
ATTRIBUTE
ASPECT_NAME
ASPECT_RANK
ABILITY_SUMMARY
CREATURE_RANK
CREATURE_CLASS
MEMORY_INFO
```

The ability's Rank/power determines which queries resolve.

This makes Cassie-like information powers mechanically meaningful without giving everyone an inspect command.

---

# 25. True Names

True Names deserve their own data type even though most players may never receive one.

```text
TrueName
├─ text
├─ meaningTags
├─ revealedTo[]
└─ specialInteractions[]
```

A True Name should not automatically grant a generic buff.

It is primarily:

- metaphysical identity;
- potential key for sorcery/powers;
- rare distinction;
- dangerous information in special cases.

Do not give every generated Aspect a Sunny-style slavery interaction. **Shadow Bond is specific to Shadow Slave.**

---

# 26. Factions, Clans and Citadels

## Data-driven ownership

Do not bake story-era ownership into block IDs.

```text
CitadelState
├─ citadelId
├─ gateway
├─ currentController
├─ historicalControllers[]
├─ domainAffiliation?
├─ steward?
├─ territory
└─ specialComponent
```

Then `Bastion` can remain Bastion whether Valor, Effie, another faction or a multiplayer clan controls it.

## Government gameplay identity

Potential systems:

- Gate-response contracts;
- contribution points;
- public-service rewards;
- access to rare supplies;
- Academy training;
- dossiers/records;
- evacuation events.

## Legacy Clan identity

Potential systems:

- shared Citadel;
- inherited Attributes/lineages;
- clan vault / Memories;
- training knowledge;
- oaths/political obligations;
- reputation;
- succession.

## Multiplayer clans

Do not wait until Phase 6 to design the data model. Even if UI/gameplay comes late, create a generic `FactionId` now so Memories, Citadels, Gates and permissions do not all invent their own team system.

---

# 27. The Spell presentation layer

## Three textual registers

### Notification

```text
[You have slain ...]
[You have received ...]
[The First Seal is broken.]
```

Short, bracketed, restrained.

### Runes/status

Structured fields and supernatural descriptions.

### Appraisal

Mythic narrative + occasional dry cruelty/irony.

## Visual identity

Spell:

- black negative space;
- silver;
- stars;
- thin connecting strings;
- runic text;
- restrained animation.

Aspect:

- generated/person-specific motif.

Do not color every supernatural screen purple just because the current bossbar is purple.

---

# 28. Procedural history — the system that could make this mod special

Long-term, the strongest version of this project is not `Minecraft with Shadow Slave powers`.

It is:

> **Minecraft capable of generating the kind of lost histories, Nightmares and personal stories that Shadow Slave uses.**

## `WorldMythology` / generated ancient history

Create a persistent graph at world creation / region creation:

```text
Civilization
HistoricalFigure
Settlement
War
Disaster
Artifact
Betrayal
Alliance
Curse
ReligiousOrder
AncientCreature
RuinedCitadel
```

Relations form historical events:

```text
Kingdom A built Citadel X
General B betrayed Kingdom A
Artifact C was used to imprison Creature D
City E fell during the betrayal
Order F survived and fled east
```

## Nightmares sample generated history

A Nightmare chooses one historical conflict and reconstructs it.

Players are assigned roles from that history.

Now `expected fate` has real meaning because the baseline history exists independently of the player.

## Memories remember that history

A Memory generated from a creature or person in the Nightmare can inherit:

- names;
- symbols;
- historical references;
- enchantment themes;
- descriptions.

## Ruins in the real Dream Realm echo it

The actual persistent Dream Realm can contain ruins/artifacts left by the same generated civilization.

A player may:

1. find a ruined city;
2. later enter a Nightmare reconstructing its fall;
3. meet the people whose statues they saw;
4. change the historical outcome inside the Nightmare;
5. receive a Memory whose description references that civilization.

That creates the **Shadow Slave feeling** far better than a catalogue of copied canon bosses.

## Scope warning

Do not attempt fully procedural mythology in the first Java release.

Build the data structures now, but start with hand-authored history templates containing procedural variables. Expand the generator once the Nightmare scenario engine is fun.

---

# 29. Data-driven content layout

Suggested resource organization:

```text
data/shadowslave/
  aspect_primitives/
  ability_primitives/
  flaw_archetypes/
  attributes/
  memory_enchantments/
  nightmare_scenarios/
  nightmare_roles/
  creature_archetypes/
  region_profiles/
  historical_templates/
  citadels/
  spell_text/
```

Not every folder must be a custom Minecraft registry. The important principle is:

> **Java supplies behavior primitives; data supplies content and composition.**

Use codecs/schema validation so broken references fail loudly during development.

---

# 30. Testing strategy after the Java port

## Preserve the current mindset

The existing harness has caught subtle bugs because it tests **state transitions and guards**, not just syntax.

Keep that.

## Pure Java/unit tests

Best for generators:

- generated Aspect always has valid Dormant expression;
- generated Flaw always has at least one executable rule;
- no impossible effect references;
- Memory Rank/Tier mappings valid;
- Memory enchantment budget not exceeded;
- scenario has at least one reachable resolution;
- exact generation seed is deterministic;
- serialization roundtrip preserves all SoulData;
- migrations preserve old player state.

Run thousands of generated seeds cheaply outside Minecraft.

## NeoForge GameTests

Best for world behavior:

- First Nightmare entry/exit;
- death cleanup;
- instance isolation;
- Memory summon/dismiss;
- Memory transfer;
- Echo death persistence;
- Gate wave progression;
- Seed conquest closes linked Gate;
- Soul data survives reconnect/restart/death according to config;
- no cross-player Nightmare cleanup.

NeoForge 1.21.1 GameTests can run in a dedicated game-test server and return failure exit codes, making them suitable for CI.

Reference:
- https://docs.neoforged.net/docs/1.21.1/misc/gametest/

## Keep the Python validator

Expand `validate.py` rather than deleting it.

Future checks can include:

```text
JSON schema validation
cross-reference resolution
missing translation keys
invalid ability primitive ids
rank/tier ranges
scenario role references
unreachable scenario resolutions
missing paired cleanup handlers
Spell message formatting
version drift
```

## Retire Mineflayer only when its coverage has moved

The current harness remains valuable for the datapack branch.

Once the Java mod requires a modded client handshake, Mineflayer may no longer represent the actual client environment. Port each assertion into GameTests/integration tests before deleting the old harness.

---

# 31. Performance principles

## Event-driven first

Do not scan every player/entity every tick for every Aspect.

Prefer:

- combat events;
- interaction events;
- player tick only for abilities that genuinely need continuous updates;
- scheduled timers;
- indexed active effects.

## Active-set ticking

Maintain sets such as:

```text
playersWithContinuousAbilities
activeNightmareInstances
activeGates
activeEchoes
```

Tick those rather than asking every player `do you have X?` for hundreds of generated powers.

## Nightmare chunks

Only ticket/load chunks belonging to active participants.

Clean dormant instances aggressively after persistence/snapshot rules allow it.

## Titan/Terror systems

Environmental effects should operate over scheduled regions/chunks, not brute-force every block every tick.

---

# 32. Server configuration

Useful configuration ideas:

```text
# progression
infection_mode
winter_solstice_interval
allow_natural_awakening

# consequences
first_nightmare_failure = canon | forgiving
player_death_memory_behavior

# world pressure
gate_frequency
gate_maturation_rate
corruption_enabled

# procedural systems
aspect_generation_complexity
nightmare_generation_mode = authored | mixed | procedural

# multiplayer
allow_player_clans
pvp_soul_information_rules

# performance
max_active_nightmare_instances
max_active_echoes_per_player_soft_limit
```

Avoid turning every canon rule into a toggle. Config should mostly control pacing, difficulty and server safety.

---

# 33. Revised roadmap recommendation

The original Phase names can survive, but a **canon bridge** needs to appear before Phase 2.

## Phase 1.5 — Canon state repair (still datapack)

Minimal-risk changes:

- distinguish Carrier from Sleeper wording;
- First Nightmare victory ends at **Sleeper/Dreamer**, not Awakened;
- rename `awaken/roll` conceptually to First-Nightmare resolution;
- remove fake `Vitality / Endurance` from canon Soul readout;
- rename placeholder `Shadow Slave` Flaw;
- mark timer/boss trial and cast-out retry explicitly as prototype deviations;
- expand tests for the corrected state.

Do **not** attempt full generated Aspects in scoreboards if Java Phase 2 is imminent.

## Phase 2 — Java foundation + Memories + first Dream Realm journey

This is where the project should cross the Java line.

Deliver:

- NeoForge project skeleton;
- imported current data/worldgen;
- SoulData + migration;
- Soul Screen;
- generated Aspect/Flaw v1;
- named Attributes;
- persistent Dream Realm dimension;
- Sleeper winter-solstice journey;
- Gateway return / actual Awakening;
- Memory Soul Arsenal;
- Memory Rank/Tier/enchantments/transfer;
- a small set of Nightmare Creature profiles that can drop generated Memories.

This would make Phase 2 substantially larger than `add loot`, but it fixes the progression foundation once instead of building Memories on the wrong state model.

## Phase 3 — Soul Essence, cores and Ascension

Deliver:

- essence reserve/control;
- shard absorption / saturation;
- ability costs;
- Rank progression framework;
- Second Nightmare infrastructure;
- Ability evolution;
- exceptional multi-core support where generated Aspect allows it;
- natural-Awakening path foundations if desired.

## Phase 4 — Echoes + richer Nightmare Creatures

Deliver:

- CreatureProfile system;
- Class-specific AI packages;
- Echo reward snapshots;
- summon command UI;
- persistent injury/destruction;
- artificial Echo hooks;
- generic extension point for growth-capable Aspect summons.

## Phase 5 — Gates, Seeds, later Nightmares, Corruption

Deliver:

- growing Seeds in Dream Realm;
- waking-world Gate events;
- linked Gate/Seed resolution;
- shared/cohort Nightmare instances;
- scenario central-conflict framework;
- Corruption sources/resistance;
- Third/Fourth-Nightmare hooks.

## Phase 6 — Clans, Citadels, information and Domains

Deliver:

- player/Legacy clans;
- Citadel control;
- faction permissions;
- private Soul information;
- revelation/inspection abilities;
- contribution/government systems;
- Domain framework;
- high-rank multiplayer politics.

## Long-term — Procedural history layer

Once all underlying systems work, unify them through generated ancient histories so Nightmares, ruins, Memories and factions refer to the same events.

---

# 34. What I would change first in the current repo

**This is a proposed next code pass, not performed by this document.**

1. Add an explicit progression/stage objective instead of using absent `ss_rank` as three different states.
2. Change Soul readout:
   - untouched → mundane/descriptive;
   - Carrier → Carrier/Aspirant-related language, **not Sleeper**;
   - post-First-Nightmare → Sleeper/Dreamer;
   - reserve Awakened for later.
3. Change `nightmare/survive.mcfunction` so it resolves the First Nightmare without setting Awakened Rank.
4. Split the current `awaken/roll` responsibilities:
   - `first_nightmare/reward`
   - `aspect/generate_placeholder`
   - `flaw/generate_placeholder`
5. Remove `Vitality` and `Endurance` labels from `soul.mcfunction`; call them internal combat values only if they remain visible at all.
6. Rename `ss_flaw_shadow_slave` and its player-facing title.
7. Correct comments that call Carriers `Sleepers`.
8. Add tests asserting:
   - Carrier is not Sleeper;
   - First Nightmare victory produces Sleeper;
   - Sleeper has Aspect + Flaw;
   - Sleeper cannot re-enter First Nightmare;
   - current prototype does **not** pretend first Dream Realm journey exists yet.
9. Update README/design docs only on the working branch after tests pass.

I would stop there in datapack code and begin the Java foundation instead of spending weeks making scoreboards simulate a system they are poorly suited to represent.

---

# 35. Things I would explicitly not do

1. **Do not copy Sunny/Nephis/Mordret Aspects into a player class selector.**
2. **Do not create 100 fixed Aspects and call that procedural.**
3. **Do not derive Flaw from one final combat statistic.**
4. **Do not use `Aspect Rank = damage tier`.**
5. **Do not show generic STR/DEX/VIT as Spell Attributes.**
6. **Do not rank up from Soul Shard XP alone.**
7. **Do not make all Memories non-transferable.**
8. **Do not make Memory Tier equal exact enchantment count.**
9. **Do not make Echoes immortal cooldown pets.**
10. **Do not make Tyrant/Terror/Titan just bigger health multipliers.**
11. **Do not give every player an inspect-other-player button.**
12. **Do not make every Nightmare a dungeon boss.**
13. **Do not make the Dream Realm one dark-purple dimension.**
14. **Do not hard-code Bastion/other Citadel ownership forever.**
15. **Do not make the Spell a chatty quest narrator.**
16. **Do not delete the test discipline that Phase 1 already has.**

---

# 36. The design north star

The mod should not try to recreate Sunny's exact story.

The strongest interpretation of the source material is:

> **Give Minecraft players the same kind of systems that could have produced a story like Sunny's, Nephis's, Rain's, Jet's or Mordret's — without requiring any of them to exist.**

That means the important things to proceduralize are not just loot tables.

They are:

- identity;
- consequences;
- historical roles;
- imperfect powers;
- meaningful choices;
- dangerous unknown information;
- persistent places;
- relationships between systems;
- fate that can be followed or defied.

If an Aspect is generated in isolation, a Flaw is rolled from another table, a Nightmare is a random arena and a Memory is a random sword, the systems will technically resemble Shadow Slave while the player's **story** will not.

If the same player's behavior influences their role, the role influences the trial, the trial influences the Aspect/Flaw, the Nightmare has a baseline history they can defy, the creature they kill can become a Memory or Echo, and that history later reappears as ruins in the Dream Realm — then the mod begins doing something much closer to what makes the novel distinctive.
