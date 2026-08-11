# NeoForge GameTest recovery path

**Date:** 2026-08-11  
**Tracks:** #34

## Why this slice exists

Issue #34's stronger successful-completion proof has been intentionally blocked: the repository had no demonstrated automated way to obtain a real server-side `ServerPlayer`-compatible actor inside a NeoForge test runtime and then drive production completion recovery. Repeating codec-only restart tests would not close that evidence gap.

A credible new approach is now available and is narrow enough to validate independently before attempting recovery semantics:

- the repository already configures NeoForge's `gameTestServer` run and enables the `shadowslave` GameTest namespace;
- NeoForge 1.21.1 documents `runGameTestServer` as the Gradle execution path for Game Tests and documents disabling forced exit so the GameTest server's result is reported reliably to Gradle;
- NeoForge exposes `FakePlayer`, a `ServerPlayer` implementation intended to simulate server-side player actions, through `FakePlayerFactory`.

This does **not** prove that FakePlayer is equivalent to a real network login, that it participates in `PlayerList` persistence identically, or that a fake player can cross two server JVMs without additional harness work. Those remain questions for the next bounded probe rather than assumptions in this infrastructure change.

## Change

This branch makes the existing GameTest run usable as hosted evidence:

1. add `setForceExit false` to the configured `gameTestServer` run;
2. execute `./mod/gradlew -p mod runGameTestServer --no-daemon --stacktrace` in the Java Preview Gates job;
3. pin both requirements in `test_preview_gates_workflow.py`.

There are intentionally no registered Shadow Slave Game Tests in this slice. The first reviewable boundary is to prove the server harness itself starts, executes NeoForge's GameTest lifecycle, reports success/failure correctly, and coexists with the existing unit/client/dedicated-server gates.

## Relationship to current persistence work

PR #262 exact head `e75bb76436d5d0338f29a4ebe8cf5f807f4c4c09` passed Preview Gates #287 / run `31480946729`, including both the full Java job and deployed frozen-datapack harness, and has no inline review threads. That supplies the new evidence needed to consider the Mineflayer transport correction valid, but repository workflow rules still reserve merging to `main` for a human. PR #259 therefore should not be blindly rerun on its unchanged branch until the transport correction is actually present in its tested ancestry or another credible new condition appears.

This GameTest slice is independent of that merge dependency and advances the previously blocked stronger Java recovery lane instead of adding another disk-image variant.

## Evidence classification

- **CANON:** not applicable. No Shadow Slave lore mechanic, outcome, reward, appraisal, death, progression, Memory, Echo, Attribute, Aspect or Flaw behavior changes.
- **INFERRED:** none.
- **DESIGN:** NeoForge GameTest and FakePlayer are technical test infrastructure candidates for exercising production server-side recovery code.
- **UNKNOWN:** whether FakePlayer enters the same login-event path as a networked player; whether it is automatically tracked/saved by `PlayerList`; whether it can reproduce return-position persistence; whether the useful recovery probe can be carried across two real server JVMs; and whether later GameTest APIs need an explicit template/registration fixture.
- **COMPATIBILITY:** runtime gameplay, persistent schemas, catalogues, dependencies, datapack behavior and lore-facing semantics are unchanged. This only adds hosted execution of an already-configured NeoForge test run and its workflow contract.

## Source boundary

This is technical infrastructure, so no new novel proposition requires primary-text research. `docs/LORE-SOURCE-POLICY.md` remains controlling if a later test reveals that runtime behavior itself needs a lore-sensitive change. `docs/JAVA-LORE-ALIGNMENT.md` explicitly classifies server restart/crash recovery as technical rather than normal Spell mercy.

Technical sources checked for this approach:

- NeoForge 1.21.1 Game Tests documentation for `runGameTestServer`, registration concepts, and the `setForceExit false` Gradle configuration requirement;
- NeoForge API Javadocs for `FakePlayerFactory` and `FakePlayer`, confirming that the latter extends `ServerPlayer` and exists to simulate server-side player actions.

## Resume / stop rules

If this GameTest server gate passes, the next slice may add one minimal GameTest proving the narrowest useful fact: a `FakePlayer` can be constructed in a real NeoForge server level and can exercise the production successful-completion recovery entry point without widening runtime APIs. The test must explicitly distinguish direct service invocation from real login-event equivalence.

If the GameTest server fails twice for the same unchanged infrastructure reason, record the Gradle/server logs and stop retrying. Resume only with a NeoForge/moddev configuration change, dependency update, owner input, or another credible harness approach.

A true two-JVM real-player reconnect remains the strongest desired evidence. Do not claim it from a one-process FakePlayer GameTest.
