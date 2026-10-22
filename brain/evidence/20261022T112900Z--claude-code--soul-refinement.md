---
uid: 20261022T112900Z-claude-code-soul-refinement
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T112859Z-claude-code-soul-refinement
created: 2026-10-22
updated: 2026-10-22
branch: claude/soul-refinement
captured_commit: 933bd3a8ad3ec8fc57e5cb9be9fca6bfe406355d
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Refinement points and respec in the Soul Sea

## Claim tested

Points come from rank, can only be placed while meditating, change health, essence and costs as described, and a respec refunds all but one.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/soul-refinement, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled Mist, Awakened with Veil, meditated at a lit candle, opened the Refine page and placed a Body point.

## Observed result

Unit tests pass (new RefinementRulesTest). Game tests: "All 36 required tests passed :)" (three new: a Body point adds a heart and a respec takes it back and loses the point, Soul and Aspect points grow the pool to 215 and cut Kindle to 33 essence on the HUD, and refining away from the candle does nothing). Screenshots show the Rise page after Awakening and the Refine page with 3 points to place, Body at 1 and the Respec button.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest; the per-point numbers are first guesses. Aspect points do not lengthen ability durations yet.

## Conclusion

Refinement points and respec work from the Soul Sea.
