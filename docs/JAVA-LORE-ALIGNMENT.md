# Java lore-alignment gate

**Status:** architecture rule for all Java-era work after datapack `1.0.0`.

**Purpose:** the Java mod and Nightmare Spell modpack are no longer constrained by datapack scoreboards, command timing, four-family tables or a single arena. They must preserve useful tested behaviour while replacing any prototype shortcut that conflicts with the setting.

**Mandatory research procedure:** read the [lore source policy](LORE-SOURCE-POLICY.md) before changing a lore-sensitive mechanic. That policy is the single authority for source selection, evidence labels, later-chapter checks, copyright limits and conflict handling.

## 1. Research authority

Do not maintain a second source hierarchy here. Apply the lore source policy directly: novel chapter text is the authority, the owner-designated third-party host is only a research access layer, official WebNovel is the official-wording/publication cross-check, and community wikis are research aids. Relevant later chapter clarifications must be checked. If this document and the policy ever differ on research procedure, the policy controls and this document must be corrected.

The manhwa adaptation may guide:

- waking-world architecture, clothing and technology;
- character scale and visual presentation;
- rune/status-screen composition;
- the atmosphere and staging of infection, exhaustion and Nightmare transition;
- environmental palette and creature presentation.

It must not silently override novel mechanics when dialogue is compressed, exposition is simplified or a visual choice resolves an ambiguity the novel left open.

Follow the policy's copyright and repository-content limits; this architecture document must contain concise evidence summaries, not source-text archives.

## 2. Canon and game design must remain distinguishable

Every data-driven definition or design document should label important statements as one of:

- **CANON** — directly supported by primary text;
- **INFERRED** — a reasoned synthesis of canon evidence;
- **DESIGN** — a Minecraft mechanic chosen for playability;
- **UNKNOWN** — deliberately unresolved rather than invented.

A good mechanic may be non-canon. The problem is not invention; the problem is presenting invention as though the novel supplied an algorithm.

## 3. Progression model

The Java domain uses three separate concepts.

### Spell-recognised or human progression status

```text
Uninfected / Mundane description
  -> Carrier
  -> Aspirant during the First Nightmare
  -> Dreamer (commonly called Sleeper)
  -> Awakened
  -> Master
  -> Saint
  -> Sovereign
  -> Spirit
  -> God
```

Carrier and Aspirant are not Soul Ranks. Dreamer/Sleeper is the Dormant post-First-Nightmare stage.

### Soul Rank

The seven-rank ladder is:

```text
Dormant
Awakened
Ascended
Transcendent
Supreme
Sacred
Divine
```

`Mundane` is not Rank zero. A person without a ranked Soul Core stores no Soul Rank.

### Awakening path

The model records whether progression is:

- undecided/unawakened;
- assisted by the Nightmare Spell;
- natural/ancient progression.

The natural route must remain possible in the architecture even though the first playable slice follows the Nightmare Spell path.

## 4. First Nightmare lifecycle

The canonical Java sequence is:

```text
infection
  -> Carrier state and worsening supernatural exhaustion
  -> unnatural sleep / trial trigger
  -> Aspirant state
  -> individually owned First Nightmare instance
  -> assigned historical role/body and provisional trial context
  -> central conflict resolution
  -> Spell appraisal
  -> Dreamer/Sleeper with Dormant Soul Rank
  -> permanent Aspect and Flaw revealed/unsealed
```

The future `NightmareInstance` owns:

- assigned historical role/body;
- scenario and reconstructed history;
- central conflict and possible resolutions;
- provisional trial abilities or attributes;
- evidence and meaningful choices;
- owned entities and temporary inventory/state;
- technical recovery information.

Permanent `SoulData` must not store temporary role bodies, boss timers, global creature selectors or trial scratch flags.

## 5. Nightmare design

A Nightmare definition is built around:

```text
role + historical situation + central conflict + possible resolutions
```

It is not universally:

```text
arena + timer + boss
```

Combat and bosses are valid where they resolve the conflict, but reaching a place, escaping, freeing or imprisoning someone, changing history, choosing a faction, preserving something or sacrificing something can also resolve a Nightmare.

The First Nightmare is individual. Later Nightmares may contain multiple challengers, potentially on different sides of the conflict.

## 6. Failure and technical recovery

Ordinary First-Nightmare failure means death and can create a small Gate through the corpse. Later Nightmare death is also real death. Safe low-health ejection and retry are datapack accessibility mechanics, not the canonical default.

