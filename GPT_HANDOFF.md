# GPT handoff — living checkpoint

> **Read first in a new GPT session.** Canon authority is `docs/lore-research/`; engineering authority
> is `docs/ENGINEERING-NOTES.md`; workflow authority is `docs/COLLABORATION.md`.

## Repository state

- Repository: `Spud0302/shadowslave`
- Release/default branch: `main`
- Current release baseline: **v1.4.9**
- Latest `main` incorporated into this branch: `a470b914f3e0710d3dfee63adc29b8e6e50d4599`
- GPT branch: **`gpt/datapack-release-completion`**
- Pull request: **#1 — Datapack release-completion hardening**
- Original pre-reconcile branch history is preserved at:
  `gpt/datapack-release-completion-pre-reconcile`

The branch originally started from `main@70d3548089e0ef3503ba260a9021cb23d4ccbacd`. While GPT was
working, `main` gained three collaboration/process commits and the PR became unmergeable because both
sides had edited README/GPT_HANDOFF. The final reviewed tree was rebuilt on top of
`main@a470b914f3e0710d3dfee63adc29b8e6e50d4599`; no release or gameplay commit from `main` was lost.

GPT does not write directly to `main`. Claude reviews/merges GPT branches, runs live verification,
stamps versions, and ships releases.

## Current project goal

The owner's current instruction is to get the **datapack fully finished and release-ready before
moving to Java**.

There are now two separate pieces of work:

1. **Release hardening** — this PR. It is complete from GPT's repository-review side and awaits
   Claude's real-server verification/review.
2. **Aspect/Flaw rework** — current-main `docs/OPEN-QUESTIONS.md` Q2. This is the remaining explicit
   pre-Java gameplay/canon task. Do it on a separate focused GPT branch; do not stack it into PR #1.

This distinction matters. The global Nightmare ownership, broad death sweep, ravager stand-in,
Overworld-noise terrain, shallow `ss_rank`, GUI/data-model limits and bespoke AI are Java-boundary
ceilings. The **four fixed Aspects/four fixed Flaws are not being silently waved through anymore**:
Q2 explicitly hands their redesign to GPT.

## Collaboration rules currently in force

The latest `docs/COLLABORATION.md` says the split is by file rather than by role.

GPT's natural side:
- lore-derived Aspect/Flaw content;
- `prototype/roll_aspect_flaw.mcfunction` and what replaces it;
- player-facing copy / Spell voice;
- progression semantics and rank/canon naming;
- canon research/design docs.

Claude's natural side:
- Nightmare state machine, guards, teardown, cooldown/threshold logic;
- `testserver/harness.mjs`;
- `shadowslave/tools/validate.py`;
- dimension/worldgen/macro/storage plumbing;
- version stamping, packaging and release.

Neither list is a hard fence, but cross-column changes must be called out. Two hard rules never move:

1. Claude runs validator + harness before anything merges.
2. `ENGINEERING-NOTES.md` invariants bind both agents.

### Process exception in PR #1

The harness, validator and release-builder work in PR #1 was written **before** the newest file split
landed on `main`. The PR/review doc calls those changes out explicitly as GPT proposals requiring
Claude's implementation-level scrutiny. The `nightmare/leave.mcfunction` edit is comment-only.

Do not read this exception as ownership precedent for future work.

## PR #1 — what is finished

Detailed review artifact:

`docs/reviews/2026-07-30-gpt-datapack-release-completion.md`

### Harness hardening

`testserver/harness.mjs` grows from 25 to **32 mechanical assertions** and now fails closed.

Fixed false-confidence paths:

- expected-query timeouts no longer become empty/negative results;
- `hasTag()` cannot convert an unreadable query into confirmed absence;
- dimension checks no longer fall back to Mineflayer's known-stale cached dimension;
- null/unreadable dimension state cannot satisfy a “not in Nightmare” assertion;
- transition polling throws on timeout;
- attribute reads parse-or-throw instead of using pass-friendly defaults;
- weakness-gate assertion now requires refusal **and** confirmed non-entry, rather than OR;
- harness exceptions enter the failure list before process exit, so they cannot exit 0.

New direct regressions cover:

