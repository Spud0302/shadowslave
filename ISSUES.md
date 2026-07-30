# Shadow Slave — Known Issues

## Current release status

**Canonical status:** see `PROJECT-STATUS.md`.

### Datapack

`datapack-v1.0.0` is released and frozen. There is no known datapack-fixable blocker requiring
more command architecture. Q4's remaining real-client judgement of the burdened-movement feel is an
evidence gap, not evidence that the released mechanic is broken; Slowness presence/removal is covered
by the accepted automated gate.

### Java core

Current development version is `0.1.0-alpha.4`.

- GitHub CI: build/tests/JAR/client/server smoke passed;
- independent Claude verification: pending;
- blocking tracker: GitHub Issue #16;
- no public Java release exists;
- no further Java feature package should merge until #16 closes.

Current implementation limits—not bugs disguised as finished features:

- no live datapack reader/writer or legacy cleanup;
- no persistent Nightmare registry/instance ownership;
- no natural infection or playable Java Nightmare;
- no appraisal/abilities/Memories/Dream Realm systems;
- no modpack manifest or external adapters.

### Current low-risk datapack uncertainties

- ravager roar damage is assumed rather than isolated;
- integer return coordinates can move a negative fractional position by roughly one block;
- death-screen presentation and the final human judgement of Slowness feel remain human-only;
- these do not justify expanding the frozen datapack unless a real defect is observed.

### Administrative correction

PRs #14 and #15 were merged after CI without the separate Claude gate required by collaboration
policy. Issue #16 restores that gate. The code is not being reverted merely for process; it is being
held from further feature stacking until independently reviewed and tested.

Everything below this line is the **historical issue log**. It is preserved because it records what
was believed/tested at each release; do not treat an unedited old sentence as the current runtime
contract. Current behaviour lives in `README.md`, current manual checks in the bottom of
`TESTING.md`, and shipped fixes in `CHANGELOG.md`.

---

> ## Fixed in `v1.2.1` — the post-sweep batch
>
> All ten outstanding bugs from the 39/39 test sweep, plus the `cure` message.
>
> |          |                                                                                                                                             |
> | -------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
> | **1.6**  | Drops now follow you out of the nightmare, including when you died — via a marker entity, since selectors cannot cross a dimension boundary |
> | **1.7**  | The soul readout has its own scratch objectives instead of borrowing `ss_health`; the ejection also now ignores a zero or failed read       |
> | **2.8**  | `return run` makes the infection branch actually branch — it was checking a tag its own previous line had just set                          |
> | **2.9**  | Creature speed comes from an effect; a ravager overwrites its own `movement_speed` attribute                                                |
> | **2.10** | The Spell no longer calls you while you are already in the trial                                                                            |
> | **2.11** | A corpse is no longer teleported home behind the death screen                                                                               |
> | **2.12** | `roll` clears all eight tags **and** all four attribute modifiers before applying a new roll                                                |
> | **2.13** | `Sleep Undisturbed` granted from `sleep.mcfunction`, where the Awakened branch actually runs                                                |
> | **3.2**  | Creature health 160 → **60** (15 wooden-sword hits). `attack_damage` stays 4                                                                |
> | **3.4**  | Sneaking on a bed shows _"The Spell reaches for you..."_ — the hold is kept, but telegraphed                                                |
> | —        | `cure` refuses on an Awakened instead of claiming the Spell lost interest                                                                   |
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

