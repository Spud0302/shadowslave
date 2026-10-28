package dev.spud.shadowslave.smithing;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.ChatFormatting;

import java.util.List;

/**
 * Smith's Hammer: strike a billet on a Smithing Anvil (attack the anvil).
 * It never breaks the anvil it works on.
 */
public final class SmithsHammerItem extends Item {
    public SmithsHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !state.is(SmithingRegistry.SMITHING_ANVIL.get());
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return state.is(SmithingRegistry.SMITHING_ANVIL.get()) ? 0.0F : super.getDestroySpeed(stack, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Strike a billet on a Smithing Anvil in time with the bar.").withStyle(ChatFormatting.GRAY));
    }
}
