---
uid: 20261022T105721Z-claude-code-soul-shards-core
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T105721Z-claude-code-soul-shards-core
created: 2026-10-22
updated: 2026-10-22
branch: claude/soul-shards
worktree: primary
base_commit: a22f371489d4f8ddd890ce3cf1c30ad9cfa08184
lease_until: 2026-10-22T18:57:21Z
targets: 
  - mod/src/main/java/dev/spud/shadowslave/soul/core/
  - mod/src/main/java/dev/spud/shadowslave/item/
  - mod/src/main/java/dev/spud/shadowslave/client/
  - mod/src/main/java/dev/spud/shadowslave/network/
  - mod/src/main/resources/assets/shadowslave/
  - mod/src/main/resources/data/shadowslave/tags/entity_type/soul_shard/
  - mod/tools/draw_shard_icons.py
  - mod/src/test/java/dev/spud/shadowslave/
  - PLAYTEST.md
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Soul shards and core saturation

## Owner intent

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 2, soul shards by creature rank, PvP shards and the core saturation meter.

## Exact scope

Four shard items (Dormant, Awakened, Fallen, Corrupted) with drawn textures (mod/tools/draw_shard_icons.py). Player kills of hostile mobs drop shards by rank, chosen by entity type tags under data/shadowslave/tags/entity_type/soul_shard/ plus armour; bosses always drop. A CORE attachment holds saturation (Dormant limit 160, Awakened 600); absorbing adds the shard's value, a quarter per rank the shard sits below the core; a saturated core refuses more. The core adds up to 4 max health per rank. PvP kills tear a quarter of the victim's saturation into a shard carrying a shard_contents data component, behind the shadowslavePvpSoulShards game rule (default true). CoreStatusPayload feeds a Core meter on the Soul tab. /shadowslave core [fill|set]. NETWORK_VERSION 9.

## Acceptance criteria

Mobs drop shards of their rank on player kills; absorbing fills the core up to saturation; PvP kills drop a shard from the victim when the rule is on; the Soul tab shows the meter.

## Target paths

## Explicit exclusions

No rank-up yet (next slice), no mob absorption of shards (nemesis system deferred).

## Dependencies and overlaps



## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T105722Z--claude-code--soul-shards-core]]; handoff in brain/ai/handoffs under the same timestamp family.
