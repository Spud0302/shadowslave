package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reaper's Dormant ability: a beam that drains the creature you look at for
 * three seconds. Each pulse hurts it and feeds its essence to you, so a full
 * drain pays back more than it costs: the Reaper's way around Hunger.
 */
public final class SoulSiphonAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.REAPER.abilityId();
    static final int ESSENCE_COST = 15;
    static final double REACH = 16.0;
    static final double HOLD_RANGE = 20.0;
    static final int DURATION_TICKS = 20 * 3;
    static final int PULSE_TICKS = 10;
    static final float PULSE_DAMAGE = 2.0F;
    static final int PULSE_ESSENCE = 6;

    record Drain(UUID target, long endsAt) {
    }

    private static final Map<UUID, Drain> DRAINS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.REAPER.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity prey = AbilityTargets.lookedAt(player, REACH, AbilityTargets::isHostile);
        if (prey == null) {
            AbilityService.feedback(player, "Look at a hostile creature to drain it.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        DRAINS.put(player.getUUID(), new Drain(prey.getUUID(), level.getGameTime() + DURATION_TICKS));
        level.playSound(null, prey.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 0.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Drain drain = DRAINS.get(player.getUUID());
        if (drain == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        Entity entity = level.getEntity(drain.target());
        if (drain.endsAt() < level.getGameTime() || !(entity instanceof LivingEntity prey) || !prey.isAlive()
                || prey.distanceTo(player) > HOLD_RANGE || !AbilityTargets.canSee(player, prey)) {
            DRAINS.remove(player.getUUID());
            return;
        }
        Vec3 from = prey.position().add(0, prey.getBbHeight() * 0.6, 0);
        Vec3 to = player.position().add(0, player.getBbHeight() * 0.6, 0);
        if (level.getGameTime() % 2 == 0) {
            AbilityTargets.beam(level, ParticleTypes.SCULK_SOUL, from, to);
        }
        if ((drain.endsAt() - level.getGameTime()) % PULSE_TICKS == 0) {
            prey.hurt(level.damageSources().indirectMagic(player, player), PULSE_DAMAGE);
            EssenceService.gain(player, PULSE_ESSENCE);
            level.sendParticles(ParticleTypes.SOUL, from.x, from.y, from.z, 4, 0.2, 0.2, 0.2, 0.02);
            AbilityService.syncStatus(player);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        DRAINS.remove(player.getUUID());
    }

    static boolean draining(ServerPlayer player) {
        return DRAINS.containsKey(player.getUUID());
    }
}
