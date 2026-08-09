# Nightmare late-entry world rollback — current lineage

**Date:** 2026-08-09

## Context

PR #136 made `LastSignalScenario.prepare(...)` transactional when preparation itself fails. PR #139 corrected failed-entry registry teardown so it removes the exact authoritative active snapshot rather than a locally modified copy. Corrected PR #140 exact head `2caf62fb33c842db5739b3e689842c3f22afdddf` then passed Preview Gates run #104 and established the authoritative entry commit boundary on both normal and exceptional teleport paths: destructive failed-entry rollback is allowed only while the player has not been observed in `NightmareService.NIGHTMARE_LEVEL`.

Historical PR #69 identified one remaining pre-entry leak. If preparation succeeds but a later pre-commit step throws, the prepared slot geometry exists even though entry ultimately fails. Removing the pursuer and registry ownership without clearing the slot leaves ownerless world state in the shared Nightmare dimension.

Historical #69 originally attempted that cleanup before the commit boundary existed. Codex correctly found that destructive rollback could then run after the player had already moved into the Nightmare. Corrected #140 resolves that prerequisite, including the throw-after-level-switch case: once authoritative player state has demonstrated Nightmare entry, `tryEnter(...)` retains ownership instead of invoking failed-entry rollback.

## Current correction

The failed-entry world rollback now:

1. derives the physical rollback origin from the immutable allocated `slot` via `LastSignalScenario.originForSlot(...)`;
2. discards the attempted pursuer;
3. clears the allocated Last Signal slot volume;
4. uses `NightmarePreparationTransaction.rollbackAll(...)` so slot clearing is still attempted if entity cleanup fails;
5. then allows the existing #139 authoritative ownership rollback and Soul rollback to continue.

The physical namespace deliberately does **not** come from persisted layout fields. `registry.update(prepared)` itself may be the failing boundary, so the registry can still own the pre-preparation snapshot whose layout remains `BlockPos.ZERO` even though world preparation already used the allocated slot.

Ordinary successful, technical/admin and terminal teardown continue to remove owned entities only. Destructive slot clearing is restricted to the pre-entry rollback side established by corrected #140.

## Evidence classification

- **CANON:** unchanged. This slice changes no Nightmare ending, survival, appraisal, progression, death, return, Aspect, Flaw, Seed, or Dream Realm rule.
- **INFERRED:** unchanged one-instance ownership of technical scenario state during an active First Nightmare.
- **DESIGN:** a failed Java entry transaction should not leave ownerless scenario geometry; the immutable allocated slot is the physical rollback namespace.
- **UNKNOWN:** live NeoForge fault injection after successful preparation; process-crash atomicity during entry; exact behavior if world rollback itself ultimately throws; restoration of arbitrary blocks that may have pre-existed in an allocated preview slot.
- **COMPATIBILITY:** successful entry, #136 preparation-failure cleanup, #139 authoritative ownership rollback, corrected #140 observed-dimension/exception-path commit behavior, persistence formats, and all completion/death/recovery transactions remain unchanged.

No canon rule is introduced or generalized.

## Regression coverage

`NightmareLateEntryWorldRollbackTest` proves rollback targets `originForSlot(instance.slot())` even when the snapshot's persisted `origin` is still `BlockPos.ZERO`.

`NightmarePreparationTransactionTest` already covers the cleanup primitive's important failure behavior: later rollback steps still run after an earlier cleanup step throws, and rollback failures are retained as suppressed evidence.

## Deliberate limits

This slice does not reclaim `nextSlot`, does not restore arbitrary previous blocks, and does not make all failed-entry cleanup infallible. If destructive world rollback itself ultimately throws, later ownership/Soul rollback can still be interrupted; that remains a separate correctness question and should not be broadened without concrete failure evidence.

Issue #34's successful-completion physical restart matrix remains separately blocked on real-player execution and is not retried here.
