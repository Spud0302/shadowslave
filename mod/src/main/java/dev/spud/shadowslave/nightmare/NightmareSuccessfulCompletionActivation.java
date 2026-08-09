package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/**
 * Orders the terminal-resolution handoff into the durable completion coordinator.
 *
 * <p>The terminal objective is validated before success authority is created. The
 * registry baseline is captured before that authority mutation, and the resulting
 * receipt must be observably durable before later world presentation or completion
 * replay can proceed.</p>
 */
final class NightmareSuccessfulCompletionActivation {
    private NightmareSuccessfulCompletionActivation() {
    }

    static void run(Operations operations) {
        Operations checked = Objects.requireNonNull(operations, "operations");
        checked.validateTerminalResolution();
        checked.captureRegistryBeforeTerminalResolution();
        checked.recordTerminalResolution();
        checked.persistRegistry();
        checked.verifyTerminalRegistryDurable();
        checked.afterTerminalRegistryDurable();
        checked.applyWorldResolutionPresentation();
        if (!checked.resumeCompletion()) {
            throw new IllegalStateException("Successful Nightmare receipt disappeared before completion recovery");
        }
    }

    interface Operations {
        void validateTerminalResolution();
        void captureRegistryBeforeTerminalResolution();
        void recordTerminalResolution();
        void persistRegistry();
        void verifyTerminalRegistryDurable();
        void afterTerminalRegistryDurable();
        void applyWorldResolutionPresentation();
        boolean resumeCompletion();
    }
}
