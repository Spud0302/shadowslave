# Combat Core MVP

`combat-core` is a deliberately small NeoForge 1.21.1 mod/JAR that owns generic combat fundamentals only. It exists so ordinary Minecraft combat and Shadow Slave can share one server-authoritative action path without moving Shadow Slave progression or lore concepts into a generic framework.

## Ownership boundary

Combat Core may own:

- action definitions with wind-up, active, and recovery durations;
- per-actor action phase state and commitment/cancellation rules;
- reusable melee reach/arc geometry;
- bounded movement/action reservation signals while an action is committed;
- generic player and mob executor seams;
- generic damage-resolution and presentation hooks.

Combat Core must not know about:

- Souls, Essence, Ranks, Classes, Aspects, Flaws, Attributes;
- Memories, Echoes, Nightmare Creatures, Nightmare Seeds, appraisal, or progression;
- Shadow Slave region/content providers or save authority.

Dependency direction is one-way: Shadow Slave may depend on Combat Core; Combat Core must not depend on Shadow Slave.

## Current MVP state

Implemented pure fundamentals:

- `CombatActionDefinition` validates one small action contract;
- `CombatActionState` advances `IDLE -> WINDUP -> ACTIVE -> RECOVERY -> IDLE`, exposes commitment/reservation state, permits cancellation only before commitment, and allows one resolution per active window;
- `MeleeGeometry` performs bounded reach/arc admission without importing Shadow Slave concepts;
- focused JUnit tests cover phase timing, malformed action definitions, and geometry boundaries.

Not yet implemented and therefore not claimed playable:

- ordinary sword input/execution against a zombie;
- mob executor integration;
- damage/presentation hooks;
- Glass Road or Chainback adapters;
- physical client/server boot proof.

## Building independently

The directory is its own Gradle project (`settings.gradle`, `build.gradle`, `gradle.properties`). From the repository root it can be built with the repository's existing Gradle wrapper without making it a subproject of `shadow-slave.jar`:

```bash
./mod/gradlew -p combat-core test build
```

The development JAR is produced under `combat-core/build/libs/`.

## Shadow Slave integration rule

A Shadow Slave adapter translates already-authored content into generic Combat Core actions/hooks. For example, Glass Road may provide its own Memory identity and supernatural damage semantics while Combat Core supplies commitment/timing/geometry. Chainback may retain its creature identity and special-action rules while using a generic mob action executor seam.

If an integration would require Combat Core to import a Shadow Slave domain type, the boundary is wrong; keep that translation in `shadow-slave.jar`.

## Explicitly deferred

Limb injury, broad hurtbox systems, deep stability/stagger, Soul/Essence economy, broad damage taxonomies, guard/parry trees, advanced movement, skill/build graphs, PvP balance, large weapon catalogues, broad cross-mod compatibility, extensive config/UI, bespoke animation frameworks, and world/progression systems are not MVP acceptance criteria.
