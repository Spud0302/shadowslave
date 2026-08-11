# Login recovery precedence contract

**Date:** 2026-08-11  
**Tracker:** Issue #34 — successful Nightmare appraisal recovery across server restart

## Why this bounded slice

PR #239 established a green two-JVM dedicated-server same-world restart substrate, but a true completion-replay restart still lacks an automated real `ServerPlayer` that can reconnect and exercise the production login event. That stronger harness is therefore blocked under the resume conditions recorded by #239 rather than being replaced by another codec-only test.

A smaller unblocked correctness edge remains in production today: `NightmareEvents.onPlayerLoggedIn(...)` must give a durable successful-completion receipt precedence over ordinary active-instance reconciliation. If that ordering regressed, a restart cut containing both a completion receipt and surviving active ownership could be routed through the ordinary resume/technical-recovery branch before `GeneratedAppraisalRecoveryService.replayPending(...)` had a chance to converge the completed transaction.

## Change

`NightmareLoginRecoveryOrderingTest` pins the existing production routing contract by requiring:

1. `GeneratedAppraisalRecoveryService.replayPending(player)` is consulted in the login handler;
2. the replay guard itself contains an unconditional immediate `return;`;
3. `NightmareService.activeFor(player)` reconciliation occurs only after that guard closes.

This is intentionally a narrow source-contract regression test. It does not claim to simulate a live login, and it does not replace the blocked dedicated-server player reconnect harness.

## Review corrections

The initial #240 version searched the entire `NightmareEvents.java` source file. Review correctly found that matching replay/return text elsewhere could let the test remain green even if `onPlayerLoggedIn(...)` itself regressed.

The first current-main port corrected that by extracting only the balanced-brace body of `onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent)`. A second review on #246 found a remaining P2: searching for the first `return;` anywhere after the replay condition still did not prove that the return belonged to the replay guard. A future unrelated early return before `activeFor(...)` could have allowed the source-contract test to pass while a handled completion replay fell through.

The corrected #246 test now extracts the replay guard's own balanced-brace body, requires that body to be exactly an unconditional `return;` modulo whitespace, and requires ordinary active-instance reconciliation to occur only after the guard's closing brace. This remains deliberately lightweight source evidence, but it now matches the advertised precedence invariant rather than a nearby textual coincidence.

## Validation evidence

PR #246 head `086b318b913db154641573464103b3c4fcc69617` ran Preview Gates #263 / Actions run `31464102017` before the second review correction:

- **Java job: PASS** — trigger-contract validation, JDK 21/wrapper setup, compile/all unit tests/package, physical NeoForge client boot, the two-JVM same-world dedicated-server restart smoke, and development JAR upload all passed.
- **Frozen datapack build/validation: PASS.**
- **Deployed datapack harness: NOT REACHED.** The vanilla server did not reach ready state inside the 180-second startup budget; the log was still unpacking libraries and ended at `Preparing level "world"`. No lifecycle/Mineflayer assertion ran.

That startup timeout is not evidence of a Java recovery regression and is not being blindly rerun. The code correction above creates a new exact-head synchronization event, which is the next legitimate validation attempt. If the same vanilla-ready timeout repeats without new diagnostics, record it as a repeated infrastructure blocker and stop automatic retries until the runner/server startup path changes or new evidence appears.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, progression, death, or failure mechanic changes.
- **INFERRED:** unchanged project expectation that an already-resolved appraisal remains the same resolved result across technical restart.
- **DESIGN:** durable completion authority must be offered to the recovery service before ordinary active-instance reconciliation on player login.
- **UNKNOWN:** real reconnect timing, player save scheduling, return-position persistence, process-loss timing, filesystem/fsync guarantees, and storage corruption after verification.
- **COMPATIBILITY:** test/documentation only. Runtime behavior, persistence schemas, catalogues, dependencies, and lore-facing semantics are unchanged.

`docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read. This slice is technical crash/restart recovery infrastructure, not an in-world Spell rule, so it introduces no new primary-novel proposition.

## Stronger proof remains blocked

Resume the real dedicated-server completion-replay test only when there is a credible automated NeoForge client or server-side test-player path that reaches the same `ServerPlayer` login/recovery event across two server JVMs. Do not substitute another codec reconstruction and call it live restart evidence.
