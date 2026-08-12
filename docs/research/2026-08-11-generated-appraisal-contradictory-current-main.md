# Generated appraisal contradictory disk-cut evidence — current main

**Date:** 2026-08-11  
**Tracks:** #34  
**Base evidence:** merged PR #253 / `ab16d39e60bd57fd2e7916f06559bd02e85d0b03`

## Purpose

Merged PR #253 proves a healthy persisted successful-completion cut can be reconstructed from compressed completion-receipt, Nightmare-registry and player attachment images across fresh JVMs on current main. This follow-up ports the complementary fail-closed case from stale PR #228 onto that current-main evidence base: a durable successful-completion receipt must not consume or overwrite a different active Nightmare that persisted for the same player.

## Persisted cut

The test writes two production-shaped compressed SavedData images:

- `shadowslave_nightmare_completion_receipts.dat` contains one resolved completion receipt;
- `shadowslave_nightmares.dat` contains a different active `NightmareInstance` for the same player.

A fresh JVM production-decodes both images and calls the package-private production `GeneratedAppraisalRecoveryService.activeInstanceForReplay(...)` selector through a test-only reflection seam. The contradictory instance must be rejected. The parent JVM then requires both persisted files to remain byte-for-byte unchanged.

This guards the recovery-authority boundary rather than adding another persistence checkpoint. The durable receipt remains available for diagnosis/retry and the unrelated active ownership is not destructively consumed.

## Historical review correction retained

Review of stale PR #228 found that its first fixture passed player and instance UUIDs to `NightmareInstance` in the wrong semantic order. That malformed fixture represented another player's active Nightmare rather than the intended same-player/different-instance contradiction.

The port retains the corrected constructor ordering and explicit assertions requiring the completed receipt and contradictory active record to share `playerId` while differing in `instanceId`. This makes future fixture-order regressions fail immediately instead of silently weakening the persistence evidence.

## Parent validation

PR #253 exact head `a58fae8cc9be912763db8da5e927b659b755c4e1` passed Preview Gates #274 / run `31471600347` and had no inline review threads. It merged to main as `ab16d39e60bd57fd2e7916f06559bd02e85d0b03`.

This current-main port requires its own exact-head Preview Gates before being called green.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, progression, reward, death or failure rule changes.
- **INFERRED:** an already-resolved appraisal should remain the same resolved result through technical restart rather than be regenerated against later code/catalogue state.
- **DESIGN:** contradictory durable ownership fails closed; recovery authority and unrelated active ownership remain untouched when receipt and active-instance identity do not match.
- **UNKNOWN:** live NeoForge process-kill timing, real `ServerPlayer` return teleport persistence, server/player save scheduling, filesystem/fsync guarantees below readable files, and post-verification corruption.
- **COMPATIBILITY:** test/documentation only. No runtime visibility, save schema, catalogue, dependency or gameplay behavior changes.

## Lore/source boundary

No new lore-sensitive proposition is introduced. `docs/LORE-SOURCE-POLICY.md` requires primary-novel research when lore mechanics change, while `docs/JAVA-LORE-ALIGNMENT.md` explicitly classifies server-restart/crash recovery as technical infrastructure. This test therefore does not invent a canon recovery rule or manufacture novel support for a Java consistency invariant.

## Blocked stronger proof

This still does not invoke `GeneratedAppraisalRecoveryService.replayPending(ServerPlayer)` across a real reconnect on two dedicated-server JVMs and does not prove waking-location persistence. That stronger proof remains blocked until there is credible automated NeoForge client/server-side test-player infrastructure, a framework/dependency change, owner input, or another technically sound way to reconnect a real player through production login recovery.

Prepared-world durability #158 remains blocked under its recorded resume condition and is not retried here. Repeated hosted vanilla harness stalls likewise remain under their existing stop-retrying rule unless new evidence appears.
