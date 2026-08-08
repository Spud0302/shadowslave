# Aspect ability presentation — wave 1

**Status:** bounded player-facing content slice on current `main`.  
**Classification:** authored Minecraft **DESIGN** constrained by verified Aspect/Aspect Ability lore.  
**Runtime status:** descriptive/presentation content only; no ability execution, appraisal mutation or persistence.

## Purpose

`ExpandedIdentityContentCatalog.waveOne()` already contains 12 reusable Aspect nature primitives and 24 compatible Dormant ability primitives. Their stable identities are generation-ready, but most have only a name and affinity tags.

This slice gives those already-resolved primitives enough authored player-facing meaning to be inspected, reviewed and eventually rendered without turning the presentation layer into identity authority or pretending that the project knows the Nightmare Spell's Aspect-generation formula.

The catalogue adds:

- one qualitative nature summary and player-facing gameplay promise for every current nature;
- one bounded ability description for every current Dormant ability primitive;
- broad presentation families spanning offense, defense, movement, perception, support, concealment, guidance, endurance and control;
- at least two concrete gameplay hooks per ability;
- explicit negative boundaries preventing flavor text from silently becoming unlimited invisibility, teleportation, prophecy, omniscience, elemental control or other unsupported power claims.

## Source-policy freshness check

Research followed `docs/LORE-SOURCE-POLICY.md`.

At research start, the owner-designated NovelFull listing exposed **Chapter 3116, "Princess of the Underworld"** as its latest listed chapter. Official WebNovel exposed **3,131 chapters**. The third-party listing is therefore behind current official publication and is treated only as the designated access layer, not current-publication authority.

The decisive Aspect mechanics for this slice occur much earlier and were cross-checked against official WebNovel chapter pages where available.

## Lore evidence checked

### Chapter 16 — Rebirth

After the First Nightmare, Sunny's Aspect Ability is explicitly awakened/acquired and then exposed through runes with its own name and qualitative description. The same chapter has Sunny reason that Aspect Abilities can matter in very different practical roles: combat, sorcery, utility and healing are all contemplated as distinct possibilities.

This is strong evidence against representing every generated Aspect ability as one generic damage skill and strong evidence for player-visible qualitative descriptions.

### Chapter 354 — Awakening

On reaching the next human Rank, Sunny receives another distinct Aspect Ability, Shadow Step. The chapter presents it as a new power expression tied to his existing Aspect rather than a replacement Aspect identity.

This supports keeping stable Aspect identity separate from individual ability identity and allowing an Aspect to accumulate distinct abilities over progression.

### Chapter 744 — Ascension

Sunny receives Shadow Manifestation as another distinct Aspect Ability during Ascension, while an existing Aspect Ability also evolves. This confirms that individual Aspect abilities can be separately named, separately described and capable of later evolution.

Sunny's appraisal theory in the same chapter remains character interpretation and is not used as a generation rule here.

### Chapter 1307 — Before the Nightmare Spell

Later natural-ascension material establishes that the path of Ascension and realization of supernatural power predates reliance on the Nightmare Spell. The project therefore must not describe these authored powers as arbitrary effects manufactured by a known Spell generation equation.

## Evidence boundary

### CANON

- An Aspect and an Aspect Ability are separable concepts.
- Aspect Abilities are named/player-visible and can have qualitative descriptions.
- Aspect Abilities can serve materially different roles, including combat and non-combat utility.
- Further progression can grant additional Aspect Abilities rather than replacing the Aspect itself.
- Existing Aspect Abilities can evolve.
- The broader path of supernatural Ascension is not reducible to a project-invented Nightmare Spell generation formula.

### INFERRED

- A reusable game catalogue benefits from separating an already-resolved Aspect nature's broad gameplay promise from an individual ability's bounded expression.
- Explicit negative boundaries are useful authoring safety rails so evocative names such as `Mirror Glimpse`, `Hollow Step` and `Low Tide` do not accidentally imply prophecy, true invisibility or unrestricted elemental control.
- Broad gameplay families are useful for content diversity and presentation, provided they are not presented as a canonical Shadow Slave taxonomy.

