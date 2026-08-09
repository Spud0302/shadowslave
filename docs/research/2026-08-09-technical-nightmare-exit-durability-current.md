# Technical Nightmare exit durability boundary — current lineage — 2026-08-09

## Scope

This note covers technical recovery and administrator abort after the exact return-dimension verification added by PR #128. It ports the audited persistence correction from historical PR #73 onto the current successful-completion and canonical-death lineage.

It does not change successful Nightmare completion, canonical death, compound `preview_reset`, appraisal, progression, scenario content, or any lore-facing Spell mechanic.

## Preconditions checked

- Current main authority documents and open issues/PRs were re-read before implementation.
- PR #128 exact head `b82c9e0ec79f80393a5b68b3a4b18ab28367d1f3` passed Preview Gates run #92 / ID `31289610125` before this slice started.
- Issue #34's successful-completion physical restart matrix remains blocked on interactive real-player execution and is not retried here.
- Historical PR #73 already identified and reviewed the technical/admin post-return crash window; this slice ports that bounded transaction instead of designing a new one.

## Confirmed persistence defect

After PR #128, `NightmareService.exit(...)` proves that the player reached the exact selected return dimension before teardown. However, `technicalRecover(...)` and `adminAbort(...)` still consumed active Nightmare ownership before their recovered Soul/identity state had a durable player save.

A process failure after ownership teardown became durable but before the recovered player state became durable could therefore leave stale Aspirant/identity state with no active Nightmare authority for login recovery.

A second ordering hazard exists when a successful-completion receipt is also retained: clearing that receipt before recording any technical-exit intent can make a restart look like an ordinary active Nightmare again. The chosen technical/admin outcome therefore needs its own durable recovery authority before any competing completion receipt is cleared.

## Corrected durable order

After the return teleport is observed in the exact selected dimension:

1. record the exact active instance plus `TECHNICAL_RECOVERY` or `ADMIN_ABORT` as a pending technical-exit marker;
2. persist the shared Nightmare SavedData through `SavedDataPersistence.saveAndWait(...)` while active ownership and any completion receipt remain;
3. clear the exact successful-completion receipt, if present;
4. persist the registry again while active ownership and the technical-exit marker remain;
5. reset permanent identity and Soul state to the existing Carrier technical-recovery result;
6. synchronously save player data;
7. remove owned Nightmare entities and atomically consume exact active ownership plus technical-exit marker in one registry mutation;
8. persist the registry again through the joined SavedData barrier.

On login, canonical-death recovery remains first. A pending technical exit is then replayed before successful-completion or ordinary active-Nightmare recovery, preserving the already-chosen technical/admin outcome across restart.

The technical-exit marker lives in the same `NightmareRegistryData` SavedData as active ownership and completion receipts. Current fail-closed decode behavior is preserved: malformed, duplicate, mismatched, or unsupported technical-exit metadata blocks Nightmare recovery rather than appearing absent.

## Evidence classification

- **CANON:** unchanged. Technical recovery and administrator abort are not ordinary Nightmare Spell mercy mechanics.
- **INFERRED:** unchanged one-instance recovery authority while a technical/admin exit is incomplete.
- **DESIGN:** the durable technical-exit intent, precedence, persistence order, same-SavedData placement, exact active-instance matching, and replay algorithm are Minecraft recovery design.
- **UNKNOWN:** physical process-kill convergence at these exact durability boundaries; storage-device/power-loss behavior beyond the joined NeoForge I/O worker; exact return position/orientation durability; compound preview-reset crash atomicity.
- **COMPATIBILITY:** successful technical recovery/admin abort still return the player, reset identity/Soul state, remove owned entities, and consume active ownership. Existing saves without `technical_exits` load normally. Successful completion and canonical death retain their existing independent transactions.

No canon rule is invented or generalized by this slice.

## Automated evidence added

`NightmareTechnicalExitCoordinatorTest` simulates restart after:

- the technical-exit intent save;
- the completion-receipt-clear save;
- the recovered-player save;

and requires replay to converge to no technical marker, no completion receipt, recovered player state, and no active ownership. It also requires ownership not to be consumed before intent, receipt clearing, and player reset are in the required state.

`NightmareRegistryTechnicalExitTest` covers:

- marker round-trip alongside exact active ownership and a completion receipt;
- preservation of technical/admin reason;
- ordinary teardown refusing to bypass a pending transaction;
- final ownership consumption refusing to proceed while a completion receipt remains;
- atomic active-ownership + marker consumption;
- reason rewrite rejection;
- malformed technical-exit metadata becoming recovery-blocking under the current registry policy.

## Remaining boundary

`abortForPreviewReset(...)` deliberately stays on its separate compound-reset path. Preview reset changes additional persistent attachments and publishes a final client snapshot, so it must be audited as its own transaction rather than silently inheriting technical/admin semantics.

Real process-kill evidence for this transaction remains required before calling it physically proven.
