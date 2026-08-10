# Ash Burrower guard-point command evidence boundary

**Date:** 2026-08-11  
**Scope:** execute the already-authored `Ash Burrower` Echo `GUARD_POINT` command without expanding the Echo catalogue or inventing a canonical command system.

## Repository authority checked

Before implementation, current `main`, open PRs/issues, `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and the active correctness/integration branches were re-read.

`EchoContentCatalog` already gives Ash Burrower the `GUARD` role and the `GUARD_POINT` command alongside `FOLLOW`, `CARRY`, and `HOLD`. The runtime on `main` persisted only the command mode and executed `FOLLOW`/`HOLD`; every other mode fell through to an inert hold. This slice therefore connects an existing Java-owned definition to runtime state rather than authoring new content.

## Primary novel evidence

### Chapter 47 — `Echo`

The owner-designated NovelFull chapter text was re-read, with official WebNovel used to confirm chapter identity/publication. It establishes the bounded points needed here:

- an Echo can be summoned into physical form;
- it follows its owner's command;
- it can be used in combat, carry cargo, and perform other tasks;
- the chapter immediately demonstrates a summoned Echo responding to command and then being used to carry a companion.

This supports commandable practical Echo execution in general. It does **not** establish a universal named `GUARD_POINT` command, a command-target persistence schema, exact navigation radii/speeds, pathfinding behavior, cross-dimension rules, or an Ash Burrower creature.

## Evidence classification

- **CANON:** Echoes can be summoned and commanded for practical tasks, including combat and carrying; the chapter presents the Echo as not truly alive.
- **INFERRED:** a Java-owned Echo command can have a persistent target separate from the manifestation's changing current position so the physical executor can continue carrying out the same already-issued task after ordinary movement or re-summoning.
- **DESIGN:** Ash Burrower; `GUARD_POINT` as the command name; `/shadowslave_echo guard ash_burrower`; anchoring the command to the player's current block and dimension; the exact 1.5-block stop radius, navigation speed, player-facing wording, and fail-safe HOLD behavior when the target cannot be executed.
- **UNKNOWN:** canonical command vocabulary/interface, mental-command detail, exact autonomy, guard radius/engagement policy, cross-realm/cross-dimension behavior, persistence through every form of dismissal or owner absence, and destruction/recovery rules.
- **COMPATIBILITY:** `EchoInstanceData` remains the canonical Java-owned identity/command/target record. NeoForge entity navigation only executes that state. Switching away from `GUARD_POINT` clears the obsolete target; dismissal preserves the active guard order for later re-summoning. A malformed/legacy targetless guard order fails safe to HOLD rather than deriving canonical state from entity position.

## Runtime contract

Issuing `GUARD_POINT` records the issuing player's current Java-known dimension and block position as the command target. If manifested in that dimension, Ash Burrower navigates back to the target and settles when close enough. If the target is absent or belongs to another dimension, the executor stops rather than teleporting, guessing, or changing the command target.

The manifestation's current UUID/dimension/position remains separate runtime-execution state and may continue updating while the guard anchor remains fixed. Dismissal removes only the manifestation state, not ownership or the selected guard command/target.

## Deliberate limits

This slice does not implement `CARRY`, combat interception around the guarded point, hostility selection, cargo inventory, cross-dimension travel, autonomous threat evaluation, damage/destruction/recovery, or any new Echo identity. Those remain separate runtime slices and must not be inferred from this command adapter.
