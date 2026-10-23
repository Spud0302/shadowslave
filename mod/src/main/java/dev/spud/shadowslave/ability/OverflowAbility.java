package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wellspring, Awakened (support): for 20 seconds the well runs over around
 * where you stood. You and your allies inside regenerate health, and every
 * player inside gains essence each second.
 */
public final class OverflowAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.OVERFLOW.abilityId();
    static final int ESSENCE_COST = 50;
    static final double RADIUS = 8.0;
    static final int DURATION_TICKS = 20 * 20;
    static final int ESSENCE_PER_SECOND = 3;

    record Aura(ResourceKey<Level> dimension, Vec3 centre, long endsAt) {
    }

    private static final Map<UUID, Aura> AURAS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.OVERFLOW.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        AURAS.put(player.getUUID(), new Aura(level.dimension(), player.position(), level.getGameTime() + DURATION_TICKS));
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.2F);
        level.playSound(null, player.blockPosition(), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.PLAYERS, 1.0F, 0.8F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Aura aura = AURAS.get(player.getUUID());
        if (aura == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (aura.endsAt() <= level.getGameTime() || !level.dimension().equals(aura.dimension())) {
            AURAS.remove(player.getUUID());
            return;
        }
        Vec3 c = aura.centre();
        if (level.getGameTime() % 4 == 0) {
            for (int i = 0; i < 20; i++) {
                double angle = Mth.TWO_PI * i / 20 + level.getGameTime() * 0.05;
                level.sendParticles(ParticleTypes.WAX_OFF, c.x + Math.cos(angle) * RADIUS, c.y + 0.2, c.z + Math.sin(angle) * RADIUS,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, c.x, c.y + 0.5, c.z, 3, RADIUS * 0.4, 0.3, RADIUS * 0.4, 0.0);
        }
        if ((aura.endsAt() - level.getGameTime()) % 20 != 0) {
            return;
        }
        for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(RADIUS),
                entity -> entity.isAlive() && entity.position().distanceTo(c) <= RADIUS
                        && (entity == player || CarryTheFlameAbility.isAlly(player, entity)))) {
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50, 0, false, true));
            if (ally instanceof ServerPlayer other) {
                EssenceService.gain(other, ESSENCE_PER_SECOND);
                AbilityService.syncStatus(other);
            }
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        AURAS.remove(player.getUUID());
    }

    static boolean active(ServerPlayer player) {
        return AURAS.containsKey(player.getUUID());
    }
}
