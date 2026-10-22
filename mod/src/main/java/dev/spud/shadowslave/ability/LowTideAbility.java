package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/** Tide, Awakened (movement): surge forward on a wave that shoves mobs out of the way. */
public final class LowTideAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.LOW_TIDE.abilityId();
    static final int ESSENCE_COST = 30;
    static final double SURGE = 1.6;
    static final double SHOVE_RADIUS = 3.5;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.LOW_TIDE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        Vec3 direction = flat.lengthSqr() < 1.0e-4 ? Vec3.directionFromRotation(0, player.getYRot()) : flat.normalize();
        player.setDeltaMovement(direction.x * SURGE, 0.3, direction.z * SURGE);
        player.hurtMarked = true;
        player.resetFallDistance();

        ServerLevel level = player.serverLevel();
        Vec3 ahead = player.position().add(direction.scale(2.0));
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().expandTowards(direction.scale(6.0)).inflate(SHOVE_RADIUS, 1.0, SHOVE_RADIUS),
                entity -> entity instanceof Mob && entity.isAlive())) {
            Vec3 away = mob.position().subtract(ahead).multiply(1, 0, 1);
            Vec3 side = new Vec3(-direction.z, 0, direction.x);
            Vec3 push = side.scale(Math.signum(away.dot(side)) == 0 ? 1 : Math.signum(away.dot(side))).add(direction.scale(0.4));
            mob.setDeltaMovement(push.x * 1.1, 0.35, push.z * 1.1);
            mob.hurtMarked = true;
        }
        for (int i = 0; i < 12; i++) {
            Vec3 at = player.position().add(direction.scale(i * 0.6));
            level.sendParticles(ParticleTypes.SPLASH, at.x, at.y + 0.3, at.z, 8, 0.5, 0.2, 0.5, 0.1);
            level.sendParticles(ParticleTypes.BUBBLE_POP, at.x, at.y + 0.6, at.z, 3, 0.4, 0.3, 0.4, 0.05);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED, SoundSource.PLAYERS, 0.8F, 0.8F);
        return Activation.USED;
    }
}
