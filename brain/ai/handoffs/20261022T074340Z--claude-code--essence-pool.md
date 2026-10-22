---
uid: 20261022T074340Z-claude-code-essence-pool
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T074338Z-claude-code-essence-pool
created: 2026-10-22
updated: 2026-10-22
branch: claude/essence-pool
base_commit: 468b0f03f4da1a21a45452ce3c10e46293a4d4f0
head_commit: 468b0f03f4da1a21a45452ce3c10e46293a4d4f0
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Essence pool replaces ability cooldowns

## Owner intent and scope

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 1, the essence pool from Dormant on, with no cooldowns.

## Outcome

ability/EssenceData, ability/EssenceService, the slot-based AbilityStatusPayload, the essence bar in client/AbilityHudLayer.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled an Aspect in a fresh world, cast Kindle twice and screenshotted the HUD and the Abilities tab. build: BUILD SUCCESSFUL. Game tests: "All 11 required tests passed :)" (Kindle now asserts two casts fit a full pool and the third is DRAINED). Headless client: the essence bar shows full with the cost tick, then partly refilled after the casts; the Abilities tab reads "Key R · Costs 35 essence · Ready". Details in [[20261022T074339Z--claude-code--essence-pool]].

## Evidence and artifacts

## Unperformed checks

No human playtest. Balance numbers are first guesses.

## Known risks

Old saves carry an unknown ability_cooldowns attachment; NeoForge logs it once and drops it.

## Lore classifications

DESIGN: gameplay numbers and mechanics are design choices, not canon.

## Explicitly deferred

Shards, core saturation, Awakening, refinement, intention offering (later PRs in this build).

## Next safe action


