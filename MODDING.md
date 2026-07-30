# From datapack to Java mod and modpack

The datapack phase is complete. Java-era development compares two delivery paths that share one
canonical Soul/Nightmare core.

See [PROJECT-STATUS.md](PROJECT-STATUS.md) for the current gate.

## Frozen reference

- product: vanilla datapack;
- release: `datapack-v1.0.0`;
- asset: `shadowslave-v1.0.0.zip`;
- purpose: behavioural/import compatibility reference, not a Java architecture constraint.

## Path B — standalone/shared Java core

Location: [`mod/`](mod/)

Current version: `0.1.0-alpha.4`.

Implemented: persistence, lore-aligned schema, networking, O-key Soul screen, schema migration and
pure datapack translation fixtures. CI is green and Claude's independent verification is complete —
Issue #16 is closed. Human walkthroughs are deferred evidence (**D2**), not gates.

## Path A — Nightmare Spell modpack

Location: [`modpack/`](modpack/)

Current state: design only. No dependency JARs, manifest, adapters or public package have been
committed. The modpack will consume the same Java core; scripts and dependencies must never become
canonical Soul or Nightmare storage.

## Current ordering

```text
datapack-v1.0.0 released and frozen
        ↓
shared Java core alpha.4
        ↓
Claude verification gate (#16)
        ↓
live datapack import + persistence verification
        ↓
persistent Nightmare registry / instance ownership
        ↓
one lore-shaped playable Java First Nightmare
        ↓
modpack implementation of the same slice
        ↓
evidence-based standalone/modpack/hybrid decision
```

## Toolchain

- Minecraft Java Edition 1.21.1;
- NeoForge 21.1.244;
- **JDK** 21 — a JRE is not enough: NeoForm recompiles Minecraft sources with `javac`, and without it
  the build fails in `:createMinecraftArtifacts` with the misleading `error: release version 21 not supported`;
- Gradle wrapper 9.2.1;
- dedicated-server compatibility from the beginning;
- server-authoritative state.

## Product-qualified tags

```text
datapack-v1.0.0
mod-v0.1.0
modpack-v0.1.0
```

Historical ordinary `v1.x` prototype tags remain untouched.

## Start here

1. [Current project status](PROJECT-STATUS.md)
2. [Lore-aligned Java model](docs/JAVA-LORE-ALIGNMENT.md)
3. [Datapack migration/lifecycle handoff](docs/JAVA-HANDOFF.md)
4. [Two-track transition plan](docs/MOD-TRANSITION-PLAN.md)
5. [Shared acceptance specification](shared-test-spec/VERTICAL-SLICE.md)
6. [Java implementation status](mod/IMPLEMENTATION-STATUS.md)
