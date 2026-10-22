package dev.spud.shadowslave.client;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

/** Stand-in item icons for each ability until the hand-drawn icons land. */
final class AbilityIcons {
    private static final Map<String, ItemStack> ICONS = Map.of(
            "shadowslave:generation/ability/kindle", new ItemStack(Items.BLAZE_POWDER),
            "shadowslave:generation/ability/mist_passage", new ItemStack(Items.PHANTOM_MEMBRANE),
            "shadowslave:generation/ability/holdfast", new ItemStack(Items.SHIELD),
            "shadowslave:generation/ability/returning_tide", new ItemStack(Items.HEART_OF_THE_SEA)
    );
    private static final ItemStack FALLBACK = new ItemStack(Items.AMETHYST_SHARD);

    private AbilityIcons() {
    }

    static ItemStack forAbility(String abilityId) {
        return ICONS.getOrDefault(abilityId, FALLBACK);
    }
}
