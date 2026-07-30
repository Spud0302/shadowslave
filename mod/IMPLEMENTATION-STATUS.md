# Java core implementation status

## Package identity

- Minecraft: `1.21.1`
- NeoForge: `21.1.244`
- Java: `21`
- ModDevGradle: `2.0.143`
- Development version: `0.1.0-alpha.1`

These values come from the current official NeoForge 1.21.1 ModDevGradle template rather than a downgraded newer-game workspace.

## Implemented in the first scaffold

- loadable `@Mod("shadowslave")` entry point;
- immutable, schema-versioned `SoulData`;
- explicit `SpellState` and `SoulRank` types;
- codec-backed attachment persisted on players and copied across death;
- `SoulService` as the only application-facing mutation boundary;
- pure, unit-tested transition rules;
- operator smoke-test commands:
  - `/shadowslave soul`
  - `/shadowslave infect`
  - `/shadowslave complete_first_nightmare_test`
  - `/shadowslave reset`
- GitHub Actions compile, unit-test and JAR packaging gate.

## Deliberately not implemented yet

- natural sleep infection hook;
- datapack scoreboard/tag importer;
- client payloads and Soul screen;
- `NightmareRegistryData` and instance lifecycle;
- dimensions, creatures, powers or external-mod adapters;
- public `mod-v0.1.0` release;
- dedicated-server boot evidence.

The next package should add networking plus the read-only Soul screen, then importer fixtures. Nightmare instances begin only after persistence and sync are proven.

## Build commands

The CI job provisions Gradle `9.2.1` and runs:

```bash
gradle -p mod build
```

The official wrapper scripts/JAR will be committed after the first successful Gradle build regenerates and verifies them. Until then, use a local Gradle 9.2.1 installation or the CI workflow.
