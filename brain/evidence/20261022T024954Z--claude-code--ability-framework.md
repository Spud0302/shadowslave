---
uid: 20261022T024954Z-claude-code-ability-framework
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T024952Z-claude-code-ability-framework
created: 2026-10-22
updated: 2026-10-22
branch: claude/ability-framework
captured_commit: 6440d2a3708edb54668280c814abef5c6a841b80
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Aspect ability framework

## Claim tested

Pressing the ability key runs the player's highest-rank activatable ability on the server, starts its cooldown, reports progress on the action bar, and routes harm through AbilityCombat.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/ability-framework, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build` and `./mod/gradlew -p mod runGameTestServer`.

## Observed result

build: BUILD SUCCESSFUL, 253 unit tests, 0 failures (AbilityServiceTest 6). Game tests: "All 6 required tests passed :)", including Kindle igniting a zombie but not a pig, reporting RECOVERING on a second press, and NO_ASPECT for a player without an Aspect.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest; the client key path is covered only by the existing source-text test.

## Conclusion

The framework and ported Kindle behave as specified at this worktree state.
