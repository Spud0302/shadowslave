package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ability.combat.AbilityCombat;
import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ember, Ascended (force): fire rains on the spot you look at for four
 * seconds; hostiles under it are burned again and again.
 */
public final class FirestormAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.FIRESTORM.abilityId();
    static final int ESSENCE_COST = 55;
    static final double RADIUS = 4.0;
    static final int DURATION_TICKS = 20 * 4;
    static final int PULSE_TICKS = 10;
    static final float DAMAGE = 2.0F;
    static final int FIRE_SECONDS = 3;

    private record Storm(Vec3 centre, long until) {
    }

    private static final Map<UUID, Storm> STORMS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.FIRESTORM.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        Vec3 centre = UndertowAbility.pointOf(player);
        ServerLevel level = player.serverLevel();
        STORMS.put(player.getUUID(), new Storm(centre, level.getGameTime() + DURATION_TICKS));
        level.playSound(null, centre.x, centre.y, centre.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Storm storm = STORMS.get(player.getUUID());
        if (storm == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        if (now >= storm.until()) {
            STORMS.remove(player.getUUID());
            return;
        }
        Vec3 c = storm.centre();
        for (int i = 0; i < 6; i++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(level.getRandom().nextDouble()) * RADIUS;
            level.sendParticles(ParticleTypes.FLAME, c.x + Math.cos(angle) * r, c.y + 4.0, c.z + Math.sin(angle) * r,
                    0, 0.0, -1.0, 0.0, 0.5);
        }
        if (now % PULSE_TICKS != 0) {
            return;
        }
        level.sendParticles(ParticleTypes.LAVA, c.x, c.y + 0.2, c.z, 4, RADIUS * 0.5, 0.1, RADIUS * 0.5, 0.0);
        AbilityCombat combat = AbilityCombat.get();
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(RADIUS, 3.0, RADIUS),
                entity -> entity instanceof Enemy && entity.isAlive() && entity.position().distanceTo(c) <= RADIUS + 0.5)) {
            combat.damage(player, mob, DAMAGE);
            mob.igniteForSeconds(FIRE_SECONDS);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        STORMS.remove(player.getUUID());
    }
}
