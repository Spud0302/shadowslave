---
uid: ss-implementation-drift-20260821
record_kind: evidence
authority: evidence
lore_class: "N/A"
state: active
owner: Codex
created: 2026-08-21
updated: 2026-08-21
captured_commit: 7223e140d625
worktree_dirty: true
sources:
  - modpack/manifest.json
  - .github/workflows/modpack-shell.yml
  - .github/workflows/java-core.yml
  - mod/build.gradle
  - mod/gradle.properties
  - PROJECT-STATUS.md
tags:
  - qa
  - documentation-drift
  - packaging
---

# Authority drift and release-doctor register

These are evidence-backed inconsistencies, not automatic authorization to rewrite history or broaden runtime scope.

| Priority | Finding | Current evidence | Safe next action |
| --- | --- | --- | --- |
| P0 | Pack dependency closure omits Combat Core | mod/build.gradle and generated metadata require combat_core 0.0.4-wip, while modpack/manifest.json lists only GeckoLib and SmartBrainLib | Add Combat Core to the manifest/package and validate the complete dependency closure |
| P0 | Modpack CI tests a fixture rather than the assembled game | modpack-shell.yml writes literal ci-core-fixture and never boots that archive | Build the real Shadow Slave and Combat Core JARs, assemble the pack, extract it, then boot client and dedicated server |
| P1 | GameTests are configured but absent from current CI execution | mod/build.gradle defines gameTestServer; java-core.yml runs build/client/server smoke but not runGameTestServer | Add runGameTestServer and archive logs/crash reports |
| P1 | Status and version documents span multiple preview eras | Current properties and local summary say preview.4; several testing, handoff, modpack, and collaboration documents still describe preview.1 or preview.2 | Add a version-consistency/release-doctor check; preserve historical evidence labels |
| P1 | Implementation status disagrees with newer project status | mod/IMPLEMENTATION-STATUS.md retains older non-claims while PROJECT-STATUS.md records representative Dream Realm, Memory, Echo, creature, and combat slices | Reconcile current authority documents through an explicit review |
| P2 | Collaboration rules contain old tool-specific assumptions | docs/COLLABORATION.md mandates gpt branches/no GPT merge, while current owner-authorized handoff permits scoped admin work and this branch uses codex prefix | Supersede with a tool-neutral accepted protocol; retain the older file as history |
| P2 | Historical research references an unavailable external lore vault | Engineering notes mention Shadow Slave - Lore Reference outside this Windows workspace | Use this brain as navigation only until any missing lore authority is deliberately imported or superseded |
| P2 | Older content waves retain stale not-wired banners | Parts of Wave 1 content now have representative runtime implementations | Treat old banners as dated evidence; verify current code before updating status |

## Completion definition

Close an item only with:

- exact changed paths;
- exact branch and commit;
- the verification command and result;
- artifact/log links;
- unperformed checks;
- a new immutable evidence record.

