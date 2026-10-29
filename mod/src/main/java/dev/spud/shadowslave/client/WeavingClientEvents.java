package dev.spud.shadowslave.client;

import dev.spud.shadowslave.ShadowSlaveMod;
import dev.spud.shadowslave.smithing.ForgedData;
import dev.spud.shadowslave.smithing.SmithingRegistry;
import dev.spud.shadowslave.weaving.BoundTo;
import dev.spud.shadowslave.weaving.MemoryWeave;
import dev.spud.shadowslave.weaving.StrandFit;
import dev.spud.shadowslave.weaving.StrandRules;
import dev.spud.shadowslave.weaving.StrandText;
import dev.spud.shadowslave.weaving.WeavingRegistry;
import dev.spud.shadowslave.weaving.WovenStrands;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Client-only Weaving presentation: the strands laid into a piece, under its grade, and what a Woven Thread holds. */
@EventBusSubscriber(modid = ShadowSlaveMod.MOD_ID, value = Dist.CLIENT)
public final class WeavingClientEvents {
    private WeavingClientEvents() {
    }

    /** After Smithing's grade lines, so a piece reads: grade, smith, then its strands. */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        WovenStrands strands = stack.get(WeavingRegistry.WOVEN_STRANDS.get());
        if (strands == null || strands.isEmpty()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        List<Component> lines = new ArrayList<>();
        if (stack.is(WeavingRegistry.WOVEN_THREAD.get())) {
            strands.strands().forEach(laid -> lines.add(StrandText.line(laid)));
            lines.add(Component.literal("Lay it into a piece at a Weaving Table").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            ForgedData forged = stack.get(SmithingRegistry.FORGED.get());
            int threads = StrandRules.threads(Optional.ofNullable(forged).map(ForgedData::quality), StrandFit.of(stack).isPresent());
            StrandText.describe(strands, threads, lines);
            memoryLines(stack, strands, lines);
        }
        int at = Math.min(tooltip.size(), 1 + graded(stack));
        tooltip.addAll(at, lines);
    }

    /** A Memory says how to dismiss it, and a Bound one says whose it is. */
    private static void memoryLines(ItemStack stack, WovenStrands strands, List<Component> lines) {
        boolean memory = strands.strands().stream().anyMatch(laid -> laid.id().equals(MemoryWeave.MEMORY_WEAVE));
        if (!memory) {
            return;
        }
        lines.add(Component.literal("Memory · " + ClientKeyMappings.TOGGLE_MEMORY.getTranslatedKeyMessage().getString()
                + " to dismiss or summon").withStyle(ChatFormatting.LIGHT_PURPLE));
        if (strands.strands().stream().anyMatch(laid -> laid.id().equals(MemoryWeave.BOUND))) {
            BoundTo bond = stack.get(WeavingRegistry.BOUND_TO.get());
            lines.add(Component.literal(bond == null ? "Bound · bonds to the next hand that holds it" : "Bound to " + bond.name())
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    /** How many lines Smithing put under the name. */
    private static int graded(ItemStack stack) {
        ForgedData forged = stack.get(SmithingRegistry.FORGED.get());
        return forged == null ? 0 : forged.smith().isEmpty() ? 1 : 2;
    }
}
