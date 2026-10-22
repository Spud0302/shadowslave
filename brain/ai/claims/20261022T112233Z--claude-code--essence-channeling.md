---
uid: 20261022T112233Z-claude-code-essence-channeling
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T112233Z-claude-code-essence-channeling
created: 2026-10-22
updated: 2026-10-22
branch: claude/essence-channeling
worktree: primary
base_commit: a609b07b0880cf2d85465bd140da2f46b9121a1b
lease_until: 2026-10-22T19:22:33Z
targets: 
  - mod/src/main/java/dev/spud/shadowslave/ability/
  - mod/src/main/java/dev/spud/shadowslave/client/
  - mod/src/main/java/dev/spud/shadowslave/network/
  - mod/src/main/java/dev/spud/shadowslave/ShadowSlaveMod.java
  - mod/src/main/resources/assets/shadowslave/lang/en_us.json
  - mod/src/test/java/dev/spud/shadowslave/
  - PLAYTEST.md
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Essence channeling for the Awakened

## Owner intent

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 4, conscious channeling (essence bursts) that Awakening opens.

## Exact scope

ability/ChannelService: from Awakened, the Channel Essence key (Z) spends 20 essence on a leap forward and up whose own height of fall is forgiven (LivingFallEvent within 3 s), or with sneak held spends 25 to load the next melee blow (x1.75 damage plus knockback, LivingIncomingDamageEvent) for up to 6 s. ChannelRequestPayload carries the sneak state; AbilityStatusPayload gains strikePrimed so the essence bar glows amber while a blow is loaded. The Abilities tab shows a Channeling section for Awakened players. NETWORK_VERSION 11.

## Acceptance criteria

Dormant players cannot channel; a leap spends essence, launches the player and forgives its fall; a loaded blow hits harder once and only once.

## Target paths

## Explicit exclusions

Refinement points, respec and intention offering (later PRs).

## Dependencies and overlaps



## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T112234Z--claude-code--essence-channeling]]; handoff in brain/ai/handoffs under the same timestamp family.
