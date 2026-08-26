---
uid: ss-adr-20260826-player-animation-library
record_kind: decision
authority: proposal
lore_class: "N/A"
state: proposed
owner: claude-code
decision_owner: Andrew
created: 2026-08-26
updated: 2026-08-26
sources:
  - https://github.com/KosmX/minecraftPlayerAnimator
  - https://github.com/PlayerAnimationLibrary/PlayerAnimationLibrary
  - https://github.com/KosmX/minecraftPlayerAnimator/issues/115
  - https://www.curseforge.com/minecraft/mc-mods/player-animation-library
supersedes: []
tags:
  - decision
---

# ADR — Player animation library: PAL, not PlayerAnimator

**Proposal.** Agents do not promote decisions; this needs Andrew's acceptance.

## Context

Owner chose a player-animation library route on 2026-08-26, naming
**PlayerAnimator**, so that player motion is authored in Blockbench rather than
hand-written as easing curves in Java. Research before implementing found the
named library is the wrong target.

## Findings

**PlayerAnimator (`KosmX/minecraftPlayerAnimator`) is no longer maintained.**
Its own README on the 1.21 branch says the project is unmaintained and directs
new projects to **PAL** instead; only major bugfixes to existing releases are
planned. Its README documents Fabric and Forge/ForgeGradle, not NeoForge, and
issue #115 — "1.21.1 neoforge please?" — is open with no maintainer answer.
A `player-animation-lib-forge` 2.0.4+1.21.1 artifact exists, but whether it
loads on NeoForge is exactly what that unanswered issue asks.

**PAL (`PlayerAnimationLibrary/PlayerAnimationLibrary`, by ZigyTheBird) is the
maintained successor.**

| | PlayerAnimator | PAL |
| --- | --- | --- |
| Maintained | No, bugfix-only | Yes |
| NeoForge 1.21.1 | Unanswered issue | Published builds, e.g. `1.1.4+1.21.1-NeoForge` |
| Licence | MIT | **MIT**, verified from its own `LICENSE` |
| Coordinates | `dev.kosmx.player-anim:...` at `maven.kosmx.dev` | `com.zigythebird.playeranim:PlayerAnimationLibNeo` at `https://repo.redlance.org/public` |

Both are MIT, so redistribution inside the bundled modpack is fine either way,
and the provenance class is DEPENDENCY (linked, not copied) under
`brain/inbox/combat-reference-study`.

## Decision (proposed)

Use **PAL**. Same capability and same licence as the library the owner named,
but maintained and with real NeoForge 1.21.1 builds. Treating "PlayerAnimator"
as the capability rather than the specific artifact.

This supersedes the vanilla-model route chosen earlier the same day. The
`ActionPoseCurve` written for that route becomes redundant once PAL plays an
authored clip; it is left in place for now rather than deleted before PAL is
proven to load, since it is the only working motion source.

## Consequences

- One new required component in `modpack/manifest.json`, and therefore a new
  fixture JAR line in `.github/workflows/modpack-shell.yml`. Omitting that is
  precisely what broke CI on PR #3.
- A deferred-scope promotion: `combat-core/README.md` lists "bespoke animation
  frameworks" as explicitly deferred, and `brain/design/deferred-scope.md`
  requires the owner's explicit approval. This ADR is that request.
- Player motion becomes a Blockbench asset, which is the point: it can be
  iterated visually and by someone who is not a programmer.

## Blocked in the current sandbox

Implementation cannot be verified here, and in one case cannot be written
correctly at all:

1. **`repo.redlance.org` is unreachable** (as is `maven.kosmx.dev`, and
   `dl.cloudsmith.io`, which serves the existing GeckoLib and SmartBrainLib
   pins). The PAL JAR therefore cannot be downloaded, so the `sha256` that
   `modpack/manifest.json` requires for a `direct` component cannot be computed
   here. That manifest entry must be added by someone who can fetch the JAR.
2. **`maven.neoforged.net` is denied by egress policy**, so no Minecraft-facing
   Java compiles in this session.
3. **`docs.zigythebird.com` is blocked**, so PAL's integration API cannot be
   read. Writing client code against an API that cannot be read, compiled or
   run would produce confident-looking code with no basis.

Consequence: the phase-sync plumbing is still safe to write, because it copies
a pattern already in this repository (`SoulSnapshotPayload`). PAL integration
itself should wait for either reachable hosts or a reviewer who can build.

## What stays true regardless of route

The client still has no combat phase. `GlassRoadCombatData` lives in a server
attachment and never calls `publishPhase`; there is no phase payload. PAL needs
that signal exactly as the vanilla-model route did, and multiplayer needs it to
show other players' wind-ups at all.
