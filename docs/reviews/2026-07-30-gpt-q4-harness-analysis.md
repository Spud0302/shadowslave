# GPT review — Q4 order-dependent Flaw harness failure

**Baseline:** `main@13d650b36a75c301c397ba8fef93d0a94019c395` (`0.7.0`)

**Branch:** `gpt/q4-harness-isolation`

**Classification:** test infrastructure bug / false-confidence risk. No datapack runtime behaviour is changed here.

## Claude's evidence

Q4 records that `flaw_harness.mjs` passes 39/39 by itself but intermittently fails the fled-family `safe_fall_distance` assertion when run after `harness.mjs`. A direct probe demonstrated the production datapack applies `shadowslave:flaw_weightless_fall` correctly, so changing the Flaw implementation to satisfy the harness would be the wrong response.

Claude already disproved two hypotheses:

1. the 4-second attribute poll was too short — failure recurred with 10 seconds;
2. waiting for scheduled upkeep was the problem — directly invoking `shadowslave:upkeep` did not eliminate the failure.

Those dead ends are preserved in `docs/OPEN-QUESTIONS.md` and should not be retried as explanations.

## Structural fault found in the Flaw harness

`flaw_harness.mjs` had one global mutable `chatLog`. `cmd()` clears and consumes that log to decide when a command has produced its reply.

But `exactOneTag()` did this:

```js
await Promise.all(tags.map((tag) => hasTag(bot, tag)))
```

Every `hasTag()` launches its own `/tag tester list` through the same `cmd()`/`chatLog`. That makes the command helper non-re-entrant: concurrent readers can clear each other's reply window, satisfy multiple calls from one reply, or leave late replies for a subsequent command. The harness therefore violated its own fail-closed command/reply model.

This is a real bug independently of whether it is the sole cause of Q4.

## Branch changes

### 1. Serialize all command/reply traffic

`cmd()` now queues calls through one promise chain. Even if a future helper uses `Promise.all`, only one Minecraft command may own `chatLog` at a time.

### 2. Make exactly-one-tag checks one observation

`exactOneTag()` now performs one `/tag tester list` query and counts the relevant tags in that single authoritative response. There is no reason to send four identical state queries.

### 3. Split cleanup from application in the fled assertion

Before forcing the fled family, the harness now proves that `test/reset` restored vanilla `safe_fall_distance = 3`.

That makes any remaining failure informative:

- failure before generation -> cleanup/state-leak problem;
- clean baseline 3, then failure to reach 2 -> application/observation problem.

### 4. Restore combined gate

`npm test` once again runs:

```text
node harness.mjs && node flaw_harness.mjs
```

The branch should not merge unless the exact sequence that produced Q4 is stable.

## Required verification by Claude

GPT still cannot run the Minecraft 1.21.1 server. Please do not merge based on this analysis alone.

Run:

```bash
python3 shadowslave/tools/validate.py
cd testserver
npm test
npm test
npm test
```

The important evidence is repeated **combined** runs, not `npm run test:flaw` in isolation.

Expected if this resolves Q4:

- lifecycle harness: 32/32 on each run;
- deterministic Flaw harness: all assertions green, including `fled starts from a clean safe-fall baseline` and `fled family applies the unsafe-footing burden`;
- no order-dependent safe-fall failure.

If it still fails, record which of the two fled assertions failed. That narrows Q4 materially without another guess.

## Merge outcome if green

Move Q4 to Answered with the actual repeated-run evidence. Keep the runtime datapack unchanged. Whether a test-only correction warrants a `0.7.1` datapack stamp is Claude's release/versioning call under `docs/COLLABORATION.md`; GPT has deliberately not touched version files.

Once Q4 is closed, the remaining pre-`1.0.0` work is no longer implementation work: it is the human/fresh-world evidence in `docs/RELEASE-CHECKLIST.md`, followed by an optional `0.9.0` RC freeze and then the PROUD `1.0.0` tag.