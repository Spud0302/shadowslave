---
uid: ss-implementation-chainback-trace
record_kind: implementation
authority: context
lore_class: DESIGN
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - combat-core/ROADMAP.md
  - docs/lore-research/2026-08-11-chainback-geckolib-presentation.md
implementation_links:
  - mod/src/main/java/dev/spud/shadowslave/world/entity/ChainbackEntity.java
  - mod/src/main/java/dev/spud/shadowslave/world/entity/ChainbackCombatAdmission.java
  - mod/src/main/java/dev/spud/shadowslave/world/entity/ChainbackCombatController.java
  - mod/src/main/java/dev/spud/shadowslave/client/model/ChainbackModel.java
test_links:
  - mod/src/test/java/dev/spud/shadowslave/world/entity/ChainbackEntityBindingTest.java
  - mod/src/test/java/dev/spud/shadowslave/world/entity/ChainbackCombatAdmissionTest.java
  - mod/src/test/java/dev/spud/shadowslave/world/entity/ChainbackCombatControllerTest.java
  - mod/src/main/java/dev/spud/shadowslave/world/entity/ChainbackGameTests.java
tags:
  - implementation
  - traceability
  - chainback
---

# Chainback traceability

## Intent

[[brain/design/combat-v1]] defines a readable one-exchange combat slice. [[brain/lore/chainback]] defines the lore and project-design boundary.

## Shadow Slave execution

- Entity and Java-owned creature binding: mod/src/main/java/dev/spud/shadowslave/world/entity/ChainbackEntity.java
- Admission seam: mod/src/main/java/dev/spud/shadowslave/world/entity/ChainbackCombatAdmission.java
- Combat controller: mod/src/main/java/dev/spud/shadowslave/world/entity/ChainbackCombatController.java
- Entity registration: mod/src/main/java/dev/spud/shadowslave/world/entity/NightmareCreatureEntities.java

## Generic Combat Core

- Action state and phases: combat-core/src/main/java/dev/spud/combatcore/api/
- Player and mob executors: combat-core/src/main/java/dev/spud/combatcore/runtime/
- Geometry and committed zones: combat-core/src/main/java/dev/spud/combatcore/api/
- Diagnostics and trace presentation: combat-core/src/main/java/dev/spud/combatcore/presentation/
- Dummy and slow-motion inspection: combat-core/src/main/java/dev/spud/combatcore/dummy/ and command/

Combat Core must remain unaware of Souls, Aspects, Flaws, Memories, Echoes, Rank, appraisal, and Chainback identity.

## Presentation

- Model provider: mod/src/main/java/dev/spud/shadowslave/client/model/ChainbackModel.java
- Geometry: mod/src/main/resources/assets/shadowslave/geo/chainback.geo.json
- Animation: mod/src/main/resources/assets/shadowslave/animations/chainback.animation.json
- GeckoLib is replaceable presentation infrastructure and cannot own authoritative outcomes.

## Automated evidence

- ChainbackEntityBindingTest
- ChainbackCombatAdmissionTest
- ChainbackCombatControllerTest
- ChainbackPresentationResourcesTest
- ChainbackGameTests
- Combat Core API, runtime, presentation, dummy, and command tests

## Remaining evidence

- Run GameTests in CI, not only locally.
- Boot the real assembled dependency-complete pack.
- Review telegraph readability, spacing, response clarity, recovery, one-punish result, and normal resumption in Prism.
- Exercise dedicated-server cleanup and later two-player latency only after the singleplayer exchange stabilizes.

## Non-goals

See [[brain/design/deferred-scope]]. This slice does not authorize the broad combat roadmap.

