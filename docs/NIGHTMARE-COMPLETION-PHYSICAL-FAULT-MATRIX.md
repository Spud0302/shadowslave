# Physical successful-completion fault matrix

**Status:** operator-only evidence procedure for issue #34 and the current restart-recoverable completion stack.

**Classification:** the restart transaction, fault injector and evidence runner are Minecraft **DESIGN**. They are not presented as canonical Nightmare Spell mechanics.

## Safety boundary

The injector is disabled unless the dedicated-server JVM starts with:

```text
-Dshadowslave.completionFault=<fault_point>
```

The repository runner forwards this property only to the isolated `completionFaultServer` ModDev run. At the matching boundary, the required Java persistence call has already completed, the server writes an `INTENTIONAL COMPLETION FAULT` marker and the Minecraft child terminates immediately with exit code `86`. Remove the property before recovery. Never use this flag on a production world.

## Fault points

The live transaction exposes six deterministic post-durability boundaries:

```text
after_terminal_registry_save
after_appraisal_player_save
after_appraisal_registry_save
after_return_player_save
after_return_registry_save
after_teardown_registry_save
```

The first point is now part of the live Last Signal terminal path. It proves that the retained terminal-resolution receipt, rather than the campfire presentation state, is sufficient authority for restart replay.

## Repository runner

Prepare the isolated run directory without deleting any existing world:

```bash
bash mod/run-completion-fault-row.sh init
```

For one row:

```bash
bash mod/run-completion-fault-row.sh fault after_terminal_registry_save
bash mod/run-completion-fault-row.sh recover after_terminal_registry_save
bash mod/run-completion-fault-row.sh verify after_terminal_registry_save <instance-uuid>
```

The `fault` stage requires the exact intentional-fault marker plus launcher evidence that the Minecraft child exited with code `86`. Because ModDev can route child logging differently, the exact fault marker may be retained in either `fault-console.log` or `fault-latest.log`; verification accepts the same two-source contract.

Exactly-once appraisal and teardown counting remains console-only so mirrored `latest.log` output cannot be double-counted. The current live successful-completion teardown marker is:

```text
Nightmare <instance-uuid> successful-completion teardown completed
```

The runner's synthetic `self-test` checks valid/misspelled fault-point handling, the retained-`latest.log` marker fallback, exactly one appraisal/teardown success, and duplicate teardown rejection without launching Minecraft:

```bash
bash mod/run-completion-fault-row.sh self-test
```

## Manual procedure

For each fault point, use a fresh copy of the same disposable dedicated-server baseline and the exact PR JAR/source head.

1. Start without a fault property, run `/shadowslave preview_reset`, then `/shadowslave preview_begin`.
2. Stop normally before completing the signal fire if preparing a reusable baseline copy.
3. Start the row with `fault <point>`, reconnect, and complete The Last Signal normally with a real compatible player.
4. Confirm the runner retained the exact boundary marker and Minecraft child exit-86 evidence.
5. Start `recover <point>` on the same world with no fault property.
6. Reconnect the same player and record `/shadowslave soul` plus `/shadowslave nightmare_status` and the retained receipt/instance identity available from the current inspection surface.
7. Relog once more without the property and repeat the state observations before stopping normally.
8. Run `verify <point> <instance-uuid>` against the retained logs.

Expected after both recovery logins:

- status is `dreamer`;
- Soul Rank is `dormant`;
- Aspect is `Last Light` at `awakened` Aspect Rank;
- Flaw is `Cold Ash`;
- no active Nightmare instance remains;
- the same successful-completion receipt/instance remains authoritative through recovery;
- the instance has exactly one preview-appraisal completion marker;
- the instance has exactly one successful-completion teardown marker;
- the second relog adds neither marker;
- another First Nightmare remains refused until explicit `preview_reset` clears the retained receipt.

## Evidence record

```text
Commit/JAR:
Fault point:
Intentional halt marker source: console / latest.log
Observed Minecraft child exit code: 86 / other
Gradle wrapper exit status:
Soul state after first recovery login:
Nightmare status after first recovery login:
Completion receipt/instance after first recovery login:
Soul state after second relog:
Nightmare status after second relog:
Completion receipt/instance after second relog:
Receipt instance UUID stable: YES / NO
Successful-completion teardown marker count:
Appraisal marker count:
Result: PASS / FAIL / BLOCKED
```

A normal shutdown, thrown command exception, simulated in-memory crash, or timing-based external kill is not evidence for one of these exact boundaries. A row is also not a pass if the fault property remains enabled during recovery or if same-world/same-player identity cannot be demonstrated.

## Automation boundary

The repository CI can compile/test and boot NeoForge when a workflow is available, but it does not currently drive a compatible authenticated player through the full interaction/reconnect sequence. The runner therefore automates process and retained-log evidence only. Until a controllable compatible client harness exists, the six-row matrix still requires a real player for terminal interaction and recovery-state observations.
