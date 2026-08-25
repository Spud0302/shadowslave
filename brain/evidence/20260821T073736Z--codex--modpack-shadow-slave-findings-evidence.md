---
uid: 20260821T073736Z-codex-modpack-shadow-slave-findings-evidence
record_kind: evidence
authority: evidence
lore_class: mixed
state: closed
owner: codex
task_id: 20260821T073644Z-codex-modpack-shadow-slave-findings-dump
created: 2026-08-21
updated: 2026-08-21
branch: packaging/combat-core-closure
captured_commit: 8c32d355f0397b89ab6c0553b8f3612fb992f474
worktree_dirty: true
sources:
  - modpack/manifest.json
  - modpack/README.md
  - modpack/ARCHIVE-VERIFICATION.md
  - modpack/EXTERNAL-BUILD-PROVENANCE.md
  - modpack/tools/build_package.py
  - modpack/tools/check_dependency_closure.py
  - modpack/tools/validate_manifest.py
  - modpack/tools/verify_package.py
  - modpack/tests/
  - .github/workflows/modpack-shell.yml
  - .github/workflows/java-core.yml
  - mod/build.gradle
  - mod/gradle.properties
  - mod/src/main/templates/META-INF/neoforge.mods.toml
  - mod/run/logs/latest.log
  - combat-core/
  - docs/LORE-SOURCE-POLICY.md
  - docs/JAVA-LORE-ALIGNMENT.md
  - docs/PREVIEW-LORE-DECISIONS.md
  - docs/lore-research/section-a-aspects-and-flaws.md
  - docs/lore-research/section-a-verification-pass-2.md
  - docs/lore-research/section-a-verification-pass-3.md
  - docs/lore-research/section-b-ranks-and-progression.md
  - docs/lore-research/section-c-memories.md
  - docs/lore-research/section-d-nightmares.md
  - docs/lore-research/section-e-soul-cores-corruption-echoes.md
  - docs/lore-research/section-f-vocabulary-world-texture.md
  - brain/design/combat-v1.md
  - brain/design/deferred-scope.md
  - brain/lore/chainback.md
  - brain/evidence/20260821T074514Z--claude--modpack-combat-core-closure.md
  - "Shadow Slave chapters 1, 2, 4, 15, 54, 74, 218, 354, 743, 778, 972, 1306, 1822, 1825, 1827, 2029, 2031-2033, 2048, 2061, 2397, 2412, 2444, 2834, 2902, and 3012"
related:
  - ss-context-lore-research
  - ss-lore-index
  - ss-design-combat-v1
  - ss-lore-chainback
  - ss-implementation-index
  - 20260821T074514Z-claude-modpack-combat-core-closure
supersedes: []
tags:
  - evidence
  - modpack
  - packaging
  - lore
  - snapshot
---

# Evidence — Modpack and Shadow Slave findings

> Scaffold: `python brain/tools/new_record.py evidence --agent <slug> --slug <short-slug> --task-id <id>`

## Claim tested

The available modpack implementation facts and existing Shadow Slave lore
findings can be captured in one durable vault snapshot without mistaking
package-format tests for a playable release, wiki navigation for canon, or
Minecraft design for novel mechanics.

## Environment and method

- Workspace: `<repository root>`
- Branch at final capture: `packaging/combat-core-closure`
- Captured HEAD: `8c32d355f0397b89ab6c0553b8f3612fb992f474`
- Worktree: dirty, with 80 pre-existing entries before this claim
- Python: bundled Codex workspace runtime because `python` was not on `PATH`
- Lore method: bounded `lore-research` context packet, mandatory source-policy
  and Java-alignment reads, then synthesis of the existing chapter-backed A-F
  research ledgers. No chapter prose was copied.
- Modpack method: current manifest, package tools, tests, workflows, generated
  Shadow Slave metadata, local JARs, and the latest local GameTest log were
  inspected directly. Current source was used instead of the two stale derived
  implementation snapshots reported by the vault brief.
- Concurrent state: the pass began at
  `vault/multi-ai-brain@acf4ed5fda81`. The shared branch advanced and switched
  while work was in progress. Claude then claimed and completed the declared
  Combat Core closure, committed as
  `packaging/combat-core-closure@8c32d355f039`; this snapshot re-ran the package
  checks against that post-fix state.

