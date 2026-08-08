package dev.spud.shadowslave.nightmare;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NightmareSuccessfulCompletionActivationTest {
    @Test
    void durableTerminalReceiptPrecedesWorldPresentationAndRecovery() {
        FakeOperations operations = new FakeOperations();

        NightmareSuccessfulCompletionActivation.run(operations);

        assertEquals(
                List.of("validate", "record", "persist", "boundary", "world", "resume"),
                operations.calls
        );
    }

    @Test
    void terminalFaultStopsBeforeReplayableWorldPresentation() {
        FakeOperations operations = new FakeOperations();
        operations.failAtBoundary = true;

        assertThrows(SimulatedFault.class, () -> NightmareSuccessfulCompletionActivation.run(operations));

        assertEquals(
                List.of("validate", "record", "persist", "boundary"),
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
                List.of("validate", "record", "persist", "boundary", "world", "resume"),
                operations.calls
        );
    }

    private static final class SimulatedFault extends RuntimeException {
    }

    private static final class FakeOperations implements NightmareSuccessfulCompletionActivation.Operations {
        private final List<String> calls = new ArrayList<>();
        private boolean failAtBoundary;
        private boolean resumeResult = true;

        public void validateTerminalResolution() {
            calls.add("validate");
        }

        public void recordTerminalResolution() {
            calls.add("record");
        }

        public void persistRegistry() {
            calls.add("persist");
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
