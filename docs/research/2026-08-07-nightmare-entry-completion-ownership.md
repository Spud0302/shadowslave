# Nightmare entry versus retained completion ownership

**Scope:** Java persistence correctness only. This note does not change Nightmare lore or receipt-retention policy.

## Finding

A successful-completion receipt deliberately survives active Nightmare teardown so restart recovery can reconcile player data and overworld `SavedData`. Before this slice, `NightmareService.tryEnter(...)` checked for that retained receipt, but `NightmareRegistryData.create(...)` itself rejected only existing active ownership.

That left the lower-level mutation boundary capable of creating a new active Nightmare for a player whose previous completion receipt was still retained. The supported service path did not normally do this, but future callers could bypass the service precondition and construct a state that restart reconstruction correctly rejects as cross-instance ownership.

## Evidence classification

- **CANON:** unchanged evidence from Issue #34 / PR #39 that Nightmare terminal resolution precedes appraisal, progression and return. No new lore claim is needed for this invariant.
- **INFERRED:** the retained completion receipt belongs to the resolved Nightmare instance until technical recovery state is explicitly cleared.
- **DESIGN:** the persistent registry refuses creation while either active ownership or a retained successful-completion receipt exists for the player. The guard lives at the registry mutation boundary rather than relying only on `NightmareService`.
- **UNKNOWN:** real process-restart behaviour at every physical fault boundary remains unproven until the dedicated-server restart matrix is executed.
- **COMPATIBILITY:** valid existing entry is unchanged once no active instance or retained receipt exists. Receipt retention and explicit reset/technical cleanup semantics remain unchanged.

## Deliberate limits

This is not arbitrary corrupt-NBT repair and does not automatically clear successful-completion receipts. A retained receipt remains authoritative recovery evidence until an existing explicit cleanup path clears that exact instance-scoped receipt.
