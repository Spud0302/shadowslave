---
uid: 20260821T073922Z-claude-modpack-combat-core-closure
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude
tool: claude-code-opus-5
task_id: 20260821T073922Z-claude-modpack-combat-core-closure
created: 2026-08-21
updated: 2026-08-21
branch: packaging/combat-core-closure
worktree: primary
base_commit: c71b5caa
lease_until: 2026-08-21T15:39:22Z
targets:
  - modpack/manifest.json
  - modpack/tools/validate_manifest.py
  - modpack/tools/build_package.py
  - modpack/tools/check_dependency_closure.py
  - modpack/tests/
  - modpack/README.md
excludes:
  - brain/
  - combat-core/
  - mod/
  - shadowslave/
  - testserver/
  - docs/
  - .github/workflows/
depends_on: []
overlaps_with:
  - 20260821T073644Z-codex-modpack-shadow-slave-findings-dump
tags:
  - multi-ai
  - claim
  - modpack
  - packaging
---

# Claim — Modpack dependency closure for Combat Core

> Scaffold: `python brain/tools/new_record.py claim --agent <slug> --slug <short-slug>`

## Owner intent

Andrew selected the P0 dependency-closure fix from
[[brain/implementation/authority-drift-register]] as the next task, over further
vault work.

## The defect, verified

`mod/src/main/templates/META-INF/neoforge.mods.toml` declares five `required`
dependencies. Two are platform-level and covered by the manifest's `pack` block:

| modId | type | covered by |
| --- | --- | --- |
| `neoforge` | required | `pack.loader.version` |
| `minecraft` | required | `pack.minecraft_version` |
| `combat_core` | required | **nothing** |
| `geckolib` | required | component `geckolib-4` |
| `smartbrainlib` | required | component `smartbrainlib-1` |

`combat_core` is pinned to an exact version, `[${combat_core_version}]` =
`0.0.4-wip`. A `required` dependency that is absent makes NeoForge refuse to load
`shadowslave`, so **the assembled pack cannot boot**. Modpack CI does not catch
it because it packages a literal `ci-core-fixture` and never boots the archive.

## Why this is more than a JSON edit

The manifest schema cannot currently express a first-party, locally-built
dependency:

- `validate_manifest.py` restricts `source.type` to `modrinth`, `curseforge`, and
  `direct`.
- Every component source requires a fixed `sha256`, and `build_package.py`
  rejects a supplied JAR whose digest differs.

Combat Core is built from `combat-core/` in this repository, so its digest
changes on every build and cannot be pinned in advance. The pinned digest exists
to verify third-party downloads; a first-party artifact built from a known commit
is verified by the generated provenance, which `build_package.py` already
records.

## Exact scope

1. Extend the manifest schema with a `local_gradle_build` component source
   carrying `artifact_glob` and `package_path` instead of `file` and `sha256`.
   Remote sources keep their mandatory digest.
2. Add the `combat-core` component to `modpack/manifest.json`.
3. Teach `build_package.py` to place a locally built component and record its
   digest in provenance rather than checking it against the manifest.
4. Add `check_dependency_closure.py`, which parses the declared `required`
   dependencies from `neoforge.mods.toml` and fails closed when the manifest does
   not cover one. This is the recurrence prevention; without it the same class of
   gap returns silently.
5. Tests for each, including a negative case that removes a component and proves
   the closure check fails.

## Acceptance criteria

- `check_dependency_closure.py` exits non-zero on today's manifest before the
  fix, and zero after it.
- `validate_manifest.py` accepts the extended manifest and still rejects a
  remote component missing its digest.
- Existing `modpack/tests` continue to pass.
- Every new check has a test proving it fires.

## Target paths

Listed in `targets`.

## Explicit exclusions

Listed in `excludes`. In particular `.github/workflows/` is **not** in scope:
making CI boot the real assembled pack is the second P0 and a separate task.
This claim fixes the closure and adds the check that detects it; it does not
change what CI runs.

## Dependencies and overlaps

`20260821T073644Z-codex-modpack-shadow-slave-findings-dump` is an active Codex
claim whose title covers modpack territory. It declares `targets: []` and its
scope sections are unfilled, so it formally holds no path and appears to be
analysis rather than this fix. Recorded here as an overlap; if Codex needs these
files, co-editing rules apply.

## Coordination notes

A claim with empty `targets` cannot be checked by `agent_brief.py --paths`. That
is a real gap in the coordination mechanism, noted here for the backlog rather
than fixed under this claim.

## Closure

Closed 2026-08-21.

- Evidence: [[brain/evidence/20260821T074514Z--claude--modpack-combat-core-closure]]
- Handoff: [[brain/ai/handoffs/20260821T074514Z--claude--modpack-combat-core-closure]]

The defect was reproduced before the fix: `check_dependency_closure.py` exited 1
naming `combat_core`. It exits 0 now, and `modpack/tests` went from 33 to 50.

The pack was never booted. Declared closure only.

Claim closure is not proof of success.
