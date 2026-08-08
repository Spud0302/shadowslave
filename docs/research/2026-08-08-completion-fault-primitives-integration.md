# Completion fault primitive integration — 2026-08-08

## Scope

This slice ports only the deterministic successful-completion fault-point vocabulary and fail-closed JVM configuration primitive into the current Nightmare-correctness consolidation lineage. It intentionally does not port the old 153-commit transaction history or wire any fault point into runtime completion yet.

Issue #34 requires physical restart evidence at named durable boundaries and explicitly rejects timing-based process kills between adjacent Java statements. A misspelled configured boundary must therefore fail closed rather than silently run a normal completion.

## Current-repository check

The active consolidation branch `gpt/integrate-nightmare-correctness-stack` is being rebuilt from current `main` because the historical #39/#57-#83 lineage conflicts with newer merged identity/content work. This child slice is independent of `AspectAbilitySetData`, Attribute, Memory, Echo, scenario-content, and appraisal-identity APIs, so it can be reviewed and integrated without duplicating that reconciliation.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, death, progression, or return mechanic is changed.
- **INFERRED:** none added.
- **DESIGN:** six named post-durability test boundaries, the `shadowslave.completionFault` JVM property, one-shot process termination, and exit code 86 are test infrastructure.
- **UNKNOWN:** no physical dedicated-server process-kill/restart row is proven by these primitives; wiring to real durable checkpoints and retained end-to-end evidence remain outstanding.
- **COMPATIBILITY:** with the property absent or blank, fault injection is disabled. Valid names select exactly one boundary. Invalid non-blank values now fail explicitly so they cannot be mistaken for restart evidence.

No canon claim is introduced, so no new primary-novel mechanic research is required beyond re-reading the mandatory lore-source and Java-alignment policies. Existing Issue #34 completion evidence remains controlling.

## Tests

`NightmareCompletionFaultPointTest` covers name round-trip and parser rejection. `NightmareCompletionFaultInjectorTest` covers disabled configuration, exact valid selection, and fail-closed misspelling without invoking `Runtime.halt`.

## Deliberate limits

- no coordinator calls `afterDurableBoundary(...)` in this slice;
- no ModDev runner/property bridge is added here;
- no shell fault-matrix runner is ported here;
- no Gradle/JUnit/client/server result is claimed until an executable gate reports against the exact head;
- PR #90's retained-`latest.log` verifier correction remains a later harness-port requirement once the runner itself is integrated.
