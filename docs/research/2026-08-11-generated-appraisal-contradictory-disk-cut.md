# Generated appraisal contradictory disk-cut evidence

**Date:** 2026-08-11  
**Tracks:** #34  
**Parent evidence:** PR #223 / `gpt/generated-appraisal-disk-image-restart-evidence`

## Purpose

PR #223 proves a healthy persisted successful-completion cut can be reconstructed from compressed completion-receipt, Nightmare-registry and player attachment images across fresh JVMs. This follow-up pins the complementary fail-closed case: a durable successful-completion receipt must not be allowed to consume or overwrite a different active Nightmare that persisted for the same player.

## Persisted cut

The test writes two production-shaped compressed SavedData images:

- `shadowslave_nightmare_completion_receipts.dat` contains one resolved completion receipt;
- `shadowslave_nightmares.dat` contains a different active `NightmareInstance` for the same player.

A fresh JVM production-decodes both images and calls the package-private production `GeneratedAppraisalRecoveryService.activeInstanceForReplay(...)` selector through a test-only reflection seam. The exact contradictory instance must be rejected. The parent JVM then requires both persisted files to remain byte-for-byte unchanged.

This guards the recovery-authority boundary rather than adding another persistence checkpoint. The durable receipt remains available for diagnosis/retry and unrelated active ownership is not destructively consumed.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, progression, reward, death or failure rule changes.
- **INFERRED:** an already-resolved appraisal should remain the same resolved result through technical restart rather than be regenerated against later code/catalogue state.
- **DESIGN:** contradictory durable ownership fails closed; recovery authority and unrelated active ownership remain untouched when the exact receipt/active-instance identity does not match.
- **UNKNOWN:** live NeoForge process-kill timing, real `ServerPlayer` return teleport persistence, server save scheduling, filesystem/fsync guarantees below readable files, and post-verification corruption.
- **COMPATIBILITY:** test-only. No runtime method visibility, save schema, catalogue, dependency or gameplay behavior changes.

## Lore/source boundary

No new lore-sensitive proposition is introduced. `docs/LORE-SOURCE-POLICY.md` requires primary-novel research when lore mechanics change, while `docs/JAVA-LORE-ALIGNMENT.md` explicitly classifies server-restart/crash recovery as technical infrastructure. This test therefore does not invent a canon recovery rule or manufacture novel support for a Java consistency invariant.

## Limitations and next step

This does not invoke `GeneratedAppraisalRecoveryService.replayPending(ServerPlayer)` in a live dedicated server and does not prove waking-location persistence. The next stronger #34 slice remains a dedicated NeoForge stop/kill-and-restart harness using actual world/player SavedData, proving return location, active ownership teardown, exact Dreamer appraisal state and receipt consumption converge exactly once.

The recurring hosted vanilla Nightmare-transition/keepalive stall is a separate infrastructure symptom. Do not resume blind timeout/rerun attempts without new diagnostics, a changed harness/server path, dependency/runner changes or another credible approach.
