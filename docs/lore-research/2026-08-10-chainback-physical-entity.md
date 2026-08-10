# Chainback physical entity execution note

**Scope:** first physical Nightmare Creature execution adapter.  
**Branch:** `gpt/world-chainback-entity`  
**Authority:** existing `NightmareCreatureContentCatalog` profile `chainback` remains Java-owned identity/content authority.

## What this slice executes

The already-authored **Chainback** profile is now mapped to a real NeoForge entity type, `shadowslave:chainback`.

For the first physical slice, the entity deliberately reuses vanilla Spider body/rendering and Spider hostile-mob behavior. That gives the project an immediately visible, climb-capable, pathfinding, attackable and fightable mob while bespoke models/animations and mature encounter AI remain absent.

The entity does not spawn naturally. It is intentionally reachable only through explicit engine/scenario spawning or development use such as:

```text
/summon shadowslave:chainback ~ ~ ~
```

An explicit empty entity loot table prevents this placeholder execution layer from silently inventing Nightmare Creature rewards.

## Lore/source boundary

This implementation reuses the primary/later research already accepted with `docs/NIGHTMARE-CREATURE-CONTENT-WAVE1.md` rather than claiming a new creature mechanic. That evidence checked Chapters 74, 201, 380, 1609 and 1652 under `docs/LORE-SOURCE-POLICY.md`.

### CANON

- Nightmare Creature Rank and Class are distinct measures.
- Nightmare Creatures can differ qualitatively in form, capability, movement and environmental interaction.

### INFERRED

- A physical Minecraft adapter may execute a resolved Java-owned creature identity without becoming the authority for that identity.
- A first implementation can use replaceable Minecraft locomotion/AI where it is clearly treated as presentation/execution rather than a canon Rank/Class rule.

### DESIGN

- `chainback` itself, including its exact Awakened Monster classification, is existing project-authored DESIGN content.
- Reusing Spider geometry, texture, climbing, pathfinding and hostile AI is a temporary Minecraft execution choice.
- The entity dimensions, tracking range, explicit `/summon` development seam and empty loot table are DESIGN.

### UNKNOWN

- No canonical mapping from Rank/Class to Minecraft health, damage, speed, AI, senses or hitbox is known.
- Natural spawn rules, occurrence rates, territory, encounter selection, drops/rewards and respawn/despawn policy remain unresolved.
- The final Chainback model, animation, sound design and displacement mechanic remain unimplemented.

### COMPATIBILITY

- `NightmareCreatureContentCatalog` remains the source of stable Chainback identity, Rank/Class and authored encounter descriptors.
- The NeoForge `EntityType`, vanilla-backed renderer and inherited AI are removable execution adapters.
- Scenario/Nightmare state, progression, appraisal, rewards and persistence remain Java-owned and are not derived from the entity renderer, loot table or AI.

## Validation boundary

`ChainbackEntityBindingTest` verifies that the physical adapter resolves the existing `chainback` profile and does not duplicate/reclassify its Java-owned identity.

Hosted Preview Gates should additionally prove compilation/unit tests, physical NeoForge client boot and dedicated-server boot. A successful boot proves registration/classloading compatibility; it does not by itself prove combat feel, climbing behavior in a real structure, multiplayer behavior, or bespoke visual quality.

## Next physical slice

After this entity boots cleanly, the next world-facing step should put Chainback into a small authored encounter/structure seam or add a second creature whose movement model differs materially (for example a water-capable Drowned Listener), rather than expanding another definition catalogue.
