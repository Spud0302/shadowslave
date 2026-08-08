# Aspect archetype presentation — wave 1

**Status:** bounded player-facing DESIGN content for the existing procedural Aspect archetype primitives.  
**Code:** `AspectArchetypeContentCatalog`  
**Presentation version:** `aspect-archetype-presentation-v1`

## Scope

The Java procedural identity catalogue already composes an Aspect from a nature, archetype and compatible Aspect Ability. This slice gives the existing ten archetype IDs player-facing role/persona meaning without adding another identity system.

The presentation layer receives an already-resolved nature ID and archetype ID. It may explain that pair and format the existing generated name. It does **not** choose, reroll, award, rank, persist or execute an Aspect.

Wave one covers:

| Existing archetype | Presentation cue | DESIGN boundary |
| --- | --- | --- |
| Bearer | Carrier | responsibility/burden, not extra inventory or universal durability |
| Keeper | Custodian | preservation, not ownership or absolute protection |
| Last | Final Holdout | resolve under isolation, not prophecy or mandatory sacrifice |
| Pilgrim | Wayfarer | purposeful journey, not objective revelation or religious identity |
| Seeker | Investigator | investigation, not omniscience, prophecy or automatic truth |
| Sentinel | Watchkeeper | vigilance/warning, not perfect detection or command authority |
| Voice | Herald | signaling/coordination, not mind control or compulsory obedience |
| Wanderer | Drifter | movement/escape, not teleportation or terrain immunity |
| Weaver | Binder | precise connections/patterns, explicitly not Weaver's canon sorcery, fate manipulation or Memory weaving |
| Witness | Observer | observation/evidence, not omniscience, perfect memory, lie detection or prophecy |

Each profile also supplies at least three decision hooks intended to help scenario/UI authors express the archetype through choices rather than treating the generated name as decoration.

## Lore research

Research followed `docs/LORE-SOURCE-POLICY.md`. At the start of this slice, the owner-designated NovelFull access layer listed through **Chapter 3116, Princess of the Underworld**, while official WebNovel exposed **3131 chapters**. The access layer is therefore behind the official publication listing at this research point. Host metadata is used only for freshness; chapter text remains authority.

### Primary and later material checked

- **Chapter 16 — Rebirth:** Sunny's post-First-Nightmare runes expose a separately named Aspect, its Rank, a qualitative Aspect description and a separately named Aspect Ability. This supports player-facing Aspect identity/presentation, not this project's archetype taxonomy.
- **Chapter 353 — Light Bringer:** Nephis' runes expose her named Aspect and qualitative description. Sunny also reflects on the semantic rendering of the name, including a possible `Fire Bearer` sense. This is useful evidence that Aspect names can carry evocative semantic meaning, but it does not establish a universal grammatical construction rule.
- **Chapter 503 — Harsh Reality:** later dialogue explicitly treats every Aspect as unique enough to demand different countermeasures. This weighs strongly against reducing procedural Aspects to a short fixed class list.
- **Chapter 1307 — Before the Nightmare Spell:** natural-ascension material makes clear that supernatural realization cannot safely be described as a project-reconstructed Nightmare Spell generation equation.
- **Chapter 1582 — Banished:** after Sunny loses access to the Nightmare Spell and its runes, his inherent supernatural capabilities remain. The chapter's later clarification treats an Aspect as belonging to the person rather than existing only as Spell-owned UI/state.

## Evidence boundary

### CANON

- Aspects are named, player-visible supernatural identities with qualitative descriptions and distinct Aspect Abilities.
- Known Aspects are highly individual rather than a universal short list of RPG classes.
- Later material establishes that an Aspect can remain part of a person independently of continued access to the Nightmare Spell.
- Aspect names can carry meaningful semantic imagery; Chapter 353 demonstrates interpretation/translation nuance around one known Aspect name.

### INFERRED

- A generated player-facing Aspect benefits from a concise role/persona cue in addition to its nature token and formal name.
- Explicit negative boundaries are useful authoring safeguards so evocative procedural words do not silently acquire unrelated canon powers.
- Scenario hooks can express an archetype through decisions without making the archetype itself a separate mechanical authority.

### DESIGN

- All ten existing archetype names/patterns in `ExpandedIdentityContentCatalog` remain project-authored procedural primitives.
- `Carrier`, `Custodian`, `Final Holdout`, `Wayfarer`, `Investigator`, `Watchkeeper`, `Herald`, `Drifter`, `Binder` and `Observer` are presentation labels only.
- Every role promise, decision hook, presentation tag and anti-overclaim boundary is Minecraft DESIGN.
- `aspect-archetype-presentation-v1` and the exact composition API are DESIGN.

### UNKNOWN

- There is no verified canonical formula for creating or realizing an Aspect.
- There is no verified universal Aspect naming grammar, including no rule that names decompose into a `nature + archetype` construction.
- The canonical relationship, if any, between a person's history, personality, fate, Nightmare deeds, Attributes, Flaw and Aspect realization remains unresolved here.
- There is no canonical `Bearer/Keeper/Seeker/...` Aspect taxonomy or role classification.
- The exact semantic intent behind every canonical Aspect name cannot be generalized from the examples checked.

### COMPATIBILITY

- `ExpandedIdentityContentCatalog` remains authoritative for the stable procedural nature/archetype IDs and existing name patterns.
- `DeterministicIdentityGenerator` remains the current Java DESIGN selector; this presentation catalogue does not alter its inputs, weights or output identity.
- Any future rune/HUD/chat/audio adapter may render the immutable presentation result, but cannot select or persist canonical Aspect identity.
- If #114's separate nature/ability presentation catalogue lands, the two slices can be composed by a higher presentation layer because both consume the same already-resolved Java-owned IDs. Neither needs to depend on the other.

## Validation contract

`AspectArchetypeContentCatalogTest` requires:

1. exact one-to-one coverage of all ten currently generated archetype IDs;
2. substantial DESIGN presentation, at least three decision hooks and non-empty tags for every archetype;
3. distinct role cues and at least thirty authored decision hooks across wave one;
4. all 12 current nature primitives to compose with all 10 current archetypes while preserving the exact resolved IDs and existing `Archetype.formatName(...)` output;
5. explicit anti-overclaim language for high-risk evocative names such as Weaver, Witness, Voice, Wanderer and Pilgrim;
6. unknown nature/archetype IDs to fail closed.

No canonical naming, realization, generation, probability or appraisal formula is claimed by this slice.
