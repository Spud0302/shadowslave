---
uid: 20261022T055917Z-claude-code-remaining-kits
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T055916Z-claude-code-remaining-kits
created: 2026-10-22
updated: 2026-10-22
branch: claude/remaining-kits
captured_commit: fa774e247e55b21b1488f6075ee66462170beaf9
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Mist, Stone and Tide kits

## Claim tested

Each of the four kits plays as described in the plan's kit table: innate rule, ability on R, and Flaw.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/remaining-kits, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build` and `./mod/gradlew -p mod runGameTestServer`.

## Observed result

build: BUILD SUCCESSFUL, 255 unit tests, 0 failures. Game tests: "All 11 required tests passed :)": Mist Passage blinks at least 1.5 blocks east, turns invisible and costs 2 hunger; Holdfast gives Resistance and cancels knockback; Returning Tide marks, returns to within 0.01 of the mark, then recovers; Ember takes 2 of 4 lava damage while Stone takes 4; underwater for 20 ticks Tide loses no air, Stone loses 20 and Mist 30.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest. Not covered by game tests: Mist's Hush on sculk, Stone's Footing standing still, Stone Sleep effects and sinking (client), Tidal Debt over a real day, Holdfast's sprint block.

## Conclusion

The kits behave as specified for the paths the game tests cover.
