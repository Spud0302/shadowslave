# Shadow Slave — Known Issues

> ## Fixed in `v1.2.1` — the post-sweep batch
>
> All ten outstanding bugs from the 39/39 test sweep, plus the `cure` message.
>
> | | |
> | --- | --- |
> | **1.6** | Drops now follow you out of the nightmare, including when you died — via a marker entity, since selectors cannot cross a dimension boundary |
> | **1.7** | The soul readout has its own scratch objectives instead of borrowing `ss_health`; the ejection also now ignores a zero or failed read |
> | **2.8** | `return run` makes the infection branch actually branch — it was checking a tag its own previous line had just set |
> | **2.9** | Creature speed comes from an effect; a ravager overwrites its own `movement_speed` attribute |
> | **2.10** | The Spell no longer calls you while you are already in the trial |
> | **2.11** | A corpse is no longer teleported home behind the death screen |
> | **2.12** | `roll` clears all eight tags **and** all four attribute modifiers before applying a new roll |
> | **2.13** | `Sleep Undisturbed` granted from `sleep.mcfunction`, where the Awakened branch actually runs |
> | **3.2** | Creature health 160 → **60** (15 wooden-sword hits). `attack_damage` stays 4 |
> | **3.4** | Sneaking on a bed shows *"The Spell reaches for you..."* — the hold is kept, but telegraphed |
> | — | `cure` refuses on an Awakened instead of claiming the Spell lost interest |
>
> Also swept every guard in the pack for the self-invalidating shape that caused 2.8, 2.10
> and 2.13. None remain.
>
> **Untested.** Every one of these was written against a game that cannot run here.


> **Fixed in `v1.0.1`** (while you were testing `v1.0.0`): §3.1 Flame no longer cancels the
> Shadow Slave flaw; §4 `test/reset` no longer strands you in the nightmare; §5 burning
> strikes and the attributes readout are implemented, and the two spec-drift entries are
> amended; §6 the validator now checks function, predicate, dimension, objective, bossbar
> and tag references plus unpaired attribute modifiers, the comment-guard is in, and the
> countdown stops once the creature spawns.
>
> Everything in §1 and §2 still needs your in-game observations — those are untouched.

Status at `v1.0.0` (Phase 1, "The First Nightmare"). Nothing here has ever run in
Minecraft — the build box has no game client, so every behavioural claim below is
reasoning, not observation.

Grouped by what you should do about it.

---

## 1. Confirm these first — they can break the whole pack

If any of these is wrong, the thing it touches fails silently. Minecraft does not
warn you.

| #   | Check                                                                                                                                              | How                                                                                                                    | If it's wrong                                                                                                                       |
| --- | -------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| 1.1 | ~~**Teleporting a sleeping player.**~~ **CONFIRMED WORKING v1.0.6.** Sleeping wakes you and pulls you in cleanly; title, bossbar and the Chosen advancement all fire. No `schedule` fallback needed. | — | — |
| 1.2 | ~~**Advancement `icon` format.**~~ **CONFIRMED WORKING v1.0.4.** `{"id": ...}` is correct for 1.21.1 — the tree renders and the icons draw. | — | — |
| 1.3 | ~~**The dimension registers.**~~ **CONFIRMED WORKING v1.0.4**, after four rounds: `height` had to match the noise settings' range, and `monster_spawn_light_level` needed its bounds at the top level rather than nested under `value`. | — | — |
| 1.4 | **`generic.` attribute prefix.** Correct for 1.21.1; the prefix was dropped in 1.21.2.                                                             | `/attribute @s minecraft:generic.armor get`                                                                            | On 1.21.2+ every Aspect and Flaw attribute silently does nothing. Drop `generic.` throughout.                                       |
| 1.5 | ~~**`minecraft:generic.safe_fall_distance` exists in 1.21.1.**~~ **CONFIRMED WORKING v1.0.8.** It exists and applies. Retuned in v1.1.2: -2 left a safe distance of 1, and a standing jump is 1.25 blocks, so jumping on the spot cost half a heart. | — | — |

---

