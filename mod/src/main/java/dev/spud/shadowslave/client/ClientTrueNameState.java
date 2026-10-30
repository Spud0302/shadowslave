package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.TrueNamePayload;

/** Client-only cache of the player's True Name page; never treated as authoritative. */
public final class ClientTrueNameState {
    private static TrueNamePayload latest = TrueNamePayload.NONE;

    private ClientTrueNameState() {
    }

    public static void update(TrueNamePayload payload) {
        latest = payload;
    }

    public static TrueNamePayload latest() {
        return latest;
    }

    public static void clear() {
        latest = TrueNamePayload.NONE;
    }
}
