# Successful-completion receipt storage integration — 2026-08-08

## Scope

This slice ports the durable successful-Nightmare completion receipt into the current-main correctness consolidation lineage. It is intentionally limited to storage identity, phase monotonicity, reconstruction invariants, entry blocking while a receipt is retained, exact-snapshot mutation authority, round-trip persistence, and fail-closed load behavior.

It does **not** wire terminal gameplay events, appraisal identity, player saving, return teleport, teardown persistence, login recovery, or the physical fault runner.

## Why this is the next correctness slice

`NightmareCompletionCoordinator` now models restart-replayable successful completion, but the consolidation branch still had an active-instance-only `NightmareRegistryData`. Without a persisted receipt the runtime adapter would have no durable authority to reconstruct a terminal success after active ownership is later consumed.

The retained receipt stores the exact resolved `NightmareInstance`, the ordered completion phase, and the resolved game time. While both active ownership and a receipt exist, their complete `NightmareInstance` snapshots must be equal, including operational entity identity such as `pursuerId`. Deferring a divergence until teardown would leave the receipt carrying stale exact-snapshot authority.

A second persistence risk is deserializer failure. Minecraft's SavedData loading path may recover from a thrown decoder exception by constructing fresh data, so a corruption check that merely throws can become fail-open. This slice follows the repository's existing preview-reset pattern: malformed or contradictory Nightmare registry input returns a `recoveryBlocked` in-memory object with the failure reason retained. Reads, mutations, and saves then refuse to treat that registry as healthy. Because uncertain ownership may not be attributable to one player, the block is intentionally global rather than guessed away.

## Evidence classification

- **CANON:** unchanged from Issue #34 / PR #39 research. Primary chapter evidence checked in Chapters 15 and 742–744 establishes terminal Nightmare resolution followed by appraisal/progression/return handling; Chapter 1581 later treats absence of the normal appraisal space as exceptional rather than redefining the ordinary sequence.
- **INFERRED:** one incomplete successful-completion recovery transaction remains associated with the resolved Nightmare instance until technical reconciliation is complete.
- **DESIGN:** retained SavedData receipt, phase enum, exact-snapshot authorization, slot/identity consistency checks, global recovery blocking for undecodable ownership/completion metadata, and blocking a new technical Nightmare while an old completion receipt is retained.
- **UNKNOWN:** physical process-kill/restart behavior at each real storage boundary remains unproven until the dedicated-server matrix executes. This slice also does not decide appraisal generation rules, administrative repair UX for blocked registry data, or exceptional Spell-disconnected behavior.
- **COMPATIBILITY:** current runtime behavior is unchanged because no runtime event or login path consumes these new receipt APIs yet. Existing saves lacking `successful_completions` load with an empty receipt set and remain healthy. Current explicit multi-ability Aspect identity and newer Attribute/Memory/Echo/content APIs are untouched.

No canon rule is added or generalized by this storage change. The repository lore policy and Java lore-alignment gate remain controlling.

## Storage invariants

1. A completion receipt can begin only from the exact active instance snapshot.
2. Phase advancement is monotonic and may advance only one durable milestone at a time; replaying an already reached phase is idempotent.
3. The active snapshot is frozen once its receipt exists, keeping teardown/recovery authority exact.
4. Active ownership and its retained receipt must carry the exact same `NightmareInstance` snapshot while both exist.
5. One instance UUID and one physical slot cannot belong to different owners/instances across active and retained state.
6. A retained receipt survives active teardown and blocks creation eligibility until explicitly cleared by the exact recorded snapshot.
7. Missing `successful_completions` remains valid legacy state, but malformed list shape, duplicate/cross-owner identity, or contradictory active/receipt snapshots load as globally recovery-blocked rather than empty healthy state.

## Tests added

`NightmareCompletionReceiptStorageTest` covers:

- receipt round-trip after active ownership has been consumed;
- terminal -> appraisal -> return -> teardown phase persistence;
- cross-instance, layout, and pursuer-snapshot reconstruction rejection without replacing the valid active record;
- exact-snapshot freeze after receipt creation;
- retained-receipt entry blocking after active teardown;
- explicit exact-instance receipt clearing restoring creation eligibility.

`NightmareRegistryLoadFailureTest` covers:

- a legacy save without `successful_completions` loading healthy;
- a wrong-type completion list becoming retained blocked state rather than an empty registry;
- contradictory active/receipt exact snapshots becoming blocked state;
- blocked reads and saves refusing to proceed.

## Remaining integration order

The next bounded slice should reconcile `PreviewAppraisalService` with the current explicit multi-ability `AspectAbilitySetData` model and make appraisal observation/idempotency suitable for `NightmareCompletionRecoveryPlan`. Runtime terminal-event/login wiring should follow only after that adapter is reviewable, so storage, player identity reconciliation, and orchestration are not collapsed into one change.
