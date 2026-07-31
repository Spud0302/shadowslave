# Current Java preview summary

**Build:** `0.1.0-preview.1`  
**Branch:** `gpt/live-datapack-import`  
**PR:** #19  
**Status:** installable development preview; automated checkpoint green; Andrew play feedback and Claude bulk review pending  
**Documentation audit:** completed 2026-07-31

This is the compact pointer for current preview truth. Older datapack and alpha statements in Git history
remain historical evidence rather than the current runtime contract.

## Current authorities

- `PROJECT-STATUS.md` — product and verification state;
- `README.md` and `MODDING.md` — repository/product overview;
- `mod/README.md` and `mod/IMPLEMENTATION-STATUS.md` — Java capabilities and boundaries;
- `mod/PREVIEW-PLAY-GUIDE.md` — installation and player walkthrough;
- `docs/JAVA-HANDOFF.md` — migration and Nightmare lifecycle contract;
- `docs/DATAPACK-MIGRATION.md` — implemented live migration transaction;
- `ISSUES.md` — current risks, gaps, and limitations;
- `TESTING.md` and `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` — current automated/manual criteria;
- `CHANGELOG.md` — active preview milestone history;
- `docs/PLAYABLE-PREVIEW-PROVENANCE.md` — artifact and workflow provenance;
- `docs/PREVIEW-LORE-DECISIONS.md` — canon/inference/design boundary;
- `docs/NIGHTMARE-SEED-ROADMAP.md` — future Nightmare and Seed completion architecture;
- `docs/COLLABORATION.md` and `GPT_HANDOFF.md` — current bulk-review handoff workflow.

## Implemented in the JAR

- transactional live datapack migration with read-back verification and rollback;
- persistent native/imported Aspect and Flaw records;
- persistent per-player First Nightmare ownership and separate play-space slots;
- bundled Nightmare dimension and the DESIGN scenario **The Last Signal**;
- Carrier -> Aspirant -> Dreamer/Sleeper progression;
- fixed DESIGN appraisal: **Last Light**, **Kindle**, and **Cold Ash**;
- expanded O-key Soul screen;
- development onboarding, recovery, inspection, reset, and migration commands.

## Not claimed

- no natural infection sequence;
- no complete historical body/inventory system;
- no custom Nightmare Creature or corpse Gate;
- no procedural/canonical appraisal formula;
- no full imported-identity mechanics;
- no full Dream Realm, later Seeds, ranks, Memories, Echoes, or modpack implementation;
- no completed human playthrough or Claude bulk verification.

## Historical records

The previous long datapack-era and alpha-era issue/testing/changelog documents remain recoverable from
Git history. Pointer files under `docs/history/` record the relevant old blob IDs. They are not full
copies in the current tree and must not be represented as such.