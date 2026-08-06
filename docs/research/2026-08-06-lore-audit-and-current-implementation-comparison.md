# Shadow Slave lore audit and current implementation comparison

**Research date:** 2026-08-06  
**Repository baseline:** `main@37287c4ccdf3709440be034f2ce5b71eb3d275cc`  
**Report status:** research and architectural recommendation; not automatically an approved implementation contract  
**Spoilers:** unrestricted through the research boundary below

## 1. Purpose

This report compares verified Shadow Slave mechanics with three products in this repository:

1. the frozen vanilla datapack;
2. the current NeoForge Java core and playable preview;
3. the planned Nightmare Spell modpack.

It does not replace:

- `docs/LORE-SOURCE-POLICY.md`;
- `docs/JAVA-LORE-ALIGNMENT.md`;
- `docs/PREVIEW-LORE-DECISIONS.md`;
- `docs/NIGHTMARE-SEED-ROADMAP.md`;
- `PROJECT-STATUS.md`;
- `GPT_HANDOFF.md`;
- `ISSUES.md`.

Where this report identifies a contradiction or a stronger later-lore constraint, the normal review process should update the relevant authority rather than treating this research file as a silent override.

## 2. Source and coverage boundary

The project source hierarchy in `docs/LORE-SOURCE-POLICY.md` was followed:

1. novel chapter text as the mechanical and terminology authority;
2. official WebNovel for publication and wording cross-checks;
3. adaptation material for compatible staging and visual reference only;
4. community wikis as indexes that require chapter verification;
5. existing repository research and accepted design decisions;
6. explicit Minecraft DESIGN where canon does not supply an algorithm.

At the time of this report:

- the official WebNovel catalogue displayed **Chapter 3126, “Inferno”**, with 3,127 catalogue entries including auxiliary material;
- the owner-designated full-chapter access layer displayed **Chapter 3116, “Princess of the Underworld”** as its latest chapter.

Therefore the detailed chapter-text audit in this report is bounded at **Chapter 3116**. Chapters **3117–3126** were not available through the designated full-chapter research layer and are not represented as fully checked. Official catalogue metadata is not a substitute for reading those chapters.

No novel text is reproduced in this repository. Evidence is paraphrased and associated with chapter numbers or short source references.

## 3. Evidence labels

- **CANON** — directly supported by novel chapter text.
- **INFERRED** — a reasoned interpretation from identified canon evidence.
- **DESIGN** — a Minecraft implementation decision not provided by canon.
- **UNKNOWN** — deliberately unresolved.
- **COMPATIBILITY** — retained prototype or migration behaviour that is not promoted into universal lore.

## 4. Executive verdict

The repository has already corrected most of the original datapack’s largest lore mistakes.

The current Java-era architecture correctly understands that:

- Carrier, Aspirant, Dreamer/Sleeper and Awakened are distinct stages;
- Soul Rank and Aspect Rank are separate;
- First-Nightmare completion produces a Dormant Dreamer/Sleeper rather than an Awakened;
- a Nightmare is a reconstructed role, situation and central conflict rather than a universal boss arena;
- normal First-Nightmare failure is death, not safe ejection;
- Aspects and Flaws are personal identities, not four universal classes;
- Attributes are supernatural traits, not generic RPG statistics;
- Memories are soul-stored but normally transferable;
- Echoes are soulless replicas and are not Sunny’s growth-capable Shadows;
- Soul Shards strengthen a core but are not a universal rank-up button;
- later Nightmares and Seeds require architecture beyond the frozen datapack.

The largest remaining lore problems are no longer basic terminology errors. They are **runtime model limitations**:

1. successful Nightmare appraisal happens after teleport and teardown in current Java code, while the canonical presentation is Nightmare resolution/collapse, appraisal and transformation, then return;
2. `SoulIdentityData` requires an Aspect and Flaw to appear as a complete pair, and each record requires a known formal name;
3. an Aspect record contains one `abilityId`, which is too narrow for innate abilities, rank abilities, evolving abilities and Aspect Legacies;
4. the core has no first-class Attributes, True Name, Soul Core, saturation, essence, Class/core count, corruption, Memories, Echoes or progression history;
5. `NightmareInstance` is correctly shaped for one individual First Nightmare, but cannot represent one later shared Nightmare with multiple participants and independent outcomes;
6. the preview scenario has no persisted conflict state or resolution graph;
7. the direct campfire interaction is a development completion trigger rather than a mature conflict-resolution system;
8. the single dark Nightmare dimension is a preview stage, not a valid universal Dream Realm aesthetic;
9. the current model chooses one interpretation of an unresolved First-Nightmare Soul Core timing question without recording the uncertainty;
10. the modpack integration layer is still design-only, so external systems have not yet proved that permanent identity survives provider removal.

The immediate lore-aligned priority remains the same as the engineering roadmap’s priority: make resolution, outcome, appraisal and teardown durable and correctly ordered before expanding content.

# Part I — Current product comparison

## 5. Frozen datapack

### What it proves well

The datapack remains useful as a tested vertical-slice and migration baseline:

- player infection/progression state can be persistent;
- entry guards should be centralized;
- every exit reason should share one teardown path;
- drawbacks must be server-authoritative and mechanically observable;
- a player-facing Soul readout must reflect persistent identity;
- automated behavioural gates are part of the feature;
- old player identities must be migrated rather than rerolled.

### What must remain frozen as prototype machinery

The datapack should not define future lore architecture through:

- ordinary sleep as the literal infection cause;
- safe low-health ejection as normal Spell behaviour;
- one global Nightmare dimension, bossbar or creature selector;
- one timer-to-creature-to-kill scenario shape;
- four Aspect roots as the setting’s complete identity space;
- four behaviour-derived Flaw families as a canon formula;
- score ranges as permanent supernatural identity;
- `ss_rank` or any single integer standing in for title, Soul Rank, Aspect Rank and core state.

Its identities remain **COMPATIBILITY** records for existing worlds, not evidence that the novel uses those generation tables.

## 6. Java playable preview

### Strong alignment

The current Java core already provides several correct boundaries:

- schema-versioned server-owned Soul state;
- separate `SpellState`, `SoulRank` and `AwakeningPath`;
- independent Aspect Rank;
- persistent full identity records outside the summary Soul record;
- one Nightmare entry choke point;
- persistent exact-instance ownership;
- role and scenario IDs on `NightmareInstance` rather than permanent Soul state;
- exact owned-entity UUIDs;
- technical recovery distinguished from canonical success/failure;
- fixed preview content labelled DESIGN;
- server-owned snapshots and read-only client presentation;
- transactional datapack migration and identity retention.

