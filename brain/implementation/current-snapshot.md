---
uid: ss-implementation-snapshot-20260821
record_kind: implementation
authority: evidence
lore_class: "N/A"
state: active
owner: Codex
created: 2026-08-21
updated: 2026-08-21
captured_commit: 7223e140d625
worktree_dirty: true
sources:
  - PROJECT-STATUS.md
  - docs/CURRENT-PREVIEW-SUMMARY.md
  - mod/gradle.properties
  - combat-core/gradle.properties
tags:
  - implementation
  - snapshot
---

# Captured local snapshot — 2026-08-21

> [!warning]
> This is derived evidence from a dirty local worktree, not a release claim. Refresh Git and the current authority documents before relying on it.

## Capture

- Branch: codex/combat-core-standalone
- Head: 7223e140d625
- Worktree: dirty, with substantial existing Combat Core and Chainback work
- Minecraft: 1.21.1
- NeoForge: 21.1.244
- Java: 21
- Shadow Slave property version: 0.1.0-preview.4
- Combat Core property version: 0.0.4-wip

## Documented local state

The current preview summary reports unit/build and world-level GameTest gates green for the local preview.4 plus Combat Core 0.0.4-wip pairing. Physical singleplayer combat feel and readability review remain pending.

This vault bootstrap did not rerun those runtime gates and therefore does not independently certify them.

## Architecture direction

- Shadow Slave owns canonical state and representative playable slices.
- Standalone Combat Core owns generic combat execution.
- Broad content should move behind coherent providers or WIP modules.
- Chainback is the active bounded consumer slice.

## Known health work

Read [[brain/implementation/authority-drift-register]]. The highest-priority recorded gap is dependency-complete assembled-pack verification, including Combat Core, followed by automated GameTest and real packaged client/server smoke coverage.

