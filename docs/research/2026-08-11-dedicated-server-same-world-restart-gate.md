# Dedicated NeoForge same-world restart gate

**Date:** 2026-08-11  
**Issue:** #34 — successful Nightmare appraisal recovery across server restart

## Why this slice

The current restart-correctness line already has stronger process-free evidence than ordinary codec round-trips:

- PR #223 crosses successful-completion receipt, active Nightmare registry, and persisted player attachment images through fresh JVM boundaries;
- PR #228 adds the complementary contradictory same-player/different-instance persisted cut and requires the production replay selector to fail closed;
- exact head `28203270ff7ac06a6ebf8288fafbb63b1594e772` passed the complete Java job in Preview Gates #245 / run `31457344565`, including compile/all unit tests/package, physical NeoForge client boot, dedicated-server boot, and development-JAR upload. The workflow failed only in the separately recurring frozen-datapack Mineflayer harness.

Those tests are valuable, but `docs/JAVA-INTEGRATION-TEST-PROCEDURES.md` correctly says that reconstructed SavedData or codec images are not a dedicated-server restart. The Java CI gate also previously started only one fresh dedicated server and deleted its scratch world before each run.

The next honest bounded improvement is therefore to establish a real same-world, two-process NeoForge restart substrate before attempting to claim successful-completion login replay.

## Implemented boundary

`mod/verify-smoke.sh server-restart` now:

1. removes only the scratch smoke world before the first boot;
2. starts a real NeoForge dedicated-server JVM through `runServerSmoke`;
3. requires both the Shadow Slave load marker and Minecraft ready marker;
4. terminates that first smoke process with the existing process-group cleanup;
5. requires a non-empty `mod/run-server-smoke/world/level.dat` from that first boot;
6. deliberately preserves the same world directory;
7. starts a second real NeoForge dedicated-server JVM against that same persisted world image;
8. requires the same mod-load and server-ready markers again.

Preview Gates now run this `server-restart` mode instead of the previous single inline dedicated-server boot. A focused workflow contract test pins that CI invokes the restart mode and that the smoke script contains both the reset-first-boot and preserve-second-boot paths.

This catches a class of real process-boundary regressions that same-JVM reconstruction cannot: leaked process/session locks, failure to reopen the generated world, startup/load failures on a persisted world image, and mod initialization failures that appear only on the second JVM.

## What this does not prove

This is deliberately **not** called successful-completion restart evidence yet.

The gate does not currently have an automated NeoForge-connected `ServerPlayer`. Therefore it does not:

- enter a player into a Nightmare;
- materialize a successful-completion receipt from live gameplay;
- persist a real player's Soul/identity/Attribute/Memory/Echo state through the server lifecycle;
- invoke `GeneratedAppraisalRecoveryService.replayPending(ServerPlayer)` on login after restart;
- prove waking return location;
- prove active Nightmare teardown, generated appraisal application, or completion-receipt consumption under the restarted server;
- prove exact-once player-visible completion across a physical process loss;
- provide filesystem, storage-device, or power-loss/fsync guarantees below the readable world image.

A boot-only second JVM must not be reported as a player relog/restart or completion-recovery pass.

## Evidence classification

- **CANON:** unchanged / not applicable. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, progression, death, or failure mechanic changes.
- **INFERRED:** unchanged project expectation that an already-resolved appraisal remains the same resolved result across technical restart; this slice adds no new lore inference.
- **DESIGN:** two real dedicated-server JVM boots against one preserved scratch world are a CI recovery substrate and a stronger process-boundary gate than one fresh boot.
- **UNKNOWN:** live successful-completion replay with a real `ServerPlayer`, waking teleport persistence, player save timing, exact process-loss windows, filesystem/fsync guarantees, and storage corruption after verification.
- **COMPATIBILITY:** CI/test infrastructure only. Runtime gameplay behavior, persistence schemas, catalogues, dependencies, and lore-facing semantics are unchanged.

`docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` were re-read. No primary-novel material was newly required because no lore-sensitive mechanic or terminology changed; restart/crash recovery remains technical infrastructure rather than ordinary Spell mercy.

## Blocker for the next stronger proof

A true Issue #34 dedicated-server completion-restart test needs a credible way to create and reconnect a real NeoForge player under automation. The current CI has a physical client smoke, but it only boots through resource initialization and does not connect to the dedicated server.

Resume the stronger completion-replay harness when one of the following exists:

- an automated NeoForge client/player harness that can connect, authenticate in the offline CI world, issue the supported preview commands/interactions, disconnect, and reconnect after restart;
- a server-side test-player facility that exercises the same login/recovery event path as a real `ServerPlayer` without inventing alternate production authority;
- another credible integration approach that materializes real player/world files and invokes the actual login recovery path across two server JVMs.

Do not substitute another codec-only checkpoint and call it a live restart.

## Repeated frozen-datapack blocker

Preview Gates #242 and #245 both produced usable Java evidence while the unrelated frozen-datapack Mineflayer path failed in the recurring Nightmare transition/runner-lag class. Do not blindly rerun that harness or increase another timeout. Resume that infrastructure investigation only with new diagnostics, a changed harness/server path, runner/dependency changes, or another credible technical approach.
