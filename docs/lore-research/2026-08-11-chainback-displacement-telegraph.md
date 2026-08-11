# Chainback displacement telegraph evidence

**Date:** 2026-08-11  
**Parent:** PR #235 / `gpt/chainback-displacement-runtime`  
**Slice:** readable pre-pull presentation for the existing Java-authored `chainback` / `DISPLACEMENT` executor

## Integration position

Current `main`, live open PRs/issues, `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and `docs/THIRD-PARTY-DEPENDENCY-POLICY.md` were checked before implementation. The status/handoff/issues snapshots are materially older than the current integration state, so live GitHub ownership controls overlap.

PR #235 exclusively owns Chainback displacement execution and explicitly names readable displacement presentation as its next slice. No other active branch owns the pre-pull telegraph, so this branch stacks directly on #235 rather than duplicating another creature, Memory, Echo, Cinder Rest, or persistence lane.

The third-party dependency policy is merged on `main`; PR #200 fallback review is therefore unnecessary.

## Primary lore check

Chapter 370, **Exploration Report**, was rechecked through the owner-designated NovelFull chapter access and official WebNovel publication. It supports the broad proposition that individual Nightmare Creatures can have practically meaningful powers, behavior, and weaknesses. It does not establish Chainback, literal chains, this pull, or any warning cue.

## Classification

- **CANON:** individual Nightmare Creatures can have meaningful creature-specific powers, behavior, and weaknesses worth learning.
- **INFERRED:** an already-authored dangerous creature action benefits from readable physical counterplay rather than firing invisibly on the same tick it becomes eligible.
- **DESIGN:** Chainback; a 12-tick windup; pulses every three ticks; vanilla chain-place sound; short CRIT-particle line between target and creature; cancellation if target/range/line-of-sight eligibility is lost before release; exact visual/audio intensity and timing.
- **UNKNOWN:** canonical Chainback existence, exact displacement mechanism, whether any canonical warning exists, chain material, cue timing, sound, particles, anchor/sever behavior, and `cut_anchor` semantics.
- **COMPATIBILITY:** `NightmareCreatureContentCatalog` remains creature/pressure identity authority. NeoForge particle/audio calls and the windup timing are removable execution/presentation and cannot own targeting, Rank/Class, rewards, progression, Nightmare resolution, or persistence.

## Runtime design

The parent pull no longer fires immediately when displacement becomes eligible. Instead, eligibility begins a bounded 12-tick telegraph. The cue emits a chain sound once and a short line of vanilla CRIT particles at a three-tick cadence. The target must still be alive, in range, inside the vertical band, and in line of sight when the windup completes; otherwise the telegraph cancels and no displacement occurs.

The pull magnitude, lift, four-block range, 1.5-block vertical band, and 50-tick cooldown remain exactly the parent #235 DESIGN values.

## Dependency / asset decision

No dependency is added. SmartBrainLib still provides no benefit for a simple existing-target windup/cancellation policy. GeckoLib remains the approved presentation library already present in the broader creature lineage, but this bounded cue does not require another controller/API surface to become readable.

No third-party assets are copied. The chain sound and CRIT particles are bundled vanilla placeholders. They are intentionally removable when a project-owned snag animation/audio/VFX treatment is available.

## Tests and evidence boundary

`ChainbackDisplacementBehaviorTest` now pins the 12-tick warning duration and three-tick pulse cadence in addition to the parent pull bounds.

Exact-head Preview Gates remain required for compile/unit/package, physical NeoForge client boot, dedicated-server boot, and frozen-datapack compatibility. Automated startup cannot prove human readability/feel of the cue.

## Still placeholder

- Spider-derived navigation, climbing, targeting, melee, and sounds remain placeholders;
- the current warning uses vanilla chain audio and CRIT particles rather than project-owned animation/VFX/audio;
- no physical chain/rope, anchor entity, severing interaction, `cut_anchor`, HUD/camera effect, or terrain grapple exists;
- exact timing and cue strength remain replaceable DESIGN.

## Next slice

After this branch is green/review-clean, the strongest Chainback presentation follow-up is to replace the vanilla telegraph materials with a project-owned GeckoLib snag/chain animation and matching project audio/VFX while preserving this Java-owned eligibility/cancellation boundary. If the active Chainback texture/presentation lineage lands first, port the telegraph onto that current-main integration rather than maintaining parallel creature stacks.