## Preconditions

`brain/tools/agent_brief.py --paths brain/ai/claims brain/evidence
brain/ai/handoffs` exited 0 before writing. It reported 77 notes, zero errors,
no active claims or path overlaps, and two pre-existing stale-snapshot warnings
for `brain/implementation/authority-drift-register.md` and
`brain/implementation/current-snapshot.md`.

During the pass, claim
`20260821T073922Z-claude-modpack-combat-core-closure` overlapped the read-only
modpack inspection. Its targets were not edited here. This evidence records the
pre-fix reproduction and the stable post-fix result after Claude closed that
claim.

## Command or research procedure

### Repository and package checks

The bundled Python interpreter was used for these exact commands:

```powershell
& "C:\Users\spud0\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" modpack/tools/validate_manifest.py
& "C:\Users\spud0\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" -m unittest discover -s modpack/tests -v
```

Initial observed output before the concurrent closure fix:

```text
OK: modpack/manifest.json
Ran 33 tests in 0.389s
OK
```

The required runtime mod IDs in the current local Shadow Slave JAR were then
compared with the state owner and components declared by the pack manifest.
The exact result was:

```text
RequiredByCore  : combat_core, geckolib, smartbrainlib
DeclaredByPack  : shadowslave, geckolib, smartbrainlib
MissingFromPack : combat_core
```

After the closure fix settled, these exact checks were re-run:

```powershell
& "C:\Users\spud0\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" modpack/tools/validate_manifest.py
& "C:\Users\spud0\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" modpack/tools/check_dependency_closure.py
& "C:\Users\spud0\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe" -m unittest discover -s modpack/tests -v
```

Post-fix result:

```text
OK: modpack/manifest.json
OK: all 5 required dependencies are covered.
Ran 50 tests in 0.234s
OK
```

A source-boundary search was also run:

```powershell
rg -n "dev\.spud\.shadowslave" combat-core/src
```

It returned no match. `combat-core` therefore does not currently import the
Shadow Slave package in the searched source tree.

### Lore research procedure

1. Read `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`,
   `brain/lore/index.md`, and `docs/lore-research/README.md`.
2. Read the findings, corrections, contradictions, design consequences, and
   open questions in research Sections A-F and the two Section A verification
   addenda.
3. Cross-check project-specific labels in `docs/PREVIEW-LORE-DECISIONS.md`,
   `brain/lore/chainback.md`, `brain/design/combat-v1.md`, and
   `brain/design/deferred-scope.md`.
4. Retain a CANON label only where the vault ledger names chapter evidence;
   treat unverified wiki-only material as UNKNOWN/research lead, not CANON.
5. Record conflicts and late corrections rather than selecting whichever rule
   is easiest to implement.

## Observed result

## A. Modpack and module findings

### A1. What exists now

- **OBSERVED:** The repository contains a real development packaging shell,
  despite the root README still saying that the modpack is design-only with no
  manifest. It includes a manifest, deterministic builder, archive verifier,
  external-provenance tool, dependency-closure check, workflow, and 50 passing
  unit tests.
- **OBSERVED:** `modpack/manifest.json` identifies pack version `0.0.0-dev` for
  Minecraft `1.21.1`, NeoForge `21.1.244`, and Java 21. It is a repository ZIP
  format, not a Modrinth `.mrpack` or public release.
- **OBSERVED:** The local Java line is Shadow Slave `0.1.0-preview.4` plus the
  separate `combat_core` `0.0.4-wip` JAR. The Shadow Slave metadata requires
  Combat Core, GeckoLib `4.9.2`, and SmartBrainLib `1.16.11` on both sides.
- **OBSERVED:** The post-fix manifest now declares all three required non-platform
  dependencies, including Combat Core as a first-party `local_gradle_build`
  component packaged at `mods/combat-core.jar`.
- **OBSERVED:** GeckoLib is active presentation infrastructure for current
  creature models/animation. SmartBrainLib is active execution infrastructure
  for the Ash Burrower. They are not merely hypothetical dependency candidates.
