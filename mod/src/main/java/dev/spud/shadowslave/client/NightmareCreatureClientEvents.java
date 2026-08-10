package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.client.renderer.AshBurrowerRenderer;
import dev.spud.shadowslave.world.entity.NightmareCreatureEntities;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Client-only rendering for physical Nightmare Creature adapters. */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class NightmareCreatureClientEvents {
    private NightmareCreatureClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Ash Burrower now has project-owned geometry/animation through GeckoLib. Its texture remains
        // an explicitly isolated vanilla placeholder until a binary project texture can be committed.
        event.registerEntityRenderer(NightmareCreatureEntities.ASH_BURROWER.get(), AshBurrowerRenderer::new);

        // Remaining vanilla visual placeholders. Java-owned Nightmare Creature identities are not
        // derived from these models and can be replaced independently.
        event.registerEntityRenderer(NightmareCreatureEntities.CHAINBACK.get(), SpiderRenderer::new);
        event.registerEntityRenderer(NightmareCreatureEntities.DROWNED_LISTENER.get(), DrownedRenderer::new);
    }
}
