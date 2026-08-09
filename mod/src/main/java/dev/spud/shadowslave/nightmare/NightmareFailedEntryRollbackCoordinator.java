package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/** Keeps pre-entry Soul restoration behind successful authoritative rollback. */
final class NightmareFailedEntryRollbackCoordinator {
    private NightmareFailedEntryRollbackCoordinator() {
    }

    static void rollback(Operations operations) {
        Operations checked = Objects.requireNonNull(operations, "operations");
        checked.rollbackAuthoritativeState();
        checked.restoreSoul();
    }

    interface Operations {
        void rollbackAuthoritativeState();

        void restoreSoul();
    }
}
