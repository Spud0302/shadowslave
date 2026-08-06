# Nightmare Spell modpack prototype

This directory is the deterministic packaging boundary for Path A: a curated NeoForge modpack that reuses mature mods for generic content while the custom Shadow Slave Java core owns identity, progression and Nightmare lifecycle.

## Current status

A validated dependency-free manifest shell now exists. It pins Minecraft `1.21.1`, NeoForge `21.1.244` and Java `21`, references the locally built Shadow Slave core, and defines the metadata every future external component must provide.

This is not yet a playable modpack or public package. No external dependency, downloaded JAR, launcher export or compatibility adapter is included.

Validate with:

```bash
python3 modpack/tools/validate_manifest.py
python3 -m unittest discover -s modpack/tests -v
```

## Manifest contract

The shared Java core is the sole canonical state owner. Every future external component must declare:

- stable component and mod IDs;
- exact version and side;
- role: execution provider, presentation provider, infrastructure or content;
- whether it is required;
- source project/file identity and SHA-256;
- licence;
- explicit removal behaviour;
- `owns_canonical_state: false`.

The validator rejects duplicate IDs, missing provenance, malformed hashes, unsorted package inputs and any external component that claims canonical state ownership.

## Purpose

The prototype answers:

- how quickly existing mods can produce convincing Shadow Slave gameplay;
- which generic systems are good enough to reuse;
- where integrations break immersion or correctness;
- how much custom Java is still required;
- whether a hybrid should become the long-term product.

This is not permission to replace the Shadow Slave architecture with disconnected quest scripts.

## First release target — `modpack-v0.1.0`

The first package must pass the shared Mundane -> Carrier -> First Nightmare -> Sleeper comparison slice.

Expected future outputs:

```text
nightmare-spell-modpack-v0.1.0.mrpack
nightmare-spell-server-v0.1.0.zip
```

A platform manifest is preferred over committing or redistributing dependency JARs.

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

## Custom core boundary

The core owns:

```text
SoulData
AspectInstance
FlawInstance
NightmareInstance
progression and appraisal
migration and history
Soul networking/UI contract
```

Compatibility packages may translate internal ability, equipment or objective references into dependency-specific mechanics. The save must retain the Shadow Slave identity, not only an opaque external ID. If a provider is absent, canonical identity must still load and report that execution is unavailable.

## Architecture and lore boundary

This manifest shell is Minecraft **DESIGN** and repository packaging infrastructure. It introduces no Shadow Slave lore mechanic.

External mods may provide presentation, generic execution, infrastructure or content, but their removal must not make canonical state undecodable or reroll generated identities.

## Deliberate limits

- no external mods are selected;
- no launcher export is generated;
- no download occurs during validation;
- no public release is claimed;
- the core JAR is referenced as a local Gradle build artifact rather than copied into source control.
