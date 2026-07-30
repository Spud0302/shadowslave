# GPT handoff — living checkpoint

> **Purpose:** this is the first file a new ChatGPT session should read before continuing work on this repository.
>
> **Maintenance rule:** update this file before ending a GPT work session whenever branch state, decisions, unfinished work, or the recommended next action changes.
>
> **Do not treat this as lore authority.** Canon research lives in `docs/lore-research/`.
>
> **Read `docs/ENGINEERING-NOTES.md` first if you are going to touch code.** It is Claude's
> reasoning behind the conventions in this repo — every rule paired with the incident that caused
> it. It exists so you do not have to reverse-engineer intent from the result, and so a guard whose
> purpose is invisible does not get refactored away. Highlights that have each cost real bugs:
> guards live at the choke point rather than in callers; an absent scoreboard score fails `matches`
> outright rather than reading as zero; player NBT cannot be written; a test assertion you have
> never seen fail is not an assertion; and when the harness and a direct probe disagree, the probe
> has been right every time.

## Repository / branch ownership

- Repository: `Spud0302/shadowslave`
- Claude Code is actively working on **`main`**.
- GPT must **not write directly to `main`** unless the project owner explicitly changes that rule.
- Current GPT implementation/review branch: **`gpt/review-improvements`**.
- **Andrew now gives most instructions to GPT** (2026-07-30); Claude owns implementation and
  testing. GPT writes the spec, Claude builds and verifies it. See `docs/COLLABORATION.md` for what
  a spec must contain, and **`docs/OPEN-QUESTIONS.md`** for questions Claude is waiting on — there
  are two open right now.
- **The agreed working protocol is `docs/COLLABORATION.md`** — branch ownership, the
  commit-attribution trailer, baseline-staleness rules, and who stamps versions. It supersedes any
  ad-hoc arrangement described elsewhere in this file.
- This branch was created from `main` commit `44986cbf7efee4052a0ed890cda21d7ec6caa113` (`v1.4.8`).
- At the time this checkpoint was written, the branch was **9 commits ahead / 0 behind** `main` before this handoff commit.
- Before doing substantial work in a new session, compare this branch with current `main` again because Claude may have moved it.

## Project goal

Build an unofficial Minecraft mod/datapack inspired by Guiltythree's *Shadow Slave*, with canon researched first and Minecraft mechanics derived from it.

The long-term goal is not merely to copy Sunny's powers. It is to make Minecraft capable of producing Shadow-Slave-like personal stories: Nightmares, unique Aspects, personal Flaws, Memories, Echoes, Soul progression, a persistent Dream Realm, Gates, and multiplayer consequences.

## Canon research status

The full A–F research pass has already been merged into `main` by Claude and lives in:

`docs/lore-research/`

Read `docs/lore-research/README.md` first. It links:

1. Section A — Aspects and Flaws (+ verification passes)
2. Section B — Ranks and progression
3. Section C — Memories
4. Section D — Nightmares, Seeds, Gates and Nightmare Creatures
5. Section E — Soul Cores, Corruption and Echoes
6. Section F — vocabulary, institutions, Spell voice and world texture
7. `minecraft-implementation-brainstorm.md`

Spoilers are unrestricted; the project owner is current with the novel.

NovelFull is explicitly allowed by the project owner for full-text research. Official WebNovel remains useful for verification/current release checks. Community wikis are secondary research aids.

Important canon corrections already established:

- First Nightmare completion produces a **Sleeper/Dreamer with Dormant Rank**, not an Awakened.
- Awakening occurs after the first Dream Realm journey and successful return through a Gateway.
- Canon **Attributes** are named supernatural traits, not generic RPG stats.
- Ordinary Memories are soul-stored but transferable; true Bound/Soulbound artifacts are exceptional.
- Soul Shards strengthen/saturate a core; they do not directly promote Soul Rank.
- Ordinary Echoes are static soulless replicas; Sunny's Shadows are a separate Aspect-specific system.
- Nightmares resolve a central conflict; they are not universally boss fights.
- There is a Seventh Nightmare beyond the six normal Rank-advancement Nightmares.
- There is no verified universal `Divine Sight` that lets every Awakened inspect another player's full status.

## Current live baseline reviewed

GPT reviewed `main` at `v1.4.8` / commit `44986cbf7efee4052a0ed890cda21d7ec6caa113`.

Claude had already:

- merged the GPT lore research into `main`;
- changed player-facing First Nightmare victory terminology from Awakened to **Sleeper (Dormant)**;
- fixed multiple entry/ejection/death/item-recovery bugs;
- hardened the static validator;
- built a Mineflayer behavioral harness with 25 assertions;
- confirmed the harness passes 25/25 across repeated runs on the baseline.

The detailed GPT review is:

`docs/reviews/2026-07-30-gpt-code-review.md`

Read that before changing Phase 1 logic.

## Overall review verdict

The Phase 1 datapack is technically strong for a prototype. Its best architectural patterns should survive future work:

- one Nightmare entry choke point;
- one teardown path;
- comments that preserve failed approaches/constraints;
- an offline static validator;
- live behavioral tests rather than syntax-only confidence.

There was no evidence of a catastrophic Phase 1 blocker in the reviewed baseline.

The main risks are now **prototype abstractions becoming permanent architecture**, stale docs, single-player global ownership, and hidden test state.

## Changes currently on `gpt/review-improvements`

These are proposals/review fixes only. They are **not merge-ready yet**.

### 1. Progression naming/refactor

Added:

- `shadowslave/data/shadowslave/function/progression/become_sleeper.mcfunction`
- `shadowslave/data/shadowslave/function/prototype/roll_aspect_flaw.mcfunction`

