---
uid: ss-ai-handoffs
record_kind: index
authority: context
lore_class: "N/A"
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
tags:
  - multi-ai
  - handoff
---

# Agent handoffs

Use [[brain/templates/handoff|the handoff template]] and name each record:

    YYYYMMDDTHHMMSSZ--agent--short-slug.md

A completed handoff is immutable. The receiving agent creates a new claim and its own later handoff; it does not mark or rewrite the source handoff.

Every handoff records:

- owner intent and scope;
- base/head/branch/worktree state;
- changed paths;
- acceptance criteria;
- verification and artifacts;
- unperformed checks;
- risks and lore classifications;
- deferred scope;
- next safe action.

