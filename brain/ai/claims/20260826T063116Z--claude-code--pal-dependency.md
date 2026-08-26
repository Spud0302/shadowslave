---
uid: 20260826T063116Z-claude-code-pal-dependency
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code
task_id: 20260826T063116Z-claude-code-pal-dependency
created: 2026-08-26
updated: 2026-08-26
branch: claude/shadow-slave-github-connect-x6nqra
worktree: primary
base_commit: 9e4280b16c2d8b168a63ef34e2960468acdf6bb0
lease_until: 2026-08-26T10:31:16Z
targets:
  - mod/build.gradle
  - mod/gradle.properties
  - mod/src/main/templates/META-INF/neoforge.mods.toml
  - modpack/manifest.json
  - .github/workflows/modpack-shell.yml
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Add PAL as a required dependency

## Owner intent

Owner approved the library route and then opened the blocked hosts so the work
could be compiled rather than written blind. Both happened 2026-08-26.

## What the unblock changed

`maven.neoforged.net`, `libraries.minecraft.net`, the Mojang piston hosts,
`dl.cloudsmith.io` and `repo.redlance.org` all became reachable. Everything
below was therefore compiled and run here, not reasoned about.

## Verified

- **Baseline first.** `./mod/gradlew -p mod build` succeeded *before* any edit,
  so a later failure was provably mine. combat-core built too.
- **PAL pinned.** `com.zigythebird.playeranim:PlayerAnimationLibNeo:1.1.6+mc.1.21.1`,
  sha256 `3cab280c…8cb47`, matching the publisher's own `.sha256` file.
- **Compiles.** A throwaway class importing `PlayerAnimationAccess`,
  `ModifierLayer` and `IAnimation` compiled, then was deleted. The types the
  integration will need are genuinely reachable.
- **Packaging reproduced.** `build_package.py` and `verify_package.py` run
  locally with the real GeckoLib, SmartBrainLib and PAL JARs, exactly as the
  workflow invokes them. PAL lands at `mods/PlayerAnimationLibNeo-1.1.6+mc.1.21.1.jar`.
- **CI URL exercised.** The `%2B`-encoded URL in the workflow was fetched here
  and its digest checked.
- `validate_manifest.py`, `check_dependency_closure.py` and the 50 modpack
  tests all pass.

## Two failures the compiler caught

Both would have shipped broken if written blind.

1. **Transitive version conflicts.** PAL's core requests netty 4.1.118,
   fastutil 8.5.15, gson 2.11.0 and slf4j 2.0.16, while Minecraft pins all four
   with `strictly` constraints (netty 4.1.97.Final, gson 2.10.1, slf4j 2.0.9).
   Gradle cannot reconcile a strict pin with a higher request. Resolved by
   excluding exactly those four groups, which Minecraft and NeoForge already
   supply at runtime, and keeping PAL's own `mochafloats` and `javassist`.
2. **Core is runtime-scoped.** The Neo module declares `PlayerAnimationLibCore`
   at runtime scope, so its animation types are absent from the compile
   classpath. Added explicitly as `compileOnly`; PAL ships as a required mod and
   supplies it at runtime.

## Version trap worth remembering

The repository publishes both `1.1.6+mc.1.21.1` and `1.1.6+mc.1.21.11`. Those
are Minecraft 1.21.1 and 1.21.11. Picking the wrong one is a one-character
mistake that would resolve and then fail at runtime. Noted in
`gradle.properties` and in the workflow.

## Not verified

No client or server was launched. Nothing here shows PAL loading in a running
game, and the `versionRange="[1.1.6,)"` in `neoforge.mods.toml` is checked by
NeoForge at runtime, not at build time, so it is the one declaration a build
cannot confirm. PAL reports its version as `1.1.6+mc.1.21.1`; if that range
rejects it, the fix is that one line.

No animation is authored and no integration code is written. Adding the
dependency is not the feature.

## Explicit exclusions

- No PAL integration code. Next step, and now compilable.
- `ActionPoseCurve` left in place; it remains the only working motion source
  until PAL is proven to load.
- The Chainback timing mismatch and the missing textures are untouched.

## Repository hygiene found

19 files under `combat-core/.gradle/` are tracked, and `.gitignore` has no
`.gradle/` or `build/` entry. Local Gradle state — binary lock files, caches,
configuration-cache temporaries — is committed and changes on every build.
Restored rather than committed each time here. Worth a `git rm --cached` and a
`.gitignore` entry, which is an owner decision since it deletes tracked files.

## Closure

Closed 2026-08-26 by claude-code. Dependency added, pinned and compiled; CI
packaging reproduced locally. Runtime load unproven.
