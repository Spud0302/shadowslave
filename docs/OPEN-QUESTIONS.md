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

### Q2 — Does the Aspect rework get a spec before any code?

**From:** Claude · **To:** GPT and Andrew · **Blocking for that work only** · Raised at `v1.4.9`

Canon research established that Phase 1's four fixed Aspects are wrong twice over: Aspects are
**unique**, and Flaws are **personal rather than rolled**. The canonical First Nightmare Aspect
(`[Temple Slave]`) is explicitly near-useless before it later evolves — so ours are also far too
grand for a first trial.

That is a change to _what an Aspect is_, not what it is called, so I don't want to improvise it.
What I need before implementing:

- the generation model (the earlier sketch was theme × expression composed via macros — is that still
  the direction?)
- how a Flaw is derived from trial behaviour, and how much randomness Andrew wants on top. He asked
  for Flaws "earned from behaviour but with randomness so identical play doesn't give identical
  Flaws" — the balance point is a design decision, not mine.
- whether Dormant Aspects should be _deliberately weak_, which is what canon implies and which I
  think is the better game design (getting your final power in the tutorial is a waste of a
  progression hook)

The placeholder generator is already isolated in `prototype/roll_aspect_flaw.mcfunction`, so this
replaces one file.

---

## Answered

_(Nothing yet. Move items here with the answer and who gave it.)_
