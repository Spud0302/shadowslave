# Claude review — GPT's fixes for #20–#26

**Branch:** `gpt/live-datapack-import` @ `ad03e00` · **PR #19** · **Reviewed from:** `main@a638efc`  
**Verdict at review time: do not merge yet.** One blocking defect and one substantive gap were returned to GPT.

This file preserves Claude's review as historical evidence. The correction response is recorded below rather than rewriting the original findings as though they never existed.

## What Claude accepted

- **#22:** `StoredSoulData` prevents record invariants from throwing before codec validation; invalid data becomes `DataResult.error`.
- **#23:** stored schema versions are validated and dispatched explicitly.
- **#24:** Spell-path stages at or beyond Dreamer retain Aspect and Flaw identity.
- **#25:** completed imports require Carrier evidence and generated identities require mechanics tags.
- **#26:** `test/reset` restores health.
- The initial #20 entry guard was correctly placed at the single choke point.

Claude independently recorded validator clean, lifecycle **32/32**, and Flaw **39/39** before the returned changes.

## Returned blocker: the contract regression hung after PASS

`regression_issue20.mjs` printed its passing verdict but left Mineflayer sockets open, so `npm test` never terminated.

**GPT correction:** commit `1466051` now quits all bots and exits explicitly on both success and failure. The regression was also expanded to exercise the actual disconnect path.

## Returned gap: an offline owner did not occupy the slot

The online-player guard used `@a[tag=ss_in_nightmare]`. An owner who disconnected disappeared from `@a`, while their persistent creature remained in unloaded Nightmare chunks. A second player could enter, reload the orphan, and become entangled with global selectors.

**GPT correction:**

- `ss_trial_lock` is a persistent scoreboard objective;
- accepted entry sets `$global ss_trial_lock = 1` before trial state is created;
- normal shared teardown clears the lock only after creature cleanup;
- an online legacy trial is reconciled into the lock before entry checks;
- the gate now kicks Alice mid-trial, proves Bob is refused while Alice is offline, reconnects Alice, proves resume/completion, then proves Bob can enter after teardown.

This intentionally preserves the frozen datapack's one-active-trial ceiling rather than pretending it has Java-style per-player ownership.

## Contract test versus defect probe

Claude correctly objected that the supported-contract regression had replaced the original global-selector defect probe. Both now exist:

- `testserver/regression_issue20.mjs` — gate test for persistent single-slot ownership, including disconnect/reconnect;
- `testserver/defect_issue20_stray_creature.mjs` — deliberately out-of-gate probe showing that an artificially introduced unrelated `ss_creature` still blocks the global prototype objective.

The defect probe retains the three hard-won measurement notes:

1. `@e` is dimension-scoped;
2. unloaded entities are absent from selectors but are not dead;
3. low health can silently test entry refusal instead of the intended path.

## Current evidence boundary

These corrections are committed but require Claude to rerun:

```bash
cd testserver
npm run deploy
npm test

./mod/gradlew -p mod clean build
mod/verify-smoke.sh
python3 shadowslave/tools/validate.py
```

The ordinary disconnect path should now be safe under the documented one-slot ceiling. The deeper global-tag architecture remains a frozen-datapack limitation and is not presented as equivalent to Java `NightmareService` ownership.
