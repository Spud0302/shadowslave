package dev.spud.shadowslave.item;

import dev.spud.shadowslave.soul.core.CoreRules;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.core.ShardRank;
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
 * A soul shard of one creature rank. Use it to absorb it into your core;
 * sneak while using to absorb the whole stack, up to what the core can hold.
 */
public class SoulShardItem extends Item {
    private final ShardRank rank;

    public SoulShardItem(ShardRank rank, Properties properties) {
        super(properties);
        this.rank = rank;
    }

    public ShardRank rank() {
        return rank;
    }

    /** Saturation units one shard of this stack carries before the core's rank is taken into account. */
    public static int units(ItemStack stack, ShardRank rank) {
        ShardContents contents = stack.get(ModDataComponents.SHARD_CONTENTS.get());
        if (contents != null) {
            return contents.units();
        }
        return isFaint(stack) ? CoreRules.faintUnits(rank) : rank.value();
    }

    /** A shard from an ordinary mob rather than a Nightmare Creature. */
    public static boolean isFaint(ItemStack stack) {
        return stack.has(ModDataComponents.FAINT_SHARD.get());
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);
        return isFaint(stack) ? Component.translatable("item.shadowslave.faint_shard", name) : name;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }
        int absorbed = CoreService.absorbFromStack(serverPlayer, stack, rank, player.isSecondaryUseActive());
        return absorbed > 0 ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ShardContents contents = stack.get(ModDataComponents.SHARD_CONTENTS.get());
        if (contents != null) {
            tooltip.add(Component.literal("Torn from " + contents.origin()).withStyle(ChatFormatting.DARK_RED));
        }
        if (isFaint(stack)) {
            tooltip.add(Component.literal("From an ordinary creature: a quarter of a true shard,")
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("and nothing to an Ascended core or higher").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.literal("Use to absorb · sneak to absorb all").withStyle(ChatFormatting.DARK_GRAY));
    }
}
