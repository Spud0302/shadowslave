# Generated appraisal restart-cut matrix

**Date:** 2026-08-11  
**Parent correctness edge:** PR #203 / `gpt/verify-generated-appraisal-player-persistence`  
**Tracker:** Issue #34

## Why this slice

PR #203 establishes durable successful-completion receipt authority, exact award replay, return/teardown convergence, persisted player-state verification, and verified receipt deletion. Its documented next requirement is deterministic restart/process-loss evidence around the adjacent durable boundaries rather than another speculative persistence checkpoint.

This slice adds a pure deterministic reconstruction matrix. Each case reconstructs the stored `GeneratedAppraisalRecoverySnapshot` through its NBT codec before planning recovery, deliberately discarding any in-memory generator result and modeling a new process reading durable receipt authority.

## Covered restart cuts

The focused matrix proves:

1. **receipt durable, active Nightmare still durable, player still Aspirant** — the exact matching active instance is selected for successful teardown and the exact stored appraisal converges;
2. **receipt durable, active Nightmare already absent, player still Aspirant** — recovery does not require stale ownership and the exact stored appraisal still converges;
3. **receipt durable, teardown absent, player attachments only partially committed** — missing Attribute/Memory/Echo state is filled without duplicating already-present exact state;
4. **receipt durable, player award fully committed, receipt not yet deleted** — planning is idempotent and retains exactly one copy of every generated award surface;
5. **repeated restart/replay planning** — the second plan is already complete and cannot duplicate Attribute, Memory or Echo ownership.

This is deterministic reconstruction evidence, not a timing-based process-kill test.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, reward, progression, or failure rule changes.
- **INFERRED:** an already-resolved appraisal should remain the same exact appraisal across technical restart instead of being regenerated against later generator/catalogue state.
- **DESIGN:** the completion receipt is recovery authority; deterministic restart tests reconstruct its exact stored snapshot and require idempotent convergence across supported persisted cuts.
- **UNKNOWN:** hardware-level fsync/power-loss guarantees, corruption after successful read-back, physical process-kill behavior in a live NeoForge server, and failures below a readable persisted file image.
- **COMPATIBILITY:** no runtime behavior or persistence schema changes. The tests exercise the existing #203 recovery contract and use the existing generated-award codecs.

`docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` were re-read. This slice is technical crash/restart recovery, so no new primary-novel proposition is introduced or inferred from convenience. `docs/NIGHTMARE-SEED-ROADMAP.md` requirement 11 remains the governing gameplay-engine expectation: restart/reload must preserve resolution state without duplicating rewards or teardown.

## Limits and next evidence

This matrix does not claim that an operating-system process was killed at each boundary. After exact-head CI/review is green, the next strongest evidence is a live or harnessed restart/fault test that materializes the corresponding files at the supported boundaries, restarts the server, and verifies exact return location, absence of active ownership, Dreamer Soul state, Aspect/Flaw identity, Attribute, Memory, Echo, and receipt consumption exactly once.

Do not reopen prepared-world durability #158 without its recorded resume condition, and do not add another persistence checkpoint unless a concrete last-authority failure model is demonstrated.
