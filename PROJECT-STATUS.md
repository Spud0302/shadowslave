# Shadow Slave project status

**Status date:** 2026-08-02  
**Current main:** `c3ffcd9c3f6139817fe84ef3c81d94ceafdda4e3`  
**Java build:** `0.1.0-preview.2`  
**Review state:** merged and Claude-verified machine-checkably  
**Public Java release:** none

## Products

| Product | Current state | Public release |
| --- | --- | --- |
| Vanilla datapack | completed compatibility reference; one active First Nightmare at a time | `datapack-v1.0.0` |
| Java playable preview | merged on `main`; installable and independently verified | development preview only |
| Nightmare Spell modpack | design only | none |

## Playable Java preview

The merged preview contains:

- persistent lore-aligned Uninfected, Carrier, Aspirant, and Dreamer/Sleeper state;
- separate Soul Rank and Aspect Rank;
- expanded read-only O-key Soul screen;
- transactional live frozen-datapack migration with exact read-back and rollback;
- persistent native/imported Aspect and Flaw records;
- persistent per-player Nightmare ownership and separate play-space slots;
- bundled Nightmare dimension;
- DESIGN scenario **The Last Signal** and role **last watchkeeper**;
- central-conflict completion by restoring a signal fire, with combat optional;
- fixed DESIGN appraisal **Last Light / Kindle / Cold Ash**;
- technical recovery, inspection, reset, and migration commands.

## Independent verification

Claude verified the merge result rather than relying on the branch or an earlier workflow:

- validator clean;
- frozen-datapack lifecycle harness **32/32**;
- Flaw harness **39/39**;
- disconnect/reconnect serialization regression **PASS**, exit `0`, repeatable twice;
- persistent global trial lock returned to `0` after cleanup, with no stray trial creature;
- Java clean build with **35 tests, 0 failures**;
- physical-client and dedicated-server smokes both passed through `mod/verify-smoke.sh`.

PR #19 was merged as `c3ffcd9c3f6139817fe84ef3c81d94ceafdda4e3`.

## Artifact

The verified runtime remains:

```text
shadowslave-0.1.0-preview.2.jar
SHA-256 48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

Runtime source: `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`. Later merged datapack, test, and documentation changes do not alter those JAR bytes.

## Human evidence still pending

Machine verification does not replace Andrew's real playthrough. Still worth testing in a disposable world:

- O-screen readability and progression presentation;
- complete Last Signal interaction and exactly-once return;
- Carrier, active-Nightmare, and Dreamer relog/restart persistence;
- death and technical-recovery wording;
- two-player Java slot separation;
- pacing, balance, readability, and general feel.

## Open issues

- **#20:** the frozen datapack intentionally remains a one-slot prototype. The supported concurrent/disconnect paths are safe, but a manually introduced unrelated entity carrying the global `ss_creature` tag can still affect its objective. Per-entity ownership already belongs to the Java `NightmareService`.
- **#29:** `PreviewPowerData` still wires a throwing invariant constructor directly into its persisted codec. Corrupt negative cooldown data can throw instead of returning `DataResult.error`. This is low severity and does not arise from normal gameplay writes, but it should be fixed with a persisted-codec guard sweep.

Issues #21–#26 were corrected and independently verified in the merged preview batch.

## Lore boundary

Novel mechanics and terminology remain primary authority. The Last Signal, last watchkeeper, Last Light, Kindle, Cold Ash, and the fixed appraisal are **DESIGN**, not canon claims.

Future Nightmare completion and later Seed behaviour must follow `docs/NIGHTMARE-SEED-ROADMAP.md`: central-conflict terminal resolution, separate challenger outcome, then appraisal. Exact later-Seed mechanics require renewed primary-novel verification before implementation.

## Next actions

1. Andrew plays `0.1.0-preview.2` using `mod/PREVIEW-PLAY-GUIDE.md` and records concrete feedback.
2. Fix #29 with codec-error regression coverage across every persisted attachment.
3. Keep #20 open as the frozen datapack's explicit global-selector limitation.
4. Do not broaden into later Seeds, the Dream Realm, or many scenarios until the preview feedback is reviewed.