| 1.6 | **Item recovery on death — mechanism VERIFIED, end-to-end UNCONFIRMED.** Direct probes show it working: the drops exist at the death position 100ms after death, are gone from the nightmare by 300ms (the sweep fires at 5 ticks), and arrive in the Overworld at exactly the stored return position. But a full harness run finds no item entities in the Overworld at all. The harness performs several entry/return cycles before the death test, and something in that sequence appears to interfere — unproven. **I have mis-graded this twice** (claimed fixed off one passing run, then claimed intermittent off a failing one), so it needs a real playthrough to settle. | Test T1 by hand. If items arrive after a single clean entry-and-die, the feature is fine and only the harness sequence is at fault. If they do not, compare against the probe: the difference is the number of entries beforehand. |

| 1.7 | ~~FIXED v1.2.1~~ — **Entering a nightmare shortly after `/trigger soul` ejects you instantly.** `soul.mcfunction:26` reuses the `ss_health` objective as scratch to hold the player's *armour* for the readout — a shortcut marked with a `ponytail:` comment claiming it was always overwritten before a real read. `tick_player:7` compares that same score against the `..8` ejection threshold. **Repro (confirmed v1.2.0):** wear/carry some armour, `/trigger soul`, then enter — instant ejection, repeatedly. **Not reproduced** when entering without running `soul` first; a later `/scoreboard players get @s ss_health` read a correct 20, so the refresh does work in steady state. The exact failure window (suspected: the read failing on the cross-dimension arrival tick) is **not yet proven** — confirm by running `get` on the tick you are ejected, not after. | Give the soul readout its own scratch objective (`ss_scratch`) rather than borrowing `ss_health` — this removes the collision regardless of the window. Then gate the ejection on `matches 1..8` so a failed or zero read can never fire it. |

| 1.8 | ~~FIXED v1.3.1~~ — **The v1.2.1 ejection guard broke the death teardown, and with it the new item recovery.** §1.7's belt-and-braces changed the threshold from `..8` to `1..8` to reject a stale reading — but a real death is exactly **0**, so death stopped firing `eject` at all. No teardown, no item tagging; the player respawned in the Overworld and the dimension-mismatch cleanup then tagged items within 8 blocks of them *there*, where there were none. Two fixes from the same batch broke each other. Confirmed in-game v1.3.0 (R1): items spilled in the nightmare and despawned, bossbar still up on the death screen. | Guard the *read*, not the value: `scoreboard players reset @s ss_health` immediately before the `data get`, so a failed read leaves the score absent and `matches` fails on it — the same absent-score behaviour that caused the original lockout, used deliberately. Range goes back to `..8` so 0 is included. |

| 1.9 | ~~FIXED v1.4.1~~ — **Re-entering after ejection healed you to full, so failure cost nothing.** The `instant_health` on arrival was added in v1.1.1 to stop an entry loop (walk in at 2 hearts, ejected on the first tick, repeat) — but it also meant being cast out at 2 hearts and walking straight back in at full. Confirmed in-game v1.4.0. | Removed the heal; refuse entry below 14 health instead, with *"You are too weak. The Spell has no use for you yet."* Failure now costs you the time to recover, and the loop is still impossible because you cannot enter weak enough to be ejected immediately. |

## 2. Probably wrong, low blast radius

| #   | Issue                                         | Detail                                                                                                                                                                                                             |
| --- | --------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 2.1 | ~~**Creature's fire resistance may not apply.**~~ **CONFIRMED BROKEN, FIXED v1.0.8.** An in-game `/data get` showed no effects on the creature at all — 1.20.5 renamed the key to `active_effects` and the pack used the old `ActiveEffects`, so it was silently dropped. |
| 2.2 | **Ravager roar damage is assumed ~6.**        | The ejection threshold (9 HP) was set against that figure. The roar fires when you block with a shield and is not governed by `attack_damage`. If it hits harder, raise the threshold in `tick_player.mcfunction`. |
| 2.3 | **Return position truncates toward zero.**    | Coordinates go through integer scoreboards, so sleeping at x = −10.7 returns you to −10. Up to one block off, and only on negative coordinates.                                                                    |

---

