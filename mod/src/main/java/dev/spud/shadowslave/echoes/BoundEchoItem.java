package dev.spud.shadowslave.echoes;

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
import java.util.Locale;

/**
 * A bound Echo held outside a soul, so it can be handed to another player
 * (lore: Echoes can be transferred, ch. 47). Use it to take it into your soul.
 * The Echo itself is in the {@link EchoesRegistry#ECHO} component.
 */
public class BoundEchoItem extends Item {
    public BoundEchoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer server) {
            EchoService.absorb(server, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Component getName(ItemStack stack) {
        EchoRecord echo = stack.get(EchoesRegistry.ECHO.get());
        return echo == null ? super.getName(stack) : Component.literal(echo.name());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        EchoRecord echo = stack.get(EchoesRegistry.ECHO.get());
        if (echo == null) {
            return;
        }
        tooltip.add(Component.literal(echo.body().rankText() + " Echo").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.shadowslave.bound_echo.health",
                String.format(Locale.ROOT, "%.0f", echo.health()), String.format(Locale.ROOT, "%.0f", echo.body().maxHealth()))
                .withStyle(ChatFormatting.GRAY));
        if (!echo.binder().isEmpty()) {
            tooltip.add(Component.translatable("item.shadowslave.bound_echo.binder", echo.binder()).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("item.shadowslave.bound_echo.hint").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
