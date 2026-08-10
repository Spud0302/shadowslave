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
6. emits only a compact result record for the parent to assert.

The parent requires the same persisted Nightmare/player identities and terminal resolution, exact stored Aspect/Flaw identities, exact generated identity equality, exactly one Attribute, Memory, and Echo, correct teardown selection for the still-active cut, and `alreadyComplete=true` for the fully committed cut.

## Evidence classification

- **CANON:** unchanged. No Nightmare, appraisal, Aspect, Flaw, Attribute, Memory, Echo, reward, progression, death, or failure rule changes.
- **INFERRED:** an already-resolved appraisal should remain the same exact result through technical restart instead of being regenerated against later generator/catalogue state.
- **DESIGN:** a fresh JVM/process boundary is stronger recovery evidence than same-process object reconstruction; the completion receipt remains Java recovery authority.
- **UNKNOWN:** a real NeoForge server process killed while world/player SavedData are being replaced, filesystem/fsync guarantees below a readable file image, post-verification corruption, and exact live timing at every adjacent durable boundary.
- **COMPATIBILITY:** no runtime code, save schema, gameplay semantics, catalogue, or dependency changes. This is test-only process-boundary evidence stacked on #208.

`docs/LORE-SOURCE-POLICY.md`, `docs/JAVA-LORE-ALIGNMENT.md`, and `docs/NIGHTMARE-SEED-ROADMAP.md` were re-read. This is technical crash/restart recovery, not a new in-world Spell mechanic, so no new primary-novel proposition is introduced.

## Deliberate limits

This is not yet a physical NeoForge server kill/restart test. It proves that recovery does not accidentally depend on originating-process receipt objects, generator results, or static state, and that the compressed durable receipt can cross a real JVM boundary.

It does not prove return-position persistence, live registry teardown, player-file attachment persistence, or receipt deletion in a restarted dedicated server process. Those remain the next stronger Issue #34 evidence layer.

Prepared-world durability #158 remains blocked under its recorded resume condition and is not retried here. The recurring hosted vanilla Nightmare-dimension transition stall is also not retried or given another timeout increase without new evidence or a changed diagnostic approach.

## Next evidence

After exact-head compile/unit/client/server gates are green and review-clean, add a dedicated-server restart harness that materializes at least one supported successful-completion persisted cut in actual world/player files, stops or kills the server, restarts it, and verifies waking return location, active Nightmare absence, Dreamer state, exact Aspect/Flaw/Attribute/Memory/Echo award, and completion-receipt consumption exactly once.
