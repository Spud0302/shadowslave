# Generated appraisal completion receipt login replay

## Scope

This slice consumes the durable successful-completion authority introduced by the generated appraisal completion receipt and makes restart/login converge the player's exact already-resolved appraisal state without invoking the generator or current content catalogues again.

The replay path accepts three recovery shapes: an untouched Aspirant after teardown, a partially written appraisal attachment set, or an already-complete matching Dreamer whose receipt survived. Contradictory permanent identity, Attribute, Memory, Echo identity, or incompatible Soul state fails closed while the receipt remains present.

## Evidence classification

- **CANON:** unchanged. Nightmare completion remains distinct from appraisal; no new Aspect, Flaw, Attribute, Memory, Echo, progression, failure, reward, or scenario rule is introduced.
- **INFERRED:** an appraisal result already resolved for a conquered Nightmare should remain the same result after a technical restart rather than being regenerated against later code/content.
- **DESIGN:** login replay order, exact-state convergence, partial-write acceptance, receipt precedence over active-instance reconciliation, and the technical recovery message are Java persistence/recovery infrastructure.
- **UNKNOWN:** physical power-loss/fsync ordering below Minecraft's player-file/SavedData writes, direct persisted-player-file verification for the final receipt-consumption boundary, and physical process-kill evidence at every adjacent write boundary.
- **COMPATIBILITY:** existing valid Aspirant and exact Dreamer states replay; the stored generated award remains authoritative; additional unrelated owned Attributes/Memories/Echoes are preserved; mutable command/manifestation state on an already-owned matching Echo is preserved.

No primary-lore claim is needed for the technical replay algorithm. The mandatory lore source policy, Java lore-alignment gate, Nightmare roadmap, and Issue #34 were re-read. The architecture continues to classify restart recovery as technical rather than ordinary mercy from the Nightmare Spell.

## Recovery ordering

1. load the exact player-scoped completion receipt;
2. compare current player attachments to the stored generated award before mutating anything;
3. reject contradictory state while retaining recovery authority;
4. converge missing exact Aspect/Flaw identity, Attribute, Memory and Echo ownership;
5. converge Aspirant to the exact matching Dreamer Soul state, or accept an already-matching Dreamer;
6. save player data;
7. only then clear and persist the completion receipt.

The generator is never invoked during replay.

## Tests

`GeneratedAppraisalRecoveryServiceTest` covers untouched Aspirant convergence, partial attachment convergence without duplication, fully committed Dreamer idempotence, contradictory partial-state rejection, and incompatible Spell-state rejection.

## Remaining Issue #34 boundary

This slice establishes restart/login replay and a player-save-before-receipt-clear order in the replay path. It does not yet claim a physical fsync guarantee or direct semantic verification of the persisted player file before receipt deletion. The normal same-process success path also still needs that final player-persistence proof before Issue #34 can be called fully solved.

Resume condition for the next correctness slice: after this stacked branch is exact-head green, either add direct persisted-player semantic verification before receipt consumption (and use the same verifier in normal success and replay), or demonstrate an equivalent stronger persistence transaction with restart-boundary tests and physical process-kill evidence.
