package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.world.entity.AshBurrowerExecutionBinding;
import dev.spud.shadowslave.world.entity.ChainbackExecutionBinding;
import dev.spud.shadowslave.world.entity.DrownedListenerExecutionBinding;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * Runtime integration binding for the already-authored Ash Compass {@code warm_needle} enchantment.
 *
 * <p>The Memory catalogue and creature execution bindings own the stable content identities. The
 * range is Minecraft DESIGN used only to execute that existing content as a bounded player-facing
 * warning; it does not define a canonical essence-detection formula.</p>
 */
public final class AshCompassWarmNeedleBinding {
    public static final ResourceLocation MEMORY_ID =
            ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/ash_compass");
    public static final ResourceLocation ENCHANTMENT_ID =
            ResourceLocation.fromNamespaceAndPath("shadowslave", "memory_enchantment/warm_needle");
    public static final double DETECTION_RANGE = 12.0D;

    private static final Set<String> THREAT_CONTENT_IDS = Set.of(
            AshBurrowerExecutionBinding.CONTENT_ID,
            ChainbackExecutionBinding.CONTENT_ID,
            DrownedListenerExecutionBinding.CONTENT_ID);

    private AshCompassWarmNeedleBinding() {
    }

    public static MemoryContentCatalog.MemoryProfile memoryProfile() {
        MemoryContentCatalog.MemoryProfile profile = MemoryContentCatalog.waveOne().memories().stream()
                .filter(candidate -> candidate.id().equals(MEMORY_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ash Compass is missing from the Memory catalogue"));
        boolean authored = profile.enchantments().stream()
                .anyMatch(enchantment -> enchantment.id().equals(ENCHANTMENT_ID));
        if (!authored) {
            throw new IllegalStateException("Ash Compass no longer authors warm_needle");
        }
        return profile;
    }

    public static Set<String> threatContentIds() {
        return THREAT_CONTENT_IDS;
    }

    public static boolean isThreatContentId(String contentId) {
        return THREAT_CONTENT_IDS.contains(contentId);
    }

    public static boolean detects(double distanceSquared) {
        if (distanceSquared < 0.0D) {
            throw new IllegalArgumentException("distanceSquared cannot be negative");
        }
        return distanceSquared <= DETECTION_RANGE * DETECTION_RANGE;
    }
}
