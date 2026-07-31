# Shadow Slave for Minecraft

An unofficial fan project inspired by _Shadow Slave_ by Guiltythree.

## Current products

| Product | State |
| --- | --- |
| Vanilla datapack | completed and frozen as `datapack-v1.0.0` |
| Standalone/shared NeoForge core | installable `0.1.0-preview.1` on draft PR #19 |
| Nightmare Spell modpack | design only; no manifest or dependency package yet |

The Java preview is **not a public release and not Claude-verified**. Its final automated checkpoint passed compilation, unit tests, physical-client startup, dedicated-server startup, packaging, and artifact upload. Andrew's complete playthrough and Claude's bulk review remain pending.

See [PROJECT-STATUS.md](PROJECT-STATUS.md) for the canonical current state and [docs/CURRENT-PREVIEW-SUMMARY.md](docs/CURRENT-PREVIEW-SUMMARY.md) for a compact preview summary.

## Playable Java preview

The Java project lives under [`mod/`](mod/). The current preview targets Minecraft Java Edition 1.21.1, NeoForge 21.1.244, and JDK 21.

Implemented in `0.1.0-preview.1`:

- persistent lore-aligned Uninfected, Carrier, Aspirant, and Dreamer/Sleeper states;
- separate Soul Rank and Aspect Rank;
- server-authoritative Soul and identity attachments;
- expanded read-only O-key Soul screen;
- transactional live datapack migration with exact read-back and rollback;
- persistent per-player First Nightmare registry and separate play-space slots;
- bundled Nightmare dimension;
- the handcrafted DESIGN scenario **The Last Signal** and role **last watchkeeper**;
- one central-conflict resolution path where combat is optional;
- fixed DESIGN appraisal: Aspect **Last Light**, ability **Kindle**, and Flaw **Cold Ash**;
- technical recovery, inspection, reset, and migration commands.

Install and play using [`mod/PREVIEW-PLAY-GUIDE.md`](mod/PREVIEW-PLAY-GUIDE.md).

Quick start in a disposable world:

```text
Press O
/shadowslave preview_begin
```

Useful commands:

```text
/shadowslave soul
/shadowslave soul_screen
/shadowslave preview_begin
/shadowslave nightmare_status
/shadowslave nightmare_recover
/shadowslave kindle
/shadowslave preview_reset
```

`/shadowslave migrate_datapack` is operator-only and should be tested on a backup legacy world.

## Evidence and limitations

Automated build provenance and checksums are recorded in [`docs/PLAYABLE-PREVIEW-PROVENANCE.md`](docs/PLAYABLE-PREVIEW-PROVENANCE.md). The complete review and play criteria are in [`docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`](docs/PLAYABLE-PREVIEW-TEST-MATRIX.md).

Still outside this preview:

- natural infection and exhaustion progression;
- complete historical body/inventory handling;
- custom Nightmare Creature AI and corpse Gates;
- procedural or canon-claimed appraisal;
- full mechanics for every imported identity;
- later Seeds, Dream Realm progression, Memories, Echoes, and later ranks;
- the modpack implementation and standalone/modpack comparison.

## Nightmare and Seed direction

Future Nightmare completion must follow [`docs/NIGHTMARE-SEED-ROADMAP.md`](docs/NIGHTMARE-SEED-ROADMAP.md): a Nightmare ends when its central conflict reaches a named terminal resolution, while each challenger's survival/outcome and appraisal are evaluated separately. Boss deaths, timers, and objective interactions are events—not universal victory conditions. Exact later-Seed behaviour requires another primary-novel check before implementation.

## Datapack v1.0.0

The frozen vanilla datapack installs unchanged at:

```text
<world>/datapacks/shadowslave-v1.0.0.zip
```

It remains a behavioural and import-compatibility reference. Its first-sleep infection, safe ejection, global ownership, finite identity tables, and timer/boss structure are prototype behaviour rather than permanent Java architecture or universal canon.

## Lore and design authority

- novel mechanics and terminology are primary authority;
- official adaptation material may guide compatible visual staging;
- translations/scanlations are access aids, not final terminology authority;
- project mechanics are labelled **DESIGN** where canon gives no deterministic rule;
- the datapack is compatibility evidence, not a Java-format constraint.

Start with [`docs/JAVA-LORE-ALIGNMENT.md`](docs/JAVA-LORE-ALIGNMENT.md), [`docs/PREVIEW-LORE-DECISIONS.md`](docs/PREVIEW-LORE-DECISIONS.md), and [`docs/JAVA-HANDOFF.md`](docs/JAVA-HANDOFF.md).

## Repository map

```text
shadowslave/          frozen datapack source
testserver/           datapack deployment and Mineflayer harnesses
mod/                  shared/standalone NeoForge Java core
modpack/              dependency-led prototype plan
shared-test-spec/     implementation-neutral acceptance scenarios
docs/                 lore, architecture, workflow, provenance, and historical records
```

## Administrative authority

| Need | Read |
| --- | --- |
| exact current state | [PROJECT-STATUS.md](PROJECT-STATUS.md) |
| current preview summary | [docs/CURRENT-PREVIEW-SUMMARY.md](docs/CURRENT-PREVIEW-SUMMARY.md) |
| install and play | [mod/PREVIEW-PLAY-GUIDE.md](mod/PREVIEW-PLAY-GUIDE.md) |
| evidence and checksums | [docs/PLAYABLE-PREVIEW-PROVENANCE.md](docs/PLAYABLE-PREVIEW-PROVENANCE.md) |
| bulk testing | [docs/PLAYABLE-PREVIEW-TEST-MATRIX.md](docs/PLAYABLE-PREVIEW-TEST-MATRIX.md) |
| future Nightmare/Seed work | [docs/NIGHTMARE-SEED-ROADMAP.md](docs/NIGHTMARE-SEED-ROADMAP.md) |
| GPT session checkpoint | [GPT_HANDOFF.md](GPT_HANDOFF.md) |

## Credit

_Shadow Slave_ is by Guiltythree. This project is unofficial and is not affiliated with the author or publisher. It does not copy novel prose or official artwork.
