---
uid: ss-context-modpack-packaging
record_kind: context
authority: context
lore_class: COMPATIBILITY
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - modpack/README.md
  - docs/THIRD-PARTY-DEPENDENCY-POLICY.md
  - brain/implementation/authority-drift-register.md
  - PROJECT-STATUS.md
tags:
  - context
  - modpack
  - packaging
  - dependencies
  - release-qa
---

# Context packet — Modpack packaging and dependency closure

## Goal

Provide a bounded context packet for resolving modpack dependency closures, verifying standalone JAR packaging (Shadow Slave + Combat Core), and executing dual-JVM client/server boot testing without drifting into experimental mod additions.

## Must read

- [[modpack/README]] — Modpack structure and verification harness.
- [[docs/THIRD-PARTY-DEPENDENCY-POLICY]] — Approved third-party libraries (GeckoLib, SmartBrainLib).
- [[brain/implementation/authority-drift-register]] — Known P0 pack closure and CI fixture gaps.
- [[combat-core/ROADMAP]] — Combat Core integration status.

## Do

- Verify complete dependency closure in `modpack/manifest.json` (including `combat_core 0.0.4-wip`).
- Build real production JARs (`mod/build.gradle` and `combat-core/build.gradle`) before pack assembly.
- Test dedicated server headless boot and client join across two separate JVM instances.
- Maintain deterministic modpack archive verification (`modpack/ARCHIVE-VERIFICATION.md`).

## Do not

- Do not test dummy CI fixtures (`ci-core-fixture`) in place of real built JARs.
- Do not introduce unapproved third-party mods outside the dependency policy.
- Do not mark release gates green without executed boot logs and checksums.

## Acceptance

Full Gradle multi-project build $\rightarrow$ valid manifest closure $\rightarrow$ zip assembly $\rightarrow$ headless dedicated server boot exit 0 $\rightarrow$ clean client handshake.
