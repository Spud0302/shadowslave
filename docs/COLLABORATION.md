# Collaboration protocol — Andrew, GPT and Claude through git

**Current protocol context:** draft PR #19 / `0.1.0-preview.1`  
**Repository is the communication channel:** decisions, tests, limitations, and disagreements must be committed, recorded in a PR/issue, or linked from an authoritative document.

## Roles

| Person/agent | Primary responsibility |
| --- | --- |
| Andrew | project owner, final decisions, player judgement, and release approval |
| GPT | lore research, architecture, implementation/docs on `gpt/*`, tests, handoff criteria, and issue discovery |
| Claude | independent review/testing, evidence-backed fixes, version stamping, merging, and releases |

GPT never writes directly to or merges into `main`. Claude is not a ceremonial approver; automated CI does not replace independent review. Andrew may override workflow, design, and release decisions.

## Current owner-directed batch rule

`docs/PLAYABLE-PREVIEW-DIRECTIVE.md` governs the preview batch:

- GPT did not pause for Claude between intermediate packages;
- tests, lore classifications, limitations, and review criteria were built alongside implementation;
- the batch stopped at an installable preview JAR with a complete accumulated handoff;
- Claude now receives one bulk review package rather than separate per-package gates;
- no document may call the preview Claude-verified before that review is recorded.

This is a deliberate batching rule, not permission for GPT to merge its own work.

## Branch and merge rules

1. GPT branches from current remote `main` and records the exact baseline.
2. GPT branches use `gpt/*`.
3. GPT keeps accumulated work in a draft PR while implementing coherent checkpoints.
4. Deliberate CI checkpoints may temporarily mark a PR ready, then return it to draft.
5. At the owner-approved stopping point, GPT records provenance, limitations, and the full test matrix.
6. Claude independently reviews/tests the accumulated diff, records evidence and fixes, and decides whether it is mergeable.
7. `main` remains the accepted/release line.
8. Andrew may redirect scope or approve a different batching strategy at any time.

## Commit attribution

- GPT: `Co-Authored-By: ChatGPT <gpt@openai.com>`
- Claude: use Claude's co-author trailer consistently.

## Test matrix

### Documentation-only change

Review factual state, links, baselines, historical annotations, and agreement between authority files. Runtime tests are required only where documentation exposes uncertainty or changes generated/runtime resources.

### Frozen datapack touched

```bash
python3 shadowslave/tools/validate.py
cd testserver && npm run deploy && npm test
cd .. && python3 shadowslave/tools/build_release.py
```

Always confirm the deployed build/version before trusting a server result.

### Java source/resources touched

```bash
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
```

Use JDK 21. Bare Gradle smoke-task exit codes are not proof that Minecraft reached readiness.

Interactive checks remain required evidence for screens, key mappings, gameplay feel, persistence, visual presentation, and multiplayer behaviour—but owner decision D2 allows those checks to be deferred rather than treated as automatic merge gates. Deferred is not passed.

### Migration touched

Required evidence includes untouched, Carrier, generated identity, legacy identity, explicit-zero rejection, inconsistent-state rejection, read-back verification, rollback, and idempotency. No live importer removes legacy values before verified Java persistence.

### Nightmare lifecycle touched

Tests cover:

- one entry/eligibility choke point;
- one ownership/teardown path;
- success, canonical death, technical recovery, and administrator abort;
- persistent per-player ownership;
- reconnect/restart handling;
- multiplayer slot and state independence;
- idempotent resolution and reward/teardown behaviour.

Future completion tests additionally follow `docs/NIGHTMARE-SEED-ROADMAP.md`.

## CI policy

CI is intentionally throttled. Keep a PR draft during development and trigger runs only at deliberate checkpoints. A corrected head must receive a new run; rerunning an old workflow does not verify a new commit.

Successful CI is evidence, not permission for GPT to merge or call human/Claude checks passed.

## Engineering invariants

- one service/choke point owns eligibility and entry;
- one lifecycle path owns teardown for every exit reason;
- client sends intent, never authoritative progression or identity values;
- tests fail closed and prove preconditions;
- every result is tied to an exact build/commit;
- optional dependencies never own canonical Soul identity;
- live migration never deletes legacy evidence before verified read-back;
- temporary historical role/conflict/evidence belongs to `NightmareInstance`;
- permanent identity belongs to Java Soul/identity records;
- project DESIGN is never presented as canon;
- Nightmare completion is terminal conflict resolution, not a universal boss death, timer, or block click.

## Handoff requirements

A handoff includes:

- owner intent and scope;
- exact base/head commits;
- observable acceptance criteria;
- automated results and artifact provenance;
- exact manual checks still unperformed;
- known correctness, presentation, balance, and lore risks;
- canon/inferred/design/unknown classifications;
- what must not change or expand yet.

The current accumulated handoff is:

- `docs/reviews/2026-07-30-gpt-playable-preview.md`;
- `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`;
- `docs/PLAYABLE-PREVIEW-PROVENANCE.md`;
- `docs/PREVIEW-LORE-DECISIONS.md`.

## Historical records

Old changelog, issue, and testing statements remain evidence of what was believed at the time. Current truth belongs in `PROJECT-STATUS.md`, `docs/CURRENT-PREVIEW-SUMMARY.md`, the root current documents, and the latest accepted implementation.

## Where to look

| Need | Authority |
| --- | --- |
| current state | `PROJECT-STATUS.md` |
| compact preview summary | `docs/CURRENT-PREVIEW-SUMMARY.md` |
| current GPT checkpoint | `GPT_HANDOFF.md` |
| lore authority | `docs/JAVA-LORE-ALIGNMENT.md` and `docs/PREVIEW-LORE-DECISIONS.md` |
| Java migration/lifecycle contract | `docs/JAVA-HANDOFF.md` |
| future Nightmare/Seed completion | `docs/NIGHTMARE-SEED-ROADMAP.md` |
| current implementation | `mod/IMPLEMENTATION-STATUS.md` |
| active issues | `ISSUES.md` |
| current testing | `TESTING.md` and `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` |
| open questions/owner decisions | `docs/OPEN-QUESTIONS.md` and `docs/PLAYABLE-PREVIEW-DIRECTIVE.md` |
