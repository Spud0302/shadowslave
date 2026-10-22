---
uid: 20261022T105723Z-claude-code-soul-shards-core
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T105721Z-claude-code-soul-shards-core
created: 2026-10-22
updated: 2026-10-22
branch: claude/soul-shards
base_commit: a22f371489d4f8ddd890ce3cf1c30ad9cfa08184
head_commit: a22f371489d4f8ddd890ce3cf1c30ad9cfa08184
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Soul shards and core saturation

## Owner intent and scope

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 2, soul shards by creature rank, PvP shards and the core saturation meter.

## Outcome

soul/core (ShardRank, CoreData, CoreRules, CoreService, ShardDrops), item/SoulShardItem, item/ShardContents, item/ModDataComponents, CoreStatusPayload and the Soul tab meter.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod test`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled an Aspect, gave the four shards, set the core to 98 and opened the Soul tab. Unit tests pass (new CoreRulesTest, Soul page meter test). Game tests: "All 15 required tests passed :)" (new: mob ranks incl. armour and Elder Guardian always, absorb until saturated, PvP shard carries 25 of 100 and the killer absorbs it, game rule off drops nothing). Screenshots show the four shard icons in the hotbar and the Core meter at 61%. Details in [[20261022T105722Z--claude-code--soul-shards-core]].

## Evidence and artifacts

## Unperformed checks

No human playtest; drop rates are first guesses. Ender Dragon drop placement not checked in a real fight.

## Known risks

Max-health bonus is a transient modifier reapplied on login and respawn; a relog at low health can briefly clamp health.

## Lore classifications

DESIGN: gameplay numbers and mechanics are design choices, not canon.

## Explicitly deferred

Awakening at the candle, Soul Sea, refinement, intention offering; nemesis system.

## Next safe action