### Deliberately narrow preview scope

The preview is not a general Shadow Slave simulator. It currently implements:

```text
Uninfected
-> development command
Carrier
-> The Last Signal entry
Aspirant / Dormant
-> direct signal-fire interaction
Dreamer / Dormant
-> Last Light / Kindle / Cold Ash
```

That is a valid vertical-slice scaffold. It should not be judged as wrong merely because later systems are absent. The issue is whether its current record shapes become permanent before the missing lore distinctions are represented.

## 7. Nightmare Spell modpack

The modpack track is currently a design shell. Its strongest existing decision is that the shared Java core—not KubeJS, quests, spell mods, Curios or another dependency—owns:

- progression;
- Soul Rank and Aspect Rank;
- permanent Aspect and Flaw identity;
- Nightmare lifecycle and appraisal;
- migration;
- history;
- server validation and Soul snapshots.

That ownership boundary is lore-compatible and should remain non-negotiable.

The modpack has not yet proved:

- optional ability-provider removal;
- provider-independent Memory identity;
- provider-independent creature/Echo identity;
- exact dependency licensing and side requirements;
- clean client/server packages;
- a shared acceptance slice equivalent to the standalone core.

# Part II — System-by-system lore comparison

## 8. Infection and Carrier progression

### Canon

**CANON:** The Nightmare Spell infects a human and a First-Nightmare Seed develops in or through the person’s soul. Symptoms include mounting fatigue and sleepiness, eventually ending in unnatural sleep and the First Trial. Chapter 1 establishes that this is a process lasting days in Sunny’s case, not an ordinary healthy sleep event.

**CANON:** Failure in the First Nightmare means real death and opens a small Gate through the dead Aspirant, allowing a Nightmare Creature into the waking world.

**CANON:** Natural Awakening can cleanse the modest Seed without undertaking a First Nightmare, as Rain demonstrates when forming her core and Awakening.

**INFERRED:** The exact timing, symptom curve and trigger are not one universal game-ready algorithm. Individual cases and institutional intervention can differ.

### Frozen datapack

The first-normal-sleep trigger is accessible and deterministic, but it is **DESIGN**, not literal canon.

### Current Java

The preview uses `/shadowslave preview_begin` and explicitly labels it a development shortcut. That is more honest than silently claiming a bed causes infection.

### Required future model

Add an `InfectionState` or equivalent with:

```text
source
seed_state
infection_time
symptom_stage
call_pressure
suppression_state
trigger_policy
```

Server configuration may choose a gameplay trigger, but the default presentation should preserve the distinction between:

- infection;
- worsening Carrier symptoms;
- forced First-Nightmare transition.

Do not store the whole process as `sleptOnce=true`.

### Verdict

**Current Java: aligned development shortcut.**  
**Datapack: acceptable frozen DESIGN.**  
**Future implementation: unimplemented.**

## 9. Aspirant state and the Dormant-core timing ambiguity

### Canon evidence

Chapter 2 displays Sunny’s First-Nightmare status as:

- Rank/status: Aspirant;
- Soul Core: Dormant;
- temporary/role-linked Aspect and Attributes.

However, Chapter 16 describes the post-Nightmare transformation as giving the previously formless soul a more solid shape and forming the Soul Core, while Chapters 1306–1307 explicitly contrast natural Awakening with the modern Spell path by saying modern humans form the core after the First Nightmare and gain conscious essence control after the first Dream Realm return.

### Interpretation

Two safe observations coexist:

1. **CANON:** the First-Nightmare interface exposes a Dormant Soul Core during the trial;
2. **CANON:** the completed First Nightmare performs the waking person’s Soul Core formation/solidification.

What remains **UNKNOWN** is the best ontological label for the in-trial core:

- a provisional core supplied by the role/trial;
- the person’s nascent core represented before final formation;
- a Spell abstraction that should not be mapped one-to-one into permanent waking-world storage.

### Current Java

`SoulData.asAspirant()` persists `SoulRank.DORMANT` on the player before success. This matches the visible First-Nightmare status, but it chooses a permanent-record interpretation that later exposition complicates.

### Recommendation

Do not immediately remove Dormant from Aspirant—the shared acceptance slice and existing tests depend on it. Instead:

- document this as **INFERRED/DESIGN**, not an uncontested canon fact;
- consider a future `CoreFormationState` such as `PROVISIONAL`, `FORMED`, `AWAKENED`;
- keep temporary role/body core details on the Nightmare participant record where appropriate;
- require another focused chapter review before schema 3 makes this distinction permanent.

### Verdict

**Current implementation: plausible but overconfident.**  
**Priority: documentation and schema-design question, not an emergency gameplay defect.**

## 10. Historical role, borrowed body and trial context

### Canon

**CANON:** The First Nightmare assigns a historical role/body. Chapter 218 establishes that role selection is not random and can reflect meaningful similarity of fate, life circumstances and body.

**CANON:** The exact role-selection law is not known.

**CANON:** The borrowed role can contribute temporary or inherited elements. Sunny’s Temple Slave Aspect and some Attributes came from the nameless slave whose role/body he inhabited, while [Fated] was innate to Sunny.

**CANON:** Trial events can produce persistent change. Sunny’s role Aspect later evolved into his true Divine Aspect through the First-Nightmare outcome and appraisal.

### Current Java

`NightmareInstance` correctly stores `historicalRoleId` and scenario ownership outside `SoulData`. The Last Signal assigns `last_watchkeeper` as DESIGN.

What is absent:

- historical-body identity;
- provisional inventory;
- provisional role Attributes or abilities;
- meaningful role-to-player affinity evidence;
- separation between inherited role traits and final appraised identity;
- role history and expected fate.

### Recommendation

Introduce a participant record with:

```text
participant_id
assigned_role_id
borrowed_body_profile
provisional_attributes
provisional_abilities
provisional_inventory
expected_history
role_affinity_evidence
player_action_evidence
survival_state
eligibility_state
```

The engine must allow a role contribution to persist after appraisal without assuming every temporary role power becomes permanent.

### Verdict

**Current architecture: correctly located but incomplete.**

## 11. Nightmare structure and central conflict

### Canon

