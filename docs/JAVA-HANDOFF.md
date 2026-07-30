# Java handoff contract — datapack `1.0.0` baseline

**Purpose:** preserve completed datapack players while replacing datapack machinery with the lore-aligned Java model in `docs/JAVA-LORE-ALIGNMENT.md`.

The port is not a line-by-line translation of mcfunctions. Preserve identity and tested contracts; replace prototype rules that exist because of datapack limits.

## 1. Frozen datapack behaviour worth preserving

```text
Uninfected/Mundane description
  -> Carrier
  -> First Nightmare prototype
  -> Sleeper/Dreamer with Dormant Soul Rank
  -> persistent generated Aspect identity
  -> persistent generated Flaw identity with a real drawback
```

The datapack proved:

- progression and identity can persist;
- a First Nightmare has a central objective seam;
- Aspect and Flaw presentation can be personal rather than one display label;
- drawbacks must remain mechanically real;
- reset/teardown and deterministic verification matter;
- a packaged release can run on a vanilla 1.21.1 server.

It did not prove a canonical infection algorithm, Aspect/Flaw generator, safe failure rule or universal boss structure.

## 2. Java progression mapping

### Uninfected datapack player

No `ss_carrier` tag and no completed rank maps to:

```text
spellState = UNINFECTED
awakeningPath = UNDECIDED
soulRank = absent
aspect = absent
flaw = absent
```

`Mundane` is descriptive language, not Soul Rank zero.

### Imported Carrier

`ss_carrier` with no completed rank maps to:

```text
spellState = CARRIER
awakeningPath = NIGHTMARE_SPELL
soulRank = absent
aspect = absent
flaw = absent
```

The Java First Nightmare service later creates the explicit Aspirant stage and Dormant Soul Core. Do not invent an active historical role during import.

### Imported completed player

`ss_rank >= 1` maps to:

```text
spellState = DREAMER
awakeningPath = NIGHTMARE_SPELL
soulRank = DORMANT
```

The social title is Sleeper; Dreamer is the Spell-facing status. First-Nightmare completion is not actual Awakening.

## 3. Aspect import

Current generated `ss_aspect` values are two digits:

- tens digit / legacy nature and mechanical root:
  - `1x` = Veiled / shadow prototype;
  - `2x` = Ashen / flame prototype;
  - `3x` = Pale / bone prototype;
  - `4x` = Restless / wind prototype;
- ones digit / legacy archetype:
  - `x1` = Witness;
  - `x2` = Bearer;
  - `x3` = Warden;
  - `x4` = Wanderer.

Import into an `AspectInstance`, not an enum:

```text
AspectInstance
  instanceId
  formalName = generated datapack name
  aspectRank = DORMANT       # explicit legacy prototype rank
  legacyNature
  legacyArchetype
  legacyMechanicalRoot
  abilities[]
  evolutionHistory[]
  importedFromDatapack = true
```

Legacy scores `1..4` may still exist. Preserve them as explicit legacy prototype identities using the matching mechanics tag; do not silently reroll them.

Aspect Rank is separate from the player's Dormant Soul Rank even though this importer assigns Dormant to the frozen prototype identities.

## 4. Flaw import

Current generated `ss_flaw` values are two digits:

- `1x` = daylight burden / compatibility tag `ss_flaw_shadow_slave`;
- `2x` = reduced-health burden / `ss_flaw_fragile`;
- `3x` = hunger burden / `ss_flaw_ravenous`;
- `4x` = retreat/burdened-movement family / historical tag `ss_flaw_weightless`;
- ones digit selects the generated player-facing name.

The `ss_flaw_weightless` tag is only an import identifier. Java imports the semantic burdened-movement identity, not the retired `safe_fall_distance` implementation.

Import into a `FlawInstance`:

```text
FlawInstance
  instanceId
  formalName = generated datapack name
  effectDefinition
  legacyFamily
  parameters
  revelationEvidence = unavailable/legacy
  importedFromDatapack = true
```

Never expose `shadow_slave` as the Flaw name; Shadow Slave is canonically an Aspect. The datapack does not retain complete causal evidence, so mark it unavailable rather than inventing history.

The four imported families remain compatibility identities, not the complete Java Flaw taxonomy or a canon generation formula.

## 5. State that must not become permanent Java architecture

Do not import these runtime/scratch values as Soul identity:

- `ss_timer`;
- `ss_gone`;
- `ss_health`;
- `ss_roll`;
- `ss_scratch_a`, `ss_scratch_b`;
- `ss_ret_x`, `ss_ret_y`, `ss_ret_z`;
- `ss_cooldown`;
- `ss_in_nightmare`;
- `ss_creature_spawned`;
- `ss_test_bypass`;
- `ss_trial_bloodied`, `ss_trial_hungry`, `ss_trial_fled`.

Java may have analogous typed runtime values, but they belong to player sessions and `NightmareInstance` records.

## 6. Datapack rules Java must replace

Do not preserve these as universal Java lore:

- first ordinary sleep causes infection;
- a Carrier skips directly to Dreamer without an Aspirant stage;
- safe low-health ejection and retry are normal Spell behaviour;
- every First Nightmare is a timer plus boss;
- the most visible trial statistic deterministically creates the Flaw;
- four Aspect roots and four Flaw families exhaust all possibilities;
- Aspect Rank equals Soul Rank;
- generic attributes such as Strength/Vitality are canon Spell Attributes.

Imported players keep the result they already earned. Fresh Java players use the lore-aligned systems.

## 7. Import safety

Recommended one-time order:

1. detect legacy identity/progression state;
2. refuse or defer while the player has active `ss_in_nightmare` state;
3. construct Java `SoulData`, `AspectInstance` and `FlawInstance` in memory;
4. validate every translated field;
5. persist Java data with schema and migration versions;
6. read it back and verify identity;
7. mark the player imported;
8. only then remove or ignore legacy datapack values.

Never delete datapack tags/objectives first and hope construction succeeds afterward.

If active Nightmare state is detected, perform explicit recovery or defer. The datapack used global prototype state and cannot be reconstructed safely as a Java instance after the fact.

## 8. Resources to keep data-driven

Continue using Minecraft data formats for:

- dimensions and dimension types;
- biomes and world generation;
- tags and predicates;
- advancements;
- recipes, loot and structures;
- future scenario, historical-role and conflict definitions where codecs are appropriate.

Java owns persistence, services, appraisal, networking, GUI, custom entities/AI and real Nightmare ownership.

## 9. Definition of migration success

Migration succeeds when:

- an uninfected legacy player has no invented Soul Rank;
- a legacy Carrier remains a Carrier with no permanent Aspect/Flaw;
- a completed legacy player becomes a Dreamer/Sleeper with Dormant Soul Rank;
- generated Aspect name/root and explicit legacy Aspect Rank are retained;
- generated Flaw name/family and effective drawback are retained;
- no identity is rerolled;
- migration is idempotent;
- optional mod integrations can disappear without erasing canonical identity.

After compatibility passes, fresh Java progression may expand beyond datapack behaviour under the lore-alignment gate.
