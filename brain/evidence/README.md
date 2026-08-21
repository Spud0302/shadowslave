---
uid: ss-evidence-index
record_kind: index
authority: context
lore_class: "N/A"
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
tags:
  - evidence
  - index
---

# Evidence records

Use one immutable file per research or verification result. Copy [[brain/templates/evidence|the evidence template]] and name it YYYYMMDDTHHMMSSZ--agent--short-slug.md.

Evidence must state:

- claim tested;
- exact branch, commit, and dirty status;
- environment and method or command;
- observed result;
- artifact, hash, log, chapter, or source path;
- limitations and unperformed checks;
- related claim, decision, or handoff.

Passed, failed, deferred, and not run are distinct. A test that did not execute is not a pass. Human feel checks may be deferred, but deferred is never reported as passed.

Evidence does not automatically authorize a design or release.

