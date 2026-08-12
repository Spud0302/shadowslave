# Alpha assembly status

**Assembly baseline:** `main@4b17902603abe46803fc672d43700fec64ded110`

This file tracks integration state, not historical preview status. Live GitHub merge/CI state remains authoritative over stale root preview-era summaries.

## Current bounded integration lanes

- **Dream Realm world slice:** PR #275, `gpt/drowned-bell-native-quarry-cut`, now contains exact current `main@4b17902603abe46803fc672d43700fec64ded110` through integration helper #280. The integration retains both `DreamRealmWorldgenFeatures.register(modEventBus)` and Glass Road's `GlassRoadMemoryItem::onPlayerTick`. Historical world edges #249/#251/#258/#261/#265/#272/#273 remain retired only after containment proof; #248 stays open until the current-main cumulative head receives executed hosted validation and final containment is reconfirmed.
- **Echo gameplay:** PR #276, `gpt/ash-burrower-guard-current-main`, is rooted on current main and remains the Ash Burrower GUARD + CARRY integration candidate.
- **Gameplay interactions:** PR #279, `gpt/gameplay-alpha-current-main`, is rooted on current main and consolidates reviewed Ash Compass, Chainback, Drowned Listener and Stonewake interaction slices while preserving merged Glass Road APIs.
- **Nightmare recovery:** PR #278, `gpt/recovery-alpha-current-main`, is rooted on current main. FakePlayer evidence is one-process/server-side only; Issue #34 remains open for genuine process/network reconnect evidence. The mature durability lineage ending at #178 remains separate until deliberately reconciled.
- **Combat dependency experiment:** PR #277 remains physical-testing-gated. Do not promote Better Combat into release packaging without interactive evidence.

## Alpha checklist

- [x] Nightmare entry represented on merged main
- [x] Nightmare completion represented on merged main
- [ ] Nightmare successful-completion recovery across a genuine process/network reconnect — Issue #34
- [x] Scenario selection represented on merged main
- [x] Generated identity award represented on merged main
- [x] Memory/Echo ownership baseline represented on merged/current integration lineage
- [x] At least one real creature represented on merged main
- [x] Dream Realm vertical slice represented on merged main; PR #275 is the cumulative current-main enhancement candidate
- [x] UI/presentation baseline represented on merged main
- [x] Client/server packaging baseline represented on merged main

## Current world-integration evidence

The pre-Glass-Road cumulative world tree passed Preview Gates #299 / Actions `31492576048` on exact functional head `e7ebf4cecb4d0d2048b889e8ce54d182f89e6b96`.

Integration helper #280 merged exact `main@4b17902603abe46803fc672d43700fec64ded110` into the existing world branch as merge commit `05b9c1ab609f5377cc85b71b01dc62ea74558994`. The only overlapping runtime file was `ShadowSlaveMod.java`; the integrated tree keeps Dream Realm worldgen registration and Glass Road's player-tick hook.

Preview Gates #403 / Actions `31613932477` triggered on `05b9c1ab...`, but both Java and datapack jobs terminated before executing repository steps (`steps=[]`, `runner_id=0`, no runner name). Treat that as hosted-runner allocation evidence, not a source/test/runtime failure. Do not retry unchanged heads merely to chase allocation.

## Remaining high-impact merge dependencies

1. #275 needs an actually executed current-main hosted run before #248 can be retired.
2. #279 needs executed hosted validation before #236/#256/#260/#264 can be retired.
3. #278 needs executed hosted validation before #259/#268 can be retired; Issue #34 still requires genuine process/network-player recovery beyond FakePlayer.
4. The mature durability lineage ending at #178 still needs deliberate API-level consolidation into #278 rather than a wholesale divergent-history merge.
5. Keep Issue #20 separate as the frozen-datapack global-selector architecture limitation.

## Next assembly step

Do not retry #275 unchanged while hosted runners remain unavailable. The next highest-impact recovery assembly is to port the smallest coherent durable successful-completion core from the mature #178 lineage into current-main #278, resolving API divergence explicitly and preserving the network-reconnect evidence boundary.
