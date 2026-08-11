# Login recovery precedence contract

**Date:** 2026-08-11  
**Tracker:** Issue #34 — successful Nightmare appraisal recovery across server restart

## Why this bounded slice

PR #239 established a green two-JVM dedicated-server same-world restart substrate, but a true completion-replay restart still lacks an automated real `ServerPlayer` that can reconnect and exercise the production login event. That stronger harness is therefore blocked under the resume conditions recorded by #239 rather than being replaced by another codec-only test.

A smaller unblocked correctness edge remains in production today: `NightmareEvents.onPlayerLoggedIn(...)` must give a durable successful-completion receipt precedence over ordinary active-instance reconciliation. If that ordering regressed, a restart cut containing both a completion receipt and surviving active ownership could be routed through the ordinary resume/technical-recovery branch before `GeneratedAppraisalRecoveryService.replayPending(...)` had a chance to converge the completed transaction.

## Change

`NightmareLoginRecoveryOrderingTest` pins the existing production routing contract by requiring:

1. `GeneratedAppraisalRecoveryService.replayPending(player)` is consulted in the login handler;
2. a handled replay returns immediately;
3. `NightmareService.activeFor(player)` reconciliation occurs only after that guard.

This is intentionally a narrow source-contract regression test. It does not claim to simulate a live login, and it does not replace the blocked dedicated-server player reconnect harness.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, progression, death, or failure mechanic changes.
- **INFERRED:** unchanged project expectation that an already-resolved appraisal remains the same resolved result across technical restart.
- **DESIGN:** durable completion authority must be offered to the recovery service before ordinary active-instance reconciliation on player login.
- **UNKNOWN:** real reconnect timing, player save scheduling, return-position persistence, process-loss timing, filesystem/fsync guarantees, and storage corruption after verification.
- **COMPATIBILITY:** test/documentation only. Runtime behavior, persistence schemas, catalogues, dependencies, and lore-facing semantics are unchanged.

`docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read. This slice is technical crash/restart recovery infrastructure, not an in-world Spell rule, so it introduces no new primary-novel proposition.

## Stronger proof remains blocked

Resume the real dedicated-server completion-replay test only when there is a credible automated NeoForge client or server-side test-player path that reaches the same `ServerPlayer` login/recovery event across two server JVMs. Do not substitute another codec reconstruction and call it live restart evidence.
