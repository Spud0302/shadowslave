# Nightmare successful-return verification boundary

**Date:** 2026-08-08  
**Scope:** technical successful-completion recovery only

## Finding

The successful-completion coordinator called `returnPlayer()` and then immediately persisted the player, advanced the durable receipt to `RETURN_COMMITTED`, and proceeded toward active-instance teardown.

The adjacent entry audit established a NeoForge 21.1.244 behavior that matters here too: a cross-dimension `ServerPlayer.teleportTo(...)` can return normally when `EntityTravelToDimensionEvent` cancels the travel. `NightmareService.teleportToReturn(...)` currently delegates to that call without its own success result.

Therefore the coordinator could treat a cancelled return as committed, then remove the active Nightmare while the player was still physically inside the Nightmare dimension. That would destroy the authoritative ownership handle needed for normal recovery while the physical player state still required it.

## Boundary

For successful completion, return is not durable merely because the return operation returned normally. Before the player save, `RETURN_COMMITTED`, or teardown, the coordinator requires `playerInNightmare()` to be false.

If the return operation returns but the player remains in the Nightmare:

- the coordinator throws;
- the already-durable appraisal remains committed;
- the return player save is not performed;
- the receipt remains at `APPRAISAL_COMMITTED`;
- active ownership remains present;
- teardown does not run;
- a later resume can retry the return from authoritative retained state.

The check is intentionally dimension-based. The verified defect is cancelled cross-dimension travel. This slice does not invent a positional tolerance or define arbitrary third-party redirection semantics.

## Evidence classification

- **CANON:** unchanged Issue #34 / PR #39 evidence for terminal Nightmare resolution before appraisal, progression and return. No lore mechanic changes here.
- **INFERRED:** unchanged association between one durable successful-completion transaction and one resolved Nightmare instance.
- **DESIGN:** a successful-return operation must be observed to have moved the player out of the Nightmare before the technical transaction can commit return or teardown.
- **UNKNOWN:** live NeoForge cancellation fault injection is not executed here; exact handling of a third-party redirect into some other non-Nightmare dimension remains outside this slice.
- **COMPATIBILITY:** normal successful return and restart replay are unchanged; a cancelled return now remains retryable instead of being misclassified as committed.

## Tests

`NightmareCompletionCoordinatorTest.cancelledReturnDoesNotCommitReturnOrTeardown` models a return operation that returns normally but leaves `playerInNightmare()` true. It asserts that appraisal is retained, the receipt remains at `APPRAISAL_COMMITTED`, ownership remains present, and neither return persistence nor teardown occurs.

The existing restart-after-every-durable-boundary test continues to cover the valid successful path.

## Deliberate limits

This slice covers successful completion only. `NightmareService.exit(...)` uses the same unchecked `teleportToReturn(...)` helper for technical recovery and admin abort, so those non-success exits should receive a separate bounded audit. This change does not claim process-crash atomicity, live event-cancellation integration coverage, or completion of Issue #34's dedicated-server restart matrix.
