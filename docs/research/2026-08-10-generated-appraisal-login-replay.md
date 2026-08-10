# Generated appraisal completion receipt login replay

## Scope

This slice consumes the durable successful-completion authority introduced by the generated appraisal completion receipt and makes restart/login converge the player's exact already-resolved appraisal state without invoking the generator or current content catalogues again.

The replay path accepts three player-state recovery shapes: an untouched Aspirant, a partially written appraisal attachment set, or an already-complete matching Dreamer whose receipt survived. It also handles the adjacent SavedData split where the completion receipt is durable but the matching active Nightmare teardown is not yet durable. Contradictory permanent identity, Attribute, Memory, Echo identity, incompatible Soul state, or contradictory active Nightmare ownership fails closed while the receipt remains present.

## Evidence classification

- **CANON:** unchanged. Nightmare completion remains distinct from appraisal; no new Aspect, Flaw, Attribute, Memory, Echo, progression, failure, reward, or scenario rule is introduced.
- **INFERRED:** an appraisal result already resolved for a conquered Nightmare should remain the same result after a technical restart rather than being regenerated against later code/content.
- **DESIGN:** login replay order, exact-state convergence, matching successful-teardown replay, partial-write acceptance, receipt precedence over ordinary active-instance reconciliation, and technical recovery wording are Java persistence/recovery infrastructure.
- **UNKNOWN:** physical power-loss/fsync ordering below Minecraft's player-file/SavedData writes, direct persisted-player-file verification for the final receipt-consumption boundary, and physical process-kill evidence at every adjacent write boundary.
- **COMPATIBILITY:** existing valid Aspirant and exact Dreamer states replay; the stored generated award remains authoritative; additional unrelated owned Attributes/Memories/Echoes are preserved; mutable command/manifestation state on an already-owned matching Echo is preserved.

No primary-lore claim is needed for the technical replay algorithm. The mandatory lore source policy, Java lore-alignment gate, Nightmare roadmap, and Issue #34 were re-read. The architecture continues to classify restart recovery as technical rather than ordinary mercy from the Nightmare Spell.

## Review correction: retained active ownership

A P1 review found a real durable-boundary split in the first replay implementation. A server can stop after the completion receipt is persisted but before the subsequent successful exit/registry removal reaches disk. Restart then loads both the receipt and the still-active exact Nightmare instance.

The original login precedence replayed appraisal and cleared the receipt immediately. That could leave a persisted Dreamer who still owned an active Nightmare; a later login outside the Nightmare could route that stale ownership through technical recovery and erase the recovered permanent state.

The corrected replay therefore treats the receipt as successful-completion authority for both halves of the interrupted transaction:

1. load the exact player-scoped completion receipt;
2. compare current player attachments to the stored generated award before mutation and reject contradictions;
3. inspect active Nightmare ownership;
4. if ownership is absent, continue because teardown already reached persistence;
5. if ownership exists, require it to equal the receipt's exact `NightmareInstance`, consume it through the normal `SUCCESS` exit/teardown path, and cross a SavedData persistence barrier while the receipt remains present;
6. reject a different active instance rather than guessing which authority is correct;
7. converge missing exact Aspect/Flaw identity, Attribute, Memory and Echo ownership;
8. converge Aspirant to the exact matching Dreamer Soul state, or accept an already-matching Dreamer;
9. save player data;
10. only then clear and persist the completion receipt.

The generator is never invoked during replay.

## Tests

`GeneratedAppraisalRecoveryServiceTest` covers untouched Aspirant convergence, partial attachment convergence without duplication, fully committed Dreamer idempotence, contradictory partial-state rejection, incompatible Spell-state rejection, exact active-instance selection for successful replay teardown, no-op ownership reconciliation when teardown is already absent, and fail-closed rejection of contradictory active ownership.

The same correction also updates stale `NightmareInstance` test fixtures to the current persisted resolution-state constructor shape so hosted CI reaches the recovery assertions rather than failing in `compileTestJava`.

## Remaining Issue #34 boundary

The replay path now preserves the receipt across an interrupted active-registry teardown and already orders player save before receipt clear. Normal same-process completion has been corrected to save the appraised player before clearing the receipt as well.

This still does not claim a physical fsync guarantee or direct semantic verification of the persisted player file before receipt deletion. A subsequent correctness slice should add direct persisted-player semantic verification before receipt consumption in both normal success and replay (or demonstrate an equivalent stronger transaction), then add restart-boundary/process-kill evidence.
