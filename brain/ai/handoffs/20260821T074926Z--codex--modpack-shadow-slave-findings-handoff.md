---
uid: 20260821T074926Z-codex-modpack-shadow-slave-findings-handoff
record_kind: handoff
authority: context
lore_class: mixed
state: closed
owner: codex
task_id: 20260821T073644Z-codex-modpack-shadow-slave-findings-dump
created: 2026-08-21
updated: 2026-08-21
branch: packaging/combat-core-closure
base_commit: 8c32d355f0397b89ab6c0553b8f3612fb992f474
head_commit: 8c32d355f0397b89ab6c0553b8f3612fb992f474
worktree_dirty: true
sources:
  - brain/evidence/20260821T073736Z--codex--modpack-shadow-slave-findings-evidence.md
  - brain/evidence/20260821T074514Z--claude--modpack-combat-core-closure.md
related:
  - 20260821T073736Z-codex-modpack-shadow-slave-findings-evidence
  - 20260821T074514Z-claude-modpack-combat-core-closure
supersedes: []
tags:
  - multi-ai
  - handoff
  - modpack
  - lore
---

# Handoff — Modpack and Shadow Slave findings dump

> Scaffold: `python brain/tools/new_record.py handoff --agent <slug> --slug <short-slug> --task-id <id>`

## Owner intent and scope

Andrew asked for the available modpack knowledge and Shadow Slave lore findings
to be made durable in the Obsidian vault. This was a documentation/evidence task:
inspect current source, preserve the existing chapter-backed research, classify
claims, and leave a handoff without changing runtime code or promoting design to
canon.

## Outcome

Created one consolidated immutable evidence snapshot:

[[brain/evidence/20260821T073736Z--codex--modpack-shadow-slave-findings-evidence]]

It records:

- the real development packaging shell and module/provider ownership boundaries;
- deterministic archive/provenance behavior and exact local test results;
- the initially reproduced Combat Core closure defect and the concurrent fix
  committed at `8c32d355f039`;
- the still-open fixture-only CI and assembled-pack boot gap;
- local dirty-worktree JAR hashes and the existing composite GameTest log;
- chapter-backed findings for Aspects/Flaws, ranks/progression, Memories,
  Nightmares/Gates/creatures, Soul Cores/Corruption/Echoes, vocabulary, Spell
  presentation, and world texture;
- project DESIGN and COMPATIBILITY consequences; and
- contradictions, UNKNOWNs, stale-source hazards, and unperformed checks.

No lore authority was promoted. No claim is made that the modpack boots or is a
release.

## Files changed

- `brain/ai/claims/20260821T073644Z--codex--modpack-shadow-slave-findings-dump.md`
- `brain/evidence/20260821T073736Z--codex--modpack-shadow-slave-findings-evidence.md`
- `brain/ai/handoffs/20260821T074926Z--codex--modpack-shadow-slave-findings-handoff.md`

The overlapping Claude task changed and committed the modpack closure; its
paths and proof are documented separately in
[[brain/evidence/20260821T074514Z--claude--modpack-combat-core-closure]]. This
claim did not edit those files.

## Acceptance criteria

Met:

- exact branch, commit, dirty state, commands, outputs, local artifact hashes,
  source paths, and limitations are recorded;
- material lore statements are classified, and CANON summaries point to chapter
  evidence in the existing ledgers;
- the note distinguishes declared closure, package-format proof, fixture archive
  proof, composite development GameTests, real-JAR pack boot, and public release;
- contradictions and UNKNOWNs remain visible; and
- the final vault validator exited 0.

## Verification performed

Exact package commands and results:

```text
<bundled-python> modpack/tools/validate_manifest.py
  -> OK: modpack/manifest.json

<bundled-python> modpack/tools/check_dependency_closure.py
  -> OK: all 5 required dependencies are covered.

<bundled-python> -m unittest discover -s modpack/tests -v
  -> Ran 50 tests in 0.234s; OK

rg -n "dev\.spud\.shadowslave" combat-core/src
  -> no match
```

