package dev.spud.shadowslave.fieldguide;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Field Guide (DESIGN, Jack 2026-10-23): every player starts with one and
 * it can be crafted again. Using it opens the guide; held in the off hand it
 * shows a corner map. What it knows belongs to the player, so any copy reads
 * the same pages.
 */
public final class FieldGuideItem extends Item {
    public FieldGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            FieldGuideService.open(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shadowslave.field_guide.tooltip").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.shadowslave.field_guide.tooltip_offhand").withStyle(ChatFormatting.DARK_GRAY));
    }
}
