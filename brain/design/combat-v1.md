---
uid: ss-design-combat-v1
record_kind: design
authority: project-authority
lore_class: DESIGN
state: active
owner: Andrew
created: 2026-08-17
updated: 2026-08-21
sources:
  - combat-core/ROADMAP.md
  - PROJECT-STATUS.md
  - docs/design/MODULAR-JAR-BOUNDARIES.md
related:
  - ss-lore-chainback
implementation_links:
  - combat-core/src/main/java/dev/spud/combatcore
  - mod/src/main/java/dev/spud/shadowslave/world/entity/ChainbackCombatController.java
tags:
  - design
  - combat-v1
  - chainback
---

# Combat v1

## Player loop

Observe → Respond → Create an opening → Commit → Reassess.

The target is a readable exchange, not stationary damage trading:

TELEGRAPH → RESPONSE → HIT, MISS, OR DEFENSE → RECOVERY OR OPENING → ONE PUNISH → REASSESS.

## Action language

- WIND_UP — readable commitment before authority resolves.
- ACTIVE — bounded resolution window.
- RECOVERY — the attacker cannot immediately erase the commitment.
- OPEN — an explicit bounded punish opportunity when the authored interaction creates one.

Hit, health, stability, opening, and action-state outcomes remain server-authoritative. Presentation can predict or illustrate but cannot decide them.

## First vertical slice

Chainback is the first acceptance target:

1. A readable telegraph begins.
2. The player can physically evade or use the bounded supported defense.
3. The result resolves exactly once.
4. Chainback enters a readable recovery or opening.
5. The player can produce exactly one expected punish result.
6. Both sides return to ordinary decision-making.

## Acceptance evidence

The exchange must repeatedly expose deterministic evidence for:

- action phase and timing;
- hit count and terminal outcome;
- health change;
- stability or opening state where applicable;
- opening duration;
- cleanup after death, unload, dimension change, logout, and server stop;
- dedicated-server behavior before wider expansion.

Physical feel and readability remain human evidence. Deferred does not mean passed.

## Scope boundary

This slice does not require a full injury system, Soul damage, complete Essence economy, advanced movement trees, a generic framework extraction, broad weapon catalogue, or broad damage taxonomy. See [[brain/design/deferred-scope]].

## Related

- [[brain/maps/combat-v1-chainback.canvas|Combat map]]
- [[brain/lore/chainback|Chainback]]
- [[brain/implementation/chainback-traceability|Traceability]]
- [[brain/ai/context/combat-v1|AI context packet]]
- [[combat-core/ROADMAP|Combat Core roadmap]]

