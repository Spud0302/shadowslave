package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareDeathCoordinatorTest {
    @Test
    void restartAfterEveryDurableBoundaryConvergesToDeathOutcome() {
        for (Checkpoint checkpoint : Checkpoint.values()) {
            FakeOperations operations = new FakeOperations(checkpoint);
            runToCompletionAcrossCrash(operations);

            assertFalse(operations.durableDeathIntentPresent);
            assertFalse(operations.durableCompletionReceiptPresent);
            assertTrue(operations.durablePlayerReset);
            assertFalse(operations.durableActiveOwnershipPresent);
        }
    }

    @Test
    void ownershipIsNotConsumedBeforeIntentAndPlayerResetAreDurable() {
        FakeOperations operations = new FakeOperations(null);

        NightmareDeathCoordinator.commit(operations);

        assertTrue(operations.intentWasPresentWhenOwnershipRemoved);
        assertTrue(operations.playerWasResetWhenOwnershipRemoved);
        assertFalse(operations.receiptWasPresentWhenOwnershipRemoved);
    }

    @Test
    void initialIntentPersistenceFailureRetainsAuthorityAndStopsBeforeLaterMutation() {
        IntentFailureOperations operations = new IntentFailureOperations(true, false);

        RuntimeException failure = assertThrows(RuntimeException.class, () -> NightmareDeathCoordinator.commit(operations));

        assertSame(operations.persistenceFailure, failure);
        assertTrue(operations.baselineCaptured);
        assertTrue(operations.intentRecorded);
        assertTrue(operations.intentPresent);
        assertFalse(operations.intentMarkedDurable);
        assertFalse(operations.verificationAttempted);
        assertFalse(operations.laterMutationReached);
    }

    @Test
    void initialIntentVerificationFailureRetainsAuthorityAndStopsBeforeLaterMutation() {
        IntentFailureOperations operations = new IntentFailureOperations(false, true);

        RuntimeException failure = assertThrows(RuntimeException.class, () -> NightmareDeathCoordinator.commit(operations));

        assertSame(operations.verificationFailure, failure);
        assertTrue(operations.baselineCaptured);
        assertTrue(operations.intentRecorded);
        assertTrue(operations.intentPresent);
        assertTrue(operations.verificationAttempted);
        assertFalse(operations.intentMarkedDurable);
        assertFalse(operations.laterMutationReached);
    }

    @Test
    void successfulInitialVerificationPromotesAuthorityBeforeLaterMutation() {
        IntentFailureOperations operations = new IntentFailureOperations(false, false);

        NightmareDeathCoordinator.commit(operations);

        assertTrue(operations.intentMarkedDurable);
        assertTrue(operations.laterMutationReached);
    }

    @Test
    void alreadyDurableIntentReplaysWithoutDemandingAnotherInitialFileChange() {
        IntentFailureOperations operations = new IntentFailureOperations(false, false);
        operations.alreadyDurable = true;
        operations.intentPresent = true;

        NightmareDeathCoordinator.commit(operations);

        assertFalse(operations.baselineCaptured);
        assertFalse(operations.intentRecorded);
        assertFalse(operations.verificationAttempted);
        assertFalse(operations.intentMarkedDurable);
        assertTrue(operations.laterMutationReached);
    }

    private static void runToCompletionAcrossCrash(FakeOperations operations) {
        boolean crashed;
        do {
            crashed = false;
            try {
                NightmareDeathCoordinator.commit(operations);
            } catch (SimulatedCrash expected) {
                crashed = true;
                operations.restartFromDurableState();
            }
        } while (crashed);
    }

    private enum Checkpoint {
        AFTER_INTENT_SAVE,
        AFTER_RECEIPT_CLEAR_SAVE,
        AFTER_PLAYER_SAVE,
        AFTER_TEARDOWN_SAVE
    }

    private static final class SimulatedCrash extends RuntimeException {
    }

    private static final class FakeOperations implements NightmareDeathCoordinator.Operations {
        private final Checkpoint crashCheckpoint;
        private boolean crashTriggered;

        private boolean durableDeathIntentPresent;
        private boolean durableCompletionReceiptPresent = true;
        private boolean durablePlayerReset;
        private boolean durableActiveOwnershipPresent = true;

        private boolean volatileDeathIntentPresent;
        private boolean volatileCompletionReceiptPresent;
        private boolean volatilePlayerReset;
        private boolean volatileActiveOwnershipPresent;

        private boolean intentWasPresentWhenOwnershipRemoved;
        private boolean playerWasResetWhenOwnershipRemoved;
        private boolean receiptWasPresentWhenOwnershipRemoved;
        private int deathPersistCount;
        private int registryPersistCount;

        private FakeOperations(Checkpoint crashCheckpoint) {
            this.crashCheckpoint = crashCheckpoint;
            restartFromDurableState();
        }

        private void restartFromDurableState() {
            volatileDeathIntentPresent = durableDeathIntentPresent;
            volatileCompletionReceiptPresent = durableCompletionReceiptPresent;
            volatilePlayerReset = durablePlayerReset;
            volatileActiveOwnershipPresent = durableActiveOwnershipPresent;
            deathPersistCount = 0;
            registryPersistCount = 0;
        }

        @Override
        public boolean deathIntentAlreadyDurable() {
            return durableDeathIntentPresent;
        }

        @Override
        public void captureDeathIntentBaseline() {
        }

        @Override
        public void recordDeathIntent() {
            volatileDeathIntentPresent = true;
        }

        @Override
        public void persistDeathIntent() {
            durableDeathIntentPresent = volatileDeathIntentPresent;
            deathPersistCount++;
            if (!crashTriggered && crashCheckpoint == Checkpoint.AFTER_INTENT_SAVE && deathPersistCount == 1) {
                crashTriggered = true;
                throw new SimulatedCrash();
            }
        }

        @Override
        public void verifyDeathIntentPersisted() {
        }

        @Override
        public void markDeathIntentDurable() {
        }

        @Override
        public void clearCompletionReceipt() {
            volatileCompletionReceiptPresent = false;
        }

        @Override
        public void persistNightmareRegistry() {
            durableCompletionReceiptPresent = volatileCompletionReceiptPresent;
            durableActiveOwnershipPresent = volatileActiveOwnershipPresent;
            registryPersistCount++;
            if (!crashTriggered && crashCheckpoint == Checkpoint.AFTER_RECEIPT_CLEAR_SAVE && registryPersistCount == 1) {
                crashTriggered = true;
                throw new SimulatedCrash();
            }
            if (!crashTriggered && crashCheckpoint == Checkpoint.AFTER_TEARDOWN_SAVE && registryPersistCount == 2) {
                crashTriggered = true;
                throw new SimulatedCrash();
            }
        }

        @Override
        public void resetPlayerState() {
            volatilePlayerReset = true;
        }

        @Override
        public void persistPlayer() {
            durablePlayerReset = volatilePlayerReset;
            if (!crashTriggered && crashCheckpoint == Checkpoint.AFTER_PLAYER_SAVE) {
                crashTriggered = true;
                throw new SimulatedCrash();
            }
        }

        @Override
        public void teardownActiveInstance() {
            intentWasPresentWhenOwnershipRemoved = volatileDeathIntentPresent;
            playerWasResetWhenOwnershipRemoved = volatilePlayerReset;
            receiptWasPresentWhenOwnershipRemoved = volatileCompletionReceiptPresent;
            volatileActiveOwnershipPresent = false;
        }

        @Override
        public void clearDeathIntent() {
            volatileDeathIntentPresent = false;
        }
    }

    private static final class IntentFailureOperations implements NightmareDeathCoordinator.Operations {
        private final boolean failPersistence;
        private final boolean failVerification;
        private final RuntimeException persistenceFailure = new IllegalStateException("persist failed");
        private final RuntimeException verificationFailure = new IllegalStateException("verify failed");
        private boolean alreadyDurable;
        private boolean baselineCaptured;
        private boolean intentRecorded;
        private boolean intentPresent;
        private boolean verificationAttempted;
        private boolean intentMarkedDurable;
        private boolean laterMutationReached;

        private IntentFailureOperations(boolean failPersistence, boolean failVerification) {
            this.failPersistence = failPersistence;
            this.failVerification = failVerification;
        }

        @Override
        public boolean deathIntentAlreadyDurable() {
            return alreadyDurable;
        }

        @Override
        public void captureDeathIntentBaseline() {
            baselineCaptured = true;
        }

        @Override
        public void recordDeathIntent() {
            intentRecorded = true;
            intentPresent = true;
        }

        @Override
        public void persistDeathIntent() {
            if (failPersistence) {
                throw persistenceFailure;
            }
        }

        @Override
        public void verifyDeathIntentPersisted() {
            verificationAttempted = true;
            if (failVerification) {
                throw verificationFailure;
            }
        }

        @Override
        public void markDeathIntentDurable() {
            intentMarkedDurable = true;
            alreadyDurable = true;
        }

        @Override
        public void clearCompletionReceipt() {
            laterMutationReached = true;
        }

        @Override
        public void persistNightmareRegistry() {
        }

        @Override
        public void resetPlayerState() {
        }

        @Override
        public void persistPlayer() {
        }

        @Override
        public void teardownActiveInstance() {
        }

        @Override
        public void clearDeathIntent() {
            intentPresent = false;
        }
    }
}
