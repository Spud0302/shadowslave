# Completion player recovery verifier CI integration — 2026-08-09

## Scope

This note records a bounded testability change on top of PR #117. It does not change Nightmare completion, appraisal, return, persistence, player identity, or any lore-facing mechanic.

## Evidence reviewed

PR #117 adds `mod/verify-completion-player-recovery.sh` and documents that its process-free `self-test` is not yet wired into hosted CI. Preview Gates run #88 for exact PR #117 head `7605364231f59abcbc5c35581f45b7a4edb21c20` proves the Java job itself is healthy: Gradle compile/unit/package, the existing completion fault-runner self-test, physical NeoForge client boot, and dedicated-server boot all passed. The overall run failed only in the frozen-datapack job during Issue #20 regression setup; that unrelated datapack failure is being treated separately and is not used to infer a Java defect.

The new verifier is part of Issue #34's evidence contract because it authenticates the same player UUID and converged successful-completion state across the recovery login and the required second relog. Leaving its own parser/negative-case self-test opt-in would allow a shell-level regression to escape the hosted Java gate even while the Java-side marker producer remained green.

## Change

The `Preview gates` Java job now executes:

```text
bash mod/verify-completion-player-recovery.sh self-test
```

immediately after the existing completion-fault runner self-test and before physical client/server smoke.

This preserves the existing gate ordering: cheap deterministic checks fail before the more expensive Minecraft boots.

## Evidence classification

- **CANON:** unchanged. No novel-facing mechanic changes.
- **INFERRED:** none added.
- **DESIGN:** hosted CI executes the recovery-verifier's process-free contract test on every relevant PR update.
- **UNKNOWN:** the six real same-world process-kill/restart rows remain unexecuted; the verifier self-test does not prove live player recovery by itself.
- **COMPATIBILITY:** workflow dispatch, PR/push triggers, Gradle build, existing fault-runner self-test, NeoForge smoke, and datapack gates remain unchanged. This adds one deterministic shell gate only.

## Lore/source-policy consequence

No lore-sensitive rule is introduced or generalized, so no new primary-novel proposition is required. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read before the change; technical restart recovery remains explicitly DESIGN rather than a Spell mechanic.

## Remaining limitation

A green hosted self-test only authenticates the verifier implementation against synthetic logs. Issue #34 still requires a real disposable-world row beginning with `after_terminal_registry_save`, followed by recovery, a second relog, transaction/provenance verification, and player-recovery verification.
