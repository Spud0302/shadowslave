# Chainback displacement runtime evidence

**Date:** 2026-08-11  
**Baseline:** `main@955bd16ce905590c5e72a21e55a224616207adb6`  
**Slice:** physical execution of the existing Java-authored `chainback` / `DISPLACEMENT` creature descriptor

## Integration position

Live GitHub state, open issues/PRs, `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and `docs/THIRD-PARTY-DEPENDENCY-POLICY.md` were checked before implementation. The root status/handoff/issues files remain materially older than the current alpha integration state, so live GitHub ownership controls overlap decisions.

Active review work already owns Ash Burrower Echo behavior, Drowned Listener sensing/presentation, Ash Compass runtime/presentation, Cinder Rest/Watch Captain presentation, multiple Memory runtimes, and Nightmare restart correctness. No active branch owns Chainback's authored `DISPLACEMENT` pressure, so this slice changes only that unclaimed executor edge.

The merged third-party dependency policy is present on `main`; the historical PR #200 fallback therefore does not need to be consulted as an unmerged policy source.

## Java-owned source identity

`NightmareCreatureContentCatalog` already defines Chainback as an authored DESIGN creature with:

- `VISION` + `SCENT` senses;
- `GROUND` + `CLIMB` locomotion;
- `PURSUIT` + `DISPLACEMENT` pressures;
- `narrow_gap`, `cut_anchor`, and `vertical_escape` counterplay tags;
- the presentation cue that loose iron lengths drag from its shell and snag what they pass.

This slice does not create or reinterpret those identities. It only makes one existing descriptor observable in Minecraft.

## Lore evidence

Primary Chapter 370, **Exploration Report**, was rechecked under `docs/LORE-SOURCE-POLICY.md` through the owner-designated full-chapter access layer and cross-checked against the official WebNovel publication. The chapter supports the broad proposition that individual Nightmare Creatures can have practically meaningful powers, behavior, and weaknesses. It does not establish this project's Chainback, chains, anatomy, rank/class, or a displacement formula.

### Classification

- **CANON:** individual Nightmare Creatures can have meaningful creature-specific powers, behavior, and weaknesses worth learning.
- **INFERRED:** when Java already authors a creature-specific `DISPLACEMENT` pressure, its physical executor should expose observable displacement/counterplay rather than leaving the creature entirely on unrelated generic Spider melee behavior.
- **DESIGN:** Chainback itself; dragging/snagging iron presentation; pulling the current hostile target toward the Chainback; four-block horizontal range; 1.5-block vertical band; line-of-sight requirement; 50-tick cooldown; 0.55 horizontal pull magnitude; 0.10 upward lift; and use of the current vanilla hostile target rather than a new supernatural targeting authority.
- **UNKNOWN:** canonical Chainback existence, anatomy, material, chain placement, rank/class, exact displacement mechanism, whether displacement is a pull/push/grapple/terrain effect, range, force, cadence, occlusion rules, exact `cut_anchor` semantics, and mature counterplay.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains creature identity/descriptor authority. NeoForge entity targeting, line-of-sight sampling, `push(...)`, Spider navigation/melee, GeckoLib rendering, exact tuning, and all presentation remain removable execution/presentation only and cannot own progression, rewards, Nightmare resolution, persistence, or canonical creature identity.

## Runtime design

`ChainbackDisplacementBehavior` is a pure Java policy with explicit bounded constants and fail-closed handling for malformed/non-finite inputs. `ChainbackEntity` executes it only server-side, only against the entity's existing live hostile target, and only when vanilla sensing has line of sight. The target receives one pull toward Chainback, then the executor waits for the bounded cooldown before another displacement can fire.

The pure policy is deliberately separate from the Minecraft entity so exact tuning can be unit-tested and later replaced without migrating creature identity or persistent state.

## Dependency decision

No dependency is added.

SmartBrainLib was considered because the active policy allows a spike once vanilla goals/sensors become the limiting factor. This behavior does not meet that threshold: one existing target, a line-of-sight gate, a bounded cooldown, and a motion impulse are smaller and clearer with native Minecraft/NeoForge execution. Adding SmartBrainLib here would increase required runtime/packaging surface without replacing meaningful solved work.

GeckoLib 4.9.2 remains presentation-only on the existing creature integration line. Curios, Veil, TerraBlender, and SmartBrainLib remain unadopted by this slice.

No third-party models, textures, sounds, structures, or other assets are copied.

## Tests and physical evidence boundary

`ChainbackDisplacementBehaviorTest` pins:

- inclusive four-block horizontal range;
- inclusive 1.5-block vertical band;
- cooldown gating;
- fail-closed zero/non-finite inputs;
- pull direction and exact bounded magnitude/lift;
- explicit tuning constants.

The repository's Preview Gates are required on the exact PR head for compile/unit/package, NeoForge physical client boot, dedicated-server boot, and frozen-datapack compatibility. Those gates prove integration/startup compatibility, not the final feel/readability of the pull in a human play session.

## Still bounded / placeholder

- Spider-derived climbing, navigation, target selection, melee, and sounds remain placeholders;
- no chain collision/rope simulation, anchor entity, severable chain, or `cut_anchor` executor exists;
- no bespoke snag animation, particles, audio, HUD telegraph, or camera treatment is added;
- the current pull only affects the existing hostile target, not arbitrary nearby entities;
- exact displacement tuning remains replaceable DESIGN.

## Next physical slice

After this executor is green/review-clean, the highest-impact Chainback follow-up is a readable displacement telegraph/counterplay layer: a short project-owned chain/snare animation or VFX cue tied to the Java-authorized pull, while keeping `cut_anchor` unimplemented until there is an explicit physical anchor model worth severing. If another branch claims that presentation lane first, move to the next unclaimed world-facing descriptor rather than duplicate it.
