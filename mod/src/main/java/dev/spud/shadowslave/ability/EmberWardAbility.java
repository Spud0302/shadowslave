package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ability.combat.AbilityCombat;
import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Ember, Awakened (control): a ring of fire around where you stood that throws hostile mobs back. */
public final class EmberWardAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.EMBER_WARD.abilityId();
    static final int ESSENCE_COST = 45;
    static final double RADIUS = 4.5;
    static final int DURATION_TICKS = 20 * 8;
    private static final int RING_POINTS = 40;

    record Ward(ResourceKey<Level> dimension, Vec3 centre, long endsAt) {
    }

    /** Live wards. Short-lived, so they are not saved. */
    private static final Map<UUID, Ward> WARDS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.EMBER_WARD.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        WARDS.put(player.getUUID(), new Ward(level.dimension(), player.position(), level.getGameTime() + DURATION_TICKS));
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8F, 0.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Ward ward = WARDS.get(player.getUUID());
        if (ward == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (ward.endsAt() <= level.getGameTime() || !level.dimension().equals(ward.dimension())) {
            WARDS.remove(player.getUUID());
            return;
        }
        if (level.getGameTime() % 4 != 0) {
            return;
        }
        Vec3 centre = ward.centre();
        for (int i = 0; i < RING_POINTS; i++) {
            double angle = Math.PI * 2.0 * i / RING_POINTS + level.getGameTime() * 0.05;
            level.sendParticles(i % 3 == 0 ? ParticleTypes.LAVA : ParticleTypes.FLAME,
                    centre.x + Math.cos(angle) * RADIUS, centre.y + 0.1, centre.z + Math.sin(angle) * RADIUS,
                    1, 0.0, 0.15, 0.0, 0.01);
        }
        AbilityCombat combat = AbilityCombat.get();
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(RADIUS + 1.0, 3.0, RADIUS + 1.0),
                entity -> entity instanceof Enemy && entity.isAlive())) {
            Vec3 offset = mob.position().subtract(centre).multiply(1, 0, 1);
            double distance = offset.length();
            if (distance > RADIUS + 0.8) {
                continue;
            }
            Vec3 out = distance < 1.0e-3 ? new Vec3(1, 0, 0) : offset.scale(1.0 / distance);
            mob.setDeltaMovement(out.x * 0.7, 0.3, out.z * 0.7);
            mob.hurtMarked = true;
            combat.ignite(player, mob, 3);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        WARDS.remove(player.getUUID());
    }

    static boolean active(ServerPlayer player) {
        return WARDS.containsKey(player.getUUID());
    }
}
