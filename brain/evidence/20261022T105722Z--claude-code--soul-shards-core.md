---
uid: 20261022T105722Z-claude-code-soul-shards-core
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T105721Z-claude-code-soul-shards-core
created: 2026-10-22
updated: 2026-10-22
branch: claude/soul-shards
captured_commit: a22f371489d4f8ddd890ce3cf1c30ad9cfa08184
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Soul shards and core saturation

## Claim tested

Mobs drop shards of their rank on player kills; absorbing fills the core up to saturation; PvP kills drop a shard from the victim when the rule is on; the Soul tab shows the meter.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/soul-shards, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod test`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled an Aspect, gave the four shards, set the core to 98 and opened the Soul tab.

## Observed result

Unit tests pass (new CoreRulesTest, Soul page meter test). Game tests: "All 15 required tests passed :)" (new: mob ranks incl. armour and Elder Guardian always, absorb until saturated, PvP shard carries 25 of 100 and the killer absorbs it, game rule off drops nothing). Screenshots show the four shard icons in the hotbar and the Core meter at 61%.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest; drop rates are first guesses. Ender Dragon drop placement not checked in a real fight.

## Conclusion

Shards drop, absorb and saturate the core as designed.
