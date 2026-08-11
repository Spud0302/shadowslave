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

## Commitment points and mutually exclusive paths

Some branches should be allowed to **lock, suppress, or make competing branches prohibitively expensive**. This can make character identity persistent instead of letting every sufficiently old character eventually collect every answer.

The preferred distinction is between:

- **baseline competence** — core survival/interaction abilities that should remain broadly recoverable;
- **specialization choices** — mutually exclusive or strongly competing paths that define how the character solves problems;
- **Aspect-locked identity** — supernatural capabilities that may be inherently unavailable because the character's Aspect developed in another direction;
- **loadout choices** — reversible choices created by Memories, weapons and current equipment;
- **training commitments** — learned styles that may be possible to retrain later, but only through meaningful time/cost rather than an instant free respec.

Examples of possible exclusive choices, all still DESIGN rather than final mechanics:

```text
Defensive school
    Brace / Rooted
        -> stronger guard, stability and rebound
        -> locks or heavily penalizes Evasive Flow mastery

    Evasive Flow
        -> faster recovery, step transitions and repositioning
        -> gives up the deepest rooted-guard bonuses
```

```text
Weapon specialization
    Heavy commitment
        -> force, penetration, stability damage
        -> slower recovery / fewer cancel options

    Precision commitment
        -> weak-point access, timing, redirects
        -> lower brute-force ceiling
```

A supernatural progression fork could be even more absolute if lore/design supports it: an Aspect evolution that becomes exceptional at body hardening may permanently forgo a hypothetical phase-oriented development path. This should be authored per Aspect rather than imposed as one universal tree.

### Aspect-driven topology changes

An Aspect should be allowed to change **which branches are compatible at all**, not merely add bonuses inside an otherwise fixed universal tree.

This means an Aspect may create an exception to an ordinary training conflict. Two specializations that are normally mutually exclusive could become simultaneously attainable because the Aspect makes the combination physically or supernaturally coherent. The cost of that exceptional combination does **not** need to be a mirror-image penalty inside the same branch. Instead, the Aspect's nature may cap, suppress, or completely close a different family elsewhere in the character's build graph.

Conceptual example:

```text
Ordinary character
    Rooted Guard <-> Evasive Flow
    choose one peak specialization

Aspect-shaped character
    Aspect affinity bridges Rooted Guard + Evasive Flow
        -> both peak defensive styles can coexist

    but Aspect incompatibility closes another domain
        -> for example, no deep phase-defense branch
        -> or no peak ranged/channeling branch
        -> or no advanced soul-ward specialization
```

The specific locked domain must make sense for that Aspect's identity. It should not be assigned merely to satisfy a gamey "one buff requires one nerf" equation.

An Aspect can therefore expose three kinds of build-graph effects:

- **affinity** — lowers requirements, improves synergy, or allows normally conflicting branches to coexist;
- **incompatibility** — caps or locks techniques/capabilities that contradict the Aspect's nature;
- **transformation** — replaces a normal branch with an Aspect-specific version instead of simply buffing or blocking it.

This creates characters whose build possibilities are structurally different before individual specialization choices are even made.

### Aspect effects should not erase player choice

An Aspect changing the graph should create new choices, not predetermine the entire build. If an Aspect allows two normally exclusive defensive branches, the player may still need to invest training, mastery, physical development, or resources to obtain both. The Aspect grants **compatibility**, not automatic mastery.

Likewise, a blocked supernatural path should not imply that all practical answers to that threat disappear. Memories, allies, terrain, generic technique, preparation, and alternate tactics can still compensate for a permanent specialization gap.

### Lockouts should create identity, not traps

A commitment should usually lock **peak specialization**, not make ordinary gameplay impossible. A rooted defender can still move. An evasive fighter can still wear armour. A physical specialist can still defend against soul threats through Memories, allies, scouting or avoidance even if they cannot personally master every soul-defense branch.

The aim is:

> choosing one answer makes another answer meaningfully less available, while the game still provides alternative ways to survive the matchup.

### Respecialization should depend on the source

Not every layer should respec the same way:

- tactical loadout: freely/reasonably changeable outside immediate combat constraints;
- Memories/equipment: changeable by acquiring/equipping different tools;
- learned technique: retrainable, but potentially slow/costly and not during combat;
- physical development: possibly redirectable over time rather than instantly reset;
- Aspect/Attribute evolution: potentially permanent or only alterable through exceptional lore-justified events;
- innate identity/Flaw: should not be treated as a normal skill-tree respec target.

Exact respec rules remain unresolved.

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
7. Some specialization paths may be mutually exclusive or contain commitment points that lock competing peak options.
8. Lockouts should define identity without removing baseline survivability or all alternate counters.
9. Respec/retraining rules should depend on the progression source rather than one universal reset button.
10. Aspects may reshape the capability graph itself: opening normally exclusive combinations, closing different domains, or replacing mundane branches with Aspect-specific variants.
11. Aspect affinity grants compatibility, not automatic mastery; investment and player choice should still matter.
12. Aspect incompatibilities should follow the Aspect's nature rather than a universal one-bonus/one-penalty balancing rule.
13. Implementation is not started or authorized by this note.
