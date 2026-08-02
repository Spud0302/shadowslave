# Java handoff contract — datapack `1.0.0` baseline

**Purpose:** preserve completed datapack players while replacing datapack machinery with the lore-aligned Java model in `docs/JAVA-LORE-ALIGNMENT.md`.

The port is not a line-by-line translation of mcfunctions. Preserve earned identity and tested contracts; replace prototype rules caused by datapack limits.

## Current implementation status

`0.1.0-preview.1` on draft PR #19 now includes:

- live transactional legacy score/tag import;
- persistent native and imported Aspect/Flaw records;
- persistent per-player Nightmare registry and ownership;
- bundled Nightmare dimension;
- one playable DESIGN First Nightmare, **The Last Signal**;
- fixed DESIGN appraisal and one executable ability/Flaw;
- expanded Soul screen and player preview commands.

The final automated checkpoint is green. Andrew's complete playthrough and Claude's bulk review remain pending. This is not a public release.

Future completion and Seed work is governed by `docs/NIGHTMARE-SEED-ROADMAP.md`.

## 1. Frozen datapack behaviour worth preserving

```text
Uninfected/Mundane description
  -> Carrier
  -> First Nightmare prototype
  -> Sleeper/Dreamer with Dormant Soul Rank
  -> persistent generated Aspect identity
  -> persistent generated Flaw identity with a real drawback
```

The datapack proved persistence, personal identity, meaningful drawbacks, lifecycle cleanup, deterministic verification, and vanilla-server packaging. It did not prove a canonical infection algorithm, appraisal formula, safe-failure rule, or universal boss structure.

## 2. Java progression mapping

| Legacy state | Java result |
| --- | --- |
| no Carrier and no completed rank | Uninfected, undecided path, no Soul Rank |
| `ss_carrier` and no completed rank | Carrier, Nightmare Spell path, no Soul Rank |
| completed `ss_rank = 1` | Dreamer/Sleeper, Nightmare Spell path, Dormant Soul Rank |

`Mundane` is descriptive language, not Soul Rank zero. Imported completed players do not receive an invented active historical role.

## 3. Aspect import

Generated `ss_aspect` scores `11..44` preserve:

- exact generated formal name;
- Veiled / Ashen / Pale / Restless legacy nature;
- Witness / Bearer / Warden / Wanderer legacy archetype;
- explicit Dormant imported Aspect Rank;
- legacy mechanical root.

Old scores `1..4` require matching compatibility tags and remain explicit Shadow, Flame, Bone, or Wind prototypes.

Java stores the authoritative ID/rank in `SoulData`, full revealed identity in `SoulIdentityData`, and frozen compatibility metadata in `ImportedIdentityData`.

## 4. Flaw import

Generated `ss_flaw` scores preserve exact names and semantic families:

- daylight burden;
- fragile vessel;
- ravenous hunger;
- burdened movement.

The historical tag `ss_flaw_weightless` is an import identifier only. Java imports the semantic burdened-movement identity, not the retired `safe_fall_distance` mechanism.

Old scores `1..4` require matching compatibility tags and preserve Nightbound, Brittle Vessel, Hollow Maw, or Leadbound.

Never expose `shadow_slave` as the Flaw's formal name. Shadow Slave is canonically an Aspect.

## 5. State that must not become permanent Soul architecture

Do not import datapack scratch/runtime values such as:

- timers and bossbar scratch;
- temporary health and roll scores;
- return-coordinate scratch;
- cooldown and test-bypass scores;
- active-Nightmare tags;
- trial-behaviour scratch.

Typed Java analogues belong to session or `NightmareInstance` state, not permanent `SoulData`.

## 6. Nightmare lifecycle contract

The preview implements the first version of the failure-earned service boundaries:

| Datapack seam | Java contract |
| --- | --- |
| `nightmare/enter` | `NightmareService.tryEnter`; one eligibility and ownership choke point |
| objective tick | scenario events; combat/object interaction is not universally completion |
| `nightmare/leave` | one lifecycle teardown for owned entities and registry ownership |
| ejection/recovery | explicit exit reason and presentation, never separate cleanup |
| survive | terminal conflict resolution -> teardown/return -> appraisal/progression |
| trial observation | temporary evidence owned by the active `NightmareInstance` |

Required invariants:

- every entry source calls the same service;
- one active instance per player UUID;
- temporary role, scenario, owned entities, conflict state, and recovery data live on the instance;
- success, canonical death, technical recovery, and admin abort converge on shared teardown ownership;
- technical recovery is not described as mercy from the Spell;
- appraisal runs after accepted lifecycle completion and must not own teardown.

`0.1.0-preview.1` uses a direct campfire event as a development terminal trigger. It is not the final completion architecture. `docs/NIGHTMARE-SEED-ROADMAP.md` requires named terminal resolutions, separate per-challenger outcomes, and later `ResolutionGraph` work.

## 7. Datapack rules Java replaces

Do not preserve these as universal lore:

- first ordinary sleep causes infection;
- Carrier skips the Aspirant stage;
- safe low-health ejection is normal;
- every Nightmare is a timer plus boss;
- one visible statistic canonically determines a Flaw;
- four Aspect roots or four Flaw families exhaust all possibilities;
- Aspect Rank equals Soul Rank;
- generic datapack attributes are automatically canonical Spell Attributes.

Imported players keep the identities they earned. Fresh Java systems use the lore gate.

## 8. Import transaction

Implemented order:

1. refuse to overwrite established native Java state;
2. read immutable legacy evidence;
3. reject active/inconsistent/unsupported evidence;
4. construct the accepted migration plan;
5. write Java Soul and identity records provisionally;
6. read back and verify exact equality and cross-references;
7. write the migration marker;
8. rollback Java attachments on failure;
9. retain legacy datapack values.

Legacy cleanup remains deliberately unimplemented.

## 9. Data-driven boundaries

Continue using Minecraft resources/codecs for:

- dimensions and dimension types;
- biomes and world generation;
- tags, predicates, recipes, loot, structures, and advancements;
- future scenario, role, conflict, and resolution definitions where codecs fit.

Java owns persistence, services, appraisal, networking, GUI, custom entities/AI, ownership, and lifecycle.

## 10. Definition of migration success

Migration succeeds when:

- no Soul Rank is invented for an untouched player;
- a Carrier remains a Carrier without a permanent identity;
- a completed legacy player becomes a Dormant Dreamer/Sleeper;
- generated Aspect name/root/rank is retained;
- generated Flaw name/family/effect identity is retained;
- nothing is rerolled;
- migration is idempotent;
- failures do not alter source evidence or leave partial Java state;
- optional integrations can disappear without erasing canonical identity.

## 11. Next handoff gate

Before broad expansion:

1. Andrew plays the preview and records concrete feedback;
2. Claude bulk-reviews PR #19 using `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`;
3. defects are fixed with repeated evidence;
4. future Nightmare work begins with renewed primary-lore verification and `docs/NIGHTMARE-SEED-ROADMAP.md`.
