# Engineering notes — why the code is like this

**Read this before changing code.** The rules in `README.md` are the _what_; this is the _why_,
with the incident behind each one. A rule with its scar tissue attached tends to survive; a bare
rule gets "improved" away by the next person who cannot see what it was protecting against.

Working with the other agent? See **`COLLABORATION.md`** for the protocol.

Audience: anyone picking this repo up cold — a fresh ChatGPT session, a future Claude session, or
Andrew in six months. Written by Claude, who has caused most of the bugs described below.

---

## The one thing to understand first

**Almost every way a datapack breaks is silent.** No exception, no log line, no failed command —
just a rule that quietly matches nobody, or a directory Minecraft never reads. This single fact
explains nearly every convention here. If you take one idea away: _a change that appears to work
is not evidence that it works._

Consequences that keep recurring:

- Code can be valid, well-formed, and completely inert.
- A test can pass because it is asking the wrong question.
- A fix can break a different feature with no visible connection to it.

This is why the project carries a static validator, a bot harness, and unusually heavy comments.
None of that is ceremony.

---

## Guard at the choke point, never in the callers

Every invariant about entering a nightmare lives in `nightmare/enter.mcfunction`. Every invariant
about leaving lives in `nightmare/leave.mcfunction`.

**Why:** three separate bugs came from guarding in callers.

| Version | What happened                                                                                                                                                                                                                                             |
| ------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `1.4.3` | The ejection cooldown was checked in `sleep.mcfunction` and the sneak selector. Both player routes were covered, so it looked fine — but any other caller walked straight past it.                                                                        |
| `1.4.3` | The cooldown was _set_ in `eject.mcfunction`, which death never reaches: death routes through the dimension-mismatch cleanup directly to `leave`. Dying set no cooldown at all.                                                                           |
| `1.4.9` | The "must be infected" rule lived only in `sleep.mcfunction` and the sneak selector, so a direct call to `enter` could drop an untouched player into a trial. This was reported by the playtester in the _first_ play session and survived nine releases. |

The pattern is identical each time: the guarded routes happen to be the only ones exercised, so the
gap is invisible in play until something calls the function a different way.

**Rule:** if an invariant is about a state transition, it belongs in the function that performs the
transition. If you find yourself adding the same check to a second caller, that is the signal to
move it inward instead.

---

## Absent scores are the single biggest bug source here

A scoreboard score that has never been set does not exist. **`matches` fails outright on an absent
score** — it does not treat it as zero.

Nothing in this pack writes 0 to mean "off". So:

- `if score @s x matches 0` → never true. Use `unless score @s x matches 1..`.
- `scores={x=..0}` in a selector → matches **nobody** who has never had `x` set.

**Three bugs, so far:**

1. The `/trigger soul` readout borrowed `ss_health` as scratch; a failed read left it stale and the
   ejection check fired on it (`1.2.1`).
2. A guard tightened to `matches 1..8` to reject a stale reading also excluded a real **0**, which
   is exactly what death produces — so death stopped triggering teardown, taking the brand-new item
   recovery with it (`1.3.1`).
3. `scores={ss_cooldown=..0}` on the sneak-entry selector excluded every player who had never been
   ejected. **Sneak-to-enter was completely dead from `1.4.0` to `1.4.5`** — five releases — and the
   rule against this was already written in the README and in a comment two lines above the
   offending selector.

`validate.py` now rejects upper-bound-only score filters statically. That check was verified by
reintroducing the original `1.4.0` line and confirming it fails.

**On that check being "too strict":** it is a **project policy**, not a Minecraft syntax rule.
`scores={x=..0}` is valid and meaningful when an objective is guaranteed to have a value for every
candidate. Nothing here guarantees that. If a legitimate use appears, add a narrow documented
allowlist — do not delete the check. The whole reason it exists is that the mistake is invisible.

### The self-invalidating guard

A related shape, and one I have written _myself_ while fixing this class of bug:

```mcfunction
execute if score @s x matches 1.. run scoreboard players reset @s x
execute if score @s x matches 1.. run return 0        # never fires — line 1 erased x
```

The second line reads the value the first line just destroyed, falls through, and does the thing it
was meant to prevent. Fix: make the mutation and the return **one command**.

```mcfunction
execute if score @s x matches 1.. run return run scoreboard players reset @s x
```

