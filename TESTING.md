# Shadow Slave — current testing

**Target:** `0.1.0-preview.1` on draft PR #19  
**Detailed matrix:** `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`  
**Artifact provenance:** `docs/PLAYABLE-PREVIEW-PROVENANCE.md`

The previous datapack-era and alpha.4 testing history remains available in Git history and is
referenced under `docs/history/`. This file now describes the current preview gate.

## Automated checkpoint

GitHub Actions `Java core` run 33 / ID `30555343642` completed successfully for source commit
`460cd31f135ae7e98f66890b6bbf60414772d57b`.

Passed:

- Gradle wrapper validation;
- compilation and JUnit suite;
- physical NeoForge client startup marker;
- dedicated NeoForge server ready marker;
- JAR packaging;
- artifact upload.

Checksums:

```text
archive SHA-256  dd6315fd25ad50bbba09c53433e8b1840a2f70b344b18a425533c4856da3a8e8
JAR SHA-256      600fa2143879f8f269aec6d048a0fa4b3150f808a091c1527fe34067d9cdd867
```

## Local machine gate

Use JDK 21.

```bash
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
python3 shadowslave/tools/validate.py
```

Do not use bare `runClientSmoke` or `runServerSmoke` task exit codes as proof. They can return success
even when Minecraft never reaches the accepted readiness marker.

## Automated domain coverage

The suite covers:

- Soul schema/defaults and progression boundaries;
- independent Soul Rank and Aspect Rank;
- bounded server-to-client Soul/identity snapshots;
- imported and native identity codec invariants;
- frozen datapack score/name mappings;
- absent score versus explicit zero handling;
- migration read-back verification and rollback rules;
- Nightmare instance NBT ownership/return/layout data;
- separate scenario-slot coordinates;
- preview ability cooldown persistence guards;
- client/server side startup compatibility.

## Required manual playtest — not yet performed

Use a disposable Minecraft 1.21.1 NeoForge 21.1.244 world.

1. Install only `shadowslave-0.1.0-preview.1.jar` and confirm Shadow Slave is listed.
2. Join without cheats and press **O**; confirm Uninfected state and no invented Soul Rank.
3. Run `/shadowslave preview_begin`; confirm it labels infection as a development shortcut.
4. Confirm Carrier transitions to Aspirant with Dormant Soul Rank on entry.
5. Confirm The Last Signal builds safely in the separate Nightmare slot.
6. Confirm role and central conflict are understandable in game.
7. Confirm the Husk creates pressure but killing it is not required.
8. Right-click the unlit soul campfire and confirm exactly one completion/return.
9. Confirm return to the original dimension and practical location.
10. Open **O** and confirm Dreamer/Sleeper, Dormant Soul Rank, Last Light, Awakened Aspect Rank, Kindle,
    and Cold Ash.
11. Run `/shadowslave kindle`; confirm effect, cooldown refusal, and later reuse.
12. Enter water/rain/bubbles; confirm Weakness applies and clears after leaving.
13. Quit/reload at practical Carrier, Aspirant, and Dreamer stages; confirm persistence.
14. Test `/shadowslave nightmare_recover`; confirm Carrier recovery and explicit technical wording.
15. Die in the Nightmare; confirm cleanup and explicit development-accommodation wording.
16. Run `/shadowslave preview_reset`; confirm Soul, identity, power state, and active instance clear.
17. With two players, enter simultaneously and verify separate slots and independent outcomes.
18. On a backup frozen-datapack world, run `/shadowslave migrate_datapack`; verify exact names/ranks,
    retained legacy evidence, rollback on bad state, and idempotent second invocation.

No document may describe these manual checks as passed until someone records the result.

## Future completion-engine tests

Before mature Nightmare/Seed completion, `docs/NIGHTMARE-SEED-ROADMAP.md` requires tests proving:

- a boss may die without resolving the conflict;
- a non-combat event may resolve it;
- prerequisites can reject an objective interaction;
- multiple event paths reach different terminal resolutions;
- another actor can cause world-driven resolution;
- per-challenger survival/outcomes remain separate from global resolution;
- appraisal runs only after completion is recorded;
- teardown/rewards remain idempotent across reload;
- one shared multiplayer resolution can produce separate challenger outcomes.

## Verdict vocabulary

Claude's bulk review should record one:

- **verified**;
- **verified with fixes**;
- **blocked**;
- **verified machine-checkably; human feedback pending**.
