# Nightmare Spell modpack prototype

This directory is the deterministic packaging boundary for Path A: a curated NeoForge modpack that reuses mature infrastructure while the custom Shadow Slave Java core owns identity, progression and Nightmare lifecycle.

## Current status

The manifest pins Minecraft `1.21.1`, NeoForge `21.1.244`, Java `21`, the locally built Shadow Slave core, and two required non-authoritative runtime providers:

- GeckoLib `4.9.2` for custom model/animation presentation;
- SmartBrainLib `1.16.11` for bounded creature AI execution.

Neither provider owns Soul state, Nightmare Creature identity, progression, rewards, persistence, Memory/Echo ownership, Rank/Class, or Nightmare lifecycle. The manifest records each exact artifact, licence, SHA-256 and removal behavior.

The exporter packages the exact supplied core JAR plus every required runtime component, records per-entry SHA-256 and size provenance, sorts every archive entry and fixes ZIP timestamps and file modes. Identical inputs therefore produce byte-identical archives.

This is still a development package, not a public release or launcher-format modpack.

Validate with:

```bash
python3 modpack/tools/validate_manifest.py
python3 -m unittest discover -s modpack/tests -v
```

Build the current archive after compiling the Java core and fetching the exact pinned runtime components:

```bash
./mod/gradlew -p mod build

mkdir -p build/components
curl --fail --location --silent --show-error \
  'https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/software/bernie/geckolib/geckolib-neoforge-1.21.1/4.9.2/geckolib-neoforge-1.21.1-4.9.2.jar' \
  --output build/components/geckolib-neoforge-1.21.1-4.9.2.jar
printf '%s  %s\n' \
  '5e548466af9ab6aca7a91a7c7d4dc0dc8bc385e22958aed5da0e7bebd0fa3fba' \
  'build/components/geckolib-neoforge-1.21.1-4.9.2.jar' | sha256sum --check --strict

curl --fail --location --silent --show-error \
  'https://dl.cloudsmith.io/public/tslat/sbl/maven/net/tslat/smartbrainlib/SmartBrainLib-neoforge-1.21.1/1.16.11/SmartBrainLib-neoforge-1.21.1-1.16.11.jar' \
  --output build/components/SmartBrainLib-neoforge-1.21.1-1.16.11.jar
printf '%s  %s\n' \
  '68036561cc5511766d54cc0deabc3fc3a5e68f9e3db2478f2574ec82b494374b' \
  'build/components/SmartBrainLib-neoforge-1.21.1-1.16.11.jar' | sha256sum --check --strict

python3 modpack/tools/build_package.py \
  --core-jar mod/build/libs/shadowslave-0.1.0-preview.2.jar \
  --component-jar geckolib-4=build/components/geckolib-neoforge-1.21.1-4.9.2.jar \
  --component-jar smartbrainlib-1=build/components/SmartBrainLib-neoforge-1.21.1-1.16.11.jar \
  --output build/nightmare-spell-modpack-dev.zip
```

Use the exact produced core JAR path rather than relying on the manifest glob when more than one development artifact is present. Required component JARs are never downloaded implicitly by the package builder; callers must supply the exact hash-checked artifacts declared by the manifest.

## Deterministic archive contract

The package currently contains:

```text
README.md
manifest.json
mods/SmartBrainLib-neoforge-1.21.1-1.16.11.jar
mods/geckolib-neoforge-1.21.1-4.9.2.jar
mods/shadowslave-core.jar
provenance.json
```

`provenance.json` records the pack ID/version and SHA-256 plus byte size for every non-provenance entry. It deliberately omits timestamps, host paths and build-machine metadata so it remains reproducible.

The archive writer uses:

- lexicographically sorted entries;
- a fixed `1980-01-01 00:00:00` ZIP timestamp;
- fixed regular-file permissions;
- deterministic JSON formatting;
- atomic replacement of the requested output;
- fail-closed rejection of missing core/component JARs, hash mismatches, unsafe/noncanonical paths and generated-path collisions.

The ZIP is a reviewable repository package, not yet a Modrinth `.mrpack`. Platform-specific export should be added only after redistribution and launcher rules are established.

## Manifest contract

The shared Java core is the sole canonical state owner. Every external component must declare:

- stable component and mod IDs;
- exact version and side;
- role: execution provider, presentation provider, infrastructure or content;
- whether it is required;
- source project/file identity and SHA-256;
- licence;
- explicit removal behaviour;
- `owns_canonical_state: false`.

The validator rejects duplicate IDs, missing provenance, malformed hashes, unsafe paths, unsorted package inputs and any external component that claims canonical state ownership.

## Purpose

The prototype answers:

- how quickly mature infrastructure can improve convincing Shadow Slave gameplay;
- which generic systems are good enough to reuse;
- where integrations break immersion or correctness;
- how much custom Java and custom art are still required;
- whether a hybrid should become the long-term product.

This is not permission to replace the Shadow Slave architecture with disconnected quest scripts or dependency-owned progression.

## First release target — `modpack-v0.1.0`

The first package must pass the shared Mundane -> Carrier -> First Nightmare -> Sleeper comparison slice.

Expected future outputs:

```text
nightmare-spell-modpack-v0.1.0.mrpack
nightmare-spell-server-v0.1.0.zip
```

A platform manifest is preferred over committing dependency JARs to source control.

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
Nightmare Creature / Memory / Echo identity
progression and appraisal
migration and history
Soul networking/UI contract
```

Compatibility packages may translate internal ability, equipment, creature or objective references into dependency-specific execution. The save must retain the Shadow Slave identity, not only an opaque external ID. If a provider is absent, canonical identity must still load and report that execution/presentation is unavailable.

## Architecture and lore boundary

This package exporter is Minecraft **DESIGN** and repository build infrastructure. It introduces no Shadow Slave lore mechanic.

External mods may provide presentation, generic execution, infrastructure or content, but their removal must not make canonical state undecodable or reroll generated identities. SmartBrainLib may schedule sensing, activities, path targets and melee for admitted executors, but the project still decides what those senses/behaviours mean and whether they are legal.

## Deliberate limits

- GeckoLib and SmartBrainLib are the only required external runtime components currently selected;
- SmartBrainLib is admitted first for the hostile Ash Burrower; other creatures are not migrated mechanically and must independently justify the abstraction;
- dependency downloads are explicit and hash-checked rather than performed by the package builder;
- no `.mrpack` is generated;
- no public release is claimed;
- the core JAR is supplied from a local Gradle build rather than copied into source control;
- package reproducibility is proven for identical input bytes, not across different compiler/toolchain outputs.
