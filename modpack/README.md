# Nightmare Spell modpack prototype

This directory is reserved for Path A: a curated NeoForge modpack that reuses mature mods for generic content while a custom Shadow Slave Java core owns identity, progression and Nightmare lifecycle.

## Purpose

The prototype answers:

- how quickly can existing mods produce convincing Shadow Slave gameplay;
- which generic systems are already good enough to reuse;
- where integrations break immersion or correctness;
- how much custom Java is still required;
- whether a hybrid should become the long-term product.

This is not permission to replace the Shadow Slave architecture with disconnected quest scripts.

## First release target — `modpack-v0.1.0`

The first package must pass the shared Mundane -> Carrier -> First Nightmare -> Sleeper comparison slice.

Expected package outputs:

```text
nightmare-spell-modpack-v0.1.0.mrpack
nightmare-spell-server-v0.1.0.zip
```

A platform manifest is preferred over committing or redistributing dependency JARs.

## Candidate dependency roles

The initial compatibility spike should evaluate a minimal set rather than installing a large kitchen-sink pack.

| Candidate | Proposed use | Boundary |
| --- | --- | --- |
| KubeJS | rapid recipe, event and pack-behaviour iteration | never authoritative for permanent Soul identity |
| LootJS | adapt loot from selected content mods | pack tuning only |
| Iron's Spells 'n Spellbooks | candidate spell visuals, casting and generic magic content | external spell IDs are adapter targets, not Aspect identity |
| Curios API | Memory/accessory-style equipment slots | Soul data records ownership/meaning separately |
| Patchouli | optional guide/codex during prototyping | not the final Soul interface |

Candidate project pages:

- <https://modrinth.com/mod/kubejs>
- <https://modrinth.com/mod/lootjs>
- <https://modrinth.com/mod/irons-spells-n-spellbooks>
- <https://modrinth.com/mod/curios>
- <https://modrinth.com/mod/patchouli>

Do not add all candidates automatically. Each must earn its place through the dependency review.

## Dependency review checklist

Before adding a mod to the published manifest, record:

1. exact Minecraft and NeoForge version;
2. client/server requirement;
3. direct and transitive dependencies;
4. licence;
5. explicit modpack/distribution permission or platform-manifest support;
6. source and issue tracker availability;
7. active maintenance signal;
8. save data written by the mod;
9. behaviour if the mod is later removed;
10. dedicated-server result;
11. performance impact;
12. configuration and scripting surfaces;
13. whether an API is available or integration requires brittle reflection/mixins;
14. replacement plan if the dependency is abandoned.

All-rights-reserved is not automatically disqualifying for a manifest-based private test pack, but it changes how the project may distribute, modify or integrate the dependency. Never copy assets or code without permission.

## Custom core boundary

The modpack depends on the same canonical Java core intended for the standalone track where possible.

The core owns:

```text
SoulData
AspectInstance
FlawInstance
NightmareInstance
ProgressionService
NightmareService
MigrationService
Soul networking/UI contract
```

Compatibility packages translate internal ability/equipment/objective references into dependency-specific mechanics.

Example:

```text
Aspect ability id: shadowslave:dormant/ember_touch
             ↓ compatibility adapter
Iron's Spells spell or effect implementation
```

The save stores `shadowslave:dormant/ember_touch`, not only an opaque external spell ID. If the integration is absent, the identity still loads and the ability reports that its provider is unavailable.

## What KubeJS may own

Good prototype uses:

- recipe removal/replacement;
- loot and structure tuning;
- rapid event experiments;
- hiding or gating dependency content;
- temporary diagnostics;
- pack-specific onboarding messages;
- proving whether a mechanic is fun before implementing it in Java.

KubeJS must not be the only storage location for:

- Spell state;
- Soul Rank;
- generated Aspect/Flaw identity;
- Nightmare ownership;
- migration completion;
- irreplaceable progression history.

## First vertical-slice mapping

### Mundane and Carrier

Implemented by the custom core. No quest mod should be required to detect or store infection.

### First Nightmare

Use one selected content source for environment or creature content, but wrap it in a custom `NightmareScenarioAdapter` so entry, evidence, victory and teardown remain ours.

### Aspect

Map one generated Aspect root to one external spell/effect provider. The Aspect instance remains custom Java data.

### Flaw

Implement one custom burden in the core first. Do not rely on a third-party quest condition to enforce a permanent Flaw.

### Soul screen

Use the custom Java Soul screen. Patchouli may explain the prototype but is not a substitute for live Soul data.

## Suggested repository contents

```text
modpack/
  README.md
  manifest/
  config/
  defaultconfigs/
  kubejs/
    startup_scripts/
    server_scripts/
    client_scripts/
  resourcepacks/
  server-overrides/
  dependency-review/
  test-results/
```

Do not commit downloaded mod JARs unless their licence and repository policy explicitly allow it and there is a strong reason not to use a manifest.

## Comparison discipline

The modpack may contain extra exploration content for testing, but the architecture comparison must score the same shared slice as the standalone mod.

Track separately:

- time spent configuring dependencies;
- time spent writing custom Java;
- time spent fighting compatibility;
- defects caused by integrations;
- content quality gained from dependencies;
- install size and client requirements;
- server performance;
- upgrade difficulty.

The modpack path succeeds if it produces a cohesive Shadow Slave experience substantially faster without surrendering canonical state ownership. It fails if the custom core has to fight every dependency or if progression becomes a fragile collection of scripts and hidden mod state.
