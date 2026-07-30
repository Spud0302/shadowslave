# GPT datapack release-completion pass

**Original baseline:** `main` at `70d3548089e0ef3503ba260a9021cb23d4ccbacd` (`v1.4.9`)

**Branch:** `gpt/datapack-release-completion`

**Latest `main` observed during final review:** `a470b914f3e0710d3dfee63adc29b8e6e50d4599`

**Goal:** leave the vanilla datapack in a deliberately completed, release-ready Phase 1 state before Java work begins.

This pass treats “complete” as **no known datapack-fixable release blocker, a trustworthy release gate, truthful current-facing documentation, public-facing presentation, and a reproducible release artifact**. It does not force Java-boundary ownership/state problems into more command machinery.

## Baseline moved while this branch was in progress

`main` advanced from `70d3548` to `a470b91` while this branch was already being written. The intervening commits are collaboration/process documentation, not a new gameplay release.

The newest `docs/COLLABORATION.md` assigns harness/validator/release machinery primarily to Claude and player/canon content primarily to GPT, while explicitly saying neither list is a hard fence. This branch had already changed `testserver/harness.mjs`, `shadowslave/tools/validate.py`, and added `shadowslave/tools/build_release.py` before that split landed.

**Process exception for Claude's review:** treat those machinery changes as GPT proposals that need Claude's normal extra scrutiny/testing. This branch is not claiming ownership of those files. The same applies to the comment-only edit in `nightmare/leave.mcfunction`. If Claude prefers to re-implement a machinery change while preserving the acceptance criteria below, that is compatible with the intent of this pass.

## Completion boundary

### Finished on this branch

- The release harness fails closed rather than turning unreadable state into a negative result.
- Five missing `v1.4.9` regressions were added; the mechanical suite is now **32 assertions**.
- The static validator enforces all three hand-maintained release version literals.
- The advancement tree is player-facing while retaining historical ids for save/harness compatibility.
- Current README/testing/issues/handoff documentation describes `v1.4.9` reality rather than historical pre-research behavior.
- Current source comments stop teaching “First Nightmare -> Awakened”; Phase 1 terminates at Sleeper/Dormant.
- A reproducible release-ZIP builder exists and excludes development/test files from the public archive.
- `npm test` now invokes the Mineflayer harness instead of the package.json placeholder failure.

### Deliberately complete *with* these prototype ceilings

These remain **prototype compromises**, not release bugs:

- one active Nightmare at a time through a global creature/bossbar/return-storage model;
- death recovery sweeping every loose item in the Nightmare dimension;
- `ss_rank` as a shallow Phase 1 state flag rather than a future Soul data model;
- the current finite placeholder power catalogue until the dedicated Aspect/Flaw redesign is resolved;
- the ravager stand-in, Overworld-noise Nightmare terrain, no custom GUI, and no custom AI;
- historical compatibility ids/names such as `awaken/roll`, `test/awaken`, and `test/awakened`.

The first three and the entity/world/data-model ceilings are clear Java migration seams. The Aspect/Flaw catalogue now has a newer explicit handover in `docs/OPEN-QUESTIONS.md` (Q2); see “Separate follow-up scope” below.

## Release-gate findings and fixes

### Q1 answer — assertions that could pass without proving their behavior

Current `main` added `docs/OPEN-QUESTIONS.md` after this branch diverged. Q1 asks which harness assertions could not fail if the behavior broke. This branch found and addresses the following classes:

1. **Weakness-gate OR hole.** The old condition accepted either the refusal text **or** being outside the Nightmare. A broken function could print the refusal and then fall through into the Nightmare and still pass. It now requires both.
2. **`hasTag()` timeout -> false.** A failed tag-list query was indistinguishable from confirmed tag absence, allowing negative tag assertions to pass without observation. Expected query timeouts now throw.
3. **Stale dimension fallback.** `dimension()` could fall back to Mineflayer's cached dimension even though this project already proved that cache can stay stale after command teleports. The helper now requires a server query that parses.
4. **`null !== nightmare` false-pass.** Several negative dimension checks accepted a failed/null read as “not in Nightmare.” Dimension reads and transition waits now fail closed.
5. **Transition timeout ambiguity.** `waitDimension()` returned its last value after timeout; it now throws if the requested transition was never observed.
6. **Attribute parse defaults.** The re-roll test defaulted unreadable values to `0`/`-1`, which could satisfy a less-than comparison and report success. Attribute reads now parse-or-throw and the upkeep effect is polled rather than guessed with a fixed sleep.
7. **Harness exception -> exit 0.** The catch set `process.exitCode = 1`, but the old `finally` called `process.exit(fail.length ? 1 : 0)`, so an exception before an assertion failure could override the error with exit 0. Exceptions now enter the failure list before the final exit.

