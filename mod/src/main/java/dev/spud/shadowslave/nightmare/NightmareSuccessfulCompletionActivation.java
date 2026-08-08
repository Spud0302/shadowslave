package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/**
 * Orders the terminal-resolution handoff into the durable completion coordinator.
 *
 * <p>The terminal objective is validated before success authority is created. Once
 * the receipt has been durably saved, later world presentation is replayable and
 * cannot be the sole authority for whether the Nightmare resolved.</p>
 */
final class NightmareSuccessfulCompletionActivation {
    private NightmareSuccessfulCompletionActivation() {
    }

    static void run(Operations operations) {
        Operations checked = Objects.requireNonNull(operations, "operations");
        checked.validateTerminalResolution();
        checked.recordTerminalResolution();
        checked.persistRegistry();
        checked.afterTerminalRegistryDurable();
        checked.applyWorldResolutionPresentation();
        if (!checked.resumeCompletion()) {
            throw new IllegalStateException("Successful Nightmare receipt disappeared before completion recovery");
        }
    }

    interface Operations {
        void validateTerminalResolution();
        void recordTerminalResolution();
        void persistRegistry();
        void afterTerminalRegistryDurable();
        void applyWorldResolutionPresentation();
        boolean resumeCompletion();
    }
}
