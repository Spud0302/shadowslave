---
uid: 20261022T112901Z-claude-code-soul-refinement
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T112859Z-claude-code-soul-refinement
created: 2026-10-22
updated: 2026-10-22
branch: claude/soul-refinement
base_commit: 933bd3a8ad3ec8fc57e5cb9be9fca6bfe406355d
head_commit: 933bd3a8ad3ec8fc57e5cb9be9fca6bfe406355d
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Refinement points and respec in the Soul Sea

## Owner intent and scope

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 5, refinement points in Body, Soul and Aspect, spent in the Soul Sea, with a respec that costs a point.

## Outcome

RefinementData, RefinementRules, RefinementService, RefinementStat, RefineRequestPayload and the Soul Sea Refine page.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled Mist, Awakened with Veil, meditated at a lit candle, opened the Refine page and placed a Body point. Unit tests pass (new RefinementRulesTest). Game tests: "All 36 required tests passed :)" (three new: a Body point adds a heart and a respec takes it back and loses the point, Soul and Aspect points grow the pool to 215 and cut Kindle to 33 essence on the HUD, and refining away from the candle does nothing). Screenshots show the Rise page after Awakening and the Refine page with 3 points to place, Body at 1 and the Respec button. Details in [[20261022T112900Z--claude-code--soul-refinement]].

## Evidence and artifacts

## Unperformed checks

No human playtest; the per-point numbers are first guesses. Aspect points do not lengthen ability durations yet.

## Known risks

Body speed stacks multiplicatively with sprint and potions like any base-speed modifier.

## Lore classifications

DESIGN: gameplay numbers and mechanics are design choices, not canon.

## Explicitly deferred

Intention offering; duration scaling from Aspect points.

## Next safe action


