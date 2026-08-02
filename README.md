# Shadow Slave for Minecraft

An unofficial fan project inspired by _Shadow Slave_ by Guiltythree.

## Current products

| Product | State |
| --- | --- |
| Vanilla datapack | completed compatibility reference, tagged `datapack-v1.0.0` |
| Standalone/shared NeoForge core | merged and Claude-verified `0.1.0-preview.2` |
| Nightmare Spell modpack | design only; no manifest or dependency package yet |

The Java preview is installable but is **not a public release or feature-complete game**. Its machine gate is independently verified; Andrew's real play/feel pass remains pending.

See [PROJECT-STATUS.md](PROJECT-STATUS.md) for canonical current state and [docs/CURRENT-PREVIEW-SUMMARY.md](docs/CURRENT-PREVIEW-SUMMARY.md) for the compact summary.

## Playable Java preview

The Java project lives under [`mod/`](mod/) and targets Minecraft Java Edition 1.21.1, NeoForge 21.1.244, and JDK 21.

Implemented in `0.1.0-preview.2`:

- persistent lore-aligned Uninfected, Carrier, Aspirant, and Dreamer/Sleeper states;
- separate Soul Rank and Aspect Rank;
- server-authoritative Soul and identity attachments;
- expanded read-only O-key Soul screen;
- transactional live datapack migration with exact read-back and rollback;
- persistent per-player First Nightmare registry and separate play-space slots;
- bundled Nightmare dimension;
- DESIGN scenario **The Last Signal** and role **last watchkeeper**;
- a central-conflict resolution path where combat is optional;
- fixed DESIGN appraisal: Aspect **Last Light**, ability **Kindle**, and Flaw **Cold Ash**;
- technical recovery, inspection, reset, and migration commands.

Install and play using [`mod/PREVIEW-PLAY-GUIDE.md`](mod/PREVIEW-PLAY-GUIDE.md).

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

## Verification

Claude verified the merged result:

- validator clean;
- datapack lifecycle **32/32**;
- Flaw suite **39/39**;
- disconnect/reconnect slot regression passed and exited cleanly twice;
- Java clean build with **35 tests, 0 failures**;
- physical-client and dedicated-server smokes passed with `mod/verify-smoke.sh`.

Artifact:

```text
shadowslave-0.1.0-preview.2.jar
SHA-256 48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

Machine verification does not prove presentation, pacing, balance, or real-player feel. Use [`docs/PLAYABLE-PREVIEW-TEST-MATRIX.md`](docs/PLAYABLE-PREVIEW-TEST-MATRIX.md) for the remaining manual pass.

## Known limits

Still outside this preview:

- natural infection and exhaustion progression;
- complete historical body/inventory handling;
- custom Nightmare Creature AI and corpse Gates;
- procedural or canon-claimed appraisal;
- full mechanics for every imported identity;
- later Seeds, Dream Realm progression, Memories, Echoes, and later ranks;
- the modpack implementation and standalone/modpack comparison.

Open engineering issues:

- #20 retains the frozen datapack's manually induced global-selector limitation;
- #29 requires a persisted-codec invariant sweep, beginning with `PreviewPowerData`.

## Nightmare and Seed direction

Future Nightmare completion follows [`docs/NIGHTMARE-SEED-ROADMAP.md`](docs/NIGHTMARE-SEED-ROADMAP.md): a Nightmare ends when its central conflict reaches a named terminal resolution, while each challenger's survival/outcome and appraisal are evaluated separately. Boss deaths, timers, and objective interactions are events, not universal victory conditions. Exact later-Seed behaviour requires another primary-novel check before implementation.

## Datapack v1.0.0

Install the vanilla datapack at:

```text
<world>/datapacks/shadowslave-v1.0.0.zip
```

A multiplayer server installs it once; joining players use ordinary vanilla 1.21.1 clients. **Only one player may be inside a First Nightmare at a time.** A persistent global lock keeps the slot occupied across disconnects and server restarts until the owner returns and teardown completes.

The supported one-slot path is verified. The frozen prototype still uses global `ss_creature` selectors, so an administrator manually introducing an unrelated entity with that internal tag can affect the objective. True simultaneous per-player ownership is implemented in Java.

The datapack remains behavioural and import-compatibility evidence. Its first-sleep infection, safe ejection, serialized global ownership, finite identity tables, and timer/boss structure are prototype behaviour rather than permanent Java architecture or universal canon.

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
| testing | [docs/PLAYABLE-PREVIEW-TEST-MATRIX.md](docs/PLAYABLE-PREVIEW-TEST-MATRIX.md) |
| future Nightmare/Seed work | [docs/NIGHTMARE-SEED-ROADMAP.md](docs/NIGHTMARE-SEED-ROADMAP.md) |
| GPT session checkpoint | [GPT_HANDOFF.md](GPT_HANDOFF.md) |

## Credit

_Shadow Slave_ is by Guiltythree. This project is unofficial and is not affiliated with the author or publisher. It does not copy novel prose or official artwork.