- **COMPATIBILITY:** `shadow-slave.jar` owns canonical Shadow Slave identity,
  progression, Nightmare lifecycle, ownership, persistence, migration, and
  provider contracts. Combat Core owns generic action execution; GeckoLib owns
  replaceable presentation; SmartBrainLib owns replaceable brain execution.
  Provider removal must not reroll or make canonical state undecodable.
- **OBSERVED:** The dependency direction currently holds in the inspected
  source: Shadow Slave consumes Combat Core, while `combat-core/src` contains no
  `dev.spud.shadowslave` import.

### A2. What the package tooling proves

- **OBSERVED PASS:** The current manifest validates.
- **OBSERVED PASS:** The closure checker accounts for all five required mod IDs:
  NeoForge and Minecraft through the platform, then Combat Core, GeckoLib, and
  SmartBrainLib through declared components.
- **OBSERVED PASS:** All 50 package, closure, archive, provenance, and
  workflow-contract unit tests passed against the post-fix worktree.
- **OBSERVED:** The builder requires declared components, checks their exact
  SHA-256 values for remote inputs, accepts a declared first-party local Gradle
  artifact whose built digest is recorded in generated provenance, rejects
  missing/unknown inputs and path collisions, writes sorted fixed-metadata ZIP
  entries, embeds payload sizes and hashes, and uses atomic output replacement.
- **OBSERVED:** The verifier checks unique safe paths, fixed archive metadata,
  complete embedded-provenance coverage, byte lengths, and hashes.
- **OBSERVED:** External provenance can bind a repository, full source commit,
  workflow run and attempt, package artifact identity, package digest, and core
  digest. It remains unsigned and is not publisher authenticity, source review,
  or cross-toolchain reproducibility proof.

These passes prove the declared dependency closure and internal package-format
contract with supplied bytes. They do not prove version-range compatibility or
that the current preview is bootable.

### A3. Closure transition and remaining verification gaps

1. **OBSERVED CLOSED IN THE CURRENT WORKTREE — declared dependency closure.**
   The initial manifest omitted required `combat_core 0.0.4-wip`; the defect was
   reproduced before the fix. Claude's overlapping claim added Combat Core,
   first-party local-build packaging support, and a fail-closed closure check.
   The post-fix checker exits 0 and the suite increased from 33 to 50 passing
   tests. This closes declared closure only; it is not a boot result.
2. **OBSERVED OPEN P0 — fixture-only CI.** `modpack-shell.yml` writes the literal
   bytes `ci-core-fixture` as `shadowslave-core.jar`, assembles that fixture,
   and verifies ZIP integrity. It does not build the real Shadow Slave or Combat
   Core JARs and does not boot the extracted pack.
3. **OBSERVED — incomplete trigger coverage.** Modpack-shell watches only
   `modpack/**` and itself. Preview gates watch `mod/**`, datapack/test paths,
   and their own workflow, but not `combat-core/**`. A Combat Core change can
   therefore miss both pack-closure and preview-gate execution.
4. **OBSERVED — GameTest gap in CI.** The combined local Gradle configuration
   enables Shadow Slave and Combat Core GameTests, while `java-core.yml` runs
   build, client smoke, and a server-restart smoke without `runGameTestServer`.
5. **OBSERVED — no release-grade assembled artifact.** No `*modpack*.zip` was
   present under the workspace `build/`. This pass found no extracted-pack
   client/server boot, clean client handshake, `.mrpack`, signature, public
   release, or cross-toolchain reproducibility evidence.

### A4. Useful local evidence, with limits

Two current dirty-worktree JARs exist:

| Artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| `mod/build/libs/shadowslave-0.1.0-preview.4.jar` | 521369 | `DD5821C105D76AB180D60B1C4B41C932501967763CA8E1A28A1ECFF40F2A273F` |
| `combat-core/build/libs/combat_core-0.0.4-wip.jar` | 103893 | `2A66C310195C4F179448AD553887AAA80044B713C6800EA66895EB04012EF326` |

`mod/run/logs/latest.log` records a local development GameTest server loading
Combat Core, GeckoLib, Shadow Slave preview.4, and SmartBrainLib, followed by:

