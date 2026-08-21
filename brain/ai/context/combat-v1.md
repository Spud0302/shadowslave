---
uid: ss-context-combat-v1
record_kind: context
authority: context
lore_class: DESIGN
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - brain/design/combat-v1.md
  - combat-core/ROADMAP.md
  - brain/implementation/chainback-traceability.md
tags:
  - context
  - combat-v1
  - chainback
---

# Context packet — Combat v1

## Goal

Prove one readable Chainback exchange on a server-authoritative action pipeline without making deferred combat systems prerequisites.

## Must read

- [[brain/design/combat-v1]]
- [[brain/design/deferred-scope]]
- [[brain/lore/chainback]]
- [[brain/implementation/chainback-traceability]]
- [[combat-core/ROADMAP]]
- [[docs/design/MODULAR-JAR-BOUNDARIES]]

## Do

- Preserve WIND_UP → ACTIVE → RECOVERY timing.
- Keep resolution server-authoritative.
- Prefer physical spacing and line-of-sight responses.
- Produce deterministic phase, outcome, health, and opening diagnostics.
- Keep Shadow Slave identity and progression out of Combat Core.
- Test cleanup and exactly-once resolution.

## Do not

- Add the full injury, Soul damage, Essence, movement, weapon, guard/parry, or damage-taxonomy trees.
- Make animations authoritative.
- Treat Rank or Class as a universal health/damage multiplier.
- Expand Combat Core because an adjacent API might be useful later.

## Acceptance

Readable telegraph → valid response → one result → bounded recovery/opening → exactly one expected punish → normal resumption, with dedicated-server evidence.

## Known unknowns

- Final physical timings and feel.
- Wider multiplayer latency and privacy-safe diagnostics.
- Canonical Chainback appearance and supernatural rules.

