# Nightmare recovery identity invariant

## Scope

This note records the bounded persistence rule added after the restart-recoverable completion work in Issue #34. It is an implementation/recovery rule, not a new Nightmare Spell mechanic.

## Finding

A retained successful-completion receipt snapshots the active `NightmareInstance` at terminal resolution. The runtime model has explicit mutation helpers for scenario layout and pursuer ownership, but it does not define any supported transition that rewrites the instance's scenario ID, historical role, original return destination/orientation, or creation game time.

Before this slice, persisted reconstruction could accept an active instance and retained completion snapshot with the same player, instance UUID, and slot while those immutable recovery fields disagreed. Depending on which record a recovery path consulted, the server could then disagree about which scenario/role completed or where the player should be returned.

## Classification

- **CANON:** unchanged evidence from the completion transaction: First/later Nightmares end before appraisal/progression/return; no new lore rule is introduced here.
- **INFERRED:** the durable completion receipt describes the same resolved Nightmare instance whose active ownership is retained until teardown.
- **DESIGN:** `scenarioId`, `historicalRoleId`, return dimension/position/orientation, and `createdGameTime` are immutable recovery identity for one Java `NightmareInstance`; reconstruction fails closed if active and retained snapshots disagree.
- **UNKNOWN:** real process-restart behaviour at every physical fault boundary remains unproven until the dedicated-server restart matrix is executed.
- **COMPATIBILITY:** valid Issue #34 completion states remain valid; `origin`, `altar`, and `pursuerId` are not made globally immutable by this rule. Layout retains its existing dedicated consistency rule, while pursuer state remains operational entity state.

## Deliberate limits

This does not validate arbitrary corrupt NBT, does not infer scenario-specific geometry from a generic registry, and does not make whole-record equality a recovery requirement. It rejects only fields that the current runtime model treats as persistent instance identity.
