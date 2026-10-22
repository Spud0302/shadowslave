---
uid: 20261022T110809Z-claude-code-awakened-abilities
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T110807Z-claude-code-awakened-abilities
created: 2026-10-22
updated: 2026-10-22
branch: claude/awakened-abilities
base_commit: 4e472864e6779b2e6c08e1cc93f0ee8d158b069d
head_commit: 4e472864e6779b2e6c08e1cc93f0ee8d158b069d
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Awakened ability variants and the second key

## Owner intent and scope

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 6, two Awakened ability variants per Nature (eight in all) and a second ability key.

## Outcome

Eight AspectAbility classes, KitLadder, AbilityVariant, AbilityGrants, the V key, icons and poses.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled Stone, granted Root in Stone, fired it from slot 1, opened the Abilities tab, then rolled Ember and fired Ember Ward. Unit tests pass (new KitLadderTest checks icons and casting poses for all eight). Game tests: "All 25 required tests passed :)" (ten new: grant rules and second-key slot order, all eight variants registered, and one per ability effect, including the stone wall rising ahead and crumbling on clear). Screenshots show the HUD with R and V slots and the wall in front of the player, and the Abilities tab listing Holdfast (Key R) and Root in Stone (Key V) with costs and descriptions. Details in [[20261022T110808Z--claude-code--awakened-abilities]].

## Evidence and artifacts

## Unperformed checks

No human playtest; numbers are first guesses. Ember Ward's ring was not captured on screen (the wall was in view). Casting poses are not reviewed in third person.

## Known risks

Root in Stone places real blocks; they are tracked in memory only, so a server crash before they crumble leaves the stone in place.

## Lore classifications

DESIGN: gameplay numbers and mechanics are design choices, not canon.

## Explicitly deferred

Awakening at the candle with the variant choice, Soul Sea, refinement, intention offering.

## Next safe action


