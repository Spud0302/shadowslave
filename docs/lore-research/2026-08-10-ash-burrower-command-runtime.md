# Ash Burrower Echo command runtime — evidence note

**Date:** 2026-08-10  
**Scope:** make the already-playable Ash Burrower Echo execute persisted FOLLOW/HOLD commands without expanding the Echo catalogue.

## Repository context checked

Before implementation this run re-read current `main`, open PRs/issues, `PROJECT-STATUS.md`, `GPT_HANDOFF.md`, `ISSUES.md`, `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, `docs/NIGHTMARE-SEED-ROADMAP.md`, and the active gameplay/correctness graph.

Open PR #186 already owns authoritative Ash Burrower Echo ownership/summoning and is green on exact-head Preview Gates #179. Open PR #184 already owns Nightmare-Creature physical execution. This slice therefore stacks on #186 and does not duplicate either ownership/summoning or creature work.

## Primary novel evidence

Research followed `docs/LORE-SOURCE-POLICY.md`.

- **Chapter 47 — Echo:** after acquiring the Carapace Scavenger Echo, Sunny observes that the Echo can be summoned and used for combat, carrying heavy cargo and other tasks. This directly establishes practical command/use beyond merely rendering an Echo beside its owner.
- **Chapter 193 — The Catacombs:** Sunny summons Stone Saint and gives her a positional/protective instruction to remain close to cohort members. This directly supports owner-issued behavioral direction to an Echo rather than a purely autonomous pet model.
- Official WebNovel was used to cross-check Chapter 47 identity/publication. The owner-designated NovelFull access layer currently lists through Chapter 3116; no rule in this slice depends on later unpublished/index-only metadata.

No novel text is copied into source or data files.

## Evidence classification

- **CANON:** ordinary Echoes can be summoned and directed to perform useful tasks; an owner can give an Echo behavioral/positional instructions.
- **INFERRED:** durable Java-owned command state is a safe representation for an instruction that should survive ordinary save/load while a replaceable Minecraft entity executes it.
- **DESIGN:** the exact `FOLLOW`/`HOLD` command syntax; defaulting old stored Echoes to `HOLD`; 10-tick executor cadence; three-block follow stopping distance; navigation speed; vanilla Armadillo body and invulnerability.
- **UNKNOWN:** universal Echo command vocabulary, exact mental-command mechanics/timing/range, cross-realm command behavior, autonomous tactical intelligence, damage/destruction/recovery behavior, and whether every Echo can execute every command family.
- **COMPATIBILITY:** `EchoInstanceData.commandMode` is Java authority. The Minecraft Mob navigation/no-AI state is execution only and is re-derived from the persisted command. Existing stored Echoes remain readable because missing `command_mode` defaults to `HOLD`.

## Runtime boundary

Ash Burrower's existing authored catalogue already declares `FOLLOW`, `CARRY`, `HOLD`, and `GUARD_POINT`. This slice implements only the first and third. `CARRY` and `GUARD_POINT` remain definition-only until they receive concrete Java-owned state/executors.

The manifested Armadillo never decides its canonical command. Server ticks read Java-owned Echo state and apply navigation/hold behavior. While FOLLOW moves the executor, Java refreshes the stored manifestation dimension/position so restart lookup does not rely on the original summon location.

## Deliberate limits

- no combat AI, cargo inventory, guard-point target or destruction semantics;
- no new Echo identity or command catalogue primitive;
- no merge/write to `main`;
- issue #34 crash-atomic successful appraisal remains a separate correctness dependency;
- #184's Nightmare Creature executor remains a separate integration edge.
