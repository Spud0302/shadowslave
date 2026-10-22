---
uid: 20261022T055916Z-claude-code-remaining-kits
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code-cloud
task_id: 20261022T055916Z-claude-code-remaining-kits
created: 2026-10-22
updated: 2026-10-22
branch: claude/remaining-kits
worktree: primary
base_commit: fa774e247e55b21b1488f6075ee66462170beaf9
lease_until: 2026-10-22T13:59:16Z
targets:
  - mod/src/main/java/dev/spud/shadowslave/ability/
  - mod/src/main/java/dev/spud/shadowslave/client/ClientKitEffects.java
  - mod/src/main/java/dev/spud/shadowslave/preview/
  - mod/src/main/java/dev/spud/shadowslave/attachment/ModAttachments.java
  - mod/src/main/java/dev/spud/shadowslave/awakening/AspectRollService.java
  - mod/src/main/java/dev/spud/shadowslave/ShadowSlaveMod.java
  - mod/src/test/java/dev/spud/shadowslave/ability/
  - PLAYTEST.md
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Mist, Stone and Tide kits

## Owner intent

Jack (2026-10-22) agreed the abilities rework plan. This claim is build-order step 3: Mist Passage, Holdfast, Returning Tide, the four innate rules and the four Flaws.

## Exact scope

Three new AspectAbility implementations, a PRIMED activation result and per-tick hook for multi-press abilities, KitPassives (innate rules and Flaws), a flaw_state attachment for Tidal Debt, client-side Stone Sleep sinking. PreviewPowerService removed (Cold Ash moved into KitPassives).

## Acceptance criteria

Each of the four kits plays as described in the plan's kit table: innate rule, ability on R, and Flaw.

## Target paths

## Explicit exclusions

No HUD, overlay or Soul screen work; no Combat Core changes.

## Dependencies and overlaps

Edits the PLAYTEST.md section written by the earlier claims of this build.

## Coordination notes

## Closure

Closed 2026-10-22 by claude-code. Evidence: [[20261022T055917Z--claude-code--remaining-kits]]; handoff in brain/ai/handoffs under the same timestamp family.
