---
uid: 20261022T074338Z-claude-code-essence-pool
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T074338Z-claude-code-essence-pool
created: 2026-10-22
updated: 2026-10-22
branch: claude/essence-pool
worktree: primary
base_commit: 468b0f03f4da1a21a45452ce3c10e46293a4d4f0
lease_until: 2026-10-22T15:43:38Z
targets: 
  - mod/src/main/java/dev/spud/shadowslave/ability/
  - mod/src/main/java/dev/spud/shadowslave/client/
  - mod/src/main/java/dev/spud/shadowslave/network/
  - mod/src/main/java/dev/spud/shadowslave/attachment/ModAttachments.java
  - mod/src/main/java/dev/spud/shadowslave/awakening/AspectRollService.java
  - mod/src/main/java/dev/spud/shadowslave/preview/PreviewResetService.java
  - mod/src/main/java/dev/spud/shadowslave/command/ShadowSlaveCommands.java
  - mod/src/test/java/dev/spud/shadowslave/
  - PLAYTEST.md
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Essence pool replaces ability cooldowns

## Owner intent

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 1, the essence pool from Dormant on, with no cooldowns.

## Exact scope

New ESSENCE attachment (EssenceData) and EssenceService: 100 essence at Dormant, 200 at Awakened, an empty pool refills in 60 s. AspectAbility.cooldownTicks becomes essenceCost (Kindle 35, Mist Passage 25, Holdfast 35, Returning Tide 30). AbilityService spends essence instead of starting cooldowns and lists abilities per key slot (lowest rank first) so a second ability key can be added later. AbilityStatusPayload now carries slots plus the pool (essence, capacity, regen per tick) and the client predicts the refill; UseAbilityRequestPayload carries a slot. HUD: an essence bar left of the slot with a tick at the ability's cost; the slot shades while short and flashes and chimes when affordable again. Soul screen shows costs and the pool. AbilityCooldownData and its attachment are removed. NETWORK_VERSION 8.

## Acceptance criteria

Abilities spend essence from one pool and refuse when short; the pool refills over time; the HUD shows the bar and slot state; no cooldown code remains.

## Target paths

## Explicit exclusions

No core saturation, shards, rank-ups, refinement or new abilities; those are later slices.

## Dependencies and overlaps



## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T074339Z--claude-code--essence-pool]]; handoff in brain/ai/handoffs under the same timestamp family.
