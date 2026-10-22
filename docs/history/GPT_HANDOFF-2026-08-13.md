# GPT handoff — current modular alpha

**Read first in a new GPT or Claude session.**  
**Repository:** `Spud0302/shadowslave`  
**Baseline branch:** `main`  
**Status baseline:** `main@c72a8f1cdeab8d7316e0a65db0486b765d0b5627` on 2026-08-13

## Mandatory first reads

Before lore-sensitive design or implementation, read `docs/LORE-SOURCE-POLICY.md` and verify relevant primary novel evidence. Keep **CANON**, **INFERRED**, **DESIGN**, **UNKNOWN**, and **COMPATIBILITY** distinct.

Before choosing scope or reviving old work, also read:

- `PROJECT-STATUS.md`
- `ISSUES.md`
- `docs/design/MODULAR-JAR-BOUNDARIES.md`
- `docs/design/NIGHTMARE-SEED-EXPANSION-SAFETY.md`
- `docs/NIGHTMARE-SEED-ROADMAP.md`

Do not treat old PR descriptions or the August 1 preview documents as current authority when they conflict with current main or these status files.

## Current architecture direction

Merged PR #281 establishes the active development model:

- `shadow-slave.jar` owns Shadow Slave grammar/authority, persistence, provider contracts, and representative playable slices;
- `combat-core` is Shadow-Slave-agnostic generic combat infrastructure;
- broad Dream Realm/region/Memory/Echo/creature content should preferentially live in coherent provider/WIP modules;
- advanced identity generation may eventually become an optional generator module while base retains a deterministic fallback;
- provider removal must fail safe for persisted Seeds, active Gates, and participants already inside provider-owned Nightmares;
- resolved permanent state never silently rerolls because an optional provider/generator changes.

Do not deepen a base feature simply because an old open branch already contains more implementation.

## Active lanes

### #282 — `gpt/combat-core-mvp` — Draft

Active generic combat direction. Source currently contains the independent project, action phases, melee geometry, and an ordinary server-side player-melee proof. Hosted jobs have failed before checkout/no runner allocation, so no standalone build or physical boot/play success is claimed. Keep scope at MVP.

### #283 — `gpt/storm-lantern-region-wip-extraction` — Draft

First Storm Lantern optional provider/WIP extraction. Preserve the Drowned Bell / later-site identity proof while moving deeper region vocabulary out of the base. Do not delete duplicate base/source implementation until provider-present loading and equivalence are proven.

### #284 — `gpt/base-entry-durability-current-main` — Draft

Current-main recovery slice pinning entry durability ordering. The coordinator is not yet wired into `NightmareService.tryEnter(...)`; do not call this solved or merge-ready.

### #275 / #276 / #278 / #279

Current world, Echo, recovery, and gameplay integration candidates. Recent exact-head Preview Gates on current-main re-roots failed before any repository step ran because no hosted runner was allocated. Older green source-lineage evidence remains relevant, but these heads are not green.

### Recovery source evidence

Keep #152, #158, and #178 available as mature correctness/durability evidence until the current-main recovery lane deliberately absorbs or replaces their contracts. Do not close them just for age.

## Current blockers / non-claims

- Issue #34 still lacks a genuine networked `ServerPlayer` reconnect across two dedicated-server JVMs for successful-completion recovery.
- #158 prepared Nightmare world/chunk durability remains blocked under its recorded resume conditions.
- Combat Core does not yet have an executed standalone build/physical boot/play gate.
- Storm Lantern provider extraction is incomplete and is not release/module admission yet.
- Provider-removal Seed/Gate/active-instance safety is architecturally specified but not yet a finished runtime subsystem.
- No public Java release is claimed.

## Backlog cleanup

Issue #285 records the archive pass. Stale faction/investigation content PRs and Better Combat spike #277 have been closed unmerged while their branches/history remain preserved.

Treat an archived PR as source/WIP material, not active base scope. Reopen only when an active provider/module explicitly consumes it, equivalence analysis shows it owns a missing base contract, or the owner changes direction.

## Working rules

- Default to a `gpt/` branch and PR for repository changes; do not casually write directly to `main`.
- **Explicit owner authorization can include admin/merge work.** When the owner directly asks for merges/admin cleanup, GPT may merge a PR only after verifying the exact head is appropriate: not intentionally incomplete, no unresolved substantive review finding, and required evidence is satisfied for the type of change.
- Never merge a Draft runtime/integration PR merely to simplify the graph.
- Never interpret a no-runner/no-steps workflow failure as a pass or as a code defect.
- Do not blindly rerun unchanged hosted heads just to chase runner allocation.
- Every GPT-authored commit includes `Co-Authored-By: ChatGPT <gpt@openai.com>`.
- Use JDK 21.
- Preserve reviewed history/evidence; port or supersede only with explicit containment/equivalence proof.
- Avoid broad catalogue/runtime growth in the base when #281 says the lane belongs behind a provider/module boundary.
- Design notes for limb injury, advanced stability, target layers, Essence economy, movement/build graphs, etc. are constraints for future systems, not authorization to implement all of them now.

## Useful current sequence

When taking over development:

1. Re-read current PR state before assuming an old handoff is still accurate.
2. Keep #282 at Combat Core MVP and obtain a real executed build/boot/play proof before expanding it.
3. Continue #284 by wiring only the current-main entry durability contract, preserving newer gameplay/appraisal APIs.
4. Let #283 prove the provider boundary before resuming broad Storm Lantern work.
5. Resume #276/#278/#279 exact-head validation only when hosted execution is credibly available or a substantive source change creates a new validation target.
6. Use #285 to keep archived breadth out of the active queue unless a module explicitly needs it.
