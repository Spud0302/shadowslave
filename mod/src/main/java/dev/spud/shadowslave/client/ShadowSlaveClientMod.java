package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import dev.spud.shadowslave.weaving.WeavingRegistry;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Loads only on the physical client, keeping rendering classes off servers. */
@Mod(value = ShadowSlaveMod.MOD_ID, dist = Dist.CLIENT)
public final class ShadowSlaveClientMod {
    public ShadowSlaveClientMod(IEventBus modEventBus) {
        modEventBus.addListener(ClientModPayloads::register);
        modEventBus.addListener(ShadowSlaveClientMod::registerGuiLayers);
        modEventBus.addListener(ShadowSlaveClientMod::clientSetup);
        modEventBus.addListener(ShadowSlaveClientMod::registerScreens);
        ShadowSlaveMod.LOGGER.info("Shadow Slave client is loading");
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(AbilityCastAnimations::register);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(WeavingRegistry.WEAVING_MENU.get(), WeavingScreen::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, AbilityHudLayer.ID, new AbilityHudLayer());
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, WardenSightLayer.ID, new WardenSightLayer());
        event.registerBelow(VanillaGuiLayers.HOTBAR, ForewarningLayer.ID, new ForewarningLayer());
        event.registerAbove(VanillaGuiLayers.HOTBAR, FieldGuideMinimapLayer.ID, new FieldGuideMinimapLayer());
    }
}
