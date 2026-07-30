# Collaboration protocol — Andrew, GPT and Claude through git

**Current protocol baseline:** `main@6a87991353480035f4fe6da08c775cd87d0e81df`  
**Repository is the communication channel:** if a decision, test result or disagreement is not
committed, recorded in a PR/issue, or linked from an authoritative document, the other agent cannot
rely on it.

## Roles

| Person/agent | Primary responsibility |
| --- | --- |
| Andrew | project owner, final decisions, player judgement and release approval |
| GPT | lore research, architecture, specs, code/docs on `gpt/*`, review and issue discovery |
| Claude | independent implementation review, all-agent testing, fixes, version stamping, merging and releases |

GPT may write Java, datapack content and documentation, but never directly to `main`. Claude is not
a ceremonial approver: automated CI does not replace Claude's independent inspection and test run.

## Branch and merge rules

1. GPT branches from the current remote `main` and records the exact baseline SHA.
2. GPT branches use `gpt/*`.
3. GPT opens a focused PR and leaves it for Claude; GPT does not merge its own feature or admin PR.
4. Claude reviews the diff, runs the required independent gate, records evidence/fixes and merges.
5. `main` is the accepted/release line. Claude may commit review fixes there or update the GPT branch,
   but must leave a durable explanation.
6. Andrew may override any design or release decision.

PRs #14 and #15 were incorrectly merged after CI without the separate Claude gate. Issue #16 records
the correction. No later Java feature package may merge until that issue closes.

## Commit attribution

- GPT: `Co-Authored-By: ChatGPT <gpt@openai.com>`
- Claude: use Claude's co-author trailer consistently.

## Test matrix

### Documentation-only change

Claude checks factual state, links, baseline, historical annotations and agreement between current
authority files. Runtime tests are needed only if the documentation change exposes uncertainty about
behaviour or changes generated/runtime resources.

### Frozen datapack touched

```bash
python3 shadowslave/tools/validate.py
cd testserver && npm test
cd .. && python3 shadowslave/tools/build_release.py
```

Claude confirms the deployed build/version before trusting a server result. A human fresh-world check
is required for presentation, balance, installation or dimension/worldgen changes.

### Java source/resources touched

```bash
./mod/gradlew -p mod build
./mod/gradlew -p mod runClientSmoke --no-daemon
./mod/gradlew -p mod runServerSmoke --no-daemon
```

Claude independently runs or inspects these rather than accepting GitHub's green badge alone.
Real-client interaction is additionally required for screens, key mappings, gameplay, persistence,
visual presentation or anything the smoke marker cannot demonstrate.

### Migration touched

Required evidence includes untouched, Carrier, generated identity, legacy identity, inconsistent
state rejection and idempotency. A live importer must persist, read back and verify Java state before
it marks completion or removes/ignores legacy values.

### Nightmare lifecycle touched

Tests must cover one eligibility choke point and one teardown service across victory, canonical death,
disconnect/restart recovery and administrator teardown. Multiplayer work also proves separate instance
ownership and no shared objective/entity/return state.

## CI policy

CI is intentionally throttled to avoid an email for every development commit. Prepare a draft PR, make
the branch coherent, then mark ready for one intentional run. A correction should re-test the corrected
head, not merely rerun an old commit. Successful CI is evidence for Claude, not permission for GPT to merge.

## File ownership and conflict avoidance

Natural strengths remain useful:

| GPT tends to own | Claude tends to own |
| --- | --- |
| canon/lore semantics, player-facing Spell copy, Aspect/Flaw definitions, architecture/specs | state-machine edge cases, test harnesses, live deployment, release/versioning, runtime verification |

This is not a fence. Cross-column edits are allowed when explicit in the PR and reviewed closely.
Prefer small packages and name every high-risk boundary touched.

## Engineering invariants

- guard eligibility at the single service/choke point;
- one teardown path handles every exit reason;
- client sends intent, never authoritative progression values;
- tests fail closed and prove their preconditions;
- know exactly which build produced a result;
- comments explain the failure/constraint a guard protects against;
- optional dependencies never own canonical Soul identity;
- no live migration deletes legacy state before verified persistence/read-back.

Datapack-specific command invariants remain in `docs/ENGINEERING-NOTES.md` and bind any future
maintenance of the frozen pack.

## Brief requirements

A handoff/spec includes:

- intent and owner goal;
- exact baseline commit;
- observable acceptance criteria;
- exact values/wording where they matter;
- what must not change;
- required automated and human evidence;
- known lore/design classification: bug, canon deviation, prototype compromise or preference.

Blocking implementation questions go in `docs/OPEN-QUESTIONS.md`. Concrete verification work may use
a GitHub issue, as with Issue #16.

## Historical records

Changelog entries, old testing plans, review docs and superseded specs remain evidence of what was
believed at the time. Do not silently rewrite them into present tense. Add a status banner, correction
or pointer to the resolution. Current truth belongs in `PROJECT-STATUS.md`, current README/status files,
open issues and the latest accepted implementation.

## Where to look

| Need | Authority |
| --- | --- |
| current state | `PROJECT-STATUS.md` |
| current GPT checkpoint | `GPT_HANDOFF.md` |
| canon/lore | `docs/lore-research/` and `docs/JAVA-LORE-ALIGNMENT.md` |
| Java migration/lifecycle contract | `docs/JAVA-HANDOFF.md` |
| current implementation | `mod/IMPLEMENTATION-STATUS.md` |
| active issues | `ISSUES.md` and GitHub issues |
| testing | `TESTING.md` |
| questions between agents | `docs/OPEN-QUESTIONS.md` |
