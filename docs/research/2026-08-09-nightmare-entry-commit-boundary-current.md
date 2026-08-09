# Nightmare entry commit boundary — current lineage

**Date:** 2026-08-09  
**Base:** PR #139 exact head `caec1526f3803faabc3e36ab93373494c7304007`

## Why this slice moved ahead of historical #69

PR #139 passed Preview Gates run #99, unblocking the next failed-entry correction. Historical PR #69 looked like the next direct port because it clears prepared Last Signal geometry after later entry failure. However, #69's own Codex review found a P2 interaction: the catch path can still execute after `ServerPlayer.teleportTo(...)` has already moved the player into the Nightmare, so destructive slot clearing can remove the platform beneath an already-entered player and then erase ownership.

Historical PR #70 corrected the normal-return prerequisite boundary. The first current-lineage #140 head then passed Preview Gates run #100, but review exposed a stronger exceptional-path case: `teleportTo(...)` can throw after the player's server level has already changed. If the catch trusted only the boolean assignment after the call, destructive rollback could still run while the player was physically inside the Nightmare.

The corrected #140 head therefore handles both normal-return and exceptional-return paths before historical #69 is allowed to add destructive slot cleanup.

## Current-lineage correction

`NightmareService.tryEnter(...)` now distinguishes pre-entry failure from committed entry by authoritative server-side state.

1. The service prepares the scenario, updates authoritative ownership and transitions the player to Aspirant as before.
2. It calls `ServerPlayer.teleportTo(...)`.
3. On normal return, the service observes `player.serverLevel().dimension()` and commits only if the dimension is exactly `NIGHTMARE_LEVEL`.
4. A returned-but-cancelled/redirection result that leaves the player outside the Nightmare becomes an explicit pre-entry failure and uses the existing entity, authoritative-ownership and Soul rollback.
5. If any runtime exception reaches the catch before the normal post-call assignment can run, the catch re-observes the player's authoritative dimension. A player already in `NIGHTMARE_LEVEL` is treated as committed and retains ownership/Aspirant state.
6. Once entry has been observed as committed, a later failure cannot erase that fact merely because the player's dimension is different by the time another exception is handled.
7. Post-entry presentation/message failure is surfaced but cannot invalidate active ownership.

This mirrors the repository's exit-side principle that teleport success is an observed authoritative-state boundary rather than merely a method-return boundary, while adding the exceptional-path observation the first #140 head lacked.

## Evidence classification

- **CANON:** unchanged. No Nightmare role, ending, appraisal, progression, death, Seed, Aspect or Flaw rule changes.
- **INFERRED:** unchanged one-instance ownership of technical scenario state while an active First Nightmare exists.
- **DESIGN:** Java entry commits when authoritative server state demonstrates that the player has entered the Nightmare, including when a teleport call throws after the level switch; a previously observed commit is monotonic for that entry attempt.
- **UNKNOWN:** live NeoForge fault injection that throws from a changed-dimension callback after the level switch; exact positional verification; process-crash atomicity during entry; arbitrary third-party teleport behavior beyond the observed-dimension boundary.
- **COMPATIBILITY:** normal successful entry, cancelled/non-moving entry rollback, preparation rollback, authoritative failed-entry ownership rollback, completion/death/technical-exit transactions and save formats are unchanged.

No canon rule is invented.

## Tests

`NightmareEntryCommitBoundaryTest` now locks both ordinary and exceptional paths:

- pre-teleport failure remains rollback-eligible;
- normal teleport return while still in the Overworld does not commit entry;
- an observed Nightmare-dimension player commits entry;
- an exception observed before any dimension switch remains rollback-eligible;
- an exception after the player has already switched into the Nightmare retains ownership even when the normal assignment after `teleportTo(...)` was never reached;
- a commit already observed earlier is not erased by a later failure observation.

## Remaining entry work

Only after this corrected head is green should historical PR #69 be ported as a separate bounded slice. Destructive prepared-slot cleanup will then be confined to a pre-entry rollback side that accounts for both normal and exceptional teleport completion.

Physical cancellation/throw-after-switch injection and process-crash evidence remain unproven and should be recorded honestly rather than inferred from unit tests.
