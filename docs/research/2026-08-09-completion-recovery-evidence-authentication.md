# Completion recovery evidence authentication

**Date:** 2026-08-09  
**Scope:** Issue #34 physical successful-completion fault harness only  
**Parent:** PR #110 / `gpt/harden-completion-fault-evidence-freshness`

## Finding

The final `verify` stage authenticated the successful fault stage through `point.txt` and the exact intentional-fault marker, and it counted exactly one preview appraisal and one successful-completion teardown across retained console logs.

It did **not** authenticate that the recovery Gradle process itself stopped successfully.

`recover <point>` writes `recovery-gradle-exit-status.txt` before checking whether the status is zero. If recovery reaches the expected appraisal/teardown markers and then the server or Gradle process fails later, the command correctly returns failure, but the row retains a recovery console containing those markers. Calling `verify` afterward could therefore report PASS because it ignored the retained non-zero recovery status.

The same final-verifier gap existed for an accidentally armed completion-fault marker in retained recovery evidence: `run_recovery` rejects it during collection, but `verify` did not independently reject it.

## Correction

`verify` now requires:

1. retained successful fault-stage authentication (`point.txt` plus exact fault marker);
2. a retained `recovery-gradle-exit-status.txt`;
3. recovery Gradle status exactly `0`;
4. no `INTENTIONAL COMPLETION FAULT` marker in either recovery console or retained recovery `latest.log`;
5. exactly one instance-keyed appraisal and one successful-completion teardown;
6. no duplicate/stale teardown warning.

The synthetic `self-test` now proves that otherwise-valid appraisal/teardown markers fail verification when the retained recovery status is non-zero, and that a recovery-stage intentional-fault marker also fails final verification.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, progression, return, death, Aspect, Flaw, Attribute, Memory, Echo, or Seed mechanic changes.
- **INFERRED:** none added.
- **DESIGN:** a physical recovery row is not authenticated unless the recovery process itself stopped successfully and the recovery launch was not still fault-armed.
- **UNKNOWN:** no real same-world physical process-kill/restart row is proven here; real ModDev child diagnostics, player state after restart, same-world/JAR/player provenance, and second-relog idempotency still require retained physical evidence.
- **COMPATIBILITY:** successful recovery attempts are accepted as before; only attempts that already failed collection, or whose retained evidence is inconsistent with a clean recovery launch, are prevented from later becoming false-positive verification results.

## Source-policy note

This is test/evidence infrastructure, not a lore-sensitive mechanic. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read. No new novel proposition is introduced, so no canon rule is inferred or invented for this slice.

## Remaining physical evidence

Issue #34 still requires execution of all six current same-world fault boundaries. `after_terminal_registry_save` remains the first recommended row after this harness correction is folded into the active runner stack.