| 2.4 | **The nightmare may still be too dark, or not dark enough.** `ambient_light` went 0.0 → 0.1 in v1.0.7 (matching the Nether) after 0.0 proved unplayable — pitch black, not atmospheric. This is the one dial for it. | Raise toward 0.15 if still unreadable; drop toward 0.05 if it feels too safe. Changing it needs a fresh world, since dimension types are baked in at world creation. |

| 2.5 | ~~**Returning from a nightmare can trap you in the bed.**~~ **FIXED v1.0.9.** Vanilla's own bed-exit search is not exposed to commands, so `nightmare/unstick` is the manual equivalent: check the eight neighbours for two blocks of headroom and step into the first one. |

| 2.6 | ~~**`is_sneaking` entity flag is unverified.**~~ **CONFIRMED WORKING v1.2.0.** Sneaking on a bed in daylight pulls a Carrier in. Requires about a second's hold, because the check polls once per second — telegraphed as of v1.2.1. | — |

| 2.7 | ~~**Creature could spawn in your face.**~~ **FIXED v1.1.1.** `spreadplayers`' distance argument is the minimum gap *between targets*, so with one creature it did nothing and the range was uniform 0-14 blocks. The spread is now centred 12 blocks ahead of the player, giving a consistent 8-16. |

| 2.8 | ~~FIXED v1.2.1~~ — **First sleep both infects you and pulls you in.** `sleep.mcfunction` runs `infect` (which adds the `ss_carrier` tag) and then guards the early return on `unless entity @s[tag=ss_carrier]` — already false, so execution falls through into the nightmare. A self-invalidating guard. Found in-game v1.2.0. | `execute unless entity @s[tag=ss_carrier] run return run function shadowslave:infect`, so the branch returns in the same command that changes the state. |

| 2.9 | ~~FIXED v1.2.1~~ — **Setting `movement_speed` on the creature does nothing.** An in-game dump showed `base: 0.0` where the file sets `0.32`, and `0.3499` on an earlier read — vanilla ravagers zero their own movement speed during their stun/attack state and restore it to the ravager default, overwriting whatever we set. | If the creature's speed needs tuning, apply it as a `minecraft:speed` entry in `active_effects` instead: effects stack on top of the base and the ravager's own logic does not touch them. |

| 3.4 | ~~FIXED v1.2.1~~ — **Sneak-to-enter needs a short hold, and does not say so.** The check polls once per second on the shared clock, so a quick tap falls between polls. A deliberate hold is arguably the *right* interaction — an accidental sneak near a bed should not drop you into a lethal trial — but it currently reads as unresponsive rather than intentional. Confirmed working in-game v1.2.0. | Either poll every 5 ticks so a tap registers, or keep the hold and telegraph it: an actionbar line like *"The Spell reaches for you..."* while sneaking on a bed. The second is better design and about as cheap. |

| 2.10 | ~~FIXED v1.2.1~~ — **The Spell keeps calling you while you are inside the nightmare.** `carrier.mcfunction` guards on Carrier and not-Awakened, but never on `ss_in_nightmare` — so a player mid-trial gets a nausea pulse and the actionbar line every 30 seconds. Nausea during the boss fight is a real handicap, and thematically you have already answered the call. Confirmed in-game v1.2.0. | Add `execute if entity @s[tag=ss_in_nightmare] run return 0` to the guards at the top of `carrier.mcfunction`. |

| 2.11 | ~~FIXED v1.2.1~~ — **Dying in the trial teleports your corpse home behind the death screen.** The per-tick health check sees 0 HP on a dead-but-not-yet-respawned player, fires the ejection, and `leave` runs its cross-dimension teleport — so the player watches the portal warp effect and their bed appear while the death screen is up. Then vanilla respawn sends them to the same place anyway. Confirmed in-game v1.2.0. | The state cleanup must still run on death; only the teleport is redundant. Add an early `return 0` in `leave.mcfunction` after the tag/bossbar/creature cleanup but before the return teleport, when `@s[nbt={Health:0.0f}]` — reading player NBT is allowed, only writing is refused. |

