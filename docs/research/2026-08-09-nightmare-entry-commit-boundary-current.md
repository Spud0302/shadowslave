# Nightmare entry commit boundary — current lineage

**Date:** 2026-08-09  
**Base:** PR #139 exact head `caec1526f3803faabc3e36ab93373494c7304007`

## Why this slice moved ahead of historical #69

PR #139 passed Preview Gates run #99, unblocking the next failed-entry correction. Historical PR #69 looked like the next direct port because it clears prepared Last Signal geometry after later entry failure. However, #69's own Codex review found a P2 interaction: the catch path can still execute after `ServerPlayer.teleportTo(...)` has already moved the player into the Nightmare, so destructive slot clearing can remove the platform beneath an already-entered player and then erase ownership.

Historical PR #70 corrected the prerequisite boundary. This current-lineage slice therefore ports that prerequisite first instead of knowingly reproducing #69's reviewed defect.

## Current-lineage correction

`NightmareService.tryEnter(...)` now distinguishes pre-entry failure from post-entry presentation failure.

1. The service prepares the scenario, updates authoritative ownership and transitions the player to Aspirant as before.
2. It calls `ServerPlayer.teleportTo(...)`.
3. Normal return from that call is not treated as proof that cross-dimension travel committed.
4. The service observes `player.serverLevel().dimension()` and commits entry only if the authoritative dimension is exactly `NIGHTMARE_LEVEL`.
5. A returned-but-cancelled/redirection result that leaves the player outside the Nightmare becomes an explicit pre-entry failure and uses the existing world/entity, authoritative ownership and Soul rollback.
6. Once the player is observed inside the Nightmare, later presentation/message failure is surfaced but does not erase active ownership or restore the pre-entry Soul state.

This mirrors the repository's already-adopted exit-side rule: teleport success is an observed authoritative state boundary, not merely a method-return boundary.

## Evidence classification

- **CANON:** unchanged. No Nightmare role, ending, appraisal, progression, death, Seed, Aspect or Flaw rule changes.
- **INFERRED:** unchanged one-instance ownership of technical scenario state while an active First Nightmare exists.
- **DESIGN:** Java entry commits only after the server-side player is observed in the Nightmare dimension; post-entry presentation failure cannot invalidate authoritative ownership.
- **UNKNOWN:** live NeoForge `EntityTravelToDimensionEvent` cancellation injection on this exact head; exact positional verification; process-crash atomicity during entry; arbitrary third-party teleport behavior beyond the observed-dimension check.
- **COMPATIBILITY:** normal successful entry, preparation rollback, authoritative failed-entry ownership rollback, completion/death/technical-exit transactions and save formats are unchanged. A cancelled/redirection entry now rolls back instead of being treated as committed.

No canon rule is invented.

## Tests

`NightmareEntryCommitBoundaryTest` locks three boundaries:

- pre-teleport failure remains rollback-eligible;
- normal teleport return while the player is still in the Overworld does not commit entry;
- an observed Nightmare-dimension player commits entry and protects authoritative ownership from later presentation failure.

## Remaining entry work

After this head is green, historical PR #69 can be ported safely as a separate bounded slice: destructive prepared-slot cleanup should run only on the pre-entry rollback side now guarded by the observed-dimension commit boundary. That later slice must still derive the physical rollback namespace from the immutable allocated slot rather than possibly-uncommitted layout fields.

Physical teleport-cancellation and process-crash evidence remain unproven and should be recorded honestly rather than inferred from unit tests.
