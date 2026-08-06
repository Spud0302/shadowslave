package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/**
 * Ordered, restart-replayable successful Nightmare completion workflow.
 *
 * <p>Each externally visible action is persisted before its receipt phase is
 * advanced. A deterministic test implementation can fail after any persistence
 * call, reconstruct from durable state, and rerun this coordinator without
 * relying on process-kill timing.</p>
 */
public final class NightmareCompletionCoordinator {
    private NightmareCompletionCoordinator() {
    }

    public static void resume(Operations operations) {
        Operations checked = Objects.requireNonNull(operations, "operations");
        NightmareCompletionRecoveryPlan plan = NightmareCompletionRecoveryPlan.forState(
                checked.phase(),
                checked.appraisalApplied(),
                checked.playerInNightmare(),
                checked.activeOwnershipPresent()
        );

        if (plan.applyAppraisal()) {
            checked.applyAppraisal();
            checked.persistPlayer();
        }
        if (!checked.appraisalApplied()) {
            throw new IllegalStateException("Successful Nightmare appraisal did not reach the expected state");
        }
        advanceAndPersist(checked, NightmareCompletionPhase.APPRAISAL_COMMITTED, false);

        if (plan.returnPlayer()) {
            checked.returnPlayer();
            checked.persistPlayer();
        }
        advanceAndPersist(checked, NightmareCompletionPhase.RETURN_COMMITTED, false);

        if (plan.teardownActiveInstance()) {
            checked.teardownActiveInstance();
        }
        advanceAndPersist(
                checked,
                NightmareCompletionPhase.TEARDOWN_COMMITTED,
                plan.teardownActiveInstance()
        );
    }

    private static void advanceAndPersist(
            Operations operations,
            NightmareCompletionPhase target,
            boolean forcePersist
    ) {
        NightmareCompletionPhase before = operations.phase();
        operations.advancePhase(target);
        if (forcePersist || operations.phase() != before) {
            operations.persistRegistry();
        }
    }

    public interface Operations {
        NightmareCompletionPhase phase();

        boolean appraisalApplied();

        boolean playerInNightmare();

        boolean activeOwnershipPresent();

        void applyAppraisal();

        void returnPlayer();

        void teardownActiveInstance();

        void advancePhase(NightmareCompletionPhase target);

        void persistPlayer();

        void persistRegistry();
    }
}
