---
uid: 20261022T064644Z-claude-code-ability-polish
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T064644Z-claude-code-ability-polish
created: 2026-10-22
updated: 2026-10-22
branch: claude/ability-polish
worktree: primary
base_commit: 7548028ed9fb5c5c7989964cfeaedd2e0986b68e
lease_until: 2026-10-22T14:46:44Z
targets: 
  - mod/src/main/java/dev/spud/shadowslave/client/
  - mod/src/main/java/dev/spud/shadowslave/ability/
  - mod/src/main/java/dev/spud/shadowslave/network/
  - mod/src/main/resources/assets/shadowslave/textures/gui/
  - mod/src/main/resources/assets/shadowslave/player_animations/ability_casts.json
  - mod/tools/draw_ability_icons.py
  - mod/src/test/java/dev/spud/shadowslave/client/
  - PLAYTEST.md
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Ability icons, casting poses and effects

## Owner intent

Jack (2026-10-22) agreed the abilities rework plan. This claim is build-order step 6, the last one: visual polish with particles, sounds, casting poses with PAL, and 8 icons.

## Exact scope

Eight hand-drawn 16x16 icons (4 abilities, 4 Flaws) generated from character grids by mod/tools/draw_ability_icons.py and used by the HUD slot and the Soul screen. Four PAL casting poses (player_animations/ability_casts.json) on a dedicated animation layer, triggered by a new clientbound ability_cast payload sent to modded players within 64 blocks, the caster included. Extra effects: Kindle's flame ring at its reach, Mist Passage's trail, Holdfast's dust while braced, Returning Tide's dripping mark, and a client chime when the cooldown ends. NETWORK_VERSION goes to 7.

## Acceptance criteria

Each kit ability shows its icon, plays its casting pose for the caster and onlookers, and has visible effects; the Flaw tab shows the Flaw icon.

## Target paths

## Explicit exclusions

No new sound assets (vanilla sounds only), no changes to ability balance.

## Dependencies and overlaps

Builds on the HUD and Soul screen claim (step 5).

## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T064645Z--claude-code--ability-polish]]; handoff in brain/ai/handoffs under the same timestamp family.
