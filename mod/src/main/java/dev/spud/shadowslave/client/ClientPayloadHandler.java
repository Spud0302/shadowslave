package dev.spud.shadowslave.client;

import dev.spud.shadowslave.network.payload.AbilityCastPayload;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import dev.spud.shadowslave.network.payload.AspectRevealPayload;
import dev.spud.shadowslave.network.payload.SoulSnapshotPayload;
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

    public static void handleAbilityStatus(AbilityStatusPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        long now = minecraft.level == null ? 0 : minecraft.level.getGameTime();
        ClientAbilityState.update(payload, now);
    }

    public static void handleAbilityCast(AbilityCastPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceLocation abilityId = ResourceLocation.tryParse(payload.abilityId());
        if (minecraft.level != null && abilityId != null
                && minecraft.level.getEntity(payload.entityId()) instanceof AbstractClientPlayer player) {
            AbilityCastAnimations.play(player, abilityId);
        }
    }
}
