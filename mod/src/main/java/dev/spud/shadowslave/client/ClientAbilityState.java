package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.AbilityStatusPayload;

/**
 * Client-only cache of the HUD ability slots and the essence pool. Between
 * server updates the pool is predicted to refill on the client's own game
 * clock, so the bar rises smoothly.
 */
public final class ClientAbilityState {
    private static AbilityStatusPayload status = AbilityStatusPayload.NONE;
    private static long receivedAt;
    /** Per slot: the client game time the pool covers its cost again, or -1 if it already did when received. */
    private static long[] affordableAt = new long[0];
    private static boolean[] cued = new boolean[0];

    private ClientAbilityState() {
    }

    public static void update(AbilityStatusPayload payload, long clientGameTime) {
        status = payload;
        receivedAt = clientGameTime;
        int count = payload.slots().size();
        affordableAt = new long[count];
        cued = new boolean[count];
        for (int i = 0; i < count; i++) {
            AbilityStatusPayload.Slot slot = payload.slots().get(i);
            float missing = slot.cost() - payload.essence();
            if (missing <= 1.0e-3F || slot.primed()) {
                affordableAt[i] = -1;
            } else if (payload.regenPerTick() <= 0.0F || slot.cost() > payload.capacity()) {
                affordableAt[i] = Long.MAX_VALUE;
            } else {
                affordableAt[i] = clientGameTime + (long) Math.ceil(missing / payload.regenPerTick());
            }
        }
    }

    public static AbilityStatusPayload status() {
        return status;
    }

    /** Essence in the pool now, as predicted from the last update. */
    public static float essence(long clientGameTime, float partialTick) {
        float elapsed = Math.max(0.0F, clientGameTime - receivedAt + partialTick);
        return Math.min(status.capacity(), status.essence() + status.regenPerTick() * elapsed);
    }

    /** How full the pool is, from 0 to 1. */
    public static float essenceFraction(long clientGameTime, float partialTick) {
        return status.capacity() <= 0 ? 0.0F : essence(clientGameTime, partialTick) / status.capacity();
    }

    /** Fraction of a slot's cost the pool still lacks, 0 when the ability can be used. */
    public static float shortfall(int slot, long clientGameTime, float partialTick) {
        if (slot < 0 || slot >= status.slots().size()) {
            return 0.0F;
        }
        AbilityStatusPayload.Slot value = status.slots().get(slot);
        if (value.primed() || value.cost() <= 0) {
            return 0.0F;
        }
        float missing = value.cost() - essence(clientGameTime, partialTick);
        return Math.max(0.0F, Math.min(1.0F, missing / value.cost()));
    }

    /** Ticks since the pool came to cover this slot's cost, or -1 while it is short or it was never short. */
    public static long ticksSinceAffordable(int slot, long clientGameTime) {
        if (slot < 0 || slot >= affordableAt.length || affordableAt[slot] < 0 || clientGameTime < affordableAt[slot]) {
            return -1;
        }
        return clientGameTime - affordableAt[slot];
    }

    /** True once each time a slot that was short of essence can be used again, so the client can chime. */
    public static boolean consumeReadyCue(long clientGameTime) {
        boolean cue = false;
        for (int i = 0; i < affordableAt.length; i++) {
            if (!cued[i] && ticksSinceAffordable(i, clientGameTime) >= 0) {
                cued[i] = true;
                cue = true;
            }
        }
        return cue;
    }

    public static void clear() {
        status = AbilityStatusPayload.NONE;
        receivedAt = 0;
        affordableAt = new long[0];
        cued = new boolean[0];
    }
}
