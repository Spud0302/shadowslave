package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.ModPayloads;
import dev.spud.shadowslave.network.ServerPayloadHandler;
import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Physical-client registration of clientbound rendering handlers. */
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
    }
}
