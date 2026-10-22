---
uid: 20261022T024240Z-claude-code-dream-candle-roll
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T023123Z-claude-code-dream-candle-roll
created: 2026-10-22
updated: 2026-10-22
branch: claude/dream-candle-roll
base_commit: b4ccf430038bdb5b9bdef3556ead2169c7afe146
head_commit: b4ccf430038bdb5b9bdef3556ead2169c7afe146
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Dream Candle ritual and roll

## Owner intent and scope

Build-order step 1 of the abilities rework plan: Dream Candle ritual and Aspect roll.

## Outcome

New package `awakening` (kit catalogue, roller, playstyle evidence, roll service, ritual, game test), `block` package with the Dream Candle, `SoulTransitions.awakenThroughRitual`, an operator reroll command, and a game-test CI step.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod build`: BUILD SUCCESSFUL, 249 tests, 0 failures. `./mod/gradlew -p mod runGameTestServer`: "All 4 required tests passed :)". `./mod/verify-smoke.sh server-restart`: "SMOKE GATE PASSED". Details in [[20261022T024241Z--claude-code--dream-candle-roll]].

## Evidence and artifacts

## Unperformed checks

Human playtest of the ritual and textures.

## Known risks

Ability descriptions in the reveal promise Mist Passage, Holdfast and Returning Tide, which do nothing until build step 3. Players who already hold a First-Nightmare Aspect keep it.

## Lore classifications

DESIGN: the Dream Candle ritual and the playstyle weighting are not canon.

## Explicitly deferred

Ability framework, the other three kits, the reveal overlay, HUD and Soul screen, visual polish.

## Next safe action
