# Shadow Slave project status

**Status date:** 2026-07-30  
**Documentation baseline:** `main@a852a76` (alpha.4 verification)  
**Canonical repository:** `Spud0302/shadowslave`

This is the current-state document. Historical assumptions remain in the changelog, issue log,
testing history, review records and Git history, but they do not override this file.

## Products

| Product                     | Current state                                           | Public release    |
| --------------------------- | ------------------------------------------------------- | ----------------- |
| Vanilla datapack            | completed and frozen behavioural reference              | `datapack-v1.0.0` |
| Standalone/shared Java core | `0.1.0-alpha.4`, CI-green and Claude-verified            | not released      |
| Nightmare Spell modpack     | design and dependency boundaries only                   | not released      |

## Datapack

The Minecraft Java 1.21.1 datapack is complete and released. Players install
`shadowslave-v1.0.0.zip` unchanged in `<world>/datapacks/`; no mod loader or client install is
required. Feature development is frozen. Datapack changes are now limited to genuine release
defects, compatibility corrections and documentation.

## Java core implemented

- NeoForge 21.1.244 / Minecraft 1.21.1 / JDK 21 workspace (JRE-only hosts cannot build it);
- committed Gradle wrapper and CI packaging;
- persistent, schema-versioned, server-authoritative `SoulData`;
- lore-aligned Uninfected -> Carrier -> Aspirant -> Dreamer model;
- optional Soul Rank before the First Nightmare and Dormant rank for Aspirants/Dreamers;
- independent Aspect Rank;
- server-to-client Soul snapshots and read-only O-key Soul screen;
- command-driven development transitions;
- alpha-schema migration;
- pure, fail-safe datapack migration translation and fixtures;
- validator cross-check ensuring all 16 imported Flaw names still match the frozen datapack;
- physical-client and dedicated-server startup smoke gates.

## Verification state

GitHub Actions passed compilation, unit tests, JAR packaging, client startup and dedicated-server
startup for PRs #14 and #15. Under `docs/COLLABORATION.md` that is not the final gate on its own, so
Claude reviewed and independently re-ran the work.

**Alpha.4 is Claude-verified.** [Issue #16](https://github.com/Spud0302/shadowslave/issues/16) is
closed. Evidence, reproduced locally rather than taken from workflow status:

- `./mod/gradlew -p mod build` — BUILD SUCCESSFUL, 14 tests, 0 failures, `shadowslave-0.1.0-alpha.4.jar`;
- dedicated-server smoke — `Done (15.023s)! For help, type "help"` with the mod loaded;
- physical-client smoke under `xvfb` — client loading plus GUI atlas creation;
- `python3 shadowslave/tools/validate.py` — clean, including the 16-name Java/datapack cross-check;
- review of client/server side separation, payload registration and the migration translator.

A fix came out of that verification: the bare Gradle smoke commands are **not** pass/fail gates — the
dedicated server failed to start three times (port conflict, then a stale `session.lock`) while Gradle
still reported `BUILD SUCCESSFUL` with exit `0`. `mod/verify-smoke.sh` now applies CI's marker-based
criterion locally and is proven to fail as well as pass.

**Human tests are deferred, not passed** (owner decision **D2** in `docs/OPEN-QUESTIONS.md`): they are
visual or balance judgements, so they no longer gate merges or releases. Nobody has run them, and no
document may claim otherwise. The real-client O-key walkthrough remains written down in `TESTING.md`.

Still true regardless: **no public Java release should be stamped** until there is something worth
releasing — that is a product decision, not a verification gate.

## Not implemented yet

- live reading/writing of datapack scores and tags;
- persistence/read-back of full imported Aspect and Flaw instances;
- legacy cleanup after verified import;
- natural infection;
- persistent Nightmare registry and instance ownership;
- data-driven historical roles, central conflicts and resolutions;
- playable Java First Nightmare;
- appraisal/revelation beyond development fixtures;
- abilities, Memories, Echoes, Dream Realm progression, Gates or later ranks;
- modpack dependency manifest or compatibility adapters.

## Next gated sequence

1. ~~Claude reviews and independently tests alpha.4.~~ **Done**; Issue #16 closed. The real client
   session is deferred per **D2**, not performed.
2. Add the live datapack reader and verified persistence writer without deleting legacy state. Absent
   scores are not zeroes — see the hazard note in `docs/DATAPACK-MIGRATION.md` before writing it.
3. Add persistent `NightmareRegistryData` and explicit instance ownership, honouring the Nightmare
   lifecycle contract in `docs/JAVA-HANDOFF.md` §6.
4. Implement one lore-shaped historical role, central conflict and valid resolution.
5. Build the same slice in the modpack track and compare with evidence.
6. Optional, closes the last real gap: a NeoForge GameTest for Soul persistence across a relog —
   `mod/build.gradle` already declares `gameTestServer`.

## Canon boundary

The novel is authoritative for mechanics and terminology. The manhwa is a secondary visual and
staging reference. Project generation/appraisal systems are labelled design rather than claimed
canon. The datapack is a compatibility baseline, not a constraint on Java architecture.
