package dev.spud.shadowslave.weaving;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * How strands read on an item, the same on client and server: a strand's
 * name comes from the lang file ({@code strand.<ns>.<path>}), falling back to
 * its id in title case so a data-pack strand still reads well.
 */
public final class StrandText {
    private StrandText() {
    }

    public static Component name(ResourceLocation id) {
        return Component.translatableWithFallback("strand." + id.getNamespace() + "." + id.getPath(), WovenMemoryItems.title(id.getPath()));
    }

    /** "◆ Dragging Links II · 82%" with a tuned strand's condition after it. */
    public static MutableComponent line(WovenStrands.Laid laid) {
        MutableComponent line = Component.literal("◆ ").append(name(laid.id())).append(" " + WeavingRules.tierName(laid.tier()));
        if (laid.potency() < 100) {
            line.append(" · " + laid.potency() + "%");
        }
        laid.restriction().ifPresent(restriction -> line.append(" · " + restriction.label()));
        return line.withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    /** The tooltip lines for a piece's threads and strands. */
    public static void describe(WovenStrands strands, int threads, List<Component> tooltip) {
        if (threads > 0) {
            tooltip.add(Component.literal("Threads: " + strands.size() + "/" + threads).withStyle(ChatFormatting.DARK_PURPLE));
        }
        java.util.LinkedHashSet<String> weavers = new java.util.LinkedHashSet<>();
        for (WovenStrands.Laid laid : strands.strands()) {
            tooltip.add(line(laid));
            if (!laid.weaver().isEmpty()) {
                weavers.add(laid.weaver());
            }
        }
        if (!weavers.isEmpty()) {
            tooltip.add(Component.literal("Woven by " + String.join(", ", weavers)).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}
