package dev.spud.shadowslave.network;

import dev.spud.shadowslave.network.payload.AbilityCastPayload;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.network.payload.AspectRevealPayload;
import dev.spud.shadowslave.network.payload.AwakenRequestPayload;
import dev.spud.shadowslave.network.payload.ChannelRequestPayload;
import dev.spud.shadowslave.network.payload.CoreStatusPayload;
import dev.spud.shadowslave.network.payload.FieldGuideMapPayload;
import dev.spud.shadowslave.network.payload.NightmareAppraisalPayload;
import dev.spud.shadowslave.network.payload.FieldGuidePinRequestPayload;
import dev.spud.shadowslave.network.payload.FieldGuideStatusPayload;
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
import dev.spud.shadowslave.network.payload.WaymarkPayload;
import dev.spud.shadowslave.network.payload.MemoryKeyPayload;
import dev.spud.shadowslave.network.payload.MemorySummonPayload;
import dev.spud.shadowslave.network.payload.StrandCreatePayload;
import dev.spud.shadowslave.network.payload.WeaveTracePayload;
import dev.spud.shadowslave.network.payload.WeavingStatusPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/** Common payload registration that is safe for a physical dedicated server. */
public final class ModPayloads {
    public static final String NETWORK_VERSION = "21";

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
        registrar.playToClient(
                AbilityCastPayload.TYPE,
                AbilityCastPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                AbilityStatusPayload.TYPE,
                AbilityStatusPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                CoreStatusPayload.TYPE,
                CoreStatusPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                AspectRevealPayload.TYPE,
                AspectRevealPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                ForewarningPayload.TYPE,
                ForewarningPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                WaymarkPayload.TYPE,
                WaymarkPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                SoulSeaPayload.TYPE,
                SoulSeaPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                WeavingStatusPayload.TYPE,
                WeavingStatusPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                FieldGuideStatusPayload.TYPE,
                FieldGuideStatusPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                NightmareAppraisalPayload.TYPE,
                NightmareAppraisalPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToClient(
                FieldGuideMapPayload.TYPE,
                FieldGuideMapPayload.STREAM_CODEC,
                (payload, context) -> {
                }
        );
        registrar.playToServer(
                WeaveTracePayload.TYPE,
                WeaveTracePayload.STREAM_CODEC,
                ServerPayloadHandler::handleWeaveTrace
        );
        registrar.playToServer(
                MemoryKeyPayload.TYPE,
                MemoryKeyPayload.STREAM_CODEC,
                ServerPayloadHandler::handleMemoryKey
        );
        registrar.playToServer(
                MemorySummonPayload.TYPE,
                MemorySummonPayload.STREAM_CODEC,
                ServerPayloadHandler::handleMemorySummon
        );
        registrar.playToServer(
                StrandCreatePayload.TYPE,
                StrandCreatePayload.STREAM_CODEC,
                ServerPayloadHandler::handleStrandCreate
        );
        registrar.playToServer(
                FieldGuidePinRequestPayload.TYPE,
                FieldGuidePinRequestPayload.STREAM_CODEC,
                ServerPayloadHandler::handleFieldGuidePin
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
