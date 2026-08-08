# Current recovery evidence and physical blocker — 2026-08-09

## Scope

This note records the evidence boundary after PR #119 and explains why the next Issue #34 step is not another speculative persistence implementation.

## Repository evidence checked

Before this update, current `main`, open issues and pull requests, `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and the current Issue #34 discussion were reviewed.

Current main is `e9f676a9b2ef58cf1e0cc18d45d1420b9e62c2f4`.

The active successful-completion correctness lineage reaches PR #119, exact head `93da9d43df160995097a5108f0e73ef6a5762046`.

GitHub Actions Preview Gates run #89 / ID `31280686707` completed successfully for that exact head. It therefore supplies hosted evidence for the checks configured by that head, including the completion-fault runner self-test and completion player-recovery verifier self-test as well as the normal Java/datapack gates.

## What remains missing

Issue #34 requires real same-world process-kill/restart evidence. The first row remains `after_terminal_registry_save`.

The current GitHub connector environment can inspect and modify the repository and inspect Actions evidence, but it cannot control a real Minecraft player through the interactive Nightmare completion/reconnect/relog sequence. No physical row was therefore executed in this run, and no physical success is claimed.

This is an execution blocker, not evidence of a new transaction defect.

## Resume condition

Resume Issue #34's physical row when at least one of the following becomes available:

1. owner/local execution supplies retained row evidence from the #119 lineage;
2. an environment becomes available that can control the required real player interactions and same-world restart;
3. new runtime/dependency evidence demonstrates a concrete defect before the row can begin;
4. an owner decision changes the physical acceptance procedure.

Until then, do not keep adding persistence phases or evidence wrappers merely because the physical row has not been run.

## Classification

- **CANON:** unchanged; this note changes no lore-facing mechanic.
- **INFERRED:** unchanged association between an incomplete successful-completion transaction and its resolved Nightmare.
- **DESIGN:** completion receipts, durability boundaries, deterministic process faults, provenance checks and two-login recovery evidence are technical recovery/test infrastructure.
- **UNKNOWN:** real process-kill/restart convergence remains unknown until the physical matrix runs.
- **COMPATIBILITY:** technical recovery remains separate from normal in-world Spell behavior and must not be presented as canonical mercy.

## Documentation correction

The former root status/handoff/issues documents still described the August 1 preview as the active state despite substantial merged content and the open Issue #34 correctness stack. This branch updates those three authority-facing entry points so future contributors do not restart obsolete work or mistake old preview evidence for current-main/current-candidate evidence.
