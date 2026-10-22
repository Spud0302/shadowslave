---
uid: 20261022T111719Z-claude-code-candle-awakening
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T111718Z-claude-code-candle-awakening
created: 2026-10-22
updated: 2026-10-22
branch: claude/candle-awakening
captured_commit: 4e472864e6779b2e6c08e1cc93f0ee8d158b069d
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Meditation at the Dream Candle and Awakening

## Claim tested

Awakening only with a met requirement and one of the player's Nature's choices, only from beside a lit candle (or the operator command), and it raises rank, grants the ability, resets the core and grows essence.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/candle-awakening, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled Stone, placed a lit Dream Candle, meditated at 100/160 core, filled the core, meditated again, clicked Root in Stone and Awaken, then opened the Abilities tab.

## Observed result

Unit tests pass (new SoulRitualTransitionTest cases for awaken). Game tests: "All 30 required tests passed :)" (five new: requirement then full rise, Nature-only choices, swapped requirement, sneak-use on a lit candle meditates without snuffing it or placing the held block, and a snuffed candle ends meditation). Screenshots show the Soul Sea before and after saturation with both cards, the selected card, the Awakened state, and Root in Stone on Key V.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest. An admin reroll after Awakening keeps the Awakened stage but rolls a fresh Dormant kit, dropping the chosen ability.

## Conclusion

Players can meditate at the candle and Awaken with a chosen ability once their core is saturated.
