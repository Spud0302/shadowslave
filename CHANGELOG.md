# Changelog

[Pride Versioning](https://pridever.org/) — `PROUD.DEFAULT.SHAME`.

The previous full datapack and early-alpha changelog remains available in Git history and under `docs/history/`. This root changelog records the active Java preview line and major retained milestones.

## `0.1.0-preview.2` — verified playable Java preview

**Status:** merged into `main` as `c3ffcd9`; independently verified machine-checkably; Andrew play feedback pending; not a public release.

### Soul persistence

- decode `SoulData` through an unvalidated storage record so inconsistent stored data returns `DataResult.error` instead of throwing through player load;
- validate stored schema before explicit schema-1 migration;
- reject invalid and future schemas;
- prevent schema-2 records from receiving schema-1 repairs;
- require every post-First-Nightmare Nightmare-Spell state to retain Aspect, Aspect Rank, and Flaw identity.

### Migration validation

- require completed frozen-datapack players to retain the Carrier tag written by the release path;
- require generated two-digit Aspect and Flaw scores to retain their matching mechanics tags;
- add four reader tests around absent scores and explicit zero, including fail-closed handling rather than treating zero as absence;
- retain exact read-back, rollback, final marker, and legacy evidence.

### Frozen datapack safety

- enforce one active First Nightmare through a persistent global lock;
- preserve the slot while its owner is offline and across restarts;
- release the lock only after shared creature cleanup;
- restore health in `test/reset` after modifier cleanup;
- add a disconnect/reconnect regression proving refusal, resume, completion, cleanup, and later admission;
- make the Mineflayer regression exit cleanly after PASS;
- preserve the separate manually induced global-selector defect probe and its measurement notes;
- document the one-active-trial ceiling and residual global-selector limitation.

### Verification

Claude verified the merge result:

```text
validator                         clean
lifecycle harness                 32/32
Flaw harness                      39/39
disconnect/reconnect regression   PASS, exit 0, repeated twice
Java tests                        35, failures 0
physical-client smoke             PASS
dedicated-server smoke            PASS
```

The regression left `$global ss_trial_lock = 0` and no stray trial creature.

### Artifact

```text
shadowslave-0.1.0-preview.2.jar
SHA-256 48686e2598f9d5354acaec6544e4a5b024206fc0944c75e026cb67586298d9d9
```

### Follow-up

- Andrew's complete play/feel pass remains pending;
- issue #20 remains open for the frozen prototype's manually induced global creature-selector limitation;
- issue #29 tracks the next persisted-codec invariant sweep, beginning with `PreviewPowerData`.

## `0.1.0-preview.1` — first installable vertical slice

**Status:** superseded by preview.2.

- added transactional live frozen-datapack migration;
- added persistent native/imported identities;
- added persistent per-player Java Nightmare ownership and separate slots;
- added bundled Nightmare dimension;
- added DESIGN scenario **The Last Signal** and role **last watchkeeper**;
- added fixed DESIGN appraisal **Last Light / Kindle / Cold Ash**;
- expanded the O-key Soul screen;
- added onboarding, inspection, recovery, reset, and migration commands;
- added preview install guide, lore ledger, artifact provenance, bulk test matrix, and Nightmare/Seed roadmap.

## `0.1.0-alpha.4` — fail-safe datapack translation foundation

- immutable legacy evidence snapshot and pure translator;
- exact generated and legacy Aspect/Flaw mappings;
- fail-closed rejection of active/inconsistent state;
- deterministic imported IDs and idempotency fixtures;
- validator cross-check for all imported Flaw names;
- independently reviewed and verified by Claude.

## `0.1.0-alpha.3` — lore-aligned Java schema

- removed Mundane from the Soul Rank ladder;
- added explicit Aspirant and Dreamer states plus awakening path;
- separated Aspect Rank from Soul Rank;
- updated Soul snapshot/screen and schema-1 migration;
- documented novel/adaptation/design authority boundaries.

## `0.1.0-alpha.2` — server-synchronised Soul screen

- limited server-owned Soul snapshots;
- O-key read-only screen and command fallback;
- login/mutation synchronization;
- physical-client and dedicated-server side-separation gates.

## `0.1.0-alpha.1` — persistent Java Soul core

- NeoForge 1.21.1 / Java 21 workspace;
- codec-backed persistent Soul attachment;
- server-owned transitions and development commands;
- wrapper, JUnit, packaging, and server startup checks.

## Datapack `1.0.0`

- released and tagged as `datapack-v1.0.0`;
- remains the vanilla product and Java migration/behavioural reference;
- later maintenance preserves the release format while correcting test/reset and safe one-slot ownership;
- historical datapack release details remain in Git history.
