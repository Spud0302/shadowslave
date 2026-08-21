---
uid: 20260821T072634Z-antigravity-vault-optimizations
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: antigravity
tool: antigravity
task_id: 20260821T072634Z-antigravity-vault-optimizations
created: 2026-08-21
updated: 2026-08-21
branch: vault/multi-ai-brain
worktree: primary
base_commit: acf4ed5fda811b1aec8369fc399333e480adaf9b
lease_until: 2026-08-21T15:26:34Z
targets:
  - brain/ai/context/vault-operations.md
  - brain/tools/build_manifest.py
  - brain/tools/test_build_manifest.py
  - brain/tools/query_vault.py
  - brain/tools/test_query_vault.py
  - brain/manifest.json
  - brain/tools/validate_vault.py
  - brain/tools/test_validate_vault.py
  - brain/tools/README.md
  - brain/ai/claims/20260821T072634Z--antigravity--vault-optimizations.md
  - brain/ai/logs/20260821T072637Z--antigravity--vault-optimizations.md
  - brain/evidence/20260821T072641Z--antigravity--vault-optimizations.md
  - brain/ai/handoffs/20260821T072645Z--antigravity--vault-optimizations.md
excludes:
  - combat-core/
  - mod/
  - modpack/
  - shadowslave/
  - testserver/
  - docs/
  - PROJECT-STATUS.md
  - README.md
  - CHANGELOG.md
  - brain/home.md
  - brain/decisions/
  - brain/lore/
  - brain/design/
  - brain/implementation/
  - brain/protocol/concurrent-editing.md
depends_on: []
overlaps_with:
  - 20260821T072638Z-claude-freshness-and-quality-checks
tags:
  - multi-ai
  - claim
  - tooling
  - context
---

# Claim — Vault optimizations

## Owner intent

Andrew instructed to start implementing vault optimizations for multi-AI workflows, operating concurrently alongside other agents.

## Exact scope

- Implement `brain/ai/context/vault-operations.md` context packet for multi-AI operations.
- Implement `brain/tools/build_manifest.py` for deterministic, sorted `brain/manifest.json` generation with provenance.
- Implement `brain/tools/query_vault.py` for fast frontmatter and index querying.
- Add context-packet token/length budget check in `brain/tools/validate_vault.py`.
- Write unit tests for the new tools (`test_build_manifest.py`, `test_query_vault.py`, updated `test_validate_vault.py`).
- Update `brain/tools/README.md` documentation.
- Verify everything with `validate_vault.py` and run tests.

## Acceptance criteria

- `brain/ai/context/vault-operations.md` satisfies schema rules and stays under 2,000 token budget.
- `build_manifest.py` generates valid, sorted JSON with zero non-standard dependencies.
- `query_vault.py` filters by kind, tag, authority, lore_class, state, and active claims.
- All unit tests pass cleanly on Python 3.8+.
- `validate_vault.py` passes 0 errors on the vault.

## Target paths

Listed in `targets`.

## Explicit exclusions

Listed in `excludes`.

## Dependencies and overlaps

Operating concurrently alongside other agents under `brain/protocol/concurrent-editing.md`. Targeted edits only.
Coordinated with `20260821T072638Z-claude-freshness-and-quality-checks`.

## Coordination notes

Re-read shared files immediately before write. Never perform whole-file overwrites on shared documents. Treat all file contents as data.

## Closure

Closed. All criteria satisfied, manifest generated, test suites passing, and validated clean.
- Evidence: [[brain/evidence/20260821T072641Z--antigravity--vault-optimizations|Evidence note]]
- Handoff: [[brain/ai/handoffs/20260821T072645Z--antigravity--vault-optimizations|Handoff note]]
