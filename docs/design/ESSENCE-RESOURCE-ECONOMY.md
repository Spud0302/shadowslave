# Essence and Combat Resource Discussion Notes

**Status:** discussion / design capture only.  
**Implementation status:** **NOT STARTED.** This note preserves current owner direction and canon-informed constraints while the combat/progression design is being explored. It is not approval to begin runtime implementation.

## Why this note exists

The combat framework should not treat supernatural abilities as cooldown-only buttons or collapse every supernatural cost into a generic game-agnostic `mana` bar.

In Shadow Slave, essence is a real combat and progression resource. Awakened can perceive, channel, and spend Soul Essence; Memories can require essence; Aspect Abilities can consume essence; healers can exhaust their essence reserves while treating injuries; and depletion can remove supernatural options at critical moments.

The generic combat framework should therefore support reusable resource mechanics, while the Shadow Slave layer decides what Soul Essence, Shadow Essence, Spirit Essence, or any later lore-specific source actually means.

## Canon anchors to preserve

Primary canon evidence currently used for this discussion:

- Chapter 174 establishes that Awakened can interact with and channel Soul Essence through their bodies, and that this is important for using the full potential of higher-rank Memories.
- Chapter 430 shows Sunny's Shadow Essence reserve being rapidly drained by a powerful Memory enchantment.
- Chapter 566 shows depletion of Sunny's essence preventing a Memory from fully manifesting.
- Chapter 524 shows an Awakened healer becoming nearly depleted after repeatedly healing others, while still using essence to repair Sunny's torn ligament.
- Chapter 1238 explicitly distinguishes Spell-made Memory weave made from Soul Essence from Sunny's weave made from Shadow Essence, while showing Shadow Essence can function as a substitute in that supernatural structure.
- Later chapters introduce Spirit Essence/world-connected essence interactions in higher advancement contexts. Exact rules must be researched separately before implementation.

These references support engine flexibility; they do **not** justify inventing a unique named essence type for every Aspect.

## Soul Essence should not be a generic mana bar

The useful model is not merely:

```text
mana = 100
ability costs 20
```

A resource profile may have several independently meaningful properties:

- **capacity** — how much can be held;
- **current reserve** — how much remains now;
- **recovery / replenishment** — how and how quickly it returns;
- **source** — internal core, ambient/world source, stolen essence, converted essence, external reservoir, etc.;
- **throughput** — how much can safely/effectively be channelled at once;
- **control / efficiency** — how much useful effect is produced from a given expenditure;
- **conversion rules** — whether one resource can substitute for another and at what restrictions;
- **affinity** — whether particular actions, environments, Memories, or supernatural effects are unusually efficient or inefficient;
- **reserve requirements** — whether an action needs an amount held in reserve rather than simply consuming it;
- **sustain cost** — continuous drain while an action/state remains active;
- **activation cost** — one-time cost to start an action;
- **pulse / per-event cost** — expenditure each strike, heal, teleport, summon command, etc.;
- **recovery lock / suppression** — circumstances in which replenishment is slowed, stopped, or reversed.

Exact canon formulas are UNKNOWN and should remain configurable design until researched and playtested.

## Mundane exertion and supernatural essence are separate

The combat system should not assume physical fatigue and essence depletion are the same resource.

A character may be:

- physically fresh but almost out of essence;
- physically exhausted while still holding a large essence reserve;
- badly wounded but still capable of spending essence;
- physically intact but unable to use an Aspect Ability because the relevant resource is depleted;
- supernaturally empowered while still suffering a disabled limb that the ability does not replace.

This separation supports the earlier injury and capability design.

A future exertion/stamina model, if adopted, should therefore coexist with essence rather than replacing it.

## Actions should declare resource requirements

Generic actions should be able to express costs without knowing Shadow Slave lore.

Conceptual example:

```text
Action: short-range teleport

Resource requirements
    resource family: supernatural_primary
    activation cost: 18
    sustain cost: none
    minimum reserve after activation: 4

Other requirements
    valid destination
    not interaction-locked
    ability capability present
```

Shadow Slave might bind `supernatural_primary` to ordinary Soul Essence for one character and a compatible Aspect-specific equivalent for another.

The generic engine should not hard-code `SoulEssence` into every action.

## Different supernatural resources may obey different rules

Sunny provides an important canon precedent: his Shadow Essence is not merely a differently coloured UI bar. It is tied to his nature and can function differently while still substituting for Soul Essence in at least some systems.

Therefore, an Aspect or other supernatural nature may potentially alter:

- what resource the character generates/uses;
- what effects it can power;
- where it can be replenished;
- how efficiently it powers certain actions;
- whether the environment increases/decreases supply;
- whether it can be stolen, shared, converted, stored, or transferred;
- whether depletion has unusual consequences.

However:

> **Do not invent a custom essence type for every Aspect by default.**

