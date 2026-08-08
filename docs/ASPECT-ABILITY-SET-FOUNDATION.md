# Aspect ability-set foundation

**Status:** persistent Java identity migration integrated with optional/unrevealed Aspect names.

## Lore evidence

- **CANON — Chapter 15:** an Aspect can expose an Innate Ability separately from later Aspect Abilities.
- **CANON — Chapter 354:** Awakening grants an additional Aspect Ability.
- **CANON — Chapter 744:** Ascension grants another Aspect Ability and an existing ability can evolve.
- **CANON — Chapters 1584 and 1592:** a Transcendent Aspect user retains and combines several earlier abilities while also possessing a Transformation Ability.
- **CANON — Chapters 2330 and 2981:** later status descriptions enumerate multiple rank abilities belonging to one Aspect.
- **INFERRED:** persistent identity therefore needs an ordered collection of ability identities rather than one universal `abilityId`.
- **DESIGN:** Java distinguishes `INNATE` from `RANK_GRANTED` and records an acquisition Soul Rank for rank-granted abilities.
- **UNKNOWN:** canon does not establish one universal storage taxonomy for every exceptional ability, evolution, or natural-awakening discovery sequence.
- **COMPATIBILITY:** legacy `AspectInstanceData.ability_id` saves do not record whether the ability was innate or rank-granted, or when it was acquired. They decode as `LEGACY_UNCLASSIFIED` with no acquisition rank rather than inventing either fact. Optional/unrevealed formal-name state is preserved independently.

Aspect Legacies are deliberately excluded. They are a distinct canonical system and must not be flattened into ordinary Aspect abilities.

## Persistent migration

`AspectInstanceData` now stores:

- stable instance ID;
- optional formal name/revelation state;
- Aspect Rank;
- stable nature ID;
- non-empty ordered `AspectAbilitySetData`;
- provenance.

The codec accepts exactly one ability storage shape:

- current `abilities`, containing fully classified ability records; or
- legacy `ability_id`, migrated to one `LEGACY_UNCLASSIFIED` compatibility entry.

Supplying both forms or neither form fails closed. New encoding writes only `abilities`; it does not preserve the obsolete scalar field. `formal_name` remains optional and is omitted for identities whose authoritative name is not yet established.

Existing Java call sites remain source-compatible during this migration through constructors that accept a single legacy ability ID and a temporary `abilityId()` first-entry accessor. Both are deliberately temporary and are removed by later migration slices once all runtime consumers use the complete set.

## Evidence boundary

The migration preserves stable identity without fabricating historical facts absent from old saves. It does not infer:

- an innate/rank-granted category for a legacy scalar ability;
- an acquisition rank;
- a formal name that was previously unrevealed;
- evolution history;
- natural-awakening discovery order.

## Follow-up

1. update snapshots and provider authorization to query the set directly;
2. remove the scalar compatibility accessor and constructor only after all call sites and stored fixtures are migrated;
3. add explicit evolution metadata only after a separately researched schema decision;
4. keep exceptional ability categories and natural-awakening ordering `UNKNOWN` until evidence supports a model.
