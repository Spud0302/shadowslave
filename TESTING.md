# Shadow Slave — current testing

**Target:** `0.1.0-preview.2` on merged `main@c3ffcd9`  
**Runtime source:** `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`  
**Detailed matrix:** `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`  
**Artifact provenance:** `docs/PLAYABLE-PREVIEW-PROVENANCE.md`

The previous datapack-era and alpha testing history remains available in Git history and under `docs/history/`.

## Independent machine verification

Claude verified the merge result rather than trusting an earlier branch or workflow:

| Gate | Result |
| --- | --- |
| validator | clean |
| frozen-datapack lifecycle | 32/32 |
| frozen-datapack Flaw suite | 39/39 |
| disconnect/reconnect trial-lock regression | PASS, exit 0 |
| regression repeatability | passed twice back to back |
| cleanup after regression | global lock 0; no stray creature |
| Java unit suite | 35 tests, 0 failures |
| physical-client smoke | PASS through `mod/verify-smoke.sh` |
| dedicated-server smoke | PASS through `mod/verify-smoke.sh` |

The merged workflow also gates Java and frozen-datapack compatibility together for future relevant changes.

## Reproduction commands

Use JDK 21.

```bash
cd testserver
npm run deploy
npm test

cd ..
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
python3 shadowslave/tools/validate.py
```

The datapack server loads a built/deployed ZIP, not the working tree. Running `npm test` without deployment can silently exercise an old build.

Do not use bare `runClientSmoke` or `runServerSmoke` task exit codes as proof. Use `mod/verify-smoke.sh`, which checks readiness markers.

## Automated coverage

The suite covers:

- Soul defaults, ranks, paths, and progression boundaries;
- invalid Soul combinations returning codec data errors;
- explicit schema-1 migration and invalid/future schema rejection;
- schema-2 records not receiving schema-1 repairs;
- post-First-Nightmare Spell states retaining Aspect/Flaw identity;
- bounded Soul/identity client snapshots;
- absent legacy score versus explicit stored zero;
- exact frozen-datapack mappings and strict mechanics-tag validation;
- migration read-back verification, rollback, and idempotency;
- persistent Nightmare owner/return/layout records;
- separate scenario-slot coordinates;
- preview ability cooldown guards;
- physical-client and dedicated-server startup compatibility;
- datapack one-slot ownership across disconnect/reconnect and teardown.

## Required manual playtest — pending

Use a disposable Minecraft 1.21.1 NeoForge 21.1.244 world.

1. Install only `shadowslave-0.1.0-preview.2.jar`.
2. Join and press **O**; confirm Uninfected state and no invented Soul Rank.
3. Run `/shadowslave preview_begin`; confirm development-shortcut wording.
4. Confirm Carrier -> Aspirant with Dormant Soul Rank on entry.
5. Confirm The Last Signal builds safely in a separate Nightmare slot.
6. Confirm the role and central conflict are understandable in game.
7. Confirm the Husk creates pressure but is not required for completion.
8. Right-click the unlit soul campfire and confirm exactly one completion/return.
9. Confirm return to the original dimension and practical location.
10. Confirm Dreamer/Sleeper, Dormant Soul Rank, Last Light, Awakened Aspect Rank, Kindle, and Cold Ash.
11. Test Kindle effect, cooldown refusal, and later reuse.
12. Test Cold Ash in water/rain/bubbles and clearing after leaving.
13. Quit/reload at Carrier, active-Nightmare, and Dreamer stages.
14. Test technical recovery wording and state.
15. Die in the Nightmare; confirm cleanup and development-accommodation wording.
16. Run preview reset and confirm all Java state clears.
17. With two Java-preview players, verify separate slots and independent outcomes.
18. On a backed-up legacy world, verify exact migration, retained evidence, bad-state rejection, and idempotency.

Deferred is not passed. Record concrete observations before changing gameplay or presentation.

## Open regression work

- **#20:** keep `testserver/defect_issue20_stray_creature.mjs` outside the gate while the frozen prototype still uses global creature selectors. The supported one-slot contract is verified; the manually induced selector defect remains documented.
- **#29:** add malformed-input tests for every registered persisted attachment codec. An invariant violation must return `DataResult.error`, never throw through player loading.

## Future completion-engine tests

Before mature Nightmare/Seed completion, `docs/NIGHTMARE-SEED-ROADMAP.md` requires tests proving:

- a boss may die without resolving the conflict;
- a non-combat event may resolve it;
- prerequisites can reject an objective interaction;
- multiple paths reach different terminal resolutions;
- another actor can cause world-driven resolution;
- per-challenger outcomes remain separate from global resolution;
- appraisal runs only after completion is recorded;
- teardown and rewards remain idempotent across reload;
- one shared multiplayer resolution can produce separate challenger outcomes.
