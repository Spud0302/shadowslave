package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareCompletionCoordinatorTest {
    @Test
    void restartAfterEachDurableBoundaryConvergesExactlyOnce() {
        for (NightmareCompletionFaultPoint point : NightmareCompletionFaultPoint.values()) {
            FakeOperations operations = new FakeOperations(point);
            boolean crashed;
            do {
                crashed = false;
                try {
                    NightmareCompletionCoordinator.resume(operations);
                } catch (SimulatedCrash expected) {
                    crashed = true;
                    operations.restartFromDurableState();
                }
            } while (crashed);

            assertEquals(NightmareCompletionPhase.TEARDOWN_COMMITTED, operations.durablePhase);
            assertTrue(operations.durableAppraisalApplied);
            assertFalse(operations.durablePlayerInNightmare);
            assertFalse(operations.durableActiveOwnershipPresent);
            assertEquals(1, operations.durableAppraisalExecutions);
            assertEquals(1, operations.durableReturnExecutions);
            assertEquals(1, operations.durableTeardownExecutions);
        }
    }

    @Test
    void replayOfFullyCommittedReceiptDoesNothing() {
        FakeOperations operations = FakeOperations.completed();
        NightmareCompletionCoordinator.resume(operations);
        assertEquals(0, operations.persistCalls);
        assertEquals(0, operations.volatileAppraisalExecutions);
        assertEquals(0, operations.volatileReturnExecutions);
        assertEquals(0, operations.volatileTeardownExecutions);
    }

    private static final class SimulatedCrash extends RuntimeException {}

    private static final class FakeOperations implements NightmareCompletionCoordinator.Operations {
        private final NightmareCompletionFaultPoint crashPoint;
        private boolean crashTriggered;
        private int persistCalls;
        private NightmareCompletionPhase durablePhase = NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED;
        private boolean durableAppraisalApplied;
        private boolean durablePlayerInNightmare = true;
        private boolean durableActiveOwnershipPresent = true;
        private int durableAppraisalExecutions;
        private int durableReturnExecutions;
        private int durableTeardownExecutions;
        private NightmareCompletionPhase volatilePhase;
        private boolean volatileAppraisalApplied;
        private boolean volatilePlayerInNightmare;
        private boolean volatileActiveOwnershipPresent;
        private int volatileAppraisalExecutions;
        private int volatileReturnExecutions;
        private int volatileTeardownExecutions;

        FakeOperations(NightmareCompletionFaultPoint crashPoint) {
            this.crashPoint = crashPoint;
            restartFromDurableState();
        }

        static FakeOperations completed() {
            FakeOperations operations = new FakeOperations(null);
            operations.durablePhase = NightmareCompletionPhase.TEARDOWN_COMMITTED;
            operations.durableAppraisalApplied = true;
            operations.durablePlayerInNightmare = false;
            operations.durableActiveOwnershipPresent = false;
            operations.restartFromDurableState();
            return operations;
        }

        void restartFromDurableState() {
            volatilePhase = durablePhase;
            volatileAppraisalApplied = durableAppraisalApplied;
            volatilePlayerInNightmare = durablePlayerInNightmare;
            volatileActiveOwnershipPresent = durableActiveOwnershipPresent;
            volatileAppraisalExecutions = durableAppraisalExecutions;
            volatileReturnExecutions = durableReturnExecutions;
            volatileTeardownExecutions = durableTeardownExecutions;
        }

        public NightmareCompletionPhase phase() { return volatilePhase; }
        public boolean appraisalApplied() { return volatileAppraisalApplied; }
        public boolean playerInNightmare() { return volatilePlayerInNightmare; }
        public boolean activeOwnershipPresent() { return volatileActiveOwnershipPresent; }
        public void applyAppraisal() { volatileAppraisalApplied = true; volatileAppraisalExecutions++; }
        public void returnPlayer() { volatilePlayerInNightmare = false; volatileReturnExecutions++; }
        public void teardownActiveInstance() { volatileActiveOwnershipPresent = false; volatileTeardownExecutions++; }
        public void advancePhase(NightmareCompletionPhase target) {
            if (target.ordinal() <= volatilePhase.ordinal()) return;
            if (target.ordinal() != volatilePhase.ordinal() + 1) throw new IllegalStateException("phase skip");
            volatilePhase = target;
        }
        public void persistPlayer() {
            durableAppraisalApplied = volatileAppraisalApplied;
            durablePlayerInNightmare = volatilePlayerInNightmare;
            durableAppraisalExecutions = volatileAppraisalExecutions;
            durableReturnExecutions = volatileReturnExecutions;
            persistCalls++;
        }
        public void persistRegistry() {
            durablePhase = volatilePhase;
            durableActiveOwnershipPresent = volatileActiveOwnershipPresent;
            durableTeardownExecutions = volatileTeardownExecutions;
            persistCalls++;
        }
        public void afterDurableBoundary(NightmareCompletionFaultPoint point) {
            if (!crashTriggered && point == crashPoint) {
                crashTriggered = true;
                throw new SimulatedCrash();
            }
        }
    }
}
