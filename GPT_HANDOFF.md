# GPT handoff — playable preview checkpoint

**Repository:** `Spud0302/shadowslave`  
**Stable main:** `5f8acf2b2e3b04198166592568dd885431a2a09f`  
**Active branch:** `gpt/live-datapack-import`  
**Draft PR:** #19  
**Target:** `shadowslave-0.1.0-preview.1.jar`

## Binding owner directive

Read `docs/PLAYABLE-PREVIEW-DIRECTIVE.md`. Andrew directed GPT to continue without per-package Claude
waits until an installable playable preview, tests, lore ledger, limitations and bulk Claude handoff
exist. Never assume lore; label canon/inference/design/unknown.

## Current branch contents

- live fail-safe datapack score/tag reader;
- provisional Java migration persistence, exact read-back and rollback;
- persistent imported and general Aspect/Flaw instance records;
- persistent one-owner-per-player Nightmare registry;
- separate scenario slots and return/recovery data;
- Last Signal historical-role/conflict preview;
- single entry choke point and teardown path;
- success, death, technical recovery and admin abort distinctions;
- fixed DESIGN appraisal `[Last Light]` / `[Cold Ash]`;
- Kindle ability/cooldown and Cold Ash drawback;
- bundled Nightmare dimension;
- expanded Soul screen;
- player onboarding/recovery/reset commands;
- preview play guide, lore ledger and bulk test matrix.

## Verification already obtained

- alpha.5 importer checkpoint: compile/tests/client/server/package/artifact green;
- alpha.6 playable-slice checkpoint: green after correcting one `InteractionResult` import namespace;
- stable alpha.4 on main remains Claude-verified.

Use `mod/verify-smoke.sh`, never bare Gradle smoke-task exit codes. JDK 21 is required.

## Final stopping sequence

1. Commit `0.1.0-preview.1` version and preview documentation.
2. Run one final workflow checkpoint.
3. Record exact workflow/run/artifact provenance.
4. Make the JAR available to Andrew with `mod/PREVIEW-PLAY-GUIDE.md`.
5. Leave PR #19 draft and unmerged for bulk Claude review.
6. Stop broad development unless Andrew redirects; do not expand into many scenarios, Dream Realm or
   modpack comparison yet.

## Honest limitations

No human has played the complete preview loop yet. No full logout/login GameTest exists. A vanilla Husk
is a placeholder pursuer. Natural infection, custom role body/inventory, corpse Gate, procedural
appraisal and complete imported mechanics are not implemented.

## Review documents

- `docs/PREVIEW-LORE-DECISIONS.md`
- `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`
- `docs/reviews/2026-07-30-gpt-playable-preview.md`
- `mod/PREVIEW-PLAY-GUIDE.md`
