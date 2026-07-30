# Playable preview bulk test matrix

**Target:** `0.1.0-preview.1` on PR #19  
**Source:** `460cd31f135ae7e98f66890b6bbf60414772d57b`  
**Review mode:** accumulated bulk Claude review  
**Human status:** Andrew has not yet played this build; deferred is not passed

## Final GPT automated checkpoint

GitHub Actions `Java core` run **33** / ID **30555343642** completed **successfully**.

| Area | Evidence | Result |
| --- | --- | --- |
| Wrapper | Gradle wrapper validation | passed |
| Java build and unit tests | `./mod/gradlew -p mod build --stacktrace` in workflow | passed |
| Physical client | accepted Shadow Slave client + GUI-atlas startup markers | passed |
| Dedicated server | accepted Shadow Slave load + `Done (...)!` ready markers | passed |
| Packaging | `shadowslave-0.1.0-preview.1.jar` | passed |
| Artifact upload | artifact `shadow-slave-java-core`, ID `8764632229` | passed |

Downloaded artifact verification:

```text
archive SHA-256  dd6315fd25ad50bbba09c53433e8b1840a2f70b344b18a425533c4856da3a8e8
JAR SHA-256      600fa2143879f8f269aec6d048a0fa4b3150f808a091c1527fe34067d9cdd867
```

See `docs/PLAYABLE-PREVIEW-PROVENANCE.md` for exact sizes, timestamps and source linkage.

Claude must reproduce rather than trust this record:

```bash
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
python3 shadowslave/tools/validate.py
```

## Automated domain coverage

| Rule | Test/evidence |
| --- | --- |
| Absent legacy score is distinguished from explicit stored zero | `LegacyDatapackReaderTest` |
| Frozen Carrier/completed/legacy mappings remain exact and fail closed | `DatapackMigrationTranslatorTest` |
| Imported Aspect/Flaw metadata codec round-trip | `ImportedIdentityDataTest` |
| Wrong migration read-back is rejected | `DatapackMigrationPersistenceVerifierTest` |
| General revealed identity codec and paired presence | `SoulIdentityDataTest` |
| Independent ranks, names, ability and Flaw effect use bounded payload | `SoulSnapshotTest` |
| Nightmare owner/role/return/layout/pursuer NBT round-trip | `NightmareInstanceTest` |
| Different instance slots have different origins | `NightmareInstanceTest` |
| Kindle cooldown codec and negative guard | `PreviewPowerDataTest` |
| Bundled dimension resources load on a fresh dedicated-server smoke | workflow run 33 |

## Claude code-review matrix

### Live migration

- Reader uses direct scoreboard API, not command/chat parsing.
- Missing objective and missing player score map to absence deliberately.
- Explicit `0`, negative values, active Nightmare state and inconsistent identities fail closed.
- Established Java Soul/identity is not overwritten.
- Provisional writes and exact read-back precede the final migration marker.
- Soul, general identity and imported metadata roll back together on failure.
- Legacy scores/tags are not removed.
- Imported names/IDs/ranks agree with frozen datapack mappings.

### Nightmare lifecycle

- `NightmareService.tryEnter` is the only preview entry choke point.
- Persistent Overworld `SavedData` enforces one active instance per owner UUID.
- Role, scenario, layout, pursuer and recovery state live on `NightmareInstance`.
- Per-player slots do not share coordinates.
- Lighting the signal resolves the conflict; pursuer death is not completion.
- Success, technical recovery, admin abort and canonical death converge on one owned-entity/registry teardown.
- Return dimension/position are captured before entry.
- Technical recovery is labelled out of world.
- Canonical death is not presented as safe ejection.
- Reconnect resumes a valid in-dimension instance; mismatch invokes technical recovery.

### Appraisal, identity and powers

- Fixed preview appraisal is explicitly DESIGN.
- Dreamer Soul Rank is Dormant while Aspect Rank is Awakened.
- `SoulIdentityData` owns full permanent identity; `SoulData` owns authoritative IDs/ranks.
- Appraisal failure cannot leave a half-identity and recovers to Carrier.
- Kindle and its cooldown are server-authoritative.
- Cold Ash is server-authoritative and applies Weakness in water/rain/bubbles.
- Client payload cannot submit progression, identity or cooldown values.

### Side and multiplayer safety

- Common/server classes do not import client GUI classes.
- Ownership lookup is by player/instance UUID rather than global tags/selectors.
- Pursuer cleanup targets only the stored entity UUID.
- Slot allocation remains unique after SavedData load.
- Concurrent players cannot share registry ownership or coordinates.

## Manual play matrix — not yet performed

Use a disposable Minecraft 1.21.1 NeoForge 21.1.244 world.

1. Install only the preview JAR and confirm Shadow Slave is listed.
2. Join without cheats and press **O**; confirm Uninfected state.
3. Run `/shadowslave preview_begin`; confirm explicit development-shortcut wording.
4. Confirm Carrier -> Aspirant/Dormant on entry.
5. Confirm The Last Signal builds without suffocation/void placement.
6. Confirm role and conflict are understandable in game.
7. Confirm the pursuer creates pressure but is not required for completion.
8. Right-click the unlit soul campfire; confirm exact return dimension/location.
9. Confirm Dreamer/Dormant, Last Light/Awakened, Kindle and Cold Ash on the Soul screen.
10. Run `/shadowslave kindle`; confirm effect, cooldown refusal and later reuse.
11. Enter water/rain; confirm Weakness applies and expires after leaving.
12. Quit/reload at practical Carrier, Aspirant and Dreamer points; confirm persistence.
13. Test `/shadowslave nightmare_recover` and its technical wording.
14. Die in the Nightmare; confirm cleanup and explicit development accommodation wording.
15. Run `/shadowslave preview_reset`; reopen O-screen and confirm cleared state.
16. With two players, confirm separate slots and independent completion.
17. On a backup legacy world, test `/shadowslave migrate_datapack`, retained evidence and idempotency.

## Known deferred risks

- No human has run the complete interaction loop.
- No GameTest simulates a real player logout/login round trip.
- Active-instance restart recovery is not end-to-end automated.
- The pursuer is a vanilla Husk placeholder.
- Full mechanics for every imported identity are not implemented.
- Visual layout, pacing, balance and readability need Andrew's feedback.

## Bulk verdict vocabulary

Claude should record exactly one:

- **verified**;
- **verified with fixes**;
- **blocked**;
- **verified machine-checkably; human feedback pending**.
