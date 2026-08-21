---
uid: ss-ai-logs
record_kind: index
authority: context
lore_class: "N/A"
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
tags:
  - multi-ai
  - logs
---

# Agent run logs

Use one append-only log per task and agent for long-running or multi-session work. The file is owned by that agent while the claim is active and becomes immutable when handed off.

Do not use one shared daily log. Separate files avoid merge conflicts and preserve attribution.

A run log is operational context, not authority. Promote durable results into evidence, decisions, or updated authority documents through the normal review path.

