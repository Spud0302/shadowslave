# Ash Burrower gameplay executor integration — 2026-08-11

## Scope

This integration combines the existing Java-owned Ash Burrower command/cargo state with the existing dedicated GeckoLib Ash Burrower Echo physical executor. It adds no new Echo identity, command family, reward, progression rule, catalogue, or dependency.

## Sources checked

Repository policy/status re-read before implementation: `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md`, plus active correctness/integration PRs.

Primary Chapter 47 (`Echo`) was rechecked against official WebNovel. It establishes that an Echo can be summoned and used to fight, carry heavy cargo, and perform other tasks. Chapter 370 (`Exploration Report`) was rechecked through the owner-designated NovelFull access layer for the project's general rule that Nightmare Creatures can have creature-specific powers, behavior and weaknesses. Neither source establishes this project's exact Ash Burrower anatomy, exact guard vocabulary, cargo capacity, navigation distances, or Minecraft command syntax.

## Evidence classification

- **CANON:** Echoes are summonable and can be used for combat, carrying heavy cargo and other practical tasks. Nightmare Creatures can have creature-specific powers, behavior and weaknesses.
- **INFERRED:** persistent Java ownership can keep a task/cargo association independently from the replaceable physical manifestation; a creature-derived Echo can use a recognizable executor while authority remains in Java state.
- **DESIGN:** Ash Burrower identity, `FOLLOW` / `HOLD` / `GUARD_POINT` / `CARRY` command vocabulary, one plain-stack cargo capacity, exact command syntax, guard anchor, navigation speed/radii, GeckoLib geometry/texture/animations, and unload-as-item-entity behavior.
- **UNKNOWN:** canonical command vocabulary/interface, arbitrary cargo capacity/component preservation, autonomous guard threat acquisition, cross-realm hauling, destruction/recovery semantics, and final Ash Burrower anatomy/presentation.
- **COMPATIBILITY:** `EchoInstanceData` owns command target, cargo and manifestation references. NeoForge entity navigation, inventory transfer, item spawning and GeckoLib rendering are replaceable executors/presentation and cannot create progression or ownership.

## Integration boundary

The dedicated `AshBurrowerEchoEntity` now executes the same persisted command/cargo state that previously existed on the runtime branch. The integration deliberately rejects component-bearing item stacks rather than flattening unsupported item data. Older Echo saves remain compatible because command-target and cargo fields are optional.
