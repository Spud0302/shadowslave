# Generated appraisal disk-image restart evidence

**Date:** 2026-08-11  
**Current merged prerequisite:** successful-completion restart recovery from PR #213  
**Tracker:** Issue #34

## Why this slice

PR #213 is merged on current `main`, including the durable generated-appraisal completion receipt, exact generated award snapshot, direct persisted active-ownership verification, player-state verification, receipt deletion verification, deterministic restart-cut tests, and fresh-JVM receipt reconstruction.

The remaining Issue #34 gap is stronger restart evidence rather than another speculative persistence checkpoint. The prior fresh-JVM test crosses only the complete receipt through a real process boundary; player and active-ownership cuts are reconstructed in memory.

This slice therefore materializes all three successful-completion persistence surfaces as compressed NBT files and crosses them through two fresh JVM boundaries without changing runtime code or save schema.

## What the test materializes

`GeneratedAppraisalCompletionDiskImageRestartTest` creates:

1. a production-shaped completion-receipt SavedData image at `shadowslave_nightmare_completion_receipts.dat`;
2. a production-shaped Nightmare-registry SavedData image at `shadowslave_nightmares.dat` containing the exact matching active instance;
3. a compressed player-data image containing the five NeoForge attachment values consumed by generated-appraisal recovery: Soul, permanent identity, Attributes, Memories, and Echoes.

A separate compressed receipt oracle is retained only as immutable test expectation data. It is not treated as runtime recovery authority; this is necessary because the mutable completion-receipt surface is expected to be empty after successful replay.

## First fresh JVM: recover the persisted cut

The first child process starts without originating-process objects or static state. It:

1. reads and production-decodes the exact completion receipt;
2. verifies the persisted receipt is present through `PersistedNightmareCompletionReceiptVerifier`;
3. reads and production-decodes the active `NightmareInstance` from the registry image;
4. requires `GeneratedAppraisalRecoveryService.activeInstanceForReplay(...)` to select that exact matching instance for successful teardown;
5. reads the actual persisted player attachment image;
6. runs the production generated-appraisal recovery planner against the receipt snapshot;
7. writes the exact converged Dreamer/identity/Attribute/Memory/Echo state back to the player image;
8. writes an empty active-Nightmare registry image;
9. writes an empty completion-receipt registry image.

## Second fresh JVM: verify the post-replay disk state

A second brand-new child process then proves the final persisted split is self-consistent:

- `PersistedGeneratedAppraisalPlayerVerifier` requires the exact generated award and Dreamer state represented by the original receipt;
- `PersistedNightmareOwnershipVerifier` requires both player and Nightmare-instance ownership absent from the registry image;
- `PersistedNightmareCompletionReceiptVerifier` requires completion authority absent after consumption.

This adds a real process/static-state boundary on both sides of the modeled replay and makes the three persisted surfaces, rather than parent-JVM objects, the state being transformed and verified.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, progression, reward, death, or failure rule changes.
- **INFERRED:** an already-resolved appraisal should retain its exact resolved result through technical restart rather than be regenerated against later generator/catalogue state.
- **DESIGN:** multi-surface compressed-NBT reconstruction, exact generated award replay, and post-replay absence verification are technical Java recovery evidence.
- **UNKNOWN:** a real NeoForge dedicated-server process killed during the transaction; server lifecycle/save scheduling; exact persisted waking dimension/position after a live return teleport; filesystem/fsync guarantees below a readable file image; storage corruption after successful verification.
- **COMPATIBILITY:** test-only. Runtime behavior, persistence schema, catalogue state, gameplay semantics, and lore-facing rules are unchanged.

`docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read. This changes only technical restart evidence. No new lore-sensitive mechanic or primary-novel proposition is introduced, so no new canon rule is inferred from convenience.

## Deliberate limits

This is stronger than the receipt-only fresh-JVM test but remains below a physical NeoForge server restart. The child directly applies the production planner's target to a test player-data image instead of invoking `GeneratedAppraisalRecoveryService.replayPending(ServerPlayer)` inside a running server.

It therefore does not prove:

- a live `ServerPlayer` is returned to the exact stored waking dimension/position after process restart;
- the production server lifecycle invokes login recovery at the intended time;
- actual world SavedData/player save scheduling has the same timing as this deterministic reconstruction;
- OS/storage flush semantics survive sudden power loss;
- later corruption cannot invalidate a previously verified image.

These limits are intentional and should not be described as solved by this test.

Prepared-world durability #158 remains blocked under its recorded resume condition and is not retried here. The recurring hosted vanilla Nightmare-dimension transition stall is likewise not a reason to increase timeouts again without new diagnostic evidence or a changed approach.

## Next evidence

After this exact head is compile/unit/client/server green and review-clean, the next Issue #34 slice should be a dedicated NeoForge restart harness that materializes a supported completion cut in an actual server world, stops or kills the server, restarts it, and verifies the real player's waking return location, active Nightmare absence, Dreamer Soul state, exact Aspect/Flaw/Attribute/Memory/Echo award, and completion-receipt consumption exactly once.
