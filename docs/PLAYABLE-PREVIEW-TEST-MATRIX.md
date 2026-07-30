# Playable preview bulk test matrix

**Target:** `0.1.0-preview.1` on PR #19  
**Review mode:** accumulated bulk Claude review after GPT's playable-preview checkpoint  
**Human status:** Andrew has not yet played this build; deferred is not passed

## Automated evidence already accumulated

| Area | Claim | Evidence/criterion | Status before final checkpoint |
| --- | --- | --- | --- |
| Java build | Sources compile on JDK 21 / NeoForge 21.1.244 | `./mod/gradlew -p mod build` | Passed at alpha.5 and alpha.6 checkpoints |
| Unit tests | Domain, codecs, migration and snapshot rules pass | Gradle JUnit suite | Passed at alpha.5 and alpha.6 checkpoints |
| Physical client | Client links without dedicated-server side leakage | marker-based workflow client smoke | Passed at alpha.5 and alpha.6 checkpoints |
| Dedicated server | Server loads mod and bundled data without client classes | marker-based workflow server smoke | Passed at alpha.5 and alpha.6 checkpoints |
| Packaging | Installable JAR is produced | workflow artifact upload | Passed at alpha.5 and alpha.6 checkpoints |
| Legacy read | Absent score maps deliberately to sentinel; explicit zero fails | `LegacyDatapackReaderTest` | Covered |
| Pure translation | Frozen Carrier/completed/legacy mappings remain exact and fail closed | existing `DatapackMigrationTranslatorTest` | Covered |
| Imported metadata | Full imported Aspect/Flaw metadata round-trips | `ImportedIdentityDataTest` | Covered |
| Transaction verification | Wrong Soul/metadata read-back is rejected | `DatapackMigrationPersistenceVerifierTest` | Covered |
| Revealed identity | Aspect/Flaw records round-trip and cannot be half-present | `SoulIdentityDataTest` | Covered |
| Network | Names, independent ranks, ability and Flaw effect round-trip in bounded payload | `SoulSnapshotTest` | Covered |
| Nightmare persistence record | Owner, role, conflict IDs, return data and pursuer ID round-trip through NBT | `NightmareInstanceTest` | Covered |
| Per-player layout | Different slots do not share the same origin | `NightmareInstanceTest` | Covered |
| Ability state | Kindle cooldown codec and negative guard | `PreviewPowerDataTest` | Covered |

## Final automated checkpoint

Claude should reproduce rather than trust this document:

```bash
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
python3 shadowslave/tools/validate.py
```

Expected artifact:

```text
mod/build/libs/shadowslave-0.1.0-preview.1.jar
```

Verify that the final GitHub workflow:

- compiles and runs all unit tests;
- reaches the accepted physical-client GUI marker;
- reaches the dedicated-server `Done (...)!` marker with Shadow Slave loaded;
- uploads exactly the preview JAR;
- contains the bundled Nightmare dimension, dimension type and biome resources.

## Claude code-review matrix

### Live migration

- Reader uses direct scoreboard API rather than parsing command/chat output.
- Missing objective and missing player score are mapped to absence deliberately.
- Explicit stored `0`, negative values, active Nightmare state and inconsistent identities fail closed.
- Existing non-empty Java Soul/identity is never overwritten.
- Writes happen provisionally; exact read-back precedes final migration marker.
- All Java attachments roll back on failure.
- Legacy scores/tags are never removed in this batch.
- Imported formal names and IDs agree with frozen datapack mappings.

### Nightmare lifecycle

- `NightmareService.tryEnter` is the only accepted preview entry choke point.
- One active instance per player is enforced in persistent Overworld `SavedData`.
- Temporary role, layout, pursuer and recovery data live on `NightmareInstance`, not `SoulData`.
- Each player receives a separate slot.
- The scenario conflict is lighting the signal; pursuer death is not the completion definition.
- Success, technical recovery, admin abort and canonical death converge on owned-entity/registry teardown.
- Return dimension/position are stored before entry.
- Technical recovery is labelled out of world.
- Ordinary death does not masquerade as safe ejection.
- Login with a valid active instance resumes instructions; dimension mismatch uses technical recovery.

### Appraisal and powers

- Fixed preview appraisal is visibly labelled DESIGN.
- Dreamer Soul Rank remains Dormant while Aspect Rank is Awakened.
- `SoulIdentityData` holds full permanent identity; `SoulData` holds authoritative IDs/ranks.
- Appraisal failure cannot leave a half-identity; it recovers to Carrier after lifecycle teardown.
- Kindle is server-authoritative and cooldown-backed.
- Cold Ash is server-authoritative and mechanically applied in water/rain/bubbles.
- Client payload cannot submit progression, identity or cooldown values.

### Side and multiplayer safety

- No common/server class imports client GUI classes.
- Every instance lookup is by owner UUID; selectors/tags are not global ownership.
- Owned pursuer cleanup targets the stored UUID only.
- Slot allocation remains unique after SavedData reload.
- Two players entering concurrently cannot share registry ownership or scenario coordinates.

## Manual play matrix — not yet performed

Use a disposable Minecraft 1.21.1 NeoForge world.

1. Install only the preview JAR and confirm the Mods screen lists Shadow Slave.
2. Join without cheats and press **O**; confirm fresh Uninfected state.
3. Run `/shadowslave preview_begin`; confirm the command clearly labels itself a development shortcut.
4. Confirm Carrier transitions to Aspirant/Dormant on entry.
5. Confirm The Last Signal builds around the player without obvious suffocation or void placement.
6. Confirm role and objective text is understandable.
7. Confirm the pursuer applies pressure but killing it is not required.
8. Right-click the unlit soul campfire; confirm return to the exact original dimension/location.
9. Confirm Dreamer/Dormant, Last Light/Awakened and Cold Ash appear in the Soul screen.
10. Run `/shadowslave kindle`; confirm visibility/speed, cooldown refusal and later reuse.
11. Enter water/rain; confirm Cold Ash applies Weakness and clears naturally after leaving.
12. Quit and reload after Carrier, Aspirant and Dreamer stages where practical; confirm persistence.
13. Use `/shadowslave nightmare_recover`; confirm Carrier recovery and explicit technical wording.
14. Die in the Nightmare; confirm normal Minecraft death presentation, instance cleanup and explicit non-canon accommodation wording.
15. Run `/shadowslave preview_reset`; confirm Soul and identity clear on the next O-screen open.
16. With two players, enter simultaneously and confirm separate slots and independent completion.
17. On a backed-up legacy datapack world, run `/shadowslave migrate_datapack` as operator and verify names, ranks, retained legacy evidence and idempotent second invocation.

## Known deferred risks

- The complete interaction loop has not been run by a human.
- No GameTest currently simulates a real player logout/login round trip.
- `SavedData` persistence is exercised through record NBT tests and dedicated-server loading, not a full
  active-instance server restart scenario.
- A vanilla Husk is a pressure placeholder, not custom Nightmare Creature AI.
- Imported identities persist, but all imported mechanics are not yet implemented.
- Visual layout, pacing, balance and readability require Andrew's play feedback.

## Verdict vocabulary

Claude should finish the bulk review with exactly one:

- **verified** — accepted as built;
- **verified with fixes** — fixes committed and evidence repeated;
- **blocked** — exact defects or missing evidence recorded;
- **verified machine-checkably; human feedback pending** — permitted only if every machine gate passes and
  unperformed play criteria remain stated as unperformed.
