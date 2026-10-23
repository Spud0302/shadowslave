package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Fury, Awakened (escape): burn all your rage at once. You heal a point for
 * every ten rage and run fast for a few seconds. It needs at least
 * {@link #MIN_RAGE} rage to light.
 */
public final class SecondWindAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SECOND_WIND.abilityId();
    static final int ESSENCE_COST = 30;
    static final float MIN_RAGE = 20.0F;
    static final float RAGE_PER_HEALTH = 10.0F;
    static final int SPRINT_TICKS = 20 * 5;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SECOND_WIND.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        float rage = FuryRage.rage(player);
        if (rage < MIN_RAGE) {
            AbilityService.feedback(player, "There is not enough rage in you to burn.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        player.heal(rage / RAGE_PER_HEALTH);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SPRINT_TICKS, 1, false, true, true));
        FuryRage.set(player, 0.0F, false);
        level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(), (int) (rage / 20), 0.4, 0.4, 0.4, 0.0);
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.2, player.getZ(), 20, 0.4, 0.1, 0.4, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 1.0F, 0.8F);
        return Activation.USED;
    }
}
