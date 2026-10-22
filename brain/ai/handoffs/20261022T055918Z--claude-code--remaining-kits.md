---
uid: 20261022T055918Z-claude-code-remaining-kits
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
task_id: 20261022T055916Z-claude-code-remaining-kits
created: 2026-10-22
updated: 2026-10-22
branch: claude/remaining-kits
base_commit: fa774e247e55b21b1488f6075ee66462170beaf9
head_commit: fa774e247e55b21b1488f6075ee66462170beaf9
worktree_dirty: true
sources: []
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
---

# Handoff — Mist, Stone and Tide kits

## Owner intent and scope

Jack (2026-10-22) agreed the abilities rework plan. This claim is build-order step 3: Mist Passage, Holdfast, Returning Tide, the four innate rules and the four Flaws.

## Outcome

Abilities: MistPassageAbility, HoldfastAbility, ReturningTideAbility; AspectAbility.Activation with PRIMED; AbilityService.startCooldown and onPlayerTick. Passives: KitPassives plus FlawStateData (attachment flaw_state) and client/ClientKitEffects.

## Files changed

See the claim's targets.

## Acceptance criteria

Met; see evidence.

## Verification performed

`./mod/gradlew -p mod build` and `./mod/gradlew -p mod runGameTestServer`. build: BUILD SUCCESSFUL, 255 unit tests, 0 failures. Game tests: "All 11 required tests passed :)": Mist Passage blinks at least 1.5 blocks east, turns invisible and costs 2 hunger; Holdfast gives Resistance and cancels knockback; Returning Tide marks, returns to within 0.01 of the mark, then recovers; Ember takes 2 of 4 lava damage while Stone takes 4; underwater for 20 ticks Tide loses no air, Stone loses 20 and Mist 30. Details in [[20261022T055917Z--claude-code--remaining-kits]].

## Evidence and artifacts

## Unperformed checks

No human playtest. Not covered by game tests: Mist's Hush on sculk, Stone's Footing standing still, Stone Sleep effects and sinking (client), Tidal Debt over a real day, Holdfast's sprint block.

## Known risks

Holdfast braces and Returning Tide marks are in memory only, so a server restart drops them. Mist Passage uses teleportTo and could in rare geometry leave the player in a tight spot; it checks the body fits before moving.

## Lore classifications

DESIGN: gameplay numbers and mechanics are design choices, not canon.

## Explicitly deferred

Reveal overlay (step 4), HUD and Soul screen (step 5), polish (step 6).

## Next safe action
