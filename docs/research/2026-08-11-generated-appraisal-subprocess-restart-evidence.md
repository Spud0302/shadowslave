# Generated appraisal subprocess restart evidence

**Date:** 2026-08-11  
**Parent correctness edge:** PR #208 / `gpt/generated-appraisal-restart-cut-matrix`  
**Tracker:** Issue #34

## Why this slice

PR #208 is now direct-to-main and its fresh combined-state Preview Gates #216 / Actions run `31426059832` passed. Its deterministic restart-cut matrix reconstructs the complete production completion receipt through NBT before recovery planning, but it still executes the modeled restart in one JVM.

The next bounded evidence step is therefore a real process/static-state boundary without changing gameplay semantics or adding another persistence checkpoint.

## What this adds

`GeneratedAppraisalCompletionSubprocessRestartTest` writes only the complete durable `NightmareCompletionReceiptData.Receipt` image to a temporary compressed-NBT file, then launches a brand-new Java process using the test runtime classpath.

The child process:

1. reads the receipt file through `NbtIo.readCompressed`;
2. reconstructs the receipt through the production `Receipt.load(...)` codec;
3. discards all originating-process Java objects and static state by construction;
4. reconstructs either an untouched Aspirant cut or an already-complete Dreamer cut;
5. runs the production active-ownership replay selector and generated-appraisal recovery planner;
6. emits the complete loaded appraisal-snapshot NBT plus a compact convergence record for the parent to assert.

The parent compares the child's complete loaded snapshot against the independently retained parent snapshot, then requires the same persisted Nightmare/player identities and terminal resolution, exact stored Aspect/Flaw identities, exact generated identity equality, exactly one Attribute, Memory, and Echo, correct teardown selection for the still-active cut, and `alreadyComplete=true` for the fully committed cut. This prevents a receipt-codec regression in generator metadata or any non-ID generated record field from hiding behind child-side self-comparison.

## Review corrections

Fresh review after the first direct-to-main green run found two gaps.

### Complete award comparison

The original subprocess result compared the child's recovered identity back to the child's loaded snapshot and asserted collection sizes. That crossed only a subset of exact values independently. The corrected test now emits the complete `GeneratedAppraisalRecoverySnapshot.save()` image from the child; the parent expectation is generated from the pre-subprocess snapshot. Any codec corruption of generator version, seed, fingerprint, Aspect/Flaw fields, Attribute, Memory, or Echo now changes the subprocess result.

### Normal-completion teardown durability

Review also identified a P1 in the inherited normal completion path. `exit(...SUCCESS)` removed active ownership only in live `NightmareRegistryData`; appraisal could then be committed and the independent completion receipt cleared before the registry removal had been directly verified on disk. A stale persisted active instance plus a consumed receipt could cause the next login to enter ordinary active-Nightmare reconciliation and technically recover a legitimately completed Dreamer.

The corrected path keeps the exact completion receipt present, performs the successful exit/teardown, calls the SavedData durability barrier, then directly reloads `shadowslave_nightmares.dat` through `PersistedNightmareOwnershipVerifier`. The verifier requires a structurally healthy `data.instances` list, production-decodes every surviving instance, and rejects either the completed player identity or completed instance identity. Appraisal mutation and eventual receipt consumption occur only after that proof succeeds.

This uses the same persistence invariant already developed on the canonical-death correctness lineage, but the implementation is carried into the current generated-appraisal integration edge instead of depending on divergent old ancestry.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, reward, progression, death, or failure rule changes.
- **INFERRED:** an already-resolved appraisal should remain the same exact result through technical restart instead of being regenerated against later generator/catalogue state.
- **DESIGN:** a fresh JVM/process boundary is stronger recovery evidence than same-process object reconstruction; the completion receipt remains Java recovery authority; receipt authority must remain available until persisted active-ownership teardown is directly observable.
- **UNKNOWN:** a real NeoForge server process killed while world/player SavedData are being replaced, filesystem/fsync guarantees below a readable file image, post-verification corruption, and exact live timing at every adjacent durable boundary.
- **COMPATIBILITY:** no save schema or lore-facing gameplay semantic changes. The new registry-path helper and verifier expose/check existing persistence only.

`docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read. This is technical crash/restart recovery, not a new in-world Spell mechanic, so no new primary-novel proposition is introduced.

## Deliberate limits

This is not yet a physical NeoForge server kill/restart test. It proves that recovery does not accidentally depend on originating-process receipt objects, generator results, or static state, and that the compressed durable receipt can cross a real JVM boundary.

The new normal-completion checkpoint proves only that the expected active ownership is absent from a readable persisted registry image while the completion receipt remains available. It does not establish storage-device fsync semantics or protect against later corruption.

Return-position persistence, actual player-file reload, live server restart timing, and receipt deletion under a killed/restarted dedicated server remain the next stronger Issue #34 evidence layer.

Prepared-world durability #158 remains blocked under its recorded resume condition and is not retried here. The recurring hosted vanilla Nightmare-dimension transition stall is also not retried or given another timeout increase without new evidence or a changed diagnostic approach.

## Next evidence

After the corrected exact head passes compile/unit/client/server gates and review is clean, add a dedicated-server restart harness that materializes at least one supported successful-completion persisted cut in actual world/player files, stops or kills the server, restarts it, and verifies waking return location, active Nightmare absence, Dreamer state, exact Aspect/Flaw/Attribute/Memory/Echo award, and completion-receipt consumption exactly once.
