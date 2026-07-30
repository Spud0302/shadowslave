# Changelog

[Pride Versioning](https://pridever.org/) — `PROUD.DEFAULT.SHAME`. Bump PROUD for something
you are proud of, DEFAULT for a release that is fine, SHAME for fixing what is embarrassing.

The convention here is **tag first, then bump SHAME for each round of fixes**. Holding a
release back until everything works makes the version number lie about what it took.

Issue numbers refer to [ISSUES.md](ISSUES.md).

---

## `0.7.2` — Q4 root-caused: Weightless can fail to apply

Diagnostic build. From `gpt/q4-server-trace`, which took the suggested approach of instrumenting the
datapack instead of probing over chat, because chat round-trips perturb the timing being measured.

**It worked, and it overturns what I had been saying.** `flaw/weightless.mcfunction` now records,
server-side and in the same tick, how many times it ran and what `safe_fall_distance` was immediately
after its `modifier add`:

| Cycle | executions | value after `add` (x1000) |
| --- | --- | --- |
| 1 pass | 4 | **2000** |
| 2 **fail** | 14 | **3000** |
| 3 pass | 3 | **2000** |

In the failing cycle the `add` ran and the attribute stayed at its base of 3, across **14** consecutive
upkeep executions. Not a stale read and not a chat artifact — the server reports it from inside the pack.

**So Q4 is a gameplay bug, not a harness bug.** Across `0.7.0` and `0.7.1` I said repeatedly that the
datapack was not at fault; that was wrong, and it came from probes that happened to hit the passing path.
The same upkeep runs in normal play, so a player who earns the `fled` Flaw family can intermittently
receive **no penalty at all** while the soul readout still names the Flaw.

**Not fixed here.** The failure is localised but not identified, and four hypotheses have already been
eliminated. `docs/OPEN-QUESTIONS.md` Q4 records what is established, what is not, and the next probe:
capture whether the modifier *exists* after the `add`, which separates "the add silently did nothing"
from "the add worked and something removed it in the same tick" — different bugs, different fixes.

Also: `validate.py` rejected the incoming branch because `ss_test_trace_weightless` was tested in the
pack but only ever applied by the harness over `/tag`. That check earns its keep — dead and misspelled
tags fail silently in Minecraft — so the trace got a proper in-pack toggle, `test/trace/weightless_on`,
rather than a validator exception.

The trace reuses `ss_scratch_a`/`ss_scratch_b`, which `nightmare/enter` and `soul.mcfunction` also write.
Fine for an isolated probe, marked with `ponytail:` comments naming the ceiling — reusing scratch across
systems is what caused §1.7.

## `0.7.1` — a real harness bug fixed, and an honest dead end

From `gpt/q4-harness-isolation`. No runtime change; test infrastructure only.

**Fixed:** `flaw_harness.mjs` had one global mutable `chatLog`, and `exactOneTag()` fired four
concurrent `hasTag()` queries through it via `Promise.all`. Concurrent readers could clear each other's
reply window or satisfy several calls from one reply — the same reply-window contamination behind most
of this harness's history of false results. Commands are serialised through one promise chain now, and
the exactly-one-tag check makes a single authoritative query instead of four.

**Not fixed:** the Q4 order-dependent failure. `harness.mjs` then `flaw_harness.mjs` still fails the
fled family's `safe_fall_distance` assertion about one cycle in three, so that file stays **out** of the
release gate — `npm test` runs the main harness, `npm run test:flaw` runs the other.

**Four hypotheses are now eliminated** (three of them mine): the poll budget, waiting on scheduled
upkeep, the non-re-entrant `chatLog` above, and driving upkeep on every poll. All recorded in
`docs/OPEN-QUESTIONS.md` Q4 so they are not retried.

The datapack is not implicated — probes show the Weightless modifier applied at `-1` over a base of `3`
resolving to `2`, and the main harness passes 32/32 on the same build every time. A diagnostic that read
state immediately before the assertion passed 2/2, but it added chat traffic and so changed the timing
it was measuring. The suggested next step is instrumenting from inside the datapack, since chat-based
probing perturbs what it observes.

Recording this rather than shipping a fifth guess. Four wrong diagnoses is well past the point where
this project's own notes say to stop patching and get data.

## `0.7.0` — the Nightmare gets an objective seam

Merged `gpt/v0.6-experience` and `gpt/v0.7-hardening` together (the latter is stacked on the former),
so `0.6.0` is skipped deliberately — the policy in `docs/VERSIONING.md` allows combining milestones.

### A Nightmare is a scenario, not a boss fight

`nightmare/objective_tick` now owns the scenario-specific machinery: the countdown's atmosphere beats,
summoning the conflict, the bossbar handover, the leash, and the victory test. `tick_player` keeps only
the player lifecycle — health, ejection, the timer, the bar.

The reasoning is canon-led and worth keeping: a Nightmare resolves a **central conflict**, and
wait-then-kill-one-creature is *this prototype's* scenario rather than the definition. Keeping it behind
a seam means the Java port grows a scenario abstraction instead of having boss-kill assumptions baked
into entry, teardown and player state. Behaviour is unchanged from `0.5.0`; the atmosphere beats at
1200/600/200 ticks are new.

### Q3 answered: the earned Flaw families are testable

`test/flaw/{baseline,bloodied,hungry,fled}` force each family deterministically, so the three families
that previously had **no** automated coverage can now be verified after classification. Classification
from real behaviour is still a human check — no bot can fight the creature.

`test/reset` also clears the transient effects the pack itself applies during verification.

### Verification

Validator clean. Main harness **32/32**. The new `flaw_harness.mjs` reaches **39/39 run alone** but has
an **order-dependent failure** when the main harness runs first, so it is **not** part of the release
gate yet: `npm test` runs the main harness, `npm run test:flaw` runs the new one.

The pack is not at fault — a direct probe shows the Weightless modifier applying at `-1` with the
attribute reaching `2`. Two of my fixes did not resolve it (a longer timeout; driving upkeep directly),
so I stopped rather than guess a third time and recorded it as **Q4** with the reproduction and the
dead ends. Two wrong diagnoses in a row is where this project's own notes say to stop patching and get
data.

One fix I did keep: `forceFamily` now throws when `test/flaw/*` replies *"Already a Sleeper"*. The
`expect` matched both the success line and that refusal, so a silent refusal would have looked like a
pass and the run would have asserted against stale state — the same class of fault as Q1.

## `0.5.0` — generated Aspects, earned Flaws, and a version policy correction

**The version went down on purpose.** Andrew's decision (`docs/VERSIONING.md`): `PROUD` stays `0`
while the datapack is still an unfinished implementation of the initial idea, and **`1.0.0` is
reserved for the release we are proud to call the completed framework** — the baseline the Java mod
will be built from. `1.0.0`-`1.4.9` remain untouched as historical prototype releases. A correction
going forward, not a rewrite of history.

Merged from three GPT branches, each verified before merge.

### Aspects are composed, Flaws are earned

- An Aspect name is now built from **two independent vocabularies** (nature x archetype) rather than
  picked from a list of four, because canon is explicit that every Aspect is unique. Confirmed live:
  *Restless Bearer*, *Veiled Warden*, *Restless Warden*, *Pale Witness*.
- The four underlying Dormant mechanics stay finite and predeclared - correctly. A datapack can
  compose names and state at runtime but cannot invent a command implementation, so a design assuming
  arbitrary generated *behaviour* would have forced the Java port early.
- **Flaws are no longer a second unrelated roll.** The trial is classified from behaviour the pack can
  actually observe - near-collapse, food consumed, distance opened from the creature - and the earned
  family decides the mechanical burden. Randomness only picks the personal name inside that family, so
  two players who survive the same way carry the same price under different names. That is what Andrew
  asked for: earned from behaviour, with randomness so identical play does not give identical Flaws.
- *Shadow Slave* is no longer shown as a Flaw. Canonically it is an Aspect.

### The harness stopped lying, at some cost to my pride

GPT's answer to `OPEN-QUESTIONS` Q1 - *which assertions could not fail if the behaviour broke* - found
several real false-confidence paths in code I wrote. It now **fails closed**: 25 -> 32 assertions,
unreadable state is a test error rather than evidence of absence, `hasTag()` can no longer turn a
failed query into confirmed absence, and the weakness gate requires refusal **and** confirmed
non-entry instead of either.

That change immediately caught two of my own broken assertions:

- **"death does not grant the Cast Out advancement" was vacuous.** `execute if entity ... run say`
  emits no reply when the condition is false, so a pass and an unreadable result were identical. It
  could never have failed. Now reads through `execute store success`.
- **The recovery-sleep check could only fail** - it waited on text the pack emits via
  `title @s actionbar`, which never reaches chat. Unfalsifiable rather than unfailable, and equally
  useless.

Then three intermittent failures, none of which were the pack:

- the item-sweep check was finding **litter from previous runs** - the death test also summons an item,
  and the death sweep legitimately moves items to the Overworld, where they persist between runs. It
  isolates itself now.
- the same check used `execute as @e[...]`, which emits nothing when nothing matches, so the clean
  passing case was unreadable. It counts into a score instead.
- the weakness gate assumed `/damage` had landed. It does not always, and regeneration can lift the
  player back across the threshold. There is now a `driveHealthTo()` that **confirms the precondition
  before asserting on behaviour that depends on it**.

**32/32 across four consecutive runs.** Also added: `build_release.py` for reproducible validated
release zips, and the advancement tree is player-facing rather than verification-flavoured.

### Known coverage gap

`test/awaken` clears trial observations by design, so it can only ever produce the **baseline** Flaw
family. The three earned families need a real fought trial, which no bot can do - so the most
interesting half of the new Flaw system is verifiable only by hand.

## `1.4.9` — the entry gate finally checks infection

Merged from `gpt/review-improvements` after running the validator and the harness against it.

- **A First Nightmare now requires infection, checked at the choke point.** The invariant lived
  only in `sleep.mcfunction` and the sneak selector, so any direct call to `nightmare/enter` could
  drop an untouched player into a trial. This closes the **B2** report from the very first playtest
  — right-clicking a bed at night without being infected pulled you in — which had never actually
  been fixed, only worked around by the two player routes happening to guard themselves.
- **`test/reset` clears transient state**, `ss_cooldown` included, ordered *after* `nightmare/leave`
  because leave is what sets it. Clearing before would have done nothing.
- **Progression is split from the placeholder roll.** `progression/become_sleeper` owns the rank
  transition; `prototype/roll_aspect_flaw` holds the temporary four-Aspects-four-Flaws generator.
  The eventual procedural system now has one seam to replace instead of a function that mixed rank
  progression with placeholder content. `awaken/roll` remains a thin alias so existing callers work.
- **The soul readout stops calling Minecraft combat stats canon Attributes.** `Vitality`/`Endurance`
  were never Attribute names; canon Attributes are named supernatural traits. It reads
  `Body: Max Health / Armor` until a real Attribute system exists.
- `README.md` and the live test plan caught up with `1.4.8`'s rank change; the older test plans are
  now marked **HISTORICAL** rather than rewritten, since they record what was actually verified at
  the time and editing them would falsify that.

The reset fix earned its keep immediately by exposing a harness assertion that had been **passing
for the wrong reason**: `ejection starts the cooldown` could read the cooldown left over from the
earlier death test rather than the one the ejection had just set. It also exposed three
timing-fragile assertions. A probe showed ejection behaving correctly — health 4, dimension
overworld, cooldown 595 — while the harness called it broken, the fourth false failure fixed sleeps
have produced here. Entry and ejection now poll for the state they care about via `waitDimension()`.

`validate.py`'s absent-score rule is now documented as **project policy rather than Minecraft
syntax**, a fair correction from the review: `scores={x=..0}` is valid and meaningful when an
objective is guaranteed to exist. Nothing here guarantees that, and the shape has cost three bugs.

Left open deliberately: the death sweep still moves every loose item in the dimension. Correctly
diagnosed in the review and deferred to the Java port, where real drop ownership is possible,
rather than patched with a radius heuristic. **25/25 assertions pass.**

## `1.4.8` — you are a Sleeper, not an Awakened

The lore research in `docs/lore-research/` (Section B) establishes that surviving a First
Nightmare produces a **Sleeper** holding a **Dormant** Aspect. Awakening is a rank further on,
reached after a first journey into the Dream Realm and a return through a Gateway — which Phase 1
does not have. The trial's climax was announcing a rank the player cannot yet hold.

The corrected ladder is **Mundane → Carrier → Sleeper (Dormant) → Awakened**. Two labels moved:

- Surviving the trial now says *"You are a Sleeper"*, and the soul readout reports
  `Rank: Sleeper (Dormant)`.
- A Carrier was previously labelled a Sleeper. Being marked is not a rank — you hold Dormant only
  after surviving. A Carrier now reads `Rank: Carrier (marked)`.

**Labels only.** `ss_rank 1` still means "survived the First Nightmare" everywhere it is tested,
so no guard, gate or advancement condition changed. Function names (`awaken/roll`, `test/awaken`)
also stay — renaming them is churn for no behavioural gain, and Awakening is still where the
ladder is heading. The stale comments in `soul.mcfunction` and `init.mcfunction` that documented
the old model were the real hazard and are corrected.

Two harness assertions had been quietly disarmed by the rewording: `cure refuses on a Sleeper`
waited on a string the pack can no longer emit, and `cure on a Carrier does not refuse` was
searching for "Awakened" in a refusal that now says "Sleeper" — passing whatever happened, which
is worse than failing. Both match the current wording.

Separately, `ejection does not sweep loose items onto you` was flaky, for a reason that inverted
it: on a fixed sleep the check could run before the ejection teleport landed, while the player was
still stood in the nightmare beside the summoned item — so "an item is near me" was true and said
nothing about the sweep. It polls for the dimension now. **25/25, stable across three runs.**

## `1.4.7` — the countdown was five minutes

- **The trial's countdown drops from 6000 ticks to 1800** — five real minutes of dark down to
  90 seconds. Andrew's call: it read as waiting rather than dread, and the trial does not
  actually begin until the creature lands.

`validate.py` gains a check for the bug shape that killed sneak-to-enter for five releases:
a selector filter like `scores={ss_cooldown=..0}`, which looks like "zero or unset" and in fact
matches **nobody**, because an absent score fails `matches` outright. Third occurrence of this
shape in the project, so it is now caught statically. Verified by reintroducing the original
v1.4.0 line and watching the validator reject it.

The harness's `dimension()` preferred mineflayer's cached `bot.game.dimension` over asking the
server, on a comment asserting it was "instant and authoritative". The second half was never
verified and is false — a cross-dimension `/tp` does not reliably produce a respawn packet, so
the cached value reported the nightmare after the player was already home. It failed ejection,
cooldown re-entry and the recovery sleep against a **correct** pack; direct probes disagreed with
it, which is what exposed it. It asks the server every time now. In a test harness, wrong is more
expensive than slow.

Also pinned the countdown assertion near its exact value. The old `timer <= 6000` would have
passed for any countdown between 1 tick and five minutes, so it could never have caught this
being retuned wrongly. **25/25 assertions pass.**

## `1.4.6` — dying is not being cast out

Spotted in a playtest screenshot: the death screen had *"You were not ready"* and *"The Nightmare
rejected you"* behind it. `tick_player` ejects at `Health <= 4` and a real death is `0`, which is
also `<= 4` — so dying ran the entire ejection ceremony, title, blindness, nausea and the **Cast
Out** advancement included. Being cast out is surviving; dying is not, and the verification tree
was recording deaths as ejections.

Death now gets its own line, and no advancement. A `tellraw` rather than a title, because a title
shown to a dead player is drawn behind the death screen and gone by the time they respawn — chat
survives, and doubles as the hint that their belongings are waiting at the bed. The teardown and
the item sweep are untouched and still run on both paths.

Confirmed in-game and closed: item recovery on death re-checked against `1.4.5`'s death-only
sweep, and ejection at 2 hearts ("health drop out is good as well"). **25/25 assertions pass.**

## `1.4.5` — sneak-to-enter had been dead for five releases

- **Sneaking on a bed did nothing but show the telegraph.** The per-tick selector filtered on
  `scores={ss_cooldown=..0}`, and a player who has never been ejected has no `ss_cooldown`
  entry at all — an absent score fails `matches` outright. So the filter excluded *everyone*,
  and the entire sneak path has been dead since `1.4.0` introduced it. This is the third bug of
  this exact shape, and the rule is written in the README and in a comment two lines above the
  offending selector. The filter is now gone: `1.4.3` already moved the cooldown guard into
  `enter.mcfunction`, where `matches 1..` behaves correctly on an absent score. Guard at the
  choke point, never in the caller.
- **Being cast out vacuumed the nightmare into your pockets.** The sweep ran on every exit and
  moves *every* item in the dimension — mob drops, the killed creature's loot, anything ever
  dropped in there — onto the return position. A player ejected alive still has their gear, so
  there is nothing to recover; they just got showered in loot they never earned. The sweep now
  runs only on death.
- **The cooldown ends when you wake, not on a 600-second wall clock.** Sleeping through the
  night *is* the recovery. That night passes untouched; from waking, the Spell can take you
  again. Andrew's call — going back to bed after losing and being told nothing reaches for you,
  twice, reads as the mod switching off.
- **Ejection threshold dropped from 4 hearts to 2**, and the entry gate from 7 to 5. The fight
  wanted to run longer. The trade is deliberate: from 5 hearts a full creature hit kills you
  outright rather than ejecting, so death becomes the ordinary failure and ejection the near
  miss. Acceptable only because item recovery on death is confirmed working.

Harness: the entry assertions were failing against a *correct* pack. `score()` matched
`has (-?\d+)`, and `/tag list` replies `"tester has 2 tags:"` — a late reply landed in the next
command's window and was read as `ss_timer=2`. Replies are drained before each command now, and
the scoreboard pattern requires the trailing `[Objective]`.

The item-recovery assertion was **deleted**, not fixed. Probes showed it could never work: the
nightmare's chunks unload once the player leaves, so the query reports nothing whether or not
items are there — and with the area force-loaded, the Overworld query returns the *same*
coordinates as the nightmare query. A check that gives the same answer for pass and fail is
worse than no check. Confirmed by hand instead: drops land around the bed, most within pickup
range. **24/24 mechanical assertions pass.**

## `1.4.4` — testing commands stop fighting the systems they test

- `test/nightmare` now carries an explicit **`ss_test_bypass`** tag that `enter.mcfunction`
  honours for the cooldown and the weakness gate, then consumes. `1.4.3` had it reaching around
  the cooldown with a bare `scoreboard players reset`, and the weakness gate still refused it
  outright — so the command for entering the trial could not enter the trial below 7 hearts.
  Andrew's call: testing commands are allowed to bypass the systems.

  The exemption is single-use and lives at the choke point, so a stray tag cannot quietly
  disable the gates for a session, and `test/` still does not carry its own copy of the entry
  logic. The **rank** gate deliberately keeps no bypass: an Awakened inside a First Nightmare is
  a state nothing downstream handles, and `test/reset` is one command away.
- `test/reset` announced "You are a Sleeper again" while clearing `ss_carrier`. Sleeper is the
  rank you hold once the Spell has marked you — it now says **Mundane**, matching
  `soul.mcfunction` and the same correction made there.
- The harness asserted the weakness gate through `test/nightmare`, which would have tested the
  new bypass while claiming to test the gate. It calls the real entry function now, and checks
  the bypass separately.

## `1.4.2` — item recovery works, and a bot now checks the work

Added a **mineflayer test harness**: a bot joins a local 1.21.1 server, runs commands and
reads the replies back, asserting on game state. 14 mechanical checks run before a build
ships; anything needing judgement is emitted as a "needs a human" list.

- **§1.6** Dying in the nightmare no longer loses your items permanently. Third attempt at
  this, and the first two failed for different reasons the harness exposed in minutes:
  tagging drops during teardown matched nothing (Minecraft spawns them _after_ the tick
  health hits 0), and a marker as the teleport destination never resolved (selectors are
  dimension-scoped, so it was looked for in the wrong dimension). Now a scheduled sweep
  chains two `in` clauses to move items across.

The harness caught its own bug first — fixed sleeps made entry look broken, because chat
replies lag after a dimension change. It waits for a matching reply now.

## `1.4.1` — failure costs something again

- **§1.9** Re-entering after being cast out healed you to full, so ejection was free. That
  heal came from `1.1.1`, where it fixed the opposite loop. Removed it and refused entry
  below 14 health instead, which closes both.

## `1.4.0` — the Spell rests

- A **600-second cooldown** after ejection. While it runs, sleeping is an ordinary night's
  sleep and crouching on a bed does nothing. Two reasons, both Andrew's: re-entering at the
  health that just ejected you ejected you again — a loop with no exit — and without it the
  mod hijacks every single night.
- Recorded the measured difficulty at 60 HP: on Normal with a wooden sword and no armour,
  4–6 hits land of the 15 needed. Coming back better equipped is the intended answer.

## `1.3.1` — restoring what my own guard broke

- **§1.8** `1.2.1`'s ejection guard used `1..8` to reject a stale reading, but a real death is
  exactly **0** — so death stopped triggering the teardown, and the brand-new item recovery
  never ran. Two fixes from one batch breaking each other. The guard now targets the _read_:
  reset the score before it, so a failure leaves it absent and `matches` fails on it.
- Drops land a block above the return position; the stored position is the bed itself.

## `1.3.0` — the pack says which version it is

- Version shown on load and `/reload`, in the datapack list, and in the self-check header.
  Both of us lost track of which build was installed more than once, and at least one test
  result was attributed to the wrong version.
- `validate.py` asserts the manifest and the load message agree. It caught a mismatch one
  release later, exactly as intended.

## `1.2.1` — the post-sweep batch

Ten bugs from a 39-check in-game sweep, plus the `cure` message. Every one was invisible to
static analysis; **four were introduced by my own earlier fixes**.

- **§1.7** Reading your soul then entering ejected you instantly, repeatedly. The readout
  borrowed `ss_health` as scratch to hold your _armour_ — the same score the ejection check
  reads. A `ponytail:` comment had asserted the reuse was safe.
- **§2.8** First sleep both infected you _and_ pulled you in: the guard checked a tag its own
  previous line had just set.
- **§2.9** Creature speed had never applied — a ravager overwrites its own `movement_speed`.
  Moved to an effect.
- **§2.10** The Spell kept calling you while you were already inside the trial.
- **§2.11** Your corpse was teleported home behind the death screen.
- **§2.12** Attribute modifiers outlived the Aspect that granted them.
- **§2.13** _Sleep Undisturbed_ was unreachable — the `1.2.0` refactor orphaned the grant
  behind a branch that could no longer reach it.
- **§3.2** The fight was **unwinnable**: 12 of 160 damage landed before ejection. 160 → 60.
- **§3.4** Sneak-to-enter now telegraphs itself.
- `cure` refuses on an Awakened instead of claiming the Spell lost interest.

Also swept every guard in the pack for the self-invalidating shape behind 2.8, 2.10 and 2.13.

## `1.2.0` — infection is an event

- **Three states**: untouched → Carrier → Awakened. A fresh player was a Carrier from spawn,
  so the calling started before they could build a bed. It is also wrong: in the novel,
  infection happens _to_ you. Your first ordinary sleep is what marks you.
- **Testing commands**: `test/help`, `infect`, `cure`, `nightmare`, `awaken`, `reset`.

## `1.1.2` — Weightless stopped punishing jumping

- **§1.5** `safe_fall_distance −2` left a safe distance of 1, and a standing jump is 1.25
  blocks — so jumping on the spot cost half a heart. Now −1. Fixed from four measured data
  points, which made it arithmetic instead of guesswork.

## `1.1.1` — the creature keeps its distance

- **§2.7** `spreadplayers`' distance argument is the minimum gap _between targets_, so with
  one creature it did nothing and the spawn range was uniform 0–14 blocks. Centred 12 blocks
  ahead instead: a consistent 8–16.

## `1.1.0` — the Spell calls its Carriers, at any hour

- **Sneaking on a bed** takes a Carrier at any time of day. Vanilla only lets you sleep at
  night, but the novel's Carriers fall under whenever the Spell takes them.
- Carriers feel it: a nausea pulse and an actionbar line every 30 seconds.

## `1.0.9` — you can get out of the bed

- **§2.5** The return position is captured while you are _in_ the bed, so you rematerialised
  inside it — inescapable under a low ceiling. Vanilla's own bed-exit search is not exposed to
  commands, so `unstick` is the manual equivalent.

## `1.0.8` — the creature spawns properly

- **§2.1** Its fire resistance had never applied: 1.20.5 renamed the NBT key to
  `active_effects` and the pack still used `ActiveEffects`. Found from an in-game `/data get`.
- Spawn placement: local coordinates follow _pitch_, so looking downhill buried it in terrain.
  Summoned overhead, then placed on the surface at a distance.

## `1.0.7` — you can see

- **§2.4** `ambient_light: 0.0` was pitch black rather than atmospheric. 0.1, matching the
  Nether. Needs a fresh world — dimension types bake in at creation.

## `1.0.6` — the verification tab renders

- Minecraft draws nothing for an advancement tree with no completed entry, so the tab looked
  broken on a fresh world. The root is granted on load.
- **Closed §1.2** — the advancement `icon` format `{"id": ...}` is correct for 1.21.1.

## `1.0.5` — the self-check stops hurting you

- Running the diagnostic damaged the operator. It probes by _calling_ each Flaw, and
  `1.0.1`'s change to magic damage removed the fire resistance that had been absorbing it by
  accident. Resistance V now wraps the probes.
- It reports its passes; previously a silent pass was indistinguishable from a check that
  never ran.

## `1.0.4` — the pack finally loads

- **§1.3** `monster_spawn_light_level` wrapped its bounds in a `value` key; an int provider
  puts them at the top level. This was the actual cause of _"Data pack validation failed!"_ —
  found from the log after two wrong guesses.
- Reverted `1.0.3`: the log reported errors for `dimension_type` only, so the biome had been
  parsing fine and that "fix" was breaking a working file.

## `1.0.3` — a wrong guess

- Changed biome `carvers` from an object to a list. **This was not the bug** and was reverted
  in `1.0.4`. Kept in the history because pretending otherwise is what the versioning scheme
  exists to prevent.

## `1.0.2` — a closer guess

- `dimension_type` height must cover the range its noise settings generate: 256 → 384.
  Correct, but not what was blocking the load.

## `1.0.1` — fixes that needed no observation

- Flame no longer cancels the Shadow Slave flaw (magic damage, not fire).
- Flame's "burning strikes" implemented via a `player_hurt_entity` trigger.
- `/trigger soul` prints attributes, as the spec always claimed.
- `test/reset` no longer strands you in the nightmare.
- The countdown stops once the creature spawns.
- Validator extended to check function, predicate, dimension, objective, bossbar and tag
  references, plus unpaired attribute modifiers.

## `1.0.0` — Phase 1, The First Nightmare

Sleep, be taken into a dark dimension, survive a countdown, kill what comes, and wake
**Awakened** with an Aspect and a Flaw.

Built as a vanilla datapack — no loader, no client install, server-side only. Ten tasks, each
reviewed. A whole-branch review then found **three Criticals that ten per-task reviews had
missed**, because each of those checked the code against a plan that was itself wrong:

- Minecraft refuses all player NBT writes, so the return teleport silently dumped every player
  at Overworld 0,0,0 — inside stone.
- Tags survive death, so dying in the trial left you permanently tagged and eventually spawned
  the boss beside your bed.
- Entry teleported you to y=150 _after_ safe placement had already put you on the ground.

All three were found by reasoning alone, and all three are now confirmed dead in-game.
