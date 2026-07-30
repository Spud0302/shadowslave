# Datapack to Java migration

The frozen datapack remains a supported origin for player identity. Migration is deliberately split so live player state is never cleared before the Java result has been constructed and validated.

## Phase 1 — immutable evidence snapshot

A live reader will collect:

- player UUID;
- `ss_carrier` state;
- `ss_rank`, `ss_aspect` and `ss_flaw` scores;
- compatibility tags needed by old `1..4` identities;
- whether `ss_in_nightmare` is active;
- completed Java migration version.

It creates `LegacyDatapackSnapshot` and performs no mutation.

## Phase 2 — pure translation

`DatapackMigrationTranslator` produces either:

- no plan because no legacy state exists;
- no plan because the migration version is already complete;
- a validated Carrier plan;
- a validated completed Dreamer/Sleeper plan;
- a fail-safe exception for inconsistent, unsupported or active-Nightmare state.

The translator does not read Minecraft entities, write attachments or clear scoreboards.

## Phase 3 — persist Java state

A later live writer will:

1. persist `SoulData` schema v2;
2. persist imported Aspect/Flaw instance records;
3. read them back;
4. verify IDs, names, ranks, family/effect metadata and migration version;
5. mark Java migration complete.

## Phase 4 — legacy cleanup

Only after verification may the live writer remove or ignore legacy state. Cleanup must be idempotent and must not affect the frozen datapack release files.

## Mappings preserved

### Progression

- untouched -> no Java migration plan;
- Carrier -> `CARRIER`, Nightmare Spell path, no Soul Rank;
- `ss_rank = 1` -> `DREAMER`, Nightmare Spell path, Dormant Soul Rank.

### Generated Aspect scores

`11..44` preserve the exact generated name:

- Veiled / Ashen / Pale / Restless;
- Witness / Bearer / Warden / Wanderer;
- explicit imported Dormant Aspect Rank;
- legacy mechanical root metadata.

### Old Aspect scores

`1..4` require the matching compatibility tag and preserve Shadow, Flame, Bone or Wind as explicit legacy prototypes.

### Generated Flaw scores

`11..44` preserve the exact generated player-facing name and semantic family:

- daylight burden;
- fragile vessel;
- ravenous hunger;
- burdened movement.

### Old Flaw scores

`1..4` require the matching compatibility tag and preserve the neutral replacement identities Nightbound, Brittle Vessel, Hollow Maw or Leadbound.

The historical internal tag `ss_flaw_weightless` maps to burdened movement, not the retired fall-distance implementation.

## Fail-safe cases

Translation rejects:

- active `ss_in_nightmare` state;
- negative or unsupported scores;
- a completed rank with missing Aspect or Flaw;
- orphaned Aspect/Flaw scores on an incomplete player;
- old `1..4` scores without matching compatibility tags.

No failure path deletes or modifies source evidence.

## Current boundary

`0.1.0-alpha.4` implements and tests the snapshot, identity mapping and pure translation layers. It does **not** yet read a live player's scoreboard or persist full `AspectInstance`/`FlawInstance` attachments. Those are the next migration package.
