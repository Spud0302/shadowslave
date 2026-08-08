# Successful-completion receipt storage integration — 2026-08-08

## Scope

This slice ports the durable successful-Nightmare completion receipt into the current-main correctness consolidation lineage. It is intentionally limited to storage identity, phase monotonicity, reconstruction invariants, entry blocking while a receipt is retained, exact-snapshot mutation authority, and round-trip persistence.

It does **not** wire terminal gameplay events, appraisal identity, player saving, return teleport, teardown persistence, login recovery, or the physical fault runner.

## Why this is the next correctness slice

`NightmareCompletionCoordinator` now models restart-replayable successful completion, but the consolidation branch still had an active-instance-only `NightmareRegistryData`. Without a persisted receipt the runtime adapter would have no durable authority to reconstruct a terminal success after active ownership is later consumed.

The retained receipt stores the exact resolved `NightmareInstance`, the ordered completion phase, and the resolved game time. Reconstruction fails closed when active and retained state disagree about owner, instance UUID, allocated slot, persistent recovery identity, or persisted scenario layout.

## Evidence classification

- **CANON:** unchanged from Issue #34 / PR #39 research. Primary chapter evidence previously checked in Chapters 15 and 742–744 establishes terminal Nightmare resolution followed by appraisal/progression/return handling; Chapter 1581 later treats absence of the normal appraisal space as exceptional rather than redefining the ordinary sequence.
- **INFERRED:** one incomplete successful-completion recovery transaction remains associated with the resolved Nightmare instance until technical reconciliation is complete.
- **DESIGN:** retained SavedData receipt, phase enum, exact-snapshot authorization, slot/identity consistency checks, and blocking a new technical Nightmare while an old completion receipt is retained.
- **UNKNOWN:** physical process-kill/restart behavior at each real storage boundary remains unproven until the dedicated-server matrix executes. This slice also does not decide appraisal generation rules or exceptional Spell-disconnected behavior.
- **COMPATIBILITY:** current runtime behavior is unchanged because no runtime event or login path consumes these new APIs yet. Existing saves lacking `successful_completions` load with an empty receipt set. Current explicit multi-ability Aspect identity and newer Attribute/Memory/Echo/content APIs are untouched.

No canon rule is added or generalized by this storage change. The repository lore policy and Java lore-alignment gate remain controlling.

## Storage invariants

1. A completion receipt can begin only from the exact active instance snapshot.
2. Phase advancement is monotonic and may advance only one durable milestone at a time; replaying an already reached phase is idempotent.
3. The active snapshot is frozen once its receipt exists, keeping teardown/recovery authority exact.
4. Active and retained records for one player must name the same instance when both exist.
5. One instance UUID and one physical slot cannot belong to different owners/instances across active and retained state.
6. Persistent recovery identity and persisted scenario layout must agree across active and retained snapshots.
7. A retained receipt survives active teardown and blocks creation eligibility until explicitly cleared by the exact recorded snapshot.

## Tests added

`NightmareCompletionReceiptStorageTest` covers:

- receipt round-trip after active ownership has been consumed;
- terminal -> appraisal -> return -> teardown phase persistence;
- cross-instance and layout-mismatch reconstruction rejection without replacing the valid active record;
- exact-snapshot freeze after receipt creation;
- retained-receipt entry blocking after active teardown;
- explicit exact-instance receipt clearing restoring creation eligibility.

## Remaining integration order

The next bounded slice should reconcile `PreviewAppraisalService` with the current explicit multi-ability `AspectAbilitySetData` model and make appraisal observation/idempotency suitable for `NightmareCompletionRecoveryPlan`. Runtime terminal-event/login wiring should follow only after that adapter is reviewable, so storage, player identity reconciliation, and orchestration are not collapsed into one change.
