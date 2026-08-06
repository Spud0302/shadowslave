package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/**
 * Pure recovery decision for one successful Nightmare receipt.
 *
 * <p>The durable phase and player save can disagree after a process failure.
 * The observed state is therefore authoritative for whether an idempotent
 * action still needs to run, while the phase preserves ordered audit history.</p>
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
        Objects.requireNonNull(phase, "phase");
        return new NightmareCompletionRecoveryPlan(
                !appraisalApplied,
                playerInNightmare,
                activeOwnershipPresent
        );
    }
}