Same trap with tags: `infect` adds `ss_carrier`, so a following `unless entity @s[tag=ss_carrier]`
is already false. `sleep.mcfunction` uses `return run` for exactly this reason. This caused §2.8,
and I reintroduced it in `1.4.5` and caught it before shipping only because the file had a comment
about it four lines below.

---

## Player NBT is read-only

`data merge entity <player>` and `execute store ... entity <player>` are **refused by Minecraft**.
Reads are fine.

This is why the return teleport goes through command storage and a macro function
(`nightmare/return.mcfunction`) instead of writing coordinates onto the player. Before it was
understood, the return teleport silently dumped every player at Overworld `0,0,0` — inside stone.

Related: selectors are **dimension-scoped**, so coordinates cannot pull entities across a dimension
boundary. Moving items out of the nightmare needs chained `in` clauses
(`in <dimA> as @e[...] in <dimB> run tp @s <coords>`). An attempt to use a marker entity as the
destination failed because the marker was looked for in the wrong dimension.

---

## Testing: the rules that cost the most to learn

### A check you have never seen fail is not a check

Two live examples, both of which passed for months while asserting nothing:

- `assert(timer > 0 && timer <= 6000)` — the countdown was 6000. This would have passed for _any_
  value from 1 tick to five minutes, so it could never have caught the countdown being retuned
  wrongly. It is pinned near the exact value now.
- `assert(!/Awakened/.test(refusal))` — after `1.4.8` reworded that refusal to say "Sleeper", this
  searched for a string the pack **can no longer emit**, so it passed regardless of what happened.

**Rule:** when you add or change an assertion, make it fail once on purpose. When you reword
player-facing text, grep the harness for the old string — assertions that match on copy are
silently disarmed by rewording.

### Never sleep where you can poll

Fixed `sleep()` calls have produced **four separate false failures** against a correct pack: entry,
ejection, cooldown re-entry, the recovery sleep, and the item sweep. A duration that looks generous
is still a guess about someone else's scheduler.

Use `waitDimension(bot, pred)` or an equivalent poll. And note the sweep case, where the fixed sleep
_inverted_ the test: the check could run before the ejection teleport landed, while the player was
still stood in the nightmare beside the item — so "an item is near me" was true and said nothing at
all about the sweep.

### When the harness and a direct probe disagree, believe the probe

This has happened three times. Every time, the pack was correct and the harness was wrong. **Twice
I came close to shipping a "fix" for a regression that did not exist.**

The discipline: before changing pack code to satisfy a failing assertion, reproduce the failure with
a minimal probe that queries the server directly. `testserver/probe*.mjs` are disposable scripts
kept for reference.

A concrete instance: `dimension()` returned mineflayer's cached `bot.game.dimension`, on a comment I
wrote claiming it was "instant and authoritative". The second half was never verified and is false —
a cross-dimension `/tp` does not reliably produce a respawn packet, so the cache reported the
nightmare after the player was already home. In a test harness, **wrong is more expensive than
slow**: ask the server.

### Some things are not machine-checkable — say so

Item recovery on death cannot be asserted from the bot. Once the player leaves, the nightmare's
chunks unload, so an item query there reports nothing whether or not items exist; force-load the
area and the Overworld query returns the _same_ coordinates as the nightmare query.

I mis-graded that feature **three times**, always as broken, always while it worked. The assertion
was **deleted**, not repaired, and moved to the harness's "needs a human" list. A check that returns
the same answer for pass and fail is worse than no check, because it manufactures false confidence.

### A correctness fix that surfaces a failing test is doing its job

Twice now, fixing real state leakage has exposed tests that were passing for the wrong reason. Most
recently, `test/reset` not clearing `ss_cooldown` meant `ejection starts the cooldown` could read the
cooldown left over from an _earlier_ test rather than the one the ejection just set.

**So: when a fix makes tests fail, suspect the tests before reverting the fix.**

---

## Test commands may bypass the systems they test — explicitly

A testing command that gets refused by the system it exists to test is useless. `test/nightmare`
carries a **single-use `ss_test_bypass` tag** which `enter.mcfunction` honours for the cooldown and
weakness gates and then consumes.

Three deliberate properties:

1. **Single-use** — a stray tag cannot quietly disable the gates for a whole session.
2. **Honoured at the choke point** — `test/` does not grow its own copy of the entry logic, which
   would drift from the real path.
