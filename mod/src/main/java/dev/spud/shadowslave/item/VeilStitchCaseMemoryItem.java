package dev.spud.shadowslave.item;

import dev.spud.shadowslave.content.memory.MemoryContentCatalog;
import dev.spud.shadowslave.memory.MemoryOwnershipService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Physical executor for the authored Veil-Stitch Case Memory. */
public final class VeilStitchCaseMemoryItem extends Item {
    public static final ResourceLocation MEMORY_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory/veil_stitch_case");
    public static final ResourceLocation QUIET_MENDING_ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "memory_enchantment/quiet_mending");
    static final double COMBAT_CLEARANCE = 8.0D;
    static final int REPAIR_AMOUNT = 16;

    public VeilStitchCaseMemoryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (!MemoryOwnershipService.owns(serverPlayer, MEMORY_ID)) {
            serverPlayer.sendSystemMessage(Component.literal("The imitation contains no thread bound to your soul.").withStyle(ChatFormatting.DARK_GRAY));
            return InteractionResultHolder.fail(stack);
        }

        ItemStack target = serverPlayer.getOffhandItem();
        if (!isRepairableTarget(target)) {
            serverPlayer.sendSystemMessage(Component.literal("Hold damaged ordinary equipment in your off hand.").withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }
        if (!isQuietEnough(level, serverPlayer)) {
            serverPlayer.sendSystemMessage(Component.literal("Veil-Stitch will not mend while danger presses close.").withStyle(ChatFormatting.RED));
            return InteractionResultHolder.fail(stack);
        }

        int repaired = Math.min(REPAIR_AMOUNT, target.getDamageValue());
        target.setDamageValue(target.getDamageValue() - repaired);
        serverPlayer.sendSystemMessage(Component.literal("Veil-Stitch quietly closes " + repaired + " points of wear.").withStyle(ChatFormatting.AQUA));
        serverPlayer.getCooldowns().addCooldown(this, 100);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    static boolean isRepairableTarget(ItemStack target) {
        return !target.isEmpty() && target.isDamageableItem() && target.isDamaged();
    }

    static boolean isQuietEnough(Level level, Player player) {
        return level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(COMBAT_CLEARANCE), entity -> entity.isAlive()).isEmpty();
    }

    static boolean bindsExistingCatalogueDefinition() {
        MemoryContentCatalog.MemoryProfile memory = MemoryContentCatalog.waveOne().memories().stream()
                .filter(profile -> profile.id().equals(MEMORY_ID))
                .findFirst()
                .orElseThrow();
        return memory.enchantments().stream().anyMatch(enchantment -> enchantment.id().equals(QUIET_MENDING_ID));
    }
}
