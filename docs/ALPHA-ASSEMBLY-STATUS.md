# Alpha assembly status

**Snapshot:** 2026-08-12  
**Current main:** `4b17902603abe46803fc672d43700fec64ded110`  
**Assembly branch:** `gpt/ash-burrower-guard-current-main` / PR #276

Live GitHub merge state controls whenever an older status/handoff note disagrees with this snapshot.

## Playable-alpha checklist

| Alpha requirement | Current assembly state |
| --- | --- |
| Nightmare entry | Integrated on `main` |
| Nightmare completion | Integrated on `main`; successful-completion recovery remains separately tracked under issue #34 |
| Nightmare recovery | Current-main PR #278 contains contradictory-cut, GameTest, and one-process FakePlayer evidence; genuine successful-player recovery across a dedicated-server process boundary remains the explicit issue #34 gap |
| Scenario selection | Integrated on `main` |
| Generated identity award | Integrated on `main` |
| Memory ownership/use | Integrated on `main`; Glass Road is now merged and additional Memory executors remain separate review edges |
| Echo ownership/summoning | Integrated on `main`; PR #276 is the current Ash Burrower GUARD + CARRY execution edge and is now re-rooted on current main |
| At least one real creature | Integrated on `main`; hostile Ash Burrower uses SmartBrainLib while the owned Echo remains a separate Java command executor |
| Dream Realm vertical slice | Integrated on `main`; cumulative native-worldgen expansion remains PR #275 |
| UI/presentation | Integrated baseline on `main`, including GeckoLib creature/Echo presentation and gameplay keybinds |
| Client/server packaging | Integrated baseline on `main`; #276's pre-re-root functional head passed hosted Preview Gates #331 and the new exact head awaits hosted execution |

## Current bounded assembly edges

PR #276 has been re-rooted directly onto `main@4b17902603abe46803fc672d43700fec64ded110` after merged Glass Road PR #270. The Glass Road delta and #276 Echo/cargo/test delta have no overlapping paths, so the re-root preserves the newer Glass Road Memory APIs unchanged while retaining the reviewed Echo implementation.

The resulting Ash Burrower Echo candidate keeps one coherent authority:

- `EchoInstanceData` persists GUARD_POINT dimension/position and one plain cargo stack independently of manifestation state;
- `/shadowslave_echo guard ash_burrower` records the guard anchor;
- the dedicated GeckoLib Ash Burrower Echo returns to that anchor and may intercept only the registered Ash Burrower, Chainback, and Drowned Listener threats inside the bounded guard area;
- `/shadowslave_echo carry ash_burrower` transfers one nearby plain main-hand stack into persistent Java-owned cargo and switches to CARRY follow behavior;
- `/shadowslave_echo unload ash_burrower` materializes that stack beside the manifestation and clears Java cargo only after entity spawn succeeds;
- dismissal preserves cargo because manifestation identity and cargo ownership are separate fields;
- custom-component stacks fail closed rather than losing item metadata;
- the hostile Ash Burrower remains the merged SmartBrainLib executor and is not replaced by owned-Echo command AI.

Current alpha integration lanes are #275 world, #276 Echo, #278 recovery, and #279 gameplay. #275 still needs explicit reconciliation with current main. #278 and #279 are already re-rooted on `main@4b179026...`. Better Combat #277 remains physical-testing-gated and is not alpha authority.

No new Echo identity, creature, Nightmare content, reward rule, progression rule, or standalone catalogue item is introduced by this consolidation.

## Evidence and supersession state

Preview Gates #322 / Actions `31509836660` passed on exact combined #276 gameplay/test head `18d5d7925bce202d0d84b414c91cb973e948a743`. Preview Gates #327 / Actions `31514539362`, #329 / Actions `31519989064`, and #331 / Actions `31525037363` also passed on subsequent #276 heads through `82c4f0fb7434df9c9d0391563c18c382721d9fef`.

The current-main re-root preserves the reviewed #276 functional blobs while taking `4b179026...` as its sole parent; hosted validation for the new exact head must execute before the re-root itself is called green.

- #244 is closed unmerged as demonstrably superseded by #276's guard integration.
- #209 and #216 were previously closed unmerged as demonstrably superseded by the modern guard integration.
- #262 is closed unmerged as demonstrably superseded by the same reviewed transport correction carried inside #276.
- #211 is closed unmerged after targeted cargo behavior/test comparison proved #276 retains the reviewed cargo authority and `EchoCargoStateTest` contracts.
- #214 is closed unmerged after targeted combined-executor comparison proved #276 retains Java-owned GUARD_POINT/CARRY state while strengthening GUARD into bounded registered-threat interception.
- #170 is closed unmerged because #276 carries equivalent-or-stronger test transport behavior and stronger direct-entrypoint regression coverage.

## Known blockers preserved

- **Issue #34:** genuine successful-Nightmare player recovery across a NeoForge dedicated-server process boundary remains the correctness evidence gap; do not substitute codec/FakePlayer/disk-image reconstruction for that stronger claim.
- **Issue #20:** the frozen datapack retains its documented global-selector architecture limitation.
- Hosted CI has recently failed to allocate runners (`steps=[]`, `runner_id=0`) on several integration heads. Do not reinterpret that infrastructure failure as source/test evidence, and do not retry unchanged heads after the recorded stop condition.
- Interactive/physical evidence blockers keep their recorded resume conditions; integration work should continue around them instead of inventing evidence.

## Next assembly order

1. Inspect the new exact #276 current-main head once hosted runners execute; if source tests fail, resolve the concrete failure on this branch rather than recreating historical Echo stacks.
2. Re-root #275 world onto `main@4b179026...` if its current-main conflict surface remains bounded.
3. Continue reconciling the mature durability lineage into #278 without mislabeling FakePlayer evidence as a real two-JVM reconnect.
