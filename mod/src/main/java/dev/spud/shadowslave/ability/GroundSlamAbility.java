package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Fury, Awakened (force): slam the ground. Hostiles within {@link #RADIUS}
 * blocks are hurt and thrown up, harder the more rage you hold, and the slam
 * spends half of it.
 */
public final class GroundSlamAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.GROUND_SLAM.abilityId();
    static final int ESSENCE_COST = 35;
    static final double RADIUS = 5.0;
    static final float BASE_DAMAGE = 4.0F;
    static final float RAGE_DAMAGE = 6.0F;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.GROUND_SLAM.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        float share = FuryRage.rage(player) / FuryRage.MAX;
        float damage = damageAt(share);
        for (LivingEntity foe : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS, 2.0, RADIUS),
                entity -> entity != player && AbilityTargets.isHostile(entity))) {
            foe.hurt(level.damageSources().indirectMagic(player, player), damage);
            Vec3 away = foe.position().subtract(player.position()).multiply(1, 0, 1);
            Vec3 push = away.lengthSqr() < 1.0e-4 ? Vec3.ZERO : away.normalize().scale(0.4 + 0.4 * share);
            foe.setDeltaMovement(push.x, 0.6 + 0.4 * share, push.z);
            foe.hurtMarked = true;
            foe.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, true));
        }
        BlockState ground = level.getBlockState(player.getOnPos());
        if (!ground.isAir()) {
            for (int i = 0; i < 24; i++) {
                float angle = i * Mth.TWO_PI / 24;
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), player.getX() + Mth.cos(angle) * 2.5,
                        player.getY() + 0.1, player.getZ() + Mth.sin(angle) * 2.5, 6, 0.4, 0.1, 0.4, 0.15);
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 0.2, player.getZ(), 3, 1.5, 0.1, 1.5, 0.0);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7F, 0.7F);
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 0.5F);
        FuryRage.set(player, FuryRage.rage(player) / 2.0F, true);
        return Activation.USED;
    }

    static float damageAt(float rageShare) {
        return BASE_DAMAGE + RAGE_DAMAGE * rageShare;
    }
}
