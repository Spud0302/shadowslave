# Preview appraisal reconciliation integration — 2026-08-08

## Scope

This note records the bounded appraisal adapter layered on top of the current successful-completion coordinator and durable receipt work. It does not wire runtime/login recovery yet and does not replace the fixed Last Signal appraisal with a procedural or canonical scoring model.

## Persistence problem

The successful-completion transaction persists Nightmare registry state and player attachments on separate durability surfaces. A restart can therefore observe either safe split-save state around appraisal:

- the expected fixed preview identity is present while the Soul is still Aspirant; or
- the expected Dreamer Soul transition is present while the persistent identity attachment is still empty.

The adapter must also recognize the exact already-applied state so replay is idempotent. Any unrelated permanent identity or unrelated Dreamer progression must fail closed rather than be overwritten.

The older recoverable-completion branch already established this reconciliation policy, but its `AspectInstanceData` used the pre-multi-ability shape. This integration ports the policy only and rebuilds the expected preview identity with the current `AspectAbilitySetData` representation and explicit legacy-unclassified compatibility metadata for Kindle.

## Primary lore evidence rechecked

Research followed `docs/LORE-SOURCE-POLICY.md`.

- **Chapter 15:** the First Nightmare trial is over before the Spell completes appraisal and the Aspirant becomes a Dreamer; permanent Aspect identity/revelation follows as part of that post-trial sequence.
- **Chapter 743:** a later Nightmare is explicitly declared over before appraisal proceeds, reinforcing the separation between terminal Nightmare resolution and appraisal.
- **Chapter 1581 (also checked on official WebNovel):** later material describes surviving challengers as normally leaving a collapsed Nightmare for appraisal and rank progression, while Sunny's missing appraisal is exceptional.

No appraisal scoring formula is inferred from those chapters. Chapter 743's subsequent speculation about what the Spell values remains character interpretation rather than a project rule.

## Evidence classification

- **CANON:** Nightmare terminal resolution and appraisal are distinct; ordinary surviving challengers are normally appraised after the Nightmare ends; the First-Nightmare sequence transitions the Aspirant into post-trial Dreamer/progression identity.
- **INFERRED:** none added by this adapter beyond the existing transaction association between one retained completion receipt and its resolved Nightmare.
- **DESIGN:** exact split-save repair states, idempotent reconciliation, fixed Last Signal identity, and fail-closed conflict handling are Minecraft persistence choices.
- **UNKNOWN:** the Spell's appraisal scoring/generation formula; physical process-kill behavior at the actual player/registry durability boundaries; exceptional Spell-disconnected appraisal behavior; administrator repair policy for conflicting permanent identity.
- **COMPATIBILITY:** the fixed preview identity continues to be Last Light / Kindle / Cold Ash. Kindle is represented through the current explicit `AspectAbilitySetData` model using the existing legacy-unclassified compatibility marker. No newer Attribute, Memory, Echo, content-catalogue or presentation identity is overwritten.

## Reconciliation contract

Given the fixed expected preview appraisal:

| Soul state | Identity state | Action |
| --- | --- | --- |
| Aspirant | empty | apply identity, then Soul transition |
| Aspirant | exact expected identity | apply Soul transition only |
| exact expected Dreamer state | empty | repair identity only |
| exact expected Dreamer state | exact expected identity | no-op |
| anything else | anything else | fail closed |

If the Soul transition throws after mutating the Soul, the adapter re-reads authoritative Soul state. It keeps the expected identity only when the expected Dreamer transition is actually present; otherwise it restores the identity that existed before the attempted transition.

## Limits and next integration boundary

The new pure classification tests prove the state policy without constructing a `ServerPlayer`. They do not prove attachment saves, NeoForge restart behavior, or the runtime coordinator adapter.

The next bounded slice should connect `NightmareCompletionCoordinator.Operations.appraisalApplied()` and `applyAppraisal()` to this adapter plus the durable `NightmareCompletionRecord`, while keeping terminal event/login routing separate if that makes the review smaller. Physical Issue #34 fault rows remain required after the runtime storage adapter exists.