Before the concurrent fix, this pass also ran the original 33-test suite and
compared the local preview.4 JAR metadata with the manifest, producing:

```text
RequiredByCore  : combat_core, geckolib, smartbrainlib
DeclaredByPack  : shadowslave, geckolib, smartbrainlib
MissingFromPack : combat_core
```

Final vault verification after closing the claim and creating this handoff:

```text
<bundled-python> brain/tools/validate_vault.py
  -> 83 note(s) checked, 0 error(s), 2 warning(s)
```

The two warnings are pre-existing stale snapshots at commit `7223e140d625`:
`brain/implementation/authority-drift-register.md` and
`brain/implementation/current-snapshot.md`.

State the exact command and its exact result. A claim that something passed, without the command and output, is not verification.

## Evidence and artifacts

- Primary consolidated snapshot:
  [[brain/evidence/20260821T073736Z--codex--modpack-shadow-slave-findings-evidence]]
- Concurrent declared-closure proof:
  [[brain/evidence/20260821T074514Z--claude--modpack-combat-core-closure]]
- Local Shadow Slave preview.4 JAR: 521369 bytes, SHA-256
  `DD5821C105D76AB180D60B1C4B41C932501967763CA8E1A28A1ECFF40F2A273F`
- Local Combat Core 0.0.4-wip JAR: 103893 bytes, SHA-256
  `2A66C310195C4F179448AD553887AAA80044B713C6800EA66895EB04012EF326`
- Existing `mod/run/logs/latest.log`: four runtime/project mods loaded and 2/2
  required GameTests passed in the composite development environment.

## Unperformed checks

- No fresh live-novel/web verification; lore was synthesized from existing
  chapter-backed vault research.
- No dependency downloads or independent GeckoLib/SmartBrainLib hash check.
- No Gradle rebuild in this claim.
- No archive from the current real JARs, extracted-pack server boot, separate
  client join, two-JVM pack test, launcher export, signature, publication, or
  clean-machine reproduction.
- No physical combat or art/readability review.

## Known risks

- The closure checker compares mod IDs but does not evaluate version ranges.
- CI still packages a literal fixture and never boots the archive.
- Local JARs and the GameTest log are dirty-worktree observations, not release
  provenance.
- Several top-level/third-party/modpack documents describe older package or
  preview states; current source and this evidence snapshot outrank them only as
  observations, not as authority edits.
- Later novel chapters may clarify or overturn the summarized lore; focused
  implementation work must still perform its own current primary-text check.

## Lore classifications

- **CANON:** only chapter-backed mechanics summarized from the existing ledgers.
- **INFERRED:** reconciliations and art/design syntheses derived from canon.
- **DESIGN:** all Minecraft scenarios, identities, algorithms, timing, content,
  UI choices, and packaging infrastructure not supplied by the novel.
- **UNKNOWN:** unresolved algorithms, formal names, caps, higher-Nightmare rules,
  contradictions, and chronology-sensitive current state.
- **COMPATIBILITY:** Java-owned canonical state, imported identity preservation,
  and replaceable provider/executor/presentation boundaries.

## Explicitly deferred

- Fixing fixture-only CI or booting a real assembled pack.
- Reconciling stale status/dependency documents.
- Changing runtime code, content, lore ledgers, shared indexes, or accepted
  project authority.
- Resolving Aspect-Legacy and Soul-Core-timing contradictions without a focused
  primary-text pass.
- Expanding Chainback combat beyond its accepted narrow slice.

## Next safe action

For packaging, build the exact Shadow Slave and Combat Core JARs from one known
commit, assemble them with the pinned providers, verify and extract the archive,
then boot a dedicated server and separate client. Only after that should the CI
fixture be described as a runtime gate.

For lore-sensitive implementation, start from the consolidated snapshot but
re-open the relevant Section A-F ledger and verify the decisive chapter evidence
and later clarification for the one mechanic being changed.
