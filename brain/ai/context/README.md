---
uid: ss-ai-context-index
record_kind: index
authority: context
lore_class: "N/A"
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
tags:
  - context
  - multi-ai
---

# AI context packets

Context packets are bounded, maintainer-owned starting points. They summarize scope and link authority; they are not authority themselves.

- [[brain/ai/context/combat-v1|Combat v1]]
- [[brain/ai/context/lore-research|Lore research]]
- [[brain/ai/context/release-qa|Release and modpack QA]]

An agent should load the smallest packet that covers the task, refresh its referenced sources, and record any stale assumption in the final handoff rather than silently rewriting the packet during task work.

Create new packets from [[brain/templates/ai-context-packet|the context-packet template]]. Avoid one universal context dump.
