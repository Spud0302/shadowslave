package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/** Reaper, Awakened (area): drain every hostile creature around you at once. */
public final class HarvestAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.HARVEST.abilityId();
    static final int ESSENCE_COST = 40;
    static final double RADIUS = 8.0;
    static final int MAX_TARGETS = 8;
    static final float DAMAGE = 4.0F;
    static final int ESSENCE_PER_TARGET = 6;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.HARVEST.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<LivingEntity> harvested = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS),
                        entity -> AbilityTargets.isHostile(entity) && entity.distanceTo(player) <= RADIUS)
                .stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(player)))
                .limit(MAX_TARGETS)
                .toList();
        Vec3 chest = player.position().add(0, player.getBbHeight() * 0.6, 0);
        for (LivingEntity prey : harvested) {
            prey.hurt(level.damageSources().indirectMagic(player, player), DAMAGE);
            AbilityTargets.beam(level, ParticleTypes.SCULK_SOUL, prey.position().add(0, prey.getBbHeight() * 0.6, 0), chest);
        }
        for (int i = 0; i < 36; i++) {
            double angle = Mth.TWO_PI * i / 36;
            level.sendParticles(ParticleTypes.SOUL, player.getX() + Math.cos(angle) * RADIUS * 0.6, player.getY() + 0.2,
                    player.getZ() + Math.sin(angle) * RADIUS * 0.6, 1, 0.0, 0.05, 0.0, 0.01);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 1.5F, 0.6F);
        level.playSound(null, player.blockPosition(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.PLAYERS, 1.2F, 0.5F);
        EssenceService.gain(player, harvested.size() * ESSENCE_PER_TARGET);
        return Activation.USED;
    }
}
