# NeoForge GameTest recovery path

**Date:** 2026-08-11  
**Tracks:** #34

## Why this slice exists

Issue #34's stronger successful-completion proof has been intentionally blocked: the repository had no demonstrated automated way to obtain a real server-side `ServerPlayer`-compatible actor inside a NeoForge test runtime and then drive production completion recovery. Repeating codec-only restart tests would not close that evidence gap.

A credible new approach is narrow enough to validate independently before attempting recovery semantics:

- the repository already configures NeoForge's `gameTestServer` run and enables the `shadowslave` GameTest namespace;
- NeoForge documents `runGameTestServer` as the Gradle execution path for Game Tests and says the Game Test Server returns an exit code derived from required failed tests;
- NeoForge exposes `FakePlayer`, a `ServerPlayer` implementation intended to simulate server-side player actions, through `FakePlayerFactory`.

This does **not** prove that FakePlayer is equivalent to a real network login, that it participates in `PlayerList` persistence identically, or that a fake player can cross two server JVMs without additional harness work. Those remain questions for the next bounded probe rather than assumptions in this infrastructure change.

## Initial hosted failure and correction

Preview Gates #292 / run `31485419930` on original #268 head `8b1e1611a1be3dbbfa70368c02cd96245c766b6b` produced useful deterministic evidence instead of reaching the GameTest server:

- the datapack job passed completely;
- Java trigger/JDK/wrapper/cache setup passed;
- Gradle configuration failed before compilation at `mod/build.gradle:71`;
- exact error: `Could not find method setForceExit() for arguments [false] on Run[gameTestServer] of type net.neoforged.moddevgradle.dsl.RunModel`;
- the GameTest, physical-client, same-world restart and artifact steps were consequently skipped.

The root cause is a tooling-family mismatch. The repository uses `net.neoforged.moddev` / ModDevGradle `2.0.143`. NeoForge's 1.21.1 Game Test documentation places `setForceExit false` inside a caution specifically about **NeoGradle**. ModDevGradle's documented `RunModel` surface supports the `gameTestServer` run type, system properties, arguments and related launch configuration, but does not expose `setForceExit`.

The corrected branch therefore:

1. keeps the existing ModDevGradle-compatible `gameTestServer { type = 'gameTestServer' ... }` configuration;
2. removes the unsupported NeoGradle-only `setForceExit false` call;
3. keeps `./mod/gradlew -p mod runGameTestServer --no-daemon --stacktrace` in the Java Preview Gates job;
4. updates `test_preview_gates_workflow.py` to require the GameTest run type, enabled namespace and hosted Gradle invocation while explicitly rejecting `setForceExit` in that ModDevGradle block.

This correction is a credible new approach to the first failure, so one fresh exact-head run is warranted. If the corrected run fails for a different GameTest/runtime reason, diagnose that reason rather than reverting the gate or assuming FakePlayer viability.

## Relationship to current persistence work

PR #262 exact head `e75bb76436d5d0338f29a4ebe8cf5f807f4c4c09` passed Preview Gates #287 / run `31480946729`, including both the full Java job and deployed frozen-datapack harness, and has no inline review threads. That supplies the new evidence needed to consider the Mineflayer transport correction valid, but repository workflow rules still reserve merging to `main` for a human. PR #259 therefore should not be blindly rerun on its unchanged branch until the transport correction is actually present in its tested ancestry or another credible new condition appears.

This GameTest slice is independent of that merge dependency and advances the previously blocked stronger Java recovery lane instead of adding another disk-image variant.

## Evidence classification

- **CANON:** not applicable. No Shadow Slave lore mechanic, outcome, reward, appraisal, death, progression, Memory, Echo, Attribute, Aspect or Flaw behavior changes.
- **INFERRED:** none.
- **DESIGN:** NeoForge GameTest and FakePlayer are technical test infrastructure candidates for exercising production server-side recovery code; CI should execute the ModDevGradle-supported GameTest server task without importing a NeoGradle-only DSL method.
- **UNKNOWN:** whether FakePlayer enters the same login-event path as a networked player; whether it is automatically tracked/saved by `PlayerList`; whether it can reproduce return-position persistence; whether the useful recovery probe can be carried across two real server JVMs; and whether later GameTest APIs need an explicit template/registration fixture.
- **COMPATIBILITY:** runtime gameplay, persistent schemas, catalogues, dependencies, datapack behavior and lore-facing semantics are unchanged. This only adds hosted execution of an already-configured NeoForge test run and its workflow contract.

## Source boundary

This is technical infrastructure, so no new novel proposition requires primary-text research. `docs/LORE-SOURCE-POLICY.md` remains controlling if a later test reveals that runtime behavior itself needs a lore-sensitive change. `docs/JAVA-LORE-ALIGNMENT.md` explicitly classifies server restart/crash recovery as technical rather than normal Spell mercy.

Technical sources checked for this approach and correction:

- NeoForge 1.21.1 Game Tests documentation for `runGameTestServer`, Game Test Server exit behavior, registration concepts, and its `setForceExit false` caution specifically for NeoGradle;
- ModDevGradle documentation for the supported `gameTestServer` run type and `RunModel` configuration surface;
- NeoForge API Javadocs for `FakePlayerFactory` and `FakePlayer`, confirming that the latter extends `ServerPlayer` and exists to simulate server-side player actions.

## Resume / stop rules

If the corrected GameTest server gate passes, the next slice may add one minimal GameTest proving the narrowest useful fact: a `FakePlayer` can be constructed in a real NeoForge server level and can exercise the production successful-completion recovery entry point without widening runtime APIs. The test must explicitly distinguish direct service invocation from real login-event equivalence.

The first #268 failure was configuration-invalid and has a concrete corrected cause, so it does not justify repeating the same broken head. If the corrected GameTest server now fails twice for the same unchanged infrastructure reason, record the Gradle/server logs, mark that reason blocked on #34/#268, and stop retrying. Resume only with a ModDevGradle/NeoForge configuration change, dependency update, owner input, or another credible harness approach.

A true two-JVM real-player reconnect remains the strongest desired evidence. Do not claim it from a one-process FakePlayer GameTest.
