---
uid: ss-implementation-index
record_kind: index
authority: context
lore_class: "N/A"
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - PROJECT-STATUS.md
  - mod/IMPLEMENTATION-STATUS.md
  - combat-core/ROADMAP.md
tags:
  - implementation
  - index
---

# Implementation index

## Current state

- [[brain/implementation/current-snapshot|Captured local snapshot]]
- [[brain/implementation/authority-drift-register|Authority drift and release-doctor queue]]
- [[PROJECT-STATUS|Project status authority]]
- [[docs/CURRENT-PREVIEW-SUMMARY|Current preview summary]]
- [[GPT_HANDOFF|Current development handoff]]

## Runtime ownership

- Shadow Slave base mod: canonical progression, identity, persistence, provider contracts, and representative slices.
- Combat Core: Shadow-Slave-agnostic timing, action phases, geometry, commitment, recovery, and generic executors.
- GeckoLib: replaceable presentation provider.
- SmartBrainLib: replaceable creature-brain execution provider.
- Modpack shell: deterministic assembly and provenance layer; it must package the complete dependency closure.

See [[docs/design/MODULAR-JAR-BOUNDARIES]] and [[combat-core/ROADMAP]].

## Evidence and testing

- [[TESTING|Project testing]]
- [[shared-test-spec/VERTICAL-SLICE|Shared vertical-slice acceptance]]
- [[docs/JAVA-INTEGRATION-TEST-PROCEDURES|Java integration procedures]]
- [[docs/PLAYABLE-PREVIEW-TEST-MATRIX|Preview test matrix]]
- [[docs/PLAYABLE-PREVIEW-PROVENANCE|Preview provenance]]
- [[brain/evidence/README|New evidence records]]

## Focused traceability

- [[brain/implementation/chainback-traceability|Chainback from design to code]]

