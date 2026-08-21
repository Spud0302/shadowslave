---
uid: ss-context-dream-realm-slice
record_kind: context
authority: context
lore_class: DESIGN
state: active
owner: Andrew
created: 2026-08-21
updated: 2026-08-21
sources:
  - docs/DREAM-REALM-REGION-CONTENT-WAVE1.md
  - docs/DREAM-REALM-STORY-CONTENT-WAVE1.md
  - PROJECT-STATUS.md
tags:
  - context
  - dream-realm
  - ashen-expanse
  - cinder-rest
---

# Context packet — Dream Realm vertical slice

## Goal

Provide a bounded context packet for Dream Realm region exploration, Ashen Expanse / Cinder Rest staging, resource collection, and Story NPC interaction without broadening into procedural world generation trees.

## Must read

- [[docs/DREAM-REALM-REGION-CONTENT-WAVE1]] — Regional definitions and boundary contracts.
- [[docs/DREAM-REALM-STORY-CONTENT-WAVE1]] — Story NPC interaction and dialogue logic.
- [[PROJECT-STATUS]] — Representative slice boundaries.
- [[docs/design/MODULAR-JAR-BOUNDARIES]] — Provider separation.

## Do

- Maintain strict server-authoritative state for NPC dialogues, dream anchors, and resource gathering.
- Keep region content behind modular provider boundaries (e.g. optional region modules).
- Preserve player progression attachments (Dreamer/Sleeper status, Soul rank).
- Test dimension transitions, anchor interactions, and persistence across server restarts.

## Do not

- Do not implement unbounded worldgen biomes or infinite custom dimension trees.
- Do not make story dialogue client-authoritative.
- Do not bypass `DreamRealmPreviewService` or create duplicate registry hooks in the core JAR.

## Acceptance

Entering Dream Realm $\rightarrow$ locating Cinder Rest anchor $\rightarrow$ interacting with Story NPC $\rightarrow$ deterministic dialogue state progression $\rightarrow$ safe return transition.
