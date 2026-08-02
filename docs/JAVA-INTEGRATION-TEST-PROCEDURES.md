# Java relog, restart and multiplayer integration procedures

**Status:** repeatable evidence plan for the playable Java preview.  
**Scope:** persisted Soul state, active Nightmare recovery, exactly-once teardown/appraisal, and two-player isolation.  
**Boundary:** this does not rewrite or close the frozen datapack limitation in Issue #20.

## Evidence vocabulary

Use the narrowest truthful label for every result.

| Label | Meaning in this repository |
| --- | --- |
| Unit | Pure domain or codec behaviour without a running Minecraft server. |
| Storage/network integration | Multiple real persistence and payload boundaries exercised together in JUnit, but without a player login or server process restart. |
| GameTest | NeoForge GameTest executed inside a game server. No relog/restart GameTest exists yet. |
| Physical client | A real NeoForge client reached the accepted resource/UI startup markers or was used for interaction. |
| Dedicated-server integration | A real dedicated server plus one or more real clients performed the described lifecycle. |
| Manual | Human-observed steps with exact commands, saved logs and expected observations. |

Do not call a codec round-trip a relog test. Do not call a reconstructed `SavedData` object a dedicated-server restart. Do not call two in-memory records multiplayer end-to-end evidence.

## Automated coverage added by this work

Run the focused checks with JDK 21:

```bash
./mod/gradlew -p mod test \
  --tests dev.spud.shadowslave.integration.PersistedSoulStateIntegrationTest \
  --tests dev.spud.shadowslave.nightmare.NightmareRegistryDataTest
```

`PersistedSoulStateIntegrationTest` proves that a completed Dreamer's permanent Soul, revealed Aspect/Flaw identity, independent Soul/Aspect ranks, and preview cooldown survive the same NBT codec boundary used by persistent attachments. It then rebuilds and stream-round-trips the authoritative client snapshot.

`NightmareRegistryDataTest` proves that the real overworld `SavedData` representation:

- saves and reloads two independent owners;
- preserves instance IDs, slots, return records, objectives and exact pursuer UUIDs;
- rejects duplicate instance IDs and duplicate owners without replacing valid state;
- consumes one exact instance only once;
- leaves the other player's instance untouched;
- prevents a stale teardown from deleting a newer instance for the same player;
- advances slot allocation past every restored instance.

These are **storage/network integration** checks. The physical procedures below remain required.

## Common setup

Use a disposable Minecraft Java **1.21.1** world with NeoForge **21.1.244**, the JAR built from the exact commit under test, and no older Shadow Slave JAR. For multiplayer, use two distinct player profiles named below as Alice and Bob.

Keep the dedicated-server console and `logs/latest.log`. Before each scenario, run as each player:

```text
/shadowslave preview_reset
/shadowslave soul
/shadowslave nightmare_status
```

Expected baseline:

```text
Status: uninfected
Soul Rank: —
Aspect: —
Aspect Rank: —
Flaw: —
No active Nightmare instance.
```

Use `/shadowslave soul_screen` or **O** after every login to verify the owning client received the same authoritative values shown by `/shadowslave soul`.

## Procedure R — player relog

### R1: Carrier persistence

1. As an operator, run `/shadowslave infect` for Alice.
2. Run `/shadowslave soul` and record the output.
3. Disconnect Alice normally without stopping the server.
4. Reconnect Alice.
5. Run `/shadowslave soul`, `/shadowslave nightmare_status`, and open the Soul screen.

Expected:

- status remains `carrier` on the `nightmare_spell` path;
- Soul Rank, Aspect, Aspect Rank and Flaw remain absent;
- no active Nightmare exists;
- server text and the client Soul screen agree.

### R2: active Aspirant persistence and recovery

1. Reset Alice, then run `/shadowslave preview_begin`.
2. Before disconnecting, run:

   ```text
   /shadowslave soul
   /shadowslave nightmare_status
   ```

3. Record the instance UUID and slot. Confirm `aspirant` with Dormant Soul Rank.
4. Disconnect Alice while she remains inside the Nightmare dimension.
5. Reconnect Alice without restarting the server.
6. Record `/shadowslave soul` and `/shadowslave nightmare_status` again, then open the Soul screen.

Expected:

- the login message says the active First Nightmare was restored;
- the same instance UUID and slot remain;
- status remains Aspirant and Soul Rank remains Dormant;
- permanent Aspect and Flaw remain absent until appraisal;
- the client screen agrees with the server readout;
- the objective and pursuer remain usable.

### R3: completed Dreamer persistence and authoritative snapshot

1. Complete R2 by right-clicking the unlit soul campfire.
2. Record `/shadowslave soul` and open the Soul screen.
3. Disconnect and reconnect Alice.
4. Repeat both observations.
5. Run `/shadowslave kindle` once to prove the reloaded identity still authorises its preview ability.

Expected before and after relog:

- status is `dreamer`;
- Soul Rank is `dormant`;
- Aspect is `Last Light`;
- Aspect Rank is `awakened`;
- Flaw is `Cold Ash`;
- `/shadowslave nightmare_status` reports no active instance;
- server readout and client screen agree.

The cooldown's NBT persistence is automated. A physical cooldown-across-relog claim must be recorded separately with an observable debug hook; do not infer it merely from a quick second command whose timing could expire during reconnect.

## Procedure S — dedicated-server restart

### S1: active instance reconstruction

1. Reset Alice and run `/shadowslave preview_begin`.
2. Record the instance UUID, slot, Soul state and Alice's position.
3. From the server console run `save-all flush`, then `stop`.
4. Restart the same dedicated-server world with the same JAR.
5. Reconnect Alice.
6. Record `/shadowslave soul`, `/shadowslave nightmare_status`, Alice's position, and the Soul screen.
7. Finish the objective normally.

