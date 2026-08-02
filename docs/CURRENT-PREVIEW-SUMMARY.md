# Current Java preview summary

**Build:** `0.1.0-preview.2`  
**Runtime source:** `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`  
**Branch:** `gpt/live-datapack-import`  
**PR:** #19  
**Status:** corrected Java gate green; deployed datapack regression, Claude bulk verification, and Andrew play feedback pending

This is the compact pointer for current preview truth. Older datapack and alpha statements remain historical evidence rather than the current runtime contract.

## Current authorities

- `PROJECT-STATUS.md` — product and verification state;
- `README.md` — installation/product overview and frozen-datapack concurrency ceiling;
- `mod/PREVIEW-PLAY-GUIDE.md` — installation and player walkthrough;
- `docs/JAVA-HANDOFF.md` — migration and Nightmare lifecycle contract;
- `docs/DATAPACK-MIGRATION.md` — live migration transaction;
- `ISSUES.md` — current defects, correction status, gaps, and limitations;
- `TESTING.md` and `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` — current automated/manual criteria;
- `docs/PLAYABLE-PREVIEW-PROVENANCE.md` — artifact and workflow provenance;
- `docs/PREVIEW-LORE-DECISIONS.md` — canon/inference/design boundary;
- `docs/NIGHTMARE-SEED-ROADMAP.md` — future Nightmare and Seed completion architecture;
- `GPT_HANDOFF.md` — current correction handoff.

## Implemented in the JAR

- transactional live datapack migration with read-back verification and rollback;
- persistent native/imported Aspect and Flaw records;
- persistent per-player First Nightmare ownership and separate play-space slots;
- bundled Nightmare dimension and DESIGN scenario **The Last Signal**;
- Carrier -> Aspirant -> Dreamer/Sleeper progression;
- fixed DESIGN appraisal: **Last Light**, **Kindle**, and **Cold Ash**;
- expanded O-key Soul screen;
- development onboarding, recovery, inspection, reset, and migration commands;
- corrected codec/schema/invariant handling from issues #22–#24;
- stricter fail-closed legacy migration validation from issue #25.

## Frozen datapack correction

The source branch also changes `datapack-v1.0.0` maintenance code so only one First Nightmare can be active at a time, restores health in `test/reset`, and adds a two-player serialization regression to `npm test`. Those changes are committed but still require the deployed Minecraft/Mineflayer gate; the Java workflow does not run that server test.

## Corrected Java evidence

Workflow run 34 / ID `30686670446` passed wrapper validation, compilation/unit tests, physical-client startup, dedicated-server startup, packaging, and upload.

```text
artifact ID       8814240590
archive SHA-256   a7ee670001042ee9c783ceb191e667fefdf043acd1b6fa498438434907291d79
JAR SHA-256       48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

## Not claimed

- no natural infection sequence;
- no complete historical body/inventory system;
- no custom Nightmare Creature or corpse Gate;
- no procedural/canonical appraisal formula;
- no full imported-identity mechanics;
- no full Dream Realm, later Seeds, ranks, Memories, Echoes, or modpack implementation;
- no passed deployed concurrency regression yet;
- no completed Andrew playthrough or corrected-head Claude bulk verification.
