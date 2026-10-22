---
uid: 20261022T024952Z-claude-code-ability-framework
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T024952Z-claude-code-ability-framework
created: 2026-10-22
updated: 2026-10-22
branch: claude/ability-framework
worktree: primary
base_commit: 6440d2a3708edb54668280c814abef5c6a841b80
lease_until: 2026-10-22T10:49:52Z
targets:
  - mod/src/main/java/dev/spud/shadowslave/ability/
  - mod/src/main/java/dev/spud/shadowslave/preview/
  - mod/src/main/java/dev/spud/shadowslave/network/
  - mod/src/main/java/dev/spud/shadowslave/client/ClientKeyMappings.java
  - mod/src/main/java/dev/spud/shadowslave/client/ClientGameEvents.java
  - mod/src/main/java/dev/spud/shadowslave/client/ClientModPayloads.java
  - mod/src/main/java/dev/spud/shadowslave/attachment/ModAttachments.java
  - mod/src/main/java/dev/spud/shadowslave/command/ShadowSlaveCommands.java
  - mod/src/main/java/dev/spud/shadowslave/awakening/AspectRollService.java
  - mod/src/test/java/dev/spud/shadowslave/ability/
  - PLAYTEST.md
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Aspect ability framework

## Owner intent

Jack (2026-10-22) agreed the abilities rework plan. This claim is build-order step 2: one ability key, server-side cooldowns, the combat hook and action-bar feedback, with Kindle ported onto it.

## Exact scope

AspectAbility interface and registry, persistent per-ability cooldowns, AbilityService behind the R key (renamed from Activate Kindle), the AbilityCombat hook with a vanilla implementation, action-bar feedback, Kindle reimplemented on top (now also sets nearby hostiles alight). Network version bumped to 4.

## Acceptance criteria

Pressing the ability key runs the player's highest-rank activatable ability on the server, starts its cooldown, reports progress on the action bar, and routes harm through AbilityCombat.

## Target paths

## Explicit exclusions

No new kits (step 3), no HUD (step 5), no Combat Core changes.

## Dependencies and overlaps

Appends to the PLAYTEST.md section written in the dream-candle-roll claim.

## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T024954Z--claude-code--ability-framework]]; handoff in brain/ai/handoffs under the same timestamp family.
