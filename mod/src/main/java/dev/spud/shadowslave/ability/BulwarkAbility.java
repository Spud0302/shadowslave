package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Keeper, Awakened (shelter): a great dome where you stand. For ten seconds
 * you and your allies inside take half harm, and arrows and other
 * projectiles loosed from outside fly back at the edge. Unlike Sanctuary,
 * you may fight out of it.
 */
public final class BulwarkAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.BULWARK.abilityId();
    static final int ESSENCE_COST = 50;
    static final double RADIUS = 7.0;
    static final int DURATION_TICKS = 20 * 10;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.BULWARK.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (KeeperDomes.dome(player).isPresent()) {
            AbilityService.feedback(player, "Your dome still stands.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        KeeperDomes.raise(player, RADIUS, DURATION_TICKS, false, true);
        return Activation.USED;
    }

    @Override
    public void clear(ServerPlayer player) {
        KeeperDomes.lower(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return KeeperDomes.dome(player).isPresent();
    }
}
