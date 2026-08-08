# Completion fault evidence freshness hardening — 2026-08-09

## Scope

This note records a bounded Issue #34 harness-integrity correction on top of PR #108. It does not change the successful Nightmare transaction, persistence schema, appraisal result, return semantics, teardown semantics, or any lore-facing mechanic.

## Finding

PR #108 intentionally retains one evidence directory per deterministic completion-fault point and supports rerunning a row. Before this correction, `run_fault` truncated the current console log but did not remove an older `fault-latest.log`, `point.txt`, or status/recovery artifacts before beginning a new attempt.

That created an evidence-contamination path:

1. an earlier attempt for a point leaves a valid `fault-latest.log` marker and `point.txt`;
2. a later attempt for the same point starts;
3. the current Minecraft run fails before producing a new `logs/latest.log`, or otherwise does not replace the fallback file;
4. `copy_latest_log` has nothing current to copy, so the old row-level `fault-latest.log` remains;
5. the fallback marker check can therefore authenticate the current attempt with stale evidence.

A failed replacement fault attempt could also leave the older `point.txt`, allowing `recover` to proceed against mixed-attempt evidence.

The same freshness principle applies to recovery artifacts: a new recovery attempt must not retain an older `recovery-latest.log` or recovery status file if the current process fails before recreating them.

## Correction

A new fault attempt now invalidates all prior attempt-specific artifacts for that row before launching Gradle:

- `fault-console.log`;
- `fault-latest.log`;
- `point.txt`;
- `gradle-exit-status.txt`;
- `recovery-console.log`;
- `recovery-latest.log`;
- `recovery-gradle-exit-status.txt`.

`point.txt` is recreated only after the current fault stage has passed both the exact fault-marker check and the child-exit-86 diagnostic check. Therefore `recover` cannot treat a failed replacement attempt as authenticated by an older run.

A new recovery attempt independently clears its prior recovery console/latest/status artifacts before launch.

Verification now also requires `point.txt` to exist and contain the requested point. This is still not a cryptographic provenance mechanism; it is an internal consistency guard preventing mixed-attempt row evidence.

Recovery's accidental-fault check now examines both Gradle console capture and the newly retained Minecraft `latest.log`, matching the established two-surface logging reality without broadening appraisal/teardown counts.

## Self-test extension

The process-free shell self-test now synthesizes stale fault and recovery artifacts, calls the new invalidation helpers, and requires those stale artifacts to be absent before rebuilding the normal synthetic evidence case. It retains the existing fallback-marker and duplicate-teardown checks.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, return, progression, death, Aspect, Flaw, Attribute, Memory, Echo, or Seed rule changes.
- **INFERRED:** none added.
- **DESIGN:** retained physical-test evidence from one attempt must not authenticate a later attempt for the same deterministic boundary; a successful fault-stage marker file is recreated only after current-attempt checks pass.
- **UNKNOWN:** the actual six same-world process-kill/restart rows remain unexecuted; real ModDev child-exit wording, live player state after restart, and second-relog idempotency remain physical evidence requirements.
- **COMPATIBILITY:** valid first attempts behave the same. Rerunning a row now deliberately discards its prior attempt-specific logs/status so evidence cannot be mixed. The disposable world itself is preserved exactly as before.

## Source-policy boundary

`docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read before this change. No lore-sensitive mechanic is modified, so no new primary-novel proposition is introduced or generalized.

## Next evidence step

After this correction is folded into the #108 runner, execute `after_terminal_registry_save` on a disposable same-world baseline. Preserve only the evidence produced by that attempt, then use the observed logs to decide whether any further harness or runtime correction is justified.
