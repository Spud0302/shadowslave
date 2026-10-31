package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.AbilityCastPayload;
import dev.spud.shadowslave.network.payload.ForewarningPayload;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.network.payload.AspectRevealPayload;
import dev.spud.shadowslave.network.payload.NightmareAppraisalPayload;
import dev.spud.shadowslave.network.payload.SpellCallingsPayload;
import dev.spud.shadowslave.network.payload.GatewayStatusPayload;
import dev.spud.shadowslave.network.payload.TrueNamePayload;
import dev.spud.shadowslave.network.payload.CoreStatusPayload;
import dev.spud.shadowslave.network.payload.FieldGuideMapPayload;
import dev.spud.shadowslave.network.payload.FieldGuidePathPayload;
import dev.spud.shadowslave.network.payload.FieldGuideStatusPayload;
import dev.spud.shadowslave.network.payload.SoulSeaPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
import dev.spud.shadowslave.network.payload.WaymarkPayload;
import dev.spud.shadowslave.network.payload.WeavingStatusPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client-only application of read-only Soul snapshots, ability status and casts, and Spell reveals. */
public final class ClientPayloadHandler {
    private ClientPayloadHandler() {
    }

    public static void handleSoulSnapshot(SoulSnapshotPayload payload, IPayloadContext context) {
        ClientSoulState.update(payload.snapshot());
        if (payload.openScreen()) {
            Minecraft.getInstance().setScreen(new SoulScreen(payload.snapshot()));
        }
    }

    public static void handleAspectReveal(AspectRevealPayload payload, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new SpellRevealScreen(payload));
    }

    public static void handleSpellCallings(SpellCallingsPayload payload, IPayloadContext context) {
        ClientCallingsState.update(payload);
    }

    public static void handleTrueName(TrueNamePayload payload, IPayloadContext context) {
        ClientTrueNameState.update(payload);
        if (payload.reveal() && payload.named()) {
            Minecraft.getInstance().setScreen(new TrueNameRevealScreen(payload));
        }
    }

    public static void handleGatewayStatus(GatewayStatusPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof GatewayScreen open && open.citadel().equals(payload.citadel())) {
            open.update(payload);
        } else {
            minecraft.setScreen(new GatewayScreen(payload));
        }
    }

    public static void handleNightmareAppraisal(NightmareAppraisalPayload payload, IPayloadContext context) {
        Minecraft.getInstance().setScreen(new NightmareAppraisalScreen(payload));
    }

    public static void handleAbilityStatus(AbilityStatusPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        long now = minecraft.level == null ? 0 : minecraft.level.getGameTime();
        ClientAbilityState.update(payload, now);
    }

    public static void handleForewarning(ForewarningPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            ForewarningLayer.warn(payload, minecraft.level.getGameTime());
        }
    }

    public static void handleWaymarks(WaymarkPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            WaymarkRenderer.show(payload, minecraft.level.getGameTime());
        }
    }

    public static void handleSoulSea(SoulSeaPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientCoreState.update(payload.core());
        if (minecraft.screen instanceof SoulSeaScreen open) {
            open.update(payload);
        } else if (payload.open()) {
            minecraft.setScreen(new SoulSeaScreen(payload));
        }
    }

    public static void handleWeavingStatus(WeavingStatusPayload payload, IPayloadContext context) {
        ClientWeavingState.update(payload);
    }

    public static void handleEchoStatus(dev.spud.shadowslave.network.payload.EchoStatusPayload payload, IPayloadContext context) {
        ClientEchoState.update(payload);
    }

    public static void handleFieldGuideStatus(FieldGuideStatusPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientFieldGuideState.update(payload);
        if (payload.open() && !(minecraft.screen instanceof FieldGuideScreen)) {
            minecraft.setScreen(new FieldGuideScreen());
        }
    }

    public static void handleFieldGuideMap(FieldGuideMapPayload payload, IPayloadContext context) {
        ClientFieldGuideState.applyMap(payload);
    }

    public static void handleFieldGuidePaths(FieldGuidePathPayload payload, IPayloadContext context) {
        ClientFieldGuideState.applyPaths(payload);
    }

    public static void handleCoreStatus(CoreStatusPayload payload, IPayloadContext context) {
        ClientCoreState.update(payload);
    }

    public static void handleAbilityCast(AbilityCastPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation abilityId = ResourceLocation.tryParse(payload.abilityId());
        if (minecraft.level != null && abilityId != null
                && minecraft.level.getEntity(payload.entityId()) instanceof AbstractClientPlayer player) {
            AbilityCastAnimations.play(player, abilityId);
            AbilityCastEffects.play(player, abilityId);
        }
    }
}
