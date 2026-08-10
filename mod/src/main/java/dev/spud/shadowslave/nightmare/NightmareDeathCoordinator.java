package dev.spud.shadowslave.nightmare;

import java.util.Objects;

/** Ordered durable commit for the canonical First-Nightmare death outcome. */
public final class NightmareDeathCoordinator {
    private NightmareDeathCoordinator() {
    }

    public static void commit(Operations operations) {
        Operations checked = Objects.requireNonNull(operations, "operations");

        if (!checked.deathIntentAlreadyDurable()) {
            checked.captureDeathIntentBaseline();
            checked.recordDeathIntent();
            checked.persistDeathIntent();
            checked.verifyDeathIntentPersisted();
            checked.markDeathIntentDurable();
        }

        checked.clearCompletionReceipt();
        checked.persistNightmareRegistry();

        checked.resetPlayerState();
        checked.persistPlayer();
        checked.verifyPlayerPersisted();

        checked.teardownActiveInstance();
        checked.persistNightmareRegistry();
        checked.verifyOwnershipTeardownPersisted();

        checked.clearDeathIntent();
        checked.persistDeathIntent();
    }

    public interface Operations {
        boolean deathIntentAlreadyDurable();

        void captureDeathIntentBaseline();

        void recordDeathIntent();

        void persistDeathIntent();

        void verifyDeathIntentPersisted();

        void markDeathIntentDurable();

        void clearCompletionReceipt();

        void persistNightmareRegistry();

        void resetPlayerState();

        void persistPlayer();

        void verifyPlayerPersisted();

        void teardownActiveInstance();

        void verifyOwnershipTeardownPersisted();

        void clearDeathIntent();
    }
}
