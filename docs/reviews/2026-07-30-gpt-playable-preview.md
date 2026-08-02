# GPT bulk handoff — playable Java preview

**PR:** #19 — `feat(mod): build playable Java preview batch`  
**Branch:** `gpt/live-datapack-import`  
**Target artifact:** `shadowslave-0.1.0-preview.1.jar`  
**Review timing:** after GPT has produced the installable preview artifact

This is the accumulated Claude handoff requested by Andrew. Intermediate packages were not held for
Claude; tests and review criteria were built alongside them.

## Owner intent

Andrew wants a coherent pre-Claude-tested JAR he can install locally and use to feel the systems. This
is not a public release and not permission to call unperformed human checks passed.

Binding brief: `docs/PLAYABLE-PREVIEW-DIRECTIVE.md`.

## Change groups

### Transactional live datapack migration

- direct scoreboard/tag evidence reader;
- absent-score versus explicit-zero distinction;
- persistent imported and general identity records;
- provisional write, exact read-back, final marker;
- rollback on any failure;
- no legacy cleanup;
- operator migration command.

### Persistent Nightmare lifecycle

- Overworld `NightmareRegistryData`;
- one active instance per owner UUID;
- persistent return position, role, scenario, layout and pursuer ownership;
- one entry choke point;
- one owned-entity/registry teardown path;
- success, technical recovery, admin abort and canonical death handling;
- reconnect resume/recovery behaviour.

### Playable Last Signal slice

- bundled Nightmare dimension;
- separate play-space slots;
- assigned role: last watchkeeper;
- central conflict: restore the signal fire;
- optional combat pressure from one owned placeholder pursuer;
- right-click resolution and return.

### Preview appraisal and feel systems

- persistent `SoulIdentityData`;
- fixed DESIGN Aspect `[Last Light]`, Awakened Aspect Rank;
- server-owned `/shadowslave kindle` ability and cooldown;
- fixed DESIGN Flaw `[Cold Ash]`, Weakness in water/rain/bubbles;
- Soul screen displays formal names, independent ranks, ability and Flaw effect.

### Player package

- no-cheat preview commands;
- technical recovery and reset;
- install/play/feedback guide;
- lore classification ledger;
- cumulative automated/manual test matrix.

## Lore review

Read, in order:

1. `docs/JAVA-LORE-ALIGNMENT.md`;
2. `docs/PREVIEW-LORE-DECISIONS.md`;
3. `docs/PLAYABLE-PREVIEW-DIRECTIVE.md`.

The scenario and fixed appraisal are labelled DESIGN. Review specifically for any wording or domain
shape that accidentally presents them as canon.

## Verification

Run every command in `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`. Use `mod/verify-smoke.sh`; bare Gradle
smoke-task exit codes are not proof that Minecraft became ready.

The highest-risk review areas are:

- NeoForge SavedData load/save and one-owner invariant;
- interaction-event resolution firing exactly once;
- lifecycle state if teleport/appraisal throws;
- death/respawn attachment order;
- reconnect while an instance exists;
- multiplayer slot independence;
- exact migration rollback and score absence handling;
- physical client/server side separation after expanded snapshot/UI work.

## Explicit non-claims

- no natural infection;
- no canonical appraisal algorithm;
- no custom Nightmare Creature;
- no complete historical body/inventory system;
- no corpse Gate;
- no full imported ability/Flaw execution;
- no complete Dream Realm or later-rank progression;
- no human playthrough has passed yet;
- no public Java release exists.

## Expected handoff result

Record the final bulk verdict on PR #19 with exact commands, logs, fixes and remaining deferred evidence.
Do not merge merely because the preview JAR exists; review the accumulated architecture and lore boundary.
