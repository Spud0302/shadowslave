# Playable preview test matrix

**Target:** `0.1.0-preview.2` on merged `main@c3ffcd9`  
**Runtime source:** `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`  
**Machine verdict:** verified  
**Human status:** Andrew play feedback pending; deferred is not passed

## Independent machine result

Claude verified the merge result rather than trusting an earlier workflow or branch:

| Area | Result |
| --- | --- |
| Validator | clean |
| Frozen-datapack lifecycle | 32/32 |
| Frozen-datapack Flaw suite | 39/39 |
| Disconnect/reconnect slot regression | PASS, exit 0 |
| Regression repeatability | passed twice back to back |
| Cleanup after regression | `$global ss_trial_lock = 0`; no stray trial creature |
| Java clean build | 35 tests, 0 failures |
| Physical client | PASS through `mod/verify-smoke.sh` |
| Dedicated server | PASS through `mod/verify-smoke.sh` |

## Artifact

```text
shadowslave-0.1.0-preview.2.jar
SHA-256 48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

Runtime source is unchanged by later datapack, harness, workflow, and documentation commits.

## Reproduction commands

```bash
cd testserver
npm run deploy
npm test

cd ..
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
python3 shadowslave/tools/validate.py
```

Use JDK 21. Deploy before running the datapack harness. Use readiness-marker smokes rather than bare Gradle task exit codes.

## Verified correction matrix

| Issue | Verified result |
| --- | --- |
| #20 supported path | persistent one-slot lock survives disconnect; second entrant refused; owner resumes and completes; teardown releases slot |
| #21 | authoritative docs state the one-active-trial ceiling and its recovery boundary |
| #22 | malformed Soul records return `DataResult.error`; raw invariant exceptions do not escape decode |
| #23 | invalid/future schemas reject; schema 1 migrates explicitly; schema 2 receives no schema-1 repair |
| #24 | post-First-Nightmare Spell states require Aspect, Aspect Rank, and Flaw identity |
| #25 | inconsistent completed/generated legacy evidence rejects fail closed |
| #26 | `test/reset` restores an enterable health baseline |

## Existing automated domain coverage

- Soul defaults, ranks, paths, and transition boundaries;
- schema migration and malformed-record rejection;
- bounded server-to-client snapshots;
- native/imported identity pairing and codec round trips;
- absent legacy score versus explicit stored zero;
- exact frozen-datapack mappings and mechanics-tag validation;
- provisional migration writes, exact read-back, rollback, and idempotency;
- Nightmare ownership, return state, layout, pursuer UUID, and separate slots;
- Kindle cooldown persistence guards;
- client/server side startup compatibility;
- frozen-datapack disconnect/reconnect ownership and cleanup.

## Manual Java play matrix — pending

Use a disposable Minecraft 1.21.1 NeoForge 21.1.244 world.

1. Install only `shadowslave-0.1.0-preview.2.jar` and confirm the mod is listed.
2. Join and press **O**; confirm Uninfected state and no invented Soul Rank.
3. Run `/shadowslave preview_begin`; confirm development-shortcut wording.
4. Confirm Carrier -> Aspirant/Dormant on entry.
5. Confirm The Last Signal builds without suffocation or void placement.
6. Confirm the role and conflict are understandable in game.
7. Confirm the Husk creates pressure but is not required for completion.
8. Right-click the unlit soul campfire; confirm exactly-once completion and return location.
9. Confirm Dreamer/Dormant, Last Light/Awakened, Kindle, and Cold Ash on the Soul screen.
10. Test Kindle effect, cooldown refusal, and later reuse.
11. Test Cold Ash in water/rain/bubbles and clearing after leaving.
12. Quit/reload at Carrier, active-Nightmare, and Dreamer stages.
13. Test `/shadowslave nightmare_recover` and technical wording.
14. Die in the Nightmare; confirm cleanup and development-accommodation wording.
15. Run `/shadowslave preview_reset`; confirm cleared state.
16. With two Java-preview players, confirm separate slots and independent completion.
17. On a backed-up legacy world, test migration, retained evidence, bad-state rejection, and idempotency.
18. Record pacing, presentation, readability, balance, and general feel.

## Open regression work

### #20 — retained frozen-datapack defect probe

`testserver/defect_issue20_stray_creature.mjs` remains outside `npm test`. It deliberately proves that a manually introduced unrelated entity carrying the prototype-global `ss_creature` tag can affect the frozen objective. The supported one-slot contract is verified; this architectural limitation remains open and explicit.

### #29 — persisted codec sweep

Add well-formed invariant-violating payload tests for every registered persisted attachment codec. Each must return `DataResult.error` rather than throw. `PreviewPowerData` is the confirmed failing case for corrupt negative cooldown data.

## Future completion-engine tests

Before mature Nightmare/Seed completion, `docs/NIGHTMARE-SEED-ROADMAP.md` requires tests proving:

- a boss may die without resolving the conflict;
- a non-combat event may resolve it;
- prerequisites can reject an objective interaction;
- multiple event paths reach different terminal resolutions;
- another actor can cause world-driven resolution;
- per-challenger outcomes remain separate from global resolution;
- appraisal runs only after completion is recorded;
- teardown and rewards remain idempotent across reload;
- one shared multiplayer resolution can produce separate challenger outcomes.
