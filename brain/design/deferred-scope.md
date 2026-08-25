---
uid: ss-design-combat-deferred
record_kind: design
authority: project-authority
lore_class: DESIGN
state: accepted
owner: Andrew
created: 2026-08-17
updated: 2026-08-21
sources:
  - combat-core/README.md
  - docs/design/MODULAR-JAR-BOUNDARIES.md
tags:
  - design
  - scope
  - deferred
---

# Deferred combat scope

These systems are explicitly not prerequisites for the Chainback-first slice:

- limb injuries and broad hurtbox taxonomies;
- Soul damage;
- the full Essence economy;
- advanced movement trees;
- universal dodge invulnerability;
- broad guard, parry, stagger, and initiative catalogues;
- broad weapon and final damage formula catalogues;
- Rank, Class, and body-quality balancing across the whole game;
- lock-on and final camera policy;
- supernatural target-layer frameworks;
- PvP and multiplayer hardening;
- generic framework extraction beyond the already bounded standalone Combat Core;
- large content catalogues.

Design notes may record constraints for these topics. They do not authorize implementation or make them acceptance dependencies.

Promote an item only when:

1. a proven playable exchange exposes a concrete missing capability;
2. the smallest addition can be stated;
3. ownership between Shadow Slave and Combat Core is clear;
4. acceptance evidence is defined;
5. Andrew explicitly approves the scope change.

