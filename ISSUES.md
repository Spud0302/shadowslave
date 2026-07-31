# Shadow Slave — current issues and limitations

**Canonical state:** `PROJECT-STATUS.md`  
**Current Java artifact:** `0.1.0-preview.1` on draft PR #19  
**Status:** automated checkpoint green; Andrew play feedback and Claude bulk review pending

The previous long-form datapack and alpha issue history remains in Git history and is referenced under
`docs/history/`. This file now tracks the current preview only.

## Confirmed build blockers

**None known.** The final preview workflow passed compilation, unit tests, physical-client startup,
dedicated-server startup, JAR packaging, and artifact upload.

That does not prove the full gameplay loop.

## Open evidence gaps

These are unverified or partially verified behaviours, not silently claimed successes:

1. The complete interactive path from fresh player through Carrier, Aspirant, Dreamer/Sleeper, Kindle,
   and Cold Ash has not been played by Andrew.
2. Claude has not yet bulk-reviewed PR #19.
3. Real logout/login persistence at Carrier, active Nightmare, and Dreamer stages has not been exercised
   end to end by a human or GameTest.
4. Active-instance server restart recovery is covered by record persistence and startup loading, not a
   full restart scenario.
5. Two-player simultaneous entry, separate slots, independent completion, and teardown need live
   multiplayer verification.
6. Migration needs a backed-up real `datapack-v1.0.0` world test, including a second idempotent run.
7. The signal-fire interaction must be checked for exactly-once completion and reliable return position.
8. Ordinary Nightmare death must be checked for cleanup and clear non-canon Minecraft-respawn wording.

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
- multiplayer slot allocation after save/reload.

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
