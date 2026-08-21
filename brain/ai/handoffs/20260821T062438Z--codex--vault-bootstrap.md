---
uid: 20260821T062438Z-codex-vault-bootstrap-handoff
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: Codex
task_id: 20260821T061640Z-codex-vault-bootstrap
created: 2026-08-21
updated: 2026-08-21
branch: codex/combat-core-standalone
base_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
head_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
worktree_dirty: true
sources:
  - brain/evidence/20260821T062438Z--codex--vault-validation.md
related:
  - ss-adr-20260821-project-brain
tags:
  - multi-ai
  - handoff
  - obsidian
---

# Handoff — Obsidian project-brain bootstrap

## Owner intent and scope

Create a mini second brain for the Shadow Slave project that can be added to Obsidian and safely used by multiple AI tools.

## Outcome

- The existing repository root is configured as the vault.
- brain contains navigation, authority, lore, design, implementation, decisions, evidence, inbox, AI operations, templates, and three Canvas maps.
- Codex, Claude, and Copilot entry-point files route to one shared protocol.
- Context packets cover Combat v1, lore research, and release/modpack QA.
- One-writer claims, append-only logs, immutable handoffs, immutable accepted decisions, and reproducible evidence are defined.
- Chainback and Combat v1 are seeded without promoting project DESIGN to canon.
- A dated drift register records dependency-closure, CI, status, and collaboration inconsistencies.

## Files changed

Only new paths were added:

- .obsidian/
- brain/
- AGENTS.md
- CLAUDE.md
- .github/copilot-instructions.md

No pre-existing project source or documentation file was rewritten.

## Verification

See [[brain/evidence/20260821T062438Z--codex--vault-validation]].

## Unperformed checks

- Obsidian UI open/render check.
- OneDrive Always keep on this device setting.
- Runtime/build/GameTest work unrelated to the vault bootstrap.

## Known risks

- The app may rewrite internal .obsidian JSON formats.
- Shared Canvas edits remain merge-conflict-prone and must have one active owner.
- Current project status contains known drift recorded in [[brain/implementation/authority-drift-register]].

## Explicitly deferred

- No lore archive or missing external lore vault was imported.
- No existing authority file was promoted, reconciled, or rewritten.
- No community Obsidian plugin was installed.
- No packaging or CI gap was fixed.

## Next safe action

Open the repository root as an Obsidian vault, open [[brain/home]], and use one context packet plus a unique claim for the next substantive AI task.

