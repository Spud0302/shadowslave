package dev.spud.shadowslave.weaving;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A strand pulled out of a piece by a Forger (lore: some Forgers can move an
 * enchantment from one item to another, ch. 2048), held as a loose thread
 * until it is laid into another piece at the Weaving Table. Its strand is in
 * its {@link WeavingRegistry#WOVEN_STRANDS} component; the tooltip names it.
 */
public class WovenThreadItem extends Item {
    public WovenThreadItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        WovenStrands strands = stack.get(WeavingRegistry.WOVEN_STRANDS.get());
        if (strands == null || strands.isEmpty()) {
            return super.getName(stack);
        }
        return Component.translatable("item.shadowslave.woven_thread.named", StrandText.name(strands.strands().getFirst()));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
