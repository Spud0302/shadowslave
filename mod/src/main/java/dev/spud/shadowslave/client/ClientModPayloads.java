package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.ModPayloads;
import dev.spud.shadowslave.network.ServerPayloadHandler;
import dev.spud.shadowslave.network.payload.AbilityCastPayload;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.network.payload.AspectRevealPayload;
import dev.spud.shadowslave.network.payload.AwakenRequestPayload;
import dev.spud.shadowslave.network.payload.ChannelRequestPayload;
import dev.spud.shadowslave.network.payload.CoreStatusPayload;
import dev.spud.shadowslave.network.payload.ForewarningPayload;
import dev.spud.shadowslave.network.payload.IntentionRequestPayload;
import dev.spud.shadowslave.network.payload.OpenSoulScreenRequestPayload;
import dev.spud.shadowslave.network.payload.RefineRequestPayload;
import dev.spud.shadowslave.network.payload.SoulSeaPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoModeRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshBurrowerEchoRequestPayload;
import dev.spud.shadowslave.network.payload.ToggleAshCompassRequestPayload;
import dev.spud.shadowslave.network.payload.UseAbilityRequestPayload;
import dev.spud.shadowslave.network.payload.WeavingStatusPayload;
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
        registrar.playToClient(
                AbilityCastPayload.TYPE,
                AbilityCastPayload.STREAM_CODEC,
                ClientPayloadHandler::handleAbilityCast
        );
        registrar.playToClient(
                AbilityStatusPayload.TYPE,
                AbilityStatusPayload.STREAM_CODEC,
                ClientPayloadHandler::handleAbilityStatus
        );
        registrar.playToClient(
                CoreStatusPayload.TYPE,
                CoreStatusPayload.STREAM_CODEC,
                ClientPayloadHandler::handleCoreStatus
        );
        registrar.playToClient(
                AspectRevealPayload.TYPE,
                AspectRevealPayload.STREAM_CODEC,
                ClientPayloadHandler::handleAspectReveal
        );
        registrar.playToClient(
                ForewarningPayload.TYPE,
                ForewarningPayload.STREAM_CODEC,
                ClientPayloadHandler::handleForewarning
        );
        registrar.playToClient(
                SoulSeaPayload.TYPE,
                SoulSeaPayload.STREAM_CODEC,
                ClientPayloadHandler::handleSoulSea
        );
        registrar.playToClient(
                WeavingStatusPayload.TYPE,
                WeavingStatusPayload.STREAM_CODEC,
                ClientPayloadHandler::handleWeavingStatus
        );
        registrar.playToServer(
                AwakenRequestPayload.TYPE,
                AwakenRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleAwaken
        );
        registrar.playToServer(
                ChannelRequestPayload.TYPE,
                ChannelRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleChannel
        );
        registrar.playToServer(
                RefineRequestPayload.TYPE,
                RefineRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleRefine
        );
        registrar.playToServer(
                IntentionRequestPayload.TYPE,
                IntentionRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleIntention
        );
        registrar.playToServer(
                OpenSoulScreenRequestPayload.TYPE,
                OpenSoulScreenRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleOpenSoulScreen
        );
        registrar.playToServer(
                UseAbilityRequestPayload.TYPE,
                UseAbilityRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleUseAbility
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
