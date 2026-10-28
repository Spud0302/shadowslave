package dev.spud.shadowslave.weaving;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.memory.MemoryInstanceData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import dev.spud.shadowslave.smithing.ForgedData;
import dev.spud.shadowslave.smithing.SmithingRegistry;
import dev.spud.shadowslave.smithing.SmithingService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Forging Memories as physical items (DESIGN): a weapon or a piece of armour
 * that names its Memory, its Rank and Tier, and who made it. They are forged
 * at the hearth and anvil now; their enchantments stay sealed until the new
 * Weaving lays strands into finished pieces.
 */
public final class WovenMemoryItems {
    private WovenMemoryItems() {
    }

    public static final class Sword extends SwordItem {
        private final ResourceLocation memoryId;

        public Sword(ResourceLocation memoryId, Tier tier, Properties properties) {
            super(tier, properties);
            this.memoryId = memoryId;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            describe(memoryId, stack, tooltip);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            grade(stack, level);
        }
    }

    public static final class Armour extends ArmorItem {
        private final ResourceLocation memoryId;

        public Armour(ResourceLocation memoryId, Holder<ArmorMaterial> material, Type type, Properties properties) {
            super(material, type, properties);
            this.memoryId = memoryId;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            describe(memoryId, stack, tooltip);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            grade(stack, level);
        }
    }

    /**
     * A piece woven before Smithing existed is given a Fair grade (its stats
     * unchanged) the first time it sits in an inventory, keeping its maker.
     */
    public static void grade(ItemStack stack, Level level) {
        if (level.isClientSide || stack.has(SmithingRegistry.FORGED.get())) {
            return;
        }
        MemoryInstanceData woven = stack.get(WeavingRegistry.WOVEN_MEMORY.get());
        String maker = woven == null ? "" : woven.provenance().replaceFirst("^Woven by ", "");
        SmithingService.stamp(stack, ForgedData.legacy(maker));
    }

    static Optional<MemoryContentCatalog.MemoryProfile> profile(ResourceLocation memoryId) {
        return MemoryContentCatalog.forging().memories().stream().filter(profile -> profile.id().equals(memoryId)).findFirst();
    }

    static void describe(ResourceLocation memoryId, ItemStack stack, List<Component> tooltip) {
        profile(memoryId).ifPresent(profile -> {
            tooltip.add(Component.literal(WeavingRules.rankName(profile.rank()) + " Memory, Tier " + WeavingRules.tierName(profile.tier()))
                    .withStyle(ChatFormatting.AQUA));
            for (MemoryContentCatalog.EnchantmentProfile enchantment : profile.enchantments()) {
                tooltip.add(Component.literal("◇ " + title(enchantment.id().getPath()) + " (sealed)")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        });
        MemoryInstanceData woven = stack.get(WeavingRegistry.WOVEN_MEMORY.get());
        if (woven != null) {
            tooltip.add(Component.literal(woven.provenance()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }

    /** "memory_enchantment/splinter_edge" to "Splinter Edge". */
    static String title(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        StringBuilder out = new StringBuilder();
        for (String word : name.split("_")) {
            if (!word.isEmpty()) {
                out.append(out.isEmpty() ? "" : " ").append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
            }
        }
        return out.toString();
    }
}