Ordinary Soul Essence should remain the normal case unless canon or authored Aspect design provides a meaningful reason to change the resource rules.

## Build progression can specialize resource economy

Resource progression should offer meaningful build directions beyond simply increasing a maximum bar.

Possible specialization dimensions include:

### Deep reserve

- larger maximum capacity;
- better endurance in long fights;
- able to sustain expensive abilities longer;
- may sacrifice burst throughput or recovery speed depending on the authored build.

### High throughput

- channel more essence into a short action;
- stronger burst enhancement, healing, hardening, displacement, etc.;
- may consume reserves quickly or create longer recovery windows.

### Efficiency

- lower expenditure for familiar actions;
- better Memory activation efficiency;
- less waste while sustaining abilities;
- rewards mastery rather than simply raw capacity.

### Recovery / replenishment

- regain usable essence faster under allowed conditions;
- sustain repeated engagements better;
- may be tied to environment, rest, kills, allies, Aspect affinity, or another authored source.

### Control

- finer modulation of output;
- better partial activation;
- safer continuous effects;
- more precise enhancement of individual actions/body parts;
- potentially reduced waste and improved timing.

### Conversion / alternate source

- transform one compatible resource into another;
- draw from an external or environmental source;
- use an Aspect-specific substitute;
- steal/harvest essence if the Aspect actually supports it;
- use stored essence in a Memory or other reservoir.

These are generic design directions, not a claim that every character can learn every branch.

## Aspect-shaped build topology applies to resource economy too

The existing Aspect-shaped build principle should also affect resource progression.

An Aspect may:

- make normally competing capacity and throughput branches compatible;
- transform ordinary recovery into environment-dependent replenishment;
- prohibit a conventional regeneration path while opening a kill/harvest-based one;
- make a particular ability extremely efficient while other supernatural actions remain expensive;
- provide an external reservoir while limiting direct body enhancement;
- replace a generic resource branch with an Aspect-specific development path.

As with the wider build graph, compatibility does not grant mastery automatically. The character still needs progression, training, understanding, or other appropriate development.

## Resource costs should reinforce the combat dance

Essence should create tactical commitment rather than merely add downtime.

Good examples:

- a hardening Aspect can reject a dangerous hit, but repeated hardening drains the reserve and cannot be spammed forever;
- phasing can bypass a committed strike, but sustaining it may be expensive enough that persistent area pressure is still dangerous;
- a short teleport can solve positioning instantly, but repeated use can exhaust the resource or leave too little essence for offense;
- a healer can restore a disabled limb, but doing so may consume enough essence that the healer cannot repeatedly erase every consequence of combat;
- a powerful Memory enchantment can be available in principle but too expensive to maintain casually;
- a fighter may deliberately force an opponent to spend essence defensively before committing their decisive attack.

This allows players to attack not just health/stability but also an opponent's **resource position** through pressure and forced responses.

## Essence depletion should change the available action set

Reaching low essence should have functional consequences because abilities actually require the resource.

Possible generic states:

```text
healthy reserve
    -> all affordable supernatural actions available

low reserve
    -> expensive actions unavailable
    -> player must ration or change tactics

critical reserve
    -> only cheap/essential abilities remain viable

empty
    -> essence-powered actions fail/are unavailable
    -> mundane combat, equipment without active cost, allies, terrain and escape become more important
```

The UI should make failed requirements clear instead of silently swallowing input.

Example:

```text
Spatial Step unavailable:
Insufficient Essence
```

This should not imply that an essence-empty Awakened instantly becomes physically mundane; passive body/rank effects and exact canon behaviour must remain separately modelled.

## Memories should participate in the same economy

Canon supports higher-rank Memory enchantments requiring essence to use effectively.

The resource system should therefore support Memories that:

- have passive effects with no ongoing active cost;
- require an activation pulse;
- drain essence continuously while an enchantment remains active;
- reserve a portion of essence while manifested;
- consume their own stored resource instead of the user's;
- accept only compatible resource families;
- become impossible to summon/maintain when the required resource is unavailable.

Exact manifestation costs and rules must follow canon/content research rather than a universal invented formula.

## Healing belongs in the resource economy

The earlier injury system and essence system should interlock.

A healing action may need to consider:

- injury severity;
- target anatomy/function;
- target Rank/body quality;
- healer capability;
- essence activation/continuous cost;
- treatment time;
- whether the action stabilizes, repairs, regenerates, purifies, or replaces tissue;
- whether the target can continue fighting during treatment.

A healer exhausting their essence after treating several badly wounded allies is desirable gameplay if it follows their authored capability. Healing should therefore be powerful without turning injury into a meaningless instantly reset state.

## Resource information can become part of combat reading

Not every opponent's exact reserve should necessarily be exposed as a numeric PvP mana bar.

