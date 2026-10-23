package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.creature.NightmareCreature;
import dev.spud.shadowslave.soul.core.CoreService;
import dev.spud.shadowslave.soul.core.ShardDrops;
import dev.spud.shadowslave.soul.core.ShardRank;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The sleep Lull's abilities put creatures in. A sleeping creature drops its
 * target, stands still and lands no blows (DreamKitPassives cancels them);
 * any damage wakes it. A sleeper being steered (Sleepwalker) walks where its
 * Lull looks instead of standing still, still dreaming.
 */
public final class Slumber {
    /** Rooted in place: a heavy slowness, refreshed while the sleep lasts so it never outlives it by much. */
    private static final int HOLD_TICKS = 10;
    static final double STEER_REACH = 24.0;

    record Sleep(ResourceKey<Level> dimension, long until, Optional<UUID> steerer) {
    }

    private static final Map<UUID, Sleep> SLEEPING = new ConcurrentHashMap<>();

    private Slumber() {
    }

    /** Whether a Lull of this player's rank can put the creature to sleep: a mob, no higher in rank than them. */
    static boolean canLull(ServerPlayer player, LivingEntity target) {
        return target instanceof Mob && !(target instanceof Player) && target.isAlive() && tier(target) <= tier(player);
    }

    /** A creature's rank as a number: Dormant 0, Awakened 1, ... Animals count as Dormant. */
    static int tier(LivingEntity entity) {
        if (entity instanceof NightmareCreature creature) {
            return creature.rank().ordinal();
        }
        return ShardDrops.rankOf(entity).map(ShardRank::tier).orElse(0);
    }

    /** A player's Soul Rank as a number on the same scale. */
    static int tier(ServerPlayer player) {
        return CoreService.rank(player).map(Enum::ordinal).orElse(0);
    }

    static void put(Mob mob, int ticks) {
        SLEEPING.put(mob.getUUID(), new Sleep(mob.level().dimension(), mob.level().getGameTime() + ticks, Optional.empty()));
        mob.setTarget(null);
        mob.getNavigation().stop();
        ServerLevel level = (ServerLevel) mob.level();
        level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getEyeY() + 0.4, mob.getZ(), 8, 0.25, 0.15, 0.25, 0.01);
    }

    /** Lets a Lull steer a sleeper for {@code ticks}; it stays asleep at least that long. */
    static boolean steer(Mob mob, ServerPlayer by, int ticks) {
        Sleep sleep = SLEEPING.get(mob.getUUID());
        if (sleep == null) {
            return false;
        }
        long until = Math.max(sleep.until(), mob.level().getGameTime() + ticks);
        SLEEPING.put(mob.getUUID(), new Sleep(sleep.dimension(), until, Optional.of(by.getUUID())));
        mob.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        return true;
    }

    public static boolean asleep(Entity entity) {
        Sleep sleep = SLEEPING.get(entity.getUUID());
        return sleep != null && sleep.until() > entity.level().getGameTime();
    }

    static boolean steered(Entity entity) {
        Sleep sleep = SLEEPING.get(entity.getUUID());
        return sleep != null && sleep.steerer().isPresent();
    }

    static void wake(Mob mob) {
        if (SLEEPING.remove(mob.getUUID()) != null) {
            mob.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            mob.getNavigation().stop();
        }
    }

    /** Damage wakes a sleeper. */
    public static void onDamaged(LivingDamageEvent.Post event) {
        if (event.getNewDamage() > 0 && event.getEntity() instanceof Mob mob) {
            wake(mob);
        }
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (SLEEPING.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        for (Iterator<Map.Entry<UUID, Sleep>> it = SLEEPING.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Sleep> entry = it.next();
            Sleep sleep = entry.getValue();
            ServerLevel level = server.getLevel(sleep.dimension());
            Entity entity = level == null ? null : level.getEntity(entry.getKey());
            if (!(entity instanceof Mob mob) || !mob.isAlive()) {
                it.remove();
                continue;
            }
            long now = level.getGameTime();
            if (sleep.until() <= now) {
                it.remove();
                mob.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                continue;
            }
            mob.setTarget(null);
            ServerPlayer steerer = sleep.steerer().map(server.getPlayerList()::getPlayer).orElse(null);
            if (steerer != null && steerer.level() == level) {
                if (now % 10 == 0) {
                    Vec3 to = lookPoint(steerer);
                    mob.getNavigation().moveTo(to.x, to.y, to.z, 1.0);
                }
            } else {
                mob.getNavigation().stop();
                mob.setDeltaMovement(0.0, mob.getDeltaMovement().y, 0.0);
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, HOLD_TICKS, 6, false, false));
            }
            if (now % 20 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, mob.getX(), mob.getEyeY() + 0.5, mob.getZ(), 2, 0.2, 0.1, 0.2, 0.005);
            }
        }
    }

    /** The block a player looks at, up to {@link #STEER_REACH} away. */
    static Vec3 lookPoint(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(STEER_REACH));
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getLocation();
    }

    /** Wakes everything, for tests. */
    static void clearAll() {
        SLEEPING.clear();
    }
}
