package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/**
 * Pure recovery decision for one successful Nightmare receipt.
 *
 * <p>The durable phase and player save can disagree after a process failure.
 * Actions are therefore derived from both the receipt and observed runtime
 * state. Replaying any requested action is required to be idempotent.</p>
 */
public record NightmareCompletionRecoveryPlan(
        boolean applyAppraisal,
        boolean returnPlayer,
        boolean teardownActiveInstance
) {
    public static NightmareCompletionRecoveryPlan forState(
            NightmareCompletionPhase phase,
            boolean appraisalApplied,
            boolean playerInNightmare,
            boolean activeOwnershipPresent
    ) {
        NightmareCompletionPhase checkedPhase = Objects.requireNonNull(phase, "phase");
        return new NightmareCompletionRecoveryPlan(
                !appraisalApplied,
                checkedPhase.ordinal() < NightmareCompletionPhase.RETURN_COMMITTED.ordinal()
                        || playerInNightmare,
                checkedPhase.ordinal() < NightmareCompletionPhase.TEARDOWN_COMMITTED.ordinal()
                        || activeOwnershipPresent
        );
    }
}
