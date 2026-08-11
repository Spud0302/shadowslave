# Alpha assembly status

**Snapshot:** 2026-08-11  
**Current main:** `3138130eaa153e3c5996dbcc6d459788cb8d0069`  
**Assembly branch:** `gpt/ash-burrower-guard-alpha-integration`

This is the current integration checklist. Live GitHub merge state controls whenever an older status/handoff note disagrees with this snapshot.

## Playable-alpha checklist

| Alpha requirement | Current assembly state |
| --- | --- |
| Nightmare entry | Integrated on `main` |
| Nightmare completion | Integrated on `main`; durable successful-completion recovery has separate correctness evidence/blockers under issue #34 |
| Nightmare recovery | API/disk-image recovery work exists; real dedicated-server process-loss/restart convergence remains unproven under issue #34 |
| Scenario selection | Integrated on `main` |
| Generated identity award | Integrated on `main` |
| Memory ownership/use | Integrated on `main`; current Ash Compass consolidation remains a separate review edge |
| Echo ownership/summoning | Integrated on `main`; #244 is the current bounded GUARD_POINT execution edge |
| At least one real creature | Integrated on `main`; Ash Burrower hostile execution uses SmartBrainLib while Java retains creature/VIBRATION authority |
| Dream Realm vertical slice | Integrated on `main` |
| UI/presentation | Integrated baseline on `main`, including GeckoLib creature/Echo presentation and merged gameplay keybinds |
| Client/server packaging | Integrated baseline on `main`; SmartBrainLib packaging/provenance passed before #242 merged |

## Current bounded assembly edge

#244 selectively ports the reviewed #209 guard-point authority and #216 bounded guard-threat execution onto the modern hostile-Ash-Burrower/GeckoLib baseline rather than carrying their stale ancestry.

- `EchoInstanceData` persists guard dimension/position independently of manifestation location.
- `/shadowslave_echo guard ash_burrower` records that Java-owned target.
- the dedicated Ash Burrower Echo returns to the target and may intercept only the already-registered Ash Burrower, Chainback, and Drowned Listener hostile executors within eight blocks of the guard anchor.
- the owned Echo receives bounded melee execution/animation; it does **not** adopt SmartBrainLib.
- the hostile Ash Burrower remains the merged SmartBrainLib executor from #242.
- CARRY/cargo from #211/#214 is deliberately not imported into this bounded integration.

## Evidence and supersession state

Preview Gates #257 / Actions `31463437162` passed on #244 exact head `aad0a3fd50d709a37e881f8fb580c6321e7301a0`: compile/all unit tests/package, physical NeoForge client, same-world dedicated-server restart, development JAR upload, frozen-datapack build/validation, and deployed vanilla harness all passed.

After that gate, `main` advanced from merge base `64abb2cfc1e1e718fe7d3e8ce6393b5225fc5c62` to `3138130eaa153e3c5996dbcc6d459788cb8d0069` through the player-gameplay-keybind integration. The 13 intervening commits touch keybind/network/language/test files and none of #244's ten files, so there is no observed file conflict with the guard slice. Fresh CI is still required for any new #244 head after documentation changes.

- #209 is closed unmerged as demonstrably superseded by #244's equivalent-or-stronger persistent GUARD_POINT authority/runtime.
- #216 is closed unmerged as demonstrably superseded by #244's equivalent-or-stronger bounded guard combat plus modern hostile-Ash-Burrower separation regression.
- #214 remains open because its CARRY/cargo delta is deliberately outside #244 and has not been superseded by this guard-only integration.

## Known blockers preserved

- **Issue #34:** real NeoForge dedicated-server successful-completion stop/kill + restart convergence remains unproven; do not claim process-loss durability from API/JVM reconstruction alone.
- **Issue #20:** the frozen datapack retains the documented global-selector architecture limitation; do not reinterpret unrelated Mineflayer setup timeouts as a Java gameplay regression.
- Prepared-world physical durability work remains evidence-blocked under its existing recorded resume condition; do not invent physical-test evidence.

## Next assembly order

1. Treat #244 as the sole guard-point/guard-combat review edge; do not reopen #209/#216 unless a demonstrated regression requires their historical evidence.
2. Evaluate only the CARRY/cargo delta from #211/#214 for a clean port onto current `main` rather than rebasing their obsolete presentation ancestry.
3. Return to issue #34's real dedicated-server restart boundary; if still physically blocked, preserve the blocker and integrate another already-reviewed green edge rather than adding content.
