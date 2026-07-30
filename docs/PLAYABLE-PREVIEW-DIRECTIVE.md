# Owner directive — playable Java preview batch

**Owner:** Andrew  
**Recorded by:** GPT  
**Date:** 2026-07-30  
**Applies to:** the current Java development batch beginning on `gpt/live-datapack-import`

This document consolidates Andrew's separate chat instructions into one binding brief. Later agents
must use this document rather than reconstructing the intent from chat history.

## Master instruction

Continue the current Java work without pausing between feature packages. Finish the live datapack
migration package first, then continue until there is a coherent **pre-Claude-tested playable JAR**
that Andrew can install in his own Minecraft 1.21.1 NeoForge game to explore and judge the different
systems.

Do not wait for Claude to test each intermediate package. Build automated checks and explicit Claude
review criteria alongside the implementation, keep honest records of untested behaviour, and submit
the accumulated work for one bulk Claude review after the playable-preview milestone.

Andrew may stop or redirect the work at any time. Otherwise, continue until the stopping condition in
this document is met.

## Lore rule — never assume

Every gameplay mechanic must be checked against the project lore sources before it becomes design or
code. The authority order in `docs/JAVA-LORE-ALIGNMENT.md` remains binding:

1. novel text for mechanics, terminology, progression and metaphysics;
2. officially released adaptation material for compatible visual/staging guidance;
3. translation/scanlation access layers as access aids, not final terminology authority;
4. community sources only as research indexes requiring primary-source confirmation;
5. project design, clearly labelled as invention.

For important mechanics and data definitions, record one of:

- **CANON** — directly supported by primary text;
- **INFERRED** — reasoned synthesis of primary evidence;
- **DESIGN** — a Minecraft implementation chosen for playability;
- **UNKNOWN** — intentionally unresolved.

Never copy a datapack shortcut into Java merely because it already exists. Never present a project
algorithm as canon. When the lore is uncertain, leave the point unresolved or label the chosen game
mechanic as design.

## Immediate implementation order

1. Complete live legacy-score/tag reading.
2. Translate through the already accepted pure migration layer.
3. Persist Java Soul, imported Aspect and imported Flaw records.
4. Read the persisted result back and verify identity before marking migration complete.
5. Retain all legacy values until every verification step succeeds.
6. Add persistent `NightmareRegistryData` and explicit player/instance ownership.
7. Implement one lore-checked First Nightmare vertical slice built around:
   - an assigned historical role;
   - a reconstructed situation;
   - a central conflict;
   - at least one valid resolution;
   - one entry choke point and one teardown path;
   - explicit distinction between canonical death and technical recovery.
8. Add appraisal/revelation sufficient to produce one persistent Aspect and Flaw outcome without
   claiming a canonical generation formula.
9. Connect the slice to the Soul screen and progression state so Andrew can experience and inspect the
   systems in an ordinary local game.
10. Produce the playable preview JAR and its install/test documentation.

This order may change when a lore check or technical dependency proves that a different sequence is
safer, but the reason must be recorded.

## What “finished preview JAR” means

“Finished” here means a coherent development preview, not a complete Shadow Slave game and not a
public release.

The preview must, at minimum:

- build as a NeoForge 1.21.1 JAR with JDK 21;
- install in a normal local NeoForge instance without requiring the datapack;
- start on both physical client and dedicated server;
- expose a usable Soul screen;
- preserve Soul state across normal saves/reloads as far as automated coverage can prove;
- provide a complete playable development path from an uninfected player through Carrier, Aspirant
  and Dreamer/Sleeper;
- contain one individually owned First Nightmare scenario with a meaningful role and conflict rather
  than only an arena timer/boss;
- reveal and persist one Aspect and Flaw outcome;
- include safe development/admin commands for reaching, inspecting and resetting systems;
- import supported frozen-datapack identities without deleting legacy evidence prematurely;
- clearly identify accessibility shortcuts, test exits and non-canon development aids;
- avoid knowingly false lore claims;
- include installation instructions, controls, commands, known limitations and a play-feedback
  checklist for Andrew.

The artifact must be labelled as a **development preview / pre-Claude-tested build**. It must not be
called a public release, canon-complete, feature-complete or Claude-verified.

## Continuous testing while building

Each package must add or update its own evidence instead of postponing all testing until the end:

- unit tests for pure domain and migration rules;
- codec/persistence round trips;
- NeoForge GameTests where practical;
- marker-based client and server smokes through `mod/verify-smoke.sh`;
- fail-closed tests for absent scoreboard values and unreadable legacy evidence;
- lifecycle tests for single entry choke point, ownership and idempotent teardown;
- restart/recovery tests where automation can exercise them;
- explicit manual play criteria for presentation, feel and interactions;
- a cumulative Claude review matrix mapping every claim to evidence.

Bare `runClientSmoke` and `runServerSmoke` Gradle success are not proof that Minecraft reached a valid
state. Use the marker-based verification script and inspect the required logs.

## Claude workflow

Claude does not gate each intermediate package in this batch.

During development:

- keep work on `gpt/*` branches;
- keep the accumulated PR draft until a deliberate checkpoint;
- record lore decisions, tests, limitations and review targets in the repository;
- do not claim Claude verification.

At the playable-preview milestone, hand Claude one bulk package containing:

- the complete branch/PR diff;
- the lore decision record;
- automated results;
- the preview JAR build provenance;
- the full manual and automated test matrix;
- known risks and deferred evidence;
- exact reproduction and installation steps.

Claude may then verify, fix, block or approve the accumulated batch.

## Stopping condition

Stop the uninterrupted development batch when all of the following are true:

1. the live migration package is implemented and tested;
2. the playable vertical slice above exists;
3. the development preview JAR has been built successfully;
4. the JAR is available to Andrew with install instructions;
5. automated checks are green at the accepted checkpoint;
6. unperformed human checks and known limitations are stated honestly;
7. the bulk Claude handoff/test matrix is complete.

Do not continue into broad content expansion, the full Dream Realm, many Aspects/Flaws, or the
standalone-versus-modpack comparison after this point unless Andrew gives a new instruction.
