# GPT handoff — current Java/core baseline

**Read first in a new GPT or Claude session.**  
**Repository:** `Spud0302/shadowslave`  
**Baseline branch:** `main`  
**Baseline commit:** `96a3422c2469a832f9a977a4521cc3f3b62edc5b`  
**Active correctness edge:** PR #178, head `a802a68cd1f0c007cef586c38a92683de87a63dc`

## Mandatory lore/source procedure

Before lore-sensitive work, read `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, the relevant roadmap, and current research notes. Novel chapter text is authoritative; the owner-designated access host is only a research access layer. Check later clarifications and keep **CANON**, **INFERRED**, **DESIGN**, **UNKNOWN**, and **COMPATIBILITY** distinct.

## Current repository state

`main` has moved materially beyond the August 1 preview baseline and now includes shared Java/core foundations plus merged Nightmare role/scenario compatibility content. Active gameplay integration is owned by PRs #179, #181 and #182; do not duplicate that work from the correctness lineage.

Issue #34 remains the persistence/restart tracker. The active correctness lineage includes replayable successful completion plus explicit persistence verification for recovery-critical canonical-death, technical/admin-exit and preview-reset authority boundaries.

Corrected PR #178 head `a802a68cd1f0c007cef586c38a92683de87a63dc` passed Preview Gates #164 / run `31368518381`. Its only review finding was fixed by validating all surviving persisted Nightmare instances through the production loader before canonical-death authority may be consumed; the review thread is resolved.

## Persistence audit rule

Do **not** add a file/digest checkpoint after every save. Add another recovery boundary only when loss of the last meaningful replay authority can demonstrably leave an unrecoverable persisted split.

Prepared Nightmare world/chunk durability is separately **BLOCKED** under #158. Resume it only when one of its recorded conditions changes: new reconstruction/live fault evidence, stronger save-path evidence, owner convergence-policy input, dependency/code change, or another credible design.

The hosted frozen-datapack Nightmare-dimension generation stall has repeated without a deterministic gameplay defect. Do not blind-rerun or raise budgets again without fresh lag/protocol evidence.

## Verification rules

For Java/core changes, the expected hosted gate covers Gradle compile/JUnit/package, completion recovery self-tests, physical NeoForge client boot, dedicated-server boot and artifact upload. The frozen datapack gate separately builds/validates/deploys and runs its live harnesses. Report hosted evidence only for the exact tested head.

Physical NeoForge smoke is not proof of fsync/power-loss behavior or a real process-kill/restart matrix. Those remain stronger evidence requirements where Issue #34 calls for them.

## Evidence classification for the current correctness lineage

- **CANON:** unchanged by the persistence infrastructure.
- **INFERRED:** none newly added by the current verifiers.
- **DESIGN:** receipts/markers, persisted-state verification, fail-closed transaction ordering, fault/provenance tooling and technical recovery.
- **UNKNOWN:** physical power-loss/fsync guarantees, post-verification storage corruption and unexecuted real process-kill rows.
- **COMPATIBILITY:** crash/admin/development recovery remains technical rather than ordinary in-world Spell mercy; no schema change should be assumed unless explicitly documented.

## Workflow rules

- Never write directly to or merge into `main`; use `gpt/` branches and reviewable PRs.
- Avoid duplicating active PRs; update an existing owner when the same slice is already open.
- Prioritize correctness/persistence blockers before new content.
- After two consecutive no-progress runs on one blocker, record attempts, evidence and the exact resume condition; mark/comment it blocked and move on.
- Do not invent canon. If source access or owner decisions block a lore-sensitive change, do research/design/test work and record the blocker instead.
- Preserve historical findings and supersede them explicitly rather than rewriting history.

## Next recommended slice

Finish review/merge evaluation of green #178 first. Then audit another transaction only if loss of its final replay authority can be demonstrated. Leave #158 blocked, and let #179/#181/#182 own the active role/scenario/physical-creature integration stack.
