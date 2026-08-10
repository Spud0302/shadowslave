package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.client.renderer.AshBurrowerEchoRenderer;
import dev.spud.shadowslave.client.renderer.AshBurrowerRenderer;
import dev.spud.shadowslave.client.renderer.ChainbackRenderer;
import dev.spud.shadowslave.client.renderer.DrownedListenerRenderer;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only rendering for physical creature and Echo adapters. */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class NightmareCreatureClientEvents {
    private NightmareCreatureClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Project-owned GeckoLib presentation. Java-owned state remains authority for every identity.
        event.registerEntityRenderer(NightmareCreatureEntities.ASH_BURROWER.get(), AshBurrowerRenderer::new);
        event.registerEntityRenderer(NightmareCreatureEntities.ASH_BURROWER_ECHO.get(), AshBurrowerEchoRenderer::new);
        event.registerEntityRenderer(NightmareCreatureEntities.CHAINBACK.get(), ChainbackRenderer::new);
        event.registerEntityRenderer(NightmareCreatureEntities.DROWNED_LISTENER.get(), DrownedListenerRenderer::new);
    }
}
