# Successful completion runtime adapter integration — 2026-08-08

## Scope

This slice binds the already-reviewed successful-completion coordinator contract to the current server persistence surfaces without yet changing terminal-event or login routing.

The adapter implements `NightmareCompletionCoordinator.Operations` using:

- `NightmareRegistryData` for the retained completion receipt, phase and exact active ownership;
- `PreviewAppraisalService` for idempotent appraisal observation/reconciliation;
- synchronous `PlayerList.saveAll()` for the player durability checkpoint;
- `SavedDataPersistence.saveAndWait(...)` for joined registry durability;
- the recorded return dimension/position for successful return;
- exact-instance Last Signal entity cleanup plus registry ownership removal for teardown.

Keeping event routing out of this slice means the adapter can be reviewed independently before it becomes the live signal-fire/login path.

## Exact authority hardening

The historical correctness lineage's server adapter treated active ownership as present when the active instance UUID matched the retained completion instance UUID. The current consolidation has since strengthened the persistence contract: a successful-completion receipt freezes the complete authoritative `NightmareInstance` snapshot, including layout and pursuer identity.

This integration therefore requires exact snapshot equality for both the retained receipt and any still-active ownership. If either surface contains a stale or different snapshot, the adapter fails closed rather than treating the transaction as recoverable authority.

The teardown operation likewise requires exact ownership removal to succeed when the coordinator decided teardown was required. A disappearing or mismatched owner is an invariant failure, not an idempotent success signal at that point.

## Evidence classification

- **CANON:** unchanged. No Nightmare ending, appraisal, progression, death, role, Aspect, Flaw, Memory, Echo or return lore rule changes.
- **INFERRED:** unchanged association between one incomplete successful-completion transaction and the exact Nightmare instance that produced it.
- **DESIGN:** server attachment/SavedData durability boundaries, exact-snapshot transaction authority, synchronous player-save implementation and technical teardown behavior are Minecraft persistence choices.
- **UNKNOWN:** terminal/login routing is not live in this slice; real dedicated-server restart/process-kill convergence, cancelled/redirection event behavior, filesystem/power-loss semantics beyond the joined NeoForge worker, and administrator repair for blocked state remain unproven.
- **COMPATIBILITY:** no save schema or player-facing mechanic changes. Current runtime still uses the old success path until a later routing slice constructs this adapter.

No new canon proposition is introduced, so no new primary-novel claim is asserted. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, the existing appraisal evidence, and Issue #34 remain controlling.

## Tests

`ServerNightmareCompletionOperationsTest` is intentionally process-free. It checks that:

- an exact retained receipt is accepted and a missing receipt fails closed;
- a same-UUID but changed snapshot is rejected;
- a different retained instance for the same player is rejected;
- exact active ownership is accepted;
- absent active ownership is observed as absent for replay after teardown;
- changed or different active ownership fails closed.

The Minecraft-bound return, attachment save, SavedData join and entity cleanup paths still require the repository's executable Gradle/smoke gates and later physical Issue #34 fault matrix.

## Next integration order

1. Route terminal signal-fire success through `beginSuccessfulCompletion(...)`, persist/join that receipt, then run the coordinator with this adapter.
2. Route player login through retained successful-completion recovery before ordinary active-Nightmare resume.
3. Port the isolated completion-fault ModDev run and corrected #90 log-fallback runner onto the consolidated current-main lineage.
4. Execute and retain all six same-world physical process-kill rows before claiming Issue #34 complete.
