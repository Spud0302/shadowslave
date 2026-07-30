# Java core implementation status

## Package identity

- Minecraft: `1.21.1`
- NeoForge: `21.1.244`
- Java: `21`
- ModDevGradle: `2.0.143`
- Gradle wrapper: `9.2.1`
- Development version: `0.1.0-alpha.1`

These values come from the current official NeoForge 1.21.1 ModDevGradle template rather than a downgraded newer-game workspace.

## Implemented and verified in the first scaffold

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
- committed and wrapper-validated Gradle `9.2.1` build;
- GitHub Actions compile, unit-test and JAR packaging gate;
- dedicated NeoForge server smoke that reaches ready state and confirms the Shadow Slave mod loaded.

## Verification evidence

The accepted CI run performs all of the following from the committed wrapper:

```bash
./mod/gradlew -p mod build
./mod/gradlew -p mod runServerSmoke --no-daemon
```

The build, JUnit suite, JAR packaging, wrapper validation and dedicated-server smoke all passed. The server log contained both Minecraft's ready-state message and:

```text
Shadow Slave Java core is loading
```

## Deliberately not implemented yet

- natural sleep infection hook;
- datapack scoreboard/tag importer;
- client payloads and Soul screen;
- `NightmareRegistryData` and instance lifecycle;
- dimensions, creatures, powers or external-mod adapters;
- public `mod-v0.1.0` release.

The next package adds networking plus a read-only Soul screen, then importer fixtures. Nightmare instances begin only after persistence and synchronization are proven.

## Local build commands

From the repository root:

```bash
./mod/gradlew -p mod build
./mod/gradlew -p mod runClient
./mod/gradlew -p mod runServer --no-daemon
```

No separate Gradle installation is required. Java 21 is required.
