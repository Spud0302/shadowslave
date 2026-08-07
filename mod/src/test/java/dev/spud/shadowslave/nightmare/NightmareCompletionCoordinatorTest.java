package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareCompletionCoordinatorTest {
    @Test
    void restartAfterEveryDurableBoundaryCommitsEachActionOnce() {
        for (Checkpoint checkpoint : Checkpoint.values()) {
            FakeOperations operations = new FakeOperations(checkpoint);

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
            assertEquals(1, operations.durableAppraisalExecutions,
                    "appraisal must have one committed application after " + checkpoint);
            assertEquals(1, operations.durableReturnExecutions,
                    "return must have one committed application after " + checkpoint);
            assertEquals(1, operations.durableTeardownExecutions,
                    "teardown must have one committed application after " + checkpoint);
        }
    }

    @Test
    void cancelledReturnDoesNotCommitReturnOrTeardown() {
        FakeOperations operations = new FakeOperations(null);
        operations.returnMovesPlayer = false;

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> NightmareCompletionCoordinator.resume(operations)
        );

        assertEquals(
                "Successful Nightmare return did not move the player out of the Nightmare dimension",
                failure.getMessage()
        );
        assertEquals(NightmareCompletionPhase.APPRAISAL_COMMITTED, operations.volatilePhase);
        assertTrue(operations.volatileAppraisalApplied);
        assertTrue(operations.volatilePlayerInNightmare);
        assertTrue(operations.volatileActiveOwnershipPresent);
        assertEquals(1, operations.volatileReturnExecutions);
        assertEquals(0, operations.volatileTeardownExecutions);
        assertEquals(2, operations.persistCalls,
                "cancelled return must fail before the return player save or RETURN_COMMITTED registry save");
    }

    @Test
    void replayOfFullyCommittedReceiptDoesNothing() {
        FakeOperations operations = FakeOperations.completed();

        NightmareCompletionCoordinator.resume(operations);

        assertEquals(0, operations.volatileAppraisalExecutions);
        assertEquals(0, operations.volatileReturnExecutions);
        assertEquals(0, operations.volatileTeardownExecutions);
        assertEquals(0, operations.persistCalls);
    }

    private enum Checkpoint {
        APPRAISAL_PLAYER_PERSISTED,
        APPRAISAL_PHASE_PERSISTED,
        RETURN_PLAYER_PERSISTED,
        RETURN_PHASE_PERSISTED,
        TEARDOWN_PHASE_PERSISTED
    }

    private static final class SimulatedCrash extends RuntimeException {
    }

    private static final class FakeOperations implements NightmareCompletionCoordinator.Operations {
        private final Checkpoint crashCheckpoint;
        private boolean crashTriggered;
        private boolean returnMovesPlayer = true;
        private int persistCalls;

        private NightmareCompletionPhase durablePhase;
        private boolean durableAppraisalApplied;
        private boolean durablePlayerInNightmare;
        private boolean durableActiveOwnershipPresent;
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

        private FakeOperations(Checkpoint crashCheckpoint) {
            this.crashCheckpoint = crashCheckpoint;
            durablePhase = NightmareCompletionPhase.TERMINAL_RESOLUTION_RECORDED;
            durablePlayerInNightmare = true;
            durableActiveOwnershipPresent = true;
            restartFromDurableState();
        }

        private static FakeOperations completed() {
            FakeOperations operations = new FakeOperations(null);
            operations.durablePhase = NightmareCompletionPhase.TEARDOWN_COMMITTED;
            operations.durableAppraisalApplied = true;
            operations.durablePlayerInNightmare = false;
            operations.durableActiveOwnershipPresent = false;
            operations.restartFromDurableState();
            operations.volatileAppraisalExecutions = 0;
            operations.volatileReturnExecutions = 0;
            operations.volatileTeardownExecutions = 0;
            return operations;
        }

        private void restartFromDurableState() {
            volatilePhase = durablePhase;
            volatileAppraisalApplied = durableAppraisalApplied;
            volatilePlayerInNightmare = durablePlayerInNightmare;
            volatileActiveOwnershipPresent = durableActiveOwnershipPresent;
            volatileAppraisalExecutions = durableAppraisalExecutions;
            volatileReturnExecutions = durableReturnExecutions;
            volatileTeardownExecutions = durableTeardownExecutions;
        }

        @Override
        public NightmareCompletionPhase phase() {
            return volatilePhase;
        }

        @Override
        public boolean appraisalApplied() {
            return volatileAppraisalApplied;
        }

        @Override
        public boolean playerInNightmare() {
            return volatilePlayerInNightmare;
        }

        @Override
        public boolean activeOwnershipPresent() {
            return volatileActiveOwnershipPresent;
        }

        @Override
        public void applyAppraisal() {
            volatileAppraisalApplied = true;
            volatileAppraisalExecutions++;
        }

        @Override
        public void returnPlayer() {
            if (returnMovesPlayer) {
                volatilePlayerInNightmare = false;
            }
            volatileReturnExecutions++;
        }

        @Override
        public void teardownActiveInstance() {
            volatileActiveOwnershipPresent = false;
            volatileTeardownExecutions++;
        }

        @Override
        public void advancePhase(NightmareCompletionPhase target) {
            if (target.ordinal() <= volatilePhase.ordinal()) {
                return;
            }
            if (target.ordinal() != volatilePhase.ordinal() + 1) {
                throw new IllegalStateException("test operation attempted to skip a completion phase");
            }
            volatilePhase = target;
        }

        @Override
        public void persistPlayer() {
            durableAppraisalApplied = volatileAppraisalApplied;
            durablePlayerInNightmare = volatilePlayerInNightmare;
            durableAppraisalExecutions = volatileAppraisalExecutions;
            durableReturnExecutions = volatileReturnExecutions;
            persistCalls++;

            Checkpoint reached = durablePlayerInNightmare
                    ? Checkpoint.APPRAISAL_PLAYER_PERSISTED
                    : Checkpoint.RETURN_PLAYER_PERSISTED;
            failOnceAfter(reached);
        }

        @Override
        public void persistRegistry() {
            durablePhase = volatilePhase;
            durableActiveOwnershipPresent = volatileActiveOwnershipPresent;
            durableTeardownExecutions = volatileTeardownExecutions;
            persistCalls++;

            Checkpoint reached = switch (durablePhase) {
                case APPRAISAL_COMMITTED -> Checkpoint.APPRAISAL_PHASE_PERSISTED;
                case RETURN_COMMITTED -> Checkpoint.RETURN_PHASE_PERSISTED;
                case TEARDOWN_COMMITTED -> Checkpoint.TEARDOWN_PHASE_PERSISTED;
                case TERMINAL_RESOLUTION_RECORDED -> throw new IllegalStateException(
                        "coordinator must not persist the already-durable terminal receipt"
                );
            };
            failOnceAfter(reached);
        }

        private void failOnceAfter(Checkpoint reached) {
            if (!crashTriggered && reached == crashCheckpoint) {
                crashTriggered = true;
                throw new SimulatedCrash();
            }
        }
    }
}
