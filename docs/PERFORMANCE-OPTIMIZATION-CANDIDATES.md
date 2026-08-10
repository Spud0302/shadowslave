# Performance and optimization candidates

**Status:** deferred modpack optimization shortlist / evaluate before alpha packaging  
**Target environment:** Minecraft 1.21.1, NeoForge, Java 21  
**Relationship to `docs/THIRD-PARTY-DEPENDENCY-POLICY.md`:** these are pack-level optimization candidates, not Shadow Slave Java-core dependencies.

## Principle

Do not reinvent mature Minecraft performance work inside the Shadow Slave core.

Performance mods may optimize execution, rendering, memory use, startup, or idle resource consumption, but they must never own or alter canonical Shadow Slave state. Removing an optimization mod must not invalidate a save, reroll an identity, alter Nightmare resolution, change Memory/Echo ownership, or become required to decode persistent state.

This file is a **candidate list for later benchmarking**, not an instruction to install every listed mod now. Exact Minecraft/NeoForge compatibility, current release version, license, source, conflicts, hash/provenance, and maintenance status must be re-verified at adoption time.

## Preferred benchmark candidates

### Lithium — high-priority server/simulation candidate

**Intended role:** game-logic and simulation optimization on server and, where supported, client-integrated simulation.

Evaluate for:

- entity and mob ticking;
- AI/simulation cost;
- physics/block update cost;
- server MSPT under Nightmare Creature load;
- dedicated-server headroom during Nightmare and Dream Realm activity.

Boundary: Lithium is optimization only. Creature Rank/Class, AI legality, rewards, Nightmare state, progression and persistence remain Java-owned.

### FerriteCore — high-priority memory candidate

**Intended role:** reduce Minecraft memory overhead on client and server.

Evaluate for:

- idle client RAM;
- dedicated-server RAM;
- loaded Dream Realm/Nightmare dimension memory;
- memory use with many registered models/items/entities;
- interaction with GeckoLib and other selected optimization mods.

### ModernFix — high-priority startup/memory candidate

**Intended role:** startup, memory and targeted engine fixes/optimizations.

Adoption should start with conservative/default behavior. Do not enable invasive optional patches merely because they exist.

Evaluate for:

- cold startup time;
- development and packaged client startup;
- dedicated-server startup;
- memory use;
- resource loading;
- compatibility with GeckoLib, FerriteCore and the modpack packaging/runtime environment.

Any optional ModernFix feature that materially changes runtime behavior must be benchmarked and documented separately.

### Embeddium — high-priority client rendering candidate

**Intended role:** client rendering/terrain performance.

Evaluate for:

- average FPS and frame-time consistency;
- Ashen Expanse traversal;
- Cinder Rest settlement rendering;
- many visible Nightmare Creatures/Echoes;
- GeckoLib custom models and animations;
- future Dream Realm fog/VFX/shader integrations.

This must remain client-side optimization. The server and canonical Java core must not require it.

### ImmediatelyFast — high-priority client rendering/UI candidate

**Intended role:** optimize immediate-mode rendering and related client rendering paths.

Evaluate alongside Embeddium rather than assuming the gains stack cleanly.

Important test surfaces:

- entity-heavy scenes;
- HUD/Spell presentation;
- inventory/Soul/Memory presentation;
- GeckoLib creatures;
- particles and future supernatural VFX.

## Optional candidates

### Entity Culling — conditional client optimization

Potentially useful once Cinder Rest, settlements, Nightmare Creatures, Echoes and decorative world objects create larger visible entity counts.

Do not make it a default until the following compatibility matrix is green:

- hostile Ash Burrower custom GeckoLib model;
- Ash Burrower Echo manifestation;
- Chainback;
- Drowned Listener;
- settlement NPCs;
- Spell/Memory/Echo particles or special renderers;
- future Veil/post-processing work if adopted.

If a legitimate supernatural effect/entity is incorrectly culled, prefer a documented compatibility rule/whitelist over weakening Shadow Slave state or presentation semantics.

### Dynamic FPS — optional client quality-of-life

