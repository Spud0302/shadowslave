---
uid: 20260825T065217Z-claude-code-ci-modpack-combat-core-fixture
record_kind: claim
authority: context
lore_class: "N/A"
state: active
owner: claude-code
tool: claude-code
task_id: 20260825T065217Z-claude-code-ci-modpack-combat-core-fixture
created: 2026-08-25
updated: 2026-08-25
branch: packaging/combat-core-closure
worktree: primary
base_commit: edf48a7b5db15f277b052060d0565951d259954a
lease_until: 2026-08-25T10:52:17Z
targets:
  - .github/workflows/modpack-shell.yml
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — CI modpack shell: supply combat-core fixture JAR

## Owner intent

Unblock the failing `validate` check on PR #3 (`packaging/combat-core-closure`)
so the branch can be reviewed and merged on its own merits rather than on a CI
gap the branch itself introduced.

## Exact scope

`modpack/manifest.json` on this branch adds `combat-core-0` as a **required**
component. `.github/workflows/modpack-shell.yml` builds its deterministic
fixture package by passing one `--component-jar` per pinned component, and was
never updated for the new component. `modpack/tools/build_package.py` fails
closed on the missing component, so the job aborts with:

    ERROR: required component JAR was not supplied: combat-core-0

Scope is exactly that: add the missing fixture JAR to the workflow's build step.
No change to the manifest, the packaging tools, or the component contract.

`combat-core-0` declares `source.type: local_gradle_build` and carries no
pinned `sha256`, so a synthetic fixture is the correct input here — the same
treatment `shadowslave-core.jar` already gets in this job. The two third-party
components (`geckolib-4`, `smartbrainlib-1`) keep their real downloads and
SHA-256 pin checks, because the manifest pins their digests.

This job validates the packaging **shell**, not compiled artifacts. Building a
real `combat-core` JAR here would need a JDK, a Gradle run and a much larger
timeout, and would duplicate what `combat-core.yml` already covers.

## Acceptance criteria

- `python3 modpack/tools/validate_manifest.py` passes.
- `python3 -m unittest discover -s modpack/tests` passes (50 tests).
- `build_package.py` produces an archive containing `mods/combat-core.jar` at
  the manifest's declared `package_path`.
- The `validate` check on PR #3 goes green.

## Target paths

- `.github/workflows/modpack-shell.yml` (build step only)

## Explicit exclusions

- `modpack/manifest.json` — the component definition is the PR's own work.
- `modpack/tools/**` — fail-closed behaviour is correct and stays as-is.
- Every other workflow.

## Dependencies and overlaps

Depends on the `combat-core-0` component added by claim
`20260821T073922Z--claude--modpack-combat-core-closure`. No active claim
overlaps `.github/workflows/`.

## Coordination notes

Verified locally on `packaging/combat-core-closure` merged with `main`
(clean merge, no conflicts). The two third-party JAR downloads could not be
exercised in this sandbox — the proxy returns 403 for `dl.cloudsmith.io` — so
the build was verified against a scratch manifest copy carrying fixture
digests for those two components only. The pinned-digest path itself is
unchanged by this claim and is exercised by CI.

## Closure

Closed 2026-08-25 by claude-code: fixture JAR added to the workflow build step
and verified locally; CI outcome recorded on PR #3.
