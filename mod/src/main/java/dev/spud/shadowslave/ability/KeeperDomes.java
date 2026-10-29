package dev.spud.shadowslave.ability;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Keeper's domes: Sanctuary (small, and nothing inside may strike out)
 * and Bulwark (large, and arrows from outside fly back). Inside either, the
 * Keeper and their allies take half harm.
 */
public final class KeeperDomes {
    static final float WARD = 0.5F;
    /** Projectiles this close outside a Bulwark's edge, heading in, are turned back. */
    static final double REFLECT_BAND = 1.5;
    /** Dome colours (rgb); the particle options are made per use so unit tests can load this class without a registry. */
    private static final Vector3f SANCTUARY_LIGHT = new Vector3f(0.85F, 0.92F, 1.0F);
    private static final Vector3f BULWARK_LIGHT = new Vector3f(1.0F, 0.8F, 0.35F);

    /**
     * One dome, keyed by the Keeper who raised it.
     *
     * @param sealed  nobody sheltering inside may hurt anything outside
     * @param reflect projectiles from outside are turned back at the edge
     */
    record Dome(ResourceKey<Level> dimension, Vec3 centre, double radius, long until, boolean sealed, boolean reflect) {
        boolean holds(Entity entity) {
            return entity.level().dimension().equals(dimension)
                    && entity.position().add(0, entity.getBbHeight() / 2, 0).distanceToSqr(centre) <= radius * radius;
        }
    }

    private static final Map<UUID, Dome> DOMES = new ConcurrentHashMap<>();

    private KeeperDomes() {
    }

    static void raise(ServerPlayer keeper, double radius, int ticks, boolean sealed, boolean reflect) {
        ServerLevel level = keeper.serverLevel();
        DOMES.put(keeper.getUUID(), new Dome(level.dimension(), keeper.position(), radius,
                level.getGameTime() + ticks, sealed, reflect));
        level.playSound(null, keeper.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, sealed ? 1.4F : 0.9F);
        draw(level, DOMES.get(keeper.getUUID()), 3);
    }

    static Optional<Dome> dome(ServerPlayer keeper) {
        Dome dome = DOMES.get(keeper.getUUID());
        return dome != null && dome.until() > keeper.serverLevel().getGameTime() ? Optional.of(dome) : Optional.empty();
    }

    static void lower(UUID keeper) {
        DOMES.remove(keeper);
    }

    /** Whether the dome shelters this creature: it is the Keeper or an ally of theirs, and inside. */
    private static boolean shelters(MinecraftServer server, UUID keeperId, Dome dome, Entity entity) {
        if (!(entity instanceof LivingEntity living) || !dome.holds(entity)) {
            return false;
        }
        if (entity.getUUID().equals(keeperId)) {
            return true;
        }
        ServerPlayer keeper = server.getPlayerList().getPlayer(keeperId);
        return keeper != null && CarryTheFlameAbility.isAlly(keeper, living);
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (DOMES.isEmpty() || event.getEntity().getServer() == null) {
            return;
        }
        MinecraftServer server = event.getEntity().getServer();
        long now = event.getEntity().level().getGameTime();
        Entity attacker = event.getSource().getEntity();
        boolean warded = false;
        for (Map.Entry<UUID, Dome> entry : DOMES.entrySet()) {
            Dome dome = entry.getValue();
            if (dome.until() <= now) {
                continue;
            }
            // Sanctuary: those it shelters cannot strike anything outside it.
            if (dome.sealed() && attacker != null && !dome.holds(event.getEntity())
                    && shelters(server, entry.getKey(), dome, attacker)) {
                event.setCanceled(true);
                return;
            }
            if (!warded && shelters(server, entry.getKey(), dome, event.getEntity())) {
                warded = true;
            }
        }
        if (warded) {
            event.setAmount(event.getAmount() * (1.0F - WARD));
        }
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (DOMES.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        for (Iterator<Map.Entry<UUID, Dome>> it = DOMES.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<UUID, Dome> entry = it.next();
            Dome dome = entry.getValue();
            ServerLevel level = server.getLevel(dome.dimension());
            if (level == null || dome.until() <= level.getGameTime()) {
                it.remove();
                if (level != null) {
                    level.playSound(null, net.minecraft.core.BlockPos.containing(dome.centre()), SoundEvents.BEACON_DEACTIVATE,
                            SoundSource.PLAYERS, 0.8F, 1.2F);
                }
                ServerPlayer keeper = server.getPlayerList().getPlayer(entry.getKey());
                if (keeper != null) {
                    AbilityService.syncStatus(keeper);
                }
                continue;
            }
            if (dome.reflect()) {
                reflect(server, entry.getKey(), level, dome);
            }
            if (level.getGameTime() % 4 == 0) {
                draw(level, dome, 1);
            }
        }
    }

    /** Bulwark: projectiles from outside, about to cross the edge inward, fly back the way they came. */
    static void reflect(MinecraftServer server, UUID keeperId, ServerLevel level, Dome dome) {
        double outer = dome.radius() + REFLECT_BAND;
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, new AABB(dome.centre(), dome.centre()).inflate(outer))) {
            double distance = projectile.position().distanceTo(dome.centre());
            Vec3 motion = projectile.getDeltaMovement();
            Vec3 inward = dome.centre().subtract(projectile.position());
            if (distance < dome.radius() || distance > outer || motion.dot(inward) <= 0
                    || (projectile.getOwner() != null && shelters(server, keeperId, dome, projectile.getOwner()))) {
                continue;
            }
            projectile.setDeltaMovement(motion.scale(-1.0));
            projectile.hasImpulse = true;
            projectile.setOwner(server.getPlayerList().getPlayer(keeperId));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, projectile.getX(), projectile.getY(), projectile.getZ(), 6, 0.1, 0.1, 0.1, 0.1);
            level.playSound(null, projectile.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8F, 1.2F);
        }
    }

    /** A shell of light points over the upper half of the dome. */
    static void draw(ServerLevel level, Dome dome, int density) {
        ParticleOptions particle = new DustParticleOptions(dome.sealed() ? SANCTUARY_LIGHT : BULWARK_LIGHT, 0.9F);
        int points = (int) (dome.radius() * dome.radius() * 1.5) * density;
        double golden = Math.PI * (3.0 - Math.sqrt(5.0));
        for (int i = 0; i < points; i++) {
            double y = (double) i / points;
            double ring = Math.sqrt(1.0 - y * y);
            double angle = golden * i + level.getGameTime() * 0.05;
            level.sendParticles(particle, dome.centre().x + Math.cos(angle) * ring * dome.radius(),
                    dome.centre().y + y * dome.radius(), dome.centre().z + Math.sin(angle) * ring * dome.radius(),
                    1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        DOMES.clear();
    }
}
