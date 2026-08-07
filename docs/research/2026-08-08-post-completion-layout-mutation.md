# Post-completion Nightmare layout mutation audit

**Date:** 2026-08-08

## Finding

`NightmareRegistryData.beginSuccessfulCompletion(...)` snapshots the authoritative active `NightmareInstance` into a durable completion receipt. Before this change, `NightmareRegistryData.update(...)` still allowed the active instance to change `origin` or `altar` after that receipt existed. A normal runtime call could therefore create the same active/receipt layout contradiction that restart reconstruction already rejects.

## Evidence boundary

- **CANON:** unchanged from Issue #34 / PR #39: Nightmare terminal resolution precedes appraisal/progression/return.
- **INFERRED:** the retained receipt continues to describe the same resolved Nightmare instance during recovery.
- **DESIGN:** once terminal success is durably recorded, persisted scenario layout (`origin` and `altar`) is frozen against the retained receipt while both records exist.
- **UNKNOWN:** real process-restart behavior at each physical fault boundary remains unproven until the dedicated-server restart matrix is executed.
- **COMPATIBILITY:** mutable operational entity state (`pursuerId`) remains mutable; this change does not promote entity UUIDs into permanent recovery identity.

No new lore rule is introduced. This is a technical persistence invariant.

## Regression coverage

`NightmareRegistryLayoutRecoveryTest` now verifies that:

1. runtime layout mutation after a completion receipt is rejected;
2. the authoritative active instance and receipt remain intact after rejection;
3. changing only mutable pursuer state remains allowed after receipt creation.

## Deliberate limits

This does not make every `NightmareInstance` field immutable, does not attempt arbitrary corrupt-NBT repair, and does not replace physical restart testing.
