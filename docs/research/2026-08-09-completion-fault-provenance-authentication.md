# Completion fault provenance authentication

**Date:** 2026-08-09  
**Scope:** Issue #34 physical successful-completion fault matrix  
**Parent lineage:** PR #113 (`gpt/restore-preview-gates-for-pr-updates`)

## Problem

The current physical runner correctly authenticates the selected fault boundary, Minecraft child exit-86 evidence, recovery process success, exactly-once appraisal, and exactly-once successful-completion teardown. It still leaves two important row-provenance claims manual:

- recovery used the same code/JAR/source state as the fault attempt;
- recovery used the same disposable world as the fault attempt.

Those are not gameplay observations. They are machine-checkable evidence properties and should not depend on operator memory. A recovery accidentally launched after changing checkout, or against a replaced world directory, could otherwise produce plausible logs that are not evidence for the intended same-build/same-world restart row.

## Change

`mod/run-completion-fault-row.sh` now records provenance only after the fault stage has already authenticated the exact configured boundary and Minecraft child exit 86:

- `source-head.txt` — exact `git rev-parse HEAD` used for the fault attempt;
- `world-id.txt` — a generated marker stored inside the actual `run-completion-fault/world` directory.

The marker lives in the world directory rather than only in the evidence directory. Replacing the disposable world therefore removes the identity unless someone deliberately copies that marker too.

Before recovery launches, the runner requires the current checkout HEAD and current world's retained marker to match the fault-stage evidence. After a normal recovery stop it records:

- `recovery-source-head.txt`;
- `recovery-world-id.txt`.

Final verification requires the fault and recovery provenance pairs to match. Replacement fault/recovery attempts invalidate their corresponding provenance artifacts so stale values cannot authenticate a newer attempt.

The process-free `self-test` now proves source mismatch and world mismatch both fail final verification, in addition to the existing stale-artifact, recovery-status, accidental-fault, exactly-once, and duplicate-teardown cases.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, return, progression, death, Aspect, Flaw, Attribute, Memory, Echo, Seed, or Spell mechanic changes.
- **INFERRED:** none added.
- **DESIGN:** exact Git commit identity and a world-directory marker authenticate that the two halves of one physical restart row use the same repository source state and the same disposable world lineage.
- **UNKNOWN:** a world marker is not a cryptographic snapshot of every world byte; deliberate copying of the marker into a replacement world can defeat the check. The same real player, post-recovery Soul/status state, and second-relog idempotency still require live evidence. The six physical rows remain unexecuted until a real player performs them.
- **COMPATIBILITY:** ordinary mod/client/server/datapack behavior is unchanged. The runner's first fault attempt gains provenance files; reruns intentionally invalidate stale attempt-specific provenance just like stale logs/status.

## Lore/source-policy note

This slice is test infrastructure only. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read. No new lore proposition was introduced, so inventing a chapter-derived rule would be inappropriate.

## Remaining physical evidence

For a complete Issue #34 row, machine verification now covers fault point, exit-86 evidence, successful recovery process, exactly-once appraisal/teardown, and same-source/same-world provenance. A real operator still has to retain evidence that:

1. the same player reconnects;
2. the player converges to the expected Dreamer/Dormant + preview identity state;
3. no active Nightmare remains;
4. a second relog does not add appraisal/teardown markers or change recovered state.

The first recommended row remains `after_terminal_registry_save`.
