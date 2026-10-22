---
uid: 20261022T110808Z-claude-code-awakened-abilities
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T110807Z-claude-code-awakened-abilities
created: 2026-10-22
updated: 2026-10-22
branch: claude/awakened-abilities
captured_commit: 4e472864e6779b2e6c08e1cc93f0ee8d158b069d
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Awakened ability variants and the second key

## Claim tested

Each variant fires from the second key, draws on the shared essence pool and does its one effect; granting checks the Nature and refuses duplicates.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/awakened-abilities, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled Stone, granted Root in Stone, fired it from slot 1, opened the Abilities tab, then rolled Ember and fired Ember Ward.

## Observed result

Unit tests pass (new KitLadderTest checks icons and casting poses for all eight). Game tests: "All 25 required tests passed :)" (ten new: grant rules and second-key slot order, all eight variants registered, and one per ability effect, including the stone wall rising ahead and crumbling on clear). Screenshots show the HUD with R and V slots and the wall in front of the player, and the Abilities tab listing Holdfast (Key R) and Root in Stone (Key V) with costs and descriptions.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest; numbers are first guesses. Ember Ward's ring was not captured on screen (the wall was in view). Casting poses are not reviewed in third person.

## Conclusion

The eight Awakened abilities work from the second key on the shared pool.
