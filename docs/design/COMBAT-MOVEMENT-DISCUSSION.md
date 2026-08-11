# Combat and Movement Discussion Notes

**Status:** discussion / design capture only.  
**Implementation status:** **NOT STARTED.** This document preserves owner direction from design discussion and is not approval to begin implementation without a later explicit development decision.  
**Scope:** reusable combat/movement framework direction plus Shadow Slave integration constraints.

## Why this note exists

Combat and exploration are intended to be major gameplay pillars. The current alpha proves entity, Memory, Echo, Aspect, Nightmare, and Dream Realm execution paths, but much of physical combat still inherits ordinary Minecraft movement, melee timing, and vanilla-derived entity statistics. The intended end state is more deliberate and readable than vanilla hit trading.

The design direction is to build a reusable combat/movement framework that is not inherently tied to Shadow Slave, then let Shadow Slave supply character progression, Soul/Aspect/Memory/Echo rules, creature identities, and supernatural modifiers through adapters.

Nothing below should be treated as final numeric balance or as canon unless separately identified by lore research.

---

## 1. Core combat feel: a back-and-forth dance

A serious encounter should feel like an exchange of initiative rather than two entities standing in range and trading damage.

Preferred rhythm:

1. **Observe** — read posture, movement, sound, terrain, or attack telegraph.
2. **Respond** — reposition, evade, guard, interrupt, use terrain, use a Memory/Aspect, or deliberately bait the action.
3. **Create an opening** — cause the enemy to miss, overcommit, lose footing, expose a weak point, enter recovery, or become tactically disadvantaged.
4. **Commit** — attack or use a meaningful ability while accepting the risk of the player's own wind-up/recovery.
5. **Disengage / reassess** — avoid endless stun-locking or DPS loops; control returns to observation and positioning.

This is a **tempo / initiative** model. Good combat is not simply higher damage-per-second.

### Design consequence

Both player and enemy actions should have readable commitment. If a creature makes a powerful attack and misses, that can create a short punish window. If the player commits to a heavy attack at the wrong moment, the player should likewise be exposed during recovery.

The framework should make these openings emerge from action timing rather than relying primarily on arbitrary stun effects.

---

## 2. Do not turn every encounter into a universal dodge test

The project should **not** silently grant every character a Soulslike roll with generous universal invulnerability frames.

A normal evasive action should usually succeed because the character physically leaves or avoids the hostile hit volume.

Special behaviour such as:

- phasing through an attack,
- teleporting,
- supernatural air-dashing,
- unusually long invulnerability,
- impossible mid-air redirection,

should come from a learned technique, equipment/Memory, Attribute, Aspect, or another explicit capability rather than being a baseline player entitlement.

This preserves build variety and prevents every enemy from becoming:

> wait for telegraph -> press the same dodge button -> punish.

---

## 3. Reusable framework, not a Shadow Slave-only combat engine

The combat/movement system should ideally become a reusable Minecraft/NeoForge framework.

Conceptual layering:

```text
Minecraft / NeoForge fundamentals
    walking, sprinting, jumping, gravity, collisions,
    swimming, riding, entity movement, base networking
                |
                v
Generic combat + movement framework
    actions
    combat timing
    movement capabilities
    hit detection
    stagger / stability
    input buffering
    action modifiers
    server authority
                |
                +--> optional GeckoLib presentation adapter
                +--> optional SmartBrainLib AI adapter
                |
                v
Shadow Slave game adapter
    Soul / Rank / body progression
    learned techniques
    Aspect abilities
    Attributes
    Memories
    Echoes
    Nightmare Creature rules
    Essence / supernatural costs
```

The generic modules should never need to know what an Aspect, Memory, Echo, Nightmare, or Soul Core is.

Shadow Slave should answer generic questions such as:

- what actions this entity currently knows;
- what movement capabilities it currently exposes;
- what modifiers apply;
- what resource/cooldown requirements exist;
- what supernatural exceptions are legal.

---

## 4. Action model

The preferred foundation is a general **Action** system.

An action may contain:

- wind-up duration;
- active duration;
- recovery duration;
- movement commitment;
- rotation/facing constraints;
- hitbox schedule;
- damage/stagger profile;
- cancel windows;
- allowed follow-ups;
- resource requirements;
- animation/presentation keys;
- server validation policy.

Examples of actions include:

- normal weapon strike;
- committed heavy strike;
- combat step;
- guard;
- parry/counter window;
- creature swipe;
- creature charge;
- Chainback displacement attack;
- Ash Burrower eruption;
- Aspect ability;
- Memory active effect.

