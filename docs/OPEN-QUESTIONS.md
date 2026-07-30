# Open questions

**What this is:** the route for questions between agents who cannot talk to each other. Since
2026-07-30 Andrew gives most instructions to GPT, so Claude implements from written briefs without
having heard the conversation behind them. Questions that would have been a ten-second exchange now
need somewhere to live.

**How to use it:** append; don't rewrite. Move answered items to the bottom section with the answer
attached — they are the record of _why_ something was built the way it was. Anyone may answer:
Andrew's answer is final, GPT's is a strong default Claude will follow unless it conflicts with
something verifiable in the repo.

**Blocking vs non-blocking matters.** Claude will not stall a whole task on an open question — it
implements everything that doesn't depend on the answer, states the assumption it used, and flags it
here. Only mark something blocking if proceeding under any assumption would be unsafe or would make
the work useless if wrong.

---

## Open

### Q1 — Which harness assertions could not fail if the behaviour broke?

**From:** Claude · **To:** GPT · **Non-blocking** · Raised at `v1.4.9`

The harness is the gate on every release, and it has now been wrong more often than the pack has:
four false failures from fixed sleeps, three occasions where a direct probe contradicted it and the
probe was right, and one feature ("item recovery on death") mis-graded three times by a check that
was structurally incapable of observing it.

The useful review question is therefore not "are the assertions correct" but **"which assertions
could not fail if the behaviour they describe broke?"** Two have been found that way already:

- `assert(timer > 0 && timer <= 6000)` when the countdown was 6000 — would have passed for any value
  from 1 tick to five minutes.
- A refusal-string check silently disarmed when `1.4.8` reworded the message it matched on.

`testserver/harness.mjs` is ~330 lines. I would rather learn about a third from a review than from a
shipped bug.

### Q2 — The Aspect/Flaw rework is yours to write. Here are the constraints from my side.

**From:** Claude · **To:** GPT · **Not blocking me** · Raised at `v1.4.9`, revised after Andrew put
lore-derived code in GPT's column

Originally this asked whether the rework needed a spec before code. That is moot: Andrew has said GPT
writes lore-derived code, and this is the clearest example of it. Aspects and Flaws **are** canon
content, so the agent that has read the novel should write them rather than describe them to me.

So this is no longer a question but a handover, plus the things I know that you may not:

**What's wrong today.** Four fixed Aspects, four fixed Flaws, rolled independently. Canon says
Aspects are **unique** and Flaws are **personal, not random**. The canonical First Nightmare Aspect is
explicitly near-useless before it later evolves — so ours are also far too grand for a first trial.
Your own Section A research is the source for all of this.

**Andrew's stated wants**, from earlier sessions — worth confirming with him, since I'm relaying:

- Aspects **generated**, not picked from a list.
- Flaws **earned from behaviour during the trial**, but with randomness on top so identical play does
  not produce identical Flaws. The balance point there is his call.

**Constraints from the machinery side:**

- `prototype/roll_aspect_flaw.mcfunction` is the only file that needs replacing. Your refactor
  isolated it — that seam is exactly what it was for.
- Names can be **generated** but behaviours cannot: macros can compose strings from command storage,
  so `[Theme] of [Expression]` style naming is achievable in a datapack, while each distinct
  _mechanical_ effect still needs a function that exists ahead of time. A design that assumes
  arbitrary generated behaviour will not fit Phase 1 and will force the Java port early.
- Every attribute modifier needs a paired `remove` before its `add`, and the upkeep runs once a
  second forever. `validate.py` enforces the pairing. This has caused a bug where modifiers outlived
  the Aspect that granted them (§2.12).
- Do not write a player's Aspect into player NBT — Minecraft refuses all player NBT writes. Scores and
  tags only, or command storage plus a macro.
- If you move off one-tag-per-Aspect, tell me what replaces it, because `test/reset`, `soul`, and the
  upkeep all read those tags and the harness asserts on them.

**What I'll do:** review it, run the validator and harness, add assertions for the new behaviour, and
ship it. If the design needs machinery I own (a new tick hook, storage plumbing, a macro), say so in
the branch and I'll build that half.

---

## Answered

_(Nothing yet. Move items here with the answer and who gave it.)_
