package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.WeavingStatusPayload;

/** Client-only cache of the latest Weaving reading. */
public final class ClientWeavingState {
    private static WeavingStatusPayload status = WeavingStatusPayload.NONE;

    private ClientWeavingState() {
    }

    public static void update(WeavingStatusPayload payload) {
        status = payload;
    }

    public static WeavingStatusPayload status() {
        return status;
    }

    public static void clear() {
        status = WeavingStatusPayload.NONE;
    }
}
