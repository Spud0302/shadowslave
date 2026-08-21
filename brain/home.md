---
uid: ss-brain-home
record_kind: index
authority: context
lore_class: "N/A"
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - PROJECT-STATUS.md
  - docs/CURRENT-PREVIEW-SUMMARY.md
  - GPT_HANDOFF.md
tags:
  - shadow-slave
  - second-brain
---

# Shadow Slave project brain

> [!important]
> This brain is the navigation and collaboration layer. Current code, reproducible evidence, explicit owner decisions, and the repository authority documents remain the source of truth.

## Start a task

1. Read [[brain/protocol/authority-model|Authority model]].
2. Read [[brain/protocol/ai-collaboration|AI collaboration protocol]].
   Several AI agents work here at once — [[brain/protocol/concurrent-editing|what
   is normal and what is not]] covers meeting another agent mid-task.
3. Select a bounded packet from [[brain/ai/README|AI operations]].
4. Check [[brain/ai/claims/README|active task claims]] before changing files.
5. End with reproducible evidence and an immutable handoff.

## Current project focus

- [[brain/design/combat-v1|Combat v1]] is a Chainback-first vertical slice.
- [[brain/lore/chainback|Chainback]] is project-authored DESIGN, not a claimed canonical creature.
- [[brain/implementation/chainback-traceability|Chainback traceability]] connects the design to code, presentation, tests, and remaining physical review.
- [[brain/design/deferred-scope|Deferred scope]] protects the slice from becoming a prerequisite tree.

## Explore

- [[brain/ai/agents/README|AI collaborators and capability profiles]]
- [[brain/maps/project-overview.canvas|Project overview map]]
- [[brain/maps/canon-to-code.canvas|Canon-to-code map]]
- [[brain/maps/combat-v1-chainback.canvas|Combat v1 Chainback map]]
- [[brain/lore/index|Lore and source index]]
- [[brain/design/index|Game-design index]]
- [[brain/implementation/index|Implementation index]]
- [[brain/decisions/README|Decision records]]
- [[brain/evidence/README|Evidence records]]

## Capture

- New idea: copy [[brain/templates/idea|Idea template]] into [[brain/inbox/README|the inbox]] using a unique filename.
- New lore claim: use [[brain/templates/lore-entity|Lore entity template]] and attach chapter references.
- New decision: create a proposed record from [[brain/templates/decision|Decision template]].
- New task: create a claim from [[brain/templates/task-claim|Task claim template]].
- New AI collaborator: create a self-authored profile from [[brain/templates/agent-profile|Agent profile template]].

## Current authority shortcuts

- [[PROJECT-STATUS|Project status]]
- [[docs/CURRENT-PREVIEW-SUMMARY|Current preview summary]]
- [[GPT_HANDOFF|Current handoff]]
- [[docs/LORE-SOURCE-POLICY|Lore source policy]]
- [[docs/JAVA-LORE-ALIGNMENT|Java lore-alignment gate]]
- [[docs/PREVIEW-LORE-DECISIONS|Preview lore decisions]]
- [[combat-core/ROADMAP|Combat Core roadmap]]
- [[mod/IMPLEMENTATION-STATUS|Java implementation status]]

## Health and drift

Before release or broad implementation work, read [[brain/implementation/authority-drift-register|Authority drift register]]. It records contradictions and packaging/test gaps without silently rewriting historical documents.
