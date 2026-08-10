# Dream Realm Ashen Expanse physical vertical slice — evidence note

**Date:** 2026-08-10  
**Scope:** one bounded Minecraft execution slice for the existing Java-owned `ashen_expanse` Dream Realm region profile.  
**Policy:** `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`.

## Primary material checked

- **Chapter 370 — Exploration Report:** Dream Realm field knowledge is organized around regional geography and environment, notable landmarks, unusual environmental features, and Nightmare Creature behavior/weaknesses. Official WebNovel was also checked for the chapter identity and the same decisive region-report material.
- **Chapter 380 — Above and Below:** the Chained Isles demonstrate that Dream Realm geography can itself impose traversal constraints: floating islands, immense chains, different creature populations, the Sky Below, and altitude-dependent crushing pressure.

These checks constrain architecture only. **Ashen Expanse is existing project-authored DESIGN content and is not a canonical location.**

## Classification

### CANON

- Dream Realm regions can have consequential geography and environmental conditions rather than being interchangeable scenery.
- Region knowledge can include landmarks and creature ecology/behavior.
- Traversal can be materially constrained by the physical rules of a region.

### INFERRED

- A Minecraft region executor can separately realize terrain, landmarks, traversal cues, and resource opportunities while consuming a Java-owned region identity.
- A bounded explorable slice is a safer first world-generation seam than attempting to infer a universal canonical Dream Realm generator.

### DESIGN

- The existing `ashen_expanse` region identity, name, hooks, arrival cue and travel rule are project-authored content.
- `shadowslave:dream_realm`, the fixed Ashen Expanse biome, exact coordinates, 49x49 ash field, refuge ruin, watchtower, obelisk and causeway geometry are Minecraft implementation choices.
- Tuff/gravel/deepslate/stone/blackstone/obsidian are placeholder terrain and structure materials.
- Bone blocks, raw iron blocks and brown mushroom blocks are placeholder physical manifestations of the existing `bone_char`, `ruin_metal` and `dry_fungus` resource hooks. Their vanilla item drops are not canonical Dream Realm resource ownership or progression.
- `/shadowslave_dreamrealm enter|exit|status` is a development access seam, not an in-world canonical travel rule.

### UNKNOWN

- Any canonical procedural Dream Realm world-generation algorithm, biome taxonomy, placement density or region probability.
- Exact geography represented by Ashen Expanse, because the profile itself is DESIGN.
- Final resource gathering/crafting behavior, spawn ecology, creature occurrence rates, settlement distribution and region-state persistence.
- Final visual palette, bespoke models, textures, particles, ambient audio and weather behavior.

### COMPATIBILITY

- `DreamRealmRegionContentCatalog` remains the Java-owned content identity source.
- The dimension, biome, block geometry and command adapter are removable execution/presentation infrastructure only.
- This slice adds no Soul/progression state, exploration authority, canonical resource inventory, Nightmare resolution, Memory/Echo ownership, or third-party-mod authority.
- Existing active gameplay work remains separate: #184 owns the Nightmare world/creature integration edge, #185 owns Memory execution and #186 owns Echo ownership/summoning.

## Placeholder boundary

This first slice intentionally favors something physically traversable over polished art. The player can enter a distinct dimension, walk the ash field, use the small refuge as cover/navigation, find all three authored landmark hooks and locate all three resource-hook nodes. The blocks are explicit placeholders and must not be treated as the canonical appearance or mechanics of those hooks.
