---
uid: 20261022T063537Z-claude-code-hud-soul-screen
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T063536Z-claude-code-hud-soul-screen
created: 2026-10-22
updated: 2026-10-22
branch: claude/hud-soul-screen
captured_commit: cf8bd6b5d946ce0e85cf7241d3ab92e2692cd485
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Ability HUD slot and tabbed Soul screen

## Claim tested

With an Aspect, the HUD slot shows the ability, drains while recovering, glows when primed and flashes when ready; the Soul screen shows the kit on four tabs over a translucent veil.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/hud-soul-screen, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that created a world, rolled an Aspect on the integrated server, clicked through the reveal, switched every Soul screen tab, pressed the ability twice and took screenshots.

## Observed result

build: BUILD SUCCESSFUL, 269 unit tests, 0 failures (new: SoulPagesTest, ClientAbilityStateTest). Game tests: "All 11 required tests passed :)", with Returning Tide now also asserting the status is primed after the first press and shows the 500-tick cooldown after the return. Headless client: the full flow ran end to end (roll, typed reveal, Soul screen opened by the second click, four tabs, Holdfast used, HUD slot showed 30 then 28 seconds, Abilities tab showed Recovering).

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest. The primed glow and ready flash were not captured on screen (the run rolled Stone). Software rendering under xvfb showed some unrelated purple smears.

## Conclusion

The HUD slot and tabbed Soul screen work in a real client against a real integrated server.