**Merge note:** because `docs/OPEN-QUESTIONS.md` exists only on the newer `main` side of this divergence, this branch deliberately does not add a competing copy. When accepting this branch, Claude should move Q1 to **Answered** on `main` and point to this review/commit history.

### New direct regressions

The harness now directly verifies:

- an untouched player calling `nightmare/enter` is refused at the choke point;
- `test/reset` clears cooldown/transient state from an ordinary state;
- `test/reset` invoked inside a Nightmare performs teardown and leaves no cooldown behind;
- `test/nightmare` bypasses weakness and cooldown while consuming the single-use bypass;
- a Sleeper's ordinary sleep remains outside the First Nightmare and grants `Sleep Undisturbed`.

Together with the pre-existing coverage, the expected suite size is **32 passed, 0 failed** once Claude runs it on the real server.

## Static/release hardening

### Three-way version agreement

`validate.py` now requires one `vX.Y.Z` value across:

1. `pack.mcmeta`
2. `data/shadowslave/function/init.mcfunction`
3. `data/shadowslave/function/test/selfcheck.mcfunction`

GPT intentionally did **not** change those version values. Version stamping remains Claude's merge/release responsibility.

### Reproducible release ZIP

`shadowslave/tools/build_release.py`:

- runs the static validator first;
- reads the version from `pack.mcmeta`;
- packages only `pack.mcmeta`, optional `pack.png`, and `data/**`;
- puts `pack.mcmeta` at archive root rather than under a wrapper directory;
- normalizes archive timestamps so identical source produces a stable ZIP;
- prints the output SHA-256;
- explicitly reminds the operator that the live Mineflayer gate is still required.

## Player-facing/documentation hardening

- Existing advancement resource ids remain `shadowslave:test/*`; only display copy changed. This avoids save/test migration solely for presentation.
- The root tab now displays **Shadow Slave**, not **Shadow Slave — Verification**.
- README is the current runtime authority and now describes the Phase 1 completion boundary, 32-assertion gate, three-way version rule, builder, and Java migration ceilings.
- `TESTING.md` retains old sweeps as history and limits the current release checklist to genuinely additional evidence.
- `ISSUES.md` opens with current status and clearly fences the old issue log as historical.
- `docs/superpowers/specs/README.md` marks the pre-research Phase 1 design spec historical/superseded rather than rewriting its body as if it always knew later canon.
- `GPT_HANDOFF.md` is being maintained as the live GPT checkpoint.

## Remaining release verification — NOT performed by GPT

GPT's connector environment does not provide the project's live Minecraft 1.21.1 test server. Therefore this branch does **not** claim the following have passed.

Claude must run before merge:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

Expected harness count if the branch is correct:

```text
32 passed, 0 failed
```

Then build the candidate archive:

```bash
python3 shadowslave/tools/build_release.py
```

The live/manual release-candidate checks in `TESTING.md` are:

- **H1:** death-screen presentation — no visible teleport/portal flash before respawn;
- **H2:** shortened natural cooldown-expiry smoke test — complements refusal and sleep-clear automation;
- **H3:** install the generated ZIP into a fresh 1.21.1 world and verify datapack registration, selfcheck, advancement presentation, and custom-dimension entry.

Item recovery on death and the full winning loop are already human-confirmed and should only be repeated after changes to their owning paths.

## Separate follow-up scope: Aspect/Flaw redesign (new main Q2)

The newest `main` introduced Q2 in `docs/OPEN-QUESTIONS.md` while this release-hardening branch was already underway. It hands the lore-derived Aspect/Flaw rework to GPT and records the owner's earlier desire for generated Aspect identity plus behavior-influenced Flaws with some randomness.

That is materially larger than release hardening and should **not** be stacked into this branch:

- it changes gameplay/canon semantics, not merely release confidence;
- it may replace the current `prototype/roll_aspect_flaw.mcfunction` seam and affect `soul`, `upkeep`, `test/reset`, and harness expectations;
- the collaboration protocol now explicitly recommends small focused branches and file-level ownership coordination.

**Recommendation:** merge/verify this hardening branch first. Then, if “completed datapack before Java” includes replacing the finite placeholder catalogue, make Q2 the next dedicated GPT branch from the new `main`. That branch can decide how far generated identity/behavior-derived Flaws can honestly go in vanilla commands without pretending arbitrary generated mechanics are possible.

Until that decision lands, the current finite powers are a documented Phase 1 prototype seam, not a hidden defect.

## Review verdict

From static repository review, the release-hardening work is ready for Claude's live verification and code review. It is **not release-approved yet** because the required real-server gates have not been run on this branch, and GPT does not stamp or merge releases.
