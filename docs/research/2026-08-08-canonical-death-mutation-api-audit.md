# Canonical Nightmare death mutation API audit — 2026-08-08

## Scope

Follow-up to PR #77's restart-replayable canonical-death transaction and PR #79's removal of the legacy `NightmareService.canonicalDeath(...)` bypass.

This audit asks whether lower-level public Java APIs can erase the persisted canonical-death recovery authority outside `NightmareDeathService` / `NightmareDeathCoordinator`.

## Finding

`NightmareDeathRegistryData.begin(...)` and `complete(...)` were public. In particular, `complete(...)` could delete the exact persisted death marker without proving that:

1. the canonical death player reset had been saved;
2. active Nightmare teardown had been persisted; or
3. the coordinator had reached its final durable boundary.

That marker is the fact login recovery uses to distinguish an already-chosen canonical death from ordinary active-Nightmare recovery. Allowing arbitrary external callers to clear it weakens the transaction boundary even though the current runtime path does not do so.

The companion active-registry `remove(...)` operation does not by itself erase this death marker. If active ownership disappears while the marker remains, `NightmareDeathService.resumePending(...)` can still replay the death outcome from the marker snapshot. Therefore this slice does not broaden into a redesign of all Nightmare registry mutations.

## Change

`NightmareDeathRegistryData.begin(...)` and `complete(...)` are now package-private. Public consumers retain read/recovery inspection (`get`, `recoveryBlocked`, `loadFailure`, `findByPlayer`) but cannot create or clear canonical-death intent directly.

`NightmareServiceApiTest` now guards that API shape in addition to the existing check that `NightmareService.canonicalDeath(...)` cannot return.

## Evidence classification

- **CANON:** unchanged from PR #77. Ordinary First-Nightmare death is real failure/death rather than a safe Spell ejection.
- **INFERRED:** none added.
- **DESIGN:** durable death-intent mutation is package-owned transaction machinery, not a general public extension point.
- **UNKNOWN:** physical process-kill behavior at the actual persistence boundaries remains unproven until Issue #34's dedicated-server fault-injection matrix runs.
- **COMPATIBILITY:** the supported NeoForge runtime already mutates the marker only from `NightmareDeathService`; read-only recovery inspection remains public. External preview Java code that was directly mutating death markers must migrate to the durable service instead of bypassing transaction ordering.

No new lore-sensitive mechanic is introduced, so no new novel rule is inferred or invented in this slice. The primary Chapter 1 / Chapter 887 evidence recorded for PR #77 remains controlling.

## Remaining boundary

Package-private visibility is an API hardening measure, not a proof against every future class placed in the same package. The stronger remaining correctness evidence is physical restart/fault injection across the coordinator's documented durable boundaries. Do not manufacture additional transaction layers unless that testing or a new review finding demonstrates another concrete failure mode.
