package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Reaper, Awakened (force): a wide scythe sweep in front of you. Creatures
 * left badly wounded by it are reaped outright and feed you essence; a
 * Tyrant or anything else with a deep well of health only takes the cut.
 */
public final class ReapingArcAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.REAPING_ARC.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 4.5;
    /** Cosine of half the arc's width: 0.5 is a 120-degree sweep. */
    static final double ARC_COS = 0.5;
    static final float DAMAGE = 6.0F;
    static final float REAP_BELOW = 0.3F;
    static final float REAP_MAX_HEALTH = 100.0F;
    static final int ESSENCE_PER_REAP = 10;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.REAPING_ARC.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle().multiply(1, 0, 1).normalize();
        int reaped = 0;
        for (LivingEntity prey : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(REACH),
                entity -> AbilityTargets.isHostile(entity) && inArc(player.position(), look, entity.position()))) {
            prey.hurt(level.damageSources().playerAttack(player), DAMAGE);
            if (prey.isAlive() && reapable(prey.getHealth(), prey.getMaxHealth())) {
                level.sendParticles(ParticleTypes.SOUL, prey.getX(), prey.getY() + prey.getBbHeight() * 0.5, prey.getZ(),
                        12, 0.3, 0.4, 0.3, 0.05);
                // The sweep has just hurt it; without this the finishing cut lands inside its hurt cooldown and is lost.
                prey.invulnerableTime = 0;
                prey.hurt(level.damageSources().indirectMagic(player, player), prey.getHealth() + 1.0F);
                reaped++;
            }
        }
        for (int i = -3; i <= 3; i++) {
            double angle = Math.toRadians(i * 18.0);
            Vec3 dir = new Vec3(look.x * Math.cos(angle) - look.z * Math.sin(angle), 0, look.x * Math.sin(angle) + look.z * Math.cos(angle));
            Vec3 at = player.position().add(dir.scale(REACH * 0.7)).add(0, 1.0, 0);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2F, 0.6F);
        if (reaped > 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 0.8F);
            EssenceService.gain(player, reaped * ESSENCE_PER_REAP);
        }
        return Activation.USED;
    }

    static boolean inArc(Vec3 origin, Vec3 flatLook, Vec3 target) {
        Vec3 to = target.subtract(origin).multiply(1, 0, 1);
        double distance = to.length();
        if (distance > REACH) {
            return false;
        }
        return distance < 0.75 || to.normalize().dot(flatLook) >= ARC_COS;
    }

    /** Badly wounded and not too mighty: under 30% health with at most 100 max health. */
    static boolean reapable(float health, float maxHealth) {
        return maxHealth <= REAP_MAX_HEALTH && health <= maxHealth * REAP_BELOW;
    }
}
