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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Ember, Awakened (support): pass your fire to the ally you look at, or to yourself. */
public final class CarryTheFlameAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.CARRY_THE_FLAME.abilityId();
    static final int ESSENCE_COST = 40;
    static final double REACH = 12.0;
    static final int REGEN_TICKS = 20 * 8;
    static final int FIRE_RESIST_TICKS = 20 * 30;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.CARRY_THE_FLAME.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = lookedAtAlly(player);
        if (target == null) {
            target = player;
        }
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, REGEN_TICKS, 1, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FIRE_RESIST_TICKS, 0, false, true));
        target.clearFire();

        ServerLevel level = player.serverLevel();
        Vec3 from = player.getEyePosition();
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.6, 0);
        int steps = Math.max(2, (int) (from.distanceTo(to) * 3));
        for (int i = 0; i <= steps; i++) {
            Vec3 at = from.lerp(to, (double) i / steps);
            level.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
        level.sendParticles(ParticleTypes.HEART, to.x, to.y + 0.6, to.z, 4, 0.3, 0.3, 0.3, 0.0);
        level.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6F, 1.6F);
        return Activation.USED;
    }

    /** Another player or one of your tamed animals under the crosshair, within reach. */
    static LivingEntity lookedAtAlly(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(REACH));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end,
                player.getBoundingBox().expandTowards(player.getLookAngle().scale(REACH)).inflate(1.0),
                entity -> entity instanceof LivingEntity living && living.isAlive() && isAlly(player, living));
        return hit != null ? (LivingEntity) hit.getEntity() : null;
    }

    static boolean isAlly(ServerPlayer player, LivingEntity entity) {
        if (entity instanceof Player other) {
            return !other.isSpectator();
        }
        return entity instanceof TamableAnimal pet && pet.isOwnedBy(player);
    }
}
