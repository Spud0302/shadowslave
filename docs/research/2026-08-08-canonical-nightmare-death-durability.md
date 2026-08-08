# Canonical First-Nightmare death durability audit

## Scope

This note covers only the Java persistence ordering for an ordinary First-Nightmare death. It does not add a mercy/ejection mechanic, corpse-Gate gameplay, or a new lore rule.

## Primary evidence checked

Working-source chapter text was rechecked under `docs/LORE-SOURCE-POLICY.md`.

- **Chapter 1 — `Nightmare Begins`: CANON.** The waking-world explanation establishes that failing the First Nightmare by dying is real death and that the failure can open a Gate for a Nightmare Creature in the waking world.
- **Chapter 887 — `Lapse of Judgment`: CANON, later confirmation.** A First-Nightmare challenger dies during the Antarctic crisis and a Nightmare Creature is consequently released into the waking world. This later scene confirms that the Chapter 1 explanation is not merely an early-character misunderstanding.
- The official WebNovel publication was checked as the publication/wording cross-reference where accessible; no contrary later rule was found for the bounded question addressed here.

No source text is copied into the repository.

## Evidence classification

- **CANON:** ordinary First-Nightmare failure by death is death, not a safe Spell ejection; a failed First Nightmare can release a Nightmare Creature through a Gate.
- **INFERRED:** none added by this persistence slice.
- **DESIGN:** Minecraft respawn remains a development accommodation; the Java server records a durable death-outcome intent before clearing completion state, resetting player attachments, or consuming active Nightmare ownership.
- **UNKNOWN:** the exact mature corpse-Gate implementation, spawn timing/location, creature selection, and physical process-kill behavior at each filesystem boundary remain unresolved/unproven here.
- **COMPATIBILITY:** existing worlds have no `shadowslave_nightmare_deaths` SavedData until a death occurs. Successful completion, technical/admin recovery, and preview reset keep their existing semantics.

## Correctness defect

Before this slice, `NightmareService.canonicalDeath(...)` could remove the active Nightmare and persist that registry removal before the player's Soul/identity reset was saved. A process failure in that window left no active recovery authority and could reload stale Aspirant/identity state.

Simply reversing the two writes is insufficient: a crash after saving the player reset but before teardown would leave active ownership that ordinary login recovery could reinterpret as a recoverable active Nightmare. A distinct durable death intent is required to disambiguate the already-chosen terminal outcome.

## Durable order

The event bridge now records death through `NightmareDeathService`:

1. persist the exact active `NightmareInstance` as a canonical-death intent and join queued SavedData I/O;
2. clear any exact retained successful-completion receipt and persist/join registry I/O;
3. reset permanent Soul/identity state to the existing canonical-death result and synchronously save player data;
4. remove owned entities and exact active ownership, then persist/join registry I/O;
5. clear the death intent and persist/join it.

Login checks pending canonical death before preview-reset, technical-exit, successful-completion, or ordinary active-instance recovery. Replay is idempotent across the documented boundaries.

The death marker stores the exact instance snapshot so replay can still identify owned entities and the relevant completion snapshot if active ownership was already consumed before the final marker-clear checkpoint.

## Fail-closed behavior

Malformed or duplicate death-marker SavedData is retained as a recovery-blocking state rather than throwing out of the SavedData decoder and risking replacement with an apparently empty registry. A marker that conflicts with a different current active snapshot also blocks rather than guessing which outcome owns the player.

## Deliberate limits

- No corpse Gate is spawned; that remains separate lore-sensitive content requiring its own evidence/design pass.
- Real dedicated-server process-kill fault injection remains required by Issue #34.
- `SavedDataPersistence.saveAndWait(...)` joins NeoForge's queued I/O worker but is not proof against every OS/filesystem/storage-device/power-loss failure.
- The legacy `NightmareService.canonicalDeath(...)` helper remains in source but the NeoForge event runtime now routes through the durable service; removing/delegating that unused bypass is a follow-up API-hardening slice if static caller audit confirms no compatibility requirement.
