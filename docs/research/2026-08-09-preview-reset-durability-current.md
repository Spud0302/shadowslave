# Compound preview reset durability — current lineage — 2026-08-09

## Scope

This note ports the reviewed compound `preview_reset` persistence correction from historical PR #74 onto the current recovery lineage through PR #129. It is development/admin infrastructure, not an in-world Nightmare Spell mechanic.

## Preconditions checked

- Current `main` authority documents, open issues/PRs, the mandatory lore-source policy, Java lore-alignment gate, and Nightmare/Seed roadmap were re-read before implementation.
- PR #129 exact head `8d102fb81170d9931d280d0ae74d4c4261ac336b` passed Preview Gates run #93 / ID `31291714894` before this slice started.
- Issue #34's successful-completion physical restart matrix remains blocked on real-player execution and was not retried.
- Historical PR #74 already identified the compound-reset cross-persistence crash window and its NeoForge I/O-barrier/fail-closed corrections.

## Confirmed defect

Before this slice, `PreviewResetService.reset(...)` could mutate Nightmare ownership/completion SavedData and then clear Soul, permanent identity, imported identity, and preview-power player attachments without a durable transaction identity spanning those persistence surfaces.

A process failure after one side became durable could restart into a mixed state with no authoritative fact saying that the complete development reset had already been chosen.

This is separate from PR #129's technical/admin exit transaction. Technical recovery intentionally converges to the existing Carrier recovery state. `preview_reset` intentionally clears the whole preview baseline to uninfected state, including imported identity and preview-power data, then publishes one final authoritative snapshot.

## Corrected durable order

1. idempotently record a player-scoped preview-reset intent;
2. persist and join overworld SavedData through `SavedDataPersistence.saveAndWait(...)` before any reset mutation;
3. if a narrower technical/admin exit was already pending, finish it first; otherwise abort active Nightmare state through the verified return/teardown path;
4. clear any retained successful-completion receipt;
5. clear Soul, permanent identity, imported identity, and preview-power state in memory;
6. synchronously save player data containing the complete cleared attachment state;
7. publish the existing one final authoritative client snapshot;
8. clear the preview-reset intent;
9. persist and join overworld SavedData again before returning.

Login recovery precedence on the current lineage is:

1. pending canonical death;
2. pending or corrupt preview reset;
3. pending technical/admin exit;
4. successful completion;
5. ordinary active/technical/no-op login policy.

Canonical death therefore remains the stronger terminal authority. Preview reset only preempts the recoverable technical/completion paths after its own durable intent exists.

A retained technical-exit marker is explicitly reconciled inside the preview-reset transaction before ordinary preview teardown. This avoids a current-lineage deadlock where `NightmareRegistryData.remove(...)` correctly refuses to bypass a pending technical exit while preview-reset login precedence would otherwise keep redispatching the same failing reset.

## Fail-closed marker behavior

`PreviewResetRegistryData` stores pending player UUIDs in a separate overworld SavedData file. Duplicate, missing-UUID, or wrong-type persisted marker data is retained as `recoveryBlocked` rather than thrown away as an apparently empty registry. Because malformed data can destroy owner identity, the block is intentionally global rather than guessed per player.

## Evidence classification

- **CANON:** unchanged. `preview_reset` is a development operation, not normal Spell behavior.
- **INFERRED:** none added.
- **DESIGN:** the reset intent, persistence ordering, replay precedence, technical-exit reconciliation, fail-closed corrupt-marker policy, and one-final-snapshot transaction are Minecraft recovery design.
- **UNKNOWN:** real process-kill/power-loss convergence at each boundary; storage guarantees below NeoForge's joined I/O worker; same-process retry after an arbitrary runtime exception; administrator repair UX for corrupt reset metadata.
- **COMPATIBILITY:** successful resets still end at the uninfected preview baseline and emit one final snapshot. Worlds without reset-marker data load an empty healthy registry. Existing successful-completion, canonical-death, and technical/admin transactions retain their own semantics.

No canon rule is invented.

## Automated evidence

`PreviewResetServiceTest` verifies the intent is persisted before mutations, the complete cleared player state is saved before sync, the marker remains through the final snapshot, the marker is cleared only afterward, and replay from an already-present intent is idempotent.

`PreviewResetRegistryDataTest` verifies round-trip/idempotency plus duplicate, missing-UUID, and wrong-type persisted input becoming recovery-blocking and rejecting reads/mutations.

The shared `SavedDataPersistence` helper was already present and green on the #129 lineage; this slice reuses it rather than adding a second durability primitive.

## Remaining boundary

Hosted Gradle/JUnit, completion verifier self-tests, physical NeoForge client/server smoke, and the frozen-datapack gate must run for this exact head before it is called green. Dedicated physical process-kill testing for compound reset remains separate evidence and is not claimed here.