**CANON:** Nightmares reconstruct or simulate historical situations around a central conflict. The exact metaphysical reality of the inhabitants is not fully settled merely because early characters call them illusions.

**CANON:** Completion is not universally a boss kill. The Second Nightmare resolves through Hope’s liberation; the Third Nightmare contains shared and personal conflict layers; First-Nightmare resolutions are inseparable from role and circumstance.

**CANON:** Later Nightmares can have multiple challengers, different roles and potentially different sides.

**INFERRED:** `role + historical situation + central conflict + possible resolutions` is a strong implementation synthesis, but not a literal in-novel data schema.

### Current Java

The Last Signal’s class comment correctly states a role and conflict. Combat is optional. However, the runtime conflict is currently:

```text
right-click exact campfire
-> ignite block
-> success
```

There is no persisted scenario state, event history, prerequisite graph, named terminal resolution or challenger outcome.

### Recommendation

Implement:

- `NightmareDefinition`;
- `ScenarioEvent`;
- `ResolutionGraph`;
- `ConflictState`;
- `TerminalResolution`;
- `ChallengerOutcome`;
- event/evidence persistence.

Refactor the campfire click into one event whose acceptance depends on scenario state. At least two materially different DESIGN endings should prove the architecture.

### Verdict

**Conceptual docs: strongly aligned.**  
**Current runtime: prototype trigger only.**

## 12. Resolution, appraisal, transformation and return ordering

### Canon

The repeated canonical presentation is:

```text
Nightmare conflict ends
-> Nightmare collapses / challenger enters the space between dream and reality
-> Spell recounts and appraises the trial
-> boons, Aspect evolution, True Name, abilities or Rank transition occur
-> challenger wakes/returns
```

Chapter 15 and Chapter 743 both place appraisal in the void after the Nightmare ends and before ordinary return.

### Current Java

`NightmareService.resolveSignalFire()` currently performs:

```text
ignite altar
-> exit(player, SUCCESS)
   -> teleport to return dimension
   -> remove owned entity
   -> consume registry instance
-> PreviewAppraisalService.appraise(...)
```

This has two consequences:

1. **Lore/presentation mismatch:** the player is physically returned and the instance is gone before appraisal.
2. **Confirmed persistence defect:** a crash after teardown but before appraisal can leave an Aspirant with no active Nightmare and no recoverable completion record.

The code’s recovery-to-Carrier catch prevents some live exceptions from stranding the player, but it cannot repair a process failure after the durable registry record is gone.

### Required architecture

Persist an explicit completion transaction or outbox:

```text
ACTIVE
-> TERMINAL_RESOLUTION_RECORDED
-> CHALLENGER_OUTCOME_RECORDED
-> APPRAISAL_PENDING
-> APPRAISAL_COMMITTED
-> RETURN_PENDING
-> RETURN_COMMITTED
-> TEARDOWN_COMMITTED
```

The exact final order may vary for safety, but the system must always retain enough durable evidence to replay the remaining phases exactly once.

A lore-shaped presentation can place the player in an appraisal state/void before return while technical storage uses an idempotent transaction underneath.

### Verdict

**P0 mismatch and correctness defect.**  
This is the most important change before broader content work.

## 13. Appraisal criteria and fate

### Canon

Chapter 743 has Sunny infer that appraisal may value how strongly challengers deviate from the predestined flow of history. Chapter 744 repeats his confidence in that idea.

The safe classification is:

- **CANON:** the Spell narrates deeds, choices, transformations and conflict resolution;
- **CANON:** two enormous feats can receive different appraisals;
- **INFERRED:** resistance to or deviation from fate is a central appraisal variable;
- **UNKNOWN:** exact weights, scoring rules and whether that explanation is complete.

### Current repository

The docs mostly preserve this boundary. The preview uses one fixed result and explicitly says canon does not provide a deterministic formula.

### Recommendation

Future appraisal should consume evidence such as:

- assigned role and expected history;
- meaningful deviations;
- sacrifices and preserved attachments;
- contribution to the terminal resolution;
- survival and eligibility;
- discovered truths;
- relationship to fate where the scenario defines it.

But all weights remain **DESIGN**. Do not call a behaviour score, kill count, speedrun metric or branch table canonical.

### Verdict

**Current documentation: aligned.**  
**Future algorithm: intentionally open.**

## 14. Failure, death and corpse Gate consequences

### Canon

**CANON:** normal First-Nightmare failure is death.

**CANON:** the dead Aspirant opens a small Gate that admits a Nightmare Creature.

**CANON:** later Nightmare death is also real death; surviving challengers can continue.

### Frozen datapack

Safe low-health ejection and retry are accessibility/prototype mechanics, not canon.

### Current Java

`canonicalDeath()` tears down the owned instance, clears preview identity and states that Minecraft respawn is a development accommodation. This is honest and preferable to pretending the Spell rescued the player.

Missing:

- persistent death outcome/history;
- corpse Gate or explicit placeholder event;
- server configuration separating canon mode from accessibility mode;
- later shared-instance behaviour when one participant dies.

### Recommendation

Define:

```text
CANON_DEATH
ACCESSIBILITY_FAILURE
TECHNICAL_RECOVERY
ADMIN_ABORT
```

as distinct outcomes. Add the corpse-Gate consequence only after its scope, server grief implications and accessibility options are designed deliberately.

### Verdict

**Current preview: honest placeholder.**  
**Full consequence: unimplemented.**

## 15. Human progression status, Soul Rank and titles

### Canon

The seven Soul Ranks are:

```text
Dormant
Awakened
Ascended
Transcendent
Supreme
Sacred
Divine
```

Human/social titles include:

```text
Dreamer/Sleeper
Awakened
Master
Saint
Sovereign
Spirit
God
```

Carrier and Aspirant are not Soul Ranks. `Mundane` is not Rank zero.

The Spell-assisted sequence is broadly:

```text
Carrier
-> First Nightmare
Dreamer/Sleeper, Dormant
-> first Dream Realm return
Awakened
-> Second Nightmare
Master, Ascended
-> Third Nightmare
Saint, Transcendent
-> Fourth Nightmare
Sovereign, Supreme
-> Fifth Nightmare
Spirit, Sacred
-> Sixth Nightmare
God, Divine
```

Late canon also reveals a Seventh Nightmare whose purpose is not an eighth Soul Rank.

### Current Java

`SpellState` correctly separates current stage from `SoulRank`, but it stores social/metaphysical later titles in a type named `SpellState`:

