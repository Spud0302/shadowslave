package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/** Ordered durable commit for the canonical First-Nightmare death outcome. */
public final class NightmareDeathCoordinator {
    private NightmareDeathCoordinator() {
    }

    public static void commit(Operations operations) {
        Operations checked = Objects.requireNonNull(operations, "operations");

        checked.recordDeathIntent();
        checked.persistDeathIntent();

        checked.clearCompletionReceipt();
        checked.persistNightmareRegistry();

        checked.resetPlayerState();
        checked.persistPlayer();

        checked.teardownActiveInstance();
        checked.persistNightmareRegistry();

        checked.clearDeathIntent();
        checked.persistDeathIntent();
    }

    public interface Operations {
        void recordDeathIntent();
        void persistDeathIntent();
        void clearCompletionReceipt();
        void persistNightmareRegistry();
        void resetPlayerState();
        void persistPlayer();
        void teardownActiveInstance();
        void clearDeathIntent();
    }
}
