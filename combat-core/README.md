# Combat Core MVP

`combat-core` is a deliberately small NeoForge 1.21.1 mod/JAR that owns generic combat fundamentals only. It exists so ordinary Minecraft combat and Shadow Slave can share one server-authoritative action path without moving Shadow Slave progression or lore concepts into a generic framework.

## Ownership boundary

Combat Core may own:

- action definitions with wind-up, active, and recovery durations;
- per-actor action phase state and commitment/cancellation rules;
- reusable melee reach/arc geometry;
- bounded movement/action reservation signals while an action is committed;
- generic player and mob executor seams;
- generic consumer-owned resolution and presentation hooks.

Combat Core must not know about:

- Souls, Essence, Ranks, Classes, Aspects, Flaws, Attributes;
- Memories, Echoes, Nightmare Creatures, Nightmare Seeds, appraisal, or progression;
- Shadow Slave region/content providers or save authority.

Dependency direction is one-way: Shadow Slave may depend on Combat Core; Combat Core must not depend on Shadow Slave.

## Current MVP state

Implemented fundamentals:

- `CombatActionDefinition` validates one small action contract;
- `CombatActionState` advances `IDLE -> WINDUP -> ACTIVE -> RECOVERY -> IDLE`, exposes commitment/reservation state, permits cancellation only before commitment, and allows one resolution per active window;
- `MeleeGeometry` performs bounded reach/arc admission without importing Shadow Slave concepts;
- `BasicPlayerMeleeExecutor` intercepts ordinary server-side melee against living targets and routes it through a 4-tick wind-up, 1-tick active window, and 6-tick recovery before one bounded resolution attempt;
- `MobActionExecutor<T>` is a deliberately small reusable action seam: the consumer owns target identity, navigation/AI, actual damage or supernatural effects, and presentation while Combat Core owns timing, commitment, movement reservation and active-window consumption;
- `MobActionExecutor.resolveActiveWindow(...)` invokes a consumer-owned effect at most once during the active window;
- `MobActionExecutor.publishPhase(...)` emits presentation-only phase changes without making presentation canonical state;
- focused JUnit tests currently cover phase timing, malformed action definitions, geometry boundaries, and the original generic mob timing seam.

Source implementation is not the same as physical proof. Hosted jobs are currently failing before checkout with no runner allocation, so standalone build, packaging, client/server boot, and ordinary sword-vs-zombie play are not yet claimed successful. Focused hook tests are also still pending because the repository mutation path rejected the test-file update during this slice.

Still required for MVP completion:

- an executing standalone build/test/JAR workflow;
- focused tests for the new consumer resolution/presentation hooks;
- physical client and dedicated-server boot proof;
- physical ordinary sword-vs-zombie timing proof;
- one Shadow Slave player action adapter and one creature/action adapter;
- final duplicate-authority audit after migration.

## Building independently

The directory is its own Gradle project (`settings.gradle`, `build.gradle`, `gradle.properties`). From the repository root it can be built with the repository's existing Gradle wrapper without making it a subproject of `shadow-slave.jar`:

```bash
./mod/gradlew -p combat-core test build
```

The development JAR is produced under `combat-core/build/libs/`.

## Shadow Slave integration rule

A Shadow Slave adapter translates already-authored content into generic Combat Core actions/hooks. For example, Glass Road may provide its own Memory identity and supernatural semantics while Combat Core supplies commitment/timing/geometry. Chainback may retain its creature identity and special-action rules while using the generic action executor seam.

If an integration would require Combat Core to import a Shadow Slave domain type, the boundary is wrong; keep that translation in `shadow-slave.jar`.

## Explicitly deferred

Limb injury, broad hurtbox systems, deep stability/stagger, Soul/Essence economy, broad damage taxonomies, guard/parry trees, advanced movement, skill/build graphs, PvP balance, large weapon catalogues, broad cross-mod compatibility, extensive config/UI, bespoke animation frameworks, and world/progression systems are not MVP acceptance criteria.
