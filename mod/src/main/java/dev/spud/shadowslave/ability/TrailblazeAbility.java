package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wayfarer, Awakened (movement): for 15 seconds your sprint lays a glowing
 * trail. You run a little faster, and any ally (another player or your pet)
 * who steps on the trail before it fades is hastened too.
 */
public final class TrailblazeAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.TRAILBLAZE.abilityId();
    static final int ESSENCE_COST = 30;
    static final int DURATION_TICKS = 20 * 15;
    /** How long each bit of trail lasts. */
    static final int TRAIL_TICKS = 20 * 5;
    static final int MAX_POINTS = 80;
    static final double TOUCH_RANGE = 1.5;
    static final int HASTE_TICKS = 40;
    private static final Vector3f TRAIL_COLOUR = new Vector3f(1.0F, 0.75F, 0.3F);

    record Point(Vec3 pos, long until) {
    }

    private static final class Trail {
        final long until;
        final Deque<Point> points = new ArrayDeque<>();

        Trail(long until) {
            this.until = until;
        }
    }

    private static final Map<UUID, Trail> TRAILS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.TRAILBLAZE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (active(player)) {
            AbilityService.feedback(player, "Your trail is still burning.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        TRAILS.put(player.getUUID(), new Trail(level.getGameTime() + DURATION_TICKS));
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.5F, 1.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Trail trail = TRAILS.get(player.getUUID());
        if (trail == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        while (!trail.points.isEmpty() && trail.points.peekFirst().until() <= now) {
            trail.points.removeFirst();
        }
        boolean burning = trail.until > now;
        if (!burning && trail.points.isEmpty()) {
            TRAILS.remove(player.getUUID());
            AbilityService.feedback(player, "Your trail fades.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
            return;
        }
        if (burning) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 0, false, false, true));
            if (player.isSprinting() && now % 2 == 0 && player.onGround()) {
                trail.points.addLast(new Point(player.position(), now + TRAIL_TICKS));
                while (trail.points.size() > MAX_POINTS) {
                    trail.points.removeFirst();
                }
            }
        }
        if (now % 4 == 0) {
            DustParticleOptions dust = new DustParticleOptions(TRAIL_COLOUR, 1.4F);
            for (Point point : trail.points) {
                level.sendParticles(dust, point.pos().x, point.pos().y + 0.15, point.pos().z, 1, 0.2, 0.02, 0.2, 0.0);
            }
        }
        if (now % 10 == 0) {
            hasten(player, trail);
        }
    }

    /** Allies standing on the trail run faster for a moment. */
    private static void hasten(ServerPlayer player, Trail trail) {
        for (LivingEntity ally : player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(48.0),
                entity -> entity != player && CarryTheFlameAbility.isAlly(player, entity))) {
            if (onTrail(trail, ally.position())) {
                ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, HASTE_TICKS, 1, false, true, true));
            }
        }
    }

    private static boolean onTrail(Trail trail, Vec3 pos) {
        for (Point point : trail.points) {
            if (point.pos().distanceToSqr(pos) <= TOUCH_RANGE * TOUCH_RANGE) {
                return true;
            }
        }
        return false;
    }

    /** Adds a trail point and hastens whoever stands on the trail, for tests: fake players neither sprint nor tick. */
    static void layAndHasten(ServerPlayer player, Vec3 pos) {
        Trail trail = TRAILS.get(player.getUUID());
        if (trail != null) {
            trail.points.addLast(new Point(pos, player.serverLevel().getGameTime() + TRAIL_TICKS));
            hasten(player, trail);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        TRAILS.remove(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return active(player);
    }

    static boolean active(ServerPlayer player) {
        Trail trail = TRAILS.get(player.getUUID());
        return trail != null && trail.until > player.serverLevel().getGameTime();
    }
}