```text
AWAKENED, MASTER, SAINT, SOVEREIGN, SPIRIT, GOD
```

This is workable, but increasingly awkward for natural progression and for a character no longer connected to the Spell.

### Recommendation

Not a current blocker, but schema 3 should consider:

```text
ProgressionStage
AwakeningPath
SoulRank
DisplayTitle (derived/localized)
SpellConnectionState
```

Do not hardcode six Nightmares as the complete cosmic system, and do not invent a Divine gameplay package merely from rank symmetry.

### Verdict

**Current model: correct core distinction; naming may need future refinement.**

## 16. Dreamer to Awakened transition and Dream Realm travel

### Canon

**CANON:** First-Nightmare completion creates a Dreamer/Sleeper, not an Awakened.

**CANON:** Sleepers are forcibly transported to the Dream Realm at the winter solstice.

**CANON:** successful return through a Gateway produces actual Awakening, conscious Soul Essence control, a stronger core/body and a new Aspect Ability.

**CANON:** travel rules change with Rank:

- Sleepers are trapped until they find a Gateway;
- Awakened are drawn to their anchor when sleeping and return on waking;
- Masters can travel physically and voluntarily;
- Saints and Sovereigns interact with realms and Gates at qualitatively different scales.

### Current Java

The core stops at Dreamer and does not implement:

- winter-solstice transition;
- Dream Anchor;
- Gateway return;
- recurring sleep travel;
- conscious essence control;
- actual Awakening.

The bundled Nightmare dimension is a trial space, not a persistent Dream Realm implementation.

### Recommendation

Do not build Awakening as `complete another boss arena`. Build a distinct persistent survival/exploration phase:

```text
Dreamer
-> forced first Dream Realm arrival
-> persistent region survival
-> discover/reach Gateway
-> return
-> Awakened transition
```

### Verdict

**Correctly deferred; major future milestone.**

## 17. Soul Core, essence, saturation, Rank and Class

### Canon

**CANON:** A Soul Core is a nexus within the soul, not the soul itself.

**CANON:** Soul Shards and essence strengthen/saturate the core. Saturation and qualitative Soul Rank are separate.

**CANON:** normal humans have one core.

**CANON:** multiple human cores and Beast-to-Titan Class are exceptional traits associated with Divine Aspect holders or similarly abnormal constitutions, not the default human progression loop.

**CANON:** Nightmare Creatures have independent Rank and Class axes.

### Current Java

`SoulData` stores only optional `SoulRank`. It does not store:

- core formation state;
- essence reserve/capacity;
- saturation;
- core type;
- core count;
- Class;
- exceptional resource types;
- natural refinement state.

That is appropriate for the preview but insufficient for the Dream Realm and later progression.

### Recommended model

```text
CoreState
├── formation_state
├── core_type
├── soul_rank
├── core_count
├── class_if_applicable
├── saturation
├── saturation_capacity
├── essence_current
├── essence_capacity
├── essence_control_state
└── exceptional_rules
```

Never make Sunny’s Shadow Fragments or multi-core progression the normal player template.

### Verdict

**Preview omission, future foundational schema work.**

## 18. Aspects

### Canon

**CANON:** An Aspect fundamentally belongs to the person. The Nightmare Spell can reveal, unseal, translate and accelerate its expression; it does not create the metaphysical potential from nothing.

**CANON:** natural Awakened can exist without having unsealed an Aspect. Many historically did.

**CANON:** the First-Nightmare role and events can contribute to the Aspect’s realized form.

**CANON:** Aspect Rank is separate from Soul Rank.

**CANON:** Aspect Rank can exceptionally evolve.

**CANON:** one Aspect can produce multiple abilities across ranks, an Innate Ability, evolving existing abilities and an Aspect Legacy.

**CANON:** formal Aspect names are often not revealed to the reader even when abilities and Rank are known.

### Current Java

`AspectInstanceData` requires:

```text
instanceId
formalName
aspectRank
natureId
abilityId
provenance
```

Strengths:

- instance identity;
- provider-independent internal IDs;
- independent Aspect Rank;
- nature and provenance fields.

Limitations:

1. `formalName` is mandatory and non-blank;
2. one `abilityId` implies one ability expression;
3. no Innate Ability;
4. no ability-by-rank collection;
5. no evolving ability history;
6. no Aspect Legacy;
7. no reveal/unsealing state;
8. no description/translation variants;
9. no distinction between unknown formal name and no Aspect.

### Recommended model

```text
AspectInstance
├── instance_id
├── realization_state
├── formal_name_state
├── formal_name?
├── display_translation_variants[]
├── aspect_rank
├── nature/source_elements[]
├── innate_ability?
├── abilities_by_rank[]
├── evolved_abilities[]
├── aspect_legacy?
├── role_inheritance
├── provenance
└── history[]
```

External spell mods may execute an ability, but the internal ability record and ownership remain canonical.

### Verdict

**Current record: good preview instance, too narrow for general lore.**

## 19. Flaws

### Canon

**CANON:** A Flaw is tied to imperfection, identity and the person’s supernatural nature. It is not a standard negative potion effect.

**CANON:** natural Awakened can remain without finding/unsealing a Flaw until later progression requires it.

**CANON:** Flaws vary enormously:

- truth/obligation constraints;
- social or perceptual effects;
- resource tension;
- impulse or behavioural pressure;
- attachment and loss;
- long-term body changes;
- benefits carrying cruel personal cost;
- effects whose exact rule remains hidden or inferred.

**CANON:** friends and enemies may deduce a Flaw without knowing a formal Spell-given name.

**UNKNOWN:** the universal mechanism by which a specific person’s Flaw is determined or revealed.

### Current Java

`FlawInstanceData` requires:

```text
instanceId
formalName
effectId
provenance
```

The current Cold Ash implementation is a mechanically real DESIGN burden, which is appropriate for the preview.

General-model gaps:

- formal name cannot be unknown;
- effect cannot be partially understood;
- no discovery state;
- no hidden rule;
- no parameter set;
- no behavioural or obligation constraint abstraction;
- no long-horizon consequence;
- no relationship to Aspect nature;
- no player-facing versus admin-facing knowledge boundary.

### Recommended model

```text
FlawInstance
├── instance_id
├── realization_state
├── formal_name_state
├── formal_name?
├── public_description?
├── effect_definition_id
├── parameters
├── player_knowledge_state
├── admin_diagnostic_state
├── aspect_relationship
├── causal_evidence
├── provenance
└── history[]
```

