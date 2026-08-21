---
uid: 20260821T062438Z-codex-vault-validation
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: closed
owner: Codex
task_id: 20260821T061640Z-codex-vault-bootstrap
created: 2026-08-21
updated: 2026-08-21
branch: codex/combat-core-standalone
captured_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
worktree_dirty: true
sources:
  - .obsidian/core-plugins.json
  - .obsidian/templates.json
  - brain/
related:
  - ss-adr-20260821-project-brain
tags:
  - evidence
  - obsidian
  - validation
---

# Evidence — Project-brain vault validation

## Claim tested

The new repository-root Obsidian vault has parseable shared configuration, valid JSON Canvas maps, resolvable internal wiki links, frontmatter boundaries, and no whitespace errors in its additive diff.

## Environment

- Windows PowerShell
- Branch codex/combat-core-standalone
- Head 7223e140d6253caa0d0a69635f76b57e3f94e0ed
- Dirty worktree existed before this vault task

## Observed results

- Shared Obsidian JSON files parsed with ConvertFrom-Json.
- project-overview.canvas: 8 unique nodes, 7 valid edges.
- canon-to-code.canvas: 9 unique nodes, 7 valid edges.
- combat-v1-chainback.canvas: 13 unique nodes, 11 valid edges.
- Every Canvas file node points to an existing vault file.
- Every Canvas edge points to existing node IDs.
- Internal wiki-link resolution check passed.
- All 36 initial brain Markdown files had frontmatter boundaries.
- git diff --check passed for the new vault, brain, and AI entry-point paths.

## Limitations

- The vault was not added to the user's Obsidian vault switcher automatically.
- Obsidian may normalize its app-owned core-plugin and template JSON after opening.
- Full YAML semantic parsing was unavailable in the local shell; the frontmatter boundary and link checks passed.
- No Gradle, Minecraft, lore-research, or modpack runtime gate was required or run because this was an additive knowledge-workspace change.

## Conclusion

The vault is structurally ready to open. The user should select the repository root as the vault and review [[brain/protocol/vault-setup]].