### DESIGN

Everything specific to this wave is project-authored DESIGN:

- all twelve nature summaries and gameplay promises;
- all twenty-four exact ability descriptions;
- `AbilityFamily` and its category names;
- gameplay hooks, presentation tags and negative boundaries;
- the rule that every ability has at least two gameplay hooks;
- `aspect-ability-presentation-v1`;
- the exact Minecraft-scale limitations implied by each description.

The underlying nature/ability primitive names, affinities, weights and compatibility rules were already project DESIGN in the merged procedural identity catalogue.

### UNKNOWN

Canon does **not** currently provide a universal formula for:

- how an Aspect is selected or realized;
- how exact Aspect Ability effects are generated;
- mapping an Aspect's theme to a fixed catalogue of ability families;
- balancing range, duration, cooldown, essence cost or numerical potency;
- how much one Aspect Ability must resemble another ability of the same Aspect;
- whether the broad gameplay families used by this project correspond to any in-world taxonomy.

Those remain UNKNOWN. No deterministic generator, content family or authored effect in this repository is claimed to reproduce the Nightmare Spell.

### COMPATIBILITY

- `ExpandedIdentityContentCatalog` remains authoritative for stable procedural nature/ability identities.
- `AspectAbilityContentCatalog.compose(natureId, abilityId)` accepts already-resolved IDs and rejects incompatible pairs; it never chooses another pair.
- This slice does not modify `DeterministicIdentityGenerator`, `GeneratedIdentityCandidate`, Aspect Rank, Soul state, appraisal, owned abilities or persistence.
- Future HUD/rune/chat/audio adapters may render this content.
- Future executable-effect adapters may perform removable Minecraft interactions, but durable identity/effect state must remain Java-owned.

## Player-facing content shape

The nature layer describes what a generated Aspect theme promises without pretending it is a canonical elemental class. Examples include:

- **Ash:** endurance and aftermath;
- **Bell:** warning and resonance;
- **Ember:** preservation and controlled light;
- **Hollow:** absence and concealment;
- **Road:** routes, distance and guidance;
- **Thread:** links, precision and severance;
- **Tide:** changing pressure and rhythm.

The ability layer then provides a bounded expression. Examples:

- **Chime Warning** provides a warning cue rather than omniscience;
- **Mirror Glimpse** exposes a short reflected local impression rather than prophecy;
- **Hollow Step** dampens presence during a short reposition rather than granting true invisibility or teleportation;
- **Shorten the Road** reduces the effort of an existing short route rather than creating paths or ignoring barriers;
- **Low Tide** opens a lull in authored water/rhythm pressure rather than commanding arbitrary bodies of water;
- **Weave Link** creates a temporary DESIGN coordination link rather than binding minds, fate or ownership.

These constraints are deliberate content boundaries, not statements that canon versions of similarly themed powers must behave this way.

## Validation

`AspectAbilityContentCatalogTest` validates:

- exact one-to-one coverage of all 12 current generated nature IDs;
- exact one-to-one coverage of all 24 current generated ability IDs;
- non-blank substantial nature and ability presentation content;
- at least two gameplay hooks per ability;
- broad family diversity rather than one combat template;
- every compatible merged nature/ability pair composes while preserving exact stable IDs and merged display names;
- incompatible pairs fail closed rather than being reinterpreted;
- unknown primitive IDs fail closed;
- selected high-risk evocative abilities retain explicit anti-overclaim boundaries.

No local Gradle/JUnit/client/server execution is claimed from the GitHub connector environment. Hosted CI is reported only if GitHub registers a workflow/status for the exact PR head.

## Integration dependency

None for review or merge. This content is based directly on current `main` and consumes only already-merged procedural identity primitives.

A later Java-owned executable-effect slice may implement one small family at a time. It should consume the already-resolved ability ID, persist any durable effect state in Java, and treat the exact Minecraft mechanics as DESIGN rather than retroactively presenting this descriptive catalogue as canon.
