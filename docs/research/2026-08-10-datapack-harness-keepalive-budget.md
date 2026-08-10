# Datapack harness keepalive budget

**Date:** 2026-08-10  
**Scope:** frozen-datapack CI transport only; no gameplay or lore mechanics

## Observed failure

Preview Gates #137 for PR #168 passed the complete Java job, including compilation/JUnit/package, both recovery verifier self-tests, physical NeoForge client boot, dedicated server boot, and JAR upload. The separate deployed vanilla datapack harness failed after the server reported substantial tick lag while entering/leaving the Nightmare dimension. Mineflayer then terminated the client at the minecraft-protocol default 30-second keepalive watchdog before the harness's own 60-second authoritative dimension-observation budget had expired.

This failure class has also appeared on unrelated Java correctness heads, so it can obscure otherwise-reviewable changes without demonstrating a datapack regression.

## Primary implementation evidence

Mineflayer's upstream FAQ recommends increasing `checkTimeoutInterval` when server lag causes disconnects. Mineflayer and node-minecraft-protocol API documentation describe the client default as 30 seconds.

The repository lifecycle harness already gives world/dimension transitions up to 60 seconds because fresh hosted worlds can block command processing while generating the destination. A transport watchdog shorter than that observation contract is internally inconsistent: the protocol layer can abort before the test layer decides whether the game state was observed.

## Design decision

- **DESIGN:** release-gate Mineflayer clients use a 90-second minimum `checkTimeoutInterval`.
- The budget is applied through a test-only preload module, so datapack functions, Minecraft tick timing, scenario timing, and assertions are unchanged.
- Explicit client watchdogs longer than 90 seconds are preserved rather than shortened.
- A pure Node test pins the invariant that the transport watchdog remains longer than the harness's 60-second maximum observation window.

## Evidence labels

- **CANON:** not applicable; no Shadow Slave mechanic changes.
- **INFERRED:** none.
- **DESIGN:** test transport watchdog must exceed the longest intentional authoritative observation budget.
- **UNKNOWN:** whether every hosted stall under severe runner contention will recover within 90 seconds; if the server or command path remains unhealthy, existing harness timeouts still fail closed.
- **COMPATIBILITY:** frozen datapack behavior, Java core behavior, release assertions, one-slot multiplayer contract, and lore semantics are unchanged.

## Limits

This does not make a hung server pass. Command/query deadlines and the 60-second dimension transition deadline remain unchanged. It only prevents minecraft-protocol's shorter default watchdog from pre-empting those assertions during temporary server-thread stalls.

If 90-second keepalive failures recur while the server later demonstrates healthy command progress, collect the server lag interval and protocol log before increasing the budget again. Do not repeatedly raise it without evidence.
