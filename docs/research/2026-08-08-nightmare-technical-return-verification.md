# Nightmare technical-return verification boundary

**Date:** 2026-08-08  
**Scope:** technical recovery, administrator abort, and preview-reset exit

## Finding

`NightmareService.exit(...)` used the shared `teleportToReturn(...)` helper and then immediately persisted the player, tore down the active Nightmare, and persisted the registry.

The adjacent entry and successful-completion audits established a NeoForge 21.1.244 behavior relevant to this path: cross-dimension travel can be cancelled while `ServerPlayer.teleportTo(...)` returns normally. A cancelled technical/admin return could therefore leave the player physically inside the Nightmare while consuming the active ownership record that ordinary recovery needs.

## Boundary

`teleportToReturn(...)` now returns the actual server dimension selected for the return attempt. That matters because technical/admin recovery deliberately falls back to the overworld when the recorded original dimension is unavailable.

After the teleport call, `NightmareService.exit(...)` compares the authoritative server-side player dimension with that selected target before any player persistence or teardown. If they differ, exit fails and active ownership is retained.

This protects all callers of the shared exit path:

- technical recovery;
- administrator abort;
- compound preview reset.

The later Soul/identity reset work in technical recovery and administrator abort is not reached when the return is rejected, so physical location and canonical Java ownership remain recoverable together.

The comparison is deliberately dimension-based. It closes both the demonstrated cancellation case and an unexpected redirect to another dimension without inventing positional tolerances.

## Evidence classification

- **CANON:** unchanged. Technical/admin recovery is not an in-world Nightmare Spell mechanic.
- **INFERRED:** unchanged one-instance ownership of technical Nightmare state while recovery is incomplete.
- **DESIGN:** a technical/admin exit commits only after the authoritative server-side player is observed in the exact dimension selected by the return helper, including technical fallback selection.
- **UNKNOWN:** live event-cancellation fault injection is not executed here; exact position/orientation verification remains outside this slice.
- **COMPATIBILITY:** normal technical recovery, administrator abort, and preview reset continue through the existing shared exit path; cancelled or redirected returns now retain ownership instead of tearing it down.

## Tests

`NightmareExitCommitBoundaryTest` covers the shared commit predicate:

- remaining in `NIGHTMARE_LEVEL` when overworld was selected does not commit exit;
- reaching the selected overworld dimension commits exit;
- redirecting to another non-Nightmare dimension also does not commit exit.

The production guard sits before player persistence and teardown in `NightmareService.exit(...)`.

## Deliberate limits

This does not claim process-crash atomicity, a physical NeoForge cancellation integration test, exact return-position verification, or completion of Issue #34's dedicated-server restart/fault-injection matrix. Successful-completion return verification remains owned by the corrected adjacent PR #71 slice.
