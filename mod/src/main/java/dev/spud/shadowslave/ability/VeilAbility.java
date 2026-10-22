package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Mist, Awakened (control): a dome of mist where hostile mobs are blind and lose their prey. */
public final class VeilAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.VEIL.abilityId();
    static final int ESSENCE_COST = 45;
    static final double RADIUS = 6.0;
    static final int DURATION_TICKS = 20 * 8;

    record Dome(ResourceKey<Level> dimension, Vec3 centre, long endsAt) {
    }

    private static final Map<UUID, Dome> DOMES = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.VEIL.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        DOMES.put(player.getUUID(), new Dome(level.dimension(), player.position(), level.getGameTime() + DURATION_TICKS));
        level.playSound(null, player.blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 0.8F, 0.7F);
        blind(player, level, player.position());
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Dome dome = DOMES.get(player.getUUID());
        if (dome == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (dome.endsAt() <= level.getGameTime() || !level.dimension().equals(dome.dimension())) {
            DOMES.remove(player.getUUID());
            return;
        }
        if (level.getGameTime() % 5 != 0) {
            return;
        }
        Vec3 c = dome.centre();
        for (int i = 0; i < 24; i++) {
            double theta = level.getRandom().nextDouble() * Math.PI * 2.0;
            double phi = level.getRandom().nextDouble() * Math.PI * 0.5;
            level.sendParticles(ParticleTypes.CLOUD, c.x + Math.cos(theta) * Math.cos(phi) * RADIUS,
                    c.y + Math.sin(phi) * RADIUS, c.z + Math.sin(theta) * Math.cos(phi) * RADIUS, 1, 0.1, 0.1, 0.1, 0.0);
        }
        blind(player, level, c);
    }

    private static void blind(ServerPlayer player, ServerLevel level, Vec3 centre) {
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(centre, centre).inflate(RADIUS),
                entity -> entity instanceof Enemy && entity.isAlive() && entity.position().distanceTo(centre) <= RADIUS)) {
            mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false));
            if (mob instanceof Mob hunter) {
                hunter.setTarget(null);
            }
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        DOMES.remove(player.getUUID());
    }
}
