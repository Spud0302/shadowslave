package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.client.renderer.CreatureRenderer;
import dev.spud.shadowslave.client.renderer.GateRiftRenderer;
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
        // Project-owned GeckoLib presentation drawn by mod/tools/draw_creatures.py. The Echo
        // shares the hostile Ash Burrower's look. Java-owned state remains authority.
        event.registerEntityRenderer(NightmareCreatureEntities.ASH_BURROWER.get(),
                context -> new CreatureRenderer<>(context, "ash_burrower", 0.5F, true));
        event.registerEntityRenderer(NightmareCreatureEntities.ASH_BURROWER_ECHO.get(),
                context -> new CreatureRenderer<>(context, "ash_burrower", 0.5F, true));
        event.registerEntityRenderer(NightmareCreatureEntities.CHAINBACK.get(),
                context -> new CreatureRenderer<>(context, "chainback", 0.9F, true));
        event.registerEntityRenderer(NightmareCreatureEntities.HOLLOW_MIMIC.get(),
                context -> new CreatureRenderer<>(context, "hollow_mimic", 0.4F, false));
        event.registerEntityRenderer(NightmareCreatureEntities.ASHEN_TYRANT.get(),
                context -> new CreatureRenderer<>(context, "ashen_tyrant", 1.2F, true));
        event.registerEntityRenderer(NightmareCreatureEntities.GATE_RIFT.get(), GateRiftRenderer::new);

        // Remaining vanilla visual placeholder.
        event.registerEntityRenderer(NightmareCreatureEntities.DROWNED_LISTENER.get(), DrownedRenderer::new);
    }
}
