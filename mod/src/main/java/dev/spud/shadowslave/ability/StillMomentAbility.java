package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;

import java.util.List;

/**
 * Hourglass, Awakened (control): every hostile within {@link #RADIUS} blocks
 * that is no higher in rank than you is held out of time for two seconds.
 */
public final class StillMomentAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.STILL_MOMENT.abilityId();
    static final int ESSENCE_COST = 60;
    static final double RADIUS = 8.0;
    static final int HOLD_TICKS = 20 * 2;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.STILL_MOMENT.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<Mob> caught = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS),
                mob -> AbilityTargets.isHostile(mob) && Slumber.canLull(player, mob) && !Stasis.held(mob));
        if (caught.isEmpty()) {
            AbilityService.feedback(player, "There is nothing here to hold.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        caught.forEach(mob -> Stasis.hold(player, mob, HOLD_TICKS));
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 80, RADIUS / 2, 1.0, RADIUS / 2, 0.5);
        level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.0F, 0.8F);
        return Activation.USED;
    }
}
