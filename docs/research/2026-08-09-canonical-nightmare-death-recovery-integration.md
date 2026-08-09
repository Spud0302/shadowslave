# Canonical First-Nightmare death recovery integration

## Scope

This slice ports the previously reviewed restart-replayable canonical-death transaction from the older correctness lineage onto the current successful-completion recovery lineage through PR #119. It changes persistence ordering and recovery precedence only. It does not add safe ejection, a corpse Gate, a Nightmare Creature spawn, or a new death rule.

## Why this is the next correctness slice

Issue #34's successful-completion path is currently blocked on a real same-world process-kill row, and its latest recorded guidance says not to add more speculative completion state without new physical evidence. Current `main` and the #119 lineage still retain the older teardown-first `NightmareService.canonicalDeath(...)` event path, however. That path can consume the only active Nightmare ownership before the player-side Soul/identity reset is durably saved.

A crash in that interval can reload stale Aspirant/player identity state with no active Nightmare authority. Reversing the two writes alone is also unsafe: a crash after the player reset but before teardown can leave active ownership that ordinary login recovery could reinterpret as a live Nightmare. The bounded fix is a separate durable terminal-death intent.

This work reuses the established #119 persistence primitives rather than rebuilding the historical stack: exact-snapshot `NightmareRegistryData` operations, `SavedDataPersistence.saveAndWait(...)`, and synchronous `PlayerList.saveAll()`.

## Primary evidence checked

Research followed `docs/LORE-SOURCE-POLICY.md` and checked the working full-chapter access source plus official WebNovel where available.

- **Chapter 1 — `Nightmare Begins`: CANON.** The First-Nightmare explanation says survival is required and that dying in the First Nightmare results in a Nightmare Creature entering the waking world. The chapter's broader opening also describes infected people dying in their sleep and their bodies becoming monsters.
- **Chapter 887 — `Lapse of Judgment`: CANON, later confirmation.** During the Antarctic crisis, a sleeping First-Nightmare challenger dies and a Nightmare Creature is released into the waking world. Official WebNovel exposes this chapter and confirms the same bounded event.
- A later-material spot check found no evidence that ordinary First-Nightmare death became a normal safe Spell ejection. Exact corpse/Gate implementation details remain outside this slice.

No source text is committed.

## Evidence classification

- **CANON:** ordinary First-Nightmare death is real failure/death rather than safe Spell ejection; failed First Nightmares can release a Nightmare Creature into the waking world.
- **INFERRED:** none added by this persistence integration.
- **DESIGN:** Minecraft respawn remains a development accommodation; Java persists an exact Nightmare death intent before completion-receipt clearing, player reset, or active-ownership teardown, and gives that pending terminal intent login precedence over successful-completion/ordinary active recovery.
- **UNKNOWN:** mature corpse-Gate behavior, exact spawn timing/location/creature selection, process-kill convergence at each death checkpoint, and storage-device/power-loss behavior beyond the joined NeoForge I/O worker.
- **COMPATIBILITY:** successful completion, technical/admin recovery, completion receipt schema, and normal active-Nightmare login behavior are unchanged. Existing worlds create no `shadowslave_nightmare_deaths` SavedData until a death intent is needed.

## Durable order

For an active First-Nightmare death:

1. record the exact active `NightmareInstance` in `NightmareDeathRegistryData`;
2. persist SavedData and join queued NeoForge I/O;
3. clear any exact successful-completion receipt and persist/join the registry;
4. clear permanent identity and reset Soul state to the existing death result;
5. synchronously save player data;
6. remove owned Nightmare entities and exact active ownership;
7. persist/join the registry;
8. clear the exact death intent and persist/join again.

On login, a retained death intent is replayed before the successful-completion/active-instance recovery policy runs. A malformed/duplicate death marker or a marker conflicting with a different active snapshot fails closed rather than guessing.

## Tests

`NightmareDeathCoordinatorTest` simulates restart after each logical durable boundary and requires convergence to: no death marker, no completion receipt, durable player reset, and no active ownership. It also requires the death intent and player reset to be present when ownership is consumed.

`NightmareDeathRegistryDataTest` covers exact marker round-trip/idempotency, stale-snapshot refusal, and malformed/duplicate persisted input becoming recovery-blocking state rather than apparently empty state.

## Deliberate limits

- This integration intentionally leaves the legacy public `NightmareService.canonicalDeath(...)` helper in place. The supported NeoForge event path no longer uses it, but historical PR #79's removal/API hardening should be ported as a separate bounded follow-up after this transaction is green.
- Historical PR #81's package-visibility hardening for death-marker mutation is likewise not bundled here.
- No corpse Gate or Nightmare Creature consequence is implemented.
- No real process-kill/restart result is claimed by the pure restart simulation.
- Issue #34's six successful-completion physical rows remain separately blocked on an environment/player capable of executing them.
