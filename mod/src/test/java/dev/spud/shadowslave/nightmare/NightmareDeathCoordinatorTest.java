package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
