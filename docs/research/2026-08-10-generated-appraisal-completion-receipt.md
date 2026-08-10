# Generated appraisal completion receipt

## Scope

This slice closes the zero-authority handoff between a terminal First-Nightmare resolution and the current generated appraisal transaction. It does not claim the full Issue #34 restart replay transaction solved.

Current `main` can prepare the exact generated Aspect, Flaw, Attribute, Memory and Echo award before player mutation, but active Nightmare ownership is still consumed before that award has independent durable recovery authority. This branch stores a `NightmareInstance + GeneratedAppraisalRecoverySnapshot` receipt in separate overworld SavedData and crosses an explicit SavedData durability barrier before normal success teardown.

## Evidence classification

- **CANON:** unchanged. Nightmare completion remains distinct from appraisal, and this slice adds no new progression or reward rule.
- **INFERRED:** once the appraisal result is resolved for a conquered Nightmare, technical recovery should preserve that exact result rather than rerun a generator whose catalogue/version may later change.
- **DESIGN:** the separate `shadowslave_nightmare_completion_receipts.dat` authority record, its exact-instance matching, and the save-and-wait durability boundary are Java recovery transaction infrastructure.
- **UNKNOWN:** physical fsync/power-loss guarantees below the observable SavedData write, final receipt phase/checkpoint schema, exact player-file persistence verification, and process-kill evidence.
- **COMPATIBILITY:** current player attachment schemas and generated-award semantics do not change. A player with a retained completion receipt is refused a new First Nightmare until recovery authority is consumed.

## Ordering established by this slice

1. resolve the exact current active Nightmare snapshot;
2. prepare the exact generated appraisal without mutating player attachments;
3. persist `NightmareInstance + GeneratedAppraisalRecoverySnapshot` recovery authority;
4. wait for the overworld SavedData write barrier;
5. only then teleport/teardown active Nightmare ownership;
6. commit the already-prepared appraisal without rerunning generation;
7. clear and persist the receipt after the normal appraisal succeeds.

If appraisal fails after teardown, the receipt is deliberately retained rather than converting the player to an unrelated Carrier state and destroying the only exact award authority.

## Remaining Issue #34 work / resume condition

This branch intentionally stops before claiming restart convergence. The next slice should consume a retained receipt during login/restart recovery and replay its exact stored identity/reward records idempotently, including the case where player attachment persistence may already contain some or all of the committed award. It must then prove receipt retention/consumption and exactly-once convergence at each durable boundary.

Resume condition: this exact branch must first pass Java unit/package, physical client, dedicated server and frozen-datapack hosted gates. If it is green, build the login replay on top of this receipt rather than porting unrelated death/reset/technical-exit branches.
