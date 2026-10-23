package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.network.payload.AbilityStatusPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Kit effects the client has to apply itself. Stone Sleep: you sink in water,
 * even while swimming up (movement is client-driven). Clouded Eyes: a short,
 * hazy view whenever Foresight isn't open (fog is drawn by the client).
 */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT)
public final class ClientKitEffects {
    static final double STONE_SINK_PER_TICK = 0.05;
    private static final String STONE_SLEEP = AspectKitCatalog.STONE.flawEffectId().toString();
    private static final String CLOUDED_EYES = AspectKitCatalog.SEER.flawEffectId().toString();
    private static final String FORESIGHT = AspectKitCatalog.SEER.abilityId().toString();
    static final float CLOUDED_FAR = 36.0F;
    static final float CLOUDED_NEAR = 2.0F;

    private ClientKitEffects() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LocalPlayer player) || !player.isInWater() || player.getAbilities().flying) {
            return;
        }
        boolean stoneSleep = ClientSoulState.latest()
                .map(snapshot -> STONE_SLEEP.equals(snapshot.flawEffectId()))
                .orElse(false);
        if (stoneSleep) {
            player.setDeltaMovement(player.getDeltaMovement().add(0.0, -STONE_SINK_PER_TICK, 0.0));
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (Minecraft.getInstance().player == null || !cloudedEyes() || event.getFarPlaneDistance() <= CLOUDED_FAR) {
            return;
        }
        event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), CLOUDED_NEAR));
        event.setFarPlaneDistance(CLOUDED_FAR);
        event.setCanceled(true);
    }

    /** Clouded Eyes is on: the player's Flaw, with Foresight not open right now. */
    static boolean cloudedEyes() {
        boolean flawed = ClientSoulState.latest()
                .map(snapshot -> CLOUDED_EYES.equals(snapshot.flawEffectId()))
                .orElse(false);
        return flawed && !foresightOpen(ClientAbilityState.status());
    }

    static boolean foresightOpen(AbilityStatusPayload status) {
        return status.slots().stream().anyMatch(slot -> slot.abilityId().equals(FORESIGHT) && slot.primed());
    }
}