| #   | Check                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | How                                         | If it's wrong                                                                                 |
| --- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------- | --------------------------------------------------------------------------------------------- |
| 1.1 | ~~**Teleporting a sleeping player.**~~ **CONFIRMED WORKING v1.0.6.** Sleeping wakes you and pulls you in cleanly; title, bossbar and the Chosen advancement all fire. No `schedule` fallback needed.                                                                                                                                                                                                                                                                                                                                                                  | —                                           | —                                                                                             |
| 1.2 | ~~**Advancement `icon` format.**~~ **CONFIRMED WORKING v1.0.4.** `{"id": ...}` is correct for 1.21.1 — the tree renders and the icons draw.                                                                                                                                                                                                                                                                                                                                                                                                                           | —                                           | —                                                                                             |
| 1.3 | ~~**The dimension registers.**~~ **CONFIRMED WORKING v1.0.4**, after four rounds: `height` had to match the noise settings' range, and `monster_spawn_light_level` needed its bounds at the top level rather than nested under `value`.                                                                                                                                                                                                                                                                                                                               | —                                           | —                                                                                             |
| 1.4 | **`generic.` attribute prefix.** Correct for 1.21.1; the prefix was dropped in 1.21.2.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | `/attribute @s minecraft:generic.armor get` | On 1.21.2+ every Aspect and Flaw attribute silently does nothing. Drop `generic.` throughout. |
| 1.5 | ~~**`minecraft:generic.safe_fall_distance` exists in 1.21.1.**~~ **CONFIRMED WORKING v1.0.8.** It exists and applies. Retuned in v1.1.2: -2 left a safe distance of 1, and a standing jump is 1.25 blocks, so jumping on the spot cost half a heart. **PARTLY FALSIFIED — see §1.11.** "It applies" held for every check made at the time and is still true of a single manual test, but `0.7.2`'s in-pack trace showed the modifier failing to take effect across 14 consecutive upkeep executions. The attribute exists; relying on a modifier against it does not. | —                                           | —                                                                                             |

---

| 1.6 | ~~**Item recovery on death.**~~ **CONFIRMED WORKING v1.4.4, in-game.** After dying in the trial the drops land around the bed — most within pickup range, a few a couple of blocks out. Mis-graded three times before this, always as broken, always while working: the harness could not observe it. The nightmare's chunks unload when the player leaves, so an item query there reports nothing either way; force-loading makes them visible, and then the Overworld query returns the same coordinates as the nightmare one. The assertion was deleted rather than repaired. | — |

| 1.7 | ~~FIXED v1.2.1~~ — **Entering a nightmare shortly after `/trigger soul` ejects you instantly.** `soul.mcfunction:26` reuses the `ss_health` objective as scratch to hold the player's _armour_ for the readout — a shortcut marked with a `ponytail:` comment claiming it was always overwritten before a real read. `tick_player:7` compares that same score against the `..8` ejection threshold. **Repro (confirmed v1.2.0):** wear/carry some armour, `/trigger soul`, then enter — instant ejection, repeatedly. **Not reproduced** when entering without running `soul` first; a later `/scoreboard players get @s ss_health` read a correct 20, so the refresh does work in steady state. The exact failure window (suspected: the read failing on the cross-dimension arrival tick) is **not yet proven** — confirm by running `get` on the tick you are ejected, not after. | Give the soul readout its own scratch objective (`ss_scratch`) rather than borrowing `ss_health` — this removes the collision regardless of the window. Then gate the ejection on `matches 1..8` so a failed or zero read can never fire it. |

| 1.8 | ~~FIXED v1.3.1~~ — **The v1.2.1 ejection guard broke the death teardown, and with it the new item recovery.** §1.7's belt-and-braces changed the threshold from `..8` to `1..8` to reject a stale reading — but a real death is exactly **0**, so death stopped firing `eject` at all. No teardown, no item tagging; the player respawned in the Overworld and the dimension-mismatch cleanup then tagged items within 8 blocks of them _there_, where there were none. Two fixes from the same batch broke each other. Confirmed in-game v1.3.0 (R1): items spilled in the nightmare and despawned, bossbar still up on the death screen. | Guard the _read_, not the value: `scoreboard players reset @s ss_health` immediately before the `data get`, so a failed read leaves the score absent and `matches` fails on it — the same absent-score behaviour that caused the original lockout, used deliberately. Range goes back to `..8` so 0 is included. |

| 1.9 | ~~FIXED v1.4.1~~ — **Re-entering after ejection healed you to full, so failure cost nothing.** The `instant_health` on arrival was added in v1.1.1 to stop an entry loop (walk in at 2 hearts, ejected on the first tick, repeat) — but it also meant being cast out at 2 hearts and walking straight back in at full. Confirmed in-game v1.4.0. | Removed the heal; refuse entry below 14 health instead, with _"You are too weak. The Spell has no use for you yet."_ Failure now costs you the time to recover, and the loop is still impossible because you cannot enter weak enough to be ejected immediately. |

