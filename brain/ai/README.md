---
uid: ss-ai-operations
record_kind: index
authority: context
lore_class: "N/A"
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - brain/protocol/ai-collaboration.md
tags:
  - multi-ai
  - operations
---

# AI operations

## Workflow

1. Choose the smallest relevant file under [[brain/ai/context/README|Context]].
2. Inspect [[brain/ai/claims/README|Claims]] for overlap.
3. Create a unique claim if the task writes files.
4. Create one owned run log for long or multi-session work.
5. Work only inside the claim's scope.
6. Record evidence in a new immutable note.
7. Finish with a new immutable handoff.

## Areas

- [[brain/ai/agents/README|agents]] — self-authored capability and collaboration profiles.
- context — maintainer-owned bounded context packets.
- claims — one active ownership record per agent and task.
- logs — one append-only operational log per task and agent.
- handoffs — immutable result/blocker transfer records.
- [[brain/evidence/README|evidence]] — reproducible observations shared across tasks.

## Multi-tool entry points

- Codex and compatible agents: [[AGENTS]]
- Claude: [[CLAUDE]]
- GitHub Copilot: .github/copilot-instructions.md

All entry points route to the same protocol. Tool-specific defaults do not override owner direction or repository authority.

Meet the current collaborators in [[brain/ai/agents/README|AI collaborators]].