| 2.12 | ~~FIXED v1.2.1~~ — **Every attribute modifier orphans when its Aspect/Flaw tag is removed.** CONFIRMED in-game v1.2.0 for both: after cycling Bone→Wind→Flame and dropping Fragile, the soul readout showed `Vitality: 14  Endurance: 6` — Fragile's max-health penalty and Bone's armour bonus both still applied, with neither tag present. The upkeep stops *reapplying* a modifier but nothing removes the one already on the player. `awaken/roll` also never clears previous tags, compounding it. Latent in normal play (you Awaken once); any re-roll leaves you carrying powers and penalties you no longer have. | Clear all eight `ss_aspect_*` / `ss_flaw_*` tags **and** strip all four attribute modifiers at the top of `roll.mcfunction` before applying the new roll. |

| 2.13 | ~~FIXED v1.2.1~~ — **`Sleep Undisturbed` is unreachable — regression from the v1.2.0 entry refactor.** The grant sits at `nightmare/enter.mcfunction:7` gated on `ss_rank matches 1..`, but `sleep.mcfunction:16` now returns early for Awakened players, so `enter` is never called for them. Adding `sleep.mcfunction` moved the Awakened check upstream and orphaned the grant behind a branch that can no longer reach it. Confirmed in-game v1.2.0 — the behaviour is right, only the advancement never fires. | Move the grant into `sleep.mcfunction`, onto the same line as the Awakened early return: `execute if score @s ss_rank matches 1.. run advancement grant @s only shadowslave:test/bypass`, immediately before the `return 0`. |

| 3.5 | ~~**Getting out of bed may BE the entry gesture.**~~ **NOT REACHABLE — closed v1.3.0.** The trap needs a Carrier to wake up lying in a bed, and that state does not occur: a Carrier who sleeps is pulled in immediately, and the only sleep that ends with you in a bed is the first one, which happens while you are still untouched. Confirmed in-game: right-clicking a bed at night as a Carrier goes straight into the trial. | — |

## 3. Design problems worth a decision

These work as built. Whether they're _right_ is your call.

| #   | Issue                                                                                                                                                                                | Options                                                                                                                                                                                                             |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 3.1 | **Flame Aspect completely nullifies the Shadow Slave flaw.** Flame grants fire resistance; Shadow Slave deals `minecraft:on_fire` damage. Roll both and you have no drawback at all. | Change the flaw's damage type to `minecraft:magic` — one word, already the documented fallback. Or keep it as a lucky roll.                                                                                         |
| 3.2 | ~~FIXED v1.2.1~~ — **The fight is unwinnable at the tier it happens.** Measured in-game v1.2.0, wooden sword and no armour: **3 hits landed (12 of 160 damage, 7.5%) before ejection at ~3 hits taken.** Winning would need roughly thirteen ejection-free runs' worth of damage in one attempt. The creature is not hard, it is impossible — and Phase 1 has nothing else to do, so the Awakening is unreachable. | Cut `max_health` hard — 160 → somewhere near 60, which is 15 wooden-sword hits. Keep `attack_damage` at 4 so it stays frightening. Also see 2.9: its speed is not actually being set, so kiting is more viable than intended. |
| 3.3 | **The verification advancement tab ships with the pack.**                                                                                                                            | Fine while testing. Before any public release either delete `advancement/test/` and its grant lines, or convert the tree into real player-facing advancements — the trigger points are already in the right places. |

---

- **Nightmare mob spawn rate is unsettled.** Judged "alright, could go either way" on one playtest — not enough to move it. The dial is the `spawners.monster` weights in `worldgen/biome/nightmare.json`: zombie 40, skeleton 30, spider 20, phantom 10, with `minCount`/`maxCount` per entry. Revisit when more than one person has played it.

## 4. Deliberate limitations — not bugs

