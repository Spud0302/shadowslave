package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Mist's Dormant ability: blink a short way where you look and turn briefly invisible, leaving mist behind. */
public final class MistPassageAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.MIST.abilityId();
    static final double RANGE = 8.0;
    static final double MIN_DISTANCE = 1.5;
    static final double STEP_BACK = 0.25;
    static final int INVISIBLE_TICKS = 20 * 3;
    static final int COOLDOWN_TICKS = 20 * 20;
    /** Borrowed Breath: every passage costs this much hunger. */
    static final int HUNGER_COST = 2;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Mist Passage";
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 origin = player.position();
        Vec3 destination = findDestination(player);
        if (destination == null) {
            AbilityService.feedback(player, "No room for the mist to carry you.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }

        leaveMist(level, player, origin);
        trail(level, origin, destination);
        player.teleportTo(destination.x, destination.y, destination.z);
        player.resetFallDistance();
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISIBLE_TICKS, 0, false, false, true));
        level.sendParticles(ParticleTypes.CLOUD, destination.x, destination.y + 1.0, destination.z, 20, 0.3, 0.5, 0.3, 0.02);
        level.playSound(null, destination.x, destination.y, destination.z, SoundEvents.ILLUSIONER_MIRROR_MOVE,
                SoundSource.PLAYERS, 0.7F, 1.4F);

        if (KitRules.hasFlaw(player, AspectKitCatalog.MIST.flawEffectId())) {
            player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - HUNGER_COST));
        }
        return Activation.USED;
    }

    /**
     * Casts along the look direction, stops short of the first block hit, then
     * steps back until the player's body fits. Null when that leaves less than
     * a block and a half of travel.
     */
    static Vec3 findDestination(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(RANGE));
        Vec3 hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
                .getLocation();
        double reach = hit.distanceTo(eye);
        double eyeHeight = player.getEyeHeight();
        for (double distance = reach - 0.3; distance >= MIN_DISTANCE; distance -= STEP_BACK) {
            Vec3 feet = eye.add(look.scale(distance)).subtract(0, eyeHeight, 0);
            AABB body = player.getBoundingBox().move(feet.subtract(player.position()));
            if (player.level().noCollision(player, body)) {
                return feet;
            }
        }
        return null;
    }

    private static void trail(ServerLevel level, Vec3 from, Vec3 to) {
        int steps = Math.max(2, (int) Math.ceil(from.distanceTo(to) * 2.0));
        for (int i = 0; i <= steps; i++) {
            Vec3 at = from.lerp(to, (double) i / steps);
            level.sendParticles(ParticleTypes.WHITE_ASH, at.x, at.y + 1.0, at.z, 3, 0.15, 0.4, 0.15, 0.0);
        }
    }

    private static void leaveMist(ServerLevel level, ServerPlayer player, Vec3 at) {
        AreaEffectCloud cloud = new AreaEffectCloud(level, at.x, at.y, at.z);
        cloud.setOwner(player);
        cloud.setParticle(ParticleTypes.CLOUD);
        cloud.setRadius(1.5F);
        cloud.setDuration(INVISIBLE_TICKS);
        cloud.setRadiusPerTick(-1.5F / INVISIBLE_TICKS);
        cloud.setWaitTime(0);
        level.addFreshEntity(cloud);
    }
}
