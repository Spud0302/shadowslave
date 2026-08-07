# Nightmare entry teleport commit boundary

**Date:** 2026-08-08  
**Scope:** technical First-Nightmare entry transaction only

## Finding

`NightmareService.tryEnter(...)` previously kept its rollback catch active after `ServerPlayer.teleportTo(...)` returned. The remaining operations were player-facing system messages. If any post-teleport operation threw, the catch path could clear scenario state, active ownership, and the Aspirant Soul transition even though the player had already been moved into the Nightmare dimension.

That would create the inverse of the earlier failed-entry leaks: the player could remain physically inside the Nightmare while the authoritative Java state claimed that entry had been rolled back.

Codex review of the first implementation identified an important NeoForge-specific correction: normal return from `ServerPlayer.teleportTo(...)` is not sufficient evidence that a cross-dimension move occurred. `EntityTravelToDimensionEvent` is cancellable, and cancellation prevents the dimension change. The review checked the bundled NeoForge 21.1.244 path and found that the cross-dimension call can return normally after the travel event cancels the move. NeoForge's event documentation independently confirms the general cancellation contract for cancellable events.

## Boundary

The entry rollback commit boundary is therefore not merely "the teleport call returned". The service commits only after the call returns **and the server-side player is actually in the Nightmare dimension**.

- Before the player is observed in the Nightmare dimension, an exception or a normal-return/cancelled travel still triggers the existing world, ownership, and Soul rollback.
- After the player is observed in the Nightmare dimension, later presentation failure does not consume the active instance or restore the pre-entry Soul snapshot.
- A returned teleport that leaves the player outside the Nightmare is converted into an explicit entry failure immediately; it does not wait for a later message failure to expose the contradiction.
- The caller still receives an exception so either pre-entry or post-entry failure is visible rather than silently ignored.

This preserves the authoritative state needed by logout/login recovery and administrative recovery if presentation fails after physical entry, while avoiding false commits when another mod cancels dimension travel.

## Evidence classification

- **CANON:** unchanged. First-Nightmare entry/progression evidence and the terminal-resolution/appraisal evidence already tracked by the project remain controlling; this slice changes no lore mechanic.
- **INFERRED:** unchanged one-instance ownership of technical scenario state during an active Nightmare.
- **DESIGN:** the Java entry transaction commits only after the server-side player is observed in `NIGHTMARE_LEVEL`; post-entry presentation must not erase authoritative ownership.
- **UNKNOWN:** physical NeoForge fault injection of `EntityTravelToDimensionEvent` cancellation has not been executed here. This slice also does not claim behavior for arbitrary third-party code that moves the player elsewhere after a successful dimension transition.
- **COMPATIBILITY:** normal successful entry, pre-teleport rollback, scenario preparation rollback, and later login/technical recovery behavior remain unchanged. A cancelled cross-dimension travel now follows the pre-entry rollback path instead of being misclassified as committed.

## Tests

`NightmareEntryCommitBoundaryTest` locks the policy at the relevant boundaries:

- pre-teleport failure remains rollback-eligible;
- normal return while the player is still in the Overworld does not count as a committed entry;
- an observed Nightmare-dimension player does count as committed and preserves authoritative state from later presentation failure.

## Deliberate limits

This is a unit-level transaction-policy correction. It does not add a live event-cancellation integration harness, does not prove process-crash atomicity, does not claim arbitrary teleport redirection handling, and does not replace the dedicated-server restart/fault-injection matrix required by Issue #34.