3. **Visible** — an explicit exemption, not a hole in the guard.

The **rank** gate has no bypass on purpose: a Sleeper inside a First Nightmare is a state nothing
downstream handles, and `test/reset` is one command away.

---

## Versioning

[Pride Versioning](https://pridever.org/) — `PROUD.DEFAULT.SHAME`. **Tag first, then bump SHAME for
each round of fixes.** Holding a release back until it is clean makes the version number lie about
what it took to get there; the history of embarrassing fixes is the point, not something to hide.

`1.0.3` is kept in the changelog explicitly as _a wrong guess that was reverted_.

**Three files carry the version and must agree:** `pack.mcmeta`, `init.mcfunction` (the load
message), and `test/selfcheck.mcfunction`. `validate.py` enforces this, because a partial bump has
happened and both of us lost track of which build was installed.

**For GPT specifically:** please leave the version alone on your branches. Claude stamps it at merge
time, because the number depends on what else landed first. A branch arriving pre-stamped just has
to be re-stamped.

---

## Documentation rules

**Mark historical, do not rewrite.** `TESTING.md` contains test plans from `v1.2.0` and `v1.3.0`
whose terminology and numbers are long superseded. They are labelled **HISTORICAL** rather than
edited, because they record what was actually verified at the time — rewriting them would falsify
the record.

`README.md` is the only document that must always describe current behaviour.

**Prune the human list, never the bot list.** Confirmed items are struck through in `TESTING.md`
with the verdict, because the playtester's attention is the scarce resource and re-asking a settled
question wastes a play session. The _automated_ suite re-runs everything every time — things
"confirmed working" are precisely what regressions break, and it costs 90 seconds of compute and
zero attention.

---

## Deliberate compromises, and how they are marked

Two marker vocabularies exist. Both are fine; use either, but always name the ceiling **and** the
upgrade path, never just "temporary".

- **`ponytail:`** — a deliberate shortcut with a known ceiling (Claude's convention).
- **`PROTOTYPE-LIMIT:`** — placeholder content or structure awaiting a real system (GPT's).

Currently live, all intentional:

| Compromise                                                  | Why it stands                                                                                                                                                                               |
| ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| One creature and one bossbar, globally                      | Phase 1 is single-player-at-a-time. Per-player ownership needs owner tags; deferred with the multiplayer work.                                                                              |
| The death sweep moves **every** loose item in the dimension | Death drops appear _after_ teardown and cannot be identified reliably from a datapack. Real drop ownership belongs in the Java port, not in a radius heuristic that would fail differently. |
| Four fixed Aspects and four fixed Flaws                     | **Wrong on canon** — Aspects are unique and Flaws are personal, not rolled. Isolated in `prototype/roll_aspect_flaw.mcfunction` so the eventual generator replaces one file.                |
| Instant-kill damage bypasses ejection                       | The threshold catches you at 4 HP; anything taking you from above that to 0 kills outright. A limit of the gear-retention promise, not a state bug.                                         |

---

## Lore authority

1. `docs/lore-research/` — the evidence base, with per-answer confidence labels.
2. `Shadow Slave - Lore Reference` (Andrew's vault) — the concise systems bible.
3. The wikis and the novel, per the source policy in `docs/lore-research/README.md`.

**Findings may invalidate shipped design, and that is the point.** `1.4.8` corrected the game's
climax because research established that surviving a First Nightmare produces a **Sleeper** with a
**Dormant** Aspect, not an Awakened — Awakening comes after a first Dream Realm journey, which Phase
1 does not have. The ladder is **Mundane → Carrier → Sleeper (Dormant) → Awakened**.

Note the shape of that fix: **labels only**. `ss_rank 1` still means "survived the First Nightmare"
everywhere it is tested, so no guard, gate or advancement condition changed. Function names
(`awaken/roll`, `test/awaken`) were left alone deliberately — renaming is churn for no behavioural
gain. The real hazard was the _stale comments_ describing the old model, since those are what
mislead the next change.

---

## Things to check with Andrew rather than decide

- Balance numbers (thresholds, timers, cooldowns) — he playtests and has clear preferences.
- Anything that changes the feel of the trial.
- Sourcing and licensing questions.
- Deleting or rewriting a historical record.

Design proposals are welcome; landing them on `main` unasked is not.
