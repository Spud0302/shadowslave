package dev.spud.shadowslave.network;

import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Common payload registration that is safe for a physical dedicated server. */
public final class ModPayloads {
    public static final String NETWORK_VERSION = "1";

    private ModPayloads() {
    }

    public static void registerDedicatedServer(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);

        // The dedicated server must know how to encode this clientbound payload,
        // but can never receive it. The no-op handler avoids linking client classes.
        registrar.playToClient(
                SoulSnapshotPayload.TYPE,
                SoulSnapshotPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToServer(
                OpenSoulScreenRequestPayload.TYPE,
                OpenSoulScreenRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleOpenSoulScreen
        );
    }
}