```text
2 GAME TESTS COMPLETE IN 1.451 s
All 2 required tests passed :)
```

That pre-existing log is useful composite-development evidence. It is not proof
that a standalone package assembled from the manifest boots, that a fresh client
can join it, or that its artifact bytes came from reviewed/reproducible source.
The same log warns that its reused world moved Combat Core from `0.0.3-wip` to
`0.0.4-wip` and Shadow Slave from preview.3 to preview.4.

### A5. Documentation drift worth preserving

- **OBSERVED:** Root `README.md` says no manifest or dependency package exists;
  current source contradicts the first half. The accurate state is that package
  infrastructure and declared closure exist, but no real-JAR, boot-tested
  release exists.
- **OBSERVED:** `docs/THIRD-PARTY-DEPENDENCY-POLICY.md` still says the manifest
  has no components and SmartBrainLib is only spike-approved. Current manifest,
  metadata, and runtime source require/use it.
- **OBSERVED:** `modpack/README.md` still shows a preview.2 core filename and an
  archive inventory with no Combat Core.
- **OBSERVED:** `PROJECT-STATUS.md` combines an August 13 baseline header with
  current local preview.4 edits. It is current local direction, not release
  provenance for the dirty-worktree artifacts above.

## B. Shadow Slave lore findings already supported in the vault

This section is a digest of existing research, not a newly promoted systems
bible. Chapter numbers identify the underlying evidence recorded in Sections
A-F; consult those ledgers for source links, exceptions, and fuller chronology.

### B1. Aspects and Flaws

- **CANON:** An Aspect belongs to the person. The Nightmare Spell can reveal or
  unseal it but does not create the underlying nature. Natural Awakening proves
  the distinction (Chapter 2029).
- **CANON:** The First Nightmare can matter to Aspect realization or evolution,
  and Aspect Rank can exceptionally evolve (Chapters 15, 218, 2029, 2291).
- **UNKNOWN:** Canon supplies no deterministic weighting among personality,
  affinities, choices, assigned role, trial action, fate deviation, kills, or
  randomness. A loot-table or scoring formula must remain DESIGN.
- **CANON:** Aspect Rank, Soul Rank, Aspect Abilities, Innate Ability, Aspect
  Legacy, and Legacy Relics are distinct concepts. `Shadow Bond` is Sunny's
  Innate Ability, not his Flaw (Chapter 15).
- **CONTRADICTION / late correction:** Chapter 218 presents every Aspect as
  containing a Legacy. Chapter 2902 says Cassie and Mordret have none and states
  that the Aspect is natural while the Aspect Legacy comes through the Spell.
  The unqualified early universal is therefore incomplete. A corresponding
  Spell-mediated latent pattern is an **INFERRED** reconciliation, not CANON.
- **UNKNOWN:** Whether a natural Awakened entirely outside the Spell can obtain
  a true Aspect Legacy is not established by the checked research.
- **CANON:** Flaws express the law of imperfection, are intrinsic to a person,
  and are tied to identity, nature, power, choices, attachments, or fate rather
  than being random negative effects (Chapters 2031-2033).
- **CANON:** Flaws can be worked around; no universal cure/removal method is
  established. Weaver's Mask reverses a Flaw rather than deleting it (Chapters
  778, 2033, 2865).
- **UNKNOWN:** Most formal Aspect and Flaw names and many exact supernatural
  rules remain unshown. A title or wiki label is not a formal rune name. In
  particular, `Soul Reaper` is Jet's title, not a verified Aspect name.

### B2. Ranks, progression, cores, and Attributes

- **CANON:** The Soul Rank ladder is Dormant, Awakened, Ascended,
  Transcendent, Supreme, Sacred, and Divine (Chapters 15 and 352).
- **CANON:** Mundane/Uninfected, Carrier, Aspirant, Dreamer/Sleeper, and the
  human rank titles are progression/status concepts, not one numeric rank axis.
  Soul Rank, human title, Aspect Rank, and creature Class must remain separate.
- **CANON:** Surviving the First Nightmare produces a Dormant Dreamer/Sleeper;
  it does not make a normal Carrier Awakened. Actual Awakening follows the first
  successful Dream Realm journey and return through a Gateway (Chapters 15 and
  354).
