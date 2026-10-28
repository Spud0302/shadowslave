package dev.spud.shadowslave.client.renderer;

import dev.spud.shadowslave.smithing.BilletData;
import dev.spud.shadowslave.smithing.SmithingRegistry;
import dev.spud.shadowslave.smithing.SmithingRules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/** Whether a piece glows: a billet at dull red or hotter. */
final class SmithingAnvilBlockEntityGlow {
    private SmithingAnvilBlockEntityGlow() {
    }

    static boolean hot(ItemStack stack) {
        BilletData billet = stack.get(SmithingRegistry.BILLET.get());
        return billet != null && !billet.cold() && Minecraft.getInstance().level != null
                && SmithingRules.glow(billet.heatAt(Minecraft.getInstance().level.getGameTime())) != SmithingRules.Glow.COLD;
    }
}
