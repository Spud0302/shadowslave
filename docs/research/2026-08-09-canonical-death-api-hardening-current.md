# Canonical Nightmare death API hardening — current recovery lineage

## Scope

This bounded follow-up stacks on PR #124's restart-replayable First-Nightmare death transaction. It ports the previously audited API hardening from historical PRs #79 and #81 onto the current successful-completion/death-recovery lineage without changing transaction ordering or lore-facing behavior.

## Trigger for this slice

PR #124 exact head `ae056313c3239c370fd1b24cedf10f9faa93f903` passed Preview Gates run #90 / ID `31285481545`. Its implementation note deliberately left two source-visible bypasses for a separate reviewable follow-up:

1. `NightmareService.canonicalDeath(ServerPlayer)` still exposed the pre-transaction teardown-first implementation;
2. `NightmareDeathRegistryData.begin(...)` and `complete(...)` remained public even though the death marker is transaction authority.

Historical PR #79 established that no supported runtime caller requires the legacy `canonicalDeath(...)` helper. Historical PR #81 established that external callers do not need public mutation of the death marker; read-only recovery inspection remains sufficient.

## Correctness finding

The new death transaction is only reliable if supported callers cannot bypass it.

A future caller using the legacy `NightmareService.canonicalDeath(...)` helper could consume active Nightmare ownership before the player reset was durably persisted, recreating the crash window #124 closes.

A caller using public `NightmareDeathRegistryData.complete(...)` could erase the durable terminal-death intent before the player reset and exact teardown had reached their required persistence checkpoints. That would remove the fact login recovery uses to distinguish an already-chosen death outcome from ordinary active-Nightmare recovery.

## Change

- remove the legacy public `NightmareService.canonicalDeath(...)` method;
- make `NightmareDeathRegistryData.begin(...)` package-private;
- make `NightmareDeathRegistryData.complete(...)` package-private;
- keep public read/recovery inspection such as `findByPlayer(...)` unchanged;
- add `NightmareServiceApiTest` so the unsafe public mutation surface cannot silently return.

Package-private visibility intentionally allows the current coordinator/service implementation in the same package to own the transaction while preventing unrelated external Java callers from treating death-marker mutation as a general extension API.

## Lore/source boundary

This slice does not add or change a lore mechanic. The mandatory source policy and Java lore-alignment gate were re-read. The controlling primary evidence is unchanged from PR #124:

- Chapter 1 (`Nightmare Begins`) establishes ordinary First-Nightmare survival/death stakes and that failure can release a Nightmare Creature into the waking world;
- Chapter 887 (`Lapse of Judgment`) provides later direct confirmation of a First-Nightmare challenger dying and a Nightmare Creature being released.

No new canon proposition is introduced, and no corpse-Gate or creature-release implementation is added here.

## Evidence classification

- **CANON:** unchanged from #124 — ordinary First-Nightmare death is genuine failure/death rather than normal safe Spell ejection; failed First Nightmares can release a Nightmare Creature.
- **INFERRED:** none added.
- **DESIGN:** restart-replayable canonical-death mutation authority is centralized behind the package-owned death transaction; unsafe duplicate public mutation APIs are rejected.
- **UNKNOWN:** real process-kill convergence at the death transaction's durability checkpoints, exact corpse-Gate behavior, and storage-device/power-loss behavior beyond the joined NeoForge worker.
- **COMPATIBILITY:** supported NeoForge runtime behavior, save schema, successful completion recovery, technical/admin recovery, and public read-only death-marker inspection are unchanged. External preview-only Java code calling the removed/made-private APIs must migrate to the durable service rather than retain unsafe semantics.

## Tests

`NightmareServiceApiTest` asserts:

1. no public method named `canonicalDeath` exists on `NightmareService`;
2. `NightmareDeathRegistryData.begin` is not public;
3. `NightmareDeathRegistryData.complete` is not public;
4. `NightmareDeathRegistryData.findByPlayer` remains public for recovery inspection.

Existing #124 coordinator and marker tests remain the behavioral transaction coverage.

## Deliberate limits / next evidence

This API hardening is not a physical restart proof. It does not alter #124's durable sequence, add corpse-Gate consequences, or change Minecraft respawn accommodation.

After hosted CI is green for this exact head, the next correctness work should be physical fault evidence for canonical death if an executable environment becomes available, or another independently demonstrated persistence defect. Issue #34's successful-completion physical matrix remains separately blocked until a real player/environment can execute its same-world rows.