Expected:

- the same instance UUID and slot are reconstructed from overworld `SavedData`;
- Alice remains in the Nightmare dimension and receives the restored-instance message;
- Soul remains Aspirant/Dormant before completion;
- the arena, objective and exact owned pursuer remain available;
- completion returns Alice once and produces Dreamer identity once.

### S2: safe technical recovery when location and registry disagree

This deliberately creates the documented technical-recovery condition without corrupting files.

1. Reset Alice and run `/shadowslave preview_begin`.
2. Record the instance UUID.
3. From the server console teleport Alice to the overworld while leaving the registry active:

   ```text
   execute in minecraft:overworld run tp Alice 0 100 0
   save-all flush
   stop
   ```

4. Restart the server and reconnect Alice.
5. Run `/shadowslave soul` and `/shadowslave nightmare_status`.

Expected:

- login invokes technical recovery because the persisted owner is outside the Nightmare dimension;
- the exact instance is torn down once;
- Alice is returned to Carrier with no permanent identity;
- no appraisal marker exists for that instance;
- a second `/shadowslave nightmare_recover` is refused because ownership was already consumed.

### S3: exactly-once log checks

Every teardown and successful preview appraisal now emits an instance-keyed marker. Replace `<INSTANCE>` with the UUID recorded by `/shadowslave nightmare_status`.

```bash
INSTANCE='<INSTANCE>'
LOG='logs/latest.log'

test "$(grep -Fc "Nightmare $INSTANCE teardown completed" "$LOG")" -eq 1
test "$(grep -Fc "Preview appraisal completed for Nightmare $INSTANCE" "$LOG")" -eq 1
```

For technical recovery, admin abort or canonical death, require one teardown and zero appraisals:

```bash
test "$(grep -Fc "Nightmare $INSTANCE teardown completed" "$LOG")" -eq 1
test "$(grep -Fc "Preview appraisal completed for Nightmare $INSTANCE" "$LOG")" -eq 0
```

Also require no line matching:

```text
teardown skipped because its ownership was already absent
```

A repeated recovery command must not add another teardown marker. A repeated objective interaction cannot appraise again because the exact ownership record has already been consumed.

## Procedure M — two-player isolation

### M1: independent entry and ownership

1. Reset Alice and Bob.
2. Have Alice run `/shadowslave preview_begin` and record her instance UUID and slot.
3. Have Bob run `/shadowslave preview_begin` and record his instance UUID and slot.
4. Both run `/shadowslave soul` and `/shadowslave nightmare_status`.

Expected:

- both are Aspirant/Dormant;
- instance UUIDs differ;
- slots differ;
- arena origins differ by the slot-based layout spacing;
- each player sees their own objective and pursuer.

### M2: Alice succeeds while Bob remains active

1. Alice right-clicks her unlit soul campfire.
2. Alice records Dreamer identity and no active instance.
3. Bob immediately records his Soul state, instance UUID, slot, objective and pursuer.
4. Apply the S3 log checks to Alice's instance.

Expected:

- Alice has one teardown and one appraisal;
- Bob remains Aspirant in the same recorded instance;
- Bob's objective and owned pursuer are unchanged;
- Bob can still complete or recover independently.

### M3: death, disconnect and recovery do not cross-contaminate

Repeat from a clean setup for each outcome rather than combining ambiguous leftovers:

1. Alice dies inside her Nightmare while Bob stays active.
2. Alice disconnects and reconnects while Bob stays active.
3. Alice runs `/shadowslave nightmare_recover` while Bob stays active.
4. After each Alice outcome, Bob records `/shadowslave soul` and `/shadowslave nightmare_status`, then verifies his objective and pursuer.

Expected in every run:

- Alice's outcome changes only Alice's Soul and instance;
- Bob retains the same instance UUID, slot, Aspirant/Dormant state and owned entities;
- Alice's death/recovery has one teardown and zero appraisals;
- Bob's later success has its own one teardown and one appraisal;
- no marker for one UUID is counted under the other UUID.

## Confirmed limitation: success is not restart-transactional yet

The success path currently performs these operations in order:

```text
teleport to return location
-> remove owned entities and active-instance SavedData
-> complete preview appraisal and permanent Soul/identity writes
```

A process failure after teardown has persisted but before appraisal has persisted can therefore leave an Aspirant with no active instance. Login recovery cannot reconstruct or appraise that already-consumed completion because no persisted completion phase exists.

This is a real restart/recovery defect found by lifecycle inspection. It is a **zero-appraisal crash window**, not evidence of duplicate appraisal. Do not create a flaky test that tries to kill the server between adjacent Java statements. A future implementation needs a persisted lifecycle phase or transaction/outbox-style recovery record and deterministic fault injection.

## Issue #20 boundary

The frozen datapack still uses global selectors and Issue #20 remains open for that residual limitation. This test work does not alter the datapack.

The current Java preview already stores a distinct instance per owner and an exact pursuer UUID, and teardown removes only that UUID plus the exact expected instance. The new automated checks cover registry ownership isolation; Procedure M is still required before claiming physical multiplayer verification. Broader future scenarios with multiple owned entities and objectives should receive their own Java implementation task and GameTest/dedicated-server coverage rather than being folded into the frozen datapack issue.

## Evidence record

For each physical run, record:

```text
Commit/JAR:
Minecraft/NeoForge:
World copy:
Server mode:
Players:
Procedure IDs:
Instance UUIDs and slots:
Commands executed:
Expected observations met:
Exact teardown counts:
Exact appraisal counts:
Relevant log excerpt:
Client screenshots/readouts:
Result: PASS / FAIL / BLOCKED
Defects:
```

A blank field or an unobserved expectation is not a pass.
