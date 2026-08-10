# GPT handoff — current Java/core baseline

**Read first in a new GPT or Claude session.**  
**Repository:** `Spud0302/shadowslave`  
**Baseline branch:** `main`  
**Baseline commit:** `96a3422c2469a832f9a977a4521cc3f3b62edc5b`  
**Active correctness edge:** PR #178, head `a802a68cd1f0c007cef586c38a92683de87a63dc`

## Mandatory lore/source procedure

Before lore-sensitive work, read `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, the relevant roadmap, and current research notes. Novel chapter text is authoritative; the owner-designated access host is only a research access layer. Check later clarifications and keep **CANON**, **INFERRED**, **DESIGN**, **UNKNOWN**, and **COMPATIBILITY** distinct.

## Current repository state

`main` has moved materially beyond the August 1 preview baseline and now includes shared Java/core foundations plus merged Nightmare role/scenario compatibility content.

Active gameplay/world integration is consolidated in PR #184, stacked on #181 and carrying #182's physical Nightmare Creature work by ancestry. Generated First-Nightmare appraisal awards are separately owned by PR #183. Do not duplicate either branch from correctness or documentation work.

Issue #34 remains the persistence/restart tracker. The active correctness lineage includes replayable successful completion plus explicit persistence verification for recovery-critical canonical-death, technical/admin-exit and preview-reset authority boundaries.

Corrected PR #178 head `a802a68cd1f0c007cef586c38a92683de87a63dc` passed Preview Gates #164 / run `31368518381`. Its only review finding was fixed by validating all surviving persisted Nightmare instances through the production loader before canonical-death authority may be consumed; the review thread is resolved.

PR #183 head `7f4a823c0252a563d2c5a5298edc6eb60c2c1f7c` passed Preview Gates #172 / run `31375808693` after correcting generated ability/Flaw runtime IDs. It persists deterministic generated Aspect/Flaw/Attribute awards on current `main`, but its PR explicitly leaves crash-atomic exactly-once completion to Issue #34's recovery lineage.

## Generated-appraisal recovery integration

Do not invent a parallel completion-receipt schema while #183 and #178 are separate active branches. The next integration is actionable only after #183 is review-stable and either merged/rebased onto a compatible correctness ancestry or an explicit integration base/order is chosen.

When that condition is met, the receipt/recovery path must retain enough exact resolved award identity/provenance to replay the same Aspect, Flaw and Attribute after restart. Do not rerun a version-sensitive generator during recovery and assume it will choose the same identities after catalogue or algorithm changes.

This is an integration/correctness dependency, not a new canon rule.

## Persistence audit rule

Do **not** add a file/digest checkpoint after every save. Add another recovery boundary only when loss of the last meaningful replay authority can demonstrably leave an unrecoverable persisted split.

Prepared Nightmare world/chunk durability is separately **BLOCKED** under #158. Resume it only when one of its recorded conditions changes: new reconstruction/live fault evidence, stronger save-path evidence, owner convergence-policy input, dependency/code change, or another credible design.

The hosted frozen-datapack Nightmare-dimension generation stall has repeated without a deterministic gameplay defect. Do not blind-rerun or raise budgets again without fresh lag/protocol evidence.

## Verification rules

For Java/core changes, the expected hosted gate covers Gradle compile/JUnit/package, completion recovery self-tests, physical NeoForge client boot, dedicated-server boot and artifact upload. The frozen datapack gate separately builds/validates/deploys and runs its live harnesses. Report hosted evidence only for the exact tested head.

Physical NeoForge smoke is not proof of fsync/power-loss behavior or a real process-kill/restart matrix. Those remain stronger evidence requirements where Issue #34 calls for them.

## Evidence classification for the current edge

- **CANON:** unchanged by the persistence/status infrastructure.
- **INFERRED:** a resolved appraisal identity should be retained exactly for restart replay rather than regenerated against potentially changed content/version state; this is the same inference explicitly recorded by #183.
- **DESIGN:** receipts/markers, persisted-state verification, exact generated-award replay, fail-closed transaction ordering, fault/provenance tooling and technical recovery.
- **UNKNOWN:** physical power-loss/fsync guarantees, post-verification storage corruption, unexecuted real process-kill rows, and the canonical Aspect/Flaw/Attribute determination formula/probabilities.
- **COMPATIBILITY:** crash/admin/development recovery remains technical rather than ordinary in-world Spell mercy; no schema change should be assumed unless explicitly documented.

## Workflow rules

- Never write directly to or merge into `main`; use `gpt/` branches and reviewable PRs.
- Avoid duplicating active PRs; update an existing owner when the same slice is already open.
- Prioritize correctness/persistence blockers before new content.
- After two consecutive no-progress runs on one blocker, record attempts, evidence and the exact resume condition; mark/comment it blocked and move on.
- Do not invent canon. If source access or owner decisions block a lore-sensitive change, do research/design/test work and record the blocker instead.
- Preserve historical findings and supersede them explicitly rather than rewriting history.

## Next recommended slice

Keep green/review-clean #178 as the correctness edge. Let #184 own gameplay/world integration and #183 own generated appraisal. Once #183 is merged/rebased onto a compatible correctness base, integrate the exact generated Aspect/Flaw/Attribute award into Issue #34's completion receipt/replay path with restart/idempotence coverage. Until that condition changes, leave #158 blocked and do not manufacture another persistence schema.
