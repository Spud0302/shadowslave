# Physical successful-completion fault matrix

**Status:** operator-only evidence procedure for issue #34 and PR #39.

**Classification:** the restart transaction and fault injector are Minecraft **DESIGN**. They are not presented as a canonical Nightmare Spell mechanic.

## Safety boundary

The injector is disabled unless the dedicated-server JVM starts with:

```text
-Dshadowslave.completionFault=<fault_point>
```

At the matching boundary, the required save has already completed. The process writes an `INTENTIONAL COMPLETION FAULT` marker and terminates immediately with exit code `86`. Remove the property before restarting. Never use this flag on a production world.

The initial terminal-resolution receipt is already covered by storage integration tests. Physical process termination is exposed for the five later split-save boundaries where player data and overworld SavedData can disagree.

## Fault points

```text
after_appraisal_player_save
after_appraisal_registry_save
after_return_player_save
after_return_registry_save
after_teardown_registry_save
```

## Procedure

For each point, use a fresh copy of the same disposable dedicated-server world and the exact PR JAR.

1. Start without a fault property, run `/shadowslave preview_reset`, then `/shadowslave preview_begin`.
2. Stop normally before completing the signal fire.
3. Restart with exactly one fault property, reconnect, and complete the signal fire.
4. Confirm the server exits with code `86` and the final log includes the exact configured boundary.
5. Remove the property and restart the same world.
6. Reconnect the same player and record `/shadowslave soul` and `/shadowslave nightmare_status`.
7. Stop and restart once more without the property, reconnect, and repeat the observations.

Expected after both recovery restarts:

- status is `dreamer`;
- Soul Rank is `dormant`;
- Aspect is `Last Light` at `awakened` Aspect Rank;
- Flaw is `Cold Ash`;
- no active Nightmare instance remains;
- the retained completion receipt is at `TEARDOWN_COMMITTED`;
- the instance has exactly one completed teardown marker;
- no second appraisal mutation or conflicting identity appears;
- another First Nightmare remains refused until explicit `preview_reset` clears the retained receipt.

## Evidence record

```text
Commit/JAR:
Fault point:
Intentional halt marker:
Observed exit code:
Soul state after first recovery login:
Registry state after first recovery login:
Soul state after second restart:
Registry state after second restart:
Teardown marker count:
Appraisal marker count:
Result: PASS / FAIL / BLOCKED
```

A normal shutdown, thrown command exception, or simulated in-memory crash is not physical process-fault evidence. A run is also not a pass if the fault property remains enabled during recovery.
