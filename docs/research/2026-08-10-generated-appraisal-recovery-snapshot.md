# Generated appraisal recovery snapshot — 2026-08-10

## Scope

This is a bounded Issue #34 prerequisite on top of the green direct-to-main alpha integration edge, PR #193.

PR #193 now commits generated Aspect, Flaw and Attribute identity plus Ash Compass Memory and Ash Burrower Echo state, but successful completion still tears down active Nightmare ownership before appraisal. The older restart-recovery lineage contains a durable completion receipt, but it predates the generated-award stack and has divergent ancestry. Blindly transplanting that whole lineage would mix transaction changes, gameplay integration and generated-award semantics in one review.

This slice therefore defines the exact replay payload that the eventual successful-completion receipt must carry before transaction integration proceeds.

## Concrete correctness requirement

`FirstNightmareAppraisalResolver` is intentionally a versioned project generator. Its result depends on generator logic, catalogue contents, the completed Nightmare instance and terminal-resolution evidence. A restart recovery path must not invoke the current generator again after an award has already been resolved. A later catalogue or generator revision could otherwise replace a previously resolved permanent identity or reward set.

`GeneratedAppraisalRecoverySnapshot` stores:

- generator version;
- generator seed;
- generation fingerprint;
- the exact persistent `SoulIdentityData` containing the resolved Aspect and Flaw;
- the exact persistent awarded `AttributeInstanceData`;
- the exact persistent `MemoryInstanceData`;
- the exact persistent `EchoInstanceData`.

The snapshot uses the same codecs as those persistent records. Missing or malformed required payload fails closed. `PreviewAppraisalService.CommittedAppraisal` now exposes the exact identity and Attribute objects it actually writes, so recovery/presentation code does not need to reconstruct those records from a generator result.

This PR does **not** yet attach the snapshot to `NightmareCompletionRecord`, change successful-completion ordering, or claim restart atomicity solved. It is the schema/replay prerequisite for that next integration.

## Why this is the next unblocked slice

The previous recorded resume condition for generated-award recovery required a stable gameplay/appraisal integration base and an explicit integration order with the Issue #34 lineage. PR #193 is now a direct-to-`main`, green, review-clean integration edge. That supplies a stable generated-appraisal side of the boundary.

The older recovery lineage is still structurally divergent, so the safest bounded step is to pin the exact recovery payload first. The subsequent transaction PR can then combine the two lineages around a tested, explicit payload rather than inventing a recovery representation during a large merge.

Prepared Nightmare world/chunk durability remains separately blocked under #158 and is not retried here.

## Evidence classification

### CANON

Unchanged. No new Shadow Slave mechanic, appraisal formula, reward rule, Aspect rule, Flaw rule, Attribute rule, Memory rule or Echo rule is introduced.

### INFERRED

Once a generated appraisal has resolved a permanent identity/reward result, restart recovery should preserve that exact resolved result rather than re-resolving it against later generator or catalogue state. This is consistent with the repository's existing generated-identity provenance design and with presentation consuming committed Java state.

### DESIGN

- the NBT recovery snapshot schema;
- storing generator version/seed/fingerprint alongside exact persistent award objects;
- failing closed on incomplete/malformed snapshot data;
- exposing exact identity/Attribute objects in `CommittedAppraisal`;
- using this snapshot as the future successful-completion replay payload.

### UNKNOWN

- the final combined completion-receipt migration/versioning strategy;
- exact ordering of snapshot persistence versus appraisal application in the combined #193 + Issue #34 transaction;
- physical process/power-loss/fsync guarantees below readable persistence images;
- physical same-world restart behavior at every future combined durable boundary.

### COMPATIBILITY

No current player attachment or SavedData schema changes in this slice. Normal appraisal behavior and generated selection are unchanged. The snapshot is not yet persisted by production runtime. Existing Memory/Echo/identity/Attribute codecs remain the canonical project persistence definitions reused by the snapshot.

## Validation target

Focused tests must prove:

1. a complete exact generated award round-trips through NBT without generator/catalogue lookup on load;
2. stored identity/reward records remain byte-semantic values independent of what a different terminal resolution would generate;
3. malformed/incomplete recovery payload fails closed.

Hosted Preview Gates on the exact PR head remain required before this prerequisite is called green.

## Next slice after green review

Integrate this snapshot into the durable successful-completion receipt on an explicit branch combining PR #193's gameplay/appraisal state with the green Issue #34 recovery lineage. Resolve and persist the snapshot before active ownership can be consumed, replay the exact snapshot after restart, and extend deterministic fault/reconstruction tests so Aspect, Flaw, Attribute, Memory, Echo, Soul progression, return and teardown converge exactly once without rerunning the generator.