The framework should not require every action to be an attack.

### Action phases

A common shape:

```text
WIND_UP -> ACTIVE -> RECOVERY
```

Possible later additions:

- hold/charge phase;
- branching follow-up;
- interrupted state;
- movement-only action;
- channelled action;
- sustained guard;
- contextual traversal action.

Exact tick values are DESIGN and must be tuned through playtesting.

---

## 5. AI should obey the same combat language

SmartBrainLib or vanilla AI may decide **what an enemy wants to do**, but the combat framework should execute the actual combat action.

Preferred separation:

```text
SmartBrainLib / AI decision
    "Ash Burrower should erupt now"
                |
                v
Combat action request
    ASH_BURROWER_ERUPTION
                |
                v
Generic action execution
    wind-up -> movement -> hitbox -> damage -> recovery
```

Enemies should not bypass the rules the player is reading by secretly using instantaneous vanilla `doHurtTarget`-style attacks when a telegraphed action system is expected.

This allows combat to be reasoned about consistently across players, Echoes, NPCs, and Nightmare Creatures.

---

## 6. Hitboxes and hurtboxes

Minecraft's ordinary melee ray/target model is likely too limited for the desired encounters.

Long-term framework support should allow server-authoritative temporary hit volumes tied to an action timeline, for example:

- weapon arcs;
- thrusts;
- overhead blows;
- claws;
- tails;
- wings;
- body slams;
- charges;
- grabs;
- giant creature limbs;
- weak-point-specific attacks.

GeckoLib may present the animation, but the combat engine should own whether and when the action actually hits.

Presentation and authority must remain separate.

---

## 7. Stability / stagger rather than constant stun-lock

A combatant can have a generic stability/posture-like concept without necessarily exposing a permanent HUD bar.

Different impacts can produce different disruption:

- negligible reaction;
- flinch;
- stumble;
- interrupted action;
- guard break;
- major opening.

Large or higher-Rank creatures may have substantially greater stability. Known weaknesses, good counters, heavy weapons, environmental interactions, or specific abilities may produce disproportionate disruption.

The goal is to create openings without letting the player permanently stun-lock serious enemies.

---

## 8. Rank disparity should change the problem, not only HP

Higher-rank or otherwise superior enemies should not simply be ordinary mobs with enormous health pools.

Possible consequences of disparity include:

- much smaller margin for player mistakes;
- player attacks causing little stability loss unless a weakness is exploited;
- harder-to-interrupt enemy commitments;
- superior reach, movement, perception, or recovery;
- stronger punishment for failed positioning;
- necessity of terrain, Memories, Echoes, traps, preparation, or escape.

A sufficiently superior opponent may turn the encounter objective from "win the DPS race" into:

- escape;
- survive;
- separate it from allies;
- trap it;
- exploit terrain;
- discover and use a weakness;
- engineer an unfair fight.

---

## 9. Creature-specific combat dances

Enemies should change *what the player must read*, not merely their damage numbers.

### Ash Burrower

Possible combat identity:

- movement/vibration information warfare;
- circle or reposition below ground;
- telegraphed eruption/ambush;
- brief exposure or recovery after a failed commitment;
- player may deliberately bait movement-sensitive behaviour.

### Chainback

Possible combat identity:

- spacing and displacement control;
- readable snag/pull wind-up;
- terrain/line-of-sight counterplay;
- punished panic retreats or poor positioning;
- recovery window after an unsuccessful major displacement action.

### Drowned Listener

Possible combat identity:

- noise/vibration management;
- reckless sprinting/attacking reveals the player more easily;
- crouching, breaking contact, dry terrain, or environmental sound can alter the engagement;
- combat includes information denial rather than only visual dodging.

The general principle is:

> Every serious enemy should ask the player to observe -> respond -> create an opening -> commit -> reassess, but each enemy should make that sequence meaningfully different.

---

## 10. Movement capabilities are not universally unlocked

The generic movement engine may know how to execute many traversal/combat actions, but **an ordinary character does not automatically know them**.

Movement should distinguish at least:

1. **Knowledge / learned technique** — has the character been taught or learned how to perform it?
2. **Physical capability** — is the body currently capable of executing it effectively?
3. **Equipment / Memory capability** — does an item enable or alter the action?
4. **Supernatural capability** — does an Aspect/Attribute/other supernatural source grant or modify the action?
5. **Current state requirements** — stance, terrain, contact, stagger, movement speed, etc.

A stronger body should improve the performance envelope of a learned movement technique; becoming stronger should not automatically teach the technique.

This preserves the distinction between **power** and **competence**.

---

## 11. Baseline movement

