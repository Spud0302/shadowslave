---
uid: 20261022T113415Z-claude-code-intention-offering
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T113414Z-claude-code-intention-offering
created: 2026-10-22
updated: 2026-10-22
branch: claude/intention-offering
captured_commit: 6f989927e27db3d77862d20895770d64489bb0a0
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - evidence
---

# Evidence — Intention offering before the Aspect roll

## Claim tested

Offering and longing for one Nature take its odds from 25% to about 44%, never certain; the item is consumed only if tagged; the roll uses the intention once.

## Environment and method

Cloud container, JDK 21, NeoForge 21.1.244, branch claude/intention-offering, worktree before commit.

## Preconditions

## Command or research procedure

`./mod/gradlew -p mod build`, `./mod/gradlew -p mod runGameTestServer`, and a headless client run (`xvfb-run` + `runClientSmoke`) with a temporary, uncommitted hook that placed a lit Dream Candle, gave blaze powder, meditated, chose Burn and pressed Offer.

## Observed result

Unit tests pass (new IntentionRulesTest, including 2000 real rolls with an Ember offering and longing landing Ember between 38% and 50%). Game tests: "All 39 required tests passed :)" (three new: a tagged item is taken and an untagged one kept, the roll spends the intention and blocks further offerings, and offering away from the candle does nothing). Screenshot shows the Intention page with Burn chosen, Blaze Powder offered and odds Ember 44%, Mist, Stone and Tide 19%.

## Artifacts, hashes, logs, or chapter references

## Limitations and unperformed checks

No human playtest; item lists are first picks. The odds preview mirrors the generator's weights rather than calling it.

## Conclusion

Players can lean their Aspect roll with an offering and a longing at the candle.
