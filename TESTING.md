# Shadow Slave — test history and current release checks

> The early plans below are **HISTORICAL** records. They intentionally preserve the terminology,
> balance numbers, and assumptions that were true when those sweeps were run.
>
> **For the current release candidate, skip to “Current release-candidate checks” at the bottom.**
> Mechanical regressions are now owned by `testserver/harness.mjs` rather than a human checklist.

---

# Historical — `v1.2.0` sweep

Work through these and note what fails. Nothing gets fixed until the sweep is done, so
report everything, including things that merely feel wrong.

**Setup:** Survival, cheats on, difficulty Easy or higher (Peaceful strips hostile mobs and
the trial will be empty). `/function shadowslave:test/help` lists every command below.

**Reset between tests:** `/function shadowslave:test/reset` — back to untouched, all tags and
modifiers stripped. Safe to run anywhere except inside a nightmare.

---

## Already known — do not re-report

|                                                    |                                                                                                                                                                 |
| -------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **First sleep both infects you AND pulls you in.** | Confirmed. `infect` adds the Carrier tag, so the guard on the next line is already false and execution falls through. You should wake normally that first time. |

---

## A. Infection lifecycle

| #   | Do this                                                            | Expect                                                                                                |
| --- | ------------------------------------------------------------------ | ----------------------------------------------------------------------------------------------------- |
| A1  | New world, `/function shadowslave:test/reset`, then wait 2 minutes | **No** nausea, **no** actionbar message. An untouched player is left alone.                           |
| A2  | `/trigger soul`                                                    | `Rank: Sleeper`, and _"The Spell has not noticed you yet."_ No `(Carrier)`.                           |
| A3  | `/function shadowslave:test/infect`                                | Cave sound, nausea, _"Something noticed you while you slept."_                                        |
| A4  | `/trigger soul`                                                    | `Rank: Sleeper  (Carrier)` and _"The Spell has marked you."_                                          |
| A5  | Wait 30 seconds as a Carrier                                       | Nausea pulse + _"Your eyelids are heavy. Something is calling."_ on the actionbar. Repeats every 30s. |
| A6  | `/function shadowslave:test/cure` then wait 60s                    | Calling stops completely. `/trigger soul` shows no `(Carrier)`.                                       |
| A7  | `/function shadowslave:test/infect` twice                          | Second run says _"already a Carrier"_ and does nothing.                                               |

## B. Entry paths

| #   | Do this                                                    | Expect                                                                                                                                                  |
| --- | ---------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- |
| B1  | As a **Carrier**, `/time set day`, then **sneak on a bed** | Pulled in. **This is the one I most need confirmed** — it depends on the `is_sneaking` flag, and if that name is wrong it fails silently with no error. |
| B2  | As a Carrier, `/time set night`, sleep normally            | Pulled in.                                                                                                                                              |
| B3  | As **untouched**, sneak on a bed in daylight               | Nothing at all.                                                                                                                                         |
| B4  | `/function shadowslave:test/nightmare`                     | Straight into the trial, no bed needed.                                                                                                                 |
| B5  | While already in a nightmare, run `test/nightmare` again   | Refuses, _"already in a nightmare"_.                                                                                                                    |
| B6  | As **Awakened**, sleep at night                            | Sleeps normally, night passes, **not** pulled in. Grants _Sleep Undisturbed_.                                                                           |

## C. The trial

| #   | Do this                                            | Expect                                                                                                                                                                    |
| --- | -------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| C1  | On entry                                           | Dark but navigable. Purple _"The Nightmare Spell"_ bar, title card, full health, gear intact, **not** stuck in the bed.                                                   |
| C2  | Watch the bar for a minute                         | Drains steadily.                                                                                                                                                          |
| C3  | Look around                                        | Hostile mobs spawning — zombies, skeletons, spiders, phantoms.                                                                                                            |
| C4  | `/scoreboard players set @s ss_timer 1`            | Creature spawns **8–16 blocks away**, on the ground, never buried and never in your face. Bar turns red, reads _Nightmare Creature_, tracks its health. Grants _Endured_. |
| C5  | `/data get entity @e[tag=ss_creature,limit=1]`     | `Health: 160`, and `active_effects` should now be **present** with fire resistance — it was missing before `v1.0.8`.                                                      |
| C6  | Run away from it, 150+ blocks                      | It follows. You should **not** win by fleeing.                                                                                                                            |
| C7  | Attack it, then quit to title and rejoin mid-fight | You do **not** get a free win. The creature is still there.                                                                                                               |

