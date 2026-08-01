# GPT handoff — corrected playable preview candidate

**Read first in a new GPT or Claude session.**  
**Repository:** `Spud0302/shadowslave`  
**Stable main:** `a638efc60866ca9a390f3172c5e712753e5764c8`  
**Active branch:** `gpt/live-datapack-import`  
**PR:** #19  
**Candidate version:** `0.1.0-preview.2`

## Current state

The playable Java slice and live migration are implemented on PR #19. Claude's 2026-08-01 pass found issues #20–#26. Candidate fixes and regression coverage are now committed on the same branch. The old `0.1.0-preview.1` JAR predates these corrections and must not be presented as the corrected build.

A fresh Java workflow, deployed datapack harness, Claude bulk review, and Andrew play feedback remain pending. Do not merge or release solely because the earlier preview.1 workflow was green.

## Playable Java loop

```text
Uninfected
→ /shadowslave preview_begin
→ Carrier
→ Aspirant / Dormant inside The Last Signal
→ restore the signal fire
→ Dreamer/Sleeper / Dormant
→ Last Light / Awakened Aspect Rank
→ Kindle ability + Cold Ash Flaw
```

The scenario, role, fixed appraisal, Aspect, ability, and Flaw are **DESIGN**, not asserted canon. Future Nightmare completion follows `docs/NIGHTMARE-SEED-ROADMAP.md`: central-conflict terminal resolution, separate challenger outcome, then appraisal.

## Claude findings and candidate fixes

Full original reasoning remains in `docs/reviews/2026-08-01-claude-test-findings.md`.

| Issue | Candidate fix on PR #19 |
| --- | --- |
| #20 | frozen datapack serializes First Nightmare entry at the single `nightmare/enter` choke point; a second entrant is refused before shared state exists |
| #21 | README and authoritative `ISSUES.md` state the one-active-trial ceiling and former trapped-player symptom |
| #22 | `SoulData.CODEC` decodes through `StoredSoulData` and converts invariant exceptions to `DataResult.error` |
| #23 | stored schema is validated and explicitly dispatched; schema 1 migrates deliberately, schema 2 is validated without silent repairs |
| #24 | Nightmare-Spell states at or beyond Dreamer require Aspect, Aspect Rank, and Flaw identity |
| #25 | completed imports require the retained Carrier tag; generated two-digit identities require their matching mechanics tags |
| #26 | datapack `test/reset` restores health after removing max-health modifiers and transient state |

`testserver/regression_issue20.mjs` now tests the supported frozen-datapack contract with two real players:

1. Alice enters.
2. Bob is refused without progression or active-trial leakage.
3. Alice completes normally.
4. Bob enters after teardown releases the global slot.

It is wired into `npm test` after the lifecycle and Flaw suites.

## What to verify now

### Java

```bash
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
```

Confirm specifically:

- malformed Soul records return codec errors without raw exceptions;
- invalid, zero, negative, and future schemas reject;
- schema-1 migration still preserves valid imported identity;
- schema-2 does not receive schema-1 repairs;
- every post-First-Nightmare Spell state retains Aspect/Flaw identity;
- new migration rejection tests pass;
- physical client and dedicated server reach the accepted readiness markers.

### Frozen datapack

A real local 1.21.1 test server is required:

```bash
cd testserver
npm run deploy
npm test
```

Expected gate now includes:

- lifecycle 32/32;
- Flaw 39/39;
- concurrency serialization regression passes.

The server tests a built/deployed ZIP, not the working tree. Never run `npm test` without confirming deployment.

### Manual Java preview

Use `mod/PREVIEW-PLAY-GUIDE.md` and `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`. Important unproven items remain real relog/restart persistence, death/recovery wording, exact signal completion, multiplayer Java instance separation, and feel/readability.

## Implemented preview systems

- transactional live datapack evidence reader and migration writer;
- exact read-back verification, rollback, and final migration marker;
- persistent imported/native Aspect and Flaw records;
- persistent per-player `NightmareRegistryData` and scenario slots;
- one Java entry choke point and shared teardown path;
- bundled Nightmare dimension;
- DESIGN Last Signal scenario and last-watchkeeper role;
- fixed DESIGN Last Light / Kindle / Cold Ash appraisal;
- expanded O-key Soul screen;
- player onboarding, inspection, recovery, reset, and migration commands.

## Known boundaries

- no natural infection/exhaustion sequence;
- no historical body/inventory replacement;
- vanilla Husk placeholder instead of custom Nightmare Creature;
- no corpse Gate;
- no procedural/canonical appraisal formula;
- no complete mechanics for every imported identity;
- no later Seeds, Dream Realm progression, Memories, Echoes, or full rank ladder gameplay;
- no modpack implementation yet.

## Workflow rules

- GPT does not write directly to or merge into `main`.
- Every GPT commit includes `Co-Authored-By: ChatGPT <gpt@openai.com>`.
- Use JDK 21.
- Use `mod/verify-smoke.sh`; bare Gradle smoke-task success is not a valid gate.
- Keep PR #19 unmerged until the corrected head has fresh evidence.
- Preserve the original Claude findings as historical evidence; append resolution rather than pretending the defects were never present.
- Never assume lore. Check primary novel evidence before adding or generalising mechanics, and label project inventions as DESIGN.
