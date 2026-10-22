---
uid: 20261022T111718Z-claude-code-candle-awakening
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T111718Z-claude-code-candle-awakening
created: 2026-10-22
updated: 2026-10-22
branch: claude/candle-awakening
worktree: primary
base_commit: 4e472864e6779b2e6c08e1cc93f0ee8d158b069d
lease_until: 2026-10-22T19:17:18Z
targets: 
  - mod/src/main/java/dev/spud/shadowslave/awakening/
  - mod/src/main/java/dev/spud/shadowslave/soul/
  - mod/src/main/java/dev/spud/shadowslave/ability/
  - mod/src/main/java/dev/spud/shadowslave/client/
  - mod/src/main/java/dev/spud/shadowslave/network/
  - mod/src/main/java/dev/spud/shadowslave/command/ShadowSlaveCommands.java
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

# Claim — Meditation at the Dream Candle and Awakening

## Owner intent

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: steps 3 to 5, a swappable rank-up requirement (a saturated core for now), meditation at the Dream Candle opening the Soul Sea, and Awakening with the choice of one Awakened ability.

## Exact scope

awakening/RankUpRequirement and RankUpRequirements (SATURATED_CORE for Awakened and Ascended, replaceable per rank). AwakeningService checks the requirement and the choice, then moves SoulData DREAMER to AWAKENED (new SoulTransitions.awaken and SoulData.asAwakened), raises the Aspect rank, grants the chosen variant, empties the core into its Awakened limit and refills the larger essence pool. SoulSeaService: sneak-use on a lit Dream Candle meditates (cancelled block use, so the candle stays lit and nothing is placed) and sends SoulSeaPayload; AwakenRequestPayload is honoured only beside that still-lit candle. client/SoulSeaScreen shows rank, core, the requirement and two choice cards with an Awaken button. /shadowslave awaken <name> for operators. NETWORK_VERSION 10.

## Acceptance criteria

Awakening only with a met requirement and one of the player's Nature's choices, only from beside a lit candle (or the operator command), and it raises rank, grants the ability, resets the core and grows essence.

## Target paths

## Explicit exclusions

Essence channeling (next PR), refinement points and respec, intention offering, anything past Awakened.

## Dependencies and overlaps



## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T111719Z--claude-code--candle-awakening]]; handoff in brain/ai/handoffs under the same timestamp family.
