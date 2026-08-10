# Ash Burrower CARRY runtime evidence — 2026-08-11

## Scope

This slice executes the already-authored `ash_burrower` Echo profile's `CARRY` command. It does not add another Echo identity, command family, reward rule, or progression system.

## Primary evidence checked

Chapter 47, **Echo**, was rechecked through the owner-designated NovelFull index/access layer and official WebNovel chapter text/identity. The chapter establishes that an Echo can be summoned and used for practical tasks, explicitly including carrying heavy cargo, while remaining an Echo rather than a living creature.

No implementation rule in this slice depends on later material overriding that baseline.

## Classification

- **CANON:** Echoes can be summoned, commanded, and used for practical work including carrying heavy cargo.
- **INFERRED:** cargo entrusted to an Echo can remain associated with that Echo while it is dismissed/re-summoned, provided project persistence owns the association rather than deriving it from a temporary entity.
- **DESIGN:** Ash Burrower identity; one-stack cargo capacity; `/shadowslave_echo carry ash_burrower` and `unload`; four-block handoff radius; CARRY following the owner while loaded; plain-stack-only restriction; dropping unloaded cargo beside the manifestation; exact messages and movement speed.
- **UNKNOWN:** canonical cargo capacity, inventory semantics, whether arbitrary item enchantments/components survive Echo storage, exact command vocabulary/interface, cross-realm cargo behavior, destruction/recovery behavior, and whether dismissal canonically preserves a physical load in every case.
- **COMPATIBILITY:** `EchoInstanceData` owns cargo identity/count. Minecraft inventory/entity state only executes transfer at the load/unload boundary. Custom-data stacks fail closed rather than being flattened into an incomplete Java cargo record.

## Runtime boundary

The executor intentionally accepts only stacks with an empty data-component patch. This first slice stores registry item identity plus count, so accepting named, enchanted, damaged, container-bearing, or otherwise component-bearing stacks could silently destroy metadata. A future broader cargo executor must persist the complete item representation before relaxing that guard.

Loading requires the owned Echo to be manifested and within four blocks. The entire main-hand plain stack moves into Java-owned cargo state and the Echo enters CARRY mode, which currently follows the owner. Unloading requires the same proximity and materializes the stored plain stack as an item entity beside the Echo; Java cargo is cleared only after the entity spawn succeeds.

No Nightmare resolution, appraisal, Memory, creature reward, Soul state, or catalogue content changes in this slice.
