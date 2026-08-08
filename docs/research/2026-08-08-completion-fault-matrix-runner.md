# Successful-completion fault matrix runner — 2026-08-08

## Scope

Issue #34 still requires physical dedicated-server restart evidence at each named successful-completion durability boundary. PR #83 hardens the in-process fault selector and defines the six-row evidence matrix, but the repository did not yet contain a dedicated server run that passes the selected fault point into the Minecraft JVM or a repeatable collector for the resulting process/log evidence.

This slice adds that missing orchestration layer. It does **not** claim to have executed a physical row, and it does not pretend a shell process can replace the real player's entry, objective interaction, reconnect, Soul/status observation, or second relog.

## Runner boundary

`mod/build.gradle` now defines `completionFaultServer`, producing the ModDev task `runCompletionFaultServer` with its own persistent `mod/run-completion-fault` game directory. Passing Gradle project property `-PcompletionFault=<point>` maps only for that run to Minecraft system property `shadowslave.completionFault=<point>`.

`mod/run-completion-fault-row.sh` provides four evidence stages plus a synthetic self-test:

- `init` creates EULA/server properties without deleting or replacing a world;
- `fault <point>` starts the dedicated server with the exact point armed, retains console/latest logs, requires the exact fault marker, and requires launcher evidence that the Minecraft child exited with 86;
- `recover <point>` restarts the same run directory with no fault property and retains the recovery console/latest logs after the operator completes reconnect/relog observations and stops normally;
- `verify <point> <instance>` requires exactly one instance-keyed preview appraisal and one teardown across the retained console evidence and rejects the known stale-teardown warning;
- `self-test` checks point validation plus positive and duplicate-teardown evidence parsing without starting Minecraft.

The Minecraft JVM is a Gradle child under ModDev. `Runtime.halt(86)` therefore commonly makes the Gradle wrapper return status 1 even though the actual Minecraft child exited 86. The runner does not misreport the wrapper status as the Minecraft status; it requires the exact intentional-fault log marker and Gradle's child-process exit-86 diagnostic.

## Physical usage

For each of the six PR #83 points, use a fresh copy of the intended disposable baseline world in `mod/run-completion-fault/world`, then:

```bash
bash mod/run-completion-fault-row.sh init
bash mod/run-completion-fault-row.sh fault after_terminal_registry_save
# real player completes the normal First Nightmare; server halts intentionally
bash mod/run-completion-fault-row.sh recover after_terminal_registry_save
# same player reconnects, records Soul/status, relogs once, repeats, then operator stops server
bash mod/run-completion-fault-row.sh verify after_terminal_registry_save <INSTANCE-UUID>
```

Repeat from a fresh baseline copy for every remaining point. Evidence defaults under `mod/completion-fault-evidence/<point>/` and can be redirected with `COMPLETION_FAULT_EVIDENCE_ROOT`.

A shell `verify` PASS is **not** the whole row PASS. The operator still must record the exact commit/JAR, same world, same player, Dreamer/Dormant state with expected preview identity, no active Nightmare, and completion of the second relog. The PR #83 matrix remains the authoritative physical acceptance contract.

## Evidence classification

- **CANON:** unchanged. This changes no Nightmare, appraisal, progression, return, death, Aspect, or Flaw mechanic.
- **INFERRED:** none added.
- **DESIGN:** the dedicated ModDev run, Gradle-property-to-JVM-property bridge, evidence directory layout, child-exit validation, and shell evidence checks are test infrastructure.
- **UNKNOWN:** no physical row has been executed by this repository change; dedicated-server convergence at all six process-kill boundaries remains unproven until real player/restart evidence is retained.
- **COMPATIBILITY:** normal `server`, `serverSmoke`, client, data and gameplay runs are unchanged. The new fault server uses an isolated run directory and only receives the fault system property when explicitly passed `-PcompletionFault`.

No canon rule is invented.

## Test/verification boundary

The runner's `self-test` is intentionally process-free. It proves that a known point is accepted, a misspelled point is rejected, one appraisal + one teardown passes evidence parsing, and a duplicate teardown fails it. It does not prove ModDev launch behaviour, Minecraft child exit propagation, server startup, player interaction, or restart convergence.

The strongest next evidence step is to execute one real row end to end. If the ModDev child-exit diagnostic differs from the runner's accepted pattern, record the actual launcher output and adjust the evidence parser from that concrete observation rather than guessing another format.
