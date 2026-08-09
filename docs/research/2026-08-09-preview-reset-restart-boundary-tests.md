# Preview reset restart-boundary simulation — 2026-08-09

## Context

PR #131 (`gpt/durable-preview-reset-current`) makes compound `preview_reset` restart-replayable and gives retained reset intent login precedence after canonical death. Exact head `8ac6399a36bd6b27094f79e37ec9498d9a076196` passed Preview Gates run #94 / ID `31293706502` before this follow-up branch was created.

Issue #34's successful-completion physical matrix remains separately blocked on real-player same-world process-kill execution. This slice does not retry or substitute for that physical evidence.

## Bounded gap

#131's service tests prove in-process ordering and idempotent replay, but they do not explicitly reconstruct state from only the durability surfaces that would survive a process loss.

The highest-value process-free cases are:

1. restart immediately after the first joined registry save, when only reset intent is newly durable;
2. restart immediately after synchronous player persistence, when the cleared player baseline is durable but final reset-marker completion is not;
3. replay when a retained preview-reset marker coexists with a narrower retained technical/admin-exit marker, the current-lineage interaction discovered during #131 integration.

## Test model

`PreviewResetRestartBoundaryTest` uses the existing `PreviewResetService.Operations` seam and a deliberately small durability model:

- registry mutations become restart-visible only through `persistRegistry()`;
- player-state clearing becomes restart-visible only through `persistPlayer()`;
- a simulated crash discards all other in-memory working state;
- a fresh operations object is then constructed solely from retained durable state and the reset is replayed.

The converged state requires no reset marker, no technical-exit marker, no active Nightmare, no successful-completion receipt, and a cleared player baseline.

This is a transaction-model regression test, not a Minecraft I/O emulator.

## Evidence classification

- **CANON:** unchanged. `preview_reset` and crash recovery are development/technical operations, not Nightmare Spell mercy mechanics.
- **INFERRED:** none added.
- **DESIGN:** durability-surface reconstruction and replay assertions are process-free transaction tests.
- **UNKNOWN:** actual JVM/process loss at these exact boundaries; filesystem/storage behavior below NeoForge's joined worker; live player reconnect behavior; corrupt-marker administrator repair UX.
- **COMPATIBILITY:** no production runtime, save schema, gameplay result, progression, appraisal, death, or lore-facing behavior changes. The tests exercise the transaction contract introduced by #131.

No new novel proposition is introduced, so no additional canon rule is inferred or invented. `docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` remain controlling.

## Next evidence

If this exact head passes hosted Gradle/JUnit plus the existing client/server/datapack gates, further preview-reset transaction expansion should stop unless a new concrete failure is demonstrated. Physical restart evidence remains desirable when an environment can execute it, while Issue #34's successful-completion matrix stays paused until its recorded real-player resume condition changes.
