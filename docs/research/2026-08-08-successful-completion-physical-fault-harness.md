# Successful Nightmare completion physical fault harness — 2026-08-08

## Scope

Issue #34 requires physical process-restart evidence at each durable successful-completion boundary without timing a kill between adjacent Java statements.

The active persistence stack already contains six deterministic completion fault points. `NightmareCompletionCoordinator` calls the hook only after the associated player or registry persistence operation returns, and `NightmareCompletionFaultInjector` halts the JVM with exit code `86` when one configured point is reached.

This slice hardens the harness configuration and records the exact physical matrix. It does **not** claim that the matrix has been executed.

## Why this is the next bounded slice

The transaction and mutation-authority audits through PR #81 have accumulated simulated restart coverage and removed demonstrated public bypasses. The remaining Issue #34 requirement is physical evidence against the real NeoForge/server persistence surfaces.

A timing-based `kill -9` is explicitly forbidden by Issue #34 because it cannot prove which side of an adjacent Java statement became durable. The existing named hook is the credible deterministic approach.

## Harness configuration correction

Before this slice, a non-blank typo in `-Dshadowslave.completionFault=...` parsed as no configured point. That could let the server perform an ordinary successful completion while an operator believed a crash boundary had been armed.

`NightmareCompletionFaultInjector.configuredPoint()` now fails closed for an unknown non-blank value and reports every accepted serialized point. Blank or absent configuration still means fault injection is disabled.

## Physical matrix

Run each point against a fresh copy of the same disposable dedicated-server world and the exact JAR/commit under review:

1. `after_terminal_registry_save`
2. `after_appraisal_player_save`
3. `after_appraisal_registry_save`
4. `after_return_player_save`
5. `after_return_registry_save`
6. `after_teardown_registry_save`

For each row:

1. start the server with `-Dshadowslave.completionFault=<point>`;
2. reset the test player and begin The Last Signal normally;
3. record the Nightmare instance UUID and slot;
4. trigger the normal signal-fire terminal resolution;
5. require the process to terminate with exit code `86` and require a log line naming the exact selected boundary;
6. restart the same world **without** the fault property;
7. reconnect the same player so login recovery executes;
8. record `/shadowslave soul` and `/shadowslave nightmare_status`;
9. require Dreamer/Dormant plus the already-established preview identity, no active Nightmare ownership, one appraisal marker for that instance, and one teardown marker for that instance;
10. reconnect once more and repeat the state/log checks to prove replay does not add another appraisal or teardown.

A row is **BLOCKED**, not PASS, if the process does not halt with code `86`, the exact boundary marker is absent, the restart uses a different world/JAR, the player does not reconnect, or exact instance-keyed appraisal/teardown counts are not collected.

## Evidence classification

- **CANON:** unchanged. This harness changes no Nightmare, appraisal, progression, return, or death mechanic.
- **INFERRED:** none added.
- **DESIGN:** deterministic process termination immediately after named Java durability checkpoints, exit code `86`, and fail-closed JVM-property validation are test infrastructure.
- **UNKNOWN:** whether every row actually converges correctly on a real dedicated server remains unknown until the matrix is run and its logs/world evidence are retained; I/O-worker completion is still not a claim about every storage-device or power-loss failure mode.
- **COMPATIBILITY:** with the fault property absent, runtime behavior is unchanged. A valid property retains the existing one-shot intentional halt. Only invalid non-blank test configuration changes, from silent disablement to explicit failure.

No canon rule is invented.

## Test coverage in this slice

`NightmareCompletionFaultInjectorTest` verifies:

- absent property leaves physical fault injection disabled;
- a serialized known boundary selects exactly that point;
- an invalid non-blank value throws and includes both the bad value and a valid expected value in its diagnostic.

The test deliberately does not invoke `Runtime.halt(...)`.

## Remaining condition to close Issue #34

Execute all six physical rows on the exact candidate stack, retain process exit status and logs for each row, and demonstrate exactly one appraisal plus exactly one teardown for the recorded instance after recovery and a second relog. Until then the logical transaction is strongly covered but physical restart correctness remains unproven.
