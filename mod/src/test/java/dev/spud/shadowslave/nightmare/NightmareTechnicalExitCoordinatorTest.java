package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareTechnicalExitCoordinatorTest {
    @Test
    void restartAfterIntentSaveReplaysTechnicalExit() {
        FakeOperations operations = new FakeOperations(Checkpoint.AFTER_INTENT_SAVE);

        runToCompletionAcrossCrash(operations);

        assertFalse(operations.durableTechnicalExitIntentPresent);
        assertFalse(operations.durableCompletionReceiptPresent);
        assertTrue(operations.durablePlayerReset);
        assertFalse(operations.durableActiveOwnershipPresent);
    }

    @Test
    void restartAfterReceiptClearReplaysFromDurableTechnicalIntent() {
        FakeOperations operations = new FakeOperations(Checkpoint.AFTER_RECEIPT_CLEAR_SAVE);

        runToCompletionAcrossCrash(operations);

        assertFalse(operations.durableTechnicalExitIntentPresent);
        assertFalse(operations.durableCompletionReceiptPresent);
        assertTrue(operations.durablePlayerReset);
        assertFalse(operations.durableActiveOwnershipPresent);
    }

    @Test
    void restartAfterPlayerResetReplaysTeardownFromDurableTechnicalIntent() {
        FakeOperations operations = new FakeOperations(Checkpoint.AFTER_PLAYER_SAVE);

        runToCompletionAcrossCrash(operations);

        assertFalse(operations.durableTechnicalExitIntentPresent);
        assertFalse(operations.durableCompletionReceiptPresent);
        assertTrue(operations.durablePlayerReset);
        assertFalse(operations.durableActiveOwnershipPresent);
    }

    @Test
    void durableOrderNeverConsumesOwnershipBeforeIntentReceiptClearAndPlayerReset() {
        FakeOperations operations = new FakeOperations(null);

        NightmareTechnicalExitCoordinator.commit(operations);

        assertTrue(operations.intentWasVerifiedBeforeReceiptClear);
        assertTrue(operations.intentWasPresentWhenOwnershipRemoved);
        assertTrue(operations.playerWasResetWhenOwnershipRemoved);
        assertFalse(operations.receiptWasPresentWhenOwnershipRemoved);
        assertFalse(operations.durableTechnicalExitIntentPresent);
    }

    @Test
    void failedIntentVerificationStopsBeforeReceiptPlayerOrOwnershipMutation() {
        FakeOperations operations = new FakeOperations(null);
        operations.failIntentVerification = true;

        assertThrows(IllegalStateException.class, () -> NightmareTechnicalExitCoordinator.commit(operations));

        assertTrue(operations.volatileTechnicalExitIntentPresent);
        assertTrue(operations.volatileCompletionReceiptPresent);
        assertFalse(operations.volatilePlayerReset);
        assertTrue(operations.volatileActiveOwnershipPresent);
    }

    private static void runToCompletionAcrossCrash(FakeOperations operations) {
        boolean crashed;
        do {
            crashed = false;
            try {
                NightmareTechnicalExitCoordinator.commit(operations);
            } catch (SimulatedCrash expected) {
                crashed = true;
                operations.restartFromDurableState();
            }
        } while (crashed);
    }

    private enum Checkpoint {
        AFTER_INTENT_SAVE,
        AFTER_RECEIPT_CLEAR_SAVE,
        AFTER_PLAYER_SAVE
    }

    private static final class SimulatedCrash extends RuntimeException {
    }

    private static final class FakeOperations implements NightmareTechnicalExitCoordinator.Operations {
        private final Checkpoint crashCheckpoint;
        private boolean crashTriggered;
        private boolean failIntentVerification;

        private boolean durableTechnicalExitIntentPresent;
        private boolean durableCompletionReceiptPresent = true;
        private boolean durablePlayerReset;
        private boolean durableActiveOwnershipPresent = true;

        private boolean volatileTechnicalExitIntentPresent;
        private boolean volatileCompletionReceiptPresent;
        private boolean volatilePlayerReset;
        private boolean volatileActiveOwnershipPresent;

        private boolean intentWasVerified;
        private boolean intentWasVerifiedBeforeReceiptClear;
        private boolean intentWasPresentWhenOwnershipRemoved;
        private boolean playerWasResetWhenOwnershipRemoved;
        private boolean receiptWasPresentWhenOwnershipRemoved;
        private int registryPersistCount;

        private FakeOperations(Checkpoint crashCheckpoint) {
            this.crashCheckpoint = crashCheckpoint;
            restartFromDurableState();
        }

        private void restartFromDurableState() {
            volatileTechnicalExitIntentPresent = durableTechnicalExitIntentPresent;
            volatileCompletionReceiptPresent = durableCompletionReceiptPresent;
            volatilePlayerReset = durablePlayerReset;
            volatileActiveOwnershipPresent = durableActiveOwnershipPresent;
            intentWasVerified = durableTechnicalExitIntentPresent;
            registryPersistCount = 0;
        }

        @Override
        public void recordTechnicalExitIntent() {
            volatileTechnicalExitIntentPresent = true;
        }

        @Override
        public void verifyTechnicalExitIntentDurable() {
            if (failIntentVerification) {
                throw new IllegalStateException("simulated technical-exit persistence verification failure");
            }
            if (!durableTechnicalExitIntentPresent) {
                throw new IllegalStateException("technical-exit intent was not durable");
            }
            intentWasVerified = true;
        }

        @Override
        public void clearCompletionReceipt() {
            intentWasVerifiedBeforeReceiptClear = intentWasVerified;
            volatileCompletionReceiptPresent = false;
        }

        @Override
        public void persistRegistry() {
            durableTechnicalExitIntentPresent = volatileTechnicalExitIntentPresent;
            durableCompletionReceiptPresent = volatileCompletionReceiptPresent;
            durableActiveOwnershipPresent = volatileActiveOwnershipPresent;
            registryPersistCount++;
            if (!crashTriggered
                    && crashCheckpoint == Checkpoint.AFTER_INTENT_SAVE
                    && registryPersistCount == 1) {
                crashTriggered = true;
                throw new SimulatedCrash();
            }
            if (!crashTriggered
                    && crashCheckpoint == Checkpoint.AFTER_RECEIPT_CLEAR_SAVE
                    && registryPersistCount == 2) {
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
            intentWasPresentWhenOwnershipRemoved = volatileTechnicalExitIntentPresent;
            playerWasResetWhenOwnershipRemoved = volatilePlayerReset;
            receiptWasPresentWhenOwnershipRemoved = volatileCompletionReceiptPresent;
            volatileActiveOwnershipPresent = false;
            volatileTechnicalExitIntentPresent = false;
        }
    }
}