An ordinary starting character should retain ordinary Minecraft-compatible fundamentals such as:

- walking;
- sprinting;
- jumping;
- crouching;
- basic swimming;
- normal ladder/basic climbing interactions;
- ordinary falling and knockback behaviour.

The character should **not** begin with a full action-game traversal kit.

Combat danger at low progression partly comes from limited options and ordinary physical vulnerability.

---

## 12. Candidate advanced movement techniques

The following are candidate generic capabilities, **not automatic starting unlocks**.

### 1. Dash / combat step

Likely learned advanced movement or Aspect-granted.

A mundane trained version should generally be a short explosive reposition with little/no universal magical invulnerability. Aspect/Memory variants may change distance, conditions, direction changes, phase rules, etc.

### 2. Slide

Learnable mundane movement. Requires suitable speed/terrain/state. More accessible than supernatural traversal but still not necessarily an untrained baseline action.

### 3. Vault

Learnable mundane traversal. Useful for obstacles, ruined structures, windows, low walls, and combat repositioning.

### 4. Wall rebound

Advanced learned movement requiring suitable physical capability. Certain Aspects could enhance or fundamentally alter it.

### 5. Aerial recovery

Advanced learned combat movement. Mundane use may restore control/landing after launch or knockback; supernatural sources may permit stronger mid-air correction.

### 6. Lunge

Often better treated as weapon/combat training or an action tied to a moveset rather than a universal traversal button.

### 7. Grapple movement

Normally requires appropriate equipment, Memory, environmental attachment, or supernatural ability. Not baseline.

### 8. Knockback recovery

Learned combat skill. An experienced fighter may regain footing/control faster after displacement without simply becoming immune to knockback.

### 9. Climbing transitions

Progressive traversal skill. Ordinary ladder/climb behaviour remains baseline; fast ledge catches, corner transfers, lateral changes, climbing attacks, and similar advanced transitions require capability.

---

## 13. Capability-based movement API direction

Avoid a simplistic global flag such as:

```text
player.canDash = true
```

Prefer a capability/technique model where an action resolves requirements and modifiers.

Conceptual example:

```text
Movement Technique: combat_step

Requirements
    learned: combat_step_basic
    physical capability threshold
    valid stance
    valid ground/contact state
    not heavily staggered

Providers / modifiers
    learned technique
    physical progression
    weapon style
    Memory/equipment
    Aspect
    Attribute
```

Two players may therefore both know the same mundane technique while their supernatural or equipment modifiers make it behave differently.

---

## 14. Training should matter independently of supernatural power

A skilled character should be able to move and fight better because of practice, instruction, experience, and technique even when compared with someone of similar raw supernatural power.

Potential acquisition sources for generic techniques include later design decisions such as:

- NPC instruction;
- training;
- martial/combat schools;
- manuals or records;
- repeated practice / mastery progression;
- scenario-specific instruction;
- other non-supernatural progression.

The exact learning system is not decided here.

Important invariant:

> Soul/body progression can improve execution, but it should not silently grant knowledge the character never acquired.

---

## 15. Aspect / Memory mobility should sit on top of learned movement

An Aspect should be able to:

- grant an otherwise unavailable movement capability;
- enhance a mundane technique;
- remove one requirement;
- change valid terrain/contact conditions;
- transform displacement distance or direction;
- replace the technique with a unique supernatural action.

A Memory can similarly provide equipment-enabled mobility such as grappling, climbing assistance, special recovery, etc.

The generic engine should only see capabilities/modifiers. It should not need to understand the lore source that produced them.

This allows characters to have genuinely different movement identities rather than converging on the same late-game nine-button moveset.

---

## 16. Exploration must respect different movement kits

The worldgen discussion and movement discussion should reinforce each other.

Generated ruins/regions can expose multiple routes, for example:

- universally accessible longer route;
- vault shortcut;
- advanced climbing route;
- wall-rebound gap;
- grapple-accessible tower;
- flooded route requiring suitable swimming/equipment;
- dangerous combat route requiring no mobility unlock at all.

Special movement routes should generally be:

- shortcuts;
- tactical advantages;
- hidden areas;
- escape routes;
- observation positions;
- optional resources/loot;
- alternative approaches.

They should **not** routinely make basic progression impossible for characters who lack one specific technique.

Different characters should be able to explore the same Dream Realm differently.

---

## 17. Combat must respect different movement kits

Enemy attacks should create problems rather than assume one universal dodge answer.

Example: Chainback displacement might be answered by different characters through different means:

- beginner: use terrain/line of sight and run before the active window;
- trained fighter: combat step;
- highly trained fighter: better knockback recovery;
- defensive build: guard/brace with an appropriate Memory;
- mobility Aspect: supernatural reposition;
- tactical build: use an Echo or environmental obstruction.

This is preferable to designing every encounter around one mandatory evade mechanic.

---

## 18. Multiplayer authority

The combat framework should be designed server-authoritative from the beginning.

Preferred direction:

```text
Client
    local input
    immediate presentation/prediction where safe
    send action intent + sequence

Server
    validate current authoritative state
    validate known capability / equipment / resource / cooldown
    start authoritative action
    resolve hitboxes, damage, stagger, movement authority

Client
    reconcile presentation to authoritative action state
```

The recent Shadow Slave keybind work already follows the useful principle that the client sends **intent**, not canonical outcome/state.

---

## 19. Potential module boundary

Do **not** immediately create a separate repository solely to make the framework look generic.

A safer initial architecture, once implementation is explicitly approved, would be module boundaries inside the existing project such as:

```text
combat-core
combat-neoforge
combat-geckolib       (optional adapter)
combat-smartbrain     (optional adapter)
mod                    (Shadow Slave)
```

A hard rule should be that generic combat modules do not import `dev.spud.shadowslave` game-state classes.

If multiple real encounters prove the API and it becomes stable, extraction into a standalone repository/library can be reconsidered later.

---

## 20. Candidate prototype sequence — discussion only

If/when implementation is explicitly approved, a useful proof sequence may be:

1. **Chainback** — tests readable attack commitment, displacement, spacing, combat step/guard/recovery, stagger, and punish windows.
2. **Ash Burrower** — tests unusual locomotion, sensing, ambush/eruption, baiting, and combat information.
3. **Drowned Listener** — tests sensory/noise-driven combat and environmental counterplay.

The first prototype should avoid Aspect complexity so the generic combat model can be judged on its own.

This is a proposed validation sequence, not an implementation task.

---

## 21. Relationship to current alpha

Current alpha executors remain useful as compatibility/prototype infrastructure. They should not be interpreted as final combat balance.

Known examples from the present implementation include:

- hostile creature attributes still derived from vanilla Silverfish/Spider/Drowned bases;
- Ash Burrower Echo originally derived physical attributes from Armadillo execution;
- Kindle currently executes as a bounded vanilla Night Vision + Movement Speed effect;
- creature-specific physical behaviours are beginning to exist, but do not yet form a unified combat-action framework.

No existing authoritative Soul, Aspect, Memory, Echo, Nightmare, or creature identity state should be moved into the generic combat engine.

---

## 22. Decisions / preferences captured so far

The design discussion currently favors these principles:

- combat should feel like a readable **back-and-forth dance**;
- positioning, timing, openings, commitment, terrain, creature knowledge, Memories, Aspects, and Echoes should matter more than raw DPS;
- generic combat/movement infrastructure should be reusable outside Shadow Slave;
- Minecraft fundamentals should remain underneath rather than replacing the entire movement engine;
- players should not start with a universal advanced movement toolkit;
- advanced movement should be learned/taught/unlocked or supplied/modified by Aspect, Memory, Attribute, equipment, or other explicit capability;
- stronger bodies improve known techniques but do not automatically teach them;
- ordinary skill/training should remain valuable independently from supernatural power;
- no universal free supernatural dodge/i-frame assumption;
- AI and players should ultimately participate in the same action timing language;
- soft stagger/openings are preferred over routine stun-locking;
- worldgen should provide optional routes that reward different movement kits rather than requiring one universal mobility build;
- serious enemies should create different tactical dances rather than merely different health/damage values;
- server authority is required from the start of any eventual implementation;
- discussion can continue before implementation begins.

---

## 23. Explicit non-decisions

The following remain intentionally undecided and should be fleshed out before implementation:

- exact input/control scheme for combat;
- whether generic guard/parry is universally learnable or weapon-style-specific;
- exact dodge/combat-step availability and any i-frame rules;
- stamina or equivalent mundane exertion model, if any;
- exact Soul Essence relationship to combat actions;
- exact damage formula;
- exact Rank/Class scaling;
- armour/penetration model;
- critical/weak-point formulas;
- stability/stagger formula;
- technique learning/mastery progression;
- weapon moveset scope;
- PvP rules;
- death/injury consequences;
- camera/lock-on behaviour;
- first-person vs third-person presentation constraints;
- animation cancellation philosophy;
- exact network prediction/reconciliation implementation;
- whether/when the generic framework should become a standalone repository.

These should remain discussion topics until explicitly approved for implementation.
