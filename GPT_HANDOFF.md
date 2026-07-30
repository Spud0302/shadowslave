# GPT handoff — living checkpoint

> **Purpose:** first file a new ChatGPT session should read before continuing work on this repository.
>
> **Maintenance rule:** update this file whenever GPT branch state, decisions, unfinished work, or the recommended next action changes.
>
> **Canon authority:** `docs/lore-research/`, not this file.
>
> **Engineering authority:** read `docs/ENGINEERING-NOTES.md` before changing code and `docs/COLLABORATION.md` before changing branches/process.

## Repository / branch ownership

- Repository: `Spud0302/shadowslave`
- Release/default branch: `main`
- GPT branch: **`gpt/datapack-release-completion`**
- GPT must not write directly to `main` unless the project owner explicitly changes that rule.
- GPT commits use `Co-Authored-By: ChatGPT <gpt@openai.com>`.
- Claude reviews/merges GPT branches, runs the real release gates, and stamps versions on `main`.

### Baseline

This branch was created from:

`main @ 70d3548089e0ef3503ba260a9021cb23d4ccbacd` (`v1.4.9`)

The older `gpt/review-improvements` checkpoint is historical: that work was validated, merged, and released as **v1.4.9** before this branch began.

Always compare this branch with current `main` again before new work because concurrent agents can move the baseline.

## Current project goal

Finish the vanilla datapack as a deliberate **completed Phase 1 reference implementation** before moving the project boundary to Java.

“Complete” does not mean hiding Java-only limitations with increasingly fragile commands. It means:

- no known datapack-fixable release blocker;
- trustworthy static and live release gates;
- current-facing docs that describe the real build;
- player-facing presentation rather than internal verification presentation;
- reproducible release packaging;
- Java-boundary prototype ceilings explicitly documented rather than repeatedly rediscovered.

The detailed scope/review artifact for this pass is:

`docs/reviews/2026-07-30-gpt-datapack-release-completion.md`

## Canon state that must not regress

The research set in `docs/lore-research/` established:

- First Nightmare completion produces a **Sleeper/Dreamer with Dormant Soul Rank**, not an Awakened.
- Actual Awakening follows the first successful Dream Realm journey and Gateway return.
- canon Attributes are named supernatural traits, not Minecraft health/armor stats;
- ordinary Memories are soul-stored but transferable;
- Soul Shards strengthen/saturate a core rather than directly promoting Soul Rank;
- ordinary Echoes are static soulless replicas; Sunny's Shadows are a distinct Aspect-specific system;
- Nightmares resolve a central conflict rather than universally requiring a boss kill.

Phase 1 intentionally stops at Sleeper. Do not extend `ss_rank` into the full future rank ladder.

## What is already complete on this branch

### 1. Release-gate harness hardened

`testserver/harness.mjs` was changed from 25 to **32 mechanical assertions**.

The important fixes are not simply “more tests”:

- expected command-query timeouts now throw instead of becoming empty strings that negative assertions could accept;
- `hasTag()` can no longer turn an unreadable tag query into `false`;
- dimension checks no longer fall back to Mineflayer's known-stale cached dimension;
- transition polling throws on timeout rather than returning an ambiguous last value;
- the weakness gate now requires both the refusal message **and** a confirmed non-Nightmare dimension;
- harness exceptions now force a non-zero process exit;
- the modifier-cleanup test polls the attribute instead of relying on a fixed one-second timing guess.

New direct regressions cover:

- untouched direct `nightmare/enter` refusal;
- `test/reset` clearing cooldown/transient state;
- reset from inside a Nightmare performing teardown and leaving no cooldown;
- `test/nightmare` bypassing both weakness and cooldown while consuming the bypass;
- Sleeper sleep staying out of the First Nightmare and granting `Sleep Undisturbed`.

`testserver/package.json` now maps `npm test` to `node harness.mjs`.

### 2. Static validator release invariant hardened

`shadowslave/tools/validate.py` still checks the existing structure/reference/worldgen/project-policy surface, and now enforces version agreement across all three hand-maintained literals:

