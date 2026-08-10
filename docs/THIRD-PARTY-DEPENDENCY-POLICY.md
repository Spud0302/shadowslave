# Third-party dependency adoption policy

**Status:** active engineering policy / first audit, 2026-08-10  
**Target:** Minecraft 1.21.1, NeoForge 21.1.244, Java 21  
**Goal:** reuse mature infrastructure without outsourcing Shadow Slave game authority.

## Principle

Do not reinvent mature rendering, animation, AI, equipment, or world-generation infrastructure when a well-maintained library already solves the problem better. Do not adopt a library merely because it is popular.

The Java core remains the authority for:

- Soul/progression state;
- Aspect, Flaw, Attribute, Memory, Echo, Nightmare Creature, scenario, role, resolution and reward identity;
- Nightmare ownership/lifecycle/recovery;
- deterministic generation inputs/results/provenance;
- persistence and migrations.

Third-party libraries may execute or present that state. Removing a presentation/execution dependency must not silently change canonical progression, reroll an identity, grant ownership, resolve a Nightmare, or invent a reward.

## Admission gate

A dependency is eligible only when all of the following are true:

1. **Concrete need:** it replaces a real implementation burden in the current roadmap, not a hypothetical future feature.
2. **Version fit:** a maintained Minecraft 1.21.1 + NeoForge build exists and can be pinned exactly.
3. **License fit:** code license is recorded; asset licensing is reviewed separately. Public source does not imply reusable art/audio/models.
4. **Stable integration surface:** we can wrap it behind a narrow Shadow Slave adapter instead of spreading its API through canonical domain code.
5. **No authority inversion:** the dependency cannot become the source of truth for progression/persistence/rewards.
6. **Packaging proof:** the modpack manifest records the dependency, version, source, license, hash/provenance and required/optional status before release packaging claims it.
7. **Physical gate:** exact dependency state must pass compile/unit/package, NeoForge client boot and dedicated-server boot; world-facing changes also require the relevant smoke/GameTest path.
8. **Exit path:** replacement/removal must be possible without migrating canonical player identity into the dependency's private state.

## Current audit

### APPROVED — GeckoLib 4 (first adoption target)

**Use:** custom Nightmare Creature/Echo/NPC/item models and animation controllers.  
**Current 1.21.1 NeoForge line:** 4.9.2 at audit time.  
**License:** MIT.  
**Official project:** https://github.com/bernie-g/geckolib  
**Official installation docs:** https://github.com/bernie-g/geckolib/wiki/Installation-(Geckolib4)

Why it passes:

- purpose-built rendering/animation engine rather than a content mod;
- NeoForge support and first-party Gradle/Maven instructions;
- Blockbench-oriented workflow matches the custom-model gap in the current alpha;
- supports entity/item animation, keyframes, layers and custom rendering without requiring canonical state ownership;
- permissive code license.

Boundary:

- GeckoLib owns model/animation execution only;
- `NightmareCreatureContentCatalog`, Echo ownership and Memory ownership remain Java authority;
- animation state may be derived from Java/entity execution state but may not decide Rank/Class, reward, damage provenance, ownership or progression.

**First implementation:** replace the hostile Ash Burrower's `SilverfishRenderer` placeholder with a custom GeckoLib model/texture/animation set. The player-owned Ash Burrower Echo should later share the same creature visual identity with an Echo-specific presentation layer rather than remain an Armadillo.

**Adoption rule:** do not add GeckoLib as an unused Gradle dependency. Add it atomically with the first custom renderer/model slice, mod metadata, modpack component/provenance entry and exact CI gate.

### APPROVED FOR SPIKE — SmartBrainLib

**Use:** reusable advanced Nightmare Creature sensing/behaviour once vanilla `Goal` implementations become the limiting factor.  
**Known 1.21.1 NeoForge line:** 1.16.x; 1.16.11 is published for 1.21.1 at audit time.  
**License:** MPL-2.0.  
**Official project:** https://github.com/Tslat/SmartBrainLib

Why it is promising:

- focused library for Minecraft Brain sensors/behaviours instead of a content mod;
- active NeoForge source tree;
- natural fit for authored creature descriptors such as sensing, pursuit, ambush, guard and environmental reactions.

Why it is not yet a required pack dependency:

- the existing alpha creatures still need only bounded behaviour and can use vanilla goals;
- distribution/Maven resolution for our exact 1.21.1 build must be proven in a clean NeoForge spike before the pack depends on it;
- introducing a second framework before one creature demonstrates a real need would increase dependency surface without player benefit.

Boundary:

- SmartBrainLib may execute sensors/tasks;
- Java-owned creature profile and runtime encounter state decide what the creature *is* and which behaviours are legal;
- SmartBrainLib may not own Rank/Class, encounter rewards, Soul interactions, region affinity or persistent canonical state.

**First spike condition:** after the Ash Burrower receives a custom visual identity, implement one authored descriptor that vanilla goals handle poorly (prefer vibration/ambush or bounded burrowing) on an isolated branch and compare complexity/testability against vanilla AI.

### DEFER — TerraBlender

**Use considered:** biome/terrain composition.  
**License:** LGPL-3.0.  
**Official project:** https://github.com/Glitchfiend/TerraBlender

