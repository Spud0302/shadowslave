---
uid: 20261022T024955Z-claude-code-ability-framework
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T024952Z-claude-code-ability-framework
created: 2026-10-22
updated: 2026-10-22
branch: claude/ability-framework
base_commit: 6440d2a3708edb54668280c814abef5c6a841b80
head_commit: 6440d2a3708edb54668280c814abef5c6a841b80
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Aspect ability framework

## Owner intent and scope

Jack (2026-10-22) agreed the abilities rework plan. This claim is build-order step 2: one ability key, server-side cooldowns, the combat hook and action-bar feedback, with Kindle ported onto it.

## Outcome

New ability package with AspectAbility, AbilityRegistry, AbilityCooldownData (attachment ability_cooldowns), AbilityService, KindleAbility, combat/AbilityCombat and VanillaAbilityCombat, plus AbilityGameTest. PreviewPowerService keeps only the Cold Ash tick.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod build` and `./mod/gradlew -p mod runGameTestServer`. build: BUILD SUCCESSFUL, 253 unit tests, 0 failures (AbilityServiceTest 6). Game tests: "All 6 required tests passed :)", including Kindle igniting a zombie but not a pig, reporting RECOVERING on a second press, and NO_ASPECT for a player without an Aspect. Details in [[20261022T024954Z--claude-code--ability-framework]].

## Evidence and artifacts

## Unperformed checks

No human playtest; the client key path is covered only by the existing source-text test.

## Known risks

Clients and servers on network version 3 can no longer connect to each other; both must update. Old Kindle cooldowns stored in preview_power are ignored.

## Lore classifications

DESIGN: gameplay numbers and mechanics are design choices, not canon.

## Explicitly deferred

Mist Passage, Holdfast, Returning Tide, the innate rules and Flaws (step 3); HUD cooldown sweep (step 5).

## Next safe action
