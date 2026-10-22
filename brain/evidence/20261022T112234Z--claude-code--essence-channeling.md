---
uid: 20261022T112234Z-claude-code-essence-channeling
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T112233Z-claude-code-essence-channeling
created: 2026-10-22
updated: 2026-10-22
branch: claude/essence-channeling
captured_commit: a609b07b0880cf2d85465bd140da2f46b9121a1b
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Essence channeling for the Awakened

## Claim tested

Dormant players cannot channel; a leap spends essence, launches the player and forgives its fall; a loaded blow hits harder once and only once.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/essence-channeling, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled Tide, Awakened with Undertow, loaded a strike and opened the Abilities tab.

## Observed result

Unit tests pass. Game tests: "All 33 required tests passed :)" (three new: Dormant cannot channel, leap launches and forgives an 8-block fall while an ordinary fall still hurts, and a loaded blow does over 1.5x the plain damage once and then reverts). Screenshots show the amber essence bar while a blow is loaded and the Channeling section on the Abilities tab.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest; leap strength and costs are first guesses. The leap is server-driven motion, so on a laggy connection it can feel slightly late.

## Conclusion

Awakened players can channel essence into a leap or a heavier blow.
