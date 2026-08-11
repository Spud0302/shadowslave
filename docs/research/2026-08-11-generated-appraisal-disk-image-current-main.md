# Generated appraisal disk-image restart evidence on current main

**Date:** 2026-08-11  
**Tracker:** Issue #34  
**Supersedes review ancestry:** PR #223 after current-main validation

## Why this slice

PR #223 already developed and reviewed the healthy three-surface fresh-JVM successful-completion recovery proof, but it is now based on substantially stale `main` ancestry. Current `main` has since gained the dedicated two-JVM same-world server restart gate and the merged login-recovery precedence contract from PR #246.

Rather than merge stale ancestry or duplicate a new recovery design, this branch ports exactly the bounded #223 evidence test onto current `main` and asks current CI to prove it still integrates with the evolved Java core.

## What is exercised

`GeneratedAppraisalCompletionDiskImageRestartTest` materializes three compressed persistence surfaces:

1. the successful-completion receipt registry;
2. the active Nightmare registry containing the exact matching instance;
3. the player attachment image containing Soul, permanent identity, Attributes, Memories, and Echoes.

A first fresh JVM production-decodes those files, verifies the receipt, exercises the production `GeneratedAppraisalRecoveryService.activeInstanceForReplay(...)` selector, runs the generated-appraisal recovery planner, writes the exact converged player state, removes active ownership, and consumes completion authority.

A second fresh JVM then verifies the exact permanent player award and Dreamer state while requiring both Nightmare ownership and completion authority to be absent.

The immutable receipt oracle exists only as test expectation data so the mutable receipt file can be consumed correctly.

## Relationship to existing work

This does not replace or broaden PR #223's claim. It ports that already-developed evidence onto current `main` after the repository moved substantially beyond #223's base.

PR #246 is now merged and pins the production login ordering contract: durable completion replay is offered before ordinary active-instance reconciliation and successful replay returns immediately.

The stronger live-player restart proof remains blocked because the repository still lacks a credible automated NeoForge client or server-side test-player path that reconnects a real `ServerPlayer` across two dedicated-server JVMs and reaches production login recovery. This test deliberately does not pretend to provide that proof.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, progression, reward, death, or failure rule changes.
- **INFERRED:** an already-resolved appraisal should retain its exact resolved result through technical restart rather than regenerate from later catalogue/generator state.
- **DESIGN:** multi-surface compressed-NBT reconstruction and exact recovery verification across fresh JVMs are technical persistence evidence.
- **UNKNOWN:** real `ServerPlayer` reconnect timing, waking-position durability through process loss, server save scheduling, filesystem/fsync guarantees below readable file images, and later storage corruption.
- **COMPATIBILITY:** test/documentation only; runtime behavior, persistence schema, catalogue, gameplay semantics, and lore-facing mechanics remain unchanged.

`docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read before this port. Restart/crash handling remains technical recovery infrastructure, so no new primary-novel proposition is introduced and no new canon rule is inferred from convenience.

## Limits

This remains below a physical NeoForge completion replay. The fresh JVM directly applies the production recovery planner's target to a compressed player-data image instead of invoking `GeneratedAppraisalRecoveryService.replayPending(ServerPlayer)` inside a running server.

It therefore does not prove live teleport persistence, actual login-event timing, real player save timing, sudden-power-loss durability, or post-verification corruption resistance.

## Next step

If this current-main port passes exact-head CI and review, close #223 as superseded by the current-main evidence PR. Then port/validate the complementary same-player/different-instance contradictory persisted-cut proof from #228 onto the new current-main evidence base rather than preserving its stale branch stack.

Resume a real two-JVM `ServerPlayer` completion-replay harness only when new client/test-player infrastructure, a dependency/framework change, owner input, or another credible approach satisfies the recorded blocker condition.
