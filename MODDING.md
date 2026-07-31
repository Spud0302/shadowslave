# From datapack to Java mod and modpack

The datapack phase is complete. Java-era development compares two delivery paths that share one server-authoritative Soul/Nightmare core.

See [PROJECT-STATUS.md](PROJECT-STATUS.md) for the canonical current state.

## Frozen reference

- product: vanilla datapack;
- release: `datapack-v1.0.0`;
- asset: `shadowslave-v1.0.0.zip`;
- purpose: behavioural/import compatibility reference, not a Java architecture constraint.

## Path B — standalone/shared Java core

Location: [`mod/`](mod/)

Current preview: `0.1.0-preview.1` on draft PR #19.

Implemented:

- persistent lore-aligned Soul and revealed-identity data;
- server-owned networking and expanded O-key Soul screen;
- transactional live datapack import with verification and rollback;
- persistent `NightmareRegistryData` and per-player instance ownership;
- bundled Nightmare dimension and separate scenario slots;
- one playable DESIGN First Nightmare, **The Last Signal**;
- fixed DESIGN Aspect, ability, Flaw, and appraisal sufficient for a coherent vertical slice;
- install guide, provenance, test matrix, lore ledger, and future Nightmare/Seed roadmap.

The final automated checkpoint is green. Andrew's full playthrough and Claude's bulk review remain pending. This is not a public mod release.

## Path A — Nightmare Spell modpack

Location: [`modpack/`](modpack/)

Current state: design only. No dependency JARs, manifest, adapters, or public package have been committed. The modpack will consume the same Java core; scripts and dependencies must never become canonical Soul or Nightmare storage.

## Current ordering

```text
datapack-v1.0.0 released and frozen
        ↓
Java alpha.4 foundation independently verified
        ↓
Java preview.1: live import + persistent Nightmare + playable slice
        ↓
Andrew play feedback and Claude bulk review
        ↓
evidence-backed preview fixes
        ↓
ResolutionGraph / multi-ending Nightmare work under renewed lore review
        ↓
modpack implementation of the same accepted slice
        ↓
evidence-based standalone/modpack/hybrid decision
```

Future Nightmare and Seed work must begin with [`docs/NIGHTMARE-SEED-ROADMAP.md`](docs/NIGHTMARE-SEED-ROADMAP.md). Completion is terminal central-conflict resolution plus separate per-challenger outcome/appraisal—not a universal boss death, timer, or objective click.

## Toolchain

- Minecraft Java Edition 1.21.1;
- NeoForge 21.1.244;
- **JDK** 21;
- Gradle wrapper 9.2.1;
- dedicated-server compatibility;
- server-authoritative state;
- marker-based client/server smokes through `mod/verify-smoke.sh`.

A JRE is not enough: NeoForm recompiles Minecraft sources with `javac`.

## Product-qualified tags

```text
datapack-v1.0.0
mod-v0.1.0
modpack-v0.1.0
```

`0.1.0-preview.1` is an untagged development artifact, not a release.

## Start here

1. [Current project status](PROJECT-STATUS.md)
2. [Preview play guide](mod/PREVIEW-PLAY-GUIDE.md)
3. [Preview lore ledger](docs/PREVIEW-LORE-DECISIONS.md)
4. [Nightmare and Seed roadmap](docs/NIGHTMARE-SEED-ROADMAP.md)
5. [Java implementation status](mod/IMPLEMENTATION-STATUS.md)
6. [Two-track transition plan](docs/MOD-TRANSITION-PLAN.md)
7. [Shared acceptance specification](shared-test-spec/VERTICAL-SLICE.md)
