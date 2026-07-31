# Datapack to Java migration

The frozen datapack remains a supported origin for player identity. `0.1.0-preview.1` implements a live, explicit, fail-closed migration transaction. Source datapack scores and tags remain read-only throughout this preview.

## Current implementation

The operator command is:

```text
/shadowslave migrate_datapack
```

Run it only on a backed-up legacy world.

The live transaction now:

1. refuses to overwrite established native Java Soul/identity state;
2. reads immutable legacy score/tag evidence through the direct scoreboard API;
3. translates through the tested pure `DatapackMigrationTranslator`;
4. constructs imported and general persistent Aspect/Flaw records;
5. writes Java attachments provisionally;
6. reads them back and verifies exact identity and cross-references;
7. writes the final migration marker only after successful verification;
8. rolls all Java attachments back if any step fails;
9. retains every legacy score and tag.

Legacy cleanup is deliberately not implemented.

## Phase 1 — immutable live evidence

`LegacyDatapackReader` collects:

- player UUID;
- `ss_carrier` state;
- `ss_rank`, `ss_aspect`, and `ss_flaw` scores;
- compatibility tags required by old `1..4` identities;
- whether `ss_in_nightmare` is active;
- completed Java migration version.

It creates `LegacyDatapackSnapshot` without mutating the player.

### An absent score is not an explicit zero

`LegacyDatapackSnapshot` uses `0` as the internal sentinel for “no stored score”. The frozen datapack never writes an explicit zero for these identity objectives; it removes the score instead.

The reader therefore:

- maps a missing objective or missing player score to the sentinel deliberately;
- rejects an explicitly stored `0` rather than treating it as absence;
- preserves positive and negative values for downstream validation;
- fails the import rather than guessing when evidence cannot be read.

This closes the repeated absent-versus-zero failure class that affected earlier datapack guards.

## Phase 2 — pure translation

`DatapackMigrationTranslator` produces:

- no plan when no supported legacy state exists;
- no new plan when the current migration is already complete;
- a validated Carrier plan;
- a validated completed Dreamer/Sleeper plan;
- a fail-safe exception for inconsistent, unsupported, or active-Nightmare state.

The translator remains independent of Minecraft entity mutation.

## Phase 3 — persistent Java identity

The live writer persists three linked records:

- authoritative progression and IDs in `SoulData`;
- full revealed identity in `SoulIdentityData`;
- frozen datapack compatibility metadata in `ImportedIdentityData`.

`DatapackMigrationPersistenceVerifier` checks exact Soul and imported metadata equality and verifies that Aspect/Flaw IDs agree across records. The service also checks the general revealed identity before finalizing the marker.

Any exception restores the player's pre-attempt Java attachments.

## Phase 4 — legacy cleanup

**Not implemented.** The preview does not remove scoreboards, scores, tags, functions, or datapack files. Cleanup requires a later owner-approved package with separate backup, idempotency, and rollback criteria.

## Preserved mappings

### Progression

- untouched -> no Java migration plan;
- Carrier -> `CARRIER`, Nightmare Spell path, no Soul Rank;
- `ss_rank = 1` -> `DREAMER`, Nightmare Spell path, Dormant Soul Rank.

### Generated Aspects

Scores `11..44` preserve:

- Veiled / Ashen / Pale / Restless nature;
- Witness / Bearer / Warden / Wanderer archetype;
- exact formal name;
- explicit Dormant imported Aspect Rank;
- legacy mechanical-root metadata.

Old scores `1..4` require their matching compatibility tag and remain explicit Shadow, Flame, Bone, or Wind prototypes.

### Generated Flaws

Scores `11..44` preserve exact formal names and semantic families:

- daylight burden;
- fragile vessel;
- ravenous hunger;
- burdened movement.

Old scores `1..4` require matching compatibility tags and preserve Nightbound, Brittle Vessel, Hollow Maw, or Leadbound.

The historical tag `ss_flaw_weightless` imports as burdened movement. It does not require Java to reproduce the retired fall-distance implementation.

## Fail-safe cases

Migration rejects:

- active `ss_in_nightmare` state;
- explicit zero, negative, unsupported, or inconsistent identity scores;
- a completed rank with missing Aspect or Flaw;
- orphaned Aspect/Flaw scores;
- old scores without matching compatibility tags;
- established non-empty native Java Soul/identity state;
- any read-back mismatch.

No failure path deletes or modifies source datapack evidence.

## Evidence status

The final preview workflow passed compilation, the migration/unit suite, physical-client startup, dedicated-server startup, packaging, and artifact upload. The bulk manual matrix still includes migration on a backed-up real datapack world; that human end-to-end check has not yet been performed.

See:

- `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`;
- `docs/PLAYABLE-PREVIEW-PROVENANCE.md`;
- `mod/PREVIEW-PLAY-GUIDE.md`.
