# Changelog

[Pride Versioning](https://pridever.org/) — `PROUD.DEFAULT.SHAME`.

The previous full datapack and early-alpha changelog remains available in Git history and is referenced
under `docs/history/`. This root changelog now records the active Java preview line and major retained
milestones.

## `0.1.0-preview.1` — installable playable Java preview

**Status:** development artifact on draft PR #19; not a public release; Andrew play feedback and Claude
bulk review pending.

### Live migration

- added direct read-only frozen-datapack scoreboard/tag reader;
- distinguished deliberately absent scores from explicit stored zero;
- added persistent imported and general Aspect/Flaw records;
- added provisional writes, exact read-back, final migration marker, and rollback;
- retained all legacy scores and tags;
- added `/shadowslave migrate_datapack`.

### Nightmare lifecycle

- added persistent Overworld `NightmareRegistryData`;
- enforced one active Nightmare per player UUID;
- added separate per-player scenario slots and return/recovery records;
- added bundled Nightmare dimension and biome;
- added one entry choke point and shared owned-entity/registry teardown;
- distinguished success, canonical death, technical recovery, and admin abort.

### Playable vertical slice

- added DESIGN scenario **The Last Signal**;
- added DESIGN historical role **last watchkeeper**;
- made combat optional to the signal-restoration conflict;
- added fixed DESIGN appraisal;
- added persistent Aspect **Last Light**, Awakened Aspect Rank;
- added server-authoritative **Kindle** ability and cooldown;
- added persistent Flaw **Cold Ash** and water/rain Weakness;
- expanded the O-key Soul screen with formal names, ability, and Flaw effect;
- added player onboarding, inspection, recovery, and reset commands.

### Documentation and evidence

- added preview install/play guide;
- added lore decision ledger;
- added final build provenance and checksums;
- added accumulated Claude test matrix;
- added binding Nightmare/Seed completion roadmap;
- final GitHub workflow run 33 passed wrapper, compile/tests, physical client, dedicated server,
  packaging, and artifact upload.

## `0.1.0-alpha.4` — fail-safe datapack translation foundation

- immutable legacy evidence snapshot and pure translator;
- exact generated and legacy Aspect/Flaw mappings;
- fail-closed rejection of active/inconsistent state;
- deterministic imported IDs and idempotency fixtures;
- validator cross-check for all imported Flaw names;
- independently reviewed and verified by Claude after CI.

## `0.1.0-alpha.3` — lore-aligned Java schema

- removed Mundane from the Soul Rank ladder;
- added explicit Aspirant and Dreamer stages plus awakening path;
- separated Aspect Rank from Soul Rank;
- updated Soul snapshot/screen and schema-1 migration;
- documented novel/adaptation/design authority boundaries.

## `0.1.0-alpha.2` — server-synchronised Soul screen

- limited server-owned Soul snapshots;
- O-key read-only screen and command fallback;
- login/mutation synchronization;
- physical-client and dedicated-server side-separation gates.

## `0.1.0-alpha.1` — persistent Java Soul core

- NeoForge 1.21.1 / Java 21 workspace;
- codec-backed persistent Soul attachment;
- server-owned transitions and development commands;
- wrapper, JUnit, packaging, and server startup checks.

## Datapack `1.0.0`

- released and frozen as `datapack-v1.0.0`;
- remains the vanilla product and Java migration/behavioural reference;
- historical prototype and datapack release details remain in Git history.
