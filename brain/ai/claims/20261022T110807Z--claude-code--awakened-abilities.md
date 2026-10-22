---
uid: 20261022T110807Z-claude-code-awakened-abilities
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T110807Z-claude-code-awakened-abilities
created: 2026-10-22
updated: 2026-10-22
branch: claude/awakened-abilities
worktree: primary
base_commit: 4e472864e6779b2e6c08e1cc93f0ee8d158b069d
lease_until: 2026-10-22T19:08:07Z
targets: 
  - mod/src/main/java/dev/spud/shadowslave/ability/
  - mod/src/main/java/dev/spud/shadowslave/awakening/
  - mod/src/main/java/dev/spud/shadowslave/soul/identity/
  - mod/src/main/java/dev/spud/shadowslave/client/
  - mod/src/main/java/dev/spud/shadowslave/command/ShadowSlaveCommands.java
  - mod/src/main/java/dev/spud/shadowslave/ShadowSlaveMod.java
  - mod/src/main/resources/assets/shadowslave/
  - mod/tools/draw_ability_icons.py
  - mod/src/test/java/dev/spud/shadowslave/
  - PLAYTEST.md
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Awakened ability variants and the second key

## Owner intent

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 6, two Awakened ability variants per Nature (eight in all) and a second ability key.

## Exact scope

awakening/KitLadder and AbilityVariant list two Awakened variants per Nature (Ember: Carry the Flame, Ember Ward; Mist: Through the Haze, Veil; Stone: Root in Stone, Quake; Tide: Low Tide, Undertow). Each is an AspectAbility registered in AbilityRegistry with an essence cost, a drawn 16x16 icon (mod/tools/draw_ability_icons.py) and a cast_<name> pose in player_animations/ability_casts.json. AbilityGrants.grant adds a variant to the player's Aspect as a rank-granted ability; /shadowslave ability grant <name> exposes it to operators until the candle rank-up lands. A second key (V, USE_SECOND_ABILITY) fires slot 1. AspectAbility gains clear(), called on logout so walls and wards do not linger. The Abilities tab reads variant descriptions from KitLadder.

## Acceptance criteria

Each variant fires from the second key, draws on the shared essence pool and does its one effect; granting checks the Nature and refuses duplicates.

## Target paths

## Explicit exclusions

No rank-up choice screen yet (next slice); no Ascended variants.

## Dependencies and overlaps



## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T110808Z--claude-code--awakened-abilities]]; handoff in brain/ai/handoffs under the same timestamp family.
