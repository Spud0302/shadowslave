# Technical/admin Nightmare exit intent persistence verification — 2026-08-10

## Scope

Audit the first durable authority in the technical recovery / administrator-abort transaction after canonical-death intent persistence was corrected on PR #164.

## Finding

`NightmareTechnicalExitCoordinator` recorded a technical-exit marker and called `SavedDataPersistence.saveAndWait(server)` before it was allowed to clear a retained successful-completion receipt, reset player state, or consume active ownership. Normal return from that save helper did not independently prove that the exact technical-exit marker existed in `shadowslave_nightmares.dat`.

That is a recovery-authority boundary: if the marker were absent after restart while later state had been consumed, the technical transaction could lose the only evidence that it must resume.

## Implemented contract

The first technical-exit phase now follows:

1. record or reassert the exact technical/admin exit intent;
2. explicitly dirty the registry so a same-process retry performs serialization even when the same marker already exists in memory;
3. call the existing joined SavedData persistence helper;
4. read the settled `shadowslave_nightmares.dat` file and require an exact marker match on player UUID, Nightmare instance UUID, and exit reason;
5. only after that proof may completion-receipt clearing, player reset, or ownership teardown begin.

Unlike the entry/completion digest checkpoints, this verifier checks the persisted marker contents rather than requiring a different whole-file digest. That matters for an ambiguous earlier write: if the exact authority is already on disk, retry may safely continue; if it is not on disk, retry remains blocked and the explicit dirty flag causes another serialization attempt.

## Evidence and source classification

- **CANON:** unchanged. This work does not alter ordinary Nightmare failure, success, appraisal, progression, or return rules.
- **INFERRED:** none added.
- **DESIGN:** technical/admin recovery requires persisted transaction authority before destructive later phases. Direct persisted-marker inspection is a Java durability guard, not a Spell rule.
- **UNKNOWN:** physical power-loss/fsync guarantees below the readable file image; behavior of failing storage hardware during atomic replacement; exact process-loss behavior between teleport return and first intent persistence.
- **COMPATIBILITY:** no registry schema change. Existing persisted technical-exit markers remain readable and replayable. Successful technical recovery/admin abort behavior is unchanged when persistence succeeds.

`docs/JAVA-LORE-ALIGNMENT.md` explicitly classifies crash, restart, administrator rescue, and development exits as technical recovery rather than ordinary mercy from the Nightmare Spell. No new novel proposition is introduced by this slice, so no new canon rule is inferred.

## Remaining risks

The initial technical-exit intent is now directly checked, but later phase saves still rely on the older durable intent as replay authority. Additional per-phase file verification should not be added mechanically unless a concrete reconstruction demonstrates that replay from the retained authority is insufficient.

Prepared Nightmare world/chunk durability remains separately blocked under the research note and resume conditions tracked by PR #158; this slice does not retry that problem.