- **The First Nightmare is not winnable at wood/no-armour, and that is now intended.** Measured on Normal difficulty, wooden sword, no armour: 4-6 hits landed of the 15 needed, ejected after ~3 hits taken (v1.3.0, 60 HP creature — was 3 of 40 at 160 HP). Each entry spawns a fresh creature, so attempts do not chip it down. The answer is to come back better equipped, which the v1.4.0 cooldown makes a real loop rather than a bounce. Revisit if iron-tier also proves impossible.
- **Instant-kill damage bypasses ejection entirely.** The threshold catches you at 9 HP, so anything that takes you from above that straight to 0 — a long fall, lava, the void, `/kill` — kills you outright and drops your gear. Confirmed in-game v1.2.0 via `/kill`. The teardown still runs correctly (tag cleared, bossbar hidden, creature removed), so this is a limit of the gear-retention promise, not a state bug.
- **The leash teleports the creature onto you, and it gets a free hit.** Confirmed in-game v1.2.0 at ~234 blocks. Judged fair by the playtester: fleeing should cost something, and you cannot outrun the Nightmare. If it ever needs softening, teleport it a few blocks away rather than to the player's exact position.
Recorded so nobody "fixes" them by accident.

- **Single player at a time.** One global bossbar, and one player leaving the nightmare kills every creature in it — so a second player mid-trial gets a free Awakening. Per-player bossbars need macro-generated ids; per-player creatures need owner tags. Both deferred to Phase 6.
- **Ejection at 9 HP, not at death.** Intercepting real death without dropping the player's gear is not reliably possible in a datapack, and keeping the gear matters more than the exact threshold.
- **The Nightmare Creature is a reskinned ravager.** A bespoke creature needs custom models and AI, which means Java. Phase 2+.
- **Terrain is Overworld noise.** Only the lighting, sky and spawns are nightmarish.
- **`/function shadowslave:test/reset` run from inside the nightmare strands you.** It clears your tag without teleporting you out, and beds don't work there. Test-only tool — `/tp` yourself out.

---

## 5. Spec drift — the design doc promises things Phase 1 doesn't deliver

Either build them or amend the spec.

- **Flame's "burning strikes"** — the spec's Aspect table promises fire immunity _and_ burning strikes; only immunity is implemented.
- **Soul readout omits attributes** — spec says `/trigger soul` prints "rank, Aspect, Flaw and attributes". It prints the first three.
- **`loot_table/nightmare_creature.json`** is in the spec's file layout and was never created. The creature drops vanilla ravager loot. Probably correct for Phase 1, since Memories are Phase 2 — strike it from the spec.
- **Objective naming** — spec says `ss_return_{x,y,z}`, implementation uses `ss_ret_{x,y,z}`. Implementation is internally consistent; update the spec.

---

## 6. Housekeeping

- **The validator's coverage is thin.** It checks `pack_format`, singular directory names, JSON validity, function-tag references, advancement parents and advancement grants. It does **not** check `function`/`predicate` references, objective names, attribute-modifier ids or bossbar ids — all silent-failure classes in Minecraft, and all cheap regex additions over the same file walk.
- **`validate.py` grant-reference regex has no `#` comment guard.** A commented-out `advancement grant` line would trip a false failure. Harmless today.
- **`ss_timer` keeps decrementing after the creature spawns.** Cosmetic — the health line overwrites the bossbar in the same tick, and `leave` resets it. Integer overflow is about 3.4 years of continuous ticking.
- **The `ss_gone` win-delay is 40 ticks.** That guards against a rejoin being misread as a victory before the boss's chunk loads. Two seconds is generous single-player; a laggy server may need more.

---

## What the reviews already caught and fixed

Listed so they don't get re-litigated. Every one of these was a real defect in the
plan, found during execution:

- `execute in <dimension>` scopes only its own `run` — bare follow-up commands ran in the Overworld.
- **Minecraft refuses all player NBT writes.** Three sites relied on them; the return teleport silently dumped every player at Overworld 0,0,0 inside stone. Now routed through command storage and a macro function.
- Tags survive death, so dying in the trial left you permanently tagged and eventually spawned the boss beside your bed.
- Entry teleported you to y=150 _after_ `spreadplayers` had already placed you safely — a 60-90 block fatal fall.
- `if score … matches 0` fails on an _absent_ score, and nothing ever wrote 0 — so `/trigger soul` printed an empty card to every un-Awakened player.
- A distance filter on an _absence_ test made the walk-away exploit easier, not harder.
- Quitting mid-fight awarded a free win, because the boss's chunk hadn't deserialized on rejoin.
- A failed summon tagged you as fighting a creature that didn't exist, which the win condition read as victory.