Do not force every Flaw into an equal-and-opposite balance formula or a continuously active combat debuff.

### Verdict

**Current effect: valid DESIGN.**  
**Current general record: substantially too narrow.**

## 20. Paired Aspect/Flaw invariants and natural progression

### Current Java invariant

`SoulIdentityData` requires Aspect and Flaw records to be both present or both absent. `SoulData` correctly limits its mandatory post-Dreamer pair to the Nightmare Spell path.

### Lore issue

For the Spell-assisted First-Nightmare path, paired revelation is a reasonable current invariant.

For natural progression, later canon establishes that a person can:

- Awaken without an Aspect;
- exist without a revealed Flaw;
- need both before Transcendence;
- potentially unseal identity elements through separate life events rather than one Spell appraisal package.

The text does not establish that Aspect and Flaw must always become known in the same transaction on the natural path.

### Recommendation

Keep the current pair invariant for the existing `NightmareSpellFirstAppraisal` operation. Do not retain it as a universal `SoulIdentityData` constructor rule when natural progression is implemented.

Use path-specific transition validation:

```text
Spell First Nightmare appraisal -> pair required
Natural Awakened -> either/both may remain unrevealed
Natural Transcendence eligibility -> Aspect and Flaw required
```

### Verdict

**Correct for current path; incorrect as a future universal identity invariant.**

## 21. Attributes

### Canon

**CANON:** Attributes are named supernatural traits and affinities, such as [Fated], [Child of Shadows] or lineage-derived Weaves. They are not a universal Strength/Vitality/Endurance stat block.

**CANON:** Attributes may be:

- innate;
- inherited from a First-Nightmare role;
- granted by lineage;
- produced by exceptional events;
- evolved during advancement.

### Current products

The old datapack’s generic physical readout does not map cleanly to canon Attributes.

The Java core has no Attribute records yet.

### Recommendation

```text
AttributeInstance
├── instance_id
├── formal_name_state
├── formal_name?
├── source
├── passive_effects[]
├── evolution_state
├── reveal_state
└── history[]
```

Minecraft physical attributes may be execution details under an Attribute or ability, but should not be presented as the canon supernatural Attribute category.

### Verdict

**Unimplemented; high-value schema addition before procedural appraisal.**

## 22. True Names, Names and fate

### Canon

**CANON:** True Names are rare, powerful and distinct from nicknames or ordinary titles.

**CANON:** a True Name can be awarded through exceptional achievement, including a First Nightmare, but is not guaranteed.

**CANON:** True Names are tied to fate. Sunny loses his True Name when becoming fateless while his Aspect, Flaw and Innate Ability remain.

**CANON:** True Names can anchor identity and interact with Sorcery of Names or Aspect-specific conditions.

**CANON:** natural progression can produce a True Name independently of the Nightmare Spell’s ordinary First-Nightmare package.

### Current Java

No True Name field or history exists.

### Recommended model

```text
NameState
├── ordinary_name
├── true_name?
├── true_name_source
├── true_name_active
├── fate_connection_state
├── known_by[] / disclosure policy
└── history[]
```

Do not model a True Name as cosmetic text. Do not infer that every player should receive one.

### Verdict

**Major future omission.**

## 23. Aspect Abilities, provider adapters and external spell mods

### Canon

Aspect Abilities are expressions of one supernatural nature. They are not an unrelated spell catalogue selected from generic schools.

An Aspect may have:

- an Innate Ability;
- a Dormant ability;
- later abilities gained through Rank;
- existing abilities that evolve;
- a Transformation Ability;
- a Domain-related expression;
- an Aspect Legacy separate from the normal ability progression.

### Current Java and modpack plan

Kindle is stored as one internal ability ID and executed by core Java effects. The modpack plan correctly proposes that an external spell mod can act as an execution provider while the core retains identity.

### Required provider contract

The first adapter must prove:

- internal ownership validation;
- core-owned cooldown and costs;
- external mana does not silently become Soul Essence;
- save loads when provider is missing;
- UI keeps identity when execution is unavailable;
- reinstalling the provider restores execution without rerolling or migration;
- provider version changes do not rewrite the Aspect.

### Verdict

**Architecture direction aligned; adapter proof unimplemented.**

## 24. Memories

### Canon

**CANON:** A Memory is a specific Woven artifact architecture that can exist as essence, be stored in the Soul Sea and be summoned into physical form.

**CANON:** ordinary Memories are transferable, normally through deliberate physical contact and ownership transfer. They are not universally permanent soulbound gear.

**CANON:** Memories can be awarded, transferred, crafted and exceptionally altered or grown.

**CANON:** Rank and Tier are separate classifications.

**CANON:** complete destruction can be permanent; ordinary damage can repair while dismissed.

**CANON:** true Bound/Soulbound relics are exceptional.

### Current products

Memories are unimplemented in Java and modpack runtime.

A Curios-style accessory layer could be useful for active equipment presentation, but it cannot be the canonical Memory store.

### Recommended model

```text
MemoryInstance
├── instance_id
├── formal_name_state
├── formal_name?
├── rank
├── tier
├── memory_type
├── enchantments[]
├── owner_id
├── soul_storage_state
├── summoned_state
├── durability/destruction_state
├── transfer_rules
├── bound_state
├── provider_binding?
└── provenance/history
```

A missing equipment provider must not delete or reroll the Memory.

### Verdict

**Correctly deferred; integration boundary must be internal-first.**

## 25. Echoes and Aspect-specific summons

### Canon

**CANON:** An Echo is a soulless magical replica, not the captured original soul.

**CANON:** Echoes can reproduce meaningful combat abilities, be summoned/dismissed, transferred and permanently destroyed.

**CANON:** artificial Echoes exist.

**CANON:** ordinary Echoes are generally static replicas rather than growth-capable companions.

**CANON:** Sunny’s Shadows are an Aspect-specific transformation and progression system, not the definition of Echoes.

### Current products

Echoes are unimplemented. The current owned Husk is a temporary scenario entity, not an Echo.

### Recommended model

```text
EchoInstance
├── instance_id
├── source_template_snapshot
├── rank
├── class
├── ability_package
├── owner_id
├── summoned_state
├── damage/destruction_state
├── transfer_state
├── provider_binding?
└── provenance
```

Aspect-specific summons—Shadows, Reflections or other exceptional systems—must use separate types and rules.

