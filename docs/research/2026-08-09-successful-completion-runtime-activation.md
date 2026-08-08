# Successful completion runtime activation — 2026-08-09

## Scope

This slice makes the rebuilt successful-Nightmare completion transaction live for the existing **The Last Signal** preview terminal interaction.

It stacks on the server-backed completion adapter rather than reimplementing persistence or appraisal state. The activation path now:

1. resolves the exact active Nightmare and altar interaction;
2. validates that the authored terminal altar is structurally valid without mutating it;
3. records `TERMINAL_RESOLUTION_RECORDED` against the exact active `NightmareInstance`;
4. persists overworld SavedData and joins NeoForge's queued I/O worker;
5. exposes the deterministic `after_terminal_registry_save` physical fault boundary;
6. applies the idempotent lit-campfire world presentation;
7. resumes the existing restart-replayable completion coordinator;
8. on login, gives any retained successful-completion receipt precedence over ordinary active-instance or technical recovery.

Terminal activation and login precedence are intentionally one review slice. Activating durable receipts without changing login classification would create an unsafe crash state: a player could restart outside the Nightmare with an incomplete successful-completion receipt and still be routed into technical recovery instead of completion replay.

## Authority boundary

The signal-fire interaction is still the current preview's **DESIGN** terminal event. Before the durable receipt exists, the server validates that the interacted block is the exact registered altar and that the authored Soul Campfire state is structurally valid.

After that validation succeeds and the receipt reaches the joined SavedData checkpoint, the receipt is the recovery authority. The campfire's lit state is then replayable world presentation, not the only evidence that the Nightmare ended. This ordering is deliberate: a process halt immediately after `after_terminal_registry_save` must still leave enough durable Java state to finish appraisal, return and teardown after restart.

The activation seam has a pure ordering test so the initial physical boundary is covered separately from `NightmareCompletionCoordinatorTest`, whose six-point loop can only inject boundaries reached from inside the coordinator after a receipt already exists.

## Login recovery precedence

A retained successful-completion receipt always wins over ordinary active-instance handling:

```text
successful completion receipt present -> resume successful completion
else active + player in Nightmare      -> restore active Nightmare
else active + player outside Nightmare -> technical recovery
else                                   -> no Nightmare recovery
```

This remains a technical Minecraft recovery rule. It is not presented as behavior of the Nightmare Spell.

## Lore evidence checked

Research followed `docs/LORE-SOURCE-POLICY.md` and rechecked primary chapter material plus later clarification.

- **Chapter 15 — Shadow Slave:** the First Nightmare trial is already over before appraisal; appraisal then precedes the post-trial Dreamer/progression state.
- **Chapter 16 — Rebirth:** the post-trial transformation continues through Dreamer/Dormant progression, Aspect Ability and Flaw revelation before ordinary waking continuation.
- **Chapter 743 — Appraisal:** a later Nightmare is explicitly over before appraisal begins in the transition space between dream and reality.
- **Chapter 1581 — Shadows and Dust:** later material again describes Nightmare collapse followed by the ordinary appraisal/rank-progression step for surviving challengers, while Sunny's missing appraisal is exceptional.

Official WebNovel was checked for the later clarification. At research time its public catalogue was ahead of the owner-designated NovelFull access index, so no claim is made that the third-party index is current through the latest official chapter.

### Evidence classification

- **CANON:** Nightmare terminal resolution/end is distinct from appraisal; ordinary successful post-Nightmare progression follows the ended Nightmare rather than being the event that ends it.
- **INFERRED:** one incomplete successful-completion recovery transaction remains associated with the exact resolved Nightmare instance until reconciliation is finished.
- **DESIGN:** durable receipt ordering, joined SavedData checkpoint, login recovery precedence, deterministic process-fault point and treating the validated campfire's lit state as replayable world presentation are Minecraft implementation choices.
- **UNKNOWN:** real same-world dedicated-server process-kill convergence at all six boundaries remains unproven; a crash after terminal receipt persistence can leave the campfire visually unlit because world presentation is deliberately not progression authority; filesystem/power-loss semantics beyond NeoForge's joined worker and administrator repair of blocked state remain unproven.
- **COMPATIBILITY:** the fixed Last Signal scenario/appraisal identity is unchanged; no new appraisal formula, Aspect/Flaw rule, Memory/Echo rule or save schema is introduced by this activation slice. Existing active-Nightmare and technical recovery routing remains unchanged when no successful-completion receipt exists.

No canonical scoring, generation or universal campfire-completion rule is claimed.

## Tests

`NightmareSuccessfulCompletionActivationTest` checks:

- terminal validation -> receipt -> registry persistence -> terminal fault boundary -> world presentation -> coordinator resume ordering;
- a simulated terminal-boundary fault stops before world presentation/recovery;
- disappearance of the receipt before coordinator replay fails closed.

`NightmareLoginRecoveryPolicyTest` checks:

- a retained successful-completion receipt always has precedence across every active/location combination;
- ordinary active-in-Nightmare restoration and outside-Nightmare technical recovery remain distinct when no receipt exists;
- no Nightmare-owned state results in no recovery action.

Existing coordinator tests continue to cover replay after the later appraisal/return/teardown durability boundaries and cancelled-return safety.

## Deliberate limits / next work

This slice does not port the historical technical-exit, preview-reset or canonical-death transaction stacks. Those remain separate correctness work and must not be silently inferred from successful-completion activation.

It also does not claim physical restart evidence. The next highest-value slice is to port the isolated ModDev completion-fault runner plus the retained-`latest.log` verifier correction onto this current consolidation, then execute the six same-world Issue #34 process-kill rows. Any failure should be recorded against the exact boundary/output rather than answered by adding speculative transaction state.
