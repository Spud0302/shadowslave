---
uid: 20261022T113416Z-claude-code-intention-offering
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T113414Z-claude-code-intention-offering
created: 2026-10-22
updated: 2026-10-22
branch: claude/intention-offering
base_commit: 6f989927e27db3d77862d20895770d64489bb0a0
head_commit: 6f989927e27db3d77862d20895770d64489bb0a0
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Intention offering before the Aspect roll

## Owner intent and scope

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 7, an intention offering before the roll: an item and a longing that raise one Nature's odds without guaranteeing it.

## Outcome

Longing, IntentionData, IntentionRules, IntentionService, intention item tags, IntentionRequestPayload and the Soul Sea Intention page.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that placed a lit Dream Candle, gave blaze powder, meditated, chose Burn and pressed Offer. Unit tests pass (new IntentionRulesTest, including 2000 real rolls with an Ember offering and longing landing Ember between 38% and 50%). Game tests: "All 39 required tests passed :)" (three new: a tagged item is taken and an untagged one kept, the roll spends the intention and blocks further offerings, and offering away from the candle does nothing). Screenshot shows the Intention page with Burn chosen, Blaze Powder offered and odds Ember 44%, Mist, Stone and Tide 19%. Details in [[20261022T113415Z--claude-code--intention-offering]].

## Evidence and artifacts

## Unperformed checks

No human playtest; item lists are first picks. The odds preview mirrors the generator's weights rather than calling it.

## Known risks

If the identity generator's weighting changes, the odds preview needs the same change (IntentionRules.EVIDENCE_WEIGHT).

## Lore classifications

DESIGN: gameplay numbers and mechanics are design choices, not canon.

## Explicitly deferred

Nemesis system, challenge triggers, anything Ascended or higher (all out of this slice).

## Next safe action


