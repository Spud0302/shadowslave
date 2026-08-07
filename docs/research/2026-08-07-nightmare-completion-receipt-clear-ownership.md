# Nightmare completion receipt clear ownership

**Status:** bounded persistence/operation-integrity follow-up for Issue #34.  
**Scope:** explicit deletion of retained successful-completion receipts during technical/admin/reset paths.

## Finding

The durable completion API already requires authoritative `NightmareInstance` snapshots when beginning completion, advancing a completion phase, and consuming active ownership. Receipt deletion was the remaining mutation keyed only by player UUID.

That player-only API could let a stale cleanup operation delete a newer retained receipt for the same player if a future caller ever crossed an instance boundary incorrectly. The supported preview flow normally prevents that state, but the persistence API should fail closed at the mutation boundary rather than rely on every caller preserving that assumption forever.

## Change

`NightmareRegistryData.clearSuccessfulCompletion` now accepts the expected `NightmareInstance` snapshot.

- no receipt, or a receipt for a different instance UUID, is a no-op;
- the same instance UUID with a modified snapshot is rejected;
- only the matching authoritative snapshot consumes the receipt;
- technical recovery, admin abort and canonical death pass the active instance they just resolved;
- preview reset first reads the authoritative retained receipt and clears that exact snapshot.

This mirrors the exact-snapshot authorization already used by phase advancement and active teardown.

## Evidence classification

- **CANON:** unchanged. The primary chapter evidence recorded for Issue #34 supports Nightmare terminal resolution before appraisal/progression/return; this slice changes no lore behavior.
- **INFERRED:** unchanged. One retained completion receipt belongs to one resolved Nightmare instance.
- **DESIGN:** deletion of durable technical recovery state requires instance-scoped authority instead of player identity alone.
- **UNKNOWN:** real process-restart behavior at every physical failure boundary remains unproven until the dedicated-server restart matrix is executed.
- **COMPATIBILITY:** valid completion recovery, technical recovery, admin abort, canonical death and explicit preview reset retain their intended behavior. No new canon rule is introduced.

## Regression coverage

`NightmareRegistryCompletionClearTest` verifies:

1. an exact snapshot clears its receipt once without consuming active ownership;
2. a stale older instance cannot clear a newer receipt for the same player;
3. a modified same-ID snapshot cannot clear the authoritative receipt.

## Deliberate limits

This is not arbitrary corrupt-NBT repair and does not change receipt retention policy. Successful receipts are still retained after teardown until explicit preview reset; a future permanent Nightmare-history system should migrate that evidence rather than silently deleting it.
