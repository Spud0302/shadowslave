# Datapack harness keepalive budget — current main

**Date:** 2026-08-11  
**Tracks:** persistence validation for #34 and exact-head validation of #259  
**Base:** `main@ab16d39e60bd57fd2e7916f06559bd02e85d0b03`

## Why this slice resumed

PR #259 exact head `3779d9d6ef769f9406e5ed445a50aae6868d58e6` received Preview Gates #279 / run `31476006233`.

The complete Java job passed, including compilation/all JUnit tests/package, physical NeoForge client boot, the same-world two-JVM dedicated-server restart smoke, and development-JAR upload. The frozen datapack also built and validated successfully.

The only failing gate was the deployed vanilla harness. The server reported severe temporary lag while processing Nightmare dimension work, including a `41470ms / 829 ticks behind` warning. The lifecycle harness continued making successful authoritative observations, but minecraft-protocol disconnected the Mineflayer client at its default 30000ms keepalive timeout before the harness's existing 60000ms dimension-observation deadline had expired.

This is the same transport/assertion budget mismatch previously investigated in stale PR #170. Because #259 supplied fresh exact-head evidence of the same failure class, resuming that CI problem now has new evidence and is not another blind retry.

## Prior approach and review finding

Stale PR #170 introduced a 90000ms test-only `checkTimeoutInterval` through Node `--import` preloads and passed its exact-head Preview Gates. Review then found a real P2 gap: the repository also documents direct `node harness.mjs` invocation, which bypassed the package-script preload and therefore retained the 30000ms watchdog.

This current-main port keeps the validated budget but corrects that integration boundary rather than copying #170 unchanged.

## Current-main design

- `harness_transport.mjs` raises Mineflayer `checkTimeoutInterval` to a minimum of 90000ms while preserving explicitly longer caller values.
- The existing authoritative gameplay observation limit remains 60000ms. A server that never reaches the expected state still fails closed on the original assertion deadline.
- `dimension_wait.mjs` imports the transport setup, so direct `node harness.mjs` and direct `node regression_issue20.mjs` both load the transport contract before their module bodies create bots.
- `deploy.mjs` imports the transport setup explicitly because it is separately documented as a direct entry point.
- the Flaw npm paths preload the same setup; their gameplay assertions and command deadlines are unchanged.
- pure Node tests pin the 90s > 60s invariant, option preservation, and the direct-entrypoint load paths.

The transport wrapper is idempotent via a global symbol marker because multiple harness modules may reach it through shared imports.

## Evidence classification

- **CANON:** not applicable. No Shadow Slave mechanic changes.
- **INFERRED:** none.
- **DESIGN:** a test transport watchdog must outlive the longest intentional authoritative observation window so transport does not pre-empt the assertion that owns pass/fail semantics.
- **UNKNOWN:** whether every extreme hosted-runner stall recovers within 90000ms; if command/state progress remains unhealthy, existing harness deadlines continue to fail closed.
- **COMPATIBILITY:** Java core, frozen-datapack behavior, Nightmare state, multiplayer contract, persistence schema, gameplay timing, lore semantics and release assertions are unchanged. This is test infrastructure only.

## Lore/source boundary

`docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` were re-read. This slice changes no lore-sensitive system and introduces no novel proposition, so no primary-novel mechanic claim is needed. Restart/crash handling remains technical infrastructure rather than an in-world Spell mercy rule.

## Retry / blocker rule

Do not rerun #259 unchanged merely hoping for a faster hosted runner. Its Java/persistence evidence is already green and its datapack failure is diagnosed as the recurring shorter transport watchdog.

The condition to resume full #259 validation is a merged or otherwise exact-head-tested transport correction, a runner/dependency change, new diagnostics contradicting this diagnosis, or another credible technical approach. If the 90000ms watchdog itself later expires while the server subsequently demonstrates healthy progress, record the lag interval and protocol evidence before changing the budget again; do not repeatedly increase timeouts without evidence.

## Remaining limits

This does not provide the stronger Issue #34 proof of a real `ServerPlayer` completing and reconnecting across two dedicated NeoForge JVMs. That remains blocked until credible automated client/test-player infrastructure, framework support, owner input, or another sound production-login approach appears.