## D. Outcomes

| #   | Do this                                        | Expect                                                                                                       |
| --- | ---------------------------------------------- | ------------------------------------------------------------------------------------------------------------ |
| D1  | Kill the creature                              | _"The Nightmare Ends"_, returned to your bed, **stepped out of it, not stuck in it**. _Slayer_ + _Awakened_. |
| D2  | `/trigger soul` after                          | `Rank: Awakened`, one Aspect, one Flaw, Vitality and Endurance numbers.                                      |
| D3  | Let it beat you below 9 HP                     | _"Cast Out"_, returned at low health, **gear still in your inventory**, still a Sleeper.                     |
| D4  | In the trial, `/kill @s`                       | You respawn normally and are **not** left tagged. No countdown continues, no creature appears near your bed. |
| D5  | After Awakening, `test/reset` then sleep again | Whole loop repeats cleanly.                                                                                  |

## E. Aspects

Force one instead of rolling: `/function shadowslave:test/awaken` then
`/tag @s remove ss_aspect_shadow` (and the other three), then add the one you want.

| #   | Aspect | Command                        | Expect                                                                                                                                    |
| --- | ------ | ------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------- |
| E1  | Shadow | `/tag @s add ss_aspect_shadow` | Night vision. Faster in darkness.                                                                                                         |
| E2  | Flame  | `/tag @s add ss_aspect_flame`  | Stand in fire, take nothing. Hit a mob — it should **catch fire**.                                                                        |
| E3  | Bone   | `/tag @s add ss_aspect_bone`   | `/attribute @s minecraft:generic.armor get` reads 6 higher. **Wait a minute and check again — it must still be exactly 6**, not stacking. |
| E4  | Wind   | `/tag @s add ss_aspect_wind`   | Visibly faster, higher jumps. Same non-stacking check.                                                                                    |

## F. Flaws

| #   | Flaw         | Command                            | Expect                                                                                                                                                                 |
| --- | ------------ | ---------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| F1  | Shadow Slave | `/tag @s add ss_flaw_shadow_slave` | Steady damage outdoors in daylight; stops in shade or at night. **Also try it with Flame active** — it should still hurt you, since it deals magic damage now.         |
| F2  | Fragile      | `/tag @s add ss_flaw_fragile`      | Max health drops to 14 (7 hearts). Wait a minute — must **stay** at 14.                                                                                                |
| F3  | Ravenous     | `/tag @s add ss_flaw_ravenous`     | Hunger drains noticeably faster.                                                                                                                                       |
| F4  | Weightless   | `/tag @s add ss_flaw_weightless`   | **Retuned in `v1.1.2`.** Jumping on the spot: no damage. Walking off 1 block: no damage. Hopping down 1 block: about half a heart. Longer falls hurt more than normal. |

## G. Edge cases

| #   | Do this                                                                    | Expect                                                                                                                |
| --- | -------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------- |
| G1  | Mid-trial, `/reload`                                                       | Bossbar comes back within a second. Trial continues.                                                                  |
| G2  | Mid-trial, quit to title and rejoin                                        | Trial continues, no free win.                                                                                         |
| G3  | `/function shadowslave:test/reset` while **inside** a nightmare            | Teleported out, not stranded.                                                                                         |
| G4  | `/function shadowslave:test/selfcheck` at full health, outdoors, at midday | Every line PASS, including _"all 8 Aspect/Flaw functions resolve"_. **You should take no damage and lose no hearts.** |
| G5  | `test/awaken` while already Awakened                                       | Refuses cleanly.                                                                                                      |

## H. Verification tree

