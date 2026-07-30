# Shadow Slave project status

**Status date:** 2026-07-30  
**Canonical repository:** `Spud0302/shadowslave`  
**Stable main:** Java `0.1.0-alpha.4`, Claude-verified  
**Active preview branch:** `gpt/live-datapack-import`, PR #19

## Products

| Product | Current state | Public release |
| --- | --- | --- |
| Vanilla datapack | completed and frozen behavioural reference | `datapack-v1.0.0` |
| Shared/standalone Java main | persistent Soul core, networking/UI and pure migration, Claude-verified | none |
| Java playable preview candidate | `0.1.0-preview.1`, pre-Claude-tested, installable artifact target | not a release |
| Nightmare Spell modpack | design and dependency boundaries only | none |

## Playable preview candidate

PR #19 follows `docs/PLAYABLE-PREVIEW-DIRECTIVE.md`. It intentionally accumulates several packages
before one bulk Claude review.

Implemented on the preview branch:

- transactional live frozen-datapack migration with exact read-back verification and rollback;
- persistent full imported Aspect/Flaw metadata;
- persistent general `SoulIdentityData`;
- Overworld `NightmareRegistryData` with one active instance per player;
- separate per-player Nightmare slots and stored return/recovery information;
- one entry choke point and one owned-entity/registry teardown path;
- bundled Nightmare dimension;
- playable DESIGN scenario **The Last Signal**;
- assigned DESIGN role **last watchkeeper**;
- central conflict resolved by restoring a signal fire; combat is optional;
- fixed DESIGN appraisal: `[Last Light]` / Awakened Aspect Rank and `[Cold Ash]`;
- server-owned Kindle ability/cooldown and Cold Ash drawback;
- expanded read-only Soul screen with formal names and mechanics;
- player preview/recovery/reset commands;
- install, lore, feedback and bulk-review documentation.

## Verification state

Stable `main` alpha.4 remains Claude-verified under closed Issue #16.

For PR #19:

- alpha.5 live-import checkpoint passed build, unit tests, physical-client boot, dedicated-server boot,
  JAR packaging and artifact upload;
- alpha.6 playable-slice checkpoint passed the same gates after one compile-only namespace correction;
- the final `0.1.0-preview.1` checkpoint and artifact provenance are recorded in
  `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md` after the final workflow completes;
- Andrew has not yet performed the local play walkthrough;
- Claude has not yet bulk-reviewed PR #19.

Pre-Claude-tested means exactly that: GPT built it with automated evidence, but it is not
Claude-verified and no human gameplay claim is made.

## Lore boundary

The novel remains authoritative for mechanics and terminology. Official adaptation material may guide
compatible visual staging. `docs/PREVIEW-LORE-DECISIONS.md` labels the handcrafted scenario, fixed
appraisal, Aspect, ability and Flaw as DESIGN rather than canon.

## Deliberately not implemented

- natural infection and exhaustion progression;
- procedural or canonical appraisal formula;
- temporary historical body/inventory;
- custom Nightmare Creature AI;
- corpse Gate creation;
- broad Nightmare scenario registry/content library;
- complete imported Aspect/Flaw mechanics;
- Memories, Echoes, Dream Realm, Gates or later-rank gameplay;
- modpack manifest/integrations.

## Next actions after the artifact

1. Andrew installs the preview JAR in a disposable 1.21.1 NeoForge world and records feel/interaction
   feedback using `mod/PREVIEW-PLAY-GUIDE.md`.
2. Claude bulk-reviews PR #19 using `docs/reviews/2026-07-30-gpt-playable-preview.md` and
   `docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`.
3. Fix evidence-backed defects without expanding scope.
4. Only after that choose the next preview or begin the shared standalone/modpack comparison.
