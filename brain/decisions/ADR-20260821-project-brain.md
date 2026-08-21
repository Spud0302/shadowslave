---
uid: ss-adr-20260821-project-brain
record_kind: decision
authority: project-authority
lore_class: "N/A"
state: accepted
owner: Andrew
recorded_by: Codex
created: 2026-08-21
updated: 2026-08-21
sources:
  - brain/protocol/authority-model.md
  - brain/protocol/ai-collaboration.md
tags:
  - decision
  - obsidian
  - multi-ai
---

# ADR — Repository-root Obsidian project brain

## Context

The project has extensive lore research, design constraints, runtime evidence, historical reviews, and multiple AI handoffs. A separate nested vault would hide or duplicate repository authority and make links drift.

## Decision

- Treat the existing repository root as the Obsidian vault.
- Keep the operational second brain under brain.
- Keep Markdown notes canonical and Canvas files as navigation views.
- Link existing repository documents rather than copying them.
- Require explicit authority and lore classification metadata.
- Use one-writer task claims, append-only run logs, immutable handoffs, and immutable accepted decisions.
- Provide bounded AI context packets instead of asking every agent to ingest the whole repository.
- Use Obsidian core features only as the baseline.
- Keep OneDrive as the existing sync layer and leave Obsidian Sync disabled for this vault.

## Consequences

- Obsidian can link directly to status documents, lore research, code, assets, and tests.
- Codex, Claude, Copilot, and other filesystem-capable agents share the same durable records.
- Parallel agents must coordinate shared Canvas, Home, and protocol edits.
- Vault summaries remain derived context and cannot silently outrank repository authority.
- App-owned Obsidian configuration may be normalized by future Obsidian versions.

## Rejected alternative

A nested Shadow-Slave-Brain vault was rejected because Obsidian links and Canvas file nodes cannot reliably treat files outside the vault as first-class knowledge.

