---
uid: 20261022T111720Z-claude-code-candle-awakening
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T111718Z-claude-code-candle-awakening
created: 2026-10-22
updated: 2026-10-22
branch: claude/candle-awakening
base_commit: 4e472864e6779b2e6c08e1cc93f0ee8d158b069d
head_commit: 4e472864e6779b2e6c08e1cc93f0ee8d158b069d
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Meditation at the Dream Candle and Awakening

## Owner intent and scope

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: steps 3 to 5, a swappable rank-up requirement (a saturated core for now), meditation at the Dream Candle opening the Soul Sea, and Awakening with the choice of one Awakened ability.

## Outcome

RankUpRequirement(s), AwakeningService, SoulSeaService, SoulSeaPayload, AwakenRequestPayload, SoulSeaScreen, SoulData.asAwakened.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that rolled Stone, placed a lit Dream Candle, meditated at 100/160 core, filled the core, meditated again, clicked Root in Stone and Awaken, then opened the Abilities tab. Unit tests pass (new SoulRitualTransitionTest cases for awaken). Game tests: "All 30 required tests passed :)" (five new: requirement then full rise, Nature-only choices, swapped requirement, sneak-use on a lit candle meditates without snuffing it or placing the held block, and a snuffed candle ends meditation). Screenshots show the Soul Sea before and after saturation with both cards, the selected card, the Awakened state, and Root in Stone on Key V. Details in [[20261022T111719Z--claude-code--candle-awakening]].

## Evidence and artifacts

## Unperformed checks

No human playtest. An admin reroll after Awakening keeps the Awakened stage but rolls a fresh Dormant kit, dropping the chosen ability.

## Known risks

Meditation state is in memory, so a server restart closes everyone's Soul Sea (they can meditate again).

## Lore classifications

DESIGN: gameplay numbers and mechanics are design choices, not canon.

## Explicitly deferred

Essence channeling, refinement points and respec, intention offering.

## Next safe action


