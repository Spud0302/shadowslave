# Shadow Slave — current issues and limitations

**Canonical project status:** `PROJECT-STATUS.md`  
**Current main:** `e9f676a9b2ef58cf1e0cc18d45d1420b9e62c2f4`  
**Primary correctness candidate:** PR #119 / head `93da9d43df160995097a5108f0e73ef6a5762046`

This file tracks current blockers and evidence gaps. Historical preview findings remain preserved under `docs/history/`, review notes, Git history and GitHub issues.

## Issue #34 — successful Nightmare completion restart recovery

This is the highest-priority Java persistence blocker.

The active stacked candidate through PR #119 now contains:

- a durable successful-completion receipt and ordered phases;
- fail-closed registry reconstruction/invariants;
- idempotent split-save appraisal reconciliation;
- joined SavedData durability checkpoints;
- a successful-return observation guard;
- server-backed completion operations;
- live terminal-success and login-recovery routing;
- deterministic process halts at six post-durability boundaries;
- evidence freshness and recovery-status checks;
- same-source/same-world provenance authentication;
- structured same-player recovery evidence across reconnect plus second relog;
- hosted self-tests for both completion evidence verifiers.

Exact PR #119 head `93da9d43df160995097a5108f0e73ef6a5762046` passed Preview Gates run #89 / ID `31280686707`.

### Still unproven

No real same-world process-kill/restart row has yet been accepted as Issue #34 evidence. Hosted compile/tests and ordinary physical client/server boot are not substitutes.

The first required physical row is `after_terminal_registry_save`. It must prove exactly-once appraisal/teardown and converged player state after restart and second relog, using the retained provenance/authentication outputs on the #119 lineage.

The current GitHub-only automation environment cannot perform the required interactive player actions. Do not infer success and do not add more speculative recovery state. Resume implementation when a physical row exposes a defect, owner/local execution supplies new evidence, or a new dependency/runtime behavior provides a credible approach.

## Issue #20 — frozen datapack global-selector ceiling

The supported frozen-datapack contract remains one active First Nightmare at a time, protected by the persistent global trial lock.

The deeper command-era architecture still uses global `@e[tag=ss_creature]` selectors inside the single Nightmare dimension. `testserver/defect_issue20_stray_creature.mjs` deliberately demonstrates that an unrelated entity carrying the prototype tag can influence the global objective. True per-player entity ownership belongs to Java and is not claimed for the frozen datapack.

A transient dimension-transition timeout occurred in PR #117 Preview Gates run #88. The Java job passed, and descendant PR #119 run #89 passed the complete workflow. Treat the earlier timeout as transient unless it recurs with fresh evidence; do not repeatedly rerun or redesign the harness based on that single failure.

## Current open evidence gaps

1. Issue #34's six real same-world process-kill/restart rows.
2. Complete interactive Java playthrough of the current merged/main plus selected correctness candidate.
3. Real Java logout/login persistence at Carrier, active Nightmare and completed Dreamer stages outside the synthetic recovery tests.
4. Two-player simultaneous Java Nightmare-instance verification.
5. Backed-up real frozen-datapack world migration plus idempotent second invocation.
6. Player-facing signal-fire completion/return-position verification.
7. Ordinary Nightmare death cleanup and wording verification.
8. Review/merge of the large stacked correctness lineage in dependency order.

Use `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` and the Issue #34 research notes/runner documentation for exact procedures.

## Known Java/content limitations

- natural infection/exhaustion is not yet the ordinary playable entry path;
- The Last Signal remains a development-facing authored scenario and its fixed preview appraisal is **DESIGN**;
- later merged content foundations are not equivalent to fully integrated runtime systems;
- procedural/content catalogues do not imply canonical generation formulas;
- Memories, Echoes, Dream Realm regions, creatures and additional scenarios remain bounded Java-owned content foundations until runtime ownership/execution is explicitly integrated;
- later Seeds, mature shared-resolution multiplayer, natural awakening progression, full modpack adapters and a public Java release remain future scope.

## Lore risks

- Follow `docs/LORE-SOURCE-POLICY.md` for every lore-sensitive change.
- Keep **CANON**, **INFERRED**, **DESIGN**, **UNKNOWN**, and compatibility consequences explicit.
- Do not generalize The Last Signal's campfire into a universal Nightmare completion rule.
- Do not turn project catalogues, weights, matchers or recovery algorithms into claimed Spell formulas.
- Technical crash/admin recovery is implementation **DESIGN**, not ordinary in-world Spell mercy.
- Later Seed behavior requires renewed primary-novel verification under `docs/NIGHTMARE-SEED-ROADMAP.md`.

## Reporting

Record defects with exact branch/commit, reproduction steps, expected versus observed behavior, logs or screenshots, and whether the issue is correctness, persistence, presentation, balance, lore wording or missing scope.

When repeated work on one blocker makes no progress twice, record both attempts and the exact condition needed to resume, mark/comment the item blocked, and move to the next unblocked slice instead of looping on the same evidence gap.
