package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only placeholder rendering for physical Nightmare Creature adapters. */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class NightmareCreatureClientEvents {
    private NightmareCreatureClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Placeholder visual: the Java-owned Chainback identity is not derived from this vanilla model.
        event.registerEntityRenderer(NightmareCreatureEntities.CHAINBACK.get(), SpiderRenderer::new);
    }
}
