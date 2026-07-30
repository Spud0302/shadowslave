# Datapack release machinery — implementation brief for Claude

**Written against:** `main` at `a470b914f3e0710d3dfee63adc29b8e6e50d4599`

**Related GPT branch:** `gpt/datapack-release`

**Intent:** Andrew wants the vanilla datapack left in a genuinely completed/releasable Phase 1 state before Java development begins. Do not turn the datapack into a miniature Java architecture; remove the remaining command-level correctness hazards and make the known ceilings safe.

This brief deliberately targets files in Claude's usual column from `docs/COLLABORATION.md`: state-machine entry, storage/macro plumbing, harness, and release machinery. GPT has handled player-facing/canon copy on the branch and should not independently race these files.

---

## 1. Enforce one active First Nightmare at a time

### Why

The current pack is documented as "single player at a time", but nothing enforces it. That is unsafe rather than merely limited:

- `shadowslave:trial` is one global bossbar;
- Nightmare Creatures use global `ss_creature` selectors;
- `leave` kills every `ss_creature` in the Nightmare dimension;
- return/sweep storage is shared.

If a second player enters while the first is active, one run can corrupt or complete the other. True ownership/instances belong in Java; the completed datapack should instead **enforce its actual supported concurrency**.

### Required behavior

At the `nightmare/enter.mcfunction` choke point:

- a player already in their own Nightmare still receives the existing duplicate-entry behavior;
- an otherwise eligible Carrier is refused if **another online player currently has `ss_in_nightmare`**;
- refusal must happen before the new player's return storage/timer/bossbar/trial state is mutated;
- message should make the temporary limitation understandable without pretending it is canon, e.g. "The Spell is already weaving another Nightmare. It will come for you soon." Exact copy is Claude's call unless Andrew prefers different wording.

A simple player-state guard is preferable to introducing a second global lock that can itself go stale. After the self-duplicate guard has returned, any remaining `@a[tag=ss_in_nightmare]` match represents another active player.

### Must NOT change

- existing rank, infection, cooldown or weakness semantics;
- `ss_test_bypass` meaning;
- current teardown order;
- death/ejection behavior;
- true multiplayer instancing is **not** part of this task.

### Acceptance criteria

A two-player behavioral test should prove:

1. Player A can enter normally.
2. Player B, eligible and healthy, attempts real entry while A is active.
3. B stays outside and receives the concurrency-specific refusal.
4. B receives no `ss_in_nightmare`, no trial timer, and does not steal/reset the bossbar/creature state.
5. A remains inside with their existing trial state intact.
6. After A exits and cleanup completes, B can enter normally.

Prefer a second Mineflayer bot over adding a production-only test seam just to make this assertion easy.

---

## 2. Preserve fractional/negative return coordinates

### Why

Current entry stores `Pos[0..2]` through integer scoreboard objectives:

```mcfunction
execute store result score @s ss_ret_x run data get entity @s Pos[0]
...
```

The command result is integral, so fractional position is discarded and negative coordinates are especially visibly shifted. `ISSUES.md` already records this as a known bug.

Minecraft refuses **writes** to player NBT, but the return position only needs a **read/copy**. Numeric entity NBT can be copied directly into command storage, and function macros can consume numeric storage values. The integer-scoreboard detour is therefore unnecessary for player return coordinates.

### Preferred implementation shape

At entry, copy the real coordinates directly:

```mcfunction
data modify storage shadowslave:ret x set from entity @s Pos[0]
data modify storage shadowslave:ret y set from entity @s Pos[1]
data modify storage shadowslave:ret z set from entity @s Pos[2]
```

Then `return.mcfunction` can continue to macro-teleport using `$(x) $(y) $(z)`.

For death-drop clearance, avoid reintroducing lossy scoreboard arithmetic merely to make `y + 1`. One clean macro shape to verify is:

```mcfunction
$execute in shadowslave:nightmare as @e[type=item] in minecraft:overworld positioned $(x) $(y) $(z) run tp @s ~ ~1 ~
```

That uses the exact stored return point as execution position and moves drops one relative block above it. If Minecraft's execution-context behavior makes this unsuitable in practice, use another direct-storage solution; the invariant is **do not round the player's return coordinates just to obtain drop clearance**.

Once no active path reads them, remove or deliberately deprecate `ss_ret_x/y/z` declarations/reset calls rather than leaving fake state that looks authoritative. Existing worlds retaining old scoreboard objectives is harmless.

### Safety dependency

Direct shared `shadowslave:ret` storage is safe for Phase 1 only when the one-active-Nightmare rule above is enforced. Do these in the same reviewed release batch.

### Must NOT change

- `unstick.mcfunction` semantics;
- death timing: drops still appear after teardown and sweep remains delayed;
- the intentional "sweep all loose Nightmare items" compromise;
- player NBT must remain read-only;
- return to the same Overworld location concept.

### Acceptance criteria

Add a behavioral regression using clear space and deliberately fractional negative X/Z, for example:

```text
x = -10.75
z = -20.25
```

Prove:

1. entry captures non-integer values (query storage directly so the test cannot pass merely because later teleport rounded in the opposite direction);
2. leaving the Nightmare returns X/Z within a small tolerance of the original fractional values;
3. use clear space so `unstick` has no reason to move the player horizontally;
4. positive coordinates still work;
5. death/ejection return paths still use the same stored destination;
6. existing item-recovery human check remains valid after the sweep macro change.

Per the repo testing rule, deliberately restore the old scoreboard capture once and confirm the new fractional-position assertion fails before trusting it.

---

## 3. Strengthen the release-gate harness

Implement the high-confidence items from:

`docs/reviews/2026-07-30-harness-vacuous-assertion-audit.md`

Priority H1-H5:

- weakness gate must prove the weakness-specific refusal **and** no entry;
- Carrier cure message check becomes positive or is deleted;
- reroll numeric parsing must fail closed, never default into a passing comparison;
- cooldown re-entry must prove the cooldown-specific guard, not merely absence from the dimension;
- low-health outcome must prove Cast Out/ejection, not merely any exit.

Then use the repo rule: intentionally break each covered behavior once and verify its assertion turns red for the intended reason.

A release gate whose failure mode is "green on the wrong path" is not a gate.

---

## 4. Review GPT's build script rather than accepting it on trust

GPT added `shadowslave/tools/build.py` on `gpt/datapack-release` even though packaging/release is normally Claude's column. This was intentional to make the release requirement concrete, **not** a request to merge it unchanged.

Review or replace it. Required contract:

- validator runs first and a failure prevents output;
- version is derived from an existing authoritative version source, not duplicated;
- archive contains `pack.mcmeta` and `data/` at ZIP root, never an extra `shadowslave/` wrapper;
- development tools/testserver/docs do not ship in the datapack archive;
- output is ignored by git;
- repeated builds from identical source should preferably be byte-stable;
- build must not stamp/increment the project version; Claude still owns versioning at merge.

The Mineflayer harness remains a separate required release gate because `build.py` cannot run gameplay.

---

## 5. Merge/review sequencing

Recommended order:

1. review GPT player-facing branch changes independently of machinery;
2. implement/verify one-active-Nightmare guard;
3. implement/verify precise return storage;
4. strengthen harness assertions and prove each can fail;
5. review/replace build script;
6. run validator + full harness repeatedly;
7. build the actual ZIP;
8. Andrew performs the final human smoke test **using that ZIP**, not the loose source folder;
9. only then stamp/tag the final datapack release.

The purpose is to freeze a trustworthy Phase 1 artifact, not to squeeze Phase 2 systems into commands before Java begins.
