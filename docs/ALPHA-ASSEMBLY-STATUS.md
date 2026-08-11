# Alpha assembly status

**Snapshot:** 2026-08-11  
**Current main:** `64abb2cfc1e1e718fe7d3e8ce6393b5225fc5c62`  
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
| Echo ownership/summoning | Integrated on `main` |
| At least one real creature | Integrated on `main`; Ash Burrower hostile execution now uses SmartBrainLib while Java retains creature/VIBRATION authority |
| Dream Realm vertical slice | Integrated on `main` |
| UI/presentation | Integrated baseline on `main`, including GeckoLib creature/Echo presentation |
| Client/server packaging | Integrated baseline on `main`; SmartBrainLib packaging/provenance passed before #242 merged |

## Current bounded assembly edge

This branch selectively ports the reviewed #209 guard-point authority and the bounded #216 guard-threat execution onto current `main`, rather than retargeting the stale #214/#216 ancestry.

- `EchoInstanceData` persists guard dimension/position independently of the manifestation location.
- `/shadowslave_echo guard ash_burrower` records that Java-owned target.
- the existing dedicated Ash Burrower Echo returns to the target and may intercept only the already-registered Ash Burrower, Chainback, and Drowned Listener hostile executors within eight blocks of the guard anchor.
- the owned Echo receives bounded melee execution/animation; it does **not** adopt SmartBrainLib.
- the hostile Ash Burrower remains the merged SmartBrainLib executor from #242. This branch does not revert or fork its AI authority.
- CARRY/cargo from #211/#214 is deliberately not imported into this bounded integration; it can be evaluated separately after this current-main edge is green.

## Supersession policy for this edge

Do not close #209, #214, or #216 merely because this branch is newer. They may be marked superseded only after exact-head hosted CI passes and comparison/file/test evidence shows the reviewed guard behavior is equivalent or stronger here. #214 also contains cargo behavior that this branch deliberately omits, so #214 cannot be wholly superseded by this guard-only integration.

## Known blockers preserved

- **Issue #34:** real NeoForge dedicated-server successful-completion stop/kill + restart convergence remains unproven; do not claim process-loss durability from API/JVM reconstruction alone.
- **Issue #20:** the frozen datapack retains the documented global-selector architecture limitation; do not reinterpret unrelated Mineflayer setup timeouts as a Java gameplay regression.
- Prepared-world physical durability work remains evidence-blocked under its existing recorded resume condition; do not invent physical-test evidence.

## Next assembly order

1. Obtain exact-head Preview Gates for this guard integration branch.
2. If green, prove which historical guard branches are fully contained before closing any of them.
3. Return to issue #34's real dedicated-server restart boundary; if still physically blocked, preserve the blocker and integrate another already-reviewed green edge rather than adding content.
