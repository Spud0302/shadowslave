# Shadow Slave — two-track mod transition plan

**Owner decision:** test two Java-era delivery paths against the same lore-aligned playable slice:

1. **Nightmare Spell modpack** — reuse mature mods for generic content and presentation while the Shadow Slave core owns identity and progression;
2. **standalone Shadow Slave mod** — implement the important systems directly in Java.

This is a controlled comparison, not a commitment to maintain two permanent products feature-for-feature.

## Current implementation status

- datapack `datapack-v1.0.0`: released and frozen;
- stable Java main: `0.1.0-alpha.4`, Claude-verified;
- active standalone preview: `0.1.0-preview.1` on draft PR #19;
- final preview automated workflow: green;
- Andrew play feedback: pending;
- Claude bulk review: pending;
- modpack track: design only.

The standalone preview now implements the first complete vertical slice: live legacy import, persistent per-player Nightmare ownership, a bundled dimension, one historical-role/conflict scenario, fixed appraisal, one ability, one Flaw, and an expanded Soul screen.

`docs/JAVA-LORE-ALIGNMENT.md` remains the lore gate. `docs/NIGHTMARE-SEED-ROADMAP.md` is binding for future completion and Seed architecture.

## 1. Comparison purpose

The modpack path tests whether strong atmosphere, spells, creatures, equipment, structures, and exploration can be assembled faster from existing ecosystems.

The standalone path tests whether owning the architecture produces a better Shadow Slave experience: personal Soul identity, lore-correct progression, Aspect/Flaw revelation, Nightmare ownership, networking, GUI, and multiplayer behaviour without dependency constraints.

The expected long-term answer may be hybrid:

- Shadow Slave Java code owns identity, progression, appraisal, migration, and Nightmare lifecycle;
- selected dependencies provide generic execution or presentation where they are stable and genuinely better;
- integrations remain optional where practical;
- no dependency becomes canonical Soul storage.

## 2. Shared technical baseline

Both paths target:

- Minecraft Java Edition 1.21.1;
- NeoForge 21.1.x;
- JDK 21;
- dedicated-server compatibility;
- server-authoritative gameplay state;
- data-driven resources where Minecraft already has strong JSON/resource formats;
- one shared domain implementation rather than duplicated progression logic.

Do not add a multi-loader abstraction before one functioning NeoForge product exists.

## 3. Frozen datapack role

The datapack contributes:

- migration fixtures and compatibility IDs;
- persistence and lifecycle lessons;
- tested drawback behaviour;
- release/install expectations;
- a broad progression reference.

Java deliberately replaces datapack limitations such as score bands, global ownership, safe retry/ejection, finite identity catalogues, and universal timer/boss assumptions.

## 4. Shared comparison slice

Both paths must eventually implement the same accepted slice:

```text
Uninfected
  -> infection event
Carrier
  -> First Nightmare trigger
Aspirant with Dormant Soul Core
  -> individually owned reconstructed conflict
  -> assigned historical role/body
  -> meaningful events and terminal resolution
  -> per-challenger outcome
  -> appraisal
Dreamer/Sleeper with Dormant Soul Rank
  -> permanent Aspect + independent Aspect Rank
  -> permanent Flaw
  -> server-synchronised Soul screen
```

The comparison is invalid if one path is evaluated with substantially more content than the other.

## 5. Shared domain ownership

The core owns:

- `SoulData` and progression state;
- `SoulIdentityData` and imported identity metadata;
- Soul Rank and Aspect Rank;
- migration and schema versions;
- Nightmare definitions, instances, participants, roles, conflict state, outcomes, and recovery;
- appraisal records and progression history;
- networking and Soul UI snapshots.

Temporary role/body/conflict state belongs to `NightmareInstance`, not permanent Soul data.

Future Nightmare completion must distinguish:

1. central conflict terminal resolution;
2. per-challenger survival/eligibility and outcome;
3. teardown/return;
4. appraisal/progression;
5. Seed post-resolution lifecycle.

## 6. Current standalone evidence

`0.1.0-preview.1` proves:

- loadable physical client and dedicated server;
- persistent Soul and identity attachments;
- transactional live datapack import;
- persistent one-owner Nightmare registry;
- separate per-player play-space slots;
- one entry choke point and shared teardown ownership;
- one bundled dimension and playable DESIGN scenario;
- Carrier -> Aspirant -> Dreamer/Sleeper progression;
- fixed DESIGN Aspect/Flaw appraisal;
- server-owned ability cooldown and Flaw execution;
- artifact packaging and install documentation.

It does not yet prove human gameplay quality, full restart/reconnect behaviour, multiple endings, custom Nightmare Creatures, natural infection, or later Seeds.

## 7. Path A — Nightmare Spell modpack

Path A will use the same core JAR. Existing mods may provide:

- spell visuals and generic execution;
- equipment/accessory slots;
- structures, creatures, and world generation;
- quest/documentation presentation;
- recipes, loot, animation, and sound;
- optional party/claims integration.

They must not own:

- canonical Soul state;
- permanent Aspect or Flaw identity;
- authoritative ranks or progression;
- Nightmare lifecycle or appraisal;
- permanent character history.

No manifest or adapters are implemented yet. Dependency, licence, redistribution, and server-install review remains required.

## 8. Path B — standalone core

The standalone path currently owns persistence, migration, networking, UI, Nightmare ownership, preview scenario construction, appraisal, ability, and Flaw execution.

Before a public `mod-v0.1.0`, it still needs evidence-backed polish and a product decision that the slice is worth releasing. The existence of `preview.1` does not itself authorise a public release.

## 9. Comparison scorecard

After both paths implement the same accepted slice, score 1–5 with evidence:

| Category | Measure |
| --- | --- |
| lore identity | purpose-built feel and lore alignment |
| development speed | time and code for the accepted slice |
| correctness | lifecycle leaks, duplication, lost state |
| multiplayer | simultaneous instances, reconnects, dedicated server |
| performance | tick, memory, load, and network cost |
| maintainability | ownership clarity and testability |
| content velocity | effort to add roles, conflicts, powers, creatures, areas |
| dependency risk | API churn, abandonment, incompatibilities |
| distribution | licences, manifests, client/server installation |
| migration | preservation of datapack and future saves |
| user experience | installation, UI consistency, clarity, configuration |

Record evidence, not preference.

## 10. Current sequence

1. Andrew plays `0.1.0-preview.1` and records concrete feedback.
2. Claude bulk-reviews PR #19 using the accumulated test matrix.
3. Fix evidence-backed defects without broad feature expansion.
4. Re-check primary lore for Nightmare endings, challenger outcomes, appraisal timing, and Seed consequences.
5. Implement `ResolutionGraph`, named terminal resolutions, and separate per-challenger outcomes.
6. Upgrade The Last Signal from direct campfire victory to a multi-step conflict with at least two valid DESIGN endings.
7. Add restart/idempotency and multiplayer shared-resolution tests.
8. Implement the same accepted slice in the modpack track.
9. Score both paths and choose standalone-led, modpack-led, hybrid, or stop an experiment.

## 11. Non-goals before the comparison gate

- every canonical Aspect or Flaw;
- full Dream Realm simulation;
- all ranks and creature classes;
- a giant public modpack;
- copied novel prose or official artwork;
- multi-loader support;
- perfect procedural generation;
- treating project design as canon;
- broad content expansion before the completion engine is sound.

The experiment exists to choose architecture with evidence. It is not the final content roadmap.
