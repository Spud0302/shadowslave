package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.CoreStatusPayload;

/** Client-only cache of the latest soul core reading. */
public final class ClientCoreState {
    private static CoreStatusPayload status = CoreStatusPayload.NONE;

    private ClientCoreState() {
    }

    public static void update(CoreStatusPayload payload) {
        status = payload;
    }

    public static CoreStatusPayload status() {
        return status;
    }

    public static void clear() {
        status = CoreStatusPayload.NONE;
    }
}
