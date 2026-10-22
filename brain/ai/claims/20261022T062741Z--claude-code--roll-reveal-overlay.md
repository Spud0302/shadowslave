---
uid: 20261022T062741Z-claude-code-roll-reveal-overlay
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T062741Z-claude-code-roll-reveal-overlay
created: 2026-10-22
updated: 2026-10-22
branch: claude/roll-reveal-overlay
worktree: primary
base_commit: 0941599e0905287205f1bb21e2c78f7c657baffd
lease_until: 2026-10-22T14:27:41Z
targets: 
  - mod/src/main/java/dev/spud/shadowslave/client/SpellRevealScreen.java
  - mod/src/main/java/dev/spud/shadowslave/client/RevealTypewriter.java
  - mod/src/main/java/dev/spud/shadowslave/client/ClientPayloadHandler.java
  - mod/src/main/java/dev/spud/shadowslave/client/ClientModPayloads.java
  - mod/src/main/java/dev/spud/shadowslave/client/SoulScreen.java
  - mod/src/main/java/dev/spud/shadowslave/network/ModPayloads.java
  - mod/src/main/java/dev/spud/shadowslave/network/payload/AspectRevealPayload.java
  - mod/src/main/java/dev/spud/shadowslave/awakening/AspectRollService.java
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

# Claim — Spell roll reveal overlay

## Owner intent

Jack (2026-10-22) agreed the abilities rework plan. This claim is build-order step 4: a Spell-style overlay that types out the rolled Aspect, then opens the Soul screen.

## Exact scope

A clientbound aspect_reveal payload with the kit's display words, a SpellRevealScreen over a translucent veil that types each line out with a soft chime, and a pure RevealTypewriter for timing. Click, Space or Enter skips the typing and then opens the Soul screen; it also continues by itself 6 seconds after finishing. The chat reveal stays as a fallback when the channel is missing. NETWORK_VERSION goes to 5. The Soul screen's stale preview-command footer now names the ability key.

## Acceptance criteria

After a Dream Candle roll a modded client sees the reveal type out, and continuing opens the Soul screen.

## Target paths

## Explicit exclusions

No HUD slot or Soul screen redesign (step 5); no new sounds or textures (step 6).

## Dependencies and overlaps

Edits the PLAYTEST.md section written by the earlier claims of this build.

## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T062742Z--claude-code--roll-reveal-overlay]]; handoff in brain/ai/handoffs under the same timestamp family.
