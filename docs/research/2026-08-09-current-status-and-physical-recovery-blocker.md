# Current recovery evidence and integration blockers — 2026-08-10

## Scope

This note records the current Issue #34 evidence boundary after corrected PR #178 and the newly active generated-appraisal PR #183. It explains why the next correctness step is neither another speculative persistence barrier nor a parallel generated-appraisal receipt schema.

## Repository evidence checked

Current `main`, open issues and pull requests, `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, Issue #34's current discussion, the corrected #178 review/gate state, and active #183/#184 ownership were reviewed.

Current main is `96a3422c2469a832f9a977a4521cc3f3b62edc5b`.

The current crash-recovery edge is PR #178, exact head `a802a68cd1f0c007cef586c38a92683de87a63dc`. Preview Gates #164 / run `31368518381` passed for that exact head. Its only review finding was corrected: before canonical-death recovery authority is consumed, every surviving persisted Nightmare instance must reconstruct through production `NightmareInstance.load(...)`, not merely expose valid ownership UUID fields.

Generated First-Nightmare appraisal is separately owned by PR #183, exact head `7f4a823c0252a563d2c5a5298edc6eb60c2c1f7c`. Preview Gates #172 / run `31375808693` passed after review corrections aligned the existing Kindle and Cold Ash runtime handlers with generated content IDs. #183 persists deterministic generated Aspect/Flaw/Attribute awards on current `main`, but explicitly leaves crash-atomic exactly-once completion to Issue #34's recovery lineage.

Gameplay/world integration is separately owned by PR #184, stacked on #181 and carrying the superseded #182 creature work by ancestry. This correctness/status work does not duplicate those slices.

## Generated-appraisal recovery integration blocker

The next meaningful completion-recovery integration is to ensure the durable completion receipt/replay path carries the exact generated Aspect, Flaw and Attribute award that #183 resolves.

That implementation is not started here because #183 and #178 currently have separate active ancestry. Creating another generator/receipt schema on the correctness branch now would duplicate #183's active data/API decisions and risk guessing at a shape that immediately conflicts when the branches are combined.

### Resume condition

Resume generated-appraisal recovery integration when:

1. #183 is review-stable; and
2. either #183 is merged/rebased onto a compatible Issue #34 correctness ancestry, or the owner chooses an explicit integration base/order.

Then add deterministic restart/idempotence coverage proving recovery replays the same resolved Aspect + Flaw + Attribute identities/provenance without rerunning a version-sensitive generator and without duplicate award.

This is a dependency/ancestry blocker, not evidence that another persistence phase is currently missing on #178.

## Prepared-world durability remains blocked

PR #158's prepared Nightmare world/chunk durability item is unchanged. `SavedDataPersistence.saveAndWait(...)` proves neither Nightmare-dimension chunk/entity durability nor a safe durable rollback policy by itself.

Resume #158 only with its already-recorded new-evidence conditions: a process-free reconstruction of the split/convergence target, live same-world process-kill evidence, stronger Minecraft/NeoForge save-path evidence, owner selection of rebuild-vs-durable-cleanup policy, a dependency/code change, or another credible transaction design. This run did not retry it.

## Hosted dimension-generation stall remains evidence-limited

The frozen-datapack Nightmare-dimension generation/observation stall has repeated while Java jobs were otherwise healthy. It is not the previously corrected Mineflayer transport-watchdog mismatch. Do not blind-rerun or extend timeout budgets again without new server-lag/protocol evidence or a deterministic bounded world-generation change.

## Physical recovery evidence still missing

Issue #34 still calls for real same-world process-kill/restart evidence beyond process-free reconstruction and physical NeoForge smoke. No new physical row is claimed here. The available repository/GitHub environment can inspect code and hosted Actions evidence but does not itself prove fsync/power-loss behavior below observed persisted images.

## Classification

- **CANON:** unchanged; this note changes no lore-facing mechanic.
- **INFERRED:** a resolved appraisal result should be retained exactly for restart recovery rather than rerun against potentially changed generation/catalogue state. This is the same implementation inference already recorded by #183, not a canonical formula.
- **DESIGN:** completion receipts, durability/semantic verification boundaries, exact generated-award replay, deterministic fault/restart tests, provenance checks and technical recovery behavior.
- **UNKNOWN:** physical fsync/power-loss convergence, post-verification storage corruption, the canonical Aspect/Flaw/Attribute determination formula/probabilities, and unexecuted real process-kill rows.
- **COMPATIBILITY:** technical recovery remains separate from normal in-world Spell behavior; older persisted state remains supported unless an explicit migration says otherwise.

## Current recommendation

Keep #178 as the green/review-clean correctness edge. Let #183 and #184 continue to own their active appraisal and gameplay/world slices. Do not add another persistence checkpoint unless losing the last meaningful replay authority has a demonstrated unrecoverable split. When #183 obtains compatible ancestry with the #34 lineage, make exact generated-award receipt/replay the next correctness integration slice.
