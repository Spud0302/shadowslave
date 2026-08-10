# Ash Burrower GUARD_POINT threat execution — evidence note

**Scope:** execute the already-authored Ash Burrower Echo `GUARD_POINT` order as a bounded defensive combat task. This note does not create a new Echo, creature, command catalogue, reward rule, or progression mechanic.

## Sources checked

- **Chapter 47 — `Echo`**: the early Echo explanation establishes that an owned Echo can be summoned, given commands, used to fight enemies, carry heavy cargo, and perform other practical tasks. The same chapter treats an Echo as a soulless replica rather than an ordinary living companion.
- **Chapter 1826 — `Coming Storm`**: later material remains compatible with an Echo being considered as a summoned protector in combat, so the early practical-combat reading is not treated as an isolated tutorial-only implication.
- Official WebNovel chapter identity/publication was cross-checked where available under `docs/LORE-SOURCE-POLICY.md`.

No novel text is reproduced here beyond chapter titles and compact paraphrase.

## Evidence classification

- **CANON:** Echoes can be summoned, commanded, and used for combat and other practical work; later material remains compatible with Echoes serving a protective combat role.
- **INFERRED:** a persistent Java-owned task such as guarding a point may keep the Echo engaged with a nearby threat while the task remains active, provided the implementation does not claim a universal canonical command vocabulary or targeting algorithm.
- **DESIGN:** Ash Burrower itself; the `GUARD_POINT` command name; an eight-block guard-threat radius; choosing the nearest eligible threat; targeting only this repository's already-registered hostile Ash Burrower, Chainback, and Drowned Listener executors; movement speed; three attack-damage points; Armadillo-derived navigation/combat execution; and attack animation presentation.
- **UNKNOWN:** canonical guard-command wording or mental interface; exact autonomy, threat priority, guard radius, damage, attack cadence, friendly-fire rules, pursuit limits, whether every Echo autonomously protects a location, and cross-realm command behavior.
- **COMPATIBILITY:** `EchoInstanceData` remains authority for owned Echo identity, command mode, guard dimension/position, cargo, and manifestation reference. NeoForge target selection, pathfinding, melee and GeckoLib animation are replaceable execution/presentation. They do not create ownership, rewards, appraisal state, Nightmare resolution, or progression.

## Runtime boundary

The executor intentionally refuses generic Minecraft hostility. While `GUARD_POINT` is active it may acquire only physical entities corresponding to the repository's existing registered hostile Nightmare Creature executors. Ordinary vanilla mobs, players, other Echoes, villagers, and arbitrary modded entities are not promoted into guard threats by this slice.

When no eligible threat is inside the authored guard area, the manifestation returns to the persistent Java-owned anchor and holds there. Switching away from `GUARD_POINT` continues to clear the guard target through the existing Java command-state path.

## Deliberate limits

This is not a complete combat AI or canonical protection system. It does not add faction allegiance, owner-defense targeting, retaliation policy, threat memory, threat ranking by creature Rank/Class, loot/reward behavior, Echo injury/destruction/recovery, cross-dimension pursuit, or scenario-resolution events. Those remain separate Java-owned systems or UNKNOWN until supported by repository evidence and a concrete gameplay slice.
