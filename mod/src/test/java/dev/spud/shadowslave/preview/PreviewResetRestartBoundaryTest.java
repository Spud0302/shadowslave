package dev.spud.shadowslave.preview;

import dev.spud.shadowslave.soul.SoulData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviewResetRestartBoundaryTest {
    @Test
    void restartAfterDurableResetIntentConverges() {
        DurableState durable = DurableState.dirtyPreview();

        try {
            PreviewResetService.reset(new SimulatedOperations(durable, Fault.AFTER_FIRST_REGISTRY_SAVE));
        } catch (SimulatedCrash expected) {
            // Simulated process loss: only durable state survives into the next operations instance.
        }

        assertTrue(durable.resetIntentPresent);
        assertTrue(durable.nightmareActive);
        assertTrue(durable.successfulCompletionPresent);
        assertFalse(durable.playerCleared);

        PreviewResetService.reset(new SimulatedOperations(durable, Fault.NONE));

        assertConverged(durable);
    }

    @Test
    void restartAfterDurablePlayerSaveConvergesWithoutRestoringPreviewState() {
        DurableState durable = DurableState.dirtyPreview();

        try {
            PreviewResetService.reset(new SimulatedOperations(durable, Fault.AFTER_PLAYER_SAVE));
        } catch (SimulatedCrash expected) {
            // The player baseline is durable while the reset marker remains recovery authority.
        }

        assertTrue(durable.resetIntentPresent);
        assertTrue(durable.playerCleared);

        PreviewResetService.reset(new SimulatedOperations(durable, Fault.NONE));

        assertConverged(durable);
    }

    @Test
    void retainedTechnicalExitUsesProductionReconciliationBeforeFinalConvergence() {
        DurableState durable = DurableState.dirtyPreview();
        durable.resetIntentPresent = true;
        durable.technicalExitPending = true;

        SimulatedOperations operations = new SimulatedOperations(durable, Fault.NONE);
        PreviewResetService.reset(operations);

        assertConverged(durable);
        assertFalse(durable.technicalExitPending);
        assertTrue(operations.technicalExitResumeCalled);
        assertFalse(operations.previewAbortCalled);
    }

    @Test
    void productionReconciliationFallsBackToPreviewAbortWithoutTechnicalExit() {
        ReconciliationProbe probe = new ReconciliationProbe(false, true);

        PreviewResetService.reconcileNightmareForReset(probe);

        assertTrue(probe.resumeCalled);
        assertTrue(probe.activeCheckCalled);
        assertTrue(probe.abortCalled);
    }

    @Test
    void productionReconciliationStopsAfterTechnicalExitResume() {
        ReconciliationProbe probe = new ReconciliationProbe(true, true);

        PreviewResetService.reconcileNightmareForReset(probe);

        assertTrue(probe.resumeCalled);
        assertFalse(probe.activeCheckCalled);
        assertFalse(probe.abortCalled);
    }

    private static void assertConverged(DurableState durable) {
        assertFalse(durable.resetIntentPresent);
        assertFalse(durable.technicalExitPending);
        assertFalse(durable.nightmareActive);
        assertFalse(durable.successfulCompletionPresent);
        assertTrue(durable.playerCleared);
    }

    private enum Fault {
        NONE,
        AFTER_FIRST_REGISTRY_SAVE,
        AFTER_PLAYER_SAVE
    }

    private static final class SimulatedCrash extends RuntimeException {
    }

    /**
     * Minimal process-free durability model for the reset transaction.
     * Registry changes become restart-visible only at persistRegistry(); player changes only at persistPlayer().
     */
    private static final class DurableState {
        private boolean resetIntentPresent;
        private boolean technicalExitPending;
        private boolean nightmareActive;
        private boolean successfulCompletionPresent;
        private boolean playerCleared;

        private static DurableState dirtyPreview() {
            DurableState state = new DurableState();
            state.nightmareActive = true;
            state.successfulCompletionPresent = true;
            return state;
        }
    }

    private static final class SimulatedOperations implements PreviewResetService.Operations {
        private final DurableState durable;
        private final Fault fault;
        private boolean workingResetIntent;
        private boolean workingTechnicalExit;
        private boolean workingNightmareActive;
        private boolean workingSuccessfulCompletion;
        private boolean workingPlayerCleared;
        private boolean technicalExitResumeCalled;
        private boolean previewAbortCalled;
        private int registrySaveCount;

        private SimulatedOperations(DurableState durable, Fault fault) {
            this.durable = durable;
            this.fault = fault;
            this.workingResetIntent = durable.resetIntentPresent;
            this.workingTechnicalExit = durable.technicalExitPending;
            this.workingNightmareActive = durable.nightmareActive;
            this.workingSuccessfulCompletion = durable.successfulCompletionPresent;
            this.workingPlayerCleared = durable.playerCleared;
        }

        @Override
        public void beginResetIntent() {
            workingResetIntent = true;
        }

        @Override
        public void persistRegistry() {
            durable.resetIntentPresent = workingResetIntent;
            durable.technicalExitPending = workingTechnicalExit;
            durable.nightmareActive = workingNightmareActive;
            durable.successfulCompletionPresent = workingSuccessfulCompletion;
            registrySaveCount++;
            if (fault == Fault.AFTER_FIRST_REGISTRY_SAVE && registrySaveCount == 1) {
                throw new SimulatedCrash();
            }
        }

        @Override
        public void verifyResetIntentDurable() {
            if (!durable.resetIntentPresent) {
                throw new IllegalStateException("simulated persisted reset marker is absent");
            }
        }

        @Override
        public void abortNightmareIfActive() {
            PreviewResetService.reconcileNightmareForReset(new PreviewResetService.NightmareResetOperations() {
                @Override
                public boolean resumeTechnicalExit() {
                    technicalExitResumeCalled = true;
                    if (!workingTechnicalExit) {
                        return false;
                    }
                    workingTechnicalExit = false;
                    workingNightmareActive = false;
                    return true;
                }

                @Override
                public boolean activeNightmarePresent() {
                    return workingNightmareActive;
                }

                @Override
                public void abortForPreviewReset() {
                    previewAbortCalled = true;
                    workingNightmareActive = false;
                }
            });
        }

        @Override
        public void clearSuccessfulCompletion() {
            workingSuccessfulCompletion = false;
        }

        @Override
        public SoulData resetSoulWithoutSync() {
            workingPlayerCleared = true;
            return SoulData.uninfected();
        }

        @Override
        public void clearSoulIdentity() {
            workingPlayerCleared = true;
        }

        @Override
        public void clearImportedIdentity() {
            workingPlayerCleared = true;
        }

        @Override
        public void clearPreviewPower() {
            workingPlayerCleared = true;
        }

        @Override
        public void persistPlayer() {
            durable.playerCleared = workingPlayerCleared;
            if (fault == Fault.AFTER_PLAYER_SAVE) {
                throw new SimulatedCrash();
            }
        }

        @Override
        public void sync(SoulData resetSoul) {
            // Client presentation is intentionally non-durable in this process-free model.
        }

        @Override
        public void completeResetIntent() {
            workingResetIntent = false;
        }
    }

    private static final class ReconciliationProbe implements PreviewResetService.NightmareResetOperations {
        private final boolean technicalExitResumed;
        private final boolean activeNightmare;
        private boolean resumeCalled;
        private boolean activeCheckCalled;
        private boolean abortCalled;

        private ReconciliationProbe(boolean technicalExitResumed, boolean activeNightmare) {
            this.technicalExitResumed = technicalExitResumed;
            this.activeNightmare = activeNightmare;
        }

        @Override
        public boolean resumeTechnicalExit() {
            resumeCalled = true;
            return technicalExitResumed;
        }

        @Override
        public boolean activeNightmarePresent() {
            activeCheckCalled = true;
            return activeNightmare;
        }

        @Override
        public void abortForPreviewReset() {
            abortCalled = true;
        }
    }
}
