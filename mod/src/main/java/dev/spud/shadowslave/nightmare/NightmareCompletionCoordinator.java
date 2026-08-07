package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/** Ordered, restart-replayable successful Nightmare completion workflow. */
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
            checked.afterDurableBoundary(NightmareCompletionFaultPoint.AFTER_APPRAISAL_PLAYER_SAVE);
        }
        if (!checked.appraisalApplied()) {
            throw new IllegalStateException("Successful Nightmare appraisal did not reach the expected state");
        }
        advanceAndPersist(checked, NightmareCompletionPhase.APPRAISAL_COMMITTED, false,
                NightmareCompletionFaultPoint.AFTER_APPRAISAL_REGISTRY_SAVE);

        if (plan.returnPlayer()) {
            checked.returnPlayer();
            if (!checked.returnDestinationReached()) {
                throw new IllegalStateException(
                        "Successful Nightmare return did not reach the recorded return dimension"
                );
            }
            checked.persistPlayer();
            checked.afterDurableBoundary(NightmareCompletionFaultPoint.AFTER_RETURN_PLAYER_SAVE);
        }
        advanceAndPersist(checked, NightmareCompletionPhase.RETURN_COMMITTED, false,
                NightmareCompletionFaultPoint.AFTER_RETURN_REGISTRY_SAVE);

        if (plan.teardownActiveInstance()) {
            checked.teardownActiveInstance();
        }
        advanceAndPersist(checked, NightmareCompletionPhase.TEARDOWN_COMMITTED,
                plan.teardownActiveInstance(), NightmareCompletionFaultPoint.AFTER_TEARDOWN_REGISTRY_SAVE);
    }

    private static void advanceAndPersist(
            Operations operations,
            NightmareCompletionPhase target,
            boolean forcePersist,
            NightmareCompletionFaultPoint faultPoint
    ) {
        NightmareCompletionPhase before = operations.phase();
        operations.advancePhase(target);
        if (forcePersist || operations.phase() != before) {
            operations.persistRegistry();
            operations.afterDurableBoundary(faultPoint);
        }
    }

    public interface Operations {
        NightmareCompletionPhase phase();
        boolean appraisalApplied();
        boolean playerInNightmare();
        boolean returnDestinationReached();
        boolean activeOwnershipPresent();
        void applyAppraisal();
        void returnPlayer();
        void teardownActiveInstance();
        void advancePhase(NightmareCompletionPhase target);
        void persistPlayer();
        void persistRegistry();

        default void afterDurableBoundary(NightmareCompletionFaultPoint point) {
            NightmareCompletionFaultInjector.afterDurableBoundary(point);
        }
    }
}
