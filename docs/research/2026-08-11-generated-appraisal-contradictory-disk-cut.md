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

## Review correction

Static review found that the initial fixture helper accepted `(playerId, instanceId)` but forwarded those UUIDs to `NightmareInstance` in the opposite semantic order. The resulting active record therefore belonged to another player instead of representing the intended same-player/different-instance contradiction. Production recovery looks up active ownership by player before applying the replay selector, so that malformed fixture did not prove the real persisted conflict.

The corrected fixture now passes `instanceId` and `playerId` in the production constructor order and adds explicit assertions that the completed receipt and contradictory active record have the same `playerId` while their `instanceId` values differ. This makes future argument-order regressions fail at fixture construction rather than silently weakening the recovery evidence.

## Parent exact-head validation

The CI trigger blocker that previously prevented corrected PR heads from receiving Preview Gates was fixed by merged PR #231. Parent PR #223 then received Preview Gates #242 / run `31454021071` against exact head `4f1b18df4d68a3cbf5ee6833622d7cabef5e7585`.

The complete Java job passed: trigger-contract validation, Gradle wrapper validation, compile/all unit tests/package, physical NeoForge client boot, dedicated NeoForge server boot, and development JAR upload. Frozen-datapack build/validation also passed; lifecycle was 32/32 and deterministic Flaw coverage was 39/39. The only failing step was the already-recurring frozen-datapack `regression_issue20.mjs` transition observation: Alice's `shadowslave:test/nightmare` command was issued, but the hosted harness timed out after 60 seconds still observing `minecraft:overworld` amid server lag/movement warnings.

That harness symptom has repeated independently of this Java persistence work. Per the repository loop rule, do not rerun it blindly or increase another timeout. Resume that infrastructure investigation only with new diagnostic evidence, a changed harness/server path, dependency/runner changes, or another credible approach.

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

Prepared-world durability #158 remains blocked under its recorded resume condition and is not retried here. The recurring hosted vanilla Nightmare-transition/keepalive stall is likewise not resumed without new evidence.
