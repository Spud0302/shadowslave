---
uid: 20260825T065911Z-claude-code-vault-link-repair-and-ci
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: claude-code
tool: claude-code
task_id: 20260825T065911Z-claude-code-vault-link-repair-and-ci
created: 2026-08-25
updated: 2026-08-25
branch: claude/shadow-slave-github-connect-x6nqra
worktree: primary
base_commit: d12c6b2d118dc3cc1e6cb009f4fad1bcaf7cad1d
lease_until: 2026-08-25T10:59:11Z
targets:
  - brain/home.md
  - brain/design/combat-v1.md
  - brain/design/index.md
  - brain/design/deferred-scope.md
  - brain/implementation/index.md
  - brain/implementation/chainback-traceability.md
  - brain/ai/context/combat-v1.md
  - brain/ai/context/modpack-packaging.md
  - brain/lore/chainback.md
  - brain/maps/combat-v1-chainback.canvas
  - brain/manifest.json
  - brain/tools/build_manifest.py
  - brain/tools/test_build_manifest.py
  - .github/workflows/vault.yml
excludes: []
depends_on: []
overlaps_with: []
tags:
  - multi-ai
  - claim
---

# Claim — Repair ROADMAP links and gate vault integrity in CI

## Owner intent

Owner request, 2026-08-25: fix the dangling `combat-core/ROADMAP` references and
add the missing CI gate for vault integrity.

## Exact scope

Two defects found while reviewing the vault after PR #3 merged.

**1. `combat-core/ROADMAP.md` never existed.** `git log --all` on that path is
empty — it was never created, not deleted. It is referenced 12 times: as a
`sources:` frontmatter entry in 9 notes, as a wikilink in 5 note bodies, and as
a node in `brain/maps/combat-v1-chainback.canvas`. Accounts for 7 of the
vault's 26 warnings (6 `LINK_BROKEN`, 1 `CANVAS_LINK_BROKEN`).

Repointing at `combat-core/README.md` rather than authoring a new ROADMAP.
The README already carries the referenced material under *Current MVP state*
and *Explicitly deferred*, and per the authority model an agent may not invent
roadmap content — that is owner design authority, not a link repair.

**2. Vault integrity is not gated.** `validate_vault.py` and
`build_manifest.py --check` exist and are the vault's own correctness checks,
but no workflow runs either, so the brain can rot without CI going red.
`brain/manifest.json` is currently stale on `main` and nothing noticed.

Adding `.github/workflows/vault.yml` to run, on `brain/**` changes:
the vault validator, a broken-link gate, the manifest freshness check, and the
vault tool tests.

**3. `build_manifest.py --check` could never pass (found while wiring the gate).**
It compared `source_commit` and `source_tree_dirty` along with note content.
Both change without the vault changing: committing the manifest moves HEAD, so
the recorded `source_commit` is always one commit behind, and a CI checkout is
clean where a working tree is dirty. The manifest was therefore structurally
guaranteed to report stale — that is why it drifted on `main` unnoticed, and
the check would have failed the new CI job on its first run.

Fixed by excluding provenance fields from the comparison, with a regression
test that commits between generate and check. Verified the test fails against
the unfixed tool.

**Gate design.** `validate_vault.py` classifies broken links as *warnings* and
exits 0, so running it alone would not have caught the ROADMAP defect this
claim exists to fix. A second step promotes `LINK_BROKEN` and
`CANVAS_LINK_BROKEN` to blocking while leaving other warnings advisory.

The job uses `fetch-depth: 0`. A default shallow checkout makes every
`base_commit`/`captured_commit` reference unresolvable, which would turn the
17 existing `COMMIT_UNKNOWN` warnings into noise proportional to vault size.

Gate on errors, not warnings — `--strict` is deliberately not used. 19 warnings
survive this claim (17 `COMMIT_UNKNOWN`, 2 `SNAPSHOT_STALE`) and are pre-existing
records issues, not gate failures. Turning them red would block every PR.

## Acceptance criteria

- `validate_vault.py` reports 0 errors and 19 warnings, with no `LINK_BROKEN`
  or `CANVAS_LINK_BROKEN` remaining.
- `build_manifest.py --check` exits 0, and still exits 0 after the manifest is
  committed.
- The vault tool tests pass, including a new regression test that fails against
  the unfixed tool.
- The link gate exits 1 on a reintroduced dangling wikilink.
- Every repointed link resolves to `combat-core/README.md`.

## Target paths

See `targets` in frontmatter.

## Explicit exclusions

- The 17 `COMMIT_UNKNOWN` warnings — three cite `acf4ed5f`, a commit absent from
  this repository, likely from rewritten history. Rewriting another agent's
  immutable claim frontmatter is out of scope and forbidden by the protocol.
- The 2 `SNAPSHOT_STALE` living notes — refreshing those is a content task
  requiring a real repository survey, not a link repair.
- Authoring `combat-core/ROADMAP.md`.
- Every workflow other than the new `vault.yml`.

## Dependencies and overlaps

None. No other active claim names these paths.

## Coordination notes

`brain/maps/combat-v1-chainback.canvas` is a Canvas — one writer at a time per
the concurrent-editing protocol. Changing exactly one node's `file` value.

## Closure

Closed 2026-08-25 by claude-code. All acceptance criteria met and verified on
PR #4 run 32819779073 (job `vault`, conclusion success):

- `88 note(s) checked, 0 error(s), 19 warning(s)` — no LINK_BROKEN or
  CANVAS_LINK_BROKEN remain.
- `88 notes checked, 0 error(s), 0 blocking link warning(s), 19 advisory
  warning(s)` — the link gate ran and passed.
- `Manifest is up-to-date.` on a clean checkout at a commit other than the one
  recorded in the manifest, which is the case the provenance fix exists for.
- `Ran 64 tests ... OK`.

Claim closure is not proof of correctness: the 17 COMMIT_UNKNOWN and 2
SNAPSHOT_STALE warnings are untouched and still open work, and the link gate
protects against dangling references only — it cannot tell whether a resolved
link points at the *right* note.
