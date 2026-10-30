package dev.spud.shadowslave.echoes;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * What a slain creature rarely leaves behind (DESIGN): the echo of it, not
 * yet bound to anyone. A Weaver binds it into an Echo at the Weaving Table's
 * Binding branch. The creature is in its {@link EchoesRegistry#REMNANT_OF}
 * component.
 */
public class EchoRemnantItem extends Item {
    public EchoRemnantItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        EchoBody body = stack.get(EchoesRegistry.REMNANT_OF.get());
        return body == null ? super.getName(stack) : Component.translatable("item.shadowslave.echo_remnant.of", body.name());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        EchoBody body = stack.get(EchoesRegistry.REMNANT_OF.get());
        if (body == null) {
            return;
        }
        tooltip.add(Component.literal(body.rankText()).withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("item.shadowslave.echo_remnant.hint").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
