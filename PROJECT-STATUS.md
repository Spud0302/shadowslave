# Shadow Slave project status

**Status date:** 2026-08-13  
**Authoritative baseline:** `main@c72a8f1cdeab8d7316e0a65db0486b765d0b5627`  
**Release state:** active alpha development; no public Java release

## Current direction

PR #281 is merged and is the current scope/architecture guardrail. `shadow-slave.jar` keeps canonical Shadow Slave authority, persistence, provider contracts, and small representative playable slices. Deep generic combat belongs in standalone `combat-core`; broad region/content work should move behind coherent optional provider/WIP modules rather than continuing to widen the base JAR.

Read before expanding scope:

- `docs/design/MODULAR-JAR-BOUNDARIES.md`
- `docs/design/NIGHTMARE-SEED-EXPANSION-SAFETY.md`
- `docs/LORE-SOURCE-POLICY.md`
- `docs/NIGHTMARE-SEED-ROADMAP.md`

## What merged main already represents

Current main is well beyond the old August 1 preview.2 baseline. The merged lineage includes:

- persistent Soul / Aspect / Flaw / Attribute identity and Nightmare lifecycle authority;
- multiple First-Nightmare scenario/role/appraisal integration work;
- generated appraisal state plus durable successful-completion recovery records and restart-focused evidence;
- Memory ownership/manifestation with Ash Compass and Glass Road playable examples;
- Echo ownership/commands with an Ash Burrower baseline;
- registered Nightmare Creature execution and GeckoLib/SmartBrainLib infrastructure;
- an Ashen Expanse / Cinder Rest Dream Realm vertical slice;
- normal gameplay keybinds backed by server-authoritative intents;
- a real same-world two-JVM dedicated-server restart gate;
- the bounded Glass Road commitment/recovery combat proof;
- design constraints for combat, progression, Essence, injuries, and supernatural target/defense interactions without treating those notes as blanket implementation authorization.

The frozen datapack remains a historical/reference product. It is not the architecture target for new Java gameplay work.

## Active integration / WIP lanes

### #282 — Combat Core MVP — Draft

Standalone NeoForge `combat-core` now has source-level action phases, bounded melee geometry, and an ordinary server-side player-melee proof. It remains Draft because hosted jobs have repeatedly failed before runner allocation/checkout, so standalone build, physical client/server boot, play proof, mob execution, Shadow Slave integration, and final duplicate-authority audit are not yet established.

### #283 — Storm Lantern provider extraction — Draft

First physical extraction of reviewed Storm Lantern vocabulary behind an optional region-provider/WIP boundary. It is intentionally not merge-ready yet and must prove independent provider loading/equivalence before duplicate base implementation is removed.

### #284 — Nightmare entry durability on current main — Draft

Re-establishes the base entry-ordering primitive on current main. It is intentionally Draft until `NightmareService.tryEnter(...)` is actually wired through the coordinator without importing stale cumulative ancestry.

### #275 / #276 / #278 / #279 — current integration candidates

These consolidate world, Echo, recovery, and gameplay slices respectively. Their current exact heads are **not green**: recent Preview Gates attempts on the current-main re-roots failed before any repository step executed because no hosted runner was allocated. Their older source-lineage passes remain useful evidence but are not substitutes for exact-head current-main validation.

Do not rerun unchanged heads merely to chase runner allocation. Resume when runner availability changes, owner intervention creates a credible execution opportunity, or a substantive source change creates a new validation target.

## Current correctness boundaries

- **Issue #34 remains open:** successful-completion recovery still lacks a genuine networked `ServerPlayer` reconnect across two dedicated-server JVMs. FakePlayer/fresh-JVM/disk-image evidence is valuable but not equivalent to that boundary.
- **Prepared Nightmare world/chunk durability remains blocked** under the recorded #158 resume conditions.
- Provider-removal safety is now architecturally pinned: unavailable providers must not leave maturing Seeds, active unwinnable Gates, or stranded active-Nightmare participants. The runtime implementation of that later-Nightmare provider-removal contract is still future work.

## Repository cleanup state

Issue #285 tracks the 2026-08-13 archive pass. Stale base-breadth PRs are being closed **without deleting their branches**, so useful implementation/history remains available for provider/WIP ports while the active PR queue reflects the current modular direction.

The Better Combat spike #277 is archived as comparison evidence; #282 Combat Core is the active generic-combat path.

## Merge / evidence rule

- Runtime/integration PRs are not merge-ready merely because their source lineage passed on an older base.
- Draft means intentionally incomplete.
- A workflow that fails with no runner/steps is infrastructure evidence, not a source failure and not a pass.
- Supersede or retire source PRs only after ancestry/equivalence evidence proves the successor contains the needed behavior.
- Documentation/admin PRs may merge without runtime gates when their paths do not trigger those gates, but substantive review findings must be resolved first.

## Next admin/development priorities

1. Keep the active queue centered on #282, #283, #284 and the current integration candidates rather than reopening archived breadth.
2. Obtain real executed exact-head validation for #276/#278/#279 when hosted runners are available.
3. Continue Combat Core only to its MVP acceptance boundary, then integrate representative Shadow Slave actions instead of expanding generic feature scope.
4. Wire #284 into current-main entry carefully and preserve the mature #152/#178 durability contracts as source evidence.
5. Continue Storm Lantern work through the provider/WIP boundary rather than broadening the base region implementation.
