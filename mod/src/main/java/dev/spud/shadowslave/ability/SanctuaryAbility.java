package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Keeper's Dormant ability: a small dome of light where you stand. For six
 * seconds you and your allies inside take half harm, but nothing sheltering
 * in it can strike anything outside.
 */
public final class SanctuaryAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.KEEPER.abilityId();
    static final int ESSENCE_COST = 35;
    static final double RADIUS = 4.0;
    static final int DURATION_TICKS = 20 * 6;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.KEEPER.abilityName();
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
        KeeperDomes.raise(player, RADIUS, DURATION_TICKS, true, false);
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
