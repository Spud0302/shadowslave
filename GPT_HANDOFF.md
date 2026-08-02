# GPT handoff — verified playable preview

**Read first in a new GPT or Claude session.**  
**Repository:** `Spud0302/shadowslave`  
**Current main:** `c3ffcd9c3f6139817fe84ef3c81d94ceafdda4e3`  
**Merged PR:** #19  
**Java build:** `0.1.0-preview.2`

## Current state

The playable Java preview and live frozen-datapack migration are merged into `main`. Claude independently re-ran the complete machine gate on the merge result and merged PR #19.

Verified:

```text
validator                         clean
frozen-datapack lifecycle         32/32
frozen-datapack Flaw suite        39/39
disconnect/reconnect regression   PASS, exit 0, repeatable twice
Java unit suite                   35 tests, 0 failures
physical-client smoke             PASS via verify-smoke.sh
dedicated-server smoke            PASS via verify-smoke.sh
```

The regression also proved cleanup: `$global ss_trial_lock` returned to `0` and no stray trial creature remained.

## Artifact

```text
shadowslave-0.1.0-preview.2.jar
SHA-256 48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

Runtime source: `9cbfe57a05095e31c1980093e4d57ea9a2f7e10c`. Later merged changes affect the frozen datapack, tests, workflow, and documentation rather than the Java runtime bytes.

## Playable loop

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

The scenario, role, fixed appraisal, Aspect, ability, and Flaw are **DESIGN**, not asserted canon.

## What remains unproven

Andrew has not yet completed the real-client play/feel pass. Use `mod/PREVIEW-PLAY-GUIDE.md` and `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` to test:

- O-screen presentation;
- exact signal completion and return;
- relog/restart persistence;
- death and technical-recovery wording;
- two-player Java instance separation;
- pacing, readability, balance, and feel;
- migration on a backed-up real datapack world.

Deferred is not passed. Do not convert machine verification into a human-experience claim.

## Open issue queue

### #20 — frozen datapack global selector limitation

The supported one-slot contract is safe, including disconnect/reconnect ownership. The datapack still uses global `@e[tag=ss_creature]` selectors inside its single Nightmare dimension, so a manually introduced unrelated tagged entity can affect the objective. Keep the defect probe outside the gate and keep #20 open. Per-entity ownership is implemented in Java rather than retrofitted into the frozen command prototype.

### #29 — persisted codec invariant sweep

`PreviewPowerData` can throw from its compact constructor when decoding corrupt negative cooldown data because the persisted codec calls the constructor directly. Fix it using the `StoredSoulData` pattern and add a regression that feeds invariant-violating payloads to every registered persisted attachment codec, asserting `DataResult.error` rather than a thrown exception.

This is low severity: normal gameplay writes cannot produce a negative cooldown. It is still the next clean corrective package before broader feature work.

## Next engineering package

Create a new `gpt/*` branch from current `main` and:

1. audit every persisted attachment codec;
2. route throwing invariants through non-throwing storage records plus explicit `DataResult` validation;
3. add one malformed-input regression per registered attachment;
4. bump the development preview only if JAR bytes change;
5. build test criteria alongside the work; Claude can review in the next bulk pass.

## Lore and architecture rules

- Never assume lore; check primary novel evidence before adding or generalising mechanics.
- Label project inventions **DESIGN**.
- Mundane is descriptive, not Soul Rank zero.
- Carrier, Aspirant, and Dreamer/Sleeper are distinct states.
- Aspect Rank is independent of Soul Rank.
- Nightmare completion is central-conflict terminal resolution, not universally a boss kill or objective click.
- Per-challenger outcome and appraisal are separate from global scenario resolution.
- Ordinary First-Nightmare failure is lethal; crash/admin recovery is technical.
- Follow `docs/NIGHTMARE-SEED-ROADMAP.md` for future Nightmare/Seed work.

## Workflow rules

- GPT does not write directly to or merge into `main`.
- Every GPT commit includes `Co-Authored-By: ChatGPT <gpt@openai.com>`.
- Use JDK 21.
- Use `mod/verify-smoke.sh`; bare Gradle smoke-task exit codes are not proof.
- Deploy the datapack before testing it: `cd testserver && npm run deploy && npm test`.
- Preserve original findings and append corrections rather than rewriting history.
