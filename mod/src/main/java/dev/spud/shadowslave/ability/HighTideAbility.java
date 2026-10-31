package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;

/**
 * Tide, Ascended (support): a wave rolls out from you. You and the players it
 * reaches are healed and washed clean of fire and poison; hostiles are thrown
 * back and slowed.
 */
public final class HighTideAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.HIGH_TIDE.abilityId();
    static final int ESSENCE_COST = 50;
    static final double RADIUS = 8.0;
    static final float HEAL = 6.0F;
    static final int SLOW_TICKS = 20 * 3;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.HIGH_TIDE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS),
                candidate -> candidate.isAlive() && candidate.distanceToSqr(player) <= RADIUS * RADIUS)) {
            if (entity instanceof ServerPlayer ally) {
                ally.heal(HEAL);
                ally.clearFire();
                ally.removeEffect(MobEffects.POISON);
                ally.removeEffect(MobEffects.WITHER);
                level.sendParticles(ParticleTypes.HEART, ally.getX(), ally.getY() + 2.0, ally.getZ(), 3, 0.3, 0.2, 0.3, 0.0);
            } else if (entity instanceof Enemy) {
                Vec3 away = entity.position().subtract(player.position()).multiply(1, 0, 1).normalize().scale(1.3);
                entity.push(away.x, 0.35, away.z);
                entity.hurtMarked = true;
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 1));
            }
        }
        for (int ring = 1; ring <= 4; ring++) {
            double r = RADIUS * ring / 4.0;
            for (int i = 0; i < 10 * ring; i++) {
                double angle = Math.PI * 2.0 * i / (10 * ring);
                level.sendParticles(ParticleTypes.SPLASH, player.getX() + Math.cos(angle) * r, player.getY() + 0.3,
                        player.getZ() + Math.sin(angle) * r, 3, 0.1, 0.2, 0.1, 0.1);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 0.6F);
        level.playSound(null, player.blockPosition(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.PLAYERS, 0.6F, 1.2F);
        return Activation.USED;
    }
}
