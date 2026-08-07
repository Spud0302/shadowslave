# Technical Nightmare exit durability boundary — 2026-08-08

## Scope

This note covers `NightmareService.technicalRecover(...)` and `adminAbort(...)` after the return teleport has been verified. It does not change successful Nightmare completion, canonical death, preview reset, lore progression, or scenario content.

## Confirmed defect

Before this slice, technical/admin exit persisted the returned player location, removed active Nightmare ownership, persisted the registry, and only afterward cleared any retained completion receipt and reset/persisted Soul and identity state.

A process failure after ownership teardown became durable but before the later player reset became durable could therefore leave a returned player with stale Aspirant/identity state and no active Nightmare record for login recovery to replay.

If a retained successful-completion receipt existed, teardown could also become durable before that receipt was cleared, allowing a later login to enter successful-completion recovery even though the technical/admin exit had already consumed active ownership.

## Corrected durable order

After the #72 return-dimension verification succeeds, technical recovery and administrator abort now commit in this order:

1. clear the exact retained successful-completion receipt, if present;
2. synchronously persist Nightmare registry state while active ownership is still retained;
3. reset permanent player identity to empty and Soul state to Carrier recovery state;
4. synchronously persist player data while active ownership is still retained;
5. tear down the exact active Nightmare instance;
6. synchronously persist the registry again.

A crash after step 2 leaves active ownership, so login can replay technical recovery without a completion receipt taking precedence. A crash after step 4 leaves the already-reset player plus active ownership, so replay is idempotent and can finish teardown.

## Evidence classification

- **CANON:** unchanged. No novel mechanic or progression rule changes.
- **INFERRED:** unchanged one-instance ownership of technical Nightmare recovery state while recovery is incomplete.
- **DESIGN:** technical/admin abort commits player recovery before consuming the final active recovery handle; completion-receipt deletion becomes durable before player reset so successful-completion recovery cannot supersede an already-committed technical abort.
- **UNKNOWN:** physical process-failure behavior at these exact server save boundaries remains unproven until dedicated-server fault injection is run.
- **COMPATIBILITY:** ordinary verified technical recovery and administrator abort still return the player, clear technical completion state, reset identity/Soul state, and tear down the owned Nightmare. Preview reset remains deliberately separate and is not claimed fixed by this slice.

No canon rule is invented.

## Automated evidence

`NightmareTechnicalExitCoordinatorTest` simulates process loss after the receipt-clear registry save and after the recovered-player save, reconstructs volatile state from durable state, and proves replay reaches a final state with no completion receipt, recovered player state, and no active ownership. It also asserts ownership is never consumed before both receipt clearing and player reset are visible to the coordinator.

## Remaining boundary

`PreviewResetService.reset(...)` still performs compound preview teardown/reset through a separate path. Its crash ordering should be audited independently rather than silently inheriting this technical/admin policy, because it resets additional attachments and intentionally emits one final client snapshot.
