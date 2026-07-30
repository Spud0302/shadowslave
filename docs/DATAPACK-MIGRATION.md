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

### Hazard for whoever writes that reader: an absent score is not `0`

`LegacyDatapackSnapshot` uses `0` to mean "this player has no such score", and the translator relies on
that — `rankScore() == 0` is how it recognises a player who never completed a First Nightmare. The
convention is sound because the datapack never _sets_ `ss_rank`, `ss_aspect` or `ss_flaw` to zero:
`progression/become_sleeper` writes `1`, the generator writes `11..44`, and `test/reset` **removes** the
score rather than zeroing it. Absent is therefore the only not-a-Sleeper state.

The hazard is on the reading side. Minecraft distinguishes "objective has no value for this player" from
"value is 0", and a failed or unread lookup must not become `0`:

- read each objective explicitly and map _absent_ to `0` deliberately, with a comment saying so;
- never let an exception, an empty result or an unparsed command reply fall through to `0` — that would
  silently downgrade a completed Sleeper to an unmigrated player and, because `hasAnyLegacyState()` would
  then be false for a non-Carrier, skip their migration entirely and lose the identity;
- if a score cannot be read, fail the import for that player rather than guessing. The translator is
  already fail-closed; keep the reader the same.

This is not a hypothetical. Absent-versus-zero is the single most repeated bug in this project's history
— §1.7, §1.10, the dead sneak-to-enter filter, and the `scores={x=..0}` shape `validate.py` now rejects
outright. `docs/ENGINEERING-NOTES.md` has the datapack-side rule; this is the Java-side restatement.

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
