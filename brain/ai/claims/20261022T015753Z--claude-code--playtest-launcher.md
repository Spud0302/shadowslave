---
uid: 20261022T015753Z-claude-code-playtest-launcher
record_kind: claim
authority: context
lore_class: "N/A"
state: active
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T015753Z-claude-code-playtest-launcher
created: 2026-10-22
updated: 2026-10-22
branch: claude/playtest-launcher
worktree: primary
base_commit: c1a7ae2b9d4f18af61e45213ff16f70fb4831c2a
lease_until: 2026-10-22T09:57:53Z
targets:
  - play.sh
  - play.bat
  - play.command
  - PLAYTEST.md
  - README.md
  - mod/build.gradle
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — One-step playtest launcher

> Scaffold: `python brain/tools/new_record.py claim --agent <slug> --slug <short-slug>`

## Owner intent

Jack (2026-10-22): a quick way to boot Minecraft with the right version of the mod loaded, to look at visuals.

## Exact scope

Root launch scripts wrapping `mod/gradlew -p mod runClient`, with optional branch switch, pull and quick-play world; a `quickPlayWorld` Gradle property on the client run; PLAYTEST.md and a README pointer.

## Acceptance criteria

One command on Windows, macOS or Linux builds the checked-out branch and opens the client with NeoForge, GeckoLib, SmartBrainLib and PAL loaded.

## Target paths

## Explicit exclusions

No gameplay code, no modpack manifest changes, no Combat Core changes.

## Dependencies and overlaps

## Coordination notes

## Closure

State whether the claim closed, expired, or blocked. Link evidence and the immutable handoff. Claim closure is not proof of success.
