# Aspect ability-set foundation

**Status:** persistent Java identity migration stacked on PR #42, with provider authorization and client snapshots migrating to the complete ability set.

## Lore evidence

- **CANON — Chapter 15:** an Aspect can expose an Innate Ability separately from later Aspect Abilities.
- **CANON — Chapter 354:** Awakening grants an additional Aspect Ability.
- **CANON — Chapter 744:** Ascension grants another Aspect Ability and an existing ability can evolve.
- **CANON — Chapters 1584 and 1592:** a Transcendent Aspect user retains and combines several earlier abilities while also possessing a Transformation Ability.
- **CANON — Chapters 2330 and 2981:** later status descriptions enumerate multiple rank abilities belonging to one Aspect.
- **INFERRED:** persistent identity and client views therefore need an ordered collection of ability identities rather than one universal `abilityId`.
- **DESIGN:** Java distinguishes `INNATE` from `RANK_GRANTED`, records an acquisition Soul Rank for rank-granted abilities, and transmits the ordered stable IDs in `SoulSnapshot`.
- **UNKNOWN:** canon does not establish one universal storage taxonomy for every exceptional ability, evolution, or natural-awakening discovery sequence.
- **COMPATIBILITY:** legacy `AspectInstanceData.ability_id` saves do not record whether the ability was innate or rank-granted, or when it was acquired. They decode as `LEGACY_UNCLASSIFIED` with no acquisition rank rather than inventing either fact.

Aspect Legacies are deliberately excluded. They are a distinct canonical system and must not be flattened into ordinary Aspect abilities.

## Persistent migration

`AspectInstanceData` now stores a non-empty ordered `AspectAbilitySetData`.

The codec accepts exactly one storage shape:

- current `abilities`, containing fully classified ability records; or
- legacy `ability_id`, migrated to one `LEGACY_UNCLASSIFIED` compatibility entry.

Supplying both forms or neither form fails closed. New encoding writes only `abilities`; it does not preserve the obsolete scalar field. Existing Java call sites remain source-compatible through the old constructor and `abilityId()` accessor, both of which represent only the first compatibility ability and are deliberately temporary.

## Provider authorization

Provider execution must authorize a stable ability ID by membership in `AspectAbilitySetData`; list position is not an authorization rule. The fixed Kindle preview now follows that boundary, so a valid Kindle entry remains executable when it is not the first ability in the ordered set.

This is **COMPATIBILITY** behavior preservation for the existing preview mechanic, not a claim that canon defines Java collection lookup or ability-provider dispatch.

## Client snapshot boundary

`SoulSnapshot` now transports the complete ordered list of stable Aspect ability IDs instead of a single scalar ID. The current Soul screen compatibility method renders the whole list; it does not treat the first element as the only or privileged ability.

The network codec places a **DESIGN** safety ceiling of 64 ability IDs in one snapshot and rejects blank or duplicate IDs. That bound is a protocol-defense choice, not a lore claim about the maximum number of Aspect abilities.

The snapshot deliberately transmits stable IDs only. Ability kind, acquisition rank, evolution state, provider metadata, and descriptions remain server-owned/domain data until a separate client requirement justifies exposing them.

## Evidence boundary

The migration preserves stable ability identity without claiming historical facts absent from old saves. It does not infer an innate/rank-granted category, acquisition rank, evolution history, natural-awakening discovery order, provider execution formula, or canonical maximum ability count.

## Follow-up

1. remove the scalar `AspectInstanceData.abilityId()` compatibility accessor only after all remaining domain call sites and stored fixtures are migrated;
2. add explicit evolution metadata only after a separately researched schema decision;
3. keep exceptional ability categories and natural-awakening ordering `UNKNOWN` until evidence supports a model;
4. broaden the client payload only when a concrete UI or gameplay consumer needs more than stable ability IDs.
