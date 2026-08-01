# Shadow Slave — current issues and limitations

**Canonical state:** `PROJECT-STATUS.md`  
**Current Java candidate:** `0.1.0-preview.2` on PR #19  
**Status:** Claude's returned blocker and disconnect gap are corrected; fresh gate and review pending

The previous long-form datapack and alpha issue history remains in Git history and is referenced under
`docs/history/`. This file tracks the current preview and shipped datapack limitations.

## Claude finding correction batch

Claude's 2026-08-01 pass found seven issues. The current PR branch contains candidate fixes and regression coverage:

| Issue | Current correction |
| --- | --- |
| #20 | frozen datapack uses a persistent global trial lock; it refuses another entrant while the owner is online or offline, and releases only after teardown |
| #21 | README and this authoritative issue file state the one-active-trial ceiling, disconnect behaviour, and deeper global-selector limitation |
| #22 | `SoulData` returns `DataResult.error` for invariant failures instead of throwing through the codec |
| #23 | stored schema is validated and dispatched explicitly; schema 1 migrates deliberately and schema 2 is validated exactly as stored |
| #24 | every Nightmare-Spell state at or beyond Dreamer requires the permanent appraised Aspect and Flaw pair |
| #25 | completed imports require retained Carrier evidence, and modern identities require matching mechanics tags |
| #26 | `test/reset` restores an enterable health baseline after removing transient state and modifiers |

Claude's first correction review is preserved in `docs/reviews/2026-08-01-claude-review-of-preview-fixes.md`. The response is appended there rather than erasing the returned findings.

## Frozen datapack multiplayer limitation

`datapack-v1.0.0` has one global Nightmare dimension, bossbar, creature selector, and persistent trial slot. **Only one player may own a First Nightmare at a time.**

The slot now remains occupied if its owner disconnects or the server restarts. Another player is refused until the original owner reconnects and reaches normal teardown. This prevents the ordinary orphan-creature overlap Claude reproduced.

If the owner will never return, an administrator must perform deliberate recovery rather than silently admitting another player. Killing the orphan and clearing `$global ss_trial_lock` without understanding the player's retained `ss_in_nightmare` state can destroy or duplicate progression evidence.

The deeper command-era architecture still uses global `@e[tag=ss_creature]` selectors. `testserver/defect_issue20_stray_creature.mjs` deliberately demonstrates that a manually introduced unrelated entity carrying that tag can still affect the prototype objective. It remains outside the release gate. True per-player entity ownership belongs to the Java `NightmareService` and is not claimed for the frozen datapack.

## Verification status

No blocker is currently proven against the latest correction head, but the replacement gate has not yet run. The earlier validator clean, lifecycle 32/32, and Flaw 39/39 results predate the persistent-lock changes.

Required next run:

```bash
cd testserver
npm run deploy
npm test

./mod/gradlew -p mod clean build
mod/verify-smoke.sh
python3 shadowslave/tools/validate.py
```

`regression_issue20.mjs` must complete the offline-owner sequence and terminate with exit 0; printing PASS while keeping Mineflayer sockets open is not a pass.

## Open evidence gaps

1. The complete Java path through Carrier, Aspirant, Dreamer/Sleeper, Kindle, and Cold Ash has not been played by Andrew.
2. Claude has not yet verified the latest correction head.
3. Real Java logout/login persistence at Carrier, active Nightmare, and Dreamer stages remains untested end to end.
4. Active Java-instance restart recovery lacks a complete restart scenario.
5. Two-player simultaneous Java-preview instances need live multiplayer verification.
6. Migration needs a backed-up real frozen-datapack world test and idempotent second invocation.
7. Signal-fire completion and return position need interactive verification.
8. Ordinary Nightmare death needs cleanup and wording verification.

Use `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` for the complete criteria.

## Known Java preview limitations

- onboarding uses `/shadowslave preview_begin`; natural infection/exhaustion is not implemented;
- The Last Signal is one handcrafted DESIGN scenario;
- the campfire interaction is a development terminal trigger, not the mature completion engine;
- one vanilla Husk is a pressure placeholder;
- no historical body/inventory/provisional power replacement;
- no corpse Gate after First-Nightmare failure;
- appraisal is a fixed DESIGN result rather than a canonical or procedural formula;
- imported identities persist, but all imported mechanics are not implemented;
- no later Seeds, Dream Realm progression, Memories, Echoes, or later ranks;
- no modpack manifest or adapters;
- no public Java release.

## Lore risks

- Do not treat The Last Signal, Last Light, Kindle, Cold Ash, or their appraisal as canon.
- Do not generalise the campfire click into universal Nightmare completion.
- Future completion must follow `docs/NIGHTMARE-SEED-ROADMAP.md`.
- Exact later-Seed rules require renewed primary-novel verification.

## Reporting

Record defects with the exact build and commit, reproduction steps, expected versus observed behaviour, logs or screenshots, and whether the issue is correctness, presentation, balance, lore wording, or missing scope.
