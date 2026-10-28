package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.client.renderer.SmithingAnvilRenderer;
import dev.spud.shadowslave.smithing.BilletData;
import dev.spud.shadowslave.smithing.ForgedData;
import dev.spud.shadowslave.smithing.SmithingRegistry;
import dev.spud.shadowslave.smithing.SmithingRules;
import dev.spud.shadowslave.smithing.SmithingService;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/** Client-only Smithing presentation: the anvil's piece, a billet's glow, and a finished piece's grade in its tooltip. */
public final class SmithingClientEvents {
    private SmithingClientEvents() {
    }

    @EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        private ModBus() {
        }

        @SubscribeEvent
        public static void clientSetup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
            SmithingService.clientTime = () -> Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
            // Cold work (hide, cloth) shows as a tied bundle instead of a metal bar.
            event.enqueueWork(() -> ItemProperties.register(SmithingRegistry.BILLET_ITEM.get(),
                    ResourceLocation.fromNamespaceAndPath(ShadowSlaveMod.MOD_ID, "cold_work"), (stack, level, entity, seed) -> {
                        BilletData billet = stack.get(SmithingRegistry.BILLET.get());
                        return billet != null && billet.cold() ? 1.0F : 0.0F;
                    }));
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(SmithingRegistry.SMITHING_ANVIL_ENTITY.get(), SmithingAnvilRenderer::new);
        }

        /** The billet's hot layer takes its glow colour; cold it is plain iron grey. */
        @SubscribeEvent
        public static void registerItemColours(RegisterColorHandlersEvent.Item event) {
            event.register((stack, layer) -> {
                BilletData billet = stack.get(SmithingRegistry.BILLET.get());
                if (layer != 0 || billet == null || billet.cold()) {
                    return 0xFFFFFFFF;
                }
                SmithingRules.Glow glow = SmithingRules.glow(billet.heatAt(SmithingService.clientTime()));
                int rgb = glow == SmithingRules.Glow.COLD ? 0x8C8C8C : glow.colour();
                return FastColor.ARGB32.opaque(rgb);
            }, SmithingRegistry.BILLET_ITEM.get());
        }
    }

    @EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT)
    public static final class GameBus {
        private GameBus() {
        }

        @SubscribeEvent
        public static void onTooltip(ItemTooltipEvent event) {
            ForgedData forged = event.getItemStack().get(SmithingRegistry.FORGED.get());
            if (forged == null) {
                return;
            }
            List<Component> tooltip = event.getToolTip();
            int at = Math.min(1, tooltip.size());
            String threads = forged.threads() + (forged.threads() == 1 ? " thread" : " threads");
            tooltip.add(at, Component.literal(forged.quality().displayName() + " · " + threads).withStyle(forged.quality().colour()));
            if (!forged.smith().isEmpty()) {
                tooltip.add(at + 1, Component.literal("Forged by " + forged.smith()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }
        }
    }
}
