package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NightmareSuccessfulCompletionActivationTest {
    @Test
    void durableTerminalReceiptPrecedesWorldPresentationAndRecovery() {
        FakeOperations operations = new FakeOperations();

        NightmareSuccessfulCompletionActivation.run(operations);

        assertEquals(
                List.of("validate", "capture", "record", "persist", "verify", "boundary", "world", "resume"),
                operations.calls
        );
    }

    @Test
    void terminalFaultStopsBeforeReplayableWorldPresentation() {
        FakeOperations operations = new FakeOperations();
        operations.failAtBoundary = true;

        assertThrows(SimulatedFault.class, () -> NightmareSuccessfulCompletionActivation.run(operations));

        assertEquals(
                List.of("validate", "capture", "record", "persist", "verify", "boundary"),
                operations.calls
        );
    }

    @Test
    void failedReceiptVerificationQuarantinesReceiptBeforeReturningFailure() {
        FakeOperations operations = new FakeOperations();
        RuntimeException failure = new RuntimeException("completion receipt did not reach disk");
        operations.verificationFailure = failure;

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> NightmareSuccessfulCompletionActivation.run(operations)
        );

        assertSame(failure, thrown);
        assertEquals(
                List.of("validate", "capture", "record", "persist", "verify", "discard"),
                operations.calls
        );
    }

    @Test
    void failedReceiptPersistenceQuarantinesReceiptBeforeReturningFailure() {
        FakeOperations operations = new FakeOperations();
        RuntimeException failure = new RuntimeException("registry save failed");
        operations.persistenceFailure = failure;

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> NightmareSuccessfulCompletionActivation.run(operations)
        );

        assertSame(failure, thrown);
        assertEquals(
                List.of("validate", "capture", "record", "persist", "discard"),
                operations.calls
        );
    }

    @Test
    void quarantineFailureIsSuppressedWithoutReplacingPersistenceFailure() {
        FakeOperations operations = new FakeOperations();
        RuntimeException persistenceFailure = new RuntimeException("registry save failed");
        RuntimeException discardFailure = new RuntimeException("receipt quarantine failed");
        operations.persistenceFailure = persistenceFailure;
        operations.discardFailure = discardFailure;

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> NightmareSuccessfulCompletionActivation.run(operations)
        );

        assertSame(persistenceFailure, thrown);
        assertEquals(List.of(discardFailure), List.of(thrown.getSuppressed()));
        assertEquals(
                List.of("validate", "capture", "record", "persist", "discard"),
                operations.calls
        );
    }

    @Test
    void missingReceiptAfterWorldPresentationFailsClosed() {
        FakeOperations operations = new FakeOperations();
        operations.resumeResult = false;

        IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> NightmareSuccessfulCompletionActivation.run(operations)
        );

        assertEquals("Successful Nightmare receipt disappeared before completion recovery", failure.getMessage());
        assertEquals(
                List.of("validate", "capture", "record", "persist", "verify", "boundary", "world", "resume"),
                operations.calls
        );
    }

    private static final class SimulatedFault extends RuntimeException {
    }

    private static final class FakeOperations implements NightmareSuccessfulCompletionActivation.Operations {
        private final List<String> calls = new ArrayList<>();
        private boolean failAtBoundary;
        private RuntimeException persistenceFailure;
        private RuntimeException verificationFailure;
        private RuntimeException discardFailure;
        private boolean resumeResult = true;

        public void validateTerminalResolution() {
            calls.add("validate");
        }

        public void captureRegistryBeforeTerminalResolution() {
            calls.add("capture");
        }

        public void recordTerminalResolution() {
            calls.add("record");
        }

        public void persistRegistry() {
            calls.add("persist");
            if (persistenceFailure != null) {
                throw persistenceFailure;
            }
        }

        public void verifyTerminalRegistryDurable() {
            calls.add("verify");
            if (verificationFailure != null) {
                throw verificationFailure;
            }
        }

        public void discardUnverifiedTerminalResolution() {
            calls.add("discard");
            if (discardFailure != null) {
                throw discardFailure;
            }
        }

        public void afterTerminalRegistryDurable() {
            calls.add("boundary");
            if (failAtBoundary) {
                throw new SimulatedFault();
            }
        }

        public void applyWorldResolutionPresentation() {
            calls.add("world");
        }

        public boolean resumeCompletion() {
            calls.add("resume");
            return resumeResult;
        }
    }
}
