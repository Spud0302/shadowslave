package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.client.renderer.AshBurrowerEchoRenderer;
import dev.spud.shadowslave.client.renderer.AshBurrowerRenderer;
import dev.spud.shadowslave.client.renderer.ChainbackRenderer;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.client.renderer.entity.DrownedRenderer;
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
        // Project-owned GeckoLib presentation. Hostile/owned Ash Burrowers share creature visual
        // identity; Chainback uses a separate project geometry. Java-owned state remains authority.
        event.registerEntityRenderer(NightmareCreatureEntities.ASH_BURROWER.get(), AshBurrowerRenderer::new);
        event.registerEntityRenderer(NightmareCreatureEntities.ASH_BURROWER_ECHO.get(), AshBurrowerEchoRenderer::new);
        event.registerEntityRenderer(NightmareCreatureEntities.CHAINBACK.get(), ChainbackRenderer::new);

        // Remaining vanilla visual placeholder.
        event.registerEntityRenderer(NightmareCreatureEntities.DROWNED_LISTENER.get(), DrownedRenderer::new);
    }
}
