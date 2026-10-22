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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Tide, Awakened (control): drag nearby mobs toward the spot you look at, and slow them. */
public final class UndertowAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.UNDERTOW.abilityId();
    static final int ESSENCE_COST = 40;
    static final double REACH = 14.0;
    static final double RADIUS = 8.0;
    static final int SLOW_TICKS = 20 * 3;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.UNDERTOW.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 point = pointOf(player);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(RADIUS),
                entity -> entity instanceof Mob && entity.isAlive() && entity.position().distanceTo(point) <= RADIUS)) {
            Vec3 toward = point.subtract(mob.position());
            double distance = toward.length();
            if (distance > 0.5) {
                Vec3 pull = toward.scale(Math.min(1.4, 0.35 + distance * 0.12) / distance);
                mob.setDeltaMovement(pull.x, Math.max(0.15, pull.y), pull.z);
                mob.hurtMarked = true;
            }
            mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOW_TICKS, 0, false, true));
        }
        for (int i = 0; i < 40; i++) {
            double angle = Math.PI * 2.0 * i / 40;
            double r = RADIUS * (0.3 + 0.7 * (i % 4) / 3.0);
            level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, point.x + Math.cos(angle) * r, point.y + 0.2,
                    point.z + Math.sin(angle) * r, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.NAUTILUS, point.x, point.y + 0.5, point.z, 30, 0.5, 0.5, 0.5, 0.5);
        level.playSound(null, point.x, point.y, point.z, SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.PLAYERS, 0.8F, 0.6F);
        return Activation.USED;
    }

    /** Where the crosshair meets a block within reach, or the end of reach. */
    static Vec3 pointOf(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(REACH));
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                .getLocation();
    }
}
