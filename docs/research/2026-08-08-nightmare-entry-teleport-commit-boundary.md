# Nightmare entry teleport commit boundary

**Date:** 2026-08-08  
**Scope:** technical First-Nightmare entry transaction only

## Finding

`NightmareService.tryEnter(...)` previously kept its rollback catch active after `ServerPlayer.teleportTo(...)` returned. The remaining operations were player-facing system messages. If any post-teleport operation threw, the catch path could clear scenario state, active ownership, and the Aspirant Soul transition even though the player had already been moved into the Nightmare dimension.

That would create the inverse of the earlier failed-entry leaks: the player could remain physically inside the Nightmare while the authoritative Java state claimed that entry had been rolled back.

## Boundary

The successful return from the teleport call is now treated as the entry commit boundary for rollback policy.

- Before teleport has returned, an exception still triggers the existing world, ownership, and Soul rollback.
- After teleport has returned, later presentation failure does not consume the active instance or restore the pre-entry Soul snapshot.
- The caller still receives an exception so the post-entry failure is visible rather than silently ignored.

This preserves the authoritative state needed by logout/login recovery and administrative recovery if presentation fails after physical entry.

## Evidence classification

- **CANON:** unchanged. First-Nightmare entry/progression evidence and the terminal-resolution/appraisal evidence already tracked by the project remain controlling; this slice changes no lore mechanic.
- **INFERRED:** unchanged one-instance ownership of technical scenario state during an active Nightmare.
- **DESIGN:** a returned teleport is the Java entry transaction's rollback commit boundary; post-entry presentation must not erase authoritative ownership.
- **UNKNOWN:** NeoForge/Minecraft behavior if `ServerPlayer.teleportTo(...)` itself throws after partially moving a player is not established by this unit slice. Physical fault injection at that exact API boundary remains required before claiming that case solved.
- **COMPATIBILITY:** normal successful entry, pre-teleport rollback, scenario preparation rollback, and later login/technical recovery behavior remain unchanged.

## Tests

`NightmareEntryCommitBoundaryTest` locks the rollback policy at both sides of the boundary: pre-teleport failure rolls back; post-teleport failure preserves the committed entry.

## Deliberate limits

This does not claim that the teleport call is internally atomic, does not add process-crash proof, and does not replace the dedicated-server restart/fault-injection matrix required by Issue #34.