### Verdict

**Unimplemented; do not delegate identity to a pet mod.**

## 26. Corruption

### Canon

**CANON:** Corruption is a metaphysical transformation/influence associated with the Void, not merely moral evil or a potion debuff.

**CANON:** it can affect soul, mind, body and identity.

**CANON:** sources and potency vary. Forbidden knowledge itself can transmit Corruption.

**CANON:** death cleanses Corruption, and natural core formation/Awakening can cleanse the modest Seed within a human.

**CANON:** Rank, Will, Domain and exceptional powers can alter resistance.

### Current products

No corruption state exists. The preview pursuer and dark environment use corruption-flavoured presentation only.

### Recommended model

```text
CorruptionState
├── source
├── potency
├── infection_depth
├── soul_manifestations
├── mind_manifestations
├── body_manifestations
├── identity_manifestations
├── resistance_sources
├── suppression_state
└── history
```

Do not begin with one generic `corruptionPercent` and then treat it as complete lore.

### Verdict

**Unimplemented; later system requiring careful source-specific design.**

## 27. Nightmare Seeds, Gates and Gateways

### Canon

**CANON:** the First-Nightmare Seed is internal to an infected person; later Seeds grow in the Dream Realm.

**CANON:** later Seeds can be challenged before or after bloom.

**CANON:** an unconquered Seed can bloom into a Nightmare Gate in the waking world.

**CANON:** killing the initial creatures or Gate Guardian does not permanently close the threat while the Seed remains unconquered.

**CANON:** a Gateway and a Nightmare Gate are not the same concept.

**CANON:** Seed conquest is achieved by resolving the contained Nightmare, not by mining or clicking the exterior Seed object.

### Current products

No `SeedRecord` exists. The Last Signal campfire is a scenario objective, not a Seed, which is good. Future work must preserve that distinction.

### Recommended model

```text
SeedRecord
├── seed_id
├── category
├── discovery_state
├── dream_realm_location
├── maturity/bloom_state
├── eligibility_rules
├── contained_nightmare_definition
├── active_instance_id?
├── gate_state?
└── post_resolution_state
```

### Verdict

**Conceptual roadmap aligned; runtime unimplemented.**

## 28. Nightmare Creatures: Rank and Class

### Canon

Nightmare Creatures have two independent axes.

Rank:

```text
Dormant, Awakened, Fallen, Corrupted, Great, Cursed, Unholy
```

Class/core count:

```text
Beast, Monster, Demon, Devil, Tyrant, Terror, Titan
```

Higher Class introduces qualitative differences such as intelligence, abilities, minions or environmental influence—not only larger health and damage numbers.

### Current products

The preview uses one vanilla Husk placeholder without a creature Rank/Class model.

### Recommendation

Build a provider-independent creature archetype and instance model before adding large content mods. An external mob supplies rendering, animation or AI components; the core or scenario definition owns lore classification and objective meaning.

### Verdict

**Placeholder only; future content foundation required.**

## 29. Individual First Nightmares versus shared later Nightmares

### Canon

**CANON:** the First Nightmare is individually tailored.

**CANON:** later Nightmares can contain multiple challengers in different roles, potentially on opposing sides.

**CANON:** one challenger can die while the shared Nightmare continues.

### Current Java

`NightmareInstance` stores exactly one `playerId`. `NightmareRegistryData` enforces one instance per owner and separate physical slots.

This is correct for the current First-Nightmare preview and solves the datapack’s global-selector limitation.

It is not the correct general shape for later Seeds.

### Required future split

```text
NightmareInstance
├── instance_id
├── definition_id
├── global_conflict_state
├── participants[]
├── owned_world_state
├── terminal_resolution?
└── lifecycle_transaction

NightmareParticipant
├── player_id
├── role
├── body/context
├── evidence
├── survival
├── eligibility
├── outcome
├── return/recovery record
└── appraisal record
```

### Verdict

**Current model: correct First-Nightmare specialization.**  
**Future shared model: not yet implemented.**

## 30. Seventh Nightmare and upper-rank planning

### Canon

Late revelation identifies a Seventh Nightmare associated with the Forgotten God and the fate of existence. It is not currently established as an eighth Soul Rank.

The known Rank ladder still ends at Divine.

### Current repository

The Nightmare Seed roadmap acknowledges the need for later verification. Future definitions should avoid arrays or progression code that assume exactly six possible Nightmares and no final non-rank cosmic conflict.

### Verdict

**Documentation mostly aligned; protect future schema from six-only assumptions.**

## 31. Spell interface, runes and appraisal voice

### Canon

The Spell’s ordinary register is terse, bracketed and formal. Appraisals become compressed mythic narration and sometimes carry dry, cruel irony.

The runes communicate supernatural meaning; they should not be treated as ordinary English database strings whose exact grammar is metaphysically fixed.

### Current Java

The Soul screen is correctly server-synchronized and read-only.

The Last Signal uses ordinary Minecraft system messages with explicit instructions. That is useful for testing, but not final Spell presentation.

### Recommendation

Separate three presentation layers:

1. **notification** — concise bracketed messages;
2. **runes/status** — structured supernatural readout with unknown/unrevealed states;
3. **appraisal** — short mythic retelling, choices, conflict outcome and restrained irony.

Tutorial guidance should come from onboarding/help UI, not be disguised as the Spell’s cosmic voice.

### Verdict

**Functional preview UI; final tone and knowledge states unimplemented.**

## 32. Dream Realm visual identity

### Canon

The Dream Realm is not one dark biome or universal purple-black fantasy world. Regions have radically different laws, materials, scales, ecologies and histories.

The strongest consistent visual language belongs to the Spell itself:

- black void;
- stars/souls;
- silver strings and weave;
- runes;
- cosmic scale.

### Current products

The bundled dark dimension and deepslate road are explicitly DESIGN. This is acceptable for The Last Signal, but must not become the global Dream Realm palette.

### Recommendation

Use scenario/region-specific worldgen profiles. The modpack may use multiple structure, biome and creature providers, but every region must earn its own coherent rules and presentation.

### Verdict

**Current staging valid; dangerous if generalized.**

# Part III — What should be preserved

## 33. Architectural decisions to keep

