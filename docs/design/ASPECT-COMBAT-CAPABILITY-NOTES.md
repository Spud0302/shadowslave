# Aspect Combat Capability Discussion Notes

**Status:** discussion / design capture only.  
**Implementation status:** **NOT STARTED.** This document records owner direction while combat design is being explored. It is not approval to begin runtime implementation.  
**Related note:** `docs/design/COMBAT-MOVEMENT-DISCUSSION.md`.

## Why this note exists

The combat framework should not assume that supernatural abilities are merely damage buttons or movement skills. Aspects, Attributes, Memories, and other supernatural sources may change the rules by which attacks, movement, defense, targeting, and damage interact.

The reusable combat engine should therefore expose generic concepts such as attack phases, damage channels, defense responses, displacement, collision/phase rules, and capability modifiers. Shadow Slave should decide which supernatural source grants those capabilities.

The engine should not contain hard-coded concepts such as `Shadow Aspect`, `Soul Core`, or a particular named Aspect.

## Aspects may change attack interaction rules

Candidate supernatural effects discussed by the owner include:

- temporarily hardening the body so an incoming physical attack is stopped, deflected, or bounces;
- temporarily allowing an incoming attack to phase through the body;
- short-range teleportation or displacement;
- different damage channels/types rather than one universal health-damage number;
- attacks that target the soul rather than only the physical body.

These are examples of capability families, not an exhaustive Aspect catalogue and not final balance rules.

## Defense should not collapse into one armour number

The framework should permit qualitatively different responses to an incoming attack: absorb, harden, deflect, rebound, guard, phase, displace, redirect, or ignore a compatible channel. The exact vocabulary is not final; the principle is that defense should sometimes change the *interaction*, not only multiply incoming damage by a resistance value.

## Body hardening / attack bounce

A defensive Aspect might briefly harden the body before impact. Compatible physical attacks could be reduced, deflected, or rebound when their penetration/force is insufficient, while stronger or incompatible attacks could still break through. Correct timing may turn an enemy commitment into the defender's opening.

Hardening should normally require timing, state, resource, or commitment instead of becoming permanent free invulnerability. Hardening the body also should not automatically protect the soul, mind, essence, or another non-physical target layer.

## Phasing / temporary non-interaction

A different Aspect might allow attacks to pass through the character for a short period. This is distinct from a universal dodge iframe.

A phase capability may define exactly what it can phase through: ordinary physical hitboxes, projectiles, terrain/collision, entities, or particular channels. It should not automatically mean immunity to every possible effect. Soul-targeting attacks, persistent zones, or attacks explicitly able to interact with phased targets may still matter depending on later design.

## Short-range teleport / displacement

Teleportation should be distinct from an ordinary dash. A movement action can expose displacement type, maximum range, destination/collision rules, line-of-sight requirements, wind-up, arrival recovery, momentum/facing rules, and whether an attack can be carried through the displacement.

**Teleportation does not automatically mean invulnerability.** Leaving an attack volume can avoid the hit without secretly granting a universal iframe unless a separate phase/defense capability explicitly provides one.

## Multiple damage channels

The long-term combat framework should not assume all harm is equivalent. Candidate engine-level descriptors may include physical slash/pierce/blunt/crush/tearing, environmental or elemental channels, and supernatural target layers such as soul or mental interaction where later lore/design justifies them.

This is engine flexibility, not a claim that every listed channel must exist in Shadow Slave.

## Soul damage should be a different target layer

If soul damage is implemented, it should be meaningfully different from ordinary body damage rather than merely being differently coloured health damage.

Possible design principles:

- body armour does not automatically protect the soul;
- physical phasing does not automatically avoid a soul-targeting effect;
- body hardening does not automatically harden the soul;
- soul-targeting attacks may still require contact, range, line of sight, or another successful action condition;
- soul damage is not automatically "true damage" and may face dedicated soul defenses/resistances;
- the consequences of soul injury, healing, death, Rank interaction, and relation to persistent Soul state remain unresolved.

The generic engine can understand a target/damage channel. Shadow Slave owns what `soul` actually means.

## Canon check: Jet / Soul Reaper

Jet is the strongest existing canon example for how soul-targeting combat should be understood.

### CANON

- Chapter 25 establishes that Soul Reaper Jet is feared because her Aspect Abilities disregard flesh and target soul cores directly; ordinary armour, physical damage resistance, and physical protection therefore do not answer that attack layer.
- Chapter 378 establishes that soul attacks are **not defense-less true damage**. Saint's version of `Stalwart` provides complete immunity to mind and soul attacks, while the Mantle of the Underworld provides a moderate amount of protection against them.
- Chapter 738 again shows dedicated soul-damage resistance as an explicit defensive property: Sunny uses `Stalwart` to gain substantial resistance while exposed to a soul-annihilation effect.
- Chapters 1061-1062 show the severe resource/life constraint attached to Jet's own existence: her shattered soul core has critically limited essence, she conserves every drop, and when deprived of kills she can become too depleted even to summon a Memory.
- Chapter 1483 shows that Jet's Aspect Legacy later grants her a soulbound mist weapon that can assume several weapon forms and can be summoned without consuming essence. This improves her execution options without erasing the wider peculiarities of her shattered soul/existence.

