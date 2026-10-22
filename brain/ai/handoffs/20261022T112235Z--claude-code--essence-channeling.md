---
uid: 20261022T112235Z-claude-code-essence-channeling
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T112233Z-claude-code-essence-channeling
created: 2026-10-22
updated: 2026-10-22
branch: claude/essence-channeling
base_commit: a609b07b0880cf2d85465bd140da2f46b9121a1b
head_commit: a609b07b0880cf2d85465bd140da2f46b9121a1b
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Essence channeling for the Awakened

## Owner intent and scope

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 4, conscious channeling (essence bursts) that Awakening opens.

## Outcome

ChannelService, ChannelRequestPayload, the Z key, the strikePrimed HUD glow and the Channeling section.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled Tide, Awakened with Undertow, loaded a strike and opened the Abilities tab. Unit tests pass. Game tests: "All 33 required tests passed :)" (three new: Dormant cannot channel, leap launches and forgives an 8-block fall while an ordinary fall still hurts, and a loaded blow does over 1.5x the plain damage once and then reverts). Screenshots show the amber essence bar while a blow is loaded and the Channeling section on the Abilities tab. Details in [[20261022T112234Z--claude-code--essence-channeling]].

## Evidence and artifacts

## Unperformed checks

No human playtest; leap strength and costs are first guesses. The leap is server-driven motion, so on a laggy connection it can feel slightly late.

## Known risks

The loaded blow applies to any damage whose direct and causing entity is the player, so a sweeping blow spends the charge on whichever mob it hurts first.

## Lore classifications

DESIGN: gameplay numbers and mechanics are design choices, not canon.

## Explicitly deferred

Refinement points and respec, intention offering.

## Next safe action