TerraBlender is mature and useful for adding biomes compatibly to Minecraft's terrain system, but the current Dream Realm alpha is a dedicated `shadowslave:dream_realm` slice with Java-owned region identity. Adding TerraBlender now would not remove enough bespoke work to justify another runtime dependency.

Adopt only if we move from bounded/custom-dimension assembly to a terrain pipeline where TerraBlender's region/biome insertion actually replaces code we would otherwise maintain.

Before that point, prefer vanilla/NeoForge datapack registries, worldgen codecs and biome modifiers.

### DEFER / OPTIONAL ADAPTER — Curios API

**Use considered:** wearable/equipped Memory manifestations where an accessory slot is genuinely useful.  
**Current 1.21.1 NeoForge line:** 9.5.1 at audit time.  
**License:** LGPL-3.0.  
**Official project:** https://github.com/TheIllusiveC4/Curios

Curios is mature and solves compatible accessory slots well. It must not become Memory ownership.

If adopted:

- Java `MemoryOwnershipData` remains the soul-owned record;
- a Curios slot is only a physical/equipment manifestation of an already-owned compatible Memory;
- removing Curios must not delete a Memory or alter its provenance;
- only Memories whose gameplay naturally requires persistent wearing should use it. Weapons/tools or summon/dismiss-style Memories should not be forced into Curios.

### DEFER — Veil

**Use considered:** Dream Realm post-processing, custom shaders, framebuffers and advanced Spell/VFX presentation.  
**1.21.1 NeoForge support exists.**  
**License:** LGPL-3.0.  
**Official project:** https://github.com/FoundryMC/Veil

Veil is powerful, but it is a larger rendering dependency and overlaps part of the model/render problem that GeckoLib already solves for us. Do not introduce two rendering frameworks for the same job.

Potential future role:

- region-specific post-processing/fog/color treatment;
- Spell-screen or appraisal visual effects;
- advanced shader-driven supernatural effects.

Do not use Veil as the first custom-model solution. GeckoLib owns that lane unless a concrete blocker is demonstrated.

### NATIVE FIRST — configuration

Do not add Cloth Config/YACL/etc. merely to store settings. NeoForge already provides configuration infrastructure (`ModConfigSpec`/registered configs). Use an external GUI/config library only if a concrete user-facing configuration UI requirement appears that native APIs do not satisfy cleanly.

Canonical gameplay rules must not be mutable client preferences. Configuration is for presentation, development, server policy and explicitly designed tuning only.

### NATIVE FIRST — structures/world data

Minecraft/NeoForge already provide datapack worldgen registries, codecs, conditions and biome modifiers. Use these before adopting a structure/worldgen framework. A third-party structure library must demonstrate a concrete reduction in complexity for Cinder Rest/Dream Realm expansion before admission.

### NATIVE FIRST — sound

Use Minecraft/NeoForge sound registration and resource-pack assets first. We need custom *audio assets* much sooner than we need a custom sound engine. Do not add a sound library until spatial/occlusion/dynamic audio requirements exceed native facilities.

## Code and asset reuse rules

### Depending on a library

Preferred. Pin an exact compatible version, record source/license/provenance, wrap the API narrowly, and test physical client/server startup.

### Copying source code

Exceptional. Before copying any source:

- verify the exact file's license and notice requirements;
- record upstream repository, commit, path and local modifications;
- prefer a small isolated adapter over copied subsystems;
- do not copy code whose license is incompatible with this project/release plan.

### Copying models, textures, sounds, structures or other assets

Treat assets as independently copyrighted even when source code is open. Do not copy another mod's art/audio/model/structure unless its asset license explicitly permits our use or we have direct permission. Record attribution/notice requirements when applicable.

Our default art path is: custom Shadow Slave project assets using mature **tooling/libraries**, not another content mod's art.

## Packaging policy

Current `modpack/manifest.json` has an empty `components` list. The first external runtime dependency therefore changes the pack contract and must update the validated manifest/export pipeline in the same integration wave.

For each runtime component record at minimum:

- stable component ID and mod ID;
- exact Minecraft/NeoForge-compatible version;
- required vs optional;
- canonical download/source location;
- license identifier/link;
- cryptographic hash or equivalent pinned provenance used by the pack builder;
- client/server environment requirement;
- authority role (`presentation`, `execution`, `worldgen`, etc.), explicitly never `canonical_state_owner`.

Do not silently download latest versions at runtime or package time.

NeoForge Jar-in-Jar may be used for an appropriate Java library, but it is not the default for full mod dependencies. Use explicit modpack components when players/servers need the mod as an independently loaded component.

## Near-term adoption order

1. **GeckoLib** — first custom Ash Burrower model/texture/idle-walk-attack animation slice.
2. **SmartBrainLib spike** — only after one authored AI descriptor demonstrates a real advantage over vanilla goals.
3. **Curios optional adapter** — only when a wearable Memory actually needs equipment slots.
4. **Veil** — only after we are ready for Dream Realm/Spell post-processing rather than ordinary entity animation.
5. **TerraBlender** — only if Dream Realm terrain generation evolves into a use case it materially simplifies.

This order deliberately attacks the current alpha's largest presentation deficit without turning the project into a dependency bundle.
