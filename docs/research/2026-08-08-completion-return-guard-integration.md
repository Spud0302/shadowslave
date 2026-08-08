# Successful completion return guard integration — 2026-08-08

## Scope

This slice restores one correctness guard from the historical Nightmare-recovery lineage onto the active current-main integration stack before the runtime `NightmareCompletionCoordinator.Operations` adapter is wired.

The rebuilt coordinator can currently call `returnPlayer()`, then persist player state, advance the receipt to `RETURN_COMMITTED`, and tear down active ownership without verifying that the authoritative player actually left the Nightmare dimension.

Bundled NeoForge 21.1.244 behavior was already audited in the historical correctness stack: cross-dimension travel can be cancelled while `ServerPlayer.teleportTo(...)` returns normally. Treating method return as successful travel is therefore not a safe commit boundary.

## Correctness rule

After the coordinator requests the successful return, it re-observes `playerInNightmare()` before any return-side player persistence or `RETURN_COMMITTED` registry advancement.

If the player remains in the Nightmare dimension, recovery fails while preserving:

- the already committed appraisal;
- the `APPRAISAL_COMMITTED` receipt;
- active Nightmare ownership;
- the ability to retry return on a later recovery attempt.

The transaction must not persist a false return or tear down its only recovery authority.

## Return-origin invariant

The post-return check is only sound if the internal Nightmare dimension can never be recorded as a legitimate return origin for a newly created Nightmare. `NightmareService.tryEnter(...)` therefore rejects a fresh entry while the player is already physically in `NIGHTMARE_LEVEL`, before registry creation.

A player stranded in the internal Nightmare dimension without valid ownership is a technical-recovery condition, not a valid origin for another First Nightmare.

## Evidence classification

- **CANON:** unchanged. This slice does not change Nightmare completion, appraisal, progression, death, or return lore.
- **INFERRED:** unchanged association between one incomplete successful-completion recovery transaction and its resolved Nightmare instance.
- **DESIGN:** successful return is committed only after authoritative server state shows the player outside the internal Nightmare dimension; the internal Nightmare dimension is rejected as a fresh return origin.
- **UNKNOWN:** live NeoForge event-cancellation fault injection is not executed in this connector-only environment; arbitrary third-party redirection into another non-Nightmare dimension remains outside this bounded guard; physical process-kill rows remain Issue #34 work.
- **COMPATIBILITY:** ordinary entry from normal dimensions and ordinary successful return are unchanged; cancelled returns remain retryable instead of being misclassified as committed.

No new canon rule is introduced, so no new primary-novel proposition is asserted. `docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` remain controlling.

## Tests

`NightmareCompletionCoordinatorTest.cancelledReturnDoesNotCommitReturnOrTeardown` requires a cancelled return to fail before return persistence, `RETURN_COMMITTED`, or teardown.

`NightmareReturnOriginGuardTest` requires `NIGHTMARE_LEVEL` to be rejected as a fresh entry origin while an ordinary overworld origin remains valid.

## Remaining integration order

After this guard is reviewable, the next bounded slice can safely implement the live `NightmareCompletionCoordinator.Operations` adapter using:

- `NightmareRegistryData` completion receipts and exact active ownership;
- `PreviewAppraisalService` appraisal observation/reconciliation;
- synchronous player persistence;
- `SavedDataPersistence.saveAndWait(...)` for registry durability;
- successful return plus the coordinator's post-return observation;
- exact-instance teardown.

Terminal signal-fire routing and login recovery should remain separate if possible so the adapter can be reviewed independently.
