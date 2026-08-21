---
uid: ss-lore-chainback
record_kind: lore
authority: context
lore_class: mixed
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - docs/NIGHTMARE-CREATURE-CONTENT-WAVE1.md
  - docs/lore-research/2026-08-10-chainback-physical-entity.md
  - docs/lore-research/2026-08-11-chainback-geckolib-presentation.md
implementation_links:
  - mod/src/main/java/dev/spud/shadowslave/world/entity/ChainbackEntity.java
  - mod/src/main/resources/assets/shadowslave/geo/chainback.geo.json
  - mod/src/main/resources/assets/shadowslave/animations/chainback.animation.json
test_links:
  - mod/src/test/java/dev/spud/shadowslave/world/entity/ChainbackEntityBindingTest.java
  - mod/src/main/java/dev/spud/shadowslave/world/entity/ChainbackGameTests.java
tags:
  - lore
  - creature
  - chainback
  - combat-v1
---

# Chainback

## Project role

Chainback is the first bounded creature combat dance used to prove the Combat Core consumer seam. It is intentionally a narrow vertical slice rather than permission to build the full combat, creature, movement, or progression trees.

## Classification

### CANON constraints

- Nightmare Creature Rank and Class are distinct measures.
- Nightmare Creatures can differ qualitatively in powers, behavior, weaknesses, form, movement, and environmental interaction.

### INFERRED

- A physical Minecraft adapter can execute a stable Java-owned creature identity without becoming authority for that identity.
- A creature-specific presentation is more legible than retaining an unrelated vanilla visual.

### DESIGN

- Chainback itself, including its authored identity and exact classification, is project content.
- The tall hunched chained silhouette, proportions, hanging-chain geometry, animation clips, combat choreography, strike/pull exchange, dimensions, and development seams are Minecraft design choices.
- GeckoLib is presentation infrastructure; Combat Core is execution infrastructure.

### UNKNOWN

- Canonical Chainback anatomy, materials, colors, gait, sounds, occurrence, rewards, and exact supernatural displacement rules are not established by the checked evidence.
- No canonical formula maps Rank or Class to Minecraft health, damage, speed, AI, or hitbox values.

### COMPATIBILITY

- The Java creature catalogue owns stable identity and authored descriptors.
- Renderers, model providers, AI adapters, and combat executors are replaceable.
- Presentation or optional libraries never own Soul state, progression, rewards, Nightmare state, or persistence.

## Implementation evolution

1. The first physical adapter deliberately reused Spider rendering and hostile behavior.
2. The later GeckoLib slice replaced the unrelated visual with project geometry and animations.
3. Local preview.4 adds the upright ground strike/pull exchange through standalone Combat Core.
4. Automated structure and contract evidence exists; physical feel, readability, and wider multiplayer tuning remain review work.

## Related

- [[brain/design/combat-v1|Combat v1]]
- [[brain/implementation/chainback-traceability|Chainback traceability]]
- [[combat-core/ROADMAP|Combat Core roadmap]]
- [[docs/lore-research/2026-08-10-chainback-physical-entity|Physical entity evidence]]
- [[docs/lore-research/2026-08-11-chainback-geckolib-presentation|Presentation evidence]]