| 1.10 | ~~FIXED v1.4.3~~ — **The cooldown was enforced in the callers, and never set on death.** Two faults of the same shape, both found by the harness. The guard lived in `sleep.mcfunction` and the sneak check rather than in `enter.mcfunction`, so any other route in bypassed it; and it was set in `eject.mcfunction`, which death never reaches — dying goes through the dimension-mismatch cleanup straight to `leave`, so a death set no cooldown at all. | Guard moved into `enter.mcfunction`, the choke point every entry passes through. Set in `leave.mcfunction`, the shared teardown every exit passes through. `test/nightmare` now clears the cooldown deliberately rather than slipping past it. |
| 1.11 | ~~FIXED v0.7.3~~ — **A `safe_fall_distance` modifier could silently fail to apply, so the Weightless Flaw cost some players nothing.** `flaw/weightless.mcfunction` ran its `modifier add` successfully while the attribute stayed at its vanilla 3, across 14 consecutive upkeep executions — the soul readout still named the Flaw the player was not paying for. Raised as **Q4** at `0.7.0` and misdiagnosed **four times** as a harness fault (poll budget, upkeep scheduling, chat re-entrancy, polling on every tick) before GPT instrumented the pack itself and read the state server-side in the same tick. The lesson that broke the deadlock: chat round-trips perturb the timing they measure, so instrument from inside. | — | Retired the mechanic rather than patching an unreliable one: `flaw/burdened` applies Slowness I, refreshed once per second by upkeep. The `ss_flaw_weightless` tag is kept purely as a save/import id, so existing worlds and the Java importer need no migration. Java should import family 4 as a retreat-derived burden, **not** as a requirement to reproduce `safe_fall_distance - 1`. |

## 2. Probably wrong, low blast radius

| #   | Issue                                                                                 | Detail                                                                                                                                                                              |
| --- | ------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 2.1 | ~~**Creature's fire resistance may not apply.**~~ **CONFIRMED BROKEN, FIXED v1.0.8.** | An in-game `/data get` showed no effects on the creature at all — 1.20.5 renamed the key to `active_effects` and the pack used the old `ActiveEffects`, so it was silently dropped. |
| 2.2 | **Ravager roar damage is assumed ~6.**                                                | The ejection threshold was set against that figure. The roar fires when you block with a shield and is not governed by `attack_damage`.                                             |
| 2.3 | **Return position truncates toward zero.**                                            | Coordinates go through integer scoreboards, so sleeping at x = −10.7 returns you to −10. Up to one block off, and only on negative coordinates.                                     |

---

| 2.4 | **The nightmare may still be too dark, or not dark enough.** `ambient_light` went 0.0 → 0.1 in v1.0.7 (matching the Nether) after 0.0 proved unplayable — pitch black, not atmospheric. This is the one dial for it. | Raise toward 0.15 if still unreadable; drop toward 0.05 if it feels too safe. Changing it needs a fresh world, since dimension types are baked in at world creation. |

| 2.5 | ~~**Returning from a nightmare can trap you in the bed.**~~ **FIXED v1.0.9.** Vanilla's own bed-exit search is not exposed to commands, so `nightmare/unstick` is the manual equivalent: check the eight neighbours for two blocks of headroom and step into the first one. |

| 2.6 | ~~**`is_sneaking` entity flag is unverified.**~~ **CONFIRMED WORKING v1.2.0.** Sneaking on a bed in daylight pulls a Carrier in. Requires about a second's hold, because the check polls once per second — telegraphed as of v1.2.1. | — |

| 2.7 | ~~**Creature could spawn in your face.**~~ **FIXED v1.1.1.** `spreadplayers`' distance argument is the minimum gap _between targets_, so with one creature it did nothing and the range was uniform 0-14 blocks. The spread is now centred 12 blocks ahead of the player, giving a consistent 8-16. |

| 2.8 | ~~FIXED v1.2.1~~ — **First sleep both infects you and pulls you in.** `sleep.mcfunction` runs `infect` (which adds the `ss_carrier` tag) and then guards the early return on `unless entity @s[tag=ss_carrier]` — already false, so execution falls through into the nightmare. A self-invalidating guard. Found in-game v1.2.0. | `execute unless entity @s[tag=ss_carrier] run return run function shadowslave:infect`, so the branch returns in the same command that changes the state. |

