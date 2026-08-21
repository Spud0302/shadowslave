---
uid: ss-context-echo-memory-pipeline
record_kind: context
authority: context
lore_class: DESIGN
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - docs/ECHO-CONTENT-WAVE1.md
  - docs/ASPECT-ABILITY-SET-FOUNDATION.md
  - PROJECT-STATUS.md
tags:
  - context
  - echo
  - memory
  - ash-compass
  - ash-burrower
---

# Context packet — Echo and Memory pipeline

## Goal

Provide a bounded context packet for implementing, inspecting, and testing Memory item manifestations (e.g. Ash Compass, Glass Road) and Echo creature bindings (e.g. Ash Burrower baseline) under server authority.

## Must read

- [[docs/ECHO-CONTENT-WAVE1]] — Echo ownership, summoning, command, and recall semantics.
- [[docs/ASPECT-ABILITY-SET-FOUNDATION]] — Soul inventory and Memory manifestation contracts.
- [[PROJECT-STATUS]] — Merged Memory and Echo baseline status.
- [[docs/design/MODULAR-JAR-BOUNDARIES]] — Modular entity boundaries.

## Do

- Keep Memory manifestation and Echo entity ownership server-authoritative (`EchoOwnershipData`, `EchoManifestationService`).
- Bind Echo commands (follow, stay, attack) to server validation.
- Clean up Echo entities deterministically on unsummon, owner death, logout, or dimension transit.
- Verify GeckoLib model/animation presentation bindings without putting game state in renderers.

## Do not

- Do not let clients dictate Memory durability, active manifestation, or Echo state.
- Do not add unlimited procedural Memory generation before the catalog contracts stabilize.
- Do not let GeckoLib renderers own game outcomes or persistent inventory state.

## Acceptance

Memory activation from Soul inventory $\rightarrow$ server-authoritative manifestation $\rightarrow$ Echo spawn with correct owner attachment $\rightarrow$ command execution $\rightarrow$ clean unsummon/recall with state persisted across server restart.
