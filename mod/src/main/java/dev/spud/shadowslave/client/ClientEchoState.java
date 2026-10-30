package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.EchoStatusPayload;

/** Client-only cache of the latest Echo reading. */
public final class ClientEchoState {
    private static EchoStatusPayload status = EchoStatusPayload.NONE;

    private ClientEchoState() {
    }

    public static void update(EchoStatusPayload payload) {
        status = payload;
    }

    public static EchoStatusPayload status() {
        return status;
    }

    public static void clear() {
        status = EchoStatusPayload.NONE;
    }
}