| 2.9 | ~~FIXED v1.2.1~~ — **Setting `movement_speed` on the creature does nothing.** An in-game dump showed `base: 0.0` where the file sets `0.32`, and `0.3499` on an earlier read — vanilla ravagers zero their own movement speed during their stun/attack state and restore it to the ravager default, overwriting whatever we set. | If the creature's speed needs tuning, apply it as a `minecraft:speed` entry in `active_effects` instead: effects stack on top of the base and the ravager's own logic does not touch them. |

| 3.4 | ~~FIXED v1.2.1, CONFIRMED WORKING v1.4.4~~ — the telegraph appears immediately on crouching, before the hold completes. — **Sneak-to-enter needs a short hold, and does not say so.** The check polls once per second on the shared clock, so a quick tap falls between polls. A deliberate hold is arguably the _right_ interaction — an accidental sneak near a bed should not drop you into a lethal trial — but it currently reads as unresponsive rather than intentional. Confirmed working in-game v1.2.0. | Either poll every 5 ticks so a tap registers, or keep the hold and telegraph it: an actionbar line like _"The Spell reaches for you..."_ while sneaking on a bed. The second is better design and about as cheap. |

| 2.10 | ~~FIXED v1.2.1~~ — **The Spell keeps calling you while you are inside the nightmare.** `carrier.mcfunction` guards on Carrier and not-Awakened, but never on `ss_in_nightmare` — so a player mid-trial gets a nausea pulse and the actionbar line every 30 seconds. Nausea during the boss fight is a real handicap, and thematically you have already answered the call. Confirmed in-game v1.2.0. | Add `execute if entity @s[tag=ss_in_nightmare] run return 0` to the guards at the top of `carrier.mcfunction`. |

| 2.11 | ~~FIXED v1.2.1~~ — **Dying in the trial teleports your corpse home behind the death screen.** The per-tick health check sees 0 HP on a dead-but-not-yet-respawned player, fires the ejection, and `leave` runs its cross-dimension teleport — so the player watches the portal warp effect and their bed appear while the death screen is up. Then vanilla respawn sends them to the same place anyway. Confirmed in-game v1.2.0. | The state cleanup must still run on death; only the teleport is redundant. Add an early `return 0` in `leave.mcfunction` after the tag/bossbar/creature cleanup but before the return teleport, when `@s[nbt={Health:0.0f}]` — reading player NBT is allowed, only writing is refused. |

| 2.12 | ~~FIXED v1.2.1~~ — **Every attribute modifier orphans when its Aspect/Flaw tag is removed.** CONFIRMED in-game v1.2.0 for both: after cycling Bone→Wind→Flame and dropping Fragile, the soul readout showed `Vitality: 14  Endurance: 6` — Fragile's max-health penalty and Bone's armour bonus both still applied, with neither tag present. `awaken/roll` also never clears previous tags, compounding it. | Clear all eight `ss_aspect_*` / `ss_flaw_*` tags **and** strip all four attribute modifiers at the top of `roll.mcfunction` before applying the new roll. |

| 2.13 | ~~FIXED v1.2.1~~ — **`Sleep Undisturbed` is unreachable — regression from the v1.2.0 entry refactor.** The grant sits at `nightmare/enter.mcfunction:7` gated on `ss_rank matches 1..`, but `sleep.mcfunction:16` now returns early for Awakened players, so `enter` is never called for them. Adding `sleep.mcfunction` moved the Awakened check upstream and orphaned the grant behind a branch that can no longer reach it. Confirmed in-game v1.2.0 — the behaviour is right, only the advancement never fires. | Move the grant into `sleep.mcfunction`, onto the same line as the Awakened early return. |

| 3.5 | ~~**Getting out of bed may BE the entry gesture.**~~ **NOT REACHABLE — closed v1.3.0.** The trap needs a Carrier to wake up lying in a bed, and that state does not occur: a Carrier who sleeps is pulled in immediately, and the only sleep that ends with you in a bed is the first one, which happens while you are still untouched. Confirmed in-game: right-clicking a bed at night as a Carrier goes straight into the trial. | — |

