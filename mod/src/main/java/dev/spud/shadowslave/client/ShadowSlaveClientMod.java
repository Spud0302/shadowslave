package dev.spud.shadowslave.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import dev.spud.shadowslave.ShadowSlaveMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import dev.spud.shadowslave.smithing.SmithingRegistry;
import dev.spud.shadowslave.weaving.WeavingRegistry;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/** Loads only on the physical client, keeping rendering classes off servers. */
@Mod(value = ShadowSlaveMod.MOD_ID, dist = Dist.CLIENT)
public final class ShadowSlaveClientMod {
    public ShadowSlaveClientMod(IEventBus modEventBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SpellClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(ShadowSlaveClientMod::onConfigReloaded);
        modEventBus.addListener(ClientModPayloads::register);
        modEventBus.addListener(ShadowSlaveClientMod::registerGuiLayers);
        modEventBus.addListener(ShadowSlaveClientMod::clientSetup);
        modEventBus.addListener(ShadowSlaveClientMod::registerScreens);
        ShadowSlaveMod.LOGGER.info("Shadow Slave client is loading");
    }

    private static void onConfigReloaded(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SpellClientConfig.SPEC) {
            SpellClientConfig.sync();
        }
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(AbilityCastAnimations::register);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(WeavingRegistry.WEAVING_MENU.get(), WeavingScreen::new);
        event.register(SmithingRegistry.FORGE_HEARTH_MENU.get(), ForgeHearthScreen::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, AbilityHudLayer.ID, new AbilityHudLayer());
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, WardenSightLayer.ID, new WardenSightLayer());
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, SmithingHudLayer.ID, new SmithingHudLayer());
        event.registerBelow(VanillaGuiLayers.HOTBAR, ForewarningLayer.ID, new ForewarningLayer());
        event.registerAbove(VanillaGuiLayers.HOTBAR, FieldGuideMinimapLayer.ID, new FieldGuideMinimapLayer());
        event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, CallingLayer.ID, new CallingLayer());
    }
}