### INFERRED design lesson

Jet's balance demonstrates a useful distinction for the game:

> bypassing **physical defense** means changing the defensive layer the opponent must answer on; it does not mean bypassing all defense.

Therefore a future soul-targeting capability should generally interact with dedicated soul protection, resistance, immunity, unusual soul nature, Rank/power differences, action timing, positioning, reach/contact, and the attacker's own costs/constraints where appropriate.

Do not implement Jet's personal death/essence condition as a universal tax on all soul-damage Aspects. That condition belongs to Jet's specific Aspect/Flaw/existence package. Other soul-interaction abilities may be balanced in completely different ways.

### UNKNOWN

This note does not claim a universal numeric formula for soul resistance, a fixed conversion between soul damage and bodily health, or that every soul-targeting ability follows Jet's exact mechanics. Those details remain ability-specific unless primary canon establishes otherwise.

## Capabilities should compose

An action may carry multiple properties. For example, a spear thrust can be physical/piercing with reach and penetration; a hardening counter can add a defensive interaction; a phase strike can combine temporary non-interaction with an attack on exit; a short teleport cut can combine teleport displacement with a committed slash; a soul-targeting strike can add a different target layer while still requiring a valid attack action.

These are structural examples, not named Aspect designs.

## Aspects should modify generic actions when possible

Preferred architecture:

```text
Generic action
    Heavy Strike

Capability providers
    learned technique
    physical body
    equipment / Memory
    supernatural provider

Shadow Slave Aspect adapter
    adds hardening
    OR changes displacement to teleport
    OR grants phase interaction
    OR adds a soul-targeting channel
```

Unique Aspects may still need bespoke action logic, but the generic combat framework should not fork into an entirely separate combat implementation for every supernatural ability.

## Different players should have different valid combat answers

The same enemy action might be answered through mundane spacing, learned combat movement, guarding, body hardening, phasing, teleportation, stability/knockback recovery, Echo intervention, or terrain use. The enemy should pose a combat problem rather than dictate one universal response button.

## Counters and matchups matter

A powerful ability should not automatically dominate every encounter. Hardening may excel against compatible physical hits but help little against soul-targeting or sufficiently penetrating attacks. Phasing may defeat one committed physical strike but struggle with persistent zones, prediction, or phase-capable attacks. Teleportation may offer excellent repositioning but be constrained by valid destinations, recovery, resource use, or area denial. Soul damage may bypass ordinary armour while still facing dedicated soul defenses.

This supports scouting, knowledge, matchup adaptation, and build identity rather than a simple rarity ladder.

## Creatures can use the same interaction system

Nightmare Creatures may possess natural armour/hardening, phase behaviour, unusual displacement, specialised physical damage profiles, supernatural target layers, resistances, or weaknesses. The same generic combat interaction system should resolve both player and creature actions.

## Exploration can reveal combat information

Multiple interaction channels make knowledge materially valuable. Ruins, reports, corpses, environmental damage, NPC information, or previous battle sites could reveal that weapons bounce from a shell, armour opens during a particular action, a creature resists one damage profile, a supernatural defense leaves one target layer exposed, or a previous expedition succeeded through a particular Memory/terrain interaction.

This reinforces the wider goal that exploration and combat feed each other.

## Generic engine boundary

The reusable framework may understand generic concepts such as `DamageChannel`, `TargetLayer`, `AttackProperty`, `DefenseResponse`, `InteractionCapability`, `PhaseMask`, `DisplacementType`, `Resistance`, `Penetration`, and `StabilityImpact`.

It should not decide what a Soul is, whether someone possesses an Aspect, what a Memory means, how Rank is awarded, Nightmare appraisal, or permanent progression. Those remain Shadow Slave Java authority.

## Questions deliberately unresolved

This discussion does not settle the final damage taxonomy, penetration/armour formulas, exact soul injury model, separate soul/body health pools, healing, phase-vs-terrain rules, teleport destination validation, resource costs, PvP implications, elemental/status taxonomy, resistance stacking, Rank disparity formulas, or balance timings/cooldowns.

These remain open while the design is being fleshed out.

## Current owner direction preserved by this note

1. Aspects may harden the body so compatible attacks bounce or are rejected.
2. Aspects may temporarily let attacks phase through the character.
3. Aspects may provide short-range teleportation/displacement.
4. Combat should support meaningful damage/interaction types rather than one undifferentiated damage number.
5. Soul-targeting/soul-damage mechanics should be possible and meaningfully distinct from physical damage.
6. These effects should compose with the reusable action/movement framework rather than making the generic engine know Shadow Slave lore objects.
7. Different builds should answer the same enemy action in different valid ways.
8. No runtime development is authorized by this document; it is design capture only.