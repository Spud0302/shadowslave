# Preview reset durability audit — 2026-08-08

## Scope

This note audits the development-only compound `preview_reset` path after the restart-replayable successful-completion and technical-exit work through PR #73.

## Confirmed defect

`PreviewResetService.reset(...)` previously performed these operations without any durable reset intent:

1. abort active Nightmare ownership if present;
2. clear a retained successful-completion receipt;
3. reset Soul state;
4. clear permanent identity;
5. clear imported identity;
6. clear preview-power state;
7. publish one client snapshot.

Several of those pieces live in different persistence surfaces. In particular, Nightmare ownership/completion state is overworld `SavedData`, while the Soul/identity/imported-identity/preview-power attachments are player data. A process failure after a registry mutation but before the reset player attachments were saved could therefore restart without any durable fact saying that the compound preview reset had already been chosen.

That is distinct from PR #73's technical-exit marker. Technical recovery/admin abort intentionally converge to the existing Carrier recovery state. `preview_reset` instead clears the entire development preview state back to the uninfected baseline, including imported identity and preview-power state. Reusing the narrower technical marker would therefore encode the wrong replay semantics.

## Design decision

Add a separate overworld `PreviewResetRegistryData` containing only player UUIDs with pending compound preview resets.

The reset sequence becomes:

1. idempotently record preview-reset intent;
2. synchronously persist overworld `SavedData` before any other reset mutation;
3. abort active Nightmare state through the existing verified-return/teardown path if needed;
4. clear any retained successful-completion receipt;
5. clear every preview player attachment in memory;
6. synchronously save player data containing the complete cleared attachment state;
7. publish the existing single final authoritative client snapshot;
8. clear the preview-reset intent;
9. synchronously persist overworld `SavedData` again.

Login recovery checks this marker before technical-exit recovery, successful-completion recovery, or ordinary active-Nightmare handling. Replay is idempotent.

## Evidence classification

- **CANON:** unchanged. This is a development reset and not a Nightmare Spell mechanic.
- **INFERRED:** none added. No novel behavior is inferred from the reset transaction.
- **DESIGN:** a persisted player-scoped preview-reset intent has recovery precedence until the complete reset player state is saved and the one final snapshot is published.
- **UNKNOWN:** real process-kill behavior at each physical save boundary remains unproven until dedicated-server fault injection exists. A runtime exception without logout/relogin does not currently auto-dispatch the pending marker again; the command may be rerun or the player may relog.
- **COMPATIBILITY:** existing saves contain no preview-reset marker and load as having no pending reset. Normal successful reset still produces one final authoritative sync. Nightmare return/teardown behavior remains delegated to the existing service.

No canon rule is invented.

## Test boundary

Unit coverage checks the commit ordering, idempotent replay, marker round-trip, duplicate-marker rejection, malformed-marker rejection, and that reset intent remains present through player persistence and the final sync.

Hosted Gradle/JUnit, physical client/server boot and process-kill evidence are separate gates and must not be claimed unless a workflow or manual run records them for the exact head.
