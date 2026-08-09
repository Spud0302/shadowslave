package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/**
 * Orders the cross-surface durability boundary for a successful Nightmare entry.
 *
 * <p>The active prepared ownership must be durable before player Soul/location
 * state may change. Once entry has committed, the complete player state is saved
 * synchronously before the operation returns.</p>
 */
final class NightmareEntryDurabilityCoordinator {
    private NightmareEntryDurabilityCoordinator() {
    }

    static void commit(Operations operations) {
        Operations checked = Objects.requireNonNull(operations, "operations");
        checked.persistPreparedOwnership();
        checked.applyPlayerEntry();
        checked.persistCommittedPlayer();
    }

    interface Operations {
        void persistPreparedOwnership();

        void applyPlayerEntry();

        void persistCommittedPlayer();
    }
}
