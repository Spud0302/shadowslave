---
uid: 20261022T063538Z-claude-code-hud-soul-screen
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T063536Z-claude-code-hud-soul-screen
created: 2026-10-22
updated: 2026-10-22
branch: claude/hud-soul-screen
base_commit: cf8bd6b5d946ce0e85cf7241d3ab92e2692cd485
head_commit: cf8bd6b5d946ce0e85cf7241d3ab92e2692cd485
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Ability HUD slot and tabbed Soul screen

## Owner intent and scope

Jack (2026-10-22) agreed the abilities rework plan. This claim is build-order step 5: the ability slot left of the hotbar with a cooldown sweep, and a tabbed Soul screen in the Spell look on a translucent background.

## Outcome

client/AbilityHudLayer, ClientAbilityState, SoulPages, SpellStyle, AbilityIcons, rewritten SoulScreen; network/payload/AbilityStatusPayload; AbilityService.status and syncStatus; AspectAbility.primed.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that created a world, rolled an Aspect on the integrated server, clicked through the reveal, switched every Soul screen tab, pressed the ability twice and took screenshots. build: BUILD SUCCESSFUL, 269 unit tests, 0 failures (new: SoulPagesTest, ClientAbilityStateTest). Game tests: "All 11 required tests passed :)", with Returning Tide now also asserting the status is primed after the first press and shows the 500-tick cooldown after the return. Headless client: the full flow ran end to end (roll, typed reveal, Soul screen opened by the second click, four tabs, Holdfast used, HUD slot showed 30 then 28 seconds, Abilities tab showed Recovering). Details in [[20261022T063537Z--claude-code--hud-soul-screen]].

## Evidence and artifacts

## Unperformed checks

No human playtest. The primed glow and ready flash were not captured on screen (the run rolled Stone). Software rendering under xvfb showed some unrelated purple smears.

## Known risks

Identity changes outside the roll (resets, Nightmares) do not resend the status; the slot hides itself unless the Soul snapshot still lists the ability, so it cannot show a stale ability.

## Lore classifications

DESIGN: gameplay numbers and mechanics are design choices, not canon.

## Explicitly deferred

Polish (step 6): hand-drawn icons, particles, sounds, casting poses.

## Next safe action
