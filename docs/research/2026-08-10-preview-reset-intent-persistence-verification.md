# Preview-reset intent persistence verification — 2026-08-10

## Scope

Audit the first durable authority in compound `preview_reset` after the technical/admin-exit intent boundary was directly verified on PR #166.

PR #131 made preview reset restart-replayable with a player-scoped intent, and PR #133 added process-free reconstruction tests. Those branches intentionally used `SavedDataPersistence.saveAndWait(...)` as their initial API-level persistence boundary. Later persistence work (#153, #161, #164 and #166) established a stronger rule: normal return from a joined save helper is not by itself proof that the recovery authority expected after restart is present in the target persistence file.

## Finding

`PreviewResetService.reset(...)` previously performed:

1. record/reset intent in `PreviewResetRegistryData`;
2. call `SavedDataPersistence.saveAndWait(...)`;
3. immediately reconcile/abort Nightmare state, clear successful-completion state, and reset player attachments.

The reset transaction spans overworld SavedData and player data. If the first reset-intent write were silently rejected or otherwise absent from `shadowslave_preview_resets.dat`, later destructive mutations could become durable without a persisted fact saying that the broader preview reset must resume after restart.

That is a first-authority persistence boundary, not a request for a checkpoint on every later phase.

## Implemented contract

The first reset phase now follows:

1. begin or reassert the player-scoped reset intent;
2. explicitly dirty the reset registry so a same-process retry performs serialization even when the same marker is already present in memory;
3. call the existing joined SavedData persistence helper;
4. read `world/data/shadowslave_preview_resets.dat` and require a healthy persisted `pending` list containing the exact player UUID;
5. only after that proof may Nightmare reconciliation, successful-completion clearing, player attachment reset, player persistence, final snapshot publication, or reset-intent completion proceed.

The persisted verifier rejects a missing file, missing `data`/`pending` structure, malformed entries, duplicate player markers, and absence of the expected player marker. This mirrors the reset registry's existing fail-closed reconstruction contract rather than accepting an exact marker from an otherwise corrupt image.

Direct marker inspection is deliberate. On an ambiguous same-process retry, an earlier write may already have persisted the exact reset authority. In that case the authority is sufficient to replay the reset, while explicit dirtying still causes another serialization attempt when the prior write did not reach disk. Requiring a whole-file digest change would incorrectly reject the already-durable identical-marker case.

## Evidence classification

- **CANON:** unchanged. `preview_reset` is a development operation and is not an ordinary Nightmare Spell mechanic.
- **INFERRED:** none added.
- **DESIGN:** destructive compound reset work begins only after its exact technical recovery marker is directly readable from a healthy persisted reset-registry image. Explicit retry dirtying and persisted-NBT inspection are Java durability guards.
- **UNKNOWN:** physical process/power-loss guarantees below a readable file image; failing-storage behavior during file replacement; whether a lower-level write can report failure while leaving a readable valid image; administrator repair UX for corrupt reset metadata.
- **COMPATIBILITY:** no SavedData schema change. Existing valid reset markers remain readable/replayable; successful reset still converges to the same uninfected preview baseline with one final client snapshot; canonical death, technical/admin exit, successful completion and ordinary Nightmare semantics are unchanged.

No new novel proposition is introduced. `docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` classify administrator/development recovery separately from in-world Spell behavior, so no primary-novel mechanic is generalized by this change.

## Tests

`PreviewResetServiceTest` now proves the initial sequence is `begin -> persist -> verify -> later mutation` and that verification failure retains the reset intent while leaving Nightmare, completion, player persistence and client sync untouched.

`PersistedPreviewResetIntentVerifierTest` covers an exact persisted marker, a missing expected marker, malformed/duplicate persisted authority and a missing registry file.

PR #133's existing restart-boundary reconstruction tests remain the broader replay evidence. This slice does not replace physical process-kill testing.

## Remaining limits and next audit

Later reset phases retain the already-persisted reset intent until complete player state is saved and the final client snapshot is published. Do not mechanically add per-phase file verification unless a reconstruction demonstrates that replay from that retained authority is insufficient.

Prepared Nightmare world/chunk durability remains blocked under PR #158's recorded resume conditions and is not retried here.
