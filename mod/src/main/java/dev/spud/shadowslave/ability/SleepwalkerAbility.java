package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Lull, Awakened (control): take hold of a sleeping creature you look at.
 * For ten seconds it walks, still dreaming, toward wherever you look: off a
 * ledge, into a trap, or away from your friends. Damage still wakes it.
 */
public final class SleepwalkerAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SLEEPWALKER.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 16.0;
    static final int STEER_TICKS = 20 * 10;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SLEEPWALKER.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = AbilityTargets.lookedAt(player, REACH, Slumber::asleep);
        if (!(target instanceof Mob sleeper)) {
            AbilityService.feedback(player, "Look at a sleeping creature to steer it.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Slumber.steer(sleeper, player, STEER_TICKS);
        player.serverLevel().sendParticles(ParticleTypes.NOTE, sleeper.getX(), sleeper.getEyeY() + 0.6, sleeper.getZ(),
                4, 0.3, 0.1, 0.3, 0.0);
        player.serverLevel().playSound(null, sleeper.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 0.8F, 0.7F);
        AbilityService.feedback(player, "It walks where you look.", ChatFormatting.LIGHT_PURPLE);
        return Activation.USED;
    }
}
