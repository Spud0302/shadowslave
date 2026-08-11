# SmartBrainLib admission — Ash Burrower AI execution

**Date:** 2026-08-11  
**Candidate:** SmartBrainLib `1.16.11` for Minecraft `1.21.1` / NeoForge  
**Scope:** replace a meaningful cluster of generic Ash Burrower AI execution, not a single one-off goal.

## Why this dependency earns a spike

The merged Ash Burrower executor had already grown a hybrid AI stack:

- vanilla `MeleeAttackGoal`, `RandomStrollGoal`, and `HurtByTargetGoal`;
- a custom four-tick nearby-player scan;
- custom previous-position bookkeeping for vibration detection;
- custom pursuit countdown/target ownership;
- direct navigation calls in `tick()`.

Recent bounded creature PRs correctly rejected SmartBrainLib for isolated cooldowns or one-shot effects because native code was smaller. This slice uses a stricter admission threshold: SmartBrainLib must replace the generic sensing/activity/path/combat scheduling cluster while the project-owned vibration rule remains independent.

## Upstream/version evidence

The upstream `Tslat/SmartBrainLib` `1.21` branch declares:

- project version `1.16.11`;
- Java `21`;
- Minecraft `1.21.1`;
- NeoForge `21.1.191` with range `[21.1.0,)`;
- mod id `smartbrainlib`;
- MPL-2.0 licensing;
- Maven repository `https://maven.cloudsmith.io/tslat/sbl/`.

The published NeoForge artifact is `net.tslat.smartbrainlib:SmartBrainLib-neoforge-1.21.1:1.16.11`, file `SmartBrainLib-neoforge-1.21.1-1.16.11.jar`. CurseForge file `7055149` also identifies that exact file as a NeoForge Minecraft 1.21.1 release.

The repository itself is pinned to NeoForge `21.1.244`, which satisfies the upstream `21.1.x` floor. Exact runtime compatibility is proven by this repository's physical client/server gates rather than inferred from version ranges alone.

## Authority boundary

- **CANON:** unchanged. No Shadow Slave lore rule is introduced by an AI library.
- **INFERRED:** unchanged. A physical creature executor may use generic scheduling infrastructure without transferring creature identity or progression authority.
- **DESIGN:** SmartBrainLib is selected as a replaceable AI execution provider for this bounded Ash Burrower executor.
- **UNKNOWN:** mature Ash Burrower AI, final sensing semantics, final locomotion/combat tuning, and whether later creatures should use the same library.
- **COMPATIBILITY:** Java-owned `NightmareCreatureContentCatalog` and `AshBurrowerVibrationBehavior` remain project authority. SmartBrainLib may schedule sensors, activities, walk targets and melee execution only. It cannot award loot, alter Soul/progression, determine Rank/Class, resolve Nightmares, own Echo/Memory state, or write canonical identity.

## Implementation boundary

SmartBrainLib takes over:

- the nearby-player sensor cadence;
- retaliation memory/execution;
- idle/fight/core activity scheduling;
- random walk/look scheduling;
- attack-target walk-target generation;
- ordinary melee scheduling.

The project retains:

- `AshBurrowerVibrationBehavior.detects(...)`;
- sampled-displacement state needed by that rule;
- the bounded pursuit window and release rule;
- player exclusions (dead/spectator/creative);
- zero-XP/no-loot reward boundary;
- the ban on Silverfish merge-with-stone/wake-friends behavior.

Only `FloatGoal` remains as a small vanilla water-safety goal; the old melee/stroll/retaliation goals and direct `tick()` navigation loop are removed.

## Packaging admission evidence

SmartBrainLib is a required runtime component only with this integration and is present in:

1. Gradle dependency resolution;
2. generated NeoForge mod dependency metadata;
3. the deterministic modpack manifest;
4. hash-verified packaging CI;
5. physical client and dedicated-server gates.

The first admission run intentionally downloaded the exact upstream NeoForge artifact, printed its digest, and failed against a temporary zero sentinel. It measured:

`68036561cc5511766d54cc0deabc3fc3a5e68f9e3db2478f2574ec82b494374b`

That exact SHA-256 is now pinned in both `modpack/manifest.json` and the Modpack shell workflow. Corrected Modpack shell run `31461720694` / run #23 passed with both GeckoLib and SmartBrainLib hash-verified and the deterministic multi-component archive validated.

The initial SmartBrainLib code head also passed Preview Gates run `31461543919` / run #250: compile/all unit tests/package, physical NeoForge client boot, same-world dedicated-server restart, development JAR upload, frozen-datapack validation and deployed vanilla harnesses. A fresh corrected-head Preview Gates run remains the final merge evidence after documentation/hash corrections.

## Removal path

If the integration proves materially more complex, unstable, or less maintainable than the native implementation, remove the SmartBrainLib dependency and port the bounded executor back to project/vanilla scheduling. No save migration should be required because the library owns no canonical persisted state.