Java still requires administrative and crash recovery. Those paths are explicitly technical:

- disconnect recovery;
- server-restart reconstruction;
- corrupt-instance teardown;
- administrator rescue;
- development test exits.

Technical recovery must not be described in-world as a normal mercy granted by the Spell. A configurable accessibility mode may offer non-canon safety, but it must be labelled as such.

## 7. Aspect model

An Aspect belongs fundamentally to the person; the Spell reveals/unseals and appraises it rather than creating it from a simple loot table.

Influences may include:

- innate nature and affinities;
- choices and lived behaviour;
- the assigned Nightmare role/history;
- exceptional deviation from fate during the trial.

Canon provides constraints and examples, not a deterministic generation formula.

Architecture requirements:

- Aspect identity is an instance, not one of four enums;
- Aspect Rank is separate from Soul Rank;
- a Dormant person may possess a higher-ranked Aspect;
- Aspect Abilities are expressions of one supernatural nature, not unrelated spells selected per rank;
- existing abilities may deepen/evolve;
- Aspect evolution is possible but exceptional;
- Aspect Legacy and Legacy Relics are separate later systems;
- temporary First-Nightmare role Aspects do not automatically become the final permanent Aspect.

Third-party spell mods may implement an ability adapter, but they never own the canonical Aspect identity.

## 8. Flaw model

A Flaw is not a random negative potion effect or a reward selected solely from the most visible trial statistic. It is tied to the person's imperfection, identity, power, choices, attachments or fate, and is innately connected to the Aspect.

Architecture requirements:

- Flaw identity is an instance with a formal name only when the game has actually established one;
- effect and formal name remain separate fields;
- meaningful trial evidence may inform revelation, but no project document may call a scoring formula canonical;
- the same supernatural nature may become harmful, exact a price, attack something cherished or create a personally cruel benefit;
- a Flaw is normally worked around, not removed like a temporary debuff;
- the drawback must remain server-authoritative and mechanically real.

The datapack's four Flaw families remain import compatibility identities and design examples. They are not the Java generator's complete taxonomy.

## 9. Attributes, Memories, Echoes and ranks

Future systems must preserve these boundaries:

- canon Attributes are named supernatural traits, not generic Strength/Vitality statistics;
- Soul Shards strengthen/saturate a core but are not a universal rank-up button;
- ordinary Memories are soul-stored but can be transferred; true Bound/Soulbound relics are exceptional;
- ordinary Echoes are static soulless replicas; growth-capable Shadows are a separate Aspect-specific system;
- creature Rank and Class are separate axes;
- human Soul Rank, human title, Aspect Rank and core count/Class are separate concepts.

## 10. Datapack compatibility without datapack constraints

The frozen datapack remains valuable for:

- installation and release evidence;
- the tested Mundane -> Carrier -> First Nightmare -> Sleeper loop;
- migration fixtures;
- identity persistence expectations;
- server-authoritative drawbacks;
- teardown and deterministic testing lessons.

Java must not preserve these datapack limitations as architecture:

- score ranges as identity models;
- four Aspect roots as the complete universe;
- four Flaw families as the complete universe;
- one shared Nightmare creature or bossbar;
- safe ejection as the normal lore outcome;
- ordinary sleep as the infection cause;
- a boss kill as the definition of completion;
- exact command-based attribute implementations.

Imported datapack players retain their identity and mechanics without pretending those prototype generation rules are canon.

## 11. Two-track consequence

Both the standalone mod and Nightmare Spell modpack use the same canonical Java core.

The modpack may reuse:

- spell visuals and generic execution;
- creatures and encounters;
- structures and world generation;
- equipment slots and presentation systems.

The shared core owns:

- progression status and awakening path;
- Soul Rank and Aspect Rank;
- permanent Aspect/Flaw identity;
- Nightmare instance ownership and appraisal;
- migration and history;
- server validation and client snapshots.

## 12. Gate before further feature work

Before implementing natural infection or a real Nightmare instance:

1. migrate the alpha Soul schema so Mundane is no longer a Soul Rank;
2. add the explicit Carrier -> Aspirant -> Dreamer lifecycle;
3. separate Aspect Rank from Soul Rank;
4. update the shared vertical-slice acceptance tests;
5. preserve alpha save compatibility through codec aliases;
6. pass unit, physical-client and dedicated-server CI;
7. manually verify the Soul screen displays the separated concepts.

Only then should the project build the Nightmare registry, historical roles, central conflicts and appraisal engine.
