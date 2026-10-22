package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/**
 * The manifested Glass Road Memory. Its clean-edge strike was part of the
 * advanced combat proof and was dropped with Combat Core; the Memory itself
 * (ownership, summon and dismiss) still works.
 */
public final class GlassRoadMemoryItem extends Item {
    public static final ResourceLocation MEMORY_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/glass_road");
    public static final ResourceLocation CLEAN_EDGE_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory_enchantment/clean_edge");

    public GlassRoadMemoryItem(Properties properties) {
        super(properties);
    }

    static boolean bindsExistingCatalogueDefinition() {
        MemoryContentCatalog.MemoryProfile memory = MemoryContentCatalog.waveOne().memories().stream()
                .filter(profile -> profile.id().equals(MEMORY_ID)).findFirst().orElseThrow();
        return memory.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(CLEAN_EDGE_ID));
    }
}
