# Preview-reset player persistence verification — 2026-08-10

## Scope

Audit the final player-side persistence boundary in the restart-replayable compound `preview_reset` transaction. This follows PR #168's verification of the first durable reset intent and the player-side recovery checks added for technical exit (#173) and canonical death (#175). Prepared Nightmare world/chunk durability under PR #158 remains blocked and is not retried here.

## Finding

`PreviewResetService.reset(...)` already records and directly verifies a durable player-scoped reset intent before mutating Nightmare state. It then clears Nightmare/completion state, resets Soul, permanent identity, imported identity and preview-power attachments, calls `PlayerList.saveAll()`, publishes the final client snapshot, clears the reset intent and persists the reset registry.

The final player save still relied on normal return from `saveAll()` as sufficient evidence. If the player file remained stale or absent while the later reset-marker deletion became durable, restart could observe partially reset or old preview state with no remaining reset transaction authority telling login recovery to finish the compound reset.

This differs from successful-completion player saves: the successful-completion receipt is retained after teardown and recovery derives required work from actual player/registry state. The preview-reset marker is deliberately removed after completion, so it is a true final-authority boundary.

## Implemented contract

After `persistPlayer()` and before client sync or `completeResetIntent()`, production now opens `playerdata/<uuid>.dat` and verifies the complete reset baseline:

- Soul is `SoulData.uninfected()` when explicitly stored;
- permanent identity is `SoulIdentityData.empty()` when explicitly stored;
- imported identity is `ImportedIdentityData.empty()` when explicitly stored;
- preview power is `PreviewPowerData.empty()` when explicitly stored;
- absence of any of those attachment entries is accepted because each registered attachment default is exactly the required reset value;
- absence of the entire NeoForge attachment compound is also accepted for the same reason;
- a missing player file, malformed attachment container/payload or non-default persisted value fails closed.

Direct semantic-value verification is intentional instead of requiring changed bytes. A legitimate restart after the reset player save already succeeded may begin with the exact all-default image already on disk. Those values are sufficient recovery evidence and replay must not require a second byte change.

On verification failure, the already-durable preview-reset intent remains present. The service does not publish the final snapshot, does not clear the marker and does not perform the final registry persistence. A later rerun/login can therefore replay the idempotent reset against retained authority.

## Evidence classification

- **CANON:** unchanged; `preview_reset` is a development operation, not ordinary Nightmare Spell behavior.
- **INFERRED:** none added.
- **DESIGN:** the complete persisted player reset baseline must be directly readable (or reconstruct exactly from registered defaults) before the final reset recovery authority is consumed.
- **UNKNOWN:** physical power-loss/fsync guarantees below a readable player image, storage corruption/replacement failure after verification, and administrator repair UX for malformed player/reset metadata.
- **COMPATIBILITY:** no save-schema change. Explicit or omitted default-valued attachments both remain valid; a successful reset still converges to the existing all-default preview baseline with one final client snapshot. Canonical death, successful completion, technical/admin recovery and ordinary Nightmare mechanics are unchanged.

No novel proposition is introduced. `docs/LORE-SOURCE-POLICY.md` and `docs/JAVA-LORE-ALIGNMENT.md` already classify development/admin recovery as technical rather than in-world Spell behavior.

## Tests

`PreviewResetServiceTest` now proves:

- player persistence verification is ordered after `persistPlayer()` and before sync/marker completion;
- verification failure retains reset authority and cannot publish a snapshot or clear the marker;
- normal and replay paths still converge to the existing reset baseline.

`PreviewResetRestartBoundaryTest` routes its process-free durable-player model through the new production verification seam, so restart reconstruction cannot stay green while omitting the newly required proof.

`PersistedPreviewResetPlayerVerifierTest` covers explicit all-default attachments, omitted defaults, an absent attachment container, stale Soul/preview-power state, malformed persisted data and a missing player file.

## Deliberate limits / next audit

This is API/file-image evidence, not a physical process-kill proof and not a storage-device durability guarantee. It does not add checkpoints to successful-completion phases where the retained completion receipt remains replay authority. It does not revisit PR #158 without one of that blocker's recorded resume conditions.

After this slice is green and review-clean, continue only with another independent transaction where the disappearance of the last recovery authority can be demonstrated to leave an unrecoverable persisted split; otherwise prefer physical fault evidence or a non-duplicative correctness issue over speculative persistence barriers.
