# Generated appraisal restart-cut matrix

**Date:** 2026-08-11  
**Parent correctness edge:** PR #203 / `gpt/verify-generated-appraisal-player-persistence`  
**Tracker:** Issue #34

## Why this slice

PR #203 establishes durable successful-completion receipt authority, exact award replay, return/teardown convergence, persisted player-state verification, and verified receipt deletion. Its documented next requirement is deterministic restart/process-loss evidence around the adjacent durable boundaries rather than another speculative persistence checkpoint.

This slice adds a pure deterministic reconstruction matrix. Every case now reconstructs the complete `NightmareCompletionReceiptData.Receipt` through its production NBT codec before planning recovery. That round-trip includes both the serialized `NightmareInstance` and the `GeneratedAppraisalRecoverySnapshot`, deliberately discarding both in-memory objects that existed before the modeled restart.

The first review version round-tripped only the appraisal snapshot and then rebuilt a receipt around the original in-memory `NightmareInstance`. Review correctly identified that as insufficient evidence: a regression in the receipt or `NightmareInstance` codec could have remained invisible while the matrix still claimed to model durable receipt reconstruction. The corrected matrix uses the production receipt codec itself at every restart cut.

## Covered restart cuts

The focused matrix proves:

1. **receipt durable, active Nightmare still durable, player still Aspirant** — the exact matching reconstructed active instance is selected for successful teardown and the exact stored appraisal converges;
2. **receipt durable, active Nightmare already absent, player still Aspirant** — recovery does not require stale ownership and the exact stored appraisal still converges;
3. **receipt durable, teardown absent, player attachments only partially committed** — missing Attribute/Memory/Echo state is filled without duplicating already-present exact state;
4. **receipt durable, player award fully committed, receipt not yet deleted** — planning is idempotent and retains exactly one copy of every generated award surface;
5. **repeated restart/replay planning** — the second plan is already complete and cannot duplicate Attribute, Memory or Echo ownership.

This is deterministic reconstruction evidence, not a timing-based process-kill test.

## CI evidence and hosted datapack failure

The initial matrix head `69fcf9a66370a095c66d8f678626c677a0fc3bec` completed Preview Gates #214 / Actions run `31417169837` with the entire Java job green: wrapper validation, compile, all unit tests, package, physical NeoForge client boot, dedicated NeoForge server boot, and development JAR upload all passed.

The separate deployed vanilla datapack job failed only in the historical issue #20 regression harness after the lifecycle **32/32** and deterministic Flaw **39/39** suites had already passed. Alice timed out after 60 seconds waiting for the initial transition into `shadowslave:nightmare`; the server log shows the command began and then no transition was observed before the harness deadline. This is the same recurring hosted Nightmare-dimension transition/generation stall seen in earlier unrelated branches.

Because this failure has repeated across unrelated heads and this PR changes no datapack runtime, the failure is recorded rather than blindly retried or addressed by another timeout increase. Resume investigation of that infrastructure symptom only with new evidence, a changed harness/server path, a dependency update, or a credible new diagnostic approach.

The complete-receipt review correction requires fresh exact-head CI before this PR is called green.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, reward, progression, or failure rule changes.
- **INFERRED:** an already-resolved appraisal should remain the same exact appraisal across technical restart instead of being regenerated against later generator/catalogue state.
- **DESIGN:** the completion receipt is recovery authority; deterministic restart tests reconstruct the complete persisted receipt and require idempotent convergence across supported persisted cuts.
- **UNKNOWN:** hardware-level fsync/power-loss guarantees, corruption after successful read-back, physical process-kill behavior in a live NeoForge server, and failures below a readable persisted file image.
- **COMPATIBILITY:** no persistence schema or gameplay semantics change. The receipt codec is exposed as a public persistence boundary so cross-package restart tests can exercise the exact production codec instead of duplicating it.

`docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` were re-read. This slice is technical crash/restart recovery, so no new primary-novel proposition is introduced or inferred from convenience. `docs/NIGHTMARE-SEED-ROADMAP.md` requirement 11 remains the governing gameplay-engine expectation: restart/reload must preserve resolution state without duplicating rewards or teardown.

## Limits and next evidence

This matrix does not claim that an operating-system process was killed at each boundary. After corrected exact-head CI/review is green, the next strongest evidence is a live or harnessed restart/fault test that materializes the corresponding files at the supported boundaries, restarts the server, and verifies exact return location, absence of active ownership, Dreamer Soul state, Aspect/Flaw identity, Attribute, Memory, Echo, and receipt consumption exactly once.

Do not reopen prepared-world durability #158 without its recorded resume condition, and do not add another persistence checkpoint unless a concrete last-authority failure model is demonstrated.
