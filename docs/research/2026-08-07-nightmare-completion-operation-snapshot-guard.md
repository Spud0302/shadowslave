# Nightmare completion operation snapshot guard

## Scope

This note records a bounded runtime/persistence guard layered on the restart-recoverable completion transaction from Issue #34. It is technical recovery DESIGN, not a Nightmare Spell mechanic.

## Finding

The registry already required an exact authoritative active snapshot when the successful-completion receipt was first created. Later completion-phase advancement and exact teardown, however, authenticated only the player and instance UUID. A caller holding a stale or modified `NightmareInstance` with the same identifiers could therefore advance durable completion state or consume active ownership without matching the snapshot that actually owns those operations.

The supported successful-completion coordinator carries the retained receipt's snapshot through appraisal, return and teardown. Technical/admin exits obtain the current active snapshot directly from the registry. There is therefore no supported path that requires a modified same-UUID snapshot to authorize either operation.

## Classification

- **CANON:** unchanged Issue #34 evidence: Nightmare resolution precedes appraisal/progression/return. This guard adds no lore rule.
- **INFERRED:** the durable completion transaction belongs to one resolved Nightmare instance.
- **DESIGN:** phase advancement must match the retained receipt snapshot exactly, and teardown must match the currently registered active snapshot exactly; same UUID alone is not sufficient authority.
- **UNKNOWN:** physical process-restart behavior at every injected fault boundary remains unproven until the dedicated-server restart matrix is executed.
- **COMPATIBILITY:** valid completion recovery, technical recovery, admin abort, canonical death and preview-reset teardown all already operate with the authoritative snapshot and retain their behavior.

## Deliberate limits

This does not make all `NightmareInstance` fields globally immutable. Layout and pursuer ownership remain mutable before terminal resolution through the supported update path. The guard applies only at operation authorization boundaries where the caller claims to act on the authoritative current/retained snapshot.
