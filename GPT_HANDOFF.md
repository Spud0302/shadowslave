# GPT handoff — current Java/core baseline

**Read first in a new GPT or Claude session.**  
**Repository:** `Spud0302/shadowslave`  
**Baseline branch:** `main`  
**Baseline commit:** `e9f676a9b2ef58cf1e0cc18d45d1420b9e62c2f4`  
**Active correctness candidate:** PR #119, head `93da9d43df160995097a5108f0e73ef6a5762046`

## Mandatory lore source

Before any lore-sensitive design, implementation, naming or review, read `docs/LORE-SOURCE-POLICY.md` and then `docs/JAVA-LORE-ALIGNMENT.md` plus the relevant roadmap/research notes.

Novel chapter text is authoritative. NovelFull is the owner-designated working access layer, not canon by itself; official WebNovel is the publication/wording cross-check. Verify later clarifications, summarize rather than copy source text, and keep **CANON**, **INFERRED**, **DESIGN**, **UNKNOWN**, and compatibility consequences distinct.

## Current main

`main` has moved materially beyond the August 1 preview baseline. It now includes, among other merged foundations:

- explicit multi-ability Aspect identity migration;
- The Drowned Bell authored Nightmare scenario;
- reusable Nightmare Creature content;
- authored Memory and Echo catalogues;
- Dream Realm region content;
- stabilized frozen-datapack trial-lock regression behavior.

Do not treat the old `0.1.0-preview.2` artifact as evidence for these newer changes.

## Highest-priority correctness work

Issue #34 remains the primary persistence blocker: successful Nightmare completion must survive a real server/process failure without zero appraisal, duplicate appraisal, duplicate teardown, stale ownership, or incorrect return state.

The active stacked lineage through PR #119 has implemented and tested the software side of that recovery path:

- durable completion phases/receipt;
- fail-closed registry reconstruction;
- idempotent appraisal reconciliation;
- joined SavedData durability checkpoints;
- return-success observation before commit;
- server runtime adapter;
- live terminal and login recovery routing;
- deterministic six-boundary process fault injection;
- stale-attempt invalidation;
- recovery-process authentication;
- source/world provenance checks;
- same-player two-login convergence evidence;
- hosted self-tests for both evidence verifiers.

Exact PR #119 head `93da9d43df160995097a5108f0e73ef6a5762046` passed Preview Gates run #89 / ID `31280686707`.

## Do not add more speculative persistence state

The next required proof is physical:

```text
after_terminal_registry_save
```

Run the Issue #34 completion-fault procedure with a real player, preserve the disposable world, restart on the same source/world, reconnect the same player, relog once more, and retain the authenticated logs/state.

This connector-only/GitHub-only environment cannot perform the real player actions. Treat that as an execution blocker, not as evidence that another transaction layer is needed. Resume code changes only if the physical row demonstrates a concrete defect, owner input changes the requirement, or a new runtime/dependency behavior provides a credible new approach.

## Verification rules

For Java/core changes:

```bash
./mod/gradlew -p mod clean build
mod/verify-smoke.sh
```

For the frozen datapack:

```bash
cd testserver
npm run deploy
npm test
```

For completion evidence infrastructure on the #119 lineage:

```bash
bash mod/run-completion-fault-row.sh self-test
bash mod/verify-completion-player-recovery.sh self-test
```

Hosted CI may be reported only for the exact tested commit. Physical NeoForge boot is not equivalent to Issue #34's process-kill/restart matrix.

## Frozen datapack boundary

Issue #20 remains a known command-era architecture limitation. The supported datapack contract is one active First Nightmare at a time with a persistent global lock. The deeper global `@e[tag=ss_creature]` selector limitation remains deliberately documented and outside the supported concurrent model.

A single Issue #20 dimension-transition timeout occurred on PR #117 run #88; its Java job passed. Descendant PR #119 run #89 passed the complete workflow, so do not repeatedly retry or redesign that harness without new failure evidence.

## Workflow rules

- Never write directly to or merge into `main`; use a new `gpt/` branch and reviewable PR.
- Every GPT commit includes `Co-Authored-By: ChatGPT <gpt@openai.com>`.
- Read current main, open issues/PRs, status docs, lore policy/alignment and current roadmap/research notes before selecting work.
- Avoid duplicating active PRs.
- Prioritize correctness/persistence blockers over new content.
- After two consecutive no-progress runs on the same blocker, record attempts/evidence and exact resume condition, mark/comment it blocked, and move to the next unblocked slice.
- Do not invent canon. When source access or owner decisions block lore-sensitive implementation, record the blocker and do research/design/test work instead.
- Preserve historical findings; append/supersede them explicitly rather than rewriting old evidence as though it never happened.
