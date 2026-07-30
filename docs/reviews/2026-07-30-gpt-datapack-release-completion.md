# GPT datapack release-completion pass

**Baseline:** `main` at `70d3548089e0ef3503ba260a9021cb23d4ccbacd` (`v1.4.9`)

**Branch:** `gpt/datapack-release-completion`

**Goal:** leave the vanilla datapack in a deliberately completed, release-ready Phase 1 state before Java work begins.

This does **not** mean forcing systems into commands that the repository has already identified as Java-boundary problems. It means the Phase 1 datapack should have no known datapack-fixable blocker, its release gate should be trustworthy, its current-facing documentation should tell the truth, and its public-facing presentation should not look like an internal verification build.

## Completion boundary

### Must be finished in the datapack

- Release-gate tests must fail when the behaviour they describe breaks, and must not turn query timeouts or malformed replies into passes.
- The static validator must enforce the repository's own three-file version agreement (`pack.mcmeta`, `init.mcfunction`, `test/selfcheck.mcfunction`).
- Current-facing documentation and the live human-test list must reflect `v1.4.9` behaviour rather than historical states.
- The shipped advancement tab must read as player-facing progression rather than an internal "Verification" screen while retaining existing ids for save/test compatibility.
- Source comments that still teach the pre-`1.4.8` First Nightmare -> Awakened model should be corrected.
- There should be a reproducible release-zip path whose archive root is correct for Minecraft.

### Deliberately complete *with* these prototype ceilings

The following are already recorded by both GPT and Claude as **prototype compromises**, not release bugs, and this pass will not grow fragile command machinery to hide them:

- one active Nightmare/player at a time via a global creature and bossbar;
- death recovery sweeping every loose item in the Nightmare dimension;
- `ss_rank` as the shallow Phase 1 "survived First Nightmare" flag;
- four fixed placeholder Aspects and four fixed placeholder Flaws;
- the ravager stand-in, Overworld-noise Nightmare terrain, no GUI and no custom AI;
- historical compatibility ids/names such as `awaken/roll`, `test/awaken` and `test/awakened`.

Those are migration seams for the Java port, not reasons to keep the datapack perpetually "unfinished".

## Release-gate findings

### Bug: the weakness-gate assertion can pass after a broken return

The harness currently accepts:

```js
/too weak/i.test(weakEntry) || dimAfterWeak !== 'shadowslave:nightmare'
```

If `enter.mcfunction` prints the refusal but then accidentally continues and teleports the player, the first half is true and the test still passes. The assertion must require both the refusal and a confirmed non-Nightmare dimension.

### Bug: failed reads can satisfy negative assertions

Several harness helpers/assertions collapse "could not prove the state" into a negative result:

- `hasTag()` returns `false` if the expected tag-list reply never arrives;
- `dimension()` falls back to Mineflayer's cached dimension even though the file documents that cache as stale after command teleports;
- multiple checks use `dimension !== 'shadowslave:nightmare'`, so `null` can count as success.

The release gate should distinguish **confirmed absence** from **failed observation**.

### Bug: a harness exception can still exit zero

The `catch` sets `process.exitCode = 1`, but `finally` then calls `process.exit(fail.length ? 1 : 0)`. If the harness throws before recording an assertion failure, that explicit `process.exit(0)` overrides the earlier failure state.

### Missing regression coverage

The merged `v1.4.9` fixes deserve direct assertions for the invariants they repaired:

- an untouched player calling `nightmare/enter` directly is refused;
- `test/reset` clears cooldown/transient state;
- reset invoked inside a Nightmare performs teardown and still leaves no cooldown behind;
- `test/nightmare` bypasses cooldown as well as weakness and consumes the bypass;
- a Sleeper's ordinary sleep grants `Sleep Undisturbed` without entering another First Nightmare.

## Documentation/release debt

- `GPT_HANDOFF.md` still describes the already-merged `gpt/review-improvements` branch as pending.
- The old Phase 1 design spec still opens with pre-research/pre-implementation assumptions; it should be marked historical/superseded rather than rewritten.
- The live section of `TESTING.md` still asks a human to repeat checks the harness now covers.
- Several source comments still say "Awakened" where runtime progression is Sleeper (Dormant).
- The advancement root still ships as **Shadow Slave — Verification** even though the pack is approaching a public release.
- README documents installing a `.zip`, but the repository has no obvious reproducible packaging command.

## Verification requirement

Before merge, run on the real 1.21.1 test server:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

Per `docs/COLLABORATION.md`, GPT does not claim those live checks have passed from connector-side reasoning. Claude should run both when reviewing this branch and stamp the release version only after they pass.
