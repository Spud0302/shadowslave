---
uid: 20261022T063536Z-claude-code-hud-soul-screen
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T063536Z-claude-code-hud-soul-screen
created: 2026-10-22
updated: 2026-10-22
branch: claude/hud-soul-screen
worktree: primary
base_commit: cf8bd6b5d946ce0e85cf7241d3ab92e2692cd485
lease_until: 2026-10-22T14:35:36Z
targets: 
  - mod/src/main/java/dev/spud/shadowslave/client/
  - mod/src/main/java/dev/spud/shadowslave/ability/AbilityService.java
  - mod/src/main/java/dev/spud/shadowslave/ability/AspectAbility.java
  - mod/src/main/java/dev/spud/shadowslave/ability/ReturningTideAbility.java
  - mod/src/main/java/dev/spud/shadowslave/ability/AbilityGameTest.java
  - mod/src/main/java/dev/spud/shadowslave/awakening/AspectRollService.java
  - mod/src/main/java/dev/spud/shadowslave/network/
  - mod/src/main/resources/assets/shadowslave/lang/en_us.json
  - mod/src/test/java/dev/spud/shadowslave/client/
  - PLAYTEST.md
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Ability HUD slot and tabbed Soul screen

## Owner intent

Jack (2026-10-22) agreed the abilities rework plan. This claim is build-order step 5: the ability slot left of the hotbar with a cooldown sweep, and a tabbed Soul screen in the Spell look on a translucent background.

## Exact scope

A clientbound ability_status payload (ability, seconds left, cooldown, primed) sent on use, cooldown start, roll and login; a client cache that runs the cooldown on the client clock; an AbilityHudLayer registered above the hotbar; SpellStyle shared colours; SoulPages (pure tab content) and a rewritten SoulScreen with Aspect, Abilities, Flaw and Soul tabs. Stand-in item icons until step 6. NETWORK_VERSION goes to 6.

## Acceptance criteria

With an Aspect, the HUD slot shows the ability, drains while recovering, glows when primed and flashes when ready; the Soul screen shows the kit on four tabs over a translucent veil.

## Target paths

## Explicit exclusions

No custom icons, particles, sounds beyond a page turn, or casting poses (step 6).

## Dependencies and overlaps

Rewrites SoulScreen, last touched by the reveal overlay claim (only its footer).

## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T063537Z--claude-code--hud-soul-screen]]; handoff in brain/ai/handoffs under the same timestamp family.
