---
uid: 20261022T023123Z-claude-code-dream-candle-roll
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T023123Z-claude-code-dream-candle-roll
created: 2026-10-22
updated: 2026-10-22
branch: claude/dream-candle-roll
worktree: primary
base_commit: b4ccf430038bdb5b9bdef3556ead2169c7afe146
lease_until: 2026-10-22T10:31:23Z
targets:
  - mod/src/main/java/dev/spud/shadowslave/awakening/
  - mod/src/main/java/dev/spud/shadowslave/block/
  - mod/src/main/java/dev/spud/shadowslave/soul/
  - mod/src/main/java/dev/spud/shadowslave/item/ModItems.java
  - mod/src/main/java/dev/spud/shadowslave/command/ShadowSlaveCommands.java
  - mod/src/main/java/dev/spud/shadowslave/network/SoulSyncService.java
  - mod/src/main/resources/
  - .github/workflows/java-core.yml
  - PLAYTEST.md
excludes: []
depends_on: []
overlaps_with:
  - 20261022T015753Z-claude-code-playtest-launcher
tags:
  - multi-ai
  - claim
---

# Claim — Dream Candle ritual and Aspect roll

## Owner intent

Jack (2026-10-22) agreed the abilities and Aspect-rolling plan: an opt-in ritual paired with sleeping, a hand-made kit per Nature with a procedural name, one Dormant ability now, a roll lightly weighted by playstyle, admin-only rerolls, everyone Dormant. This claim is build-order step 1 (ritual and roll).

## Exact scope

Dream Candle block and item (candle + amethyst shard), the wake-up ritual that rolls once, the roller cut to the Ember, Mist, Stone and Tide kits, playstyle evidence from vanilla stats, `/shadowslave aspect reroll`, and a placeholder chat reveal.

## Acceptance criteria

Sleeping with a lit Dream Candle within two blocks of the bed rolls and commits an Aspect once; without one, sleep is vanilla; rerolls are operator-only; unit and game tests pass.

## Target paths

## Explicit exclusions

No ability effects beyond the existing Kindle and Cold Ash, no HUD or reveal overlay, no First Nightmare changes, no Combat Core changes.

## Dependencies and overlaps

Appends one section to PLAYTEST.md, which the playtest-launcher claim also holds; shared-document rules apply.

## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T024241Z--claude-code--dream-candle-roll]] in brain/evidence; handoff in brain/ai/handoffs with the same name.
