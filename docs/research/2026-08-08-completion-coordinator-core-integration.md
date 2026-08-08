# Successful-completion coordinator core integration

## Scope

This slice ports only the pure, restart-replayable successful-Nightmare completion coordinator into the active current-main consolidation lineage. Runtime registry persistence, appraisal adapters, login recovery wiring, and teardown integration remain separate follow-up work.

## Evidence boundary

**CANON:** unchanged from the existing Issue #34 / PR #39 research: terminal Nightmare resolution precedes appraisal and return/progression handling.

**INFERRED:** one successful completion transaction remains associated with one resolved Nightmare instance while recovery is incomplete.

**DESIGN:** Java models completion as ordered durable milestones and replays idempotent appraisal, return, and teardown actions from observed durable state. Named post-save fault points are test infrastructure.

**UNKNOWN:** no real dedicated-server process-kill/restart row is proven by these pure coordinator tests. Runtime persistence adapters are not yet wired on this consolidation branch.

**COMPATIBILITY:** no current runtime path is changed by this slice. Newer `AspectAbilitySetData` and content APIs are untouched.

## Tests

`NightmareCompletionCoordinatorTest` simulates one crash after each named durable boundary and reconstructs only durable state before replay. Every row must converge to `TEARDOWN_COMMITTED` with exactly one committed appraisal, return, and teardown. A fully committed receipt must be a no-op.

## Next integration boundary

The next bounded slice should add the durable completion receipt to `NightmareRegistryData` with fail-closed ownership/snapshot invariants, then adapt the current preview appraisal implementation to an idempotent `isApplied`/reconcile contract using the current explicit `AspectAbilitySetData` identity model. Runtime event/login wiring should follow only after those storage and appraisal contracts are reviewable together.
