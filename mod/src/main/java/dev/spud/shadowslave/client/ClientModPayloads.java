package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.ModPayloads;
import dev.spud.shadowslave.network.ServerPayloadHandler;
import dev.spud.shadowslave.network.payload.ActivateKindleRequestPayload;
import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoModeRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshCompassRequestPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Physical-client registration of clientbound rendering handlers and outbound gameplay intents. */
public final class ClientModPayloads {
    private ClientModPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(ModPayloads.NETWORK_VERSION);
        registrar.playToClient(
                SoulSnapshotPayload.TYPE,
                SoulSnapshotPayload.STREAM_CODEC,
                ClientPayloadHandler::handleSoulSnapshot
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
