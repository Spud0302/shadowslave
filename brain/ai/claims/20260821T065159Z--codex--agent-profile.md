---
uid: ss-claim-20260821-codex-agent-profile
record_kind: claim
authority: context
lore_class: "N/A"
state: closed
owner: Codex
tool: Codex desktop
task_id: codex-agent-profile-20260821
created: 2026-08-21
updated: 2026-08-21
branch: codex/combat-core-standalone
worktree: primary
base_commit: 7223e140d6253caa0d0a69635f76b57e3f94e0ed
lease_until: 2026-08-21T08:52:16Z
targets:
  - brain/ai/agents/README.md
  - brain/ai/agents/codex.md
  - brain/templates/agent-profile.md
  - brain/ai/README.md
  - brain/home.md
excludes:
  - brain/protocol/
  - brain/decisions/
  - combat-core/
  - mod/
depends_on:
  - brain/protocol/authority-model.md
  - brain/protocol/ai-collaboration.md
overlaps_with: []
tags:
  - multi-ai
  - claim
  - agent-profile
---

# Claim — Codex agent profile

## Owner intent

Announce Codex in the shared vault and explain its identity, strengths, limitations, and preferred collaboration style.

## Exact scope

Create a self-authored Codex profile, an index for AI collaborator profiles, and a reusable profile template. Add navigation links from the existing AI and vault indexes.

## Acceptance criteria

- Codex's stable identity and session-dependent details are distinguished.
- Strengths and limitations are candid and task-oriented.
- The note gives a concrete request format and feedback guidance.
- The profile is context only and cannot promote itself to project authority.
- Other AI collaborators can use the same structure without editing Codex's profile.

## Target paths

Only the files listed in `targets` and this claim.

## Explicit exclusions

No implementation, lore, decision, protocol, or release changes.

## Dependencies and overlaps

No overlapping active claim was found for these paths.

## Coordination notes

The project owner explicitly requested this profile and therefore authorized links from the maintainer-owned indexes.

## Closure

Closed after scoped validation passed. See [[brain/ai/handoffs/20260821T070340Z--codex--agent-profile|the immutable handoff]].
