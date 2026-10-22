package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.AbilityStatusPayload;

/**
 * Client-only cache of the HUD ability slot. The cooldown is kept as an end
 * time on the client's own game clock so the sweep runs smoothly between
 * server updates.
 */
public final class ClientAbilityState {
    private static AbilityStatusPayload status = AbilityStatusPayload.NONE;
    private static long readyAt;
    private static long receivedAt;

    private ClientAbilityState() {
    }

    public static void update(AbilityStatusPayload payload, long clientGameTime) {
        status = payload;
        readyAt = clientGameTime + payload.remainingTicks();
        receivedAt = clientGameTime;
    }

    public static AbilityStatusPayload status() {
        return status;
    }

    public static long remainingTicks(long clientGameTime) {
        return Math.max(0, readyAt - clientGameTime);
    }

    /** Fraction of the cooldown still to run, 0 when ready. */
    public static float cooldownFraction(long clientGameTime, float partialTick) {
        int total = status.cooldownTicks();
        if (total <= 0) {
            return 0.0F;
        }
        float remaining = readyAt - clientGameTime - partialTick;
        return Math.max(0.0F, Math.min(1.0F, remaining / total));
    }

    /** Ticks since the cooldown ran out, or -1 while it is still running or nothing was used. */
    public static long ticksSinceReady(long clientGameTime) {
        if (readyAt <= receivedAt || clientGameTime < readyAt) {
            return -1;
        }
        return clientGameTime - readyAt;
    }

    public static void clear() {
        status = AbilityStatusPayload.NONE;
        readyAt = 0;
        receivedAt = 0;
    }
}
