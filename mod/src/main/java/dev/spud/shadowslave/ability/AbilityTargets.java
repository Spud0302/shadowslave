package dev.spud.shadowslave.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

/** Shared target picking for the abilities that act on what the player looks at. */
final class AbilityTargets {
    private AbilityTargets() {
    }

    static boolean isHostile(LivingEntity entity) {
        return entity instanceof Enemy && entity.isAlive();
    }

    /** The living entity under the crosshair within {@code reach}, not hidden behind blocks. */
    static LivingEntity lookedAt(ServerPlayer player, double reach, Predicate<LivingEntity> filter) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(reach));
        HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level(), player, eye, end,
                player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0),
                entity -> entity instanceof LivingEntity living && living.isAlive() && filter.test(living));
        return hit != null ? (LivingEntity) hit.getEntity() : null;
    }

    /** The ally under the crosshair (another player or your own pet), or the player when there is none. */
    static LivingEntity allyOrSelf(ServerPlayer player, double reach) {
        LivingEntity ally = lookedAt(player, reach, entity -> CarryTheFlameAbility.isAlly(player, entity));
        return ally != null ? ally : player;
    }

    /** Whether a clear line runs from the player's eyes to the target's eyes or chest. */
    static boolean canSee(ServerPlayer player, LivingEntity target) {
        Vec3 eye = player.getEyePosition();
        for (Vec3 point : new Vec3[] {target.getEyePosition(), target.position().add(0, target.getBbHeight() * 0.5, 0)}) {
            if (player.level().clip(new ClipContext(eye, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                    .getType() == HitResult.Type.MISS) {
                return true;
            }
        }
        return false;
    }

    /** A line of particles from one point to another, about three per block. */
    static void beam(net.minecraft.server.level.ServerLevel level, net.minecraft.core.particles.ParticleOptions particle,
                     Vec3 from, Vec3 to) {
        int steps = Math.max(2, (int) (from.distanceTo(to) * 3));
        for (int i = 0; i <= steps; i++) {
            Vec3 at = from.lerp(to, (double) i / steps);
            level.sendParticles(particle, at.x, at.y, at.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }
}