1. **One server-authoritative Soul core.**
2. **Separate Soul Rank and Aspect Rank.**
3. **Explicit Awakening path.**
4. **Migration preserves identity instead of rerolling it.**
5. **One First-Nightmare entry choke point.**
6. **One technical teardown boundary.**
7. **Exact owned entity and instance IDs.**
8. **Temporary role and conflict state outside permanent Soul data.**
9. **Read-only client snapshots.**
10. **Fixed preview content honestly labelled DESIGN.**
11. **Technical recovery described as technical rather than Spell mercy.**
12. **Core-owned identity with optional external execution providers.**
13. **The frozen datapack treated as migration evidence, not future architecture.**
14. **Canon, inference, design and unknown kept separate.**

# Part IV — What should change before broad expansion

## 34. P0 changes

### P0.1 Durable completion transaction

Close Issue #34 with persisted, replayable, exactly-once resolution/outcome/appraisal/return/teardown phases.

### P0.2 Lore-shaped success ordering

Do not return and erase the instance before appraisal has a durable record. Present appraisal before waking return where practical.

### P0.3 Truthful current documentation

Reconcile status documents that still describe merged work as an active PR or pending correction branch.

### P0.4 Physical evidence

Run and record:

- active-instance restart;
- relog at Carrier/Aspirant/Dreamer;
- two-player First-Nightmare isolation;
- successful exactly-once appraisal;
- death and recovery;
- real datapack migration with backup and second invocation.

## 35. P1 schema changes

Before natural progression, procedural identity or broader appraisals:

1. optional formal names and explicit reveal states;
2. path-specific Aspect/Flaw invariants;
3. multiple Aspect ability expressions;
4. Innate Ability and Aspect Legacy slots;
5. first-class Attributes;
6. True Name and fate-connection state;
7. CoreState and Essence state;
8. progression and appraisal history;
9. conflict and participant records;
10. internal provider-independent definitions for abilities, Memories, Echoes and creatures.

## 36. P1 Nightmare engine changes

1. `ScenarioEvent`;
2. persisted `ConflictState`;
3. `ResolutionGraph`;
4. named terminal resolutions;
5. independent `ChallengerOutcome`;
6. evidence ledger;
7. multi-step Last Signal;
8. at least two valid DESIGN endings;
9. world/other-actor-driven resolution;
10. tests proving completion is not synonymous with boss death or one block click.

## 37. P2 modpack integration changes

After P0/P1 boundaries are accepted:

- bootstrap a reproducible manifest and server package;
- add only minimal infrastructure dependencies;
- implement one optional Kindle ability provider;
- prove missing-provider recovery;
- treat Curios or similar systems as equipment execution/presentation, not canonical Memory storage;
- treat creature mods as providers, not owners of Nightmare Creature identity;
- keep quests/guides outside authoritative Spell progression.

# Part V — Recommended future schema direction

## 38. Conceptual player domain

```text
PlayerSoul
├── schema_version
├── progression_stage
├── awakening_path
├── spell_connection_state
├── infection_state?
├── core_state?
├── aspect_state?
├── flaw_state?
├── attributes[]
├── true_name_state?
├── memories[]
├── echoes[]
├── dream_anchor?
├── corruption_state?
└── progression_history[]
```

This is a conceptual boundary, not a recommendation to place every collection directly inside one attachment. Large systems may use separate attachments or SavedData indexes.

## 39. Conceptual Nightmare domain

```text
NightmareDefinition
├── historical_context
├── roles[]
├── factions[]
├── world_profile
├── scenario_events[]
├── resolution_graph
└── appraisal_evidence_schema

NightmareInstance
├── lifecycle_phase
├── conflict_state
├── participants[]
├── owned_entities/world_state
├── terminal_resolution?
├── seed_relationship?
└── durable_transaction

NightmareParticipant
├── player
├── assigned_role/body
├── provisional state
├── evidence
├── survival/eligibility
├── outcome
├── return/recovery
└── appraisal
```

# Part VI — Comparison scorecard

## 40. Current alignment matrix

| System | Datapack | Java preview | Modpack plan | Verdict |
| --- | --- | --- | --- | --- |
| Infection | first-sleep DESIGN | command shortcut, honestly labelled | configurable future trigger | Java direction aligned |
| Carrier/Aspirant/Dreamer separation | compressed historically | implemented | shared core | aligned |
| Aspirant core timing | prototype score model | persists Dormant | not decided | unresolved interpretation |
| Soul Rank vs Aspect Rank | historically compressed | separated | core-owned | aligned |
| First-Nightmare role | minimal | instance-owned role ID | core-owned | aligned but shallow |
| Central conflict | objective seam | direct signal trigger | planned richer scenario | concept aligned, runtime shallow |
| Appraisal order | datapack-specific | after return/teardown | not decided | P0 mismatch |
| Appraisal formula | behaviour-derived prototype | fixed DESIGN | future evidence engine | correctly non-canonical |
| Failure | safe ejection prototype | canonical death label + respawn accommodation | configurable future | honest but incomplete |
| Aspect instance | finite score/tag identity | provider-independent record | core-owned | good foundation |
| Unknown Aspect name | unsupported | unsupported | not specified | mismatch |
| Multiple Aspect abilities | unsupported | one ability ID | adapters proposed | mismatch |
| Flaw diversity/hidden rule | four families | one fixed effect model | adapters not yet designed | mismatch |
| Natural Awakening | none | path field only | future | unimplemented |
| Attributes | generic physical readout | absent | future | unimplemented |
| True Names | absent | absent | future | unimplemented |
| Core/essence/saturation | absent | Soul Rank only | future | unimplemented |
| Dream Realm | one Nightmare dimension | trial-only dimension | future content | unimplemented |
| Memories | none | none | Curios candidate | internal model required first |
| Echoes | none | none | creature providers possible | internal model required first |
| Seeds/Gates | none | none | future | roadmap aligned |
| First-Nightmare multiplayer | one global slot | separate owner instances | shared core | Java aligned |
| Later shared Nightmares | impossible | one owner per instance | future | new participant model required |
| Spell UI voice | Minecraft chat | read-only Soul UI + tutorial chat | guide candidate | needs final presentation layer |
| World diversity | one dark dimension | one dark DESIGN scenario | dependency content possible | do not generalize preview palette |

# Part VII — Findings that should update current authorities

## 41. Proposed documentation corrections

The following should be considered for promotion through normal review:

