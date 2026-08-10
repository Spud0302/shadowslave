# Generated appraisal player persistence verification

**Date:** 2026-08-11  
**Scope:** successful First-Nightmare appraisal recovery / Issue #34  
**Parent:** PR #201 durable generated-appraisal receipt + login replay

## Parent evidence

PR #201 exact head `9c209e0de5392d879c192d481641a009cb66bde1` passed Preview Gates #205 / Actions run `31402364266`. This note does not reuse that pass as evidence for the additional persisted-player verifier or the review corrections below; the successor head requires its own complete gate.

## Problem

PR #201 orders both normal successful completion and login replay as player mutation -> `PlayerList.saveAll()` -> completion-receipt deletion. That is materially safer than deleting the receipt before the player save, but successful return from the save call does not itself prove that the exact expected player attachment image is readable from `playerdata/<uuid>.dat`.

If a stale, partial or malformed player image were followed by durable completion-receipt deletion, restart could lose the final independent authority needed to reconstruct the exact generated award.

## Change

Add a shared semantic verifier for the persisted player image before the generated-appraisal completion receipt can be consumed.

The verifier:

- requires the expected in-memory state itself to be an already-complete exact match for the receipt snapshot;
- reads the compressed persisted player NBT directly;
- decodes the same serializer-backed NeoForge attachments used by runtime state: Soul, permanent identity, Attribute ownership, Memory ownership and Echo ownership;
- requires the persisted attachment state to equal the exact intended committed state, including unrelated pre-existing ownership and mutable matching Echo state that recovery deliberately preserves;
- fails closed on missing files, missing required completion attachments, malformed payloads, stale/partial state or contradictions.

Normal `commitPrepared(...)` now performs the player save and semantic verification immediately after the successful Dreamer/appraisal mutation. Login replay performs the same semantic verification after applying the stored recovery plan and before clearing its receipt.

## Review corrections

### Receipt must be observable before teardown

`SavedDataPersistence.saveAndWait(...)` alone does not prove that the exact completion receipt reached `world/data/shadowslave_nightmare_completion_receipts.dat`; the asynchronous save path can report/log an I/O failure without giving this transaction a trustworthy exact-receipt proof. `NightmareCompletionReceiptData.begin(...)` now owns the production persistence checkpoint: it re-dirties the registry, saves/waits, and directly reads back the exact expected receipt through `PersistedNightmareCompletionReceiptVerifier` before returning. Idempotent retries re-dirty before another write attempt. If persistence or verification fails, active Nightmare ownership has not yet been consumed.

### Returned player image must survive stale registry teardown

A later review exposed a distinct split: the completion receipt may be durable while the active Nightmare registry image is already absent, even though the waking-world teleport was never persisted. Absence of active ownership therefore cannot be treated as proof that successful teardown fully completed.

Login replay now always converges the return half of the receipt. If exact active ownership still exists, the normal successful teardown path performs the teleport and ownership removal. If ownership is already absent, recovery teleports directly to the exact return dimension, position and rotation retained in the receipt's `NightmareInstance`. The returned player image is then saved while the receipt still exists. Contradictory active ownership still fails closed.

This deliberately makes the stored return destination idempotent recovery authority instead of relying on an inferred registry state.

### Receipt deletion must itself be verified

Another review finding showed that removing a receipt from memory and calling the async SavedData save barrier is not enough: a failed deletion write can leave the stale receipt on disk while the live process forgets it. A later preview reset or restart can then observe contradictory recovery state.

`NightmareCompletionReceiptData.clear(...)` now treats persisted absence as part of the operation in production. It saves/waits, parses the persisted receipt registry through the production receipt loader, and rejects any surviving receipt with the consumed player or instance identity. On ambiguous save/read-back failure it restores the exact receipt in memory and re-dirties the registry before throwing. Missing receipt files are accepted as absence; malformed persisted registries fail closed.

Because verification is owned by `clear(...)`, both normal completion and login replay receive the same deletion guarantee rather than depending on every caller to remember an extra checkpoint.

## Evidence classification

- **CANON:** unchanged. Nightmare completion remains distinct from appraisal; no Aspect, Flaw, Attribute, Memory, Echo, progression or reward rule changes.
- **INFERRED:** an appraisal already resolved before a technical crash should retain the same resolved identities/rewards rather than being regenerated from later code/catalogues.
- **DESIGN:** exact persisted-player semantic verification, exact receipt read-back, retry re-dirtying, replay of the stored return destination, persisted receipt-deletion verification, and fail-closed authority restoration are Java transaction/durability guards.
- **UNKNOWN:** physical fsync/power-loss guarantees below readable file images, storage corruption after verification, and full physical process-kill convergence across every boundary.
- **COMPATIBILITY:** no attachment or SavedData schema change. Existing unrelated Attribute/Memory/Echo ownership and mutable matching Echo command/manifestation state remain part of the exact expected image rather than being discarded. Successful-completion receipts retain the existing serialized `NightmareInstance`, so no new return-location field is required.

No new lore proposition is introduced. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` remain the governing lore/architecture sources.

## Tests

Focused persisted-player verifier tests cover exact committed persisted award acceptance, stale/partial ownership, malformed attachments, invalid expected state, and missing player files.

Focused persisted-receipt verifier tests cover:

- exactly one production-loadable expected receipt;
- missing and duplicate expected recovery authority;
- malformed receipt data rejected by the production loader;
- healthy persisted absence with unrelated receipts retained;
- stale exact, same-player, or same-instance receipt identities rejected after consumption.

Existing recovery-plan tests continue to cover exact active ownership, absent active ownership, and contradictory ownership. Hosted Preview Gates remain required for Java compile/unit/package, physical NeoForge client boot, dedicated-server boot, development JAR and frozen-datapack/deployed harnesses.

## Remaining boundary

These checkpoints are stronger than relying on save-call return or registry absence alone, but they are not hardware-level durability claims. Issue #34 should remain open until deterministic restart/process-loss evidence demonstrates that receipt creation, return, active-registry teardown, exact player award, and receipt consumption converge without duplicate appraisal or teardown across each supported durable boundary.
