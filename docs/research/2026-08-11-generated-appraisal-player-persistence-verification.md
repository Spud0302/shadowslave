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

Fresh review found two additional P1 durability gaps in the same transaction.

### Receipt must be observable before teardown

`SavedDataPersistence.saveAndWait(...)` alone does not prove that the exact completion receipt reached `world/data/shadowslave_nightmare_completion_receipts.dat`; the asynchronous save path can report/log an I/O failure without giving this transaction a trustworthy exact-receipt proof. `NightmareCompletionReceiptData.begin(...)` now owns the production persistence checkpoint: it re-dirties the registry, saves/waits, and directly reads back the exact expected receipt through `PersistedNightmareCompletionReceiptVerifier` before returning. Idempotent retries re-dirty before another write attempt. If persistence or verification fails, active Nightmare ownership has not yet been consumed.

Focused receipt-verifier tests require exactly one production-loadable expected receipt and reject missing, duplicate, or malformed authority.

### Return teleport must precede durable teardown

Login replay can restart with both the exact completion receipt and the matching active Nightmare. `recoverSuccessfulCompletion(...)` teleports the player back and removes active ownership in memory. Persisting the registry removal before saving the returned player location creates a split where a second restart can load no active ownership but still load the player inside the Nightmare.

Replay now saves player data immediately after successful return/teardown and only then crosses the SavedData persistence barrier. The completion receipt remains present across both writes. A later crash can therefore replay from the retained receipt rather than strand a player in the Nightmare without active ownership.

## Evidence classification

- **CANON:** unchanged. Nightmare completion remains distinct from appraisal; no Aspect, Flaw, Attribute, Memory, Echo, progression or reward rule changes.
- **INFERRED:** an appraisal already resolved before a technical crash should retain the same resolved identities/rewards rather than being regenerated from later code/catalogues.
- **DESIGN:** exact persisted-player semantic verification, exact receipt read-back, retry re-dirtying, and return-before-registry persistence ordering are Java transaction/durability guards.
- **UNKNOWN:** physical fsync/power-loss guarantees below readable file images, storage corruption after verification, and full physical process-kill convergence across every boundary.
- **COMPATIBILITY:** no attachment or SavedData schema change. Existing unrelated Attribute/Memory/Echo ownership and mutable matching Echo command/manifestation state remain part of the exact expected image rather than being discarded.

No new lore proposition is introduced. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` remain the governing lore/architecture sources.

## Tests

Focused persisted-player verifier tests cover:

- exact committed persisted award acceptance;
- stale/partial persisted ownership rejection;
- malformed attachment rejection;
- refusal to verify an expected state that does not itself contain the exact receipt award;
- missing player-file rejection.

Focused persisted-receipt verifier tests cover:

- exactly one production-loadable expected receipt;
- missing expected receipt;
- duplicate expected recovery authority;
- malformed receipt rejected by the production loader.

Hosted Preview Gates remain required for Java compile/unit/package, physical NeoForge client boot, dedicated-server boot, development JAR and frozen-datapack/deployed harnesses.

## Remaining boundary

These checkpoints are stronger than relying on save-call return alone, but they are not hardware-level durability claims. Issue #34 should remain open until restart-boundary/process-kill convergence evidence demonstrates that the retained receipt, returned location, registry teardown, and exact player award converge without duplicate appraisal or teardown across the supported crash points.
