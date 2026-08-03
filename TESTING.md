# Shadow Slave — current testing

**Target:** `0.1.0-preview.2` on PR #19  
**Runtime source:** `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`  
**Detailed matrix:** `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`  
**Integration procedures:** `docs/JAVA-INTEGRATION-TEST-PROCEDURES.md`<br>
**Artifact provenance:** `docs/PLAYABLE-PREVIEW-PROVENANCE.md`

The previous datapack-era and alpha testing history remains available in Git history and is referenced under `docs/history/`.

## Corrected Java checkpoint

GitHub Actions `Java core` run 34 / ID `30686670446` completed successfully.

Passed:

- Gradle wrapper validation;
- compilation and expanded JUnit suite;
- physical NeoForge client startup marker;
- dedicated NeoForge server ready marker;
- JAR packaging;
- artifact upload.

```text
archive SHA-256  a7ee670001042ee9c783ceb191e667fefdf043acd1b6fa498438434907291d79
JAR SHA-256      48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

## Frozen datapack gate — pending

Changes for issues #20, #21, and #26 touch the frozen datapack source and its harness. They require a deployed Minecraft 1.21.1 server:

```bash
cd testserver
npm run deploy
npm test
```

The expected gate includes lifecycle 32/32, Flaw 39/39, and the new two-player serialization regression. Merely wiring the regression into `npm test` is not evidence that it passed.

## Local Java gate

Use JDK 21.

```bash
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
python3 shadowslave/tools/validate.py
```

Do not use bare `runClientSmoke` or `runServerSmoke` task exit codes as proof. They can return success even when Minecraft never reaches the accepted readiness marker.

## Relog, restart and multiplayer evidence

Use `docs/JAVA-INTEGRATION-TEST-PROCEDURES.md` for the repeatable physical-client and dedicated-server procedures.

The Java suite now includes storage/network integration checks that:

- round-trip permanent Soul, revealed identity and preview cooldown through the persistent NBT codecs;
- rebuild and stream-round-trip the authoritative owning-client snapshot;
- save and reload two independent active Nightmare owners through the real overworld `SavedData` shape;
- reject duplicate owners and instance IDs;
- consume one exact instance only once without touching another player;
- prevent stale teardown from deleting a newer instance for the same player.

These checks are not a simulated logout, process restart or multiplayer server. Physical relog, dedicated restart and two-client isolation remain separate evidence and must not be marked passed until the documented procedures are performed.

The success path has a confirmed restart gap: teardown removes the active-instance record before appraisal is persisted. A crash in that narrow interval can leave an Aspirant with no active instance and no recoverable completion phase. The procedures record this honestly rather than using a timing-based fake restart test.

## Correction coverage

The expanded suite covers:

- invalid Soul combinations return codec data errors rather than throwing through parse;
- invalid/future schema rejection and explicit schema-1 migration;
- schema-2 state is not silently repaired as schema 1;
- post-First-Nightmare Spell states retain permanent Aspect/Flaw identity;
- completed legacy state requires the retained Carrier tag;
- generated Aspect/Flaw scores require matching mechanics tags;
- existing Soul, migration, snapshot, identity, Nightmare-record, power-state, restart-storage and owner-isolation coverage.

The datapack harness additionally needs to prove:

- reset restores an enterable health baseline;
- Alice can own the global trial slot;
- Bob is refused before shared state is created;
- Alice can complete normally;
- Bob can enter after teardown releases the slot.

## Required manual playtest — not yet performed

Use a disposable Minecraft 1.21.1 NeoForge 21.1.244 world.

1. Install only `shadowslave-0.1.0-preview.2.jar` and remove `preview.1`.
2. Join and press **O**; confirm Uninfected state and no invented Soul Rank.
3. Run `/shadowslave preview_begin`; confirm development-shortcut wording.
4. Confirm Carrier -> Aspirant with Dormant Soul Rank on entry.
5. Confirm The Last Signal builds safely in the separate Nightmare slot.
6. Confirm role and central conflict are understandable.
7. Confirm the Husk creates pressure but is not required for completion.
8. Right-click the unlit soul campfire and confirm exactly one completion/return.
9. Confirm return to the original dimension and practical location.
10. Confirm Dreamer/Sleeper, Dormant Soul Rank, Last Light, Awakened Aspect Rank, Kindle, and Cold Ash.
11. Test Kindle effect, cooldown refusal, and reuse.
12. Test Cold Ash in water/rain/bubbles and clearing after leaving.
13. Perform Procedures R, S and M in `docs/JAVA-INTEGRATION-TEST-PROCEDURES.md`.
14. Test technical recovery wording and state.
15. Die in the Nightmare; confirm cleanup and development-accommodation wording.
16. Run preview reset and confirm all Java state clears.
17. On a backed-up legacy world, verify exact migration, retained evidence, bad-state rejection, and idempotency.

No document may describe these checks as passed until someone records the result.

## Future completion-engine tests

Before mature Nightmare/Seed completion, `docs/NIGHTMARE-SEED-ROADMAP.md` requires tests proving:

- a boss may die without resolving the conflict;
- a non-combat event may resolve it;
- prerequisites can reject an objective interaction;
- multiple event paths reach different terminal resolutions;
- another actor can cause world-driven resolution;
- per-challenger outcomes remain separate from global resolution;
- appraisal runs only after completion is recorded;
- teardown/rewards remain idempotent across reload;
- one shared multiplayer resolution can produce separate challenger outcomes.

## Verdict vocabulary

Claude's bulk review should record one:

- **verified**;
- **verified with fixes**;
- **blocked**;
- **verified machine-checkably; human feedback pending**.
