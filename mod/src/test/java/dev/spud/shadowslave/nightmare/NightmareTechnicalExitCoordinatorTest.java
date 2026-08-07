package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareTechnicalExitCoordinatorTest {
    @Test
    void restartAfterReceiptClearReplaysFromActiveOwnership() {
        FakeOperations operations = new FakeOperations(Checkpoint.AFTER_RECEIPT_CLEAR_SAVE);

        runToCompletionAcrossCrash(operations);

        assertFalse(operations.durableCompletionReceiptPresent);
        assertTrue(operations.durablePlayerReset);
        assertFalse(operations.durableActiveOwnershipPresent);
    }

    @Test
    void restartAfterPlayerResetReplaysTeardownFromActiveOwnership() {
        FakeOperations operations = new FakeOperations(Checkpoint.AFTER_PLAYER_SAVE);

        runToCompletionAcrossCrash(operations);

        assertFalse(operations.durableCompletionReceiptPresent);
        assertTrue(operations.durablePlayerReset);
        assertFalse(operations.durableActiveOwnershipPresent);
    }

    @Test
    void durableOrderNeverConsumesOwnershipBeforePlayerReset() {
        FakeOperations operations = new FakeOperations(null);

        NightmareTechnicalExitCoordinator.commit(operations);

        assertTrue(operations.playerWasResetWhenOwnershipRemoved);
        assertFalse(operations.receiptWasPresentWhenOwnershipRemoved);
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
        AFTER_RECEIPT_CLEAR_SAVE,
        AFTER_PLAYER_SAVE
    }

    private static final class SimulatedCrash extends RuntimeException {
    }

    private static final class FakeOperations implements NightmareTechnicalExitCoordinator.Operations {
        private final Checkpoint crashCheckpoint;
        private boolean crashTriggered;

        private boolean durableCompletionReceiptPresent = true;
        private boolean durablePlayerReset;
        private boolean durableActiveOwnershipPresent = true;

        private boolean volatileCompletionReceiptPresent;
        private boolean volatilePlayerReset;
        private boolean volatileActiveOwnershipPresent;

        private boolean playerWasResetWhenOwnershipRemoved;
        private boolean receiptWasPresentWhenOwnershipRemoved;
        private int registryPersistCount;

        private FakeOperations(Checkpoint crashCheckpoint) {
            this.crashCheckpoint = crashCheckpoint;
            restartFromDurableState();
        }

        private void restartFromDurableState() {
            volatileCompletionReceiptPresent = durableCompletionReceiptPresent;
            volatilePlayerReset = durablePlayerReset;
            volatileActiveOwnershipPresent = durableActiveOwnershipPresent;
        }

        @Override
        public void clearCompletionReceipt() {
            volatileCompletionReceiptPresent = false;
        }

        @Override
        public void persistRegistry() {
            durableCompletionReceiptPresent = volatileCompletionReceiptPresent;
            durableActiveOwnershipPresent = volatileActiveOwnershipPresent;
            registryPersistCount++;
            if (!crashTriggered
                    && crashCheckpoint == Checkpoint.AFTER_RECEIPT_CLEAR_SAVE
                    && registryPersistCount == 1) {
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
            playerWasResetWhenOwnershipRemoved = volatilePlayerReset;
            receiptWasPresentWhenOwnershipRemoved = volatileCompletionReceiptPresent;
            volatileActiveOwnershipPresent = false;
        }
    }
}