- **CANON:** Spell-assisted and natural Awakening are distinct paths. The
  natural path senses, controls, refines, and condenses essence without relying
  on the Spell's trial automation (Chapters 1306, 1822, 1825, 2029).
- **CANON:** A Soul Core is an essence nexus within the soul, not the soul
  itself. Normal humans have one core; multiple cores and Sunny's Shadow
  Fragments are exceptional Aspect-specific mechanics (Chapters 34, 352, 1306).
- **CANON:** Soul Shards strengthen or saturate a core and its capacity. They do
  not automatically promote Soul Rank, and absorbing whole Soul Cores is not the
  ordinary human progression loop (Chapters 51 and 1306).
- **CONTRADICTION / UNKNOWN:** Section B records Aspirant Sunny's runes already
  displaying a Dormant core, while Section E describes Spell-assisted core
  formation/solidification after First-Nightmare completion. Preserve the
  wording conflict pending a focused primary-text reconciliation.
- **CANON:** Attributes are named supernatural traits or affinities, not a
  universal STR/DEX/Vitality character sheet (Chapters 2-3 and later status
  examples).
- **UNKNOWN:** No universal `Divine Sight` mechanic has been verified for all
  Awakened. Information reading is ability-specific; Cassie's progressive rune
  sight is the major canonical example.

### B3. Memories

- **CANON:** Memories are Woven artifacts that can exist as soul essence, be
  stored in the Soul Sea, and be summoned into matter. Their foundational weave
  supports soul linkage, manifestation, and ordinary repair.
- **CANON:** Memory Rank and Tier are separate dimensions. Rank tracks essence
  quality/source Rank; Tier reflects structural capacity and is not simply the
  exact number of enchantments (Chapters 15 and 261; the latter formulation is
  partly an **INFERRED** synthesis).
- **CANON:** Ordinary Memories are transferable by deliberate physical-contact
  transfer. They are soul-stored, not universally non-transferable soulbound
  gear (Chapters 54 and 74).
- **CANON:** True Bound/Soulbound relics are exceptional, complete destruction
  can be permanent, and crafted or specially authored Memories can be modified,
  elevated, grow, or scale through distinct mechanisms (Chapters 972, 1342,
  2048, and 2061).
- **UNKNOWN:** No universal numeric Memory capacity or slot limit is established.
  Nor is summoned ownership ordinary Minecraft pickup-loot behavior.

### B4. Nightmares, Seeds, Gates, and creatures

- **CANON:** A Nightmare reconstructs a historical situation around a central
  conflict. The universal completion rule is conflict resolution, not killing a
  designated boss (Chapters 1, 4, 14, 459, and 743).
- **CANON:** First Nightmares are individual, with a Seed inside the infected
  person's soul and an assigned historical role/body. Later Seeds exist in the
  Dream Realm and can admit multiple challengers, possibly on opposing sides.
- **CANON:** Ordinary First-Nightmare failure means death and a small Gate
  through the corpse; later-Nightmare death is also real. Safe low-health
  ejection/retry is not the canonical default (Chapters 1 and 2761).
- **CANON:** An unresolved later Seed can bloom into a Nightmare Gate. Killing
  an initial wave or guardian is not necessarily the same as conquering the
  Seed (Chapters 459 and 1738).
- **CANON:** Appraisal attends to the challenger's deeds and apparent deviation
  from expected fate, not only survival or kill count. The full scoring rule is
  **UNKNOWN** (Chapter 743).
- **CANON:** Six Nightmares advance through the known Soul Rank ladder. Late
  canon reveals a Seventh Nightmare tied to the Forgotten God; it is not an
  established eighth Soul Rank (Chapters 2313, 2412, and 2444).
- **CANON:** Nightmare Creature Rank and Class are independent axes. Class is
  qualitative: intelligence, powers, authority, minions, and environmental
  influence change rather than merely adding health.
- **CANON:** Nightmare Gates, Dream Gates, and Gateways are distinct mechanisms;
  `Gate` should not be used as an unqualified synonym for every portal.
- **UNKNOWN:** Exact challenger caps, complete Fourth/Sixth/Seventh Nightmare
  procedure, post-Seventh progression, post-conquest Seed lifecycle, and a
  universal Gate-category formula remain unresolved.

