# Technical Nightmare exit durability boundary — 2026-08-08

## Scope

This note covers `NightmareService.technicalRecover(...)` and `adminAbort(...)` after the return teleport has been verified. It does not change successful Nightmare completion, canonical death, preview reset, lore progression, or scenario content.

## Confirmed defect

Before this slice, technical/admin exit persisted the returned player location, removed active Nightmare ownership, persisted the registry, and only afterward cleared any retained completion receipt and reset/persisted Soul and identity state.

A process failure after ownership teardown became durable but before the later player reset became durable could therefore leave a returned player with stale Aspirant/identity state and no active Nightmare record for login recovery to replay.

The first implementation of this slice still had a second crash hole found in Codex review: it cleared and saved the successful-completion receipt before the player reset was durable, but did not persist any technical-exit intent. If the process stopped after that registry save, the player could restart inside the Nightmare with active ownership and no completion receipt. Login would then treat the state as an ordinary restored Nightmare instead of replaying the technical exit.

## Corrected durable order

After the #72 return-dimension verification succeeds, technical recovery and administrator abort now commit in this order:

1. record the exact active instance plus technical/admin exit reason as a durable technical-exit marker;
2. synchronously persist Nightmare registry state while active ownership and any completion receipt are still retained;
3. clear the exact retained successful-completion receipt, if present;
4. synchronously persist the registry again while active ownership and the technical-exit marker remain;
5. reset permanent player identity to empty and Soul state to Carrier recovery state;
6. synchronously persist player data while active ownership and the technical-exit marker remain;
7. remove owned Nightmare entities and atomically consume the exact active ownership plus technical-exit marker in the registry;
8. synchronously persist the registry again.

`NightmareEvents.onPlayerLoggedIn(...)` checks the technical-exit marker before successful-completion recovery. Therefore a restart after step 2 replays the already-chosen technical/admin exit instead of allowing the still-present success receipt to win, and a restart after step 4 cannot be mistaken for an ordinary restored Nightmare after the receipt has been cleared.

The marker is stored inside the same `NightmareRegistryData` SavedData file as active ownership and successful-completion receipts. This avoids creating a second independently saved recovery file whose write order could itself split marker and ownership state.

## Evidence classification

- **CANON:** unchanged. No novel mechanic or progression rule changes.
- **INFERRED:** unchanged one-instance ownership of technical Nightmare recovery state while recovery is incomplete.
- **DESIGN:** a persisted technical/admin exit intent takes precedence over competing completion recovery until recovered player state is durable and exact active ownership is consumed; marker and active ownership are removed together from the same registry state.
- **UNKNOWN:** physical process-failure behavior at these exact server save boundaries remains unproven until dedicated-server fault injection is run. The verified return teleport itself is not claimed durable until player data is saved; the technical marker exists to make later recovery deterministic once registry persistence begins.
- **COMPATIBILITY:** ordinary verified technical recovery and administrator abort still return the player, clear technical completion state, reset identity/Soul state, and tear down the owned Nightmare. Existing saves have no `technical_exits` list and load with no pending marker. Preview reset remains deliberately separate and is not claimed fixed by this slice.

No canon rule is invented.

## Automated evidence

`NightmareTechnicalExitCoordinatorTest` simulates process loss after the technical-intent registry save, after the completion-receipt-clear registry save, and after the recovered-player save. Each restart reconstructs volatile state from durable state and proves replay converges to no completion receipt, recovered player state, no active ownership, and no retained technical-exit marker.

`NightmareRegistryTechnicalExitTest` proves that a pending marker round-trips with exact active ownership and a retained completion receipt, preserves the technical/admin reason, prevents ordinary teardown from bypassing the transaction, refuses final ownership consumption until the completion receipt is gone, removes ownership and marker together, and rejects rewriting the pending reason.

## Remaining boundary

`PreviewResetService.reset(...)` still performs compound preview teardown/reset through a separate path. Its crash ordering should be audited independently rather than silently inheriting this technical/admin policy, because it resets additional attachments and intentionally emits one final client snapshot.
