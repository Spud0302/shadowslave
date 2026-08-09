# Failed-entry ownership rollback: fail-closed boundary

**Date:** 2026-08-09  
**Base:** PR #146 exact head `d3db129257e56be0ca97e5aeb06e6c8322cb490d`  
**Scope:** failed First-Nightmare entry rollback only

## Why this audit exists

PR #146 deliberately left one adjacent question open: if exact authoritative registry ownership rollback throws, should the failed-entry catch still restore the pre-entry Carrier Soul?

The answer for the current lineage is **no**. Restoring Carrier state while authoritative active Nightmare ownership is still retained would intentionally create a mixed player/registry state. The current ordering is therefore fail-closed: ownership rollback must return successfully before `SoulService.replace(player, beforeSoul)` runs.

This note does not claim that every possible registry failure is physically reproducible during normal single-threaded entry. It records the required consistency rule if stronger retained recovery authority or future code causes ownership removal to refuse.

## Current authority and recovery behavior

`NightmareService.removeMatchingEntryOwnership(...)` re-reads the active registry snapshot and removes it only when the attempted instance UUID still matches. `NightmareRegistryData.remove(...)` additionally refuses ordinary removal while a pending technical-exit transaction exists.

That refusal is intentional: a durable technical/admin exit is stronger recovery authority than an ordinary ownership teardown. Failed-entry cleanup must not erase it.

The login recovery chain also gives pending technical exit precedence over ordinary active-Nightmare recovery. Retaining both the technical marker and its active ownership therefore preserves a replayable authority instead of manufacturing Carrier state behind that transaction.

## Evidence classification

- **CANON:** unchanged. No Nightmare role, failure, death, appraisal, progression, return, Aspect, Flaw, Seed, or Dream Realm mechanic changes.
- **INFERRED:** unchanged association between one retained technical recovery transaction and the exact active Nightmare it owns.
- **DESIGN:** failed-entry Soul restoration is subordinate to successful authoritative ownership rollback. A stronger retained technical-exit authority blocks ordinary ownership removal and must remain intact.
- **UNKNOWN:** live NeoForge fault injection that produces this competing state during one entry attempt; process-crash behavior during entry; behavior if `SoulService.replace(...)` itself throws after ownership removal; administrator repair for unrelated corrupt registry state.
- **COMPATIBILITY:** successful entry, successful failed-entry rollback, PR #146 best-effort world cleanup, technical/admin replay, persistence schemas, and completion/death transactions remain unchanged.

No new novel proposition is introduced, so no canon rule is inferred or invented from this technical ordering.

## Regression evidence

`NightmareEntryRollbackOwnershipTest.pendingTechnicalExitBlocksFailedEntryOwnershipRollbackAndRetainsAuthority` establishes that:

1. a matching active Nightmare can carry a pending technical/admin exit;
2. failed-entry ownership rollback cannot bypass that transaction through ordinary `remove(...)`;
3. the active instance remains present;
4. the technical-exit reason remains present.

The production catch already restores the Carrier Soul only after `rollbackFailedEntry(...)` returns, so a thrown ownership-removal guard prevents the mixed Carrier-plus-retained-ownership state.

## Deliberate limit / next work

Do not add a best-effort Soul restore around registry-removal failure without new evidence and an explicit recovery design. If a real failure demonstrates that the retained ownership cannot be replayed safely, capture the exact state/logs first and design against that evidence.

Issue #34's successful-completion physical restart matrix remains a separate blocked evidence task and is not broadened by this audit.
