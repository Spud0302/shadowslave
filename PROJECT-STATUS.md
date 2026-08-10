# Shadow Slave project status

**Status date:** 2026-08-10  
**Stable main:** `96a3422c2469a832f9a977a4521cc3f3b62edc5b`  
**Active correctness edge:** PR #178 / `gpt/verify-canonical-death-ownership-teardown-persistence`  
**Correctness head:** `a802a68cd1f0c007cef586c38a92683de87a63dc`

## Current state

`main` contains the shared Java/core foundations plus merged Nightmare role/scenario compatibility work. Active gameplay/world integration is now consolidated in PR #184, stacked on #181 and carrying the superseded #182 physical-creature work by ancestry. Generated First-Nightmare appraisal awards are separately owned by PR #183. Do not duplicate either stack from correctness work.

Issue #34 remains the primary persistence/restart tracker. The active correctness lineage covers replayable successful completion plus strengthened first-authority and final-authority persistence boundaries for canonical death, technical/admin exit and preview reset.

The exact corrected PR #178 head `a802a68cd1f0c007cef586c38a92683de87a63dc` passed **Preview Gates run #164 / ID `31368518381`**. Its only inline review finding was corrected by requiring every persisted unrelated Nightmare instance to decode through the production `NightmareInstance.load(...)` path before canonical-death recovery authority can be consumed; that review thread is resolved.

PR #183 exact head `7f4a823c0252a563d2c5a5298edc6eb60c2c1f7c` passed **Preview Gates #172 / run `31375808693`** after both runtime-ID review findings were corrected. It owns deterministic generated Aspect/Flaw/Attribute appraisal persistence on current `main`, but explicitly does **not** claim crash-atomic exactly-once completion. The completion-receipt/recovery lineage must incorporate the exact generated award before an integrated candidate can make that claim.

## Correctness integration boundary

Do not create a parallel generated-appraisal recovery transaction while #183 and #178 remain separate active branches. The next correctness integration becomes actionable when #183 is review-stable and either:

- it is merged/rebased so the generated award API/data are available on the completion-recovery lineage; or
- the owner chooses an explicit integration base/order for combining #183 with the #178/#34 lineage.

At that point the completion receipt/replay path must retain and replay the exact generated Aspect, Flaw and Attribute identities/provenance rather than rerunning a version-sensitive generator after restart. Until then, treat this as an integration dependency, not a reason to guess at a second schema.

## Remaining blockers and evidence gaps

Prepared Nightmare world/chunk durability remains **BLOCKED** under PR #158 / Issue #34. Resume only with new reconstruction or live process-kill evidence, stronger Minecraft/NeoForge save-path evidence, an owner choice of convergence policy, a dependency/code change, or another credible transaction design. Do not retry it automatically without one of those conditions.

Real process-kill/restart convergence remains stronger evidence than hosted smoke/CI. The persistence verifiers prove readable recovery authority and fail-closed ordering, not physical fsync guarantees below the observed file images.

The repeatedly observed hosted frozen-datapack Nightmare-dimension generation stall is also not a reason for blind timeout increases or reruns; collect new lag/protocol evidence before changing that budget again.

## Lore/evidence boundary

Follow `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` before lore-sensitive work.

- **CANON:** no new lore proposition is introduced by the current persistence/status work.
- **INFERRED:** when an appraisal result has already been resolved, restart-safe persistence should retain that exact identity rather than silently resolving a potentially different result later; this matches #183's recorded inference and does not assert a canonical generation formula.
- **DESIGN:** recovery receipts/markers, semantic persisted-state verification, fail-closed ordering, provenance/fault tooling, exact generated-award replay, and technical/admin recovery behavior.
- **UNKNOWN:** physical power-loss/fsync guarantees, post-verification storage corruption, unexecuted real process-kill rows, and the canonical Aspect/Flaw/Attribute determination formula/probabilities.
- **COMPATIBILITY:** crash/admin/development recovery remains technical and must not masquerade as ordinary in-world Spell mercy; valid older persisted state remains readable unless a documented schema migration says otherwise.

## Next actions

1. Keep green/review-clean PR #178 as the current correctness edge; do not extend it mechanically with more persistence barriers without a last-authority failure model.
2. Let #184 own the Drowned Bell/physical-creature gameplay stack and #183 own generated appraisal awards; do not duplicate them.
3. When #183 is merged/rebased onto a compatible correctness base, integrate its exact generated award into Issue #34's completion receipt/replay path and add restart/idempotence coverage for Aspect + Flaw + Attribute together.
4. Keep PR #158 blocked until its recorded resume condition changes.
5. Do not blind-rerun or further extend the hosted dimension-generation timeout without new lag/protocol evidence.
6. Update `GPT_HANDOFF.md` and `ISSUES.md` in this same documentation PR as the correctness/gameplay baseline advances; do not open a competing status-sync PR.
