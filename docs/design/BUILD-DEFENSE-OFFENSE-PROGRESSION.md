# Build, Offense, Defense, and Progression Discussion Notes

**Status:** discussion / design capture only.  
**Implementation status:** **NOT STARTED.** This note preserves current owner direction and is not approval to begin runtime implementation.

## Core direction

Combat should support multiple offensive properties and corresponding defensive answers rather than one universal attack stat and one universal armour stat.

An attack can expose several properties at once, for example:

- target layer: body, soul, or another later-justified supernatural layer;
- physical profile: slash, pierce, impact/crush, tearing, etc.;
- penetration / force / stability impact;
- supernatural interaction properties;
- range, reach, area, persistence and displacement.

Defensive capability can answer different subsets of those properties, for example:

- physical armour / toughness;
- slash, pierce or impact resistance;
- penetration resistance;
- stability / knockback resistance;
- soul resistance;
- hardening / rebound interaction;
- guarding / deflection;
- phasing or interaction exclusion;
- mobility-based avoidance;
- recovery skill.

The exact final stat list and formulas remain unresolved.

## Build progression should branch

Players should be able to develop in different directions rather than converging on one complete late-game moveset. Current preference is a layered capability graph rather than one universal RPG point tree.

Progression sources should remain distinct:

1. **Learned / taught techniques** — weapon handling, guard/parry/deflection, movement, knockback recovery, climbing, stance/timing and other mundane competence.
2. **Physical development** — strength, speed, control, endurance, stability and recovery. A stronger body can improve a known technique without automatically teaching it.
3. **Aspect / Attribute capabilities** — unique movement, hardening, phasing, teleportation, soul targeting, unusual resistance and other supernatural interaction rules.
4. **Memories / equipment** — armour profiles, weapon profiles, penetration, soul protection, traversal tools and active effects.
5. **Current tactical loadout** — weapon, Memories, stance, selected actions, terrain, Echo support and preparation.

## Builds should create different answers

Two characters of similar overall power may legitimately specialize differently, for example:

- physical bruiser with heavy armour/stability but poor soul defense;
- evasive fighter with low passive resistance but strong movement/recovery;
- hardening specialist who turns physical commitments into rebound openings;
- soul-protected hunter suited to soul-targeting opponents;
- penetration-focused duelist designed to defeat armour/hardening;
- control fighter focused on stagger, displacement, reach and recovery;
- Aspect-heavy supernatural build with weaker mundane technique;
- highly trained mundane fighter whose broad competence compensates for a less combat-oriented Aspect.

The system should avoid one build becoming complete at everything.

## Offensive progression should also branch

Offense should not mean only increasing raw damage. Possible directions include:

- physical force;
- penetration;
- attack speed / recovery;
- reach and spacing;
- stability damage;
- precision / weak-point exploitation;
- slash / pierce / impact specialization;
- supernatural target-layer access;
- mobility integrated into attacks;
- resource efficiency;
- improved feints, cancels and follow-ups.

## Skill tree vs capability graph

Some progression can still be presented as visible skill trees where appropriate. For example, a mundane combat school could expose teachable branches, while an Aspect has its own fixed/evolving supernatural structure and Memories are acquired externally rather than bought with generic skill points.

Conceptually:

```text
Character build
    learned techniques
    + physical development
    + Aspect / Attributes
    + Memories / equipment
    + tactical loadout
        -> exposed combat capabilities and defenses
```

## Weaknesses should remain meaningful

Players should not be expected to max every resistance. Defensive gaps create matchup danger, reasons to scout or change Memories, value for team composition, value for specialized Aspects, and meaningful preparation before fighting unknown creatures.

A strong character can therefore be excellent in one matchup and uncomfortable in another without simply being weaker overall.

## Exploration should reveal matchup information

Creature remains, damaged armour, expedition notes, NPC testimony, attack marks and environmental evidence may reveal that:

- a shell rejects slashing attacks;
- impact or penetration works better;
- an enemy attacks the soul;
- physical armour is insufficient;
- a creature has extreme stability but poor recovery;
- a hardening/phase interaction has a specific counter.

This keeps exploration, build planning and combat tightly connected.

## Current owner direction preserved

1. Different defensive stats/capabilities should cover different offensive stats/properties.
2. Characters should have genuinely different build directions.
3. Progression should include skill/capability-tree concepts without flattening training, body development, Aspects and Memories into one generic point pool.
4. Players should retain meaningful weaknesses rather than eventually covering every matchup.
5. Offense should branch into penetration, stability, target layer, timing, reach, precision and other interaction properties rather than only raw damage.
6. Build diversity should affect both combat and exploration.
7. Implementation is not started or authorized by this note.
