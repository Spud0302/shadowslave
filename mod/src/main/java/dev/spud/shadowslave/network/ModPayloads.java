package dev.spud.shadowslave.network;

import dev.spud.shadowslave.network.payload.ActivateKindleRequestPayload;
import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoModeRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshCompassRequestPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Common payload registration that is safe for a physical dedicated server. */
public final class ModPayloads {
    public static final String NETWORK_VERSION = "3";

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
        registrar.playToServer(
                ActivateKindleRequestPayload.TYPE,
                ActivateKindleRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleActivateKindle
        );
        registrar.playToServer(
                ToggleAshCompassRequestPayload.TYPE,
                ToggleAshCompassRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleToggleAshCompass
        );
        registrar.playToServer(
                ToggleAshBurrowerEchoRequestPayload.TYPE,
                ToggleAshBurrowerEchoRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleToggleAshBurrowerEcho
        );
        registrar.playToServer(
                ToggleAshBurrowerEchoModeRequestPayload.TYPE,
                ToggleAshBurrowerEchoModeRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleToggleAshBurrowerEchoMode
        );
    }
}
