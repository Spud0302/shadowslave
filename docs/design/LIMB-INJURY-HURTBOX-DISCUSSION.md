# Limb Injury and Hurtbox Discussion Notes

**Status:** discussion / design capture only.  
**Implementation status:** **NOT STARTED.** This document preserves current owner direction and is not approval to begin runtime implementation.  
**Related:** `COMBAT-MOVEMENT-DISCUSSION.md`, `BUILD-DEFENSE-OFFENSE-PROGRESSION.md`, `ASPECT-COMBAT-CAPABILITY-NOTES.md`.

## Core direction

The combat framework should be capable of resolving attacks against meaningful anatomical hurt zones rather than treating every successful hit as equivalent damage to one undifferentiated body volume.

A significant injury to a body part should have a functional consequence. If an action materially depends on an injured limb or organ, the action may become:

- unavailable;
- slower;
- weaker;
- less accurate;
- shorter-ranged;
- more expensive or unstable;
- easier to interrupt;
- replaced by an improvised one-limb/compensating variant.

The purpose is not simulation for its own sake. The purpose is to make precision, armour coverage, weapon choice, positioning, injury, recovery, creature anatomy, build specialization and adaptation matter during the combat "dance."

## Hurt zones, not microscopic simulation

Do not model every finger, tendon or bone independently.

A generic humanoid body could expose coarse functional zones such as:

- head / sensory region;
- torso / core;
- left arm;
- right arm;
- left leg;
- right leg.

Hands, feet, wings, tails, horns, jaws, eyes, antennae, cores, shells or other parts may be separate zones only when the creature/action design actually benefits from them.

The generic engine should support arbitrary anatomy definitions rather than hard-code a six-zone human skeleton.

## Functional tags

Actions should be able to declare which anatomical functions they require, rather than containing character-specific injury checks everywhere.

Conceptual examples:

```text
Two-handed heavy strike
    requires: LEFT_ARM + RIGHT_ARM + STABLE_TORSO

Shield guard
    requires: SHIELD_ARM

Combat step
    requires: at least one FUNCTIONAL_LEG
    improved by: two FUNCTIONAL_LEGS

Sprint
    requires: two FUNCTIONAL_LEGS

Vault
    requires: locomotion + relevant support/contact capability

Climbing transition
    requires: sufficient ARM_SUPPORT + LEG_SUPPORT

Bow draw
    requires: DRAW_ARM + SUPPORT_ARM
```

An Aspect, Memory, transformation, Echo body plan or supernatural adaptation may satisfy, replace or bypass a requirement when explicitly authored to do so.

## Injury states

Prefer a small number of meaningful states rather than continuous penalties on every point of local damage.

Candidate vocabulary, not final balance:

```text
HEALTHY
    normal function

IMPAIRED
    function remains but suffers meaningful penalties

DISABLED
    ordinary actions requiring the part cannot use it effectively

MAIMED / LOST
    severe structural loss; ordinary function unavailable until an appropriate recovery mechanism exists
```

Not every hit should advance an injury state. Armour, Rank/body durability, damage profile, penetration, attack force, angle, current injury, supernatural defenses and target anatomy can all matter.

Exact thresholds and formulas remain unresolved.

## Limb consequences should be action-specific

Do not apply a generic global debuff such as `-25% everything` for an arm injury.

Examples:

### Arm impairment

Possible consequences:

- slower or weaker attacks using that arm;
- poorer guard stability on that side;
- reduced accuracy/control;
- higher recovery on two-handed actions;
- some grappling/climbing actions degraded.

### Arm disabled/maimed

Possible consequences:

- two-handed weapon actions unavailable unless an ability/equipment rule compensates;
- off-hand/shield actions requiring that limb unavailable;
- one-handed actions on the other side remain possible;
- climbing/grappling capability may be severely reduced;
- Aspect/Memory substitutions may provide alternate function.

### Leg impairment

Possible consequences:

- acceleration and combat-step performance reduced;
- unstable landings;
- worse knockback recovery;
- slower sprinting/repositioning;
- some vault/climbing transitions become harder.

### Leg disabled/maimed

Possible consequences:

- ordinary sprint/dash/vault actions may become unavailable;
- movement becomes substantially slower or asymmetric;
- stability and recovery after displacement degrade;
- crawling, support-assisted, grappling, teleporting or other alternate movement may still be valid if the build supports it.

### Sensory/head injuries

Only model these where they produce clear gameplay rather than frustrating screen effects. Possible consequences include reduced perception, targeting quality, reaction information or sensory capability, but exact presentation/accessibility rules remain unresolved.

## Global vitality and local injury should be related but not identical

A local injury system does not require every limb to become an independent conventional HP bar.

The design should distinguish:

- overall survival/vitality;
- local structural/functional injury;
- stagger/stability;
- supernatural target layers such as soul interaction.

A precise strike may create a serious local impairment without simply being the highest raw vitality damage attack. Conversely, a large torso impact may threaten overall survival without severing/disablement being its primary function.

Exact relationship between global health and local injury is unresolved.

## Offensive build implications

Limb hurt zones give offensive specialization more dimensions than raw DPS.

Potential offensive capabilities include:

- precision / targeted strikes;
- armour-gap exploitation;
- maiming/structural damage profile;
- penetration sufficient to reach a protected limb;
- attacks specialized for disabling mobility;
- attacks specialized for disarming/guard disruption;
- wide attacks that are easier to land but less anatomically precise;
- supernatural attacks that bypass or alter ordinary anatomy rules.

A precision fighter might intentionally attack the weapon arm or locomotion limb rather than maximizing torso damage.

## Defensive build implications

