---
uid: 20261022T062742Z-claude-code-roll-reveal-overlay
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T062741Z-claude-code-roll-reveal-overlay
created: 2026-10-22
updated: 2026-10-22
branch: claude/roll-reveal-overlay
captured_commit: 0941599e0905287205f1bb21e2c78f7c657baffd
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Spell roll reveal overlay

## Claim tested

After a Dream Candle roll a modded client sees the reveal type out, and continuing opens the Soul screen.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/roll-reveal-overlay, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that opened the screen with sample words and saved screenshots.

## Observed result

build: BUILD SUCCESSFUL, 261 unit tests, 0 failures (new: RevealTypewriterTest, AspectRevealPayloadTest). Game tests: "All 11 required tests passed :)". Headless client: the screen opened, typed out, and the screenshot after 11 seconds showed every line laid out and the continue hint.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest. The hand-off to the Soul screen (sending open_soul_screen) was not exercised in the headless run because no world was loaded. The chime could not be heard (no audio device).

## Conclusion

The overlay renders and types as designed; the Soul screen hand-off reuses the existing request path.
