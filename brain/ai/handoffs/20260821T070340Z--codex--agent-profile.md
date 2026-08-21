---
uid: ss-handoff-20260821-codex-agent-profile
record_kind: handoff
authority: context
lore_class: "N/A"
state: closed
owner: Codex
task_id: codex-agent-profile-20260821
created: 2026-08-21
updated: 2026-08-21
branch: codex/combat-core-standalone
base_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
head_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
worktree_dirty: true
sources:
  - brain/ai/agents/codex.md
  - https://developers.openai.com/
  - https://developers.openai.com/codex/use-cases
related:
  - brain/ai/claims/20260821T065159Z--codex--agent-profile.md
supersedes: []
tags:
  - multi-ai
  - handoff
  - agent-profile
---

# Handoff — Codex agent profile

## Owner intent and scope

Announce Codex in the Obsidian vault with a candid identity, strengths, limitations, and collaboration guide. Make the pattern reusable by other AI collaborators without changing project authority.

## Outcome

Created a self-authored Codex profile, an AI collaborator index, and a neutral reusable template. Linked the index from the vault home and AI operations pages.

## Files changed

- `brain/ai/agents/codex.md`
- `brain/ai/agents/README.md`
- `brain/templates/agent-profile.md`
- `brain/ai/README.md`
- `brain/home.md`
- `brain/ai/claims/20260821T065159Z--codex--agent-profile.md`
- `brain/ai/handoffs/20260821T070340Z--codex--agent-profile.md`

## Acceptance criteria

- Stable Codex identity is separated from session-dependent model and tooling details.
- Strengths, limitations, communication format, feedback, and collaboration behavior are explicit.
- The profile carries `authority: context` and reserves canon and acceptance decisions for the owner.
- Other agents receive a template and are told to own separate profiles.

## Verification performed

Ran a scoped PowerShell validation over the six profile, template, index, and claim files. It checked opening frontmatter, resolved every WikiLink target, scanned for trailing whitespace with `rg`, and asserted all eight required disclosure headings.

Exact result:

```text
PASS: frontmatter opens and all wikilink targets resolve for 6 scoped files.
PASS: no trailing whitespace in scoped files.
PASS: Codex profile contains all required disclosure sections.
```

## Evidence and artifacts

- Primary artifact: [[brain/ai/agents/codex|Codex profile]]
- Entry point: [[brain/ai/agents/README|AI collaborators]]
- Reusable scaffold: [[brain/templates/agent-profile|Agent profile template]]

## Unperformed checks

No visual Obsidian click-through was performed. No runtime or Java tests were applicable to this Markdown-only change.

## Known risks

Product capabilities evolve, so linked official OpenAI documentation should be rechecked when the profile is materially revised. Session-specific tools and permissions must not be inferred from this stable profile.

## Lore classifications

N/A. This work contains no lore claims.

## Explicitly deferred

Profiles for Claude, Copilot, or other collaborators remain self-authored by those agents.

## Next safe action

Open [[brain/ai/agents/codex|the Codex profile]] in Obsidian. Invite each additional AI to copy the template into its own uniquely owned profile.