Open advancements (**L**) → _Shadow Slave — Verification_. After a full win run plus one
ejection run, all nine should be earned:

`Shadow Slave — Verification` · `Chosen` · `Endured` · `Slayer` · `Awakened` · `Cast Out` ·
`Aspect Holds` · `Flaw Bites` · `Sleep Undisturbed`

Anything still locked tells me which mechanic never fired.

---

## The thing no command can tell me

**How does the fight feel?** 160 health, faster than you can sprint, heavy knockback
resistance, and the leash means you cannot retreat to heal. Too easy, about right, or a
slog? You are the only source for this, and it is the one number I would most like to get
right before Phase 2.

### Fighting it at the gear tier people will actually have

A First Nightmare comes early, so wood or stone is the realistic loadout. The arithmetic
is not encouraging:

| Weapon        | Damage | Hits to kill 160 HP | Roughly                              |
| ------------- | ------ | ------------------- | ------------------------------------ |
| Wooden sword  | 4      | **40**              | 25+ seconds of uninterrupted hitting |
| Stone sword   | 5      | 32                  | 20 seconds                           |
| Iron sword    | 6      | 27                  | 17 seconds                           |
| Diamond sword | 7      | 23                  | 15 seconds                           |

Against that, the creature deals 4 per hit and ejection fires below 9 HP — so unarmoured
you can absorb **about three hits** before the trial ends. Forty hits landed versus three
taken, against something faster than you that you are not allowed to run from.

If that turns out to be as bad as it looks, the honest fix is dropping `max_health` well
below 160 rather than nerfing its damage — the creature should feel dangerous, just not
take a minute of swinging. Report how it actually plays; the table is theory.

---

> **HISTORICAL — do not use as a current expectation.** This list records the v1.3.0 regression
> sweep. Terminology predates v1.4.8 and later releases changed several behaviours.

# `v1.3.0` regression list

Only what changed since the 39/39 sweep. Ordered by how likely each is to be wrong, not by
importance. Start with a clean state: `/function shadowslave:test/reset`.

## Highest risk — new mechanisms, never run

| # | Do this | Expect |
| --- | --- | --- |
| R1 | **Die in the trial with a full inventory.** `test/nightmare`, fill your hotbar, then `/kill @s`. | **Your items are at your bed**, not stranded in the nightmare. This is the most speculative code in the batch: it drags item entities across a dimension boundary using a temporary marker as the teleport destination, because selectors cannot cross dimensions with coordinates. If the items are missing, say so — the fallback is a different approach entirely. |
| R2 | **Read your soul, then enter.** `/trigger soul`, then immediately `test/nightmare`. | You enter and **stay in**. This was the lockout: the readout was writing your armour into the score the ejection check reads. Try it a few times — it used to fire on every attempt. |
| R3 | **Fight it at wooden sword, no armour.** | 60 health = 15 hits. Should be a real fight you can actually win, rather than 40 hits you never survive. Tell me if it is now trivial — that is a one-number change either way. |

## Fixes to specific reported bugs

| # | Do this | Expect |
| --- | --- | --- |
| R4 | `test/cure`, then sleep at night as an **untouched** player | *"Something noticed you while you slept"* and you **wake up normally**. You should NOT be pulled in on that first sleep. |
| R5 | Sleep again as a **Carrier** | Now you are pulled in. |
| R6 | Inside the trial, wait 30+ seconds | **No** nausea, **no** *"Your eyelids are heavy"*. The Spell should not call someone already in its trial. |
| R7 | Die in the trial, watch the death screen | **No** portal warp, no view of your bed behind it. Just a normal death screen. |
| R8 | `test/awaken`, note the Aspect, then `test/reset` and `test/awaken` again | Old Aspect and Flaw fully gone. Check `/trigger soul` — Vitality should read 20 unless the NEW roll is Fragile, and Endurance 0 unless it is Bone. Previously modifiers outlived their Aspect. |
| R9 | Sleep at night as an **Awakened** player | Sleeps normally, and grants **Sleep Undisturbed** — the advancement was unreachable before. |
| R10 | `test/cure` while Awakened | Refuses, and points you at `test/reset`. It used to claim the Spell had lost interest, which was untrue. |
| R11 | As a Carrier, sneak on a bed | *"The Spell reaches for you..."* appears **immediately** on the actionbar, before the hold completes. |
| R12 | Watch the creature chase you | Should move noticeably faster than before — its speed now comes from an effect, since a ravager overwrites its own speed attribute. |

