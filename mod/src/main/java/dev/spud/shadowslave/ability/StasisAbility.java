package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Hourglass's Dormant ability: hold the creature you look at (within
 * {@link #REACH} blocks, no higher in rank than you) out of time for four
 * seconds. It cannot move or strike, but it can be struck.
 */
public final class StasisAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.HOURGLASS.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 16.0;
    static final int HOLD_TICKS = 20 * 4;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.HOURGLASS.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = AbilityTargets.lookedAt(player, REACH, entity -> entity instanceof Mob);
        if (!(target instanceof Mob mob)) {
            AbilityService.feedback(player, "There is nothing there to hold.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        if (!Slumber.canLull(player, mob)) {
            AbilityService.feedback(player, "Its soul runs too fast to hold.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        if (Stasis.held(mob)) {
            AbilityService.feedback(player, "It is already out of time.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        AbilityTargets.beam(level, ParticleTypes.ENCHANT, player.getEyePosition(), mob.position().add(0, mob.getBbHeight() * 0.6, 0));
        Stasis.hold(player, mob, HOLD_TICKS);
        level.playSound(null, mob.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.6F);
        return Activation.USED;
    }
}
