# GPT handoff — playable preview baseline

**Read first in a new GPT or Claude session.**  
**Repository:** `Spud0302/shadowslave`  
**Baseline branch:** `main`
**Candidate version:** `0.1.0-preview.2`

## Mandatory lore source

Before any lore-sensitive design, implementation, naming or review, read the **[lore source policy](docs/LORE-SOURCE-POLICY.md)**.

The project owner has designated [NovelFull](https://novelfull.com/shadow-slave.html) as the working full-chapter research access source. That host is not itself canonical: use novel chapter text rather than site metadata, memory or wiki summaries as the authority, and use [official WebNovel](https://www.webnovel.com/book/shadow-slave_22196546206090805) for official wording and publication cross-checks. Check relevant later clarifications, summarise evidence without committing novel text, and keep **CANON**, **INFERRED**, **DESIGN** and **UNKNOWN** distinct.

## Current state

The playable Java slice and live migration from PR #19 are merged into `main`. Claude reviewed the #20–#26 correction batch at `ad03e00` and returned one blocking test-harness hang plus one real disconnect gap in the frozen datapack's single-trial guard.

Both returned items are addressed in the merged source:

- `regression_issue20.mjs` terminates cleanly on success and failure;
- the frozen datapack uses a persistent `$global` score in `ss_trial_lock`, so the one shared First-Nightmare slot remains occupied while its owner is offline and across server restarts;
- teardown releases the lock only after shared creature cleanup;
- the regression now kicks Alice mid-trial, proves Bob is refused while Alice is offline, reconnects Alice, proves resume/completion, then proves Bob can enter after teardown;
- Claude's global-selector defect probe is restored separately as `testserver/defect_issue20_stray_creature.mjs`, deliberately outside `npm test`.

The exact response to Claude's review is in `docs/reviews/2026-08-01-claude-review-of-preview-fixes.md`.

## Evidence already established

Java runtime source `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c` passed GitHub Actions `Java core` run 34 / ID `30686670446`:

- wrapper validation;
- compilation and expanded JUnit suite;
- physical-client startup;
- dedicated-server startup;
- JAR packaging and upload.

No Java runtime source changed in the disconnect-lock response. The replacement JAR remains `0.1.0-preview.2` with SHA-256:

```text
48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

Claude independently established before returning the latest changes:

- validator clean;
- lifecycle 32/32;
- Flaw 39/39;
- #22–#26 code shapes accepted by inspection.

Those datapack counts predate the persistent-lock regression and are not proof of the current head.

## Next verification

Fresh evidence is still needed against current `main`:

```bash
cd testserver
npm run deploy
npm test

./mod/gradlew -p mod clean build
mod/verify-smoke.sh
python3 shadowslave/tools/validate.py
```

The datapack gate must now prove:

1. lifecycle 32/32;
2. Flaw 39/39;
3. `regression_issue20.mjs` exits 0 after the disconnect/reconnect sequence;
4. no process remains hanging after PASS.

`defect_issue20_stray_creature.mjs` is not a release-gate test. It deliberately demonstrates that a manually introduced unrelated entity carrying the prototype-global `ss_creature` tag can still affect the global objective. That is retained as an architecture limitation rather than confused with the supported one-slot contract.

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

## Claude findings and current resolution

Full original reasoning remains in `docs/reviews/2026-08-01-claude-test-findings.md`.

| Issue | Merged state |
| --- | --- |
| #20 | ordinary concurrent and disconnect overlap is prevented by a persistent global slot; deeper global-selector limitation remains explicitly measured out of gate |
| #21 | authoritative docs state the one-active-trial ceiling, offline ownership behaviour, and admin recovery boundary |
| #22 | `SoulData.CODEC` converts invariant failures to `DataResult.error` |
| #23 | stored schemas are validated and explicitly dispatched |
| #24 | post-First-Nightmare Spell states retain Aspect, Aspect Rank, and Flaw |
| #25 | inconsistent completed/generated migration evidence fails closed |
| #26 | datapack `test/reset` restores an enterable health baseline |

## Manual Java preview

Use `mod/PREVIEW-PLAY-GUIDE.md` and `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`. Important unproven items remain real relog/restart persistence, death/recovery wording, exact signal completion, multiplayer Java instance separation, and feel/readability.

## Workflow rules

- GPT does not write directly to or merge into `main`.
- Every GPT commit includes `Co-Authored-By: ChatGPT <gpt@openai.com>`.
- Use JDK 21.
- Use `mod/verify-smoke.sh`; bare Gradle smoke-task success is not a valid gate.
- Treat merged PRs as historical context and verify current repository state before following an old status instruction.
- Preserve original findings and review records; append corrections rather than rewriting history.
- Never assume lore. Check primary novel evidence before adding or generalising mechanics, and label project inventions as DESIGN.