Defensive builds can also specialize around local injury:

- armour coverage by body region;
- local hardening;
- guard positioning that protects a vulnerable limb;
- improved structural resistance;
- faster functional recovery;
- ability to continue using an impaired limb;
- redundant/supernatural anatomy;
- phase/teleport avoidance;
- Memories that brace or replace a function.

This should combine with the earlier principle that different defensive properties answer different offensive properties.

## Armour should have coverage

If local hurt zones matter, armour should be able to protect specific regions rather than every equipped item becoming a universal scalar.

Examples:

- gauntlet / armguard protects arm/hand-related zones;
- greaves protect legs;
- helmet protects head/sensory region;
- cuirass protects torso/core;
- a supernatural Memory may project protection across zones or protect a non-physical target layer.

Coverage does not necessarily require a conventional equipment slot for every body part; it is an interaction property.

## Combat dance implications

Injury should change the ongoing exchange rather than merely punish the player after combat.

Example:

```text
Enemy commits to heavy overhead strike
Player redirects it
Player counter-cuts enemy weapon arm
Enemy arm becomes impaired
Enemy's original two-handed sequence becomes slower/less stable
Enemy switches to kicks, one-handed attacks, retreat, or another ability
Player must now read a changed combat pattern
```

Likewise, if the player is injured, the correct answer may change mid-fight. A player who loses reliable use of a leg may stop relying on combat steps and instead guard, use terrain, summon an Echo, equip/use a Memory, teleport if their Aspect allows it, or escape by another route.

The desired result is adaptation, not a snowball where the first limb hit guarantees death.

## Creature anatomy should matter

Nightmare Creatures should define their own functional anatomy.

Examples of possible mappings:

- damaging a wing limits flight but may not affect ground attacks;
- damaging a forelimb changes charge/claw actions;
- breaking a horn removes a gore/charge action but may enrage or alter behavior;
- damaging a sensory appendage weakens detection;
- damaging a tail removes a sweep/balance action;
- damaging shell plates may expose a vulnerable layer instead of disabling locomotion;
- multi-limbed creatures may retain an action at reduced effectiveness because they have redundant limbs;
- amorphous, incorporeal or regenerating creatures may not use conventional limb injury at all.

Do not assume every enemy can be permanently trivialized by targeting its legs.

## AI should adapt to injuries

An injured AI should not continue requesting actions that its current anatomy can no longer execute.

The same capability/action requirement system used for players should constrain AI actions.

AI may respond by:

- switching movesets;
- protecting an injured region;
- retreating;
- becoming more aggressive;
- changing stance;
- using ranged/supernatural abilities;
- relying on remaining limbs;
- attempting regeneration/recovery where authored.

This keeps player and enemy combat inside the same rules.

## Supernatural exceptions

Aspect/Attribute/Memory rules may deliberately break ordinary anatomical assumptions.

Examples:

- hardening prevents an injury threshold from being reached;
- regeneration restores function faster than ordinary recovery;
- shadow/telekinetic force substitutes for a disabled arm;
- teleportation remains usable despite leg disablement if its activation does not require ordinary locomotion;
- a transformation changes the entity's anatomy graph entirely;
- an incorporeal form does not expose ordinary physical limbs;
- an ability temporarily permits use of an otherwise impaired body part at a cost.

The generic combat engine should expose anatomy/function requirements and modifiers. Shadow Slave decides why an exception exists.

## Soul damage is separate

Physical limb injury should not automatically describe soul injury.

A soul-targeting attack can use its own target-layer semantics and should not be forced into `left arm soul HP`, etc., unless primary lore evidence and later design justify a specific relationship.

The previously recorded Jet/Soul Reaper principle remains: soul targeting bypasses the physical defense layer, but dedicated soul defense/resistance can exist.

## Recovery and persistence remain open

This discussion does **not** decide:

- natural healing rates;
- whether impairment persists between encounters;
- when a disabled limb becomes functional again;
- how permanent maiming works;
- regeneration;
- prosthetics;
- medical items/abilities;
- what death/respawn means for bodily injury;
- how Dream Realm/Nightmare transitions affect injury;
- whether Rank progression changes recovery;
- accessibility settings around injury presentation.

These require separate lore and gameplay decisions before implementation.

## Anti-frustration guardrails

1. Do not make routine chip damage constantly disable actions.
2. Severe functional injuries should result from meaningful hits, accumulated trauma, exposed weaknesses or explicit maiming attacks.
3. Injuries should create adaptation opportunities, not deterministic death spirals.
4. The UI/presentation should clearly communicate why an action is degraded/unavailable.
5. Player input should not silently fail; the system should communicate the injured requirement and, where possible, choose an authored degraded variant.
6. Different creature anatomies must be authored deliberately; no universal humanoid assumptions.
7. Precision targeting should require skill/opportunity and should not invalidate heavy/generalist combat styles.
8. Multiplayer authority for hurt-zone hits and injury state remains server-side.

## Current owner direction preserved

1. Combat may use hurtboxes on meaningful limbs/body regions.
2. Maiming or seriously injuring a required limb should materially affect actions that depend on that limb.
3. Consequences may include action loss, slower execution, reduced power/control, or degraded variants rather than one generic stat penalty.
4. The system should apply to creatures through authored anatomy and functional requirements, not only players.
5. Limb injury should integrate with armour coverage, offensive/defensive specialization, movement, action requirements, AI adaptation, Aspects and Memories.
6. Anatomy-based physical injury is separate from soul-targeting mechanics unless later lore/design says otherwise.
7. The system should favor meaningful injury states over excessive anatomical micro-simulation.
8. Implementation is not started or authorized by this note.
