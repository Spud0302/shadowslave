# Aspect ability-set foundation

**Status:** bounded Java identity foundation; not wired into `AspectInstanceData` yet.

## Lore evidence

- **CANON — Chapter 15:** an Aspect can expose an Innate Ability separately from later Aspect Abilities.
- **CANON — Chapter 354:** Awakening grants an additional Aspect Ability.
- **CANON — Chapter 744:** Ascension grants another Aspect Ability and an existing ability can evolve.
- **CANON — Chapters 1584 and 1592:** a Transcendent Aspect user retains and combines several earlier abilities while also possessing a Transformation Ability.
- **CANON — Chapters 2330 and 2981:** later status descriptions enumerate multiple rank abilities belonging to one Aspect.
- **INFERRED:** persistent identity therefore needs an ordered collection of ability identities rather than one universal `abilityId`.
- **DESIGN:** Java distinguishes `INNATE` from `RANK_GRANTED` and records an acquisition Soul Rank for rank-granted abilities.
- **UNKNOWN:** canon does not establish one universal storage taxonomy for every exceptional ability, evolution, or natural-awakening discovery sequence.
- **COMPATIBILITY:** the current `AspectInstanceData.abilityId` remains untouched until a separate migration slice can preserve old saves and current preview mechanics.

Aspect Legacies are deliberately excluded. They are a distinct canonical system and must not be flattened into ordinary Aspect abilities.

## Runtime model

`AspectAbilityData` stores:

- stable ability ID;
- ability kind;
- optional acquisition rank constrained by kind;
- provenance.

`AspectAbilitySetData` stores an immutable ordered list, rejects duplicate IDs, and provides a codec that fails closed on invalid persisted combinations.

## Integration boundary

This PR does not alter player saves, snapshots, commands, ability execution, or the current fixed preview. A later schema migration should:

1. add the ability set to the persistent Aspect record;
2. decode legacy `ability_id` as one compatibility entry;
3. preserve the old field during a defined compatibility window if required;
4. update snapshots and provider authorization to query by stable ability ID;
5. prove old named and unnamed identities still round-trip.