## Regression

| # | Do this | Expect |
| --- | --- | --- |
| R13 | The full loop once, clean | infect → sleep → survive → kill → Awakened, with the right Aspect and Flaw. Eleven things changed; this confirms none of them broke the loop. |

---

# Current release-candidate checks

**Baseline for this section:** `main` v1.4.9 plus `gpt/datapack-release-completion`.

The mechanical release gate is the Mineflayer harness, not this list:

```bash
python3 shadowslave/tools/validate.py
cd testserver && node harness.mjs
```

The harness now covers **32 assertions**, including infection/cure guards, untouched direct-entry
refusal, weakness and cooldown gates, test bypasses, entry state/countdown, soul-readout isolation,
reset from both outside and inside the Nightmare, death teardown, Sleeper readout/rank gate,
Sleep Undisturbed, modifier cleanup, ejection, recovery sleep, and the no-sweep-on-ejection rule.

Do **not** spend human playtest time repeating those unless the harness fails or the relevant code
changes. The following are the remaining checks where a person or release environment adds evidence
the harness cannot.

## H1 — death-screen presentation

1. `/function shadowslave:test/reset`
2. `/function shadowslave:test/infect`
3. `/function shadowslave:test/nightmare`
4. `/kill @s`

**Expect:** a normal death screen. No portal warp and no visible glimpse of the bed/Overworld behind
the death screen before respawn.

This has never been cleanly confirmed by a human on a build where the current death teardown was
active. The harness verifies state cleanup but cannot judge what the client visually renders.

## H2 — natural cooldown expiry

This is mechanical, but the harness currently checks **cooldown refusal** and **sleep-to-clear** rather
than waiting out the timer itself. Use a shortened score instead of waiting ten real minutes:

1. become a Carrier and get cast out once;
2. heal above the entry threshold;
3. `/scoreboard players set @s ss_cooldown 2`;
4. wait about three seconds;
5. crouch on a bed or call the real `shadowslave:nightmare/enter` function.

**Expect:** the cooldown reaches zero and normal entry works again.

If this fails, add it to the harness before fixing the datapack so the regression is reproducible.

## H3 — release ZIP smoke test

From the repository root:

```bash
python3 shadowslave/tools/build_release.py
```

Then install the generated `shadowslave-vX.Y.Z.zip` into a **fresh Minecraft 1.21.1 world**.

Confirm:

- `/datapack list` sees and enables the pack;
- the load banner has the expected version;
- `/function shadowslave:test/selfcheck` reports PASS;
- advancements render under the player-facing **Shadow Slave** tab, not “Verification”;
- `/function shadowslave:test/nightmare` can enter the custom dimension.

The fresh-world step matters because dimension and worldgen validation can fail at world creation in
ways `/reload` on an existing world does not reproduce.

## Already confirmed — re-run only after the owning code changes

- **Death item recovery:** confirmed v1.4.4 and v1.4.5. Re-check only if `nightmare/sweep*` or death-return timing changes.
- **Whole winning loop:** confirmed v1.4.6. Re-check after changes to `nightmare/survive`, `progression/`, placeholder generation, or the fight itself.
- **Fight balance:** intentionally not winnable at wood/no-armour; come back better equipped. Revisit only if stone/iron gear still walls progression.
- **Ambient darkness:** 0.1 accepted in repeated play.
- **Bossbar handover:** confirmed working.
- **Creature chase/leash:** confirmed working and judged fair.
- **Spawn rate:** “could go either way”; leave unchanged without a stronger signal.
- **Sneak hold/telegraph:** confirmed and judged intentional.
- **Recovery sleep:** confirmed and now automated.
