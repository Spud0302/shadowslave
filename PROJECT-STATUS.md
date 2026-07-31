# Shadow Slave project status

**Status date:** 2026-07-31  
**Stable main:** Java `0.1.0-alpha.4`, Claude-verified  
**Active preview:** PR #19 / `gpt/live-datapack-import`  
**Artifact:** `shadowslave-0.1.0-preview.1.jar`  
**Documentation audit:** active status, implementation, migration, testing, issue, collaboration, handoff, and roadmap files reconciled to the preview

## Products

| Product | Current state | Public release |
| --- | --- | --- |
| Vanilla datapack | completed and frozen behavioural reference | `datapack-v1.0.0` |
| Java stable main | persistent Soul foundation, networking/UI, and pure migration | none |
| Java playable preview | automated checkpoint green; Andrew and Claude review pending | not a release |
| Nightmare Spell modpack | design only | none |

## Preview milestone reached

The uninterrupted batch in `docs/PLAYABLE-PREVIEW-DIRECTIVE.md` has reached its stopping condition.
PR #19 contains:

- transactional live datapack migration with read-back verification, rollback, and no legacy cleanup;
- persistent imported and native Aspect/Flaw instance records;
- persistent one-owner Nightmare registry and separate player slots;
- one entry choke point and one teardown path;
- bundled Nightmare dimension;
- playable DESIGN scenario **The Last Signal** with the **last watchkeeper** role;
- a central conflict resolved by restoring a signal fire rather than requiring a boss kill;
- explicit success, canonical-death, technical-recovery, and admin-abort handling;
- fixed DESIGN appraisal `[Last Light]` / `[Cold Ash]`;
- server-authoritative Kindle ability/cooldown and Cold Ash drawback;
- expanded O-key Soul screen;
- player onboarding, recovery, and reset commands;
- install/feedback guide, lore ledger, bulk Claude test matrix, and future Nightmare/Seed roadmap.

## Final automated evidence

Workflow `Java core` run 33 / ID `30555343642` passed:

- wrapper validation;
- compilation and unit tests;
- physical-client startup;
- dedicated-server startup;
- JAR packaging;
- artifact upload.

Source commit: `460cd31f135ae7e98f66890b6bbf60414772d57b`.

```text
archive SHA-256  dd6315fd25ad50bbba09c53433e8b1840a2f70b344b18a425533c4856da3a8e8
JAR SHA-256      600fa2143879f8f269aec6d048a0fa4b3150f808a091c1527fe34067d9cdd867
```

Full details: `docs/PLAYABLE-PREVIEW-PROVENANCE.md`.

## Documentation authority

Current-state documents now agree on `0.1.0-preview.1`:

- `README.md` and `MODDING.md` — product overview and two-track context;
- `mod/README.md` and `mod/IMPLEMENTATION-STATUS.md` — exact Java capabilities and limits;
- `docs/JAVA-HANDOFF.md` and `docs/DATAPACK-MIGRATION.md` — implemented migration/lifecycle contract;
- `ISSUES.md`, `TESTING.md`, and `CHANGELOG.md` — current preview risks, tests, and milestone history;
- `docs/COLLABORATION.md` and `GPT_HANDOFF.md` — bulk-review workflow and next handoff;
- `docs/NIGHTMARE-SEED-ROADMAP.md` — binding future completion architecture.

Long datapack-era and alpha-era records remain available in Git history and are explicitly treated as historical evidence, not current runtime truth. `docs/CURRENT-PREVIEW-SUMMARY.md` is the compact cross-reference.

## Evidence boundary

The artifact is **pre-Claude-tested**. It is not Claude-verified, not a public release, and not claimed
feature-complete. Andrew has not yet played the full loop. Visual quality, feel, multiplayer interaction,
and real logout/reload behaviour remain explicit follow-up evidence.

## Lore boundary

Novel mechanics remain authoritative. The handcrafted scenario, fixed appraisal, Aspect, ability, and
Flaw are labelled DESIGN in `docs/PREVIEW-LORE-DECISIONS.md`; no project formula is presented as canon.

Future Nightmare completion and later Seed behaviour must follow `docs/NIGHTMARE-SEED-ROADMAP.md` and
begin with renewed primary-lore verification.

## Next actions

1. Andrew installs the JAR using `mod/PREVIEW-PLAY-GUIDE.md` and records play feedback.
2. Claude bulk-reviews PR #19 using `docs/reviews/2026-07-30-gpt-playable-preview.md` and
   `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`.
3. Fix evidence-backed defects without broad scope expansion.
4. Do not begin many scenarios, the full Dream Realm, or the modpack comparison until Andrew redirects.
