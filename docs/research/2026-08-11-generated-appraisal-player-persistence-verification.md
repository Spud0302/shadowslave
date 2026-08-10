# Generated appraisal player persistence verification

**Date:** 2026-08-11  
**Scope:** successful First-Nightmare appraisal recovery / Issue #34  
**Parent:** PR #201 durable generated-appraisal receipt + login replay

## Parent evidence

PR #201 exact head `9c209e0de5392d879c192d481641a009cb66bde1` passed Preview Gates #205 / Actions run `31402364266`. This note does not reuse that pass as evidence for the additional persisted-player verifier; the successor head requires its own complete gate.

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

## Evidence classification

- **CANON:** unchanged. Nightmare completion remains distinct from appraisal; no Aspect, Flaw, Attribute, Memory, Echo, progression or reward rule changes.
- **INFERRED:** an appraisal already resolved before a technical crash should retain the same resolved identities/rewards rather than being regenerated from later code/catalogues.
- **DESIGN:** exact persisted-player semantic verification is a Java transaction/durability guard; the attachment keys, NBT read-back checkpoint and fail-closed behavior are implementation details.
- **UNKNOWN:** physical fsync/power-loss guarantees below the readable file image, storage corruption after verification, and full physical process-kill convergence across every boundary.
- **COMPATIBILITY:** no attachment or SavedData schema change. Existing unrelated Attribute/Memory/Echo ownership and mutable matching Echo command/manifestation state remain part of the exact expected image rather than being discarded.

No new lore proposition is introduced. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` remain the governing lore/architecture sources.

## Tests

Focused verifier tests cover:

- exact committed persisted award acceptance;
- stale/partial persisted ownership rejection;
- malformed attachment rejection;
- refusal to verify an expected state that does not itself contain the exact receipt award;
- missing player-file rejection.

Hosted Preview Gates remain required for Java compile/unit/package, physical NeoForge client boot, dedicated-server boot, development JAR and frozen-datapack/deployed harnesses.

## Remaining boundary

This semantic checkpoint is stronger than relying on `saveAll()` return alone, but it is not a hardware-level durability claim. Issue #34 should remain open until restart-boundary/process-kill convergence evidence demonstrates that the retained receipt and exact player state converge without duplicate appraisal or teardown across the supported crash points.
