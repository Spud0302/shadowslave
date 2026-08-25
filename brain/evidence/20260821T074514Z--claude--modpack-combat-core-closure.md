---
uid: 20260821T074514Z-claude-modpack-combat-core-closure
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: claude
task_id: 20260821T073922Z-claude-modpack-combat-core-closure
created: 2026-08-21
updated: 2026-08-21
branch: packaging/combat-core-closure
captured_commit: c71b5caa
worktree_dirty: true
sources:
  - modpack/manifest.json
  - modpack/tools/check_dependency_closure.py
  - modpack/tools/validate_manifest.py
  - mod/src/main/templates/META-INF/neoforge.mods.toml
related: []
supersedes: []
tags:
  - evidence
  - modpack
  - packaging
---

# Evidence — Modpack dependency closure

## Claim tested

That the P0 recorded in [[brain/implementation/authority-drift-register]] — the
pack manifest omitting Combat Core — is real, and that it is now closed and
detectable.

## Environment and method

Windows 11, Python 3.9.13. Branch `packaging/combat-core-closure` from
`c71b5caa`. Worktree dirty with unrelated Combat Core and Chainback work.

## Preconditions

`mod/src/main/templates/META-INF/neoforge.mods.toml` declares five `required`
dependencies. `modpack/manifest.json` declared two components.

## Command or research procedure

```
python modpack/tools/check_dependency_closure.py
python modpack/tools/validate_manifest.py
python -m unittest discover -s modpack/tests
python modpack/tools/build_package.py --manifest <fixture> --core-jar <fixture> \
    --component-jar combat-core-0=<fixture> --component-jar geckolib-4=<fixture> \
    --component-jar smartbrainlib-1=<fixture> --output <fixture>
python modpack/tools/verify_package.py <fixture>
```

## Observed result

**The defect, reproduced before the fix.** `check_dependency_closure.py` exited
1:

```
  MISSING combat_core      [${combat_core_version}] <- nothing in the manifest
ERROR: 1 of 5 required dependencies are not shipped by the pack: combat_core
```

`combat_core` is `type="required"` at an exact version, so NeoForge refuses to
load `shadowslave` without it. The assembled pack could not boot.

**After the fix.** All five covered, exit 0. `validate_manifest.py` OK.
`python -m unittest discover -s modpack/tests` — 50 tests, OK (was 33).

**End-to-end package build**, with fixture JARs:

```
  README.md
  manifest.json
  mods/SmartBrainLib-neoforge-1.21.1-1.16.11.jar
  mods/combat-core.jar
  mods/geckolib-neoforge-1.21.1-4.9.2.jar
  mods/shadowslave-core.jar
  provenance.json
```

`verify_package.py` OK, archive SHA-256
`8a3c81228b0b8e8b13f8bfe2af1b07a51a2327b2434af1539a4c162e10ae3715`. Provenance
recorded `mods/combat-core.jar` with digest `a63f5e63ca9c23ba...`.

## Design decision, and why

The manifest schema could not express a first-party locally-built dependency:
`source.type` was restricted to `modrinth`, `curseforge`, and `direct`, and every
component required a fixed `sha256` that `build_package.py` enforced.

Combat Core is built from `combat-core/` in this repository, so its digest
changes on every build and cannot be pinned in advance. A new
`local_gradle_build` source type carries `artifact_glob` and `package_path`
instead, and pinning `file` or `sha256` on one is **rejected** rather than
ignored — a digest that is never checked reads as a guarantee the build cannot
make. The digest that actually shipped is recorded in the generated provenance,
which is the appropriate verification for a first-party artifact.

## Artifacts, hashes, logs, or chapter references

Fixture archive built under the session scratchpad, not committed. Tools and
tests are the durable artifacts.

## Limitations and unperformed checks

- **The pack was never booted.** This proves *declared* closure and that a ZIP
  assembles with the right entries. It does not prove the real JARs resolve,
  load, or that Minecraft reaches readiness.
- **Built with fixture JARs, not real ones.** No Gradle build was run; neither
  `mod/build/libs` nor `combat-core/build/libs` was produced or used.
- CI still packages `ci-core-fixture` and never boots the archive. That is the
  second P0 and was explicitly out of scope; `.github/workflows/` is unchanged.
- The closure check compares declared `modId`s. It does not evaluate version
  ranges, so a component present at an incompatible version still passes.
- `neoforge` and `minecraft` are treated as satisfied by the pack platform
  rather than by a shipped component.

## Conclusion

The declared dependency closure is complete and a fail-closed check now detects
regressions. The pack has still never been booted, so no claim is made that it
runs.

An observation proves behaviour at one commit. It does not authorize a design,
merge, release, canon classification, or scope change.
