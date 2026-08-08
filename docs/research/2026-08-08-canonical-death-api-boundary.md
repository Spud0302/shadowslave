# Canonical Nightmare death API-boundary audit

## Scope

This slice follows PR #77 and audits whether supported callers can still bypass its restart-replayable canonical-death transaction.

## Finding

PR #77 routes the NeoForge death event through `NightmareDeathService.record(...)`, but `NightmareService` still exposed the older public `canonicalDeath(ServerPlayer)` implementation. That legacy helper tears down active ownership before player Soul/identity state is saved and therefore retains the crash window PR #77 was created to remove.

A repository caller audit found no supported runtime caller that still requires the legacy public method. Leaving it public creates a future correctness trap: a new caller can compile while bypassing the durable death intent and login-replay ordering.

## Change

The legacy public method is removed. Canonical First-Nightmare death now has one supported runtime entry point: `NightmareDeathService`, whose transaction records durable death intent before completion cleanup, player reset, and teardown.

A reflection regression test prevents a public `NightmareService.canonicalDeath` bypass from being reintroduced accidentally.

## Evidence classification

- **CANON:** unchanged from PR #77. Ordinary First-Nightmare death is failure/death rather than safe Spell ejection.
- **INFERRED:** none added.
- **DESIGN:** canonical-death mutation authority is intentionally centralized behind the restart-replayable death service; unsafe duplicate public mutation APIs are rejected.
- **UNKNOWN:** physical process-kill behavior at the durable boundaries remains unproven until Issue #34's dedicated-server fault-injection work runs.
- **COMPATIBILITY:** the NeoForge runtime already used `NightmareDeathService.record(...)`; no supported runtime behavior or save schema changes. External Java code compiled specifically against the old preview-only helper would need to move to the durable service rather than retain unsafe semantics.

## Lore/source boundary

No lore mechanic changes in this API-hardening slice. `docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` were re-read; the primary death evidence remains the Chapter 1 / Chapter 887 evidence recorded in PR #77's research note. No additional canon claim is introduced.

## Deliberate limits

- does not implement corpse-Gate consequences;
- does not alter Minecraft respawn accommodation;
- does not claim executable Gradle/JUnit or physical restart evidence until a gate runs for this exact head;
- does not resolve Issue #34's remaining physical fault-injection matrix.
