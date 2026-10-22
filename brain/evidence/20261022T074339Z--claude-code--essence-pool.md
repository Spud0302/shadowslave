---
uid: 20261022T074339Z-claude-code-essence-pool
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T074338Z-claude-code-essence-pool
created: 2026-10-22
updated: 2026-10-22
branch: claude/essence-pool
captured_commit: 468b0f03f4da1a21a45452ce3c10e46293a4d4f0
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Essence pool replaces ability cooldowns

## Claim tested

Abilities spend essence from one pool and refuse when short; the pool refills over time; the HUD shows the bar and slot state; no cooldown code remains.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/essence-pool, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled an Aspect in a fresh world, cast Kindle twice and screenshotted the HUD and the Abilities tab.

## Observed result

build: BUILD SUCCESSFUL. Game tests: "All 11 required tests passed :)" (Kindle now asserts two casts fit a full pool and the third is DRAINED). Headless client: the essence bar shows full with the cost tick, then partly refilled after the casts; the Abilities tab reads "Key R · Costs 35 essence · Ready".

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest. Balance numbers are first guesses.

## Conclusion

Abilities run on a shared essence pool with no cooldowns.
