# Current Java preview summary

**Build:** `0.1.0-preview.1`  
**Branch:** `gpt/live-datapack-import`  
**PR:** #19  
**Status:** installable development preview; automated checkpoint green; Andrew play feedback and Claude bulk review pending

This file is a compact pointer for historical documents whose older sections deliberately remain unchanged.
The current authorities are:

- `PROJECT-STATUS.md` — product and verification state;
- `mod/PREVIEW-PLAY-GUIDE.md` — installation and player walkthrough;
- `docs/PLAYABLE-PREVIEW-PROVENANCE.md` — artifact and workflow provenance;
- `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` — automated and manual criteria;
- `docs/PREVIEW-LORE-DECISIONS.md` — canon/inference/design boundary;
- `docs/NIGHTMARE-SEED-ROADMAP.md` — future Nightmare and Seed completion architecture.

Implemented in the JAR:

- transactional live datapack migration with read-back verification and rollback;
- persistent native/imported Aspect and Flaw records;
- persistent per-player First Nightmare ownership and separate play-space slots;
- bundled Nightmare dimension and the DESIGN scenario **The Last Signal**;
- Carrier -> Aspirant -> Dreamer/Sleeper progression;
- fixed DESIGN appraisal: **Last Light**, **Kindle**, and **Cold Ash**;
- expanded O-key Soul screen;
- development onboarding, recovery, inspection, reset, and migration commands.

Not claimed:

- no natural infection sequence;
- no complete historical body/inventory system;
- no custom Nightmare Creature or corpse Gate;
- no procedural/canonical appraisal formula;
- no full imported-identity mechanics;
- no full Dream Realm, later Seeds, ranks, Memories, Echoes, or modpack implementation;
- no completed human playthrough or Claude bulk verification.

Historical sections in `CHANGELOG.md`, `ISSUES.md`, and `TESTING.md` may mention earlier versions and
unimplemented systems. They are evidence of project history, not the current runtime contract.