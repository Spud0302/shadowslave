# Shadow Slave — current issues, blockers, and limitations

**Status date:** 2026-08-13  
**Canonical current state:** `PROJECT-STATUS.md`  
**Baseline:** `main@c72a8f1cdeab8d7316e0a65db0486b765d0b5627`

The old August 1 preview.2 / Claude correction batch is historical evidence, not the current issue list. Preserve those records under `docs/reviews/`, `docs/history/`, Git history, and their original issues/PRs rather than carrying obsolete status forward here.

## 1. Successful-completion recovery still lacks the strongest process/network proof

Issue #34 remains open.

Current main and the recovery lineage have durable completion receipts, exact generated-appraisal replay state, persisted-image verification, fresh-JVM/disk-image evidence, login-precedence contracts, and a same-world two-JVM dedicated-server restart substrate. #278 additionally carries a bounded server-side FakePlayer GameTest proof.

What is **not** yet proven is the strongest target: a genuine networked `ServerPlayer` completing/recovering through production login behavior across two dedicated-server JVMs while exact return state, active ownership teardown, appraisal/rewards, and receipt consumption converge exactly once.

Do not relabel FakePlayer, codec, disk-image, or same-world boot evidence as that proof.

## 2. Prepared Nightmare world/chunk durability remains blocked

The prepared-world durability work tracked through #158 remains blocked under its recorded resume conditions. Do not add another speculative checkpoint or retry the blocked lane without new evidence, a credible physical test seam, or owner input that changes the boundary.

## 3. Hosted GitHub runner allocation is blocking current exact-head validation

Several current integration heads have GitHub Actions runs that reached a completed failure state **before checkout**, with no job steps/runner allocation. This has affected current-main validation for lanes including #275, #276, #278, #279, and Combat Core #282.

This is infrastructure evidence:

- it is not a source/test/runtime failure;
- it is not a green gate;
- older green source-lineage runs remain useful but do not validate a newer current-main re-root;
- do not repeatedly rerun an unchanged head solely to chase allocation.

Resume when runners are credibly available, owner intervention changes execution conditions, or a substantive source change creates a legitimate new head.

## 4. Combat Core MVP is source-implemented but physically unproven

PR #282 is Draft.

Current source includes the standalone NeoForge project, action phase/state primitives, bounded melee geometry, and an ordinary server-side player-melee proof. Remaining MVP evidence includes:

- standalone build/JUnit execution;
- physical NeoForge client and dedicated-server boot;
- ordinary sword-vs-target play proof;
- a generic mob executor seam;
- generic damage/presentation hooks needed by the Shadow Slave integration proof;
- one representative Shadow Slave player integration and one creature/action integration;
- final duplicate-authority audit.

Do not expand into limb injury, advanced stability, broad damage channels, parry trees, Essence economy, advanced movement, or build graphs before the MVP proves those seams are needed.

## 5. Storm Lantern / Drowned Bell module extraction is incomplete

PR #283 is Draft. The direction is now to preserve the Drowned Bell/later-site identity proof in base while moving deeper Storm Lantern region vocabulary into an optional provider/WIP module.

Still required before destructive cleanup:

- independent optional JAR build/load proof;
- provider-present runtime discovery;
- equivalent-or-stronger site/encounter behavior for whatever is ported;
- a clear compatibility/removal path;
- deletion of duplicate base execution only after those proofs.

Do not resume broad Storm Lantern expansion inside the base mod simply because #275 contains useful reviewed work.

## 6. Current Nightmare entry durability primitive is not yet integrated

PR #284 is Draft. `NightmareEntryDurabilityCoordinator` pins the intended ordering, but current-main `NightmareService.tryEnter(...)` is not yet wired through it.

The integration must preserve newer scenario assignment, Drowned Bell support, generated appraisal/recovery, Glass Road, and current persistence APIs instead of wholesale-porting stale cumulative ancestry.

## 7. Provider-removal safety is a design contract, not a finished runtime feature

Merged #281 now requires optional later-Nightmare providers to fail safe when missing:

- unresolved Seeds must stop escalating;
- already-bloomed linked Gates must be suspended/contained while remaining unresolved;
- participants already inside provider-owned active Nightmares need a base-owned technical recovery route;
- provider disappearance must never itself award success, appraisal, rewards, or progression.

The architecture is pinned in `docs/design/NIGHTMARE-SEED-EXPANSION-SAFETY.md`; later runtime implementation remains outstanding.

## 8. Current integration candidates are not merge-ready until exact-head evidence exists

- #275 — cumulative Drowned Bell/Storm Lantern world integration — Draft / current head not executed successfully.
- #276 — Ash Burrower guard + carry integration — older lineage green; current-main re-root failed before checkout.
- #278 — recovery consolidation — current-main exact head failed before checkout; mature #178 durability contracts are not yet fully reconciled.
- #279 — gameplay interaction consolidation — source PRs were individually green; current-main exact head failed before checkout.

Do not close their source PRs solely because these consolidation branches exist. Retire sources only after successful exact-head validation and final containment/equivalence checks.

## 9. The frozen datapack is legacy/reference architecture

The frozen datapack remains useful as a reference/regression product, but its single global Nightmare slot and command-era global selectors are not the target architecture for Java multiplayer or provider/module work.

Do not spend current alpha scope trying to evolve the frozen datapack into feature parity with the Java mod unless the owner explicitly requests maintenance of that product.

## 10. No public Java release / full interactive alpha acceptance yet

Merged main has a substantial playable foundation, but no public Java release is claimed. Outstanding release-level evidence includes an accepted current-main integration state, interactive play/feel validation of the assembled alpha, and resolution or explicit deferral of the correctness boundaries above.

## Admin/backlog limitation

Issue #285 tracks the archive pass. Closing a stale base-breadth PR under that issue preserves its branch/history; it is not evidence that the implementation is bad or permanently rejected. Reopen only when a coherent provider/WIP module actually needs it, a missing base contract is demonstrated, or the owner changes the scope boundary.

## Reporting new defects

Record:

- exact branch/head/build;
- reproduction steps;
- expected vs observed result;
- logs/workflow/job evidence where relevant;
- whether the finding is correctness, integration, presentation, balance, lore, packaging, external infrastructure, or missing scope;
- whether an existing durable/replay authority still survives the failure.
