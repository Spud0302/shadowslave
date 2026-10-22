---
uid: 20261022T112859Z-claude-code-soul-refinement
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T112859Z-claude-code-soul-refinement
created: 2026-10-22
updated: 2026-10-22
branch: claude/soul-refinement
worktree: primary
base_commit: 933bd3a8ad3ec8fc57e5cb9be9fca6bfe406355d
lease_until: 2026-10-22T19:28:59Z
targets: 
  - mod/src/main/java/dev/spud/shadowslave/soul/refinement/
  - mod/src/main/java/dev/spud/shadowslave/ability/
  - mod/src/main/java/dev/spud/shadowslave/awakening/SoulSeaService.java
  - mod/src/main/java/dev/spud/shadowslave/attachment/ModAttachments.java
  - mod/src/main/java/dev/spud/shadowslave/preview/PreviewResetService.java
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

# Claim — Refinement points and respec in the Soul Sea

## Owner intent

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 5, refinement points in Body, Soul and Aspect, spent in the Soul Sea, with a respec that costs a point.

## Exact scope

soul/refinement: RefinementData attachment (body, soul, aspect, respecs; copied on death), RefinementRules (1 point at naming plus 3 per rank after; Body +2 max health, +3% speed, +0.5 attack damage per point; Soul +15 essence capacity and +10% refill per point; Aspect -6% ability cost per point, capped at 30%; a respec returns all placed points and loses one), RefinementService (allocate, respec, transient attribute modifiers reapplied on login and respawn). EssenceService capacity and refill and AbilityService.costFor read refinement. The Soul Sea gains Rise and Refine pages; RefineRequestPayload is honoured only while meditating at a lit candle. Preview reset clears refinement. NETWORK_VERSION 12.

## Acceptance criteria

Points come from rank, can only be placed while meditating, change health, essence and costs as described, and a respec refunds all but one.

## Target paths

## Explicit exclusions

Longer ability durations from Aspect points (cost only for now); intention offering (next PR).

## Dependencies and overlaps



## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T112900Z--claude-code--soul-refinement]]; handoff in brain/ai/handoffs under the same timestamp family.
