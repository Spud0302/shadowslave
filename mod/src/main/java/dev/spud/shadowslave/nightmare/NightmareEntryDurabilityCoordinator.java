package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/**
 * Orders the cross-surface durability boundary for a successful Nightmare entry.
 *
 * <p>The prepared active ownership must become durable before any player Soul/location
 * mutation. Once entry has committed, the player state must be synchronously persisted
 * before the operation may report success.</p>
 *
 * <p>This coordinator deliberately contains no scenario, appraisal, content-provider,
 * or presentation policy. It is a base persistence/authority primitive.</p>
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
