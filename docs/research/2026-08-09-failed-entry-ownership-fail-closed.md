# Failed-entry ownership rollback: fail-closed boundary

**Date:** 2026-08-09  
**Base:** PR #146 exact head `d3db129257e56be0ca97e5aeb06e6c8322cb490d`  
**Scope:** failed First-Nightmare entry rollback only

## Why this audit exists

PR #146 deliberately left one adjacent question open: if exact authoritative registry ownership rollback throws, should the failed-entry catch still restore the pre-entry Carrier Soul?

The answer for the current lineage is **no**. Restoring Carrier state while authoritative active Nightmare ownership is still retained would intentionally create a mixed player/registry state. The ordering is therefore fail-closed: ownership rollback must return successfully before `SoulService.replace(player, beforeSoul)` runs.

This note does not claim that every possible registry failure is physically reproducible during normal single-threaded entry. It records the required consistency rule if stronger retained recovery authority or future code causes ownership removal to refuse.

## Current authority and recovery behavior

`NightmareService.removeMatchingEntryOwnership(...)` re-reads the active registry snapshot and removes it only when the attempted instance UUID still matches. `NightmareRegistryData.remove(...)` additionally refuses ordinary removal while a pending technical-exit transaction exists.

That refusal is intentional: a durable technical/admin exit is stronger recovery authority than an ordinary ownership teardown. Failed-entry cleanup must not erase it.

The login recovery chain also gives pending technical exit precedence over ordinary active-Nightmare recovery. Retaining both the technical marker and its active ownership therefore preserves a replayable authority instead of manufacturing Carrier state behind that transaction.

## Review correction

The first PR #148 head tested only the registry helper. Codex correctly identified that this was insufficient regression protection: the helper test would remain green if `tryEnter(...)` later swallowed an ownership-rollback failure and restored `beforeSoul` anyway.

The corrected implementation routes the actual pre-entry catch through `NightmareFailedEntryRollbackCoordinator`. Its contract is intentionally minimal:

1. perform authoritative failed-entry rollback;
2. only if that returns successfully, restore the pre-entry Soul snapshot.

`NightmareService.tryEnter(...)` now uses that production coordinator, and `NightmareFailedEntryRollbackCoordinatorTest` exercises the same sequencing seam with fake operations. The failure case proves that an exception from authoritative rollback propagates unchanged and that the Soul-restore operation is never invoked.

No recovery exception is swallowed, and no new best-effort Soul restoration path is introduced.

## Evidence classification

- **CANON:** unchanged. No Nightmare role, failure, death, appraisal, progression, return, Aspect, Flaw, Seed, or Dream Realm mechanic changes.
- **INFERRED:** unchanged association between one retained technical recovery transaction and the exact active Nightmare it owns.
- **DESIGN:** failed-entry Soul restoration is subordinate to successful authoritative ownership rollback. A stronger retained technical-exit authority blocks ordinary ownership removal and must remain intact. The package-local coordinator exists only to make that production ordering directly regression-testable.
- **UNKNOWN:** live NeoForge fault injection that produces this competing state during one entry attempt; process-crash behavior during entry; behavior if `SoulService.replace(...)` itself throws after ownership removal; administrator repair for unrelated corrupt registry state.
- **COMPATIBILITY:** successful entry, successful failed-entry rollback, PR #146 best-effort world cleanup, technical/admin replay, persistence schemas, and completion/death transactions remain unchanged.

No new novel proposition is introduced, so no canon rule is inferred or invented from this technical ordering.

## Regression evidence

`NightmareEntryRollbackOwnershipTest.pendingTechnicalExitBlocksFailedEntryOwnershipRollbackAndRetainsAuthority` establishes that:

1. a matching active Nightmare can carry a pending technical/admin exit;
2. failed-entry ownership rollback cannot bypass that transaction through ordinary `remove(...)`;
3. the active instance remains present;
4. the technical-exit reason remains present.

`NightmareFailedEntryRollbackCoordinatorTest` additionally establishes that:

1. successful authoritative rollback is followed by Soul restoration in that order;
2. an authoritative rollback failure prevents Soul restoration entirely;
3. the original rollback exception remains the surfaced failure.

Because `tryEnter(...)` now calls this same coordinator, the regression coverage owns the actual production sequencing policy rather than a test-only reimplementation.

## Validation note

The first PR #148 exact head `03a2a8bee520faafa3a5d6a5e9e636014706b88b` ran Preview Gates run #110. Its Java job passed compile/JUnit/package, both completion recovery verifier self-tests, physical NeoForge client boot, and dedicated-server boot. The datapack job failed independently when the vanilla server reported being roughly 53 seconds behind and Mineflayer hit its 30-second keepalive timeout; no Java or datapack source in #148 caused that overload symptom. The failed datapack job was retried once.

That first head is superseded by the review correction and is not final evidence for the corrected branch. The corrected exact head requires its own fresh hosted gate.

## Deliberate limit / next work

Do not add a best-effort Soul restore around registry-removal failure without new evidence and an explicit recovery design. If a real failure demonstrates that the retained ownership cannot be replayed safely, capture the exact state/logs first and design against that evidence.

Issue #34's successful-completion physical restart matrix remains a separate blocked evidence task and is not broadened by this audit.
