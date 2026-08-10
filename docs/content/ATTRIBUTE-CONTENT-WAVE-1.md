# Attribute content wave 1

**Status:** player-facing DESIGN catalogue; no canonical generation formula is claimed.  
**Architecture:** Java owns canonical Attribute identity/state. External mods may only present or execute removable adapters.  
**Lore workflow:** `docs/LORE-SOURCE-POLICY.md` was re-read before authoring this slice.

## Player-facing scope

Wave 1 adds 18 authored Attribute primitives intended for procedural identity composition rather than a small fixed character-stat list:

- Ashen Lungs
- Bell Sense
- Blackwater Blood
- Borrowed Compass
- Cinder Heart
- Glass Nerves
- Hollow Presence
- Mist-Born
- Red Thread
- Roadwise
- Stone Sleeper
- Thorn-Kin
- Tide Listener
- Unbroken Breath
- Veil-Touched
- Watcher's Mark
- Weathered Hands
- Living Ember

The catalogue deliberately avoids generic Strength/Dexterity/Intelligence-style RPG statistics. Each Attribute instead carries a stable identity, an authored origin category, an explicit visibility state, an effect family, affinity tags and concrete gameplay hooks. Three authored evolution links demonstrate that an Attribute can change identity without turning evolution into a universal rule.

`Catalog.select(seed, evidence)` provides deterministic weighted selection from authored primitives. Evidence tags alter weights, but the same seed and logically identical evidence always resolve to the same stable Attribute ID. This selector is a Minecraft generation tool only.

## Primary chapter evidence checked

Primary chapter material was checked through the owner-designated access layer and official WebNovel where available.

### Chapter 3 — The Strings of Fate

Sunny's early runes present Attributes as named traits distinct from Aspect/Rank. The First Nightmare establishes that Attributes can describe unusual personal conditions rather than ordinary numerical statistics.

### Chapters 83 and 85 — Five / One Step At A Time

Chapter 83 shows that an Attribute can exist while its identity remains inaccessible to the bearer. Chapter 85 shows named Attribute descriptions, acquisition of Blood Weave, and Mark of Divinity becoming ready to evolve into Spark of Divinity.

### Chapters 450-451 — Alabaster Phalanx / Bone Weave

After consuming Weaver's phalanx, the Spell reports both an evolved Attribute and a newly acquired Attribute. Bone Weave produces a concrete bodily change rather than a generic stat-line increase.

### Chapter 551 — Slave's Inheritance

Blood Weave, Bone Weave and Ember of Divinity are all reported as having evolved after another lineage interaction. This confirms that evolution is not limited to one special Attribute identity and that an Attribute may evolve without necessarily becoming a wholly unrelated mechanic.

### Chapter 744 — Ascension

The Spell explicitly reports that one of Sunny's Attributes evolved during Ascension. Child of Shadows becomes Master of Shadows, demonstrating another evolution path tied to changed supernatural nature/authority.

### Chapter 2029 — Fortune Telling

This later clarification is decisive for origin handling: Sunny reflects that Fated was innate before his First Nightmare, while Child of Shadows and Mark of Divinity came from the nameless temple slave whose role/body he inhabited. Rain's situation also makes clear that the Spell is not the only possible context in which Attributes can exist or be examined.

## Evidence classification

### CANON

- Attributes are named supernatural traits distinct from Aspect identity and ordinary Rank/Class presentation.
- An Attribute can be innate.
- A First Nightmare role/body can contribute Attributes to the challenger; Chapter 2029 explicitly identifies this for Sunny.
- Attributes can be acquired later through exceptional transformations or inheritances.
- Attributes can evolve.
- An Attribute can exist while its identity is obscured from the bearer.
- Attribute effects can be qualitative and highly specific rather than generic numerical stats.

### INFERRED

- A reusable Java content model benefits from separating origin, visibility, effect family, gameplay hooks and evolution relationships so later presentation/execution layers do not own identity.
- Appraisal/scenario evidence is an appropriate DESIGN input for choosing among authored candidate Attributes when generating preview identities, provided the repository never labels that weighting rule canonical.

### DESIGN

- All 18 Wave-1 names and exact effects.
- `OriginKind` values and assignment of each authored Attribute to one of them.
- `Visibility.REVEALED` / `Visibility.OBSCURED` as Java presentation-state vocabulary.
- The eight `EffectFamily` values.
- All affinity tags, gameplay hooks, base weights and the three evolution links.
- Deterministic weighted selection from appraisal evidence.
- The factor of three applied to matching evidence weights.

### UNKNOWN

- Any universal formula by which the Nightmare Spell creates, awards, selects or evolves Attributes.
- Whether every First Nightmare role contributes an Attribute.
- Exact probabilities for innate, role-inherited or later-acquired Attributes.
- A universal number of Attributes per person.
- Universal rules controlling when an Attribute is hidden, revealed or self-discovered.
- Whether appraisal quality directly causes specific Attributes outside explicitly demonstrated cases.
- Universal scaling, rarity tiers or balance values for Attributes.

### COMPATIBILITY

- This slice does not persist player Attribute ownership, change Soul/Aspect/Flaw state, or alter Nightmare completion/appraisal transactions.
- Stable `ResourceLocation` IDs are Java-owned content identity.
- A later Java state layer may persist which Attribute IDs a player owns and any evolution/visibility state.
- NeoForge/UI/entity/effect implementations should consume those Java-owned IDs through removable adapters rather than becoming canonical state owners.

## Validation contract

`AttributeContentCatalogTest` checks:

- 18 unique authored profiles;
- coverage of all three authored origin categories and all eight effect families;
- absence of a generic Strength/Dexterity/Agility/Intelligence/Vitality catalogue;
- non-empty gameplay hooks for every Attribute;
- all evolution targets resolve to stable profiles;
- deterministic selection is independent of input-map iteration order;
- a 256-seed sweep explores at least 12 authored identities;
- materially different evidence sets change the selected distribution;
- negative evidence fails closed;
- all obscured identities are explicit data, not null/implicit visibility state.

## Deliberate limits

This is a content and generation foundation only. It does not award Attributes to players, execute their gameplay hooks, decide canon provenance for generated characters, or make Attribute evolution part of appraisal. Those integration steps should happen only after the Java-owned player identity model has an explicit Attribute slot/state contract.