1. `pack.mcmeta`
2. `function/init.mcfunction`
3. `function/test/selfcheck.mcfunction`

This matches the rule already documented in `ENGINEERING-NOTES.md`.

### 3. Reproducible release ZIP builder added

`shadowslave/tools/build_release.py`:

- runs the static validator first;
- extracts the version from `pack.mcmeta`;
- creates `shadowslave-vX.Y.Z.zip` at repository root;
- packages only `pack.mcmeta`, optional `pack.png`, and `data/**`;
- normalizes ZIP timestamps for byte-stable rebuilds;
- prints SHA-256;
- reminds the operator that the live harness is still required.

### 4. Advancement tree made public-facing

The historical `shadowslave:test/*` IDs remain unchanged for save/harness compatibility.

Only displayed copy changed:

- root is now **Shadow Slave**, not “Shadow Slave — Verification”;
- progression entries read as player achievements rather than test assertions.

The impossible-trigger/manual-grant mechanism remains unchanged.

### 5. Current source comments corrected

Comments/help text in the sleep, teardown, upkeep, Flame trigger, `test/awaken`, and `test/cure` paths now teach the actual Phase 1 terminal state: **Sleeper**, not Awakened.

Compatibility IDs such as `awaken/roll`, `test/awaken`, and `test/awakened` remain deliberately untouched.

### 6. README updated as current-state authority

README now documents:

- the completed Phase 1 boundary;
- the 32-assertion harness;
- three-file version validation;
- player-facing advancement tab;
- release ZIP builder;
- current function layout (`progression/`, `prototype/`, compatibility `awaken/`);
- Java-boundary prototype ceilings.

## Prototype ceilings that are intentionally NOT release blockers

Do not “fix” these in the datapack unless the project owner explicitly changes the boundary:

- **global Nightmare ownership:** one shared creature/bossbar/return storage; one active trial at a time;
- **broad death sweep:** every loose item in the Nightmare dimension is moved on the death-recovery path;
- **shallow `ss_rank`:** only enough state for the Phase 1 endpoint;
- **four fixed placeholder Aspects + four fixed placeholder Flaws:** do not expand into a huge tag catalogue;
- **ravager stand-in/custom AI ceiling:** Java problem;
- **Overworld-noise Nightmare terrain:** acceptable prototype worldgen;
- **no Soul GUI/custom persistent data model:** explicit Java forcing function;
- **instant-kill edge cases:** datapack cannot reliably intercept all real-death inventory behavior.

The Java migration should preserve the good architectural instincts—one entry choke point, one teardown service, data-driven resources, strong static/integration tests—while replacing state and ownership with proper objects/services.

## Still to finish on this branch

Before declaring the GPT branch review-ready:

1. update `TESTING.md` so its live section asks humans only for genuinely human-only checks;
2. mark the old Phase 1 design spec clearly historical/superseded rather than letting its pre-research “Awakened” model look current;
3. record the advancement-tab release concern in `ISSUES.md` as resolved without erasing historical context;
4. re-read the final branch diff/current docs for contradictions;
5. update this handoff again with the final branch state.

## Required review / release gate

GPT has **not** claimed live Minecraft verification from the GitHub connector environment.

Claude should run on the real 1.21.1 test server before merge:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

Expected mechanical count after this branch: **32 passed, 0 failed**.

Then build the candidate archive:

```bash
python3 shadowslave/tools/build_release.py
```

Claude should stamp the next Pride Versioning release on `main` only after review/gates pass.

## Process rules worth keeping visible

- Guard state transitions at their choke point, not in callers.
- An absent scoreboard score is not numeric zero.
- Player NBT can be read but not written by datapack commands.
- A test that cannot fail when behavior breaks is worse than no test.
- Prefer polling observable state over fixed scheduler sleeps.
- Preserve save compatibility unless migration is deliberate.
- Record prototype compromises once with their ceiling and upgrade path.
- Do not build future Java architecture as unused datapack seams.
- Never assume a named baseline is still current; compare refs before substantial work.
