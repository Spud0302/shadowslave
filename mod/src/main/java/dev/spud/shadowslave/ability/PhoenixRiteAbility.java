package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ember, Ascended (shelter): for a minute, the next blow that would kill you
 * leaves you standing with four hearts, and you burst into flame that throws
 * nearby hostiles back burning.
 */
public final class PhoenixRiteAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.PHOENIX_RITE.abilityId();
    static final int ESSENCE_COST = 60;
    static final int DURATION_TICKS = 20 * 60;
    static final float SURVIVE_HEALTH = 8.0F;
    static final double BURST_RADIUS = 5.0;
    static final int FIRE_SECONDS = 5;

    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.PHOENIX_RITE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (active(player)) {
            AbilityService.feedback(player, "The rite already burns in you.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        ACTIVE_UNTIL.put(player.getUUID(), level.getGameTime() + DURATION_TICKS);
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.3, 0.6, 0.3, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9F, 1.3F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (until <= level.getGameTime()) {
            ACTIVE_UNTIL.remove(player.getUUID());
            AbilityService.feedback(player, "The rite fades unspent.", ChatFormatting.GRAY);
            AbilityService.syncStatus(player);
        } else if (level.getGameTime() % 10 == 0) {
            level.sendParticles(ParticleTypes.SMALL_FLAME, player.getX(), player.getY() + 1.9, player.getZ(), 1, 0.15, 0.05, 0.15, 0.0);
        }
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return active(player);
    }

    @Override
    public void clear(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());
    }

    static boolean active(ServerPlayer player) {
        return ACTIVE_UNTIL.containsKey(player.getUUID());
    }

    /** A killing blow is turned aside once while the rite burns. */
    public static void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !active(player)
                || player.getHealth() - event.getNewDamage() > 0.0F) {
            return;
        }
        ACTIVE_UNTIL.remove(player.getUUID());
        event.setNewDamage(Math.max(0.0F, player.getHealth() - SURVIVE_HEALTH));
        ServerLevel level = player.serverLevel();
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BURST_RADIUS),
                entity -> entity instanceof Enemy && entity.isAlive())) {
            Vec3 away = mob.position().subtract(player.position()).multiply(1, 0, 1).normalize().scale(1.4);
            mob.push(away.x, 0.5, away.z);
            mob.hurtMarked = true;
            mob.igniteForSeconds(FIRE_SECONDS);
        }
        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 120, 1.5, 1.0, 1.5, 0.15);
        level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
        AbilityService.feedback(player, "You rise from the flame.", ChatFormatting.GOLD);
        AbilityService.syncStatus(player);
    }
}
