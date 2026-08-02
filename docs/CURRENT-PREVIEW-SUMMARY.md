# Current Java preview summary

**Build:** `0.1.0-preview.2`  
**Runtime source:** `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`  
**Merged main:** `c3ffcd9c3f6139817fe84ef3c81d94ceafdda4e3`  
**PR:** #19, merged 2026-08-02  
**Status:** independently verified machine-checkably; Andrew play feedback pending

This is the compact pointer for current preview truth. Older datapack and alpha statements remain historical evidence rather than the current runtime contract.

## Implemented in the JAR

- transactional live datapack migration with read-back verification and rollback;
- persistent native/imported Aspect and Flaw records;
- persistent per-player First Nightmare ownership and separate play-space slots;
- bundled Nightmare dimension and DESIGN scenario **The Last Signal**;
- Carrier -> Aspirant -> Dreamer/Sleeper progression;
- fixed DESIGN appraisal **Last Light / Kindle / Cold Ash**;
- expanded O-key Soul screen;
- development onboarding, recovery, inspection, reset, and migration commands;
- corrected codec/schema/invariant handling from issues #22–#24;
- stricter fail-closed legacy migration validation from issue #25.

## Verified evidence

Claude verified the merge result:

```text
validator                         clean
lifecycle harness                 32/32
Flaw harness                      39/39
disconnect/reconnect regression   PASS, exit 0, repeated twice
Java tests                        35, failures 0
physical-client smoke             PASS
dedicated-server smoke            PASS
```

The regression cleaned up after itself: the persistent datapack trial lock returned to `0` and no stray trial creature remained.

Artifact:

```text
shadowslave-0.1.0-preview.2.jar
SHA-256 48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

## Remaining evidence

Andrew still needs to judge the real-client loop, presentation, persistence, multiplayer feel, pacing, and balance. No document should represent that deferred playtest as passed.

## Open issues

- **#20:** supported frozen-datapack one-slot ownership is safe, including disconnect/reconnect. A manually introduced unrelated `ss_creature`-tagged entity can still affect its global prototype objective.
- **#29:** corrupt negative `PreviewPowerData` can throw through its persisted codec; audit every registered persisted attachment and add malformed-input guards.

## Current authorities

- `PROJECT-STATUS.md` — exact product and verification state;
- `README.md` — product overview and installation;
- `mod/PREVIEW-PLAY-GUIDE.md` — player walkthrough;
- `TESTING.md` and `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` — machine and manual criteria;
- `docs/PLAYABLE-PREVIEW-PROVENANCE.md` — artifact linkage and checksums;
- `docs/PREVIEW-LORE-DECISIONS.md` — canon/inference/design boundary;
- `docs/NIGHTMARE-SEED-ROADMAP.md` — future Nightmare and Seed architecture;
- `GPT_HANDOFF.md` — next engineering package.