## 3. Design problems worth a decision

These entries are historical; current resolutions are called out where known.

| #   | Issue                                                            | Resolution / options                                                                                                                                                         |
| --- | ---------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 3.1 | ~~**Flame Aspect completely nullifies the Shadow Slave flaw.**~~ | **FIXED v1.0.1.** The Flaw uses magic damage so Flame's fire resistance does not cancel it.                                                                                  |
| 3.2 | ~~**The fight is unwinnable at the tier it happens.**~~          | **FIXED/RETUNED v1.2.1.** Creature health dropped from 160 to 60. Later play established that wood/no-armour is still intentionally insufficient; come back better equipped. |
| 3.3 | ~~**The verification advancement tab ships with the pack.**~~    | **RESOLVED on `gpt/datapack-release-completion`, pending merge/version.** Display copy is now player-facing while historical ids remain compatible.                          |

---

- **Nightmare mob spawn rate is unsettled.** Judged "alright, could go either way" on one playtest — not enough to move it. The dial is the `spawners.monster` weights in `worldgen/biome/nightmare.json`: zombie 40, skeleton 30, spider 20, phantom 10, with `minCount`/`maxCount` per entry. Revisit when more than one person has played it.

## 4. Deliberate limitations — historical wording

The current authoritative list is at the top of this file and in README. These older notes are kept
for the development record.

- **The First Nightmare is not winnable at wood/no-armour, and that is now intended.** Measured on Normal difficulty, wooden sword, no armour: 4-6 hits landed of the 15 needed, ejected after ~3 hits taken (v1.3.0, 60 HP creature — was 3 of 40 at 160 HP). Each entry spawns a fresh creature, so attempts do not chip it down. The answer is to come back better equipped.
- **Instant-kill damage bypasses ejection entirely.** The teardown still runs correctly; this is a limit of the gear-retention promise, not a state bug.
- **The leash teleports the creature onto you, and it gets a free hit.** Confirmed and judged fair; fleeing should cost something.
- **Single player at a time.** One global bossbar/creature ownership model; true instance ownership is deferred to Java.
- **Ejection happens above death, not at death.** Intercepting real death without dropping gear is not reliably possible in a datapack.
- **The Nightmare Creature is a reskinned ravager.** Bespoke AI/model belongs in Java.
- **Terrain is Overworld noise.** Only lighting, sky and spawns are nightmarish.
- ~~**`test/reset` from inside the nightmare strands you.**~~ **FIXED v1.0.1 and hardened again v1.4.9.** Current reset tears down first and clears transient cooldown state afterward.

---

## 5. Spec drift — historical

The original Phase 1 spec is now indexed as **HISTORICAL / SUPERSEDED** in
`docs/superpowers/specs/README.md`. Do not treat the old checklist below as current work.

- Flame's burning strikes were later implemented.
- The soul readout terminology/model was corrected in v1.4.8/v1.4.9.
- The old planned creature loot table was never needed for Phase 1.
- Historical objective-name differences are implementation details, not current defects.

---

## 6. Housekeeping — historical

The old validator-coverage concerns have been resolved across later releases. Current `validate.py`
checks references, objectives, bossbars, tags, modifier pairing, dimension/biome schema assumptions,
absent-score policy, and release version agreement.

`ss_gone` still intentionally uses a 40-tick win delay to avoid a rejoin/chunk-load race.

---

## What the reviews already caught and fixed

Listed so they don't get re-litigated. Every one of these was a real defect in the
plan, found during execution:

- `execute in <dimension>` scopes only its own `run` — bare follow-up commands ran in the Overworld.
- **Minecraft refuses all player NBT writes.** Return teleports now route through command storage and a macro.
- Tags survive death, so death teardown must explicitly clear trial state.
- Entry once teleported players to y=150 after safe placement, causing fatal falls.
- `if score … matches 0` fails on an absent score rather than treating it as zero.
- A distance filter on an absence test once made the walk-away exploit easier.
- Quitting mid-fight once awarded a free win because the boss chunk had not deserialized on rejoin.
- A failed summon once marked the player as fighting a creature that did not exist, which the win condition read as victory.