### B5. Corruption and Echoes

- **CANON:** Corruption is Void influence on beings of the created world and
  can spread through exposure, tainted phenomena, or forbidden knowledge
  (Chapters 2397 and 2834).
- **CANON:** Death can cleanse Corruption, and successful natural Awakening can
  cleanse the modest Seed within a person. Exceptional immunities or multi-core
  surgery are not a general cure (Chapter 1827).
- **CANON:** Echoes are soulless summonable replicas, not captured souls or
  defeated shadows. They can obey commands, retain meaningful capabilities, be
  transferred, and be permanently destroyed.
- **CANON:** Ordinary Echoes are static replicas. Sunny's growth-capable Shadows
  are a separate Aspect-specific system, and his seven-Shadow relationship is
  not a universal Echo limit.
- **UNKNOWN:** No universal small ownership cap, complete transfer ritual,
  drop-award formula, or general growth route for ordinary Echoes is established.

### B6. Vocabulary, presentation, and world texture

- **CANON:** Carrier, Aspirant, Dreamer, Sleeper, Awakened, Master, Saint,
  Sovereign, Seed, Nightmare Gate, Gateway, Dreamscape, Soul Sea, Aspect,
  Flaw, Attribute, Memory, and Echo carry distinct setting meanings. Overloaded
  substitutes erase real mechanics.
- **CANON:** The Spell commonly uses concise bracketed notices and structured
  runes; appraisal can shift into mythic narration with flashes of dry cruelty
  (Chapters 15 and 743).
- **INFERRED:** Runes communicate supernatural meaning through an interface and
  need not be treated as a rigid English localization grammar.
- **INFERRED:** The Dream Realm has no universal dark-purple palette. Its regions
  differ radically in geography, material, traversal, ecology, and hazard.
  Aspect imagery is also individual rather than one shared aura system.
- **INFERRED:** `sublime first, horrifying second` is the strongest compact art
  direction supported by the surveyed regions; it is not a canonical law.
- **CONTRADICTION / UNKNOWN:** Community material still lists Jet as Government
  leader, while Chapter 3012 records her resignation. Her post-war leadership
  and resignation are CANON; the exact current successor is UNKNOWN.

## C. Current project design and compatibility consequences

- **DESIGN / project authority:** Model a First Nightmare as role + historical
  situation + central conflict + terminal resolution, with appraisal after
  resolution. Crash/admin recovery is technical and must never be narrated as
  ordinary mercy from the Spell.
- **DESIGN:** The Last Signal, last watchkeeper, Last Light, Kindle, Cold Ash,
  its deterministic appraisal, terrain, and placeholder pursuer are Minecraft
  content. They are deliberately not presented as novel events or identities.
- **COMPATIBILITY:** Imported datapack identities persist, but the datapack's
  four Aspect roots, four Flaw families, boss/timer structure, global instance,
  first-sleep shortcut, and safe ejection are not canonical Java constraints.
- **DESIGN / project authority:** Combat v1 follows `Observe -> Respond -> Create
  an opening -> Commit -> Reassess`. The Chainback acceptance slice is one
  readable telegraph, a valid response, one authoritative result, one bounded
  recovery/opening, one expected punish, and normal resumption.
- **DESIGN:** Chainback itself, its appearance, exact classification, chained
  silhouette, climbing idea, strike/pull choreography, timings, and rewards are
  authored project content. Only the general Rank/Class and qualitative-creature
  constraints are canon-backed.
- **DESIGN / explicit defer:** Limb injuries, Soul damage, full Essence economy,
  advanced movement, universal dodge invulnerability, broad guard/parry and
  damage catalogues, whole-game Rank/Class balance, PvP/multiplayer hardening,
  and large content catalogues are not prerequisites for the Chainback slice.
- **COMPATIBILITY:** Broad regions, creatures, Memories, Echoes, and advanced
  identity generation should live behind coherent provider/module boundaries
  after the base owns stable identity and recovery. Updating or removing a
  provider must not reroll resolved state, strand players, mature an unwinnable
  threat, or award success because content vanished.

