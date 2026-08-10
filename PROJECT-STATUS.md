# Shadow Slave project status

**Status date:** 2026-08-10  
**Stable main:** `96a3422c2469a832f9a977a4521cc3f3b62edc5b`  
**Active correctness edge:** PR #178 / `gpt/verify-canonical-death-ownership-teardown-persistence`  
**Correctness head:** `a802a68cd1f0c007cef586c38a92683de87a63dc`

## Current state

`main` contains the shared Java/core foundations plus merged Nightmare role/scenario compatibility work. Active gameplay integration is separately owned by PRs #179, #181 and #182; do not duplicate that stack from correctness work.

Issue #34 remains the primary persistence/restart tracker. The active correctness lineage now covers replayable successful completion plus strengthened first-authority and final-authority persistence boundaries for canonical death, technical/admin exit and preview reset.

The exact corrected PR #178 head `a802a68cd1f0c007cef586c38a92683de87a63dc` passed **Preview Gates run #164 / ID `31368518381`**. Its only inline review finding was corrected by requiring every persisted unrelated Nightmare instance to decode through the production `NightmareInstance.load(...)` path before canonical-death recovery authority can be consumed; that review thread is resolved.

## Remaining blockers and evidence gaps

Prepared Nightmare world/chunk durability remains **BLOCKED** under PR #158 / Issue #34. Resume only with new reconstruction or live process-kill evidence, stronger Minecraft/NeoForge save-path evidence, an owner choice of convergence policy, a dependency/code change, or another credible transaction design. Do not retry it automatically without one of those conditions.

Real process-kill/restart convergence remains stronger evidence than hosted smoke/CI. The persistence verifiers prove readable recovery authority and fail-closed ordering, not physical fsync guarantees below the observed file images.

The repeatedly observed hosted frozen-datapack Nightmare-dimension generation stall is also not a reason for blind timeout increases or reruns; collect new lag/protocol evidence before changing that budget again.

## Lore/evidence boundary

Follow `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` before lore-sensitive work.

- **CANON:** no new lore proposition is introduced by the current persistence lineage.
- **INFERRED:** none added by the persistence verifiers.
- **DESIGN:** recovery receipts/markers, semantic persisted-state verification, fail-closed ordering, provenance/fault tooling and technical/admin recovery behavior.
- **UNKNOWN:** physical power-loss/fsync guarantees, post-verification storage corruption, and unexecuted real process-kill rows.
- **COMPATIBILITY:** crash/admin/development recovery remains technical and must not masquerade as ordinary in-world Spell mercy; valid older persisted state remains readable unless a documented schema migration says otherwise.

## Next actions

1. Finish review/merge evaluation of green PR #178 before stacking another canonical-death persistence edge.
2. Audit only transactions where loss of the **last meaningful replay authority** can demonstrably create an unrecoverable persisted split; do not add checkpoints mechanically after every save.
3. Keep PR #158 blocked until its recorded resume condition changes.
4. Let #179/#181/#182 own their current role/scenario/physical-creature gameplay integration and avoid duplicate content work.
5. Update `GPT_HANDOFF.md` and `ISSUES.md` in this same documentation PR as the correctness/gameplay baseline advances; do not open a competing status-sync PR.