1. **Aspirant core timing should be labelled uncertain/inferred.** The trial status displays Dormant, while post-Nightmare exposition describes core formation.
2. **Appraisal deviation from fate remains an inference, not a complete canon formula.**
3. **Formal Aspect and Flaw names must be optional in future schemas.** Known power/effect does not imply known formal name.
4. **The paired Aspect/Flaw record is path-specific.** It should not become a universal natural-Awakening invariant.
5. **The current one-owner `NightmareInstance` is a First-Nightmare specialization.** Later Seeds need shared instances plus participant outcomes.
6. **Appraisal-before-return is both more canon-shaped and safer when implemented through a durable transaction.**
7. **True Names deserve first-class identity and history.**
8. **Spell presentation should separate tutorial guidance from Spell voice.**
9. **Provider-backed equipment is not the Memory ownership model.**
10. **No future progression table should assume only six Nightmares.**

# Part VIII — Recommended implementation order after this audit

## 42. Order

```text
1. Reconcile current status truth
2. Persist terminal resolution and outcome
3. Make appraisal/return/teardown exactly once and restart-safe
4. Run physical relog/restart/multiplayer/migration evidence
5. Add scenario events and resolution graph
6. Refactor The Last Signal into multiple endings
7. Design schema-3 reveal/name/history boundaries
8. Add Attributes, True Name and CoreState foundations
9. Bootstrap the modpack manifest and optional provider interface
10. Prove one missing-safe external ability adapter
11. Build the persistent first Dream Realm journey and Awakening
12. Add Memories/Echoes/Seeds only on internal provider-independent records
```

Broad content expansion before steps 1–6 would make the project look richer while increasing the cost of repairing the lifecycle and identity model underneath it.

# Part IX — Primary chapter register

The following chapters carried the largest load in this audit. This is a navigation register, not a complete bibliography.

## Infection, First Nightmare and appraisal

- Chapter 1, **Nightmare Begins** — infection symptoms, First-Nightmare failure and Gate consequence.  
  https://novelfull.com/shadow-slave/chapter-1-nightmare-begins.html
- Chapter 2, **Slave Caravan** — Aspirant status, Dormant in-trial core, role Aspect and Attributes.  
  https://novelfull.com/shadow-slave/chapter-2-slave-caravan.html
- Chapter 15, **Shadow Slave** — appraisal, Dreamer title, True Name, Aspect evolution, independent Aspect Rank.  
  https://novelfull.com/shadow-slave/chapter-15.html
- Chapter 16, **Rebirth** — post-First-Nightmare soul/core formation and body transformation.
- Chapter 218, **Shadow Dance** — role selection/fate similarity and Aspect Legacy.  
  https://novelfull.com/shadow-slave/chapter-218-shadow-dance.html
- Chapter 743, **Appraisal** — post-Nightmare appraisal and Sunny’s fate-deviation inference.  
  https://novelfull.com/shadow-slave/chapter-743-appraisal.html
- Chapter 744, **Ascension** — new/evolved Aspect Abilities and appraisal inference.  
  https://novelfull.com/shadow-slave/chapter-744-ascension.html

## Progression and natural Awakening

- Chapter 354, **Awakening** — actual Dreamer-to-Awakened transition after Dream Realm return.  
  https://novelfull.com/shadow-slave/chapter-354-awakening.html
- Chapter 1306, **Paths of Ascension** — natural order of essence control and core formation.  
  https://novelfull.com/shadow-slave/chapter-1306-paths-of-ascension.html
- Chapter 1307, **Before the Nightmare Spell** — natural rank progression and requirement for Aspect/Flaw before Transcendence.  
  https://novelfull.com/shadow-slave/chapter-1307-before-the-nightmare-spell.html
- Chapter 1825, **Edge of the Abyss** — Rain’s natural core formation and Awakening.  
  https://novelfull.com/shadow-slave/chapter-1825-edge-of-the-abyss.html
- Chapter 1827, **Cleansing** — death and natural Awakening as corruption-cleansing mechanisms.  
  https://novelfull.com/shadow-slave/chapter-1827-cleansing.html
- Chapter 2029, **Fortune Telling** — natural Awakened without unsealed Aspect/Flaw; Attributes and Spell translation of insight.  
  https://novelfull.com/shadow-slave/chapter-2029-fortune-telling.html

## Seeds, Gates and later Nightmares

- Chapter 459, **Seed of Nightmare** — pre/post-bloom challenge and cohort danger.  
  https://novelfull.com/shadow-slave/chapter-459-seed-of-nightmare.html
- Chapter 1738, **Crumbling Dam** — Gate persists after initial wave/Guardian while Seed remains unconquered.  
  https://novelfull.com/shadow-slave/chapter-1738-crumbling-dam.html
- Chapter 2412, **Weaver’s Lullaby** — Seventh Nightmare and the Spell’s late-revealed purpose.  
  https://novelfull.com/shadow-slave/chapter-2412-weavers-lullaby.html
- Chapter 2761, **Cautionary Tale** — Fifth Nightmare personal and collective trials; loss-of-self risk.  
  https://novelfull.com/shadow-slave/chapter-2761-cautionary-tale.html

## Identity, fate, Memories and Echoes

- Chapter 1589, **Untethered** — True Name lost with fate while Aspect/Flaw remain.  
  https://novelfull.com/shadow-slave/chapter-1589-untethered.html
- Chapter 2048, **Different Foundations** — crafted Memories and artificial Echoes.  
  https://novelfull.com/shadow-slave/chapter-2048-different-foundations.html
- Chapter 1609, **Reclusive Saint** — ordinary and artificial Echoes used as mounts.  
  https://novelfull.com/shadow-slave/chapter-1609-reclusive-saint.html

## Publication boundary

- Official WebNovel catalogue:  
  https://www.webnovel.com/book/shadow-slave_22196546206090805/catalog
- Owner-designated full-chapter access layer:  
  https://novelfull.com/shadow-slave.html

# 43. Final conclusion

The repository is not suffering from a lack of lore research. It already contains a surprisingly strong evidence base and has corrected the prototype’s largest misconceptions.

The present risk is **prematurely freezing preview data shapes as the complete lore model**.

The safest direction is:

> Keep the current preview playable, repair its durable completion transaction, then expand the domain through reveal states, optional formal names, participant outcomes, Attributes, True Names and CoreState before adding large amounts of dependent content.

That preserves what the project has done well—server authority, migration safety, clear ownership and honest DESIGN labels—while preventing one fixed scenario, one named Aspect, one named Flaw and one ability field from becoming the accidental definition of Shadow Slave.