Normal First Nightmare victory now routes through `progression/become_sleeper`.

`awaken/roll.mcfunction` remains as a thin **compatibility alias** so old test/tooling references do not break.

Intent: separate the real progression transition from the temporary four-Aspect/four-Flaw generator without building an oversized framework in the datapack.

### 2. Soul readout terminology

`/trigger soul` no longer presents Minecraft max health and armor as canon `Attributes` / `Vitality` / `Endurance`.

Those numbers remain useful but are labelled as ordinary body/combat statistics. The real canon Attribute system remains future work.

### 3. `test/reset` hidden-state bug

A real bug was found during review:

1. calling `test/reset` inside a Nightmare first calls `nightmare/leave`;
2. `leave` sets `ss_cooldown = 600`;
3. baseline reset never cleared that cooldown;
4. therefore a supposedly clean test reset could contaminate the next run.

Review branch fix: reset clears cooldown and other scratch/return state after teardown.

**This fix still needs a regression test in the Mineflayer harness.**

### 4. Carrier guard at the choke point

Baseline normal callers required `ss_carrier`, but `nightmare/enter` itself did not. A direct function call could therefore put an untouched player into a First Nightmare.

Review branch fix: enforce Carrier eligibility inside `nightmare/enter` itself. The supported `test/nightmare` route deliberately supplies the testing state/bypass it needs.

**This also needs a Mineflayer regression assertion.**

### 5. Comment cleanups

Some Carrier/Sleeper/Awakened comments were corrected so future agents do not learn the old state model from source comments.

## Important findings deliberately NOT fixed yet

### Global Nightmare ownership

The datapack still assumes one active Nightmare/player at a time:

- global bossbar;
- global `ss_creature` selectors;
- teardown kills all Nightmare creatures;
- shared `shadowslave:ret` storage;
- scheduled death-drop sweep uses shared storage.

Do **not** sink a lot of datapack complexity into making this Phase-6-ready. Replace it with explicit instance ownership during the Java transition.

### Death-drop sweep

`sweep_move.mcfunction` teleports **every loose item in the Nightmare dimension** to the dead player's return location.

That can include unrelated mob drops/loot. It is a known prototype compromise, not precise ownership.

Do not replace it with a fragile radius guess just to make it look cleaner. Solve ownership properly in Java when death/inventory state can be tracked explicitly.

### `ss_rank`

Do not extend `ss_rank` into `0 sleeper, 1 awakened, 2 master...`.

Long term, keep separate concepts for:

- Spell/progression state;
- Soul Rank;
- Aspect Rank;
- core saturation/essence;
- exceptional core count/Class.

See the `SoulData` proposal in `docs/lore-research/minecraft-implementation-brainstorm.md`.

### One tag per Aspect/Flaw

Fine for Phase 1 placeholders; wrong for generated unique Aspects and personal Flaws. Do not expand this into dozens/hundreds of one-of-N tags.

## Documentation debt found

Current-facing docs on baseline are inconsistent with v1.4.8 runtime behavior.

Examples:

- `README.md` still says First Nightmare victory wakes the player **Awakened**.
- `TESTING.md` contains historical expectations and old balance/state values.
- `ISSUES.md` contains fixed items in sections that can look current.
- the old Phase 1 design spec still encodes pre-research lore assumptions.

Recommended approach: create/maintain one clearly authoritative current-state document (or make README that document), while marking historical plans/tests as historical instead of trying to rewrite development history.

## Best-practice rules for GPT work

1. Never write to `main` while Claude owns it.
2. Re-read/compare current `main` before substantial changes.
3. Use small, single-purpose commits.
4. Prefer behavior-preserving refactors before behavior changes.
5. Update tests/validator in the same work that changes behavior.
6. Do not create dead/orphan architecture.
7. Comments explain constraints/intent, not obvious syntax.
8. Label prototype limits and canon deviations explicitly.
9. Do not over-engineer future Java systems into the datapack.
10. Do not merge based on static reasoning alone.
11. Preserve existing save behavior unless migration is deliberately designed.
12. Keep canon claims separate from proposed Minecraft mechanics.

## REQUIRED next step — do this before more gameplay refactors

**Verify the review branch. Do not stack more behavior changes first.**

Run:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

Then add/confirm behavioral assertions for:

1. untouched player calling `nightmare/enter` directly is refused;
2. `test/reset` clears `ss_cooldown`;
3. reset also clears cooldown when invoked from inside a Nightmare;
4. `test/nightmare` still bypasses weakness/cooldown as intended;
5. normal victory still produces Sleeper + one placeholder Aspect + one placeholder Flaw;
6. `test/awaken` compatibility command still works.

The previous GPT session was interrupted while preparing to modify `testserver/harness.mjs`; **the regression tests above have not yet been added/run**.

## After verification

Recommended order:

1. fix any regression exposed by validator/harness;
2. clean README/current-facing docs;
3. decide which review-branch changes are worth proposing to Claude/merging;
4. keep Phase 1 stable;
5. decide/prepare the Java boundary before implementing Phase 2 Memories;
6. implement the real Sleeper -> Awakened Dream Realm/Gateway stage as its own feature, not another label patch.

## Coordination with Claude

Treat Claude as the active implementer on `main`.

Good workflow:

- Claude implements/tests on `main`;
- GPT researches, reviews, prototypes isolated improvements on `gpt/*` branches;
- GPT re-reviews current `main` before starting implementation;
- Claude can review GPT branches for Minecraft practicality;
- GPT can review Claude commits for canon/architecture drift;
- merge only after explicit owner review/approval.

Never assume `main` is stable just because this file names a baseline commit. Always compare refs first.
