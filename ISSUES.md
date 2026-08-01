# Shadow Slave — current issues and limitations

**Canonical state:** `PROJECT-STATUS.md`  
**Current Java candidate:** `0.1.0-preview.2` on PR #19  
**Status:** corrections for issues #20–#26 committed; fresh automated and human verification pending

The previous long-form datapack and alpha issue history remains in Git history and is referenced under
`docs/history/`. This file tracks the current preview and shipped datapack limitations.

## Claude finding correction batch

Claude's 2026-08-01 pass found seven issues. The current PR branch contains candidate fixes and regression coverage:

| Issue | Candidate correction |
| --- | --- |
| #20 | frozen datapack refuses a second concurrent First Nightmare before shared state is created; regression verifies refusal, first-player completion, teardown, and later admission |
| #21 | README and this authoritative issue file state the one-active-trial ceiling and the former trapped-player symptom |
| #22 | `SoulData` decodes through an unvalidated storage record and returns `DataResult.error` for invariant failures instead of throwing through the codec |
| #23 | stored schema is validated and dispatched explicitly; schema 1 migrates deliberately and schema 2 is validated exactly as stored |
| #24 | every Nightmare-Spell state at or beyond Dreamer requires the permanent appraised Aspect and Flaw pair |
| #25 | completed imports require the retained Carrier tag, and modern two-digit Aspect/Flaw identities require their matching mechanics tags |
| #26 | `test/reset` restores an enterable health baseline after removing transient state and modifiers |

These issues should remain open until the corrected branch passes its fresh gates and Claude records the result. The old `0.1.0-preview.1` JAR does not contain this correction batch.

## Frozen datapack multiplayer limitation

`datapack-v1.0.0` can be installed on a multiplayer server, but it has one global Nightmare dimension, bossbar, and creature slot. **Only one player may be inside a First Nightmare at a time.** A second entrant is refused until teardown releases the slot.

Before the correction, concurrent trials could see each other's creatures: one player's surviving creature reset another player's victory counter, blocked completion, and could leave that player trapped after the timer expired. Serializing entry prevents that broken overlap. True simultaneous per-player Nightmares are implemented in the Java preview rather than retrofitted into the frozen command architecture.

## Confirmed build blockers

No blocker is currently proven against the correction branch, but the branch has not yet completed its replacement verification run. Do not reuse the `preview.1` green result as evidence for `preview.2`.

## Open evidence gaps

These are unverified or partially verified behaviours, not silently claimed successes:

1. The complete interactive path from fresh player through Carrier, Aspirant, Dreamer/Sleeper, Kindle,
   and Cold Ash has not been played by Andrew.
2. Claude has not yet bulk-verified the corrected PR #19 head.
3. Real logout/login persistence at Carrier, active Nightmare, and Dreamer stages has not been exercised
   end to end by a human or GameTest.
4. Active-instance server restart recovery is covered by record persistence and startup loading, not a
   full restart scenario.
5. Two-player simultaneous Java-preview entry, separate slots, independent completion, and teardown need
   live multiplayer verification.
6. Migration needs a backed-up real `datapack-v1.0.0` world test, including a second idempotent run.
7. The signal-fire interaction must be checked for exactly-once completion and reliable return position.
8. Ordinary Nightmare death must be checked for cleanup and clear non-canon Minecraft-respawn wording.
9. The frozen-datapack concurrency regression requires a deployed Minecraft test server; wiring it into
   `npm test` does not itself prove it passed.

Use `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` for exact criteria.

## Known implementation limitations

These are deliberate preview boundaries rather than hidden bugs:

- onboarding uses `/shadowslave preview_begin`; natural infection/exhaustion is not implemented;
- The Last Signal is one handcrafted DESIGN scenario;
- the campfire interaction is one development terminal trigger, not the mature completion engine;
- one vanilla Husk is a pressure placeholder, not a custom Nightmare Creature;
- no historical body/inventory/provisional power replacement;
- no corpse Gate after First-Nightmare failure;
- appraisal is a fixed DESIGN result rather than a canonical or procedural formula;
- imported identities persist, but all imported mechanics are not implemented;
- no later Seeds, Dream Realm progression, Memories, Echoes, or later ranks;
- no modpack manifest or adapters;
- no public Java release.

## Architecture risks for review

Claude should focus on:

- SavedData ownership and duplicate-player rejection;
- teardown ordering if teleport, interaction, or appraisal throws;
- death/respawn attachment ordering;
- reconnect and dimension mismatch recovery;
- owned-entity cleanup by exact UUID;
- migration rollback across all linked attachments;
- client/server class separation after the expanded Soul snapshot;
- multiplayer slot allocation after save/reload;
- codec failures remaining data errors rather than escaping exceptions.

## Lore risks

- Do not treat The Last Signal, Last Light, Kindle, Cold Ash, or their appraisal as canon.
- Do not generalise the campfire click into universal Nightmare completion.
- Future completion must follow `docs/NIGHTMARE-SEED-ROADMAP.md`.
- Exact later-Seed rules require renewed primary-novel verification.

## Reporting

Record defects with:

- exact build and commit;
- single-player or multiplayer context;
- reproduction steps;
- expected versus observed behaviour;
- relevant logs/screenshots;
- whether the issue is correctness, presentation, balance, lore wording, or missing scope.
