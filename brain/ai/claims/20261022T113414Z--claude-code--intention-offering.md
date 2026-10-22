---
uid: 20261022T113414Z-claude-code-intention-offering
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T113414Z-claude-code-intention-offering
created: 2026-10-22
updated: 2026-10-22
branch: claude/intention-offering
worktree: primary
base_commit: 6f989927e27db3d77862d20895770d64489bb0a0
lease_until: 2026-10-22T19:34:14Z
targets: 
  - mod/src/main/java/dev/spud/shadowslave/awakening/
  - mod/src/main/java/dev/spud/shadowslave/attachment/ModAttachments.java
  - mod/src/main/java/dev/spud/shadowslave/preview/PreviewResetService.java
  - mod/src/main/java/dev/spud/shadowslave/client/
  - mod/src/main/java/dev/spud/shadowslave/network/
  - mod/src/main/resources/data/shadowslave/tags/item/intention/
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

# Claim — Intention offering before the Aspect roll

## Owner intent

Jack (2026-10-22) approved the rank-up, essence and Soul Sea design in the "Lore to gameplay ideas" thread (doc https://claude.ai/artifact/6Ppkk9vv8V9LwEvBY2P1EQ). This claim is one slice of the Dormant-to-Awakened build: step 7, an intention offering before the roll: an item and a longing that raise one Nature's odds without guaranteeing it.

## Exact scope

awakening/Longing (Burn, Hide, Endure, Return mapped to the ember, mist, stone and water evidence tags), IntentionData attachment (offering tag and name, longing), IntentionRules (offering +2 and longing +2 evidence, per-tag cap 4, odds preview mirroring the generator's base 24 plus 8 per evidence point), IntentionService (takes one tagged item from the main hand; untagged items stay; an earlier offering stands until replaced). Item tags under data/shadowslave/tags/item/intention/. AspectRollService rolls on playstyle evidence leaned by the intention and clears it after. Meditating before the roll opens an Intention page in the Soul Sea with the four longings, the odds per Nature and an Offer button; IntentionRequestPayload is honoured only beside a lit candle. NETWORK_VERSION 13.

## Acceptance criteria

Offering and longing for one Nature take its odds from 25% to about 44%, never certain; the item is consumed only if tagged; the roll uses the intention once.

## Target paths

## Explicit exclusions

Anything past the Dormant roll; intention for later rank-ups.

## Dependencies and overlaps



## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T113415Z--claude-code--intention-offering]]; handoff in brain/ai/handoffs under the same timestamp family.