Useful for lowering CPU/GPU/resource use while Minecraft is unfocused, minimized, idle, or otherwise not actively being played.

Treat as optional convenience rather than an active-gameplay performance dependency. It should not be required by the pack/server.

## Not planned

### OptiFine

Do not plan around OptiFine as the performance/rendering foundation for this modpack.

Reason: the project should prefer narrow, independently testable NeoForge-compatible optimization components instead of coupling rendering and many unrelated patches into one opaque requirement. Revisit only if the ecosystem changes enough to justify a new compatibility audit.

## Benchmark methodology

Do not compare a heavily modified pack against an unrelated vanilla result and call it proof. Establish a repeatable Shadow Slave baseline and add optimization mods incrementally.

Suggested sequence:

1. NeoForge + Shadow Slave required runtime dependencies only;
2. + Lithium;
3. + FerriteCore;
4. + ModernFix;
5. + Embeddium on client;
6. + ImmediatelyFast on client;
7. optionally + Entity Culling;
8. optionally + Dynamic FPS.

Where ordering or interactions matter, also test the intended final combination rather than relying only on isolated results.

Record at minimum:

- cold client startup time;
- cold dedicated-server startup time;
- client idle RAM after a fixed settling period;
- server idle RAM after a fixed settling period;
- client average FPS plus frame-time/1% low where tooling permits;
- server MSPT/TPS under repeatable load;
- dimension-transition duration;
- chunk-generation/loading pressure;
- crash, log error and mixin/conflict evidence.

## Shadow Slave benchmark scenes

Use repeatable project-specific workloads rather than generic screenshots.

### Nightmare lifecycle

- enter a First Nightmare;
- run a representative scenario path;
- transition out through successful completion;
- verify appraisal/reward presentation;
- verify no optimizer changes persistence/restart results.

### Dream Realm traversal

- enter the Ashen Expanse;
- traverse all current landmarks/resource hooks;
- move through Cinder Rest;
- interact with the Watch Captain;
- repeat entry/exit to expose loading or memory regressions.

### Creature load

Benchmark bounded scenes with approximately:

- 1 creature;
- 25 creatures;
- 50 creatures;
- 100 creatures where the machine/server can safely sustain the test.

Include a mixture of Ash Burrower, Chainback and Drowned Listener once their current presentation lineage is merged.

Measure both visible rendering cost and server-side ticking/AI cost separately.

### Echo execution

Exercise Ash Burrower Echo:

- summon/dismiss;
- FOLLOW;
- HOLD;
- GUARD_POINT;
- guard combat once merged;
- CARRY/unload once merged.

Optimization must not change the Java-owned command, cargo, manifestation or ownership result.

### Presentation compatibility

After custom presentation work lands, explicitly test:

- GeckoLib idle/walk/swim/attack animations;
- project textures/models;
- Memory item rendering;
- NPC rendering;
- Spell HUD/chat/particles;
- any future shader/post-processing layer.

## Adoption gate

Before any candidate becomes part of the normal pack:

1. re-verify exact Minecraft 1.21.1 + NeoForge support;
2. record current version, source and license;
3. classify client/server requirement;
4. pin deterministic download/provenance and hash in the validated modpack manifest;
5. prove it does not own canonical state;
6. run physical NeoForge client and dedicated-server gates as applicable;
7. run the benchmark matrix against the no-optimizer baseline;
8. keep it only if it gives measurable value or solves a concrete stability/resource problem;
9. document known incompatibilities/config deviations;
10. prove removing it does not make existing Shadow Slave saves undecodable.

## Initial intended pack tiers

**Likely core optimization tier, pending benchmark:** Lithium, FerriteCore, ModernFix.  
**Likely client performance tier, pending benchmark:** Embeddium, ImmediatelyFast.  
**Optional/conditional:** Entity Culling, Dynamic FPS.  
**Not planned:** OptiFine.

These classifications are intentionally provisional. Benchmark evidence and compatibility with the actual Shadow Slave alpha take precedence over reputation or popularity.