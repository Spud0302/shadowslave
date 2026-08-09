# Technical Nightmare return verification — current recovery lineage

## Scope

This bounded correctness slice stacks on PR #126's green canonical-death API-hardening head and applies only to the shared technical/admin/preview-reset return path in `NightmareService.exit(...)`. It does not change successful Nightmare completion, canonical death, appraisal, progression, scenario content, or any in-world Spell rule.

## Trigger for this slice

PR #126 exact head `cfba29a0cb989d08e4624a6ca271babe15bf4fc4` passed Preview Gates run #91 / ID `31287546943`.

The successful-completion Issue #34 physical matrix remains blocked on an environment that can drive a real player through completion, process restart, reconnect, and second relog. Rather than retrying that unchanged blocker, this run audited the next independent persistence/correctness surface on the current lineage.

The current `NightmareService.exit(...)` selected a return level, called `ServerPlayer.teleportTo(...)`, and immediately tore down exact active Nightmare ownership. Historical PR #72 had already isolated the relevant NeoForge 21.1.244 boundary: cross-dimension travel may be cancelled or redirected while the teleport call itself returns normally. Consuming active ownership after such an attempt can strand authoritative player location and Java-owned Nightmare state on opposite sides of the transaction.

Historical #72 also received a review finding that a Nightmare-origin entry could make the intended return dimension itself the internal Nightmare dimension. The current lineage already resolves that prerequisite: `tryEnter(...)` rejects a fresh Nightmare while the player is in `NIGHTMARE_LEVEL` through `entryOriginAllowed(...)`.

## Correction

The shared exit path now captures the exact server dimension selected for the return attempt, including the existing technical fallback to overworld when the recorded return dimension is unavailable.

After `teleportTo(...)`, it checks the authoritative `player.serverLevel().dimension()` against that selected target. If they differ, exit fails before `teardown(...)`. Active ownership is therefore retained and the caller's later Soul/identity/reset mutations are not reached.

This one guard covers all current callers of the shared exit path:

- `technicalRecover(...)`;
- `adminAbort(...)`;
- `abortForPreviewReset(...)`.

The predicate is deliberately dimension-based. This closes the demonstrated cancellation/redirect class without inventing positional tolerances or treating any arbitrary non-Nightmare dimension as success.

## Evidence classification

- **CANON:** unchanged. Technical recovery, administrator abort, and development reset are not ordinary in-world Nightmare Spell mercy mechanics.
- **INFERRED:** unchanged one-instance ownership of technical recovery state while an exit has not actually committed.
- **DESIGN:** a technical/admin/reset return commits only after authoritative server state observes the player in the exact dimension selected for that return attempt; failed or redirected attempts retain active ownership.
- **UNKNOWN:** live NeoForge event-cancellation fault injection on this exact current head; exact return position/orientation verification; process-crash atomicity of the later technical/admin/reset mutations; storage-device/power-loss behavior.
- **COMPATIBILITY:** ordinary successful technical/admin/reset returns keep their existing target selection and teardown behavior. Only cancelled or redirected returns stop before teardown and later caller-side reset mutations.

No new canon proposition is introduced. `docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and the current Nightmare/Seed roadmap were re-read; the alignment document already requires technical recovery to remain explicitly separate from normal Spell behavior.

## Tests

`NightmareExitCommitBoundaryTest` covers the pure commit predicate:

1. remaining in `NIGHTMARE_LEVEL` when overworld was selected does not commit;
2. reaching the exact selected overworld dimension commits;
3. redirection to a different non-Nightmare dimension does not commit.

The production guard is placed after the teleport attempt and before exact active-ownership teardown.

## Deliberate limits / next slice

This is a return commit guard, not a restart-replayable technical-exit transaction. A process can still fail after a verified return while technical/admin state is being committed. Historical PR #73 addresses that separate persistence window with a durable technical-exit intent and should be the next bounded correctness port after this exact head is green.

Compound `preview_reset` has additional attachment/reset persistence surfaces; historical #74 remains the later separate transaction after technical/admin exit durability is restored.

The successful-completion six-row physical matrix remains paused until its recorded resume condition changes. Do not add more successful-completion transaction state without new physical evidence.
