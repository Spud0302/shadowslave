package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Kit effects that change the local player's own movement, which the client
 * drives. Stone Sleep: you sink in water, even while swimming up.
 */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT)
public final class ClientKitEffects {
    static final double STONE_SINK_PER_TICK = 0.05;
    private static final String STONE_SLEEP = AspectKitCatalog.STONE.flawEffectId().toString();

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
}
