---
uid: 20260826T052844Z-claude-code-player-action-pose-curve
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code
task_id: 20260826T052844Z-claude-code-player-action-pose-curve
created: 2026-08-26
updated: 2026-08-26
branch: claude/shadow-slave-github-connect-x6nqra
worktree: primary
base_commit: b51af2dd95624cce7505a16092e33ac0f6ffddaf
lease_until: 2026-08-26T09:28:44Z
targets:
  - mod/src/main/java/dev/spud/shadowslave/client/anim/ActionPoseCurve.java
  - mod/src/test/java/dev/spud/shadowslave/client/anim/ActionPoseCurveTest.java
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Player action pose curve (vanilla model route)

## Owner intent

Owner chose the vanilla-model route for player animation, 2026-08-26: drive
`HumanoidModel` limb rotations from combat phase rather than adding an
animation framework. No new dependency, works with every skin and armour, and
is the smallest addition that satisfies the Glass Road gap.

## Exact scope

The arm motion itself, as a pure function of phase and progress. Nothing that
imports Minecraft.

Glass Road commits the player for 27 ticks (1.35s) with no visual feedback at
all, which reads as input lag. This is the motion half of closing that gap.

## Sandbox constraint that shaped the design

`maven.neoforged.net` is denied by this session's egress policy (the proxy logs
`connect_rejected`, 403 to CONNECT), and `libraries.minecraft.net` is
unreachable, so Gradle cannot resolve NeoForge and no Minecraft-touching Java
can be compiled here. `repo1.maven.org` and `plugins.gradle.org` are reachable.

Rather than write the motion blind, the curve was deliberately kept free of
Minecraft imports. That made it compile-checkable with plain `javac` against
Combat Core's API — which is itself Minecraft-free — and runnable under a JUnit
console standalone JAR fetched from Maven Central.

All 11 tests were actually executed here and pass. The remaining Minecraft
layer stays as thin as possible for exactly this reason.

## What the curve does

- **Wind-up** rises fast on an ease-out curve and then holds the cocked pose.
  The hold is the telegraph; a linear lift would only show the readable shape
  on the final tick, too late to react to. 88% of the lift is done by mid-phase.
- **Active** snaps through. One tick in practice.
- **Recovery** eases back on an ease-in-out curve across the whole window.

## A flaw found by running it

Recovery was first written with the same ease-out curve as wind-up. Printing
the actual trace over a 27-tick Glass Road action showed it returning to rest
by tick 9 of 16, leaving the player standing idle for the last 7 ticks while
still committed — the opposite of what recovery must communicate.

Changed to ease-in-out and covered by `recoveryStillMovesLateInTheWindow`,
which fails against the old curve. Compiling was not enough to catch this;
running it and reading the numbers was.

## Acceptance criteria

- Compiles against Combat Core's API with plain `javac`. Met.
- Phase boundaries continuous, so the arm never teleports mid-swing. Met.
- Survives overrun, negative ticks, zero-length phases and NaN. Met.
- 11 JUnit tests pass, executed here, not merely written. Met.

## Explicit exclusions — the feature is NOT yet wired

Nothing calls this class yet. Two Minecraft-touching pieces remain, both of
which are genuinely blind work in this sandbox:

1. **Client has no phase.** `GlassRoadCombatData` lives in a server attachment
   (`serverPlayer.getData(ModAttachments.GLASS_ROAD_COMBAT)`) and never calls
   `publishPhase`, unlike `ChainbackEntity` which calls it at two sites.
   `network/payload/` has no phase payload. A clientbound payload following the
   existing `SoulSnapshotPayload` pattern is needed, plus almost certainly a
   `ModPayloads.NETWORK_VERSION` bump from "3".
2. **The arm adapter.** A thin `IClientItemExtensions#getArmPose` returning a
   custom `HumanoidModel.ArmPose` built with `ArmPose.create(...)`, registered
   through `RegisterClientExtensionsEvent#registerItem`. NeoForge enum
   extension needs an `EnumProxy`, which is the detail most likely to be wrong
   when written without a compiler.

Also untouched: entity animations, the Chainback timing mismatch, textures.

## Coordination notes

No physical proof. Nothing here demonstrates how the motion looks in a running
client; it demonstrates only that the numbers are the intended numbers. Whether
the pose reads as a weighted strike is a judgement that needs a client and eyes.

## Closure

Closed 2026-08-26 by claude-code. Verified motion delivered; wiring deliberately
not attempted blind in one pass.