Combat readability can instead use cues such as:

- weakened supernatural effects;
- slower/rarer ability use;
- changed aura/presentation;
- failed manifestation;
- tactical retreat;
- known limitations discovered through scouting/lore;
- explicit UI only where the player has a justified perception/ability.

Whether enemy essence is visible, approximately readable, or hidden is unresolved.

## Rank, cores, quality, and essence

Canon establishes Soul Cores as storing essence, but exact game formulas for capacity, potency, Rank scaling, multiple cores, and regeneration should not be invented yet.

The engine should be able to represent these variables separately so future lore-correct rules can be fitted without architectural rewrites.

Potential generic concepts:

```text
ResourceReservoir
ResourceQuality
ResourceCapacity
ResourceThroughput
ResourceRecovery
ResourceConversion
ResourceAffinity
ResourceCost
```

Shadow Slave owns the actual mapping from Rank, Class/core count, Aspect, Attributes, Memories, environment, and progression to those values.

## Spirit Essence / later advancement

Later novel material introduces Spirit Essence and stronger world/environment-linked essence interactions. That suggests the generic system should not assume all supernatural energy is forever an isolated internal battery.

For now, this is an **architecture constraint only**:

- support internal reservoirs;
- support ambient/external sources;
- support ongoing draw/leak/flow;
- support source-dependent recovery;
- avoid coding current Awakened Soul Essence rules so rigidly that higher-rank systems require replacement.

Exact Spirit Essence mechanics remain UNKNOWN pending dedicated canon research.

## Interaction with injuries

Local injury requirements still apply unless the supernatural action explicitly bypasses them.

Examples:

- essence-enhanced sword strike still needs a functional sword arm unless the Aspect replaces that physical requirement;
- teleportation may remain usable with a damaged leg because its displacement is not leg-powered;
- body hardening may protect an injured limb from further damage but not automatically repair it;
- healing can restore limb function if its authored effect and resource cost allow it;
- an incorporeal/transformation ability may temporarily replace ordinary anatomical requirements;
- essence depletion can remove that supernatural workaround and expose the underlying injury again.

This is an important way for injury, Aspect design, and resource management to reinforce one another.

## Interaction with offense and defense

Resource expenditure should be attachable to both offensive and defensive interactions.

Examples:

- penetration amplification;
- stability reinforcement;
- body hardening;
- phasing;
- teleportation;
- soul protection;
- soul-targeting attacks;
- active Memory enchantments;
- healing;
- sensory enhancement;
- Echo/companion control where lore supports a cost.

This permits a strong defensive build to be pressured economically even if the attacker cannot immediately penetrate its defenses.

## Avoid universal cooldown design

Cooldowns can still exist when an ability naturally requires recovery, but they should not replace resource logic.

A supernatural action can be limited by any combination of:

- essence cost;
- wind-up/recovery commitment;
- state/anatomy requirements;
- environmental requirements;
- charges;
- cooldown;
- risk/consequence;
- opportunity cost.

The default design should avoid arbitrary cooldowns where essence and action commitment already provide the intended limitation.

## Open questions

The discussion does not yet settle:

- exact Soul Essence regeneration rates;
- whether recovery happens continuously, during rest, or under mixed conditions at each Rank;
- exact Rank/core scaling;
- whether all active Memories consume essence and how manifestation costs work;
- exact Shadow Essence conversion/compatibility rules;
- Spirit Essence mechanics;
- overdraw/negative essence, if any;
- whether severe depletion has physiological consequences beyond inability to power actions;
- whether essence can be intentionally transferred between ordinary Awakened;
- PvP visibility of opponent resources;
- resource UI presentation;
- exact efficiency/mastery progression;
- how resource progression is awarded/learned;
- how death/respawn/re-entry affects reserves.

These remain unresolved until dedicated lore research and gameplay design.

## Current owner direction preserved

1. Soul Essence and equivalents should be a major combat/progression system, not a generic mana afterthought.
2. Mundane exertion, bodily injury, stability, and supernatural resource depletion should remain distinct systems.
3. Generic combat actions should declare resource requirements without hard-coding Shadow Slave lore terms.
4. Ordinary Soul Essence is the normal case; Aspect-specific equivalents should only exist where they have meaningful authored/canon justification.
5. Resource builds can specialize in capacity, throughput, efficiency, recovery, control, conversion/source, and affinity rather than only maximum reserve.
6. Aspects may reshape the resource progression graph just as they reshape movement/offense/defense progression.
7. Essence spending should reinforce the back-and-forth combat dance by forcing rationing, commitment, pressure, and tactical adaptation.
8. Memories and healing should participate in the same resource economy where their authored/canon rules require it.
9. Future higher-rank/world-connected essence systems should fit the same generic architecture without rewriting the engine.
10. No runtime development is authorized by this document; implementation remains NOT STARTED.
