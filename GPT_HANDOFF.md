# GPT handoff — playable preview milestone reached

**Repository:** `Spud0302/shadowslave`  
**Stable main:** `5f8acf2b2e3b04198166592568dd885431a2a09f`  
**Preview branch:** `gpt/live-datapack-import`  
**Draft PR:** #19  
**Artifact:** `shadowslave-0.1.0-preview.1.jar`

## Owner directive status

The stopping condition in `docs/PLAYABLE-PREVIEW-DIRECTIVE.md` is satisfied:

- live migration implemented and tested;
- playable historical-role/conflict slice exists;
- development preview JAR built;
- artifact made available to Andrew;
- final automated workflow green;
- unperformed human checks and limitations recorded;
- accumulated Claude review matrix complete.

Do not continue broad feature expansion unless Andrew redirects.

## Final automated provenance

- source commit: `460cd31f135ae7e98f66890b6bbf60414772d57b`;
- workflow: `Java core` run 33 / ID `30555343642`;
- result: success;
- artifact ID: `8764632229`;
- archive SHA-256: `dd6315fd25ad50bbba09c53433e8b1840a2f70b344b18a425533c4856da3a8e8`;
- JAR SHA-256: `600fa2143879f8f269aec6d048a0fa4b3150f808a091c1527fe34067d9cdd867`.

See `docs/PLAYABLE-PREVIEW-PROVENANCE.md`.

## What Andrew should do

Follow `mod/PREVIEW-PLAY-GUIDE.md` in a disposable Minecraft 1.21.1 NeoForge 21.1.244 world. The core
loop starts with `/shadowslave preview_begin`; O opens the Soul screen.

## What Claude should do

Bulk-review PR #19 using:

- `docs/reviews/2026-07-30-gpt-playable-preview.md`;
- `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`;
- `docs/PREVIEW-LORE-DECISIONS.md`.

This work is **pre-Claude-tested**. Do not call it Claude-verified until that review is recorded.

## Future Nightmare and Seed direction

Read `docs/NIGHTMARE-SEED-ROADMAP.md` before changing Nightmare completion, scenario objectives,
appraisal or later Seed behaviour.

The binding direction is:

- a Nightmare ends when its central conflict reaches a named terminal resolution;
- boss deaths, objective interactions and timers are events, not universal completion conditions;
- challenger survival/eligibility is evaluated separately from global scenario resolution;
- appraisal happens after completion and may differ between challengers;
- conquering a Seed follows resolution of its contained Nightmare rather than clicking or damaging the
  external Seed;
- First Nightmares and later Seeds share scenario machinery without erasing their lore differences;
- all exact Seed lifecycle and edge-case rules require another primary-novel check before implementation.

The roadmap includes the planned `ResolutionGraph`, per-challenger outcomes, Last Signal upgrade path,
multiplayer direction, forbidden anti-patterns and required automated tests.

## Honest limitations

No human has played the complete loop yet. Real logout/login and active-instance restart are not
end-to-end automated. A vanilla Husk is a placeholder. Natural infection, historical body/inventory,
corpse Gate, procedural appraisal, broad content and full imported mechanics are outside this preview.

## Workflow reminders

- GPT does not merge PR #19 or write directly to main.
- Use JDK 21.
- Use `mod/verify-smoke.sh`, not bare Gradle smoke-task exit codes.
- Preserve lore classifications and do not present DESIGN as canon.
- Fix play/review findings before expanding scope.