## D. Research hazards and unresolved work

- `WIKI` in the older research README is a navigation-confidence label, not an
  allowed replacement for project evidence classes. A wiki-only claim remains
  UNKNOWN until chapter-backed.
- Early and late explanations can genuinely differ. The Aspect Legacy and Soul
  Core timing cases above must remain visible until a focused primary pass
  reconciles them.
- Formal names must be separated from titles, wiki page labels, descriptions,
  and project IDs.
- Current political geography and office-holders are chronology-sensitive; do
  not freeze one arc's state into permanent runtime assumptions.
- The unavailable external `Shadow Slave - Lore Reference` mentioned by older
  notes was not treated as authority in this pass.
- `docs/lore-research/minecraft-implementation-brainstorm.md` is an unapproved
  proposal collection. Only the subset already accepted in
  `docs/JAVA-LORE-ALIGNMENT.md` and current owner-owned brain notes was treated
  as project authority.

## Artifacts, hashes, logs, or chapter references

- Local JAR identities are recorded in Section A4. They are dirty-worktree
  observations, not release checksums.
- Initial package verification: manifest valid; 33/33 tests passed; direct
  comparison exposed missing `combat_core`.
- Post-fix package verification: manifest valid; all 5 required dependency IDs
  covered; 50/50 tests passed.
- Claude's overlapping closure evidence records a deterministic fixture archive
  containing `mods/combat-core.jar`, verified with SHA-256
  `8a3c81228b0b8e8b13f8bfe2af1b07a51a2327b2434af1539a4c162e10ae3715`.
  It used fixture JARs and is not a runtime artifact.
- Local runtime log: `mod/run/logs/latest.log`, 2026-08-21 local GameTest run,
  four project/runtime mods loaded, 2/2 required GameTests passed.
- Decisive lore chapter clusters:
  - Aspects/Flaws: 15, 218, 778, 2029, 2031-2033, 2291, 2865, 2902.
  - Progression/cores: 2, 15, 34, 51, 352, 354, 1306, 1822, 1825.
  - Memories: 15, 54, 74, 261, 972, 1342, 2048, 2061.
  - Nightmares/Gates: 1, 4, 14, 459, 743, 1738, 2313, 2412, 2444, 2761.
  - Corruption: 1827, 2397, 2834.
  - Spell voice/current politics: 15, 743, 3012.
- Exact links and narrower claim-by-claim evidence remain in the Section A-F
  lore ledgers listed in frontmatter.

## Limitations and unperformed checks

- This pass did not browse or re-read the current live novel publication. Lore
  is a snapshot of the chapter-backed research already in the vault, and later
  publication may add or revise context.
- No external dependency was downloaded. The pinned GeckoLib and SmartBrainLib
  bytes/hashes were not independently fetched in this pass.
- The Shadow Slave or Combat Core Gradle builds were not rerun. Their local JARs
  and GameTest log pre-existed this task.
- A deterministic fixture archive was assembled and verified by the overlapping
  closure claim, but no pack was assembled from the current real JARs. No
  dedicated-server pack boot, separate client join, two-JVM test, launcher
  export, signing, publication, or clean-machine reproduction ran.
- No physical playtest occurred. Combat feel, telegraph readability, spacing,
  art quality, and fun remain human evidence.
- This claim did not edit code, manifests, workflows, shared authority notes,
  or lore ledgers. The overlapping Claude claim fixed declared closure only.
  Findings do not authorize implementation, release, or canon promotion.

## Conclusion

The vault now has a consolidated snapshot of both bodies of knowledge. The
modpack has meaningful deterministic packaging and provenance infrastructure,
and its declared dependency closure now includes Combat Core with a fail-closed
regression check. Preview.4 is still not a proven runnable modpack: CI packages
a fixture, version ranges are not evaluated by the closure check, and no archive
built from the real JARs has been booted. The lore research establishes strong
architecture constraints across progression, Aspects/Flaws, Memories,
Nightmares, Soul systems, Echoes, and presentation while preserving open
algorithms, source conflicts, and all Minecraft-authored content as DESIGN
rather than canon.

An observation proves behaviour at one commit. It does not authorize a design, merge, release, canon classification, or scope change.
