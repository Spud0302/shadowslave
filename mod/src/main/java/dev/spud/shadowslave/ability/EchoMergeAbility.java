package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;

import java.util.Optional;

/**
 * Vessel, Dormant: fold your summoned Echo into yourself for ten seconds. Its
 * strength, hide and speed become yours, and its trait grows stronger.
 */
public final class EchoMergeAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.VESSEL.abilityId();
    static final int ESSENCE_COST = 40;
    static final int MERGE_TICKS = 20 * 10;
    static final double REACH = 16.0;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.VESSEL.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (VesselBond.merging(player)) {
            AbilityService.feedback(player, "You already carry an Echo inside you.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Optional<Mob> echo = VesselBond.traitSource(player).filter(body -> body.distanceToSqr(player) <= REACH * REACH);
        if (echo.isEmpty()) {
            AbilityService.feedback(player, "No Echo of yours stands near enough to merge with.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        VesselBond.merge(player, echo.get(), MERGE_TICKS);
        return Activation.USED;
    }

    @Override
    public void clear(ServerPlayer player) {
        VesselBond.unmerge(player);
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return VesselBond.merging(player);
    }
}
