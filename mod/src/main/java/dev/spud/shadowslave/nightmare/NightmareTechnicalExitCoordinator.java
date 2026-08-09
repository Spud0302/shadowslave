package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/** Ordered durable commit for technical recovery and administrator abort after return teleport. */
public final class NightmareTechnicalExitCoordinator {
    private NightmareTechnicalExitCoordinator() {
    }

    public static void commit(Operations operations) {
        Operations checked = Objects.requireNonNull(operations, "operations");

        checked.recordTechnicalExitIntent();
        checked.persistRegistry();

        checked.clearCompletionReceipt();
        checked.persistRegistry();

        checked.resetPlayerState();
        checked.persistPlayer();

        checked.teardownActiveInstance();
        checked.persistRegistry();
    }

    public interface Operations {
        void recordTechnicalExitIntent();

        void clearCompletionReceipt();

        void persistRegistry();

        void resetPlayerState();

        void persistPlayer();

        void teardownActiveInstance();
    }
}
