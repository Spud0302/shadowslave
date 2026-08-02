# Shadow Slave — current issues and limitations

**Canonical state:** `PROJECT-STATUS.md`  
**Current Java build:** `0.1.0-preview.2` on merged `main@c3ffcd9`  
**Verification:** Claude-verified machine-checkably; Andrew play feedback pending

The previous long-form datapack and alpha issue history remains in Git history and under `docs/history/`. This file tracks current open defects, evidence gaps, and deliberate preview limitations.

## Open issues

### #20 — frozen datapack global creature selector

The supported datapack contract is safe and verified:

- one active First Nightmare at a time;
- persistent global lock survives owner disconnect and server restart;
- a second player is refused before shared trial state is created;
- reconnecting owner resumes and can complete;
- teardown clears the creature and releases the lock;
- the gate regression exits cleanly and is repeatable.

The frozen prototype still uses global selectors such as `@e[tag=ss_creature]` inside its one Nightmare dimension. A manually introduced unrelated entity carrying that internal tag can therefore affect its objective, bossbar, or leash logic. `testserver/defect_issue20_stray_creature.mjs` preserves this measurement outside the release gate.

This is not a normal supported-player path, but the issue remains open honestly. True per-entity and per-instance ownership belongs to the Java `NightmareService` and is already used by the playable preview.

### #29 — persisted attachment codec invariants

`PreviewPowerData` is persisted and its codec calls a throwing compact constructor directly. Corrupt negative `kindle_cooldown_until` data can raise `IllegalArgumentException` through decode instead of returning `DataResult.error`.

Normal gameplay cannot produce a negative cooldown, so severity is low. The durable fix is broader than one line:

1. audit every registered persisted attachment codec;
2. route throwing invariants through non-throwing storage records and explicit `DataResult` validation;
3. feed each codec a well-formed invariant-violating payload;
4. assert a codec error is returned and no exception escapes.

## Resolved Claude findings

Issues #21–#26 were corrected and independently verified in the merged preview batch:

- authoritative documentation states the frozen datapack's one-slot contract;
- `SoulData` invariant failures return codec errors;
- stored schemas are validated and explicitly dispatched;
- post-First-Nightmare Spell states retain Aspect/Flaw identity;
- inconsistent legacy migration evidence fails closed;
- `test/reset` restores an enterable health baseline.

## Human evidence gaps

These are unverified behaviours, not silently claimed successes:

1. Andrew has not played the complete loop from fresh player through Kindle and Cold Ash.
2. O-screen presentation and readability have not been judged in a real client.
3. Carrier, active-Nightmare, and Dreamer relog/restart persistence need real-world exercise.
4. Two-player Java-preview slot separation and independent completion need live verification.
5. Migration should be tested on a backed-up real `datapack-v1.0.0` world, including an idempotent second run.
6. Signal-fire completion should be checked for exactly-once progression and reliable return.
7. Ordinary Nightmare death and technical recovery need wording/presentation review.
8. Pacing, difficulty, atmosphere, and system feel need Andrew's feedback.

Use `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` for the exact play matrix.

## Deliberate preview limitations

- onboarding uses `/shadowslave preview_begin`; natural infection/exhaustion is not implemented;
- The Last Signal is one handcrafted DESIGN scenario;
- the campfire interaction is a development terminal event, not the mature completion engine;
- one vanilla Husk is a pressure placeholder, not a custom Nightmare Creature;
- no historical body/inventory/provisional power replacement;
- no corpse Gate after failure;
- appraisal is a fixed DESIGN result rather than a canonical or procedural formula;
- imported identities persist, but all imported mechanics are not implemented;
- no later Seeds, Dream Realm progression, Memories, Echoes, or later ranks;
- no modpack manifest or adapters;
- no public Java release.

## Lore risks

- Do not treat The Last Signal, Last Light, Kindle, Cold Ash, or their appraisal as canon.
- Do not generalise the campfire click into universal Nightmare completion.
- Follow `docs/NIGHTMARE-SEED-ROADMAP.md` for future completion work.
- Recheck primary novel evidence before implementing later Seeds or broadening mechanics.

## Reporting

Record defects with:

- exact build and commit;
- single-player or multiplayer context;
- reproduction steps;
- expected versus observed behaviour;
- relevant logs/screenshots;
- whether the issue is correctness, presentation, balance, lore wording, or missing scope.
