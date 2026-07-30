# Shadow Slave project status

**Status date:** 2026-07-30  
**Documentation baseline:** `main@6a87991353480035f4fe6da08c775cd87d0e81df`  
**Canonical repository:** `Spud0302/shadowslave`

This is the current-state document. Historical assumptions remain in the changelog, issue log,
testing history, review records and Git history, but they do not override this file.

## Products

| Product | Current state | Public release |
| --- | --- | --- |
| Vanilla datapack | completed and frozen behavioural reference | `datapack-v1.0.0` |
| Standalone/shared Java core | `0.1.0-alpha.4`, CI-green, awaiting Claude verification | not released |
| Nightmare Spell modpack | design and dependency boundaries only | not released |

## Datapack

The Minecraft Java 1.21.1 datapack is complete and released. Players install
`shadowslave-v1.0.0.zip` unchanged in `<world>/datapacks/`; no mod loader or client install is
required. Feature development is frozen. Datapack changes are now limited to genuine release
defects, compatibility corrections and documentation.

## Java core implemented

- NeoForge 21.1.244 / Minecraft 1.21.1 / Java 21 workspace;
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
startup for PRs #14 and #15. That is not the final project gate. Under `docs/COLLABORATION.md`,
Claude independently reviews and tests GPT work.

**Blocking gate:** [Issue #16 — Claude verification gate: Java alpha.4](https://github.com/Spud0302/shadowslave/issues/16).

Until #16 is closed as verified or verified-with-fixes:

- `main` is CI-green but not Claude-verified for alpha.4;
- no further Java feature package should merge;
- no public Java release should be stamped;
- no claim should be made that the real O-key interaction has passed.

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

1. Claude reviews and independently tests alpha.4, including a real client session.
2. Record fixes/evidence and close Issue #16.
3. Add the live datapack reader and verified persistence writer without deleting legacy state.
4. Add persistent `NightmareRegistryData` and explicit instance ownership.
5. Implement one lore-shaped historical role, central conflict and valid resolution.
6. Build the same slice in the modpack track and compare with evidence.

## Canon boundary

The novel is authoritative for mechanics and terminology. The manhwa is a secondary visual and
staging reference. Project generation/appraisal systems are labelled design rather than claimed
canon. The datapack is a compatibility baseline, not a constraint on Java architecture.
