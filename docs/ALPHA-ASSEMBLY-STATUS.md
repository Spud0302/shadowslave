# Alpha assembly status

**Assembly baseline:** `main@2e0097e8d1f21f3cadf89f3d8931fef94d2d6b94`

This file tracks integration state, not historical preview status. Live GitHub merge/CI state remains authoritative over stale root preview-era summaries.

## Current bounded integration lanes

- **Dream Realm world slice:** PR #275, `gpt/drowned-bell-native-quarry-cut`, now targets current `main` directly. It cumulatively contains the #248 -> #249 -> #251 -> #258 -> #261 -> #265 -> #272/#273 lineage. #249/#251/#258/#261/#265/#272/#273 are already closed unmerged after containment proof. #248 remains open until the direct-to-main review context executes green and final containment is confirmed.
- **Echo gameplay:** PR #276, current-main Ash Burrower GUARD + CARRY integration candidate. Historical cargo/combined edges were retired only after hosted-green containment proof.
- **Nightmare recovery:** PR #278, current-main recovery consolidation. FakePlayer evidence is one-process/server-side only; Issue #34 remains open for genuine process/network reconnect evidence. Do not retire #259/#268 until #278 executes hosted CI successfully and containment is rechecked.
- **Combat dependency experiment:** PR #277 remains a physical-admission spike. Do not promote Better Combat into release packaging without interactive evidence.

## Alpha checklist

- [x] Nightmare entry represented on merged main
- [x] Nightmare completion represented on merged main
- [ ] Nightmare successful-completion recovery across a genuine process/network reconnect — Issue #34
- [x] Scenario selection represented on merged main
- [x] Generated identity award represented on merged main
- [x] Memory/Echo ownership baseline represented on merged main/current integration lane
- [x] At least one real creature represented on merged main
- [x] Dream Realm vertical slice represented on merged main; PR #275 is the current cumulative enhancement candidate
- [x] UI/presentation baseline represented on merged main
- [x] Client/server packaging baseline represented on merged main

## Current world-integration evidence

PR #275 exact source head `e7ebf4cecb4d0d2048b889e8ce54d182f89e6b96` passed Preview Gates #299 / Actions `31492576048` before direct-to-main retargeting.

Current main diverged from the world head at merge base `3138130eaa153e3c5996dbcc6d459788cb8d0069`. The 20 current-main-only commits change combat-design documentation and recovery research/tests only; none overlap #275's world/history/appraisal/native-worldgen paths. GitHub currently reports #275 mergeable against exact current main.

Fresh pull-request-context hosted CI is still required after this status commit before #248 can be retired as superseded.

## Remaining high-impact merge dependencies

1. #275 direct-to-main hosted validation, then final #248 containment decision.
2. #278 hosted execution before #259/#268 retirement; Issue #34 remains stronger than FakePlayer proof.
3. Keep Issue #20 separate as the frozen-datapack global-selector architecture limitation.
4. Avoid expanding standalone Memory/faction/world catalogues while these integration edges remain open.

## Next assembly step

Inspect the exact-head Preview Gates for this branch. If green, compare #248's identity/divergence contracts against the current #275 head and close #248 unmerged only if equivalent-or-stronger behavior is demonstrably retained. If CI exposes a real current-main conflict, repair it on this integration branch instead of reopening retired world-stack dependencies.
