# Procedural identity foundation

**Status:** first implementation slice for Issue #37.  
**Classification:** Minecraft **DESIGN** constrained by accepted lore architecture.  
**Runtime status:** not connected to the playable preview appraisal.

## Purpose

This slice proves that player Aspects and Flaws can be generated from composable primitives rather than selected from a finite list of finished identity records.

The initial generator combines:

```text
recorded scenario evidence
+ deterministic seed
+ Aspect nature
+ Aspect archetype
+ compatible ability
+ compatible Flaw
→ resolved persistent candidate
```

The catalogue is deliberately small and shaped around The Last Signal. It is an architectural proof, not a production content claim and not a canonical appraisal formula.

## Implemented boundaries

- Evidence tags are normalized and sorted before generation.
- Identical canonical inputs produce identical output.
- Evidence insertion order does not affect output.
- Different seeds explore different valid combinations.
- Evidence changes weighted selection without becoming the only source of identity.
- Abilities are filtered through their compatible Aspect nature.
- Flaws can exclude incompatible natures.
- Generated records retain the seed, generator version, selected primitive IDs, a SHA-256 generation fingerprint and provenance.
- Existing results are designed to be saved as resolved records rather than regenerated whenever the catalogue changes.

## Current primitives

The bounded prototype contains:

- natures such as Ember, Ash, Road and Signal;
- archetypes such as Keeper, Witness, Wanderer and Last;
- two compatible ability expressions for each nature;
- several mechanically different Flaw families, including environmental, compulsion, resource, social and physical constraints.

These primitives create a combinatorial space. Future data-driven catalogues can add new primitives without requiring one Java class per completed Aspect.

## Not implemented in this slice

- live appraisal integration;
- persistence codecs for generated candidates;
- player-facing candidate preview or selection;
- generated effect execution;
- optional/unrevealed formal names;
- multi-rank ability growth;
- Aspect Legacies or Domains;
- procedural Nightmare scenarios, Memories or creatures.

## Integration gate

Issue #34 must make successful Nightmare appraisal restart-recoverable before generated identity results replace the fixed `PreviewAppraisalService` result.

The safe future order is:

```text
record terminal resolution
→ record challenger evidence
→ generate and persist resolved candidate
→ commit appraisal identity/progression
→ return player
→ teardown Nightmare
```

The saved candidate, not a rerun of the generator, becomes the authority after the generation step.

## Testing

`DeterministicIdentityGeneratorTest` covers:

- exact repeatability;
- evidence-map order independence;
- variation across seeds;
- strong evidence bias;
- incompatibility filtering;
- persistence-ready version, seed, IDs and provenance.

This is unit-level proof only. No claim is made yet about physical-client, dedicated-server or restart behaviour.
