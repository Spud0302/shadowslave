# Shadow Slave — Known Issues

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
| 1.2 | **Advancement `icon` format.** The pack uses the 1.20.5+ item-stack form `{"id": "minecraft:red_bed"}`.                                            | Watch the log on first `/reload` for advancement parse errors, and check the "Shadow Slave — Verification" tab exists. | Older form is `{"item": "minecraft:red_bed"}`. A bad icon kills the entire advancement file.                                        |
| 1.3 | **The dimension registers.** `dimension_type` declares `height: 256` while the `minecraft:overworld` noise settings it generates from declare 384. | `/function shadowslave:test/selfcheck` — its dimension probe covers this.                                              | If it fails to register the whole pack is dead. Set `height: 384` and `logical_height: 384`.                                        |
| 1.4 | **`generic.` attribute prefix.** Correct for 1.21.1; the prefix was dropped in 1.21.2.                                                             | `/attribute @s minecraft:generic.armor get`                                                                            | On 1.21.2+ every Aspect and Flaw attribute silently does nothing. Drop `generic.` throughout.                                       |
| 1.5 | **`minecraft:generic.safe_fall_distance` exists in 1.21.1.**                                                                                       | Roll the Weightless flaw, jump off something.                                                                          | Swap the Weightless flaw to a different mechanism.                                                                                  |

---

## 2. Probably wrong, low blast radius

| #   | Issue                                         | Detail                                                                                                                                                                                                             |
| --- | --------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 2.1 | **Creature's fire resistance may not apply.** | `spawn_creature.mcfunction` uses `ActiveEffects:` — the 1.20.5 rename made it `active_effects`. Check with `/data get entity @e[tag=ss_creature,limit=1]`. Minor gameplay impact.                                  |
| 2.2 | **Ravager roar damage is assumed ~6.**        | The ejection threshold (9 HP) was set against that figure. The roar fires when you block with a shield and is not governed by `attack_damage`. If it hits harder, raise the threshold in `tick_player.mcfunction`. |
| 2.3 | **Return position truncates toward zero.**    | Coordinates go through integer scoreboards, so sleeping at x = −10.7 returns you to −10. Up to one block off, and only on negative coordinates.                                                                    |

---

## 3. Design problems worth a decision

These work as built. Whether they're _right_ is your call.

| #   | Issue                                                                                                                                                                                | Options                                                                                                                                                                                                             |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 3.1 | **Flame Aspect completely nullifies the Shadow Slave flaw.** Flame grants fire resistance; Shadow Slave deals `minecraft:on_fire` damage. Roll both and you have no drawback at all. | Change the flaw's damage type to `minecraft:magic` — one word, already the documented fallback. Or keep it as a lucky roll.                                                                                         |
| 3.2 | **The trial is probably very hard.** 160 HP boss, faster than sprinting, 0.8 knockback resistance, and the leash means you cannot retreat to heal.                                   | Lower `max_health` in `spawn_creature.mcfunction`, or accept it — failure is soft (gear kept, retry next sleep). Tune after playing.                                                                                |
| 3.3 | **The verification advancement tab ships with the pack.**                                                                                                                            | Fine while testing. Before any public release either delete `advancement/test/` and its grant lines, or convert the tree into real player-facing advancements — the trigger points are already in the right places. |

---

## 4. Deliberate limitations — not bugs

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