- untouched direct-entry refusal at `nightmare/enter`;
- `test/reset` clearing transient cooldown state;
- reset inside a Nightmare performing teardown and leaving no cooldown;
- `test/nightmare` bypassing weakness and cooldown and consuming its bypass;
- Sleeper sleep staying outside another First Nightmare and granting `Sleep Undisturbed`.

This is GPT's concrete answer to current-main **OPEN-QUESTIONS Q1**. Because that file was created on
`main` after the original branch diverged, PR #1 does not rewrite it. Claude should move Q1 to
Answered on `main` when accepting the result.

### Static/release hardening

`shadowslave/tools/validate.py` still covers the existing structural/reference/worldgen/project
policy checks and now enforces version agreement across:

1. `pack.mcmeta`
2. `function/init.mcfunction`
3. `function/test/selfcheck.mcfunction`

`shadowslave/tools/build_release.py` was added as a reproducible public-ZIP builder:

- runs static validation first;
- packages only `pack.mcmeta`, optional `pack.png`, and `data/**`;
- keeps `pack.mcmeta` at archive root;
- normalizes ZIP timestamps;
- prints SHA-256;
- does not pretend the live harness was run.

`testserver/package.json` now maps `npm test` to the real harness.

### Player/release presentation

- advancement display is now a player-facing **Shadow Slave** tree;
- historical `shadowslave:test/*` ids are retained for save/harness compatibility;
- source/help comments use the correct Phase 1 endpoint: Sleeper/Dormant, not Awakened;
- README, TESTING and ISSUES were updated to distinguish current behavior from historical records;
- `docs/superpowers/specs/README.md` marks the old pre-research design spec historical/superseded;
- current manual release checks are reduced to genuinely additional evidence rather than repeating
  harness coverage.

## PR #1 — verification still required

GPT **cannot claim live success** from the connector environment.

Claude must run:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

Expected result if PR #1 is correct:

```text
32 passed, 0 failed
```

Then:

```bash
python3 shadowslave/tools/build_release.py
```

And the current release-environment/human checks in `TESTING.md`:

- H1 death-screen presentation;
- H2 shortened natural cooldown-expiry smoke test;
- H3 generated ZIP installed into a fresh 1.21.1 world.

No version files were changed by GPT. Claude stamps the next Pride Versioning release only after the
review/gates pass.

## Next datapack task before Java — Q2 Aspect/Flaw redesign

Current `main` explicitly hands this to GPT.

### Why it remains

The current implementation rolls one of four Aspects and one of four Flaws independently. Canon
research says:

- Aspects are unique/personal rather than a universal four-item list;
- Flaws are personal rather than an independent random penalty;
- a First Nightmare's Dormant Aspect can begin deliberately weak and become meaningful through later
  progression rather than granting a near-final power in the tutorial.

The repository also records the owner's earlier direction:

- **generated Aspect identity**, not simply choosing from a fixed name list;
- Flaws influenced/earned by behavior in the trial, with some randomness so identical play does not
  necessarily produce identical results.

### Vanilla datapack ceiling

Names/descriptions can be composed with macros/storage, but arbitrary new behavior cannot be created
at runtime: each mechanical effect still needs pre-existing command logic. Do not fake “infinite
procedural powers” when the actual behavior is one of a small hidden list.

The right Phase 1 target is therefore a **generated identity layered over a finite, honest mechanical
vocabulary**, plus behavior-derived Flaw selection, unless the owner explicitly chooses a different
boundary.

### Machinery constraints to preserve

- `prototype/roll_aspect_flaw.mcfunction` is the intended replacement seam.
- Every persistent attribute modifier needs paired remove-before-add.
- Never write player NBT.
- If one-tag-per-power changes, `test/reset`, `soul`, `upkeep`, advancements and the harness all need
  a migration plan.
- Keep this rework on a new focused `gpt/*` branch; Claude owns any new harness/validator plumbing
  needed to verify it.

## Recommended next action

1. Claude reviews PR #1 and runs the required gates.
2. GPT starts the dedicated Aspect/Flaw rework from the latest `main` (or rebases after PR #1 lands),
   using Q2 and `docs/lore-research/` as the design authority.
3. Only after that gameplay rework is merged/verified should the project honestly call the datapack
   final and move the architecture boundary to Java.

Do not return to the old assumption that the fixed four/four catalogue automatically waits for Java;
the newest repository state supersedes that decision.
