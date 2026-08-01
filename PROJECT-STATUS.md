# Shadow Slave project status

**Status date:** 2026-08-01  
**Stable main:** Java `0.1.0-alpha.4`, Claude-verified  
**Active preview:** PR #19 / `gpt/live-datapack-import`  
**Corrected Java candidate:** `shadowslave-0.1.0-preview.2.jar`  
**Runtime source commit:** `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`

## Products

| Product | Current state | Public release |
| --- | --- | --- |
| Vanilla datapack | completed reference; one active First Nightmare at a time | `datapack-v1.0.0` |
| Java stable main | persistent Soul foundation, networking/UI, and pure migration | none |
| Java playable preview | corrected preview.2 Java gate green; Claude/player review pending | not a release |
| Nightmare Spell modpack | design only | none |

## Playable preview scope

PR #19 contains:

- transactional live datapack migration with read-back verification, rollback, and no legacy cleanup;
- persistent imported and native Aspect/Flaw instance records;
- persistent per-player Nightmare registry and separate scenario slots;
- one Java entry choke point and one teardown path;
- bundled Nightmare dimension;
- playable DESIGN scenario **The Last Signal** with the **last watchkeeper** role;
- central-conflict completion by restoring a signal fire, with combat optional;
- fixed DESIGN appraisal **Last Light** / **Kindle** / **Cold Ash**;
- expanded O-key Soul screen;
- onboarding, inspection, recovery, reset, and migration commands.

## Claude finding correction batch

Claude's 2026-08-01 review found issues #20–#26. The active branch now contains candidate fixes:

- `SoulData` codec invariant failures become `DataResult.error` rather than raw load exceptions;
- schema versions are validated and explicitly migrated/dispatched;
- every post-First-Nightmare Nightmare-Spell state retains Aspect/Flaw identity;
- completed datapack imports require the retained Carrier tag;
- generated identities require their matching mechanics tags;
- datapack `test/reset` restores an enterable health baseline;
- the frozen datapack refuses a second concurrent First Nightmare before shared state is created;
- README and authoritative issues documentation state that one-active-trial ceiling.

The original findings remain in `docs/reviews/2026-08-01-claude-test-findings.md`.

## Corrected Java evidence

GitHub Actions `Java core` run **34** / ID `30686670446` passed:

- Gradle wrapper validation;
- compilation and expanded unit tests;
- physical-client startup;
- dedicated-server startup;
- JAR packaging;
- artifact upload.

Artifact ID: `8814240590`.

```text
archive SHA-256  a7ee670001042ee9c783ceb191e667fefdf043acd1b6fa498438434907291d79
JAR SHA-256      48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

Full artifact details belong in `docs/PLAYABLE-PREVIEW-PROVENANCE.md`.

## Evidence boundary

The corrected **Java** gate is green. This does not yet prove:

- the deployed frozen-datapack lifecycle/Flaw/concurrency Mineflayer gate;
- Claude's independent bulk verification of the corrected head;
- Andrew's complete interactive playthrough;
- real relog/restart persistence;
- live Java multiplayer instance separation and gameplay feel.

The old `0.1.0-preview.1` JAR is superseded by the correction candidate and must not be used as evidence for issues #20–#26.

## Frozen datapack multiplayer contract

The datapack can run on a multiplayer server, but it owns one global Nightmare dimension, bossbar, and creature slot. Only one player may be inside a First Nightmare at a time. A second entrant is refused until teardown releases the slot. True simultaneous per-player trials are provided by the Java architecture.

## Lore boundary

Novel mechanics remain authoritative. The handcrafted scenario, fixed appraisal, Aspect, ability, and Flaw are labelled **DESIGN**. Future Nightmare completion and later Seed behaviour must follow `docs/NIGHTMARE-SEED-ROADMAP.md` and begin with renewed primary-lore verification.

## Next actions

1. Run the deployed datapack gate: `cd testserver && npm run deploy && npm test`.
2. Claude bulk-reviews the corrected PR head and records verified, verified with fixes, or blocked.
3. Andrew plays `0.1.0-preview.2` and records presentation/feel findings.
4. Fix evidence-backed defects without broad content expansion.
5. Merge only after the corrected evidence is accepted.
