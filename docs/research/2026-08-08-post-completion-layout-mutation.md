# Post-completion Nightmare snapshot mutation audit

**Date:** 2026-08-08

## Finding

`NightmareRegistryData.beginSuccessfulCompletion(...)` snapshots the authoritative active `NightmareInstance` into a durable completion receipt. Before this change, `NightmareRegistryData.update(...)` still allowed the active instance to change after that receipt existed.

The first version of this guard froze only persisted layout (`origin` and `altar`) while deliberately allowing `pursuerId` to remain mutable. Codex review identified that this was inconsistent with the exact-snapshot teardown contract: successful recovery later tears down using the retained receipt snapshot, so a post-receipt pursuer mutation would make that receipt stale and cause teardown to reject it after the player had already been returned.

The corrected rule is therefore narrower and stronger: once a successful-completion receipt exists, the active `NightmareInstance` snapshot is frozen exactly until teardown. Mutable pursuer state remains supported before terminal success is recorded.

## Evidence boundary

- **CANON:** unchanged from Issue #34 / PR #39: Nightmare terminal resolution precedes appraisal/progression/return.
- **INFERRED:** the retained receipt continues to describe the same resolved Nightmare instance during recovery.
- **DESIGN:** after terminal success is durably recorded, the active registry snapshot must remain exactly equal to the receipt snapshot until teardown so exact-snapshot completion operations remain coherent.
- **UNKNOWN:** real process-restart behavior at each physical fault boundary remains unproven until the dedicated-server restart matrix is executed.
- **COMPATIBILITY:** pre-terminal pursuer/layout updates remain available; successful recovery, phase advancement and teardown continue to operate on one authoritative snapshot.

No new lore rule is introduced. This is a technical persistence invariant.

## Regression coverage

`NightmareRegistryLayoutRecoveryTest` now verifies that:

1. runtime layout mutation after a completion receipt is rejected;
2. runtime pursuer mutation after a completion receipt is also rejected;
3. the authoritative active instance and receipt remain intact after either rejected mutation.

## Deliberate limits

This does not make every `NightmareInstance` field immutable before terminal success, does not attempt arbitrary corrupt-NBT repair, and does not replace physical restart testing.
