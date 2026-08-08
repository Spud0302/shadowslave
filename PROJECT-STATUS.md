# Shadow Slave project status

**Status date:** 2026-08-09  
**Stable main:** `e9f676a9b2ef58cf1e0cc18d45d1420b9e62c2f4`  
**Active correctness candidate:** PR #119 / `gpt/gate-completion-player-recovery-verifier`  
**Correctness candidate head:** `93da9d43df160995097a5108f0e73ef6a5762046`

## Products

| Product | Current state | Public release |
| --- | --- | --- |
| Vanilla datapack | completed reference; one supported First Nightmare at a time | `datapack-v1.0.0` |
| Java main | persistent Soul/lifecycle foundation plus merged procedural identity, Nightmare scenario, Dream Realm region, Memory, Echo and creature content foundations | none |
| Java correctness candidate | restart-recoverable successful-Nightmare completion stack through PR #119; physical same-world restart matrix still outstanding | not a release |
| Nightmare Spell modpack | shared Java-core/content foundations in progress; broad adapter/modpack integration remains future work | none |

## Current main

Current `main` includes the playable Java preview foundation plus later merged shared-core/content work, including:

- explicit multi-ability Aspect identity migration;
- The Drowned Bell authored Nightmare scenario;
- reusable Nightmare Creature catalogue;
- authored Memory and Echo catalogue foundations;
- Dream Realm region catalogue;
- stabilized frozen-datapack trial-lock regression coverage.

The older `0.1.0-preview.2` JAR remains historical preview evidence only. It is not evidence for the newer merged content or the open restart-recovery stack.

## Active correctness stack

Issue #34 tracks the zero-appraisal crash window in successful Nightmare completion. The current review lineage is intentionally stacked and has rebuilt the recovery work on top of current main rather than merging the historical branch wholesale.

The active stack now includes:

1. deterministic successful-completion fault points;
2. replayable completion coordinator phases;
3. durable completion receipt storage with fail-closed reconstruction;
4. idempotent split-save appraisal reconciliation;
5. joined SavedData persistence checkpoints;
6. successful-return observation before commit;
7. server-backed completion operations;
8. live terminal-success and login-recovery routing;
9. six-boundary physical fault runner;
10. stale-evidence, recovery-status and source/world provenance authentication;
11. structured same-player/two-login recovery evidence;
12. hosted CI execution of both completion evidence self-tests.

Exact PR #119 head `93da9d43df160995097a5108f0e73ef6a5762046` passed **Preview Gates run #89 / ID `31280686707`** on 2026-08-09. That run is evidence for hosted compile/unit/package, both completion evidence self-tests, physical NeoForge client/server smoke, and the frozen-datapack gate on that exact stacked head.

## Remaining correctness blocker

Hosted CI does **not** prove Issue #34's required real same-world process-kill/restart behavior.

The next required row is:

```text
after_terminal_registry_save
```

It must be executed with a real player on the disposable completion-fault world, then recovered on the same world/source and followed by a second relog. Retained evidence must prove:

- the intended fault boundary actually halted the Minecraft child;
- recovery ran successfully with the fault disabled;
- exactly one appraisal and exactly one successful-completion teardown occurred;
- source and world provenance match across fault/recovery;
- both recovery logins refer to the same player and Nightmare;
- appraisal is applied, no active Nightmare remains, and the player is outside the Nightmare dimension on both observations.

This GitHub-only automation environment cannot perform the required interactive player actions. Do not replace the missing physical row with another speculative persistence layer. Resume Issue #34 implementation only if the physical row exposes a concrete defect or new owner/runtime evidence changes the recovery boundary.

## Frozen datapack

The datapack remains deliberately limited to one supported First Nightmare at a time. The persistent global lock covers ordinary concurrent/disconnect overlap, while deeper global `ss_creature` selector behavior remains documented in Issue #20 and `testserver/defect_issue20_stray_creature.mjs`.

A transient Issue #20 regression timeout occurred in PR #117 Preview Gates run #88, while the Java job passed. Descendant PR #119 run #89 passed the complete Preview Gates workflow, so that single timeout is not treated as a current Java or datapack blocker.

## Lore boundary

Novel mechanics remain authoritative. Follow `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` before any lore-sensitive change.

For the restart-recovery work:

- **CANON:** unchanged by the technical persistence stack;
- **INFERRED:** an incomplete successful-completion recovery transaction remains associated with its resolved Nightmare until reconciliation completes;
- **DESIGN:** receipts, durability checkpoints, fault injection, login replay, provenance and verification tooling;
- **UNKNOWN:** real process-kill convergence until the physical matrix is executed;
- **COMPATIBILITY:** technical recovery must not be presented as normal in-world Spell mercy.

## Next actions

1. Review/land the stacked Issue #34 correctness lineage in dependency order rather than cherry-picking later heads without prerequisites.
2. Execute `after_terminal_registry_save` as the first real same-world physical recovery row against the #119 lineage.
3. If it passes, execute the remaining five durable-boundary rows with retained evidence.
4. If a row fails, record exact logs/state on Issue #34 and fix only the demonstrated defect.
5. Continue independent content work only where it does not duplicate or obscure the correctness stack.
