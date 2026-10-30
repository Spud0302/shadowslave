package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.ModPayloads;
import dev.spud.shadowslave.network.ServerPayloadHandler;
import dev.spud.shadowslave.network.payload.AbilityCastPayload;
import dev.spud.shadowslave.network.payload.DisguisePayload;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.network.payload.AspectRevealPayload;
import dev.spud.shadowslave.network.payload.AwakenRequestPayload;
import dev.spud.shadowslave.network.payload.CallingFrequencyPayload;
import dev.spud.shadowslave.network.payload.SpellCallingsPayload;
import dev.spud.shadowslave.network.payload.GatewayActionPayload;
import dev.spud.shadowslave.network.payload.GatewayStatusPayload;
import dev.spud.shadowslave.network.payload.TrueNameActionPayload;
import dev.spud.shadowslave.network.payload.TrueNamePayload;
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
                DisguisePayload.TYPE,
                DisguisePayload.STREAM_CODEC,
                (payload, context) -> ClientDisguises.set(payload.entityId(), payload.entityType())
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
                WaymarkPayload.TYPE,
                WaymarkPayload.STREAM_CODEC,
                ClientPayloadHandler::handleWaymarks
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
        registrar.playToClient(
                FieldGuideStatusPayload.TYPE,
                FieldGuideStatusPayload.STREAM_CODEC,
                ClientPayloadHandler::handleFieldGuideStatus
        );
        registrar.playToClient(
                NightmareAppraisalPayload.TYPE,
                NightmareAppraisalPayload.STREAM_CODEC,
                ClientPayloadHandler::handleNightmareAppraisal
        );
        registrar.playToClient(
                FieldGuideMapPayload.TYPE,
                FieldGuideMapPayload.STREAM_CODEC,
                ClientPayloadHandler::handleFieldGuideMap
        );
        registrar.playToClient(
                SpellCallingsPayload.TYPE,
                SpellCallingsPayload.STREAM_CODEC,
                ClientPayloadHandler::handleSpellCallings
        );
        registrar.playToClient(
                TrueNamePayload.TYPE,
                TrueNamePayload.STREAM_CODEC,
                ClientPayloadHandler::handleTrueName
        );
        registrar.playToClient(
                GatewayStatusPayload.TYPE,
                GatewayStatusPayload.STREAM_CODEC,
                ClientPayloadHandler::handleGatewayStatus
        );
        registrar.playToServer(
                GatewayActionPayload.TYPE,
                GatewayActionPayload.STREAM_CODEC,
                ServerPayloadHandler::handleGatewayAction
        );
        registrar.playToServer(
                TrueNameActionPayload.TYPE,
                TrueNameActionPayload.STREAM_CODEC,
                ServerPayloadHandler::handleTrueNameAction
        );
        registrar.playToServer(
                CallingFrequencyPayload.TYPE,
                CallingFrequencyPayload.STREAM_CODEC,
                ServerPayloadHandler::handleCallingFrequency
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
                dev.spud.shadowslave.network.payload.EchoActionPayload.TYPE,
                dev.spud.shadowslave.network.payload.EchoActionPayload.STREAM_CODEC,
                ServerPayloadHandler::handleEchoAction
        );
        registrar.playToClient(
                dev.spud.shadowslave.network.payload.EchoStatusPayload.TYPE,
                dev.spud.shadowslave.network.payload.EchoStatusPayload.STREAM_CODEC,
                ClientPayloadHandler::handleEchoStatus
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
