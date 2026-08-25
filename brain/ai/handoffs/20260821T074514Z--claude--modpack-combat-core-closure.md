---
uid: 20260821T074514Z-claude-modpack-combat-core-closure-handoff
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: claude
task_id: 20260821T073922Z-claude-modpack-combat-core-closure
created: 2026-08-21
updated: 2026-08-21
branch: packaging/combat-core-closure
base_commit: c71b5caa
head_commit: c71b5caa
worktree_dirty: true
sources:
  - brain/evidence/20260821T074514Z--claude--modpack-combat-core-closure.md
related: []
supersedes: []
tags:
  - multi-ai
  - handoff
  - modpack
---

# Handoff — Modpack dependency closure

## Owner intent and scope

Andrew selected the P0 dependency-closure fix over further vault work. Scope was
the closure and its detection only; making CI boot the real pack is the second
P0 and was deliberately excluded.

## Outcome

`shadowslave` requires `combat_core` at an exact version; the manifest shipped
only GeckoLib and SmartBrainLib, so the assembled pack could not boot. The
manifest now ships Combat Core, and a fail-closed check detects the whole class
of regression.

## Files changed

- `modpack/tools/check_dependency_closure.py` — new; compares `required`
  dependencies in `neoforge.mods.toml` against the manifest
- `modpack/manifest.json` — added the `combat-core-0` component
- `modpack/tools/validate_manifest.py` — new `local_gradle_build` source type
- `modpack/tools/build_package.py` — places a locally built component and skips
  the pinned-digest check for it
- `modpack/tests/test_check_dependency_closure.py` — new, 9 tests
- `modpack/tests/test_validate_manifest.py` — 8 tests for the new source type
- `modpack/tests/test_build_package.py`, `test_verify_package.py` — fixture
  builders taught about components without a pinned digest

`.github/workflows/` is unchanged. Nothing under `brain/`, `mod/`,
`combat-core/`, or `shadowslave/` was modified.

## Acceptance criteria

Met. The closure check exits 1 on the pre-fix manifest and 0 after; the schema
still rejects a remote component missing its digest; all existing modpack tests
pass.

## Verification performed

```
python modpack/tools/check_dependency_closure.py   -> exit 1 before, exit 0 after
python modpack/tools/validate_manifest.py          -> OK
python -m unittest discover -s modpack/tests       -> 50 tests, OK (was 33)
python modpack/tools/build_package.py ...          -> archive contains mods/combat-core.jar
python modpack/tools/verify_package.py ...         -> OK
```

## Evidence and artifacts

[[brain/evidence/20260821T074514Z--claude--modpack-combat-core-closure]],
including the exact pre-fix failure output and the assembled archive listing.

## Unperformed checks

- **The pack was never booted.** Declared closure and archive assembly only.
- **No Gradle build was run.** Fixture JARs stood in for the real artifacts.
- No client or dedicated-server launch, no GameTest, no runtime check.
- CI still packages a fixture and never boots the archive.

## Known risks

- The closure check compares `modId`s and ignores version ranges, so a component
  present at an incompatible version still passes. Version-range evaluation
  would need a real Maven-range parser.
- `local_gradle_build` components are not digest-verified at package time by
  design; their integrity rests on the generated provenance and on building from
  a known commit.
- The manifest now describes a pack no automated gate has ever launched. The
  closure is necessary but not sufficient for a bootable pack.

## Lore classifications

N/A. Packaging and tooling only.

## Explicitly deferred

The second P0: modpack CI writes a literal `ci-core-fixture` and never boots the
assembled archive. Until that changes, this fix is protected by local checks and
unit tests but not by CI. It remains the highest-value open packaging item.

## Next safe action

Make `.github/workflows/modpack-shell.yml` build the real Shadow Slave and
Combat Core JARs, assemble the pack from them, extract it, and boot a client and
dedicated server — adding `check_dependency_closure.py` to that workflow so the
gap cannot reopen. Note `PROJECT-STATUS.md` records hosted runners have been
unavailable, so that work may not be executable here.

The bounded, executable next step is running the closure check and the modpack
suite against a real Gradle build of both JARs, which this agent did not do.
