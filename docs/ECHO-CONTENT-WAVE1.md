# Echo content wave 1

**Status:** player-facing content definition; not yet wired to entity spawning, AI, ownership persistence or loot.  
**Architecture:** Java owns the canonical Echo definition. External entity/model/animation/AI providers remain removable presentation/execution adapters.  
**Lore rule:** `docs/LORE-SOURCE-POLICY.md` remains controlling.

## Player-facing catalogue

Wave 1 defines thirteen reusable Echo profiles:

- Ash Burrower
- Bell-Eater
- Chainback
- Drowned Listener
- Glasswing
- Gutter Choir
- Hollow Mimic
- Mire Runner
- Pale Ferryman
- Stone Maw
- Thorn Matron
- Veil Stalker
- Steel Courser

The first twelve deliberately reuse the stable creature identities authored in Nightmare Creature content wave 1. They add Echo-oriented practical roles and command vocabulary rather than creating a second incompatible creature taxonomy. Steel Courser is an artificial-Echo template and deliberately carries no invented creature source Rank or Class.

Roles include mounts, carriers, scouts, guards, trackers, pursuers, screens, disruptors, area control, escorts and labour. Commands include follow, hold, move, carry, mount, search, intercept, screen, guard-point and withdraw. Exact role labels and command semantics are project DESIGN.

## Primary chapter evidence checked

### Chapter 47 — Echo

**CANON:** Echoes can be received after slaying Nightmare Creatures; the observed Carapace Scavenger Echo is represented separately from Memories, can be summoned, obeys commands, fights, carries cargo and serves as a mount. The runes identify its creature-like Type/Core/Attributes. The text also explicitly describes the Echo as not truly alive.

**INFERRED:** a Minecraft Echo definition should preserve a stable supernatural identity independently from one concrete mob implementation, and its useful behaviour should not be limited to attacking enemies.

### Chapter 256 — True Reason

**CANON:** a bizarre liquid-metal Echo is discussed as a copy of a Corrupted Nightmare Creature. Echo form can therefore be much broader than ordinary rideable animals or humanoid combat pets.

**INFERRED:** reusable Echo content should allow unusual body plans and practical roles without forcing every Echo into the same entity skeleton or equipment category.

### Chapter 1552 — Lifeboat

**CANON:** Cassie summons the Echo of a Defiled sybil and gives it a practical command as part of preparing and lowering a boat. The Echo follows and performs commanded work outside a direct combat exchange.

**INFERRED:** utility/labour/escort commands are compatible with observed Echo use and should be first-class content hooks rather than incidental AI behaviours.

### Chapter 1609 — Reclusive Saint

**CANON:** multiple bestial Echoes are summoned specifically as fast mounts even though most are not powerful enough to matter in a fierce battle. Masters deliberately hunt suitable Nightmare Creatures to obtain useful mount Echoes. The same chapter explicitly identifies Nephis's steel stallion as an artificial Echo created by Valor enchanters.

**INFERRED:** an Echo catalogue should model practical role separately from raw combat strength, and should support both creature-derived and artificial provenance.

## Evidence classification

- **CANON:** Echoes are distinct summonable entities; creature-derived Echoes can reproduce creature identity/form; they can obey commands and perform combat and non-combat work; mount selection can prioritize speed/usefulness over battle power; artificial Echoes exist.
- **INFERRED:** stable Echo content should expose role, command and utility descriptors separately from the removable Minecraft AI/entity provider; creature-derived profiles can reuse the stable identity of their source profile.
- **DESIGN:** all thirteen Wave-1 profile selections, role assignments, command modes, tactical notes, utility tags, presentation cues and the `Steel Courser` content ID/name.
- **UNKNOWN:** no universal Echo drop probability, artificial-Echo crafting formula, summoning cost formula, repair/healing formula, command vocabulary, AI model or exact relationship between every source creature capability and its Echo copy is claimed.
- **COMPATIBILITY:** no existing player save, Soul state, creature, loot, Nightmare, appraisal or inventory path changes. Growth-capable Shadows, Reflections and other exceptional summons remain separate Aspect-specific systems and are not generalized into ordinary Echoes.

## Validation

`EchoContentCatalogTest` verifies:

- 13 unique profiles;
- 12 creature-derived profiles matching all first-wave Nightmare Creature stable IDs;
- one artificial Echo whose creature provenance is deliberately absent;
- broad non-combat role coverage including mount, carrier, scout, guard, tracker, escort and labour;
- all ten first-wave command modes are represented;
- at least 25 distinct utility tags;
- every profile has a tactical-use description and presentation cue.

## Integration boundary

This PR is content-only. A later Java-owned Echo-instance/storage layer can bind a resolved Echo profile to a player and persist ownership/provenance. A removable Minecraft adapter may then map the stable profile ID to an entity implementation, AI goals, animation/model resources and command UI.

Do not let the external provider own Echo Rank/Class provenance, ownership, acquisition history or permanent identity.

## Best next content slice

Add an **Attribute content and generation foundation**: authored origin/effect primitives, visibility/revelation state, evolution hooks and deterministic evidence weighting, while avoiding generic RPG Strength/Dexterity statistics and keeping exact canonical Attribute-generation rules UNKNOWN.
