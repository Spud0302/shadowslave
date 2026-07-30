# GPT pre-1.0 completion handoff

**Baseline:** `main@9f0cca9a0342f340cbdd9a72514363129f2156eb` (`0.5.0`)

**Branches:**

1. `gpt/v0.6-experience` — gameplay/presentation + Nightmare objective seam
2. `gpt/v0.7-hardening` — stacked on `v0.6`; deterministic Flaw verification + Java/release contracts

**Status:** code written; **not Minecraft-verified by GPT**. Claude owns validator/live execution/version stamping and should not merge based on static confidence.

## Why these are the remaining codeable stages

The project owner wants the datapack complete enough to become the behavioural reference for an actual Java mod, while reserving `1.0.0` for that completed datapack.

`0.5.0` already solved the largest gameplay identity problem: generated Aspect identities and behaviour-earned Flaws. What remains before the freeze is not Phase 2 content. It is:

- make the First Nightmare read as a finished vertical slice;
- stop the current boss-kill scenario from becoming the universal Nightmare abstraction in Java;
- close the known Flaw-family test coverage hole;
- write down exactly what state/behaviour Java must preserve;
- define a release gate strict enough that `1.0.0` means something.

`0.8.x` and `0.9.x` are deliberately **not pre-filled with invented features**. They are correction/RC stages whose contents depend on what testing actually finds. See `docs/PRE1-ROADMAP.md`.

## Branch 1 — `gpt/v0.6-experience`

### Runtime change: `nightmare/objective_tick`

The current scenario still does exactly this:

```text
90-second approach
-> spawn the existing Nightmare Creature
-> track its health
-> observe trial behaviour
-> leash it at 48 blocks
-> require two seconds of confirmed absence
-> survive
```

That machinery moved out of `nightmare/tick_player` and behind `nightmare/objective_tick`.

Intent: canon Nightmares share a central conflict, not a universal boss-kill shape. Java should implement scenario/objective types independently of entry, failure and teardown. The datapack does **not** need multiple elaborate scenarios before the port; it does need to stop teaching the wrong abstraction.

No values changed: countdown 1800, leash 48, gone threshold 40, spawn function and survive path all remain.

### Presentation

At timer values 1200 / 600 / 200 the current scenario emits one atmospheric beat. Exact matches mean one fire each and preserve the existing `ss_timer 1` testing shortcut.

`progression/become_sleeper` now appraises the earned Flaw family in project-authored Spell-style copy after the real generator runs. It does not expose the exact algorithm as canon and still tells the player to use `/trigger soul` for their generated names.

### Required verification

Run the normal gate unchanged. Pay particular attention to:

- countdown still starts near 1800;
- `ss_timer 1` still spawns the creature;
- bossbar handover still occurs;
- behaviour observation still happens before the leash;
- creature death still waits 40 absent ticks and reaches `survive`;
- no warning repeats/spams;
- `test/awaken` still sees "Sleeper" in command output despite the added appraisal line.

## Branch 2 — `gpt/v0.7-hardening`

### Q3 resolution: deterministic Flaw-family hooks

New test-only functions:

```text
shadowslave:test/flaw/baseline
shadowslave:test/flaw/bloodied
shadowslave:test/flaw/hungry
shadowslave:test/flaw/fled
```

They do not set `ss_flaw` or mechanics tags directly. They clear observations, inject the chosen observation tag, then execute the real `progression/become_sleeper -> prototype/roll_aspect_flaw` path.

That distinction matters: classification from actual combat remains human/integration behaviour, while generated score bands, tag selection and burden application become deterministic and machine-reachable.

### Dedicated family harness

`testserver/flaw_harness.mjs` checks:

- all four score bands (`11..14`, `21..24`, `31..34`, `41..44`);
- correct family tag;
- exactly one Flaw mechanics tag;
- exactly one Aspect mechanics tag;
- valid two-axis Aspect encoding;
- observation tags consumed after generation;
- reduced-health burden applies and reset removes it;
- hunger burden applies and reset clears the effect;
- fall-distance burden applies and reset removes it;
- production precedence remains `fled > hungry > bloodied` when all observations are present.

`npm test` now runs the existing main harness **then** this dedicated harness. I intentionally did not rewrite the 533-line main harness just to bolt Q3 onto it.

### `test/reset`

Adds explicit clearing for every short-lived effect the pack itself can apply during verification: night vision, speed, fire resistance, jump boost, hunger, weakness, blindness and nausea.

This is test hygiene, not normal gameplay teardown.

### Java/release contracts

- `docs/JAVA-HANDOFF.md` maps current persistent state to the future typed data model and explicitly says what **not** to import.
- `docs/RELEASE-CHECKLIST.md` defines automated, human, RC and `1.0.0` gates.
- `README.md` is brought current, including the objective seam and generated Aspect/Flaw state.

## Cross-column edits to scrutinise

Per `COLLABORATION.md`, these are naturally Claude-owned machinery files even though Andrew asked GPT to code as much of the remaining work as possible:

- `nightmare/tick_player.mcfunction` — refactor only; lifecycle/threshold behaviour should be unchanged.
- `test/reset.mcfunction` — verification hygiene only.
- `testserver/package.json`
- `testserver/flaw_harness.mjs`

Do not preserve my implementation if a live test proves it wrong. Preserve the intent/acceptance criteria.

## Commands for Claude

On `gpt/v0.6-experience`:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

On `gpt/v0.7-hardening` after/with the parent branch:

```bash
python3 shadowslave/tools/validate.py
cd testserver && npm test
cd .. && python3 shadowslave/tools/build_release.py
```

No expected pass count is asserted here for the new family harness until it actually runs. A test total written before execution is theatre.

## Human checks that cannot be delegated to the bot

- countdown beats improve atmosphere and do not obscure combat/UI;
- full natural First Nightmare win still feels coherent;
- appraisal wording feels like consequence rather than a debug classifier;
- natural behaviour actually reaches the intended Flaw families at reasonable rates;
- death-drop recovery remains sane;
- generated release ZIP installs in a fresh 1.21.1 world.

## Changelog/version handling

The repository already has `CHANGELOG.md`; do not create a second changelog. Claude still owns version stamping on merge. Record the actual tested result under the version that ships rather than pretending these unverified branches are releases already.

Suggested release-note summaries if the branches pass:

### `0.6.0`

> First Nightmare experience and objective boundary: the existing scenario keeps its balance but its conflict/win machinery is isolated from player lifecycle, countdown presentation gains one-shot atmospheric beats, and Sleeper progression appraises the earned Flaw family.

### `0.7.0`

> Verification and Java handoff hardening: all four Flaw families are deterministically machine-reachable through the real generator, `npm test` covers family mechanics/cleanup, reset clears transient pack effects, and the Java migration plus `1.0.0` release contracts are explicit.

If testing finds bugs, Pride Versioning applies normally: stamp the release that actually shipped, then SHAME-bump fixes rather than rewriting history.
