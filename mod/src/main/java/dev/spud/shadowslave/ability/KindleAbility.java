package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ability.combat.AbilityCombat;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;

/** Ember's Dormant ability: a burst of light that speeds you up and sets nearby hostiles alight. */
public final class KindleAbility implements AspectAbility {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("shadowslave", "generation/ability/kindle");
    static final int DURATION_TICKS = 20 * 10;
    static final int COOLDOWN_TICKS = 20 * 30;
    static final double IGNITE_RADIUS = 4.0;
    static final int IGNITE_SECONDS = 3;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Kindle";
    }

    @Override
    public int cooldownTicks() {
        return COOLDOWN_TICKS;
    }

    @Override
    public boolean activate(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, DURATION_TICKS, 0, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_TICKS, 0, false, true));

        ServerLevel level = player.serverLevel();
        AbilityCombat combat = AbilityCombat.get();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(IGNITE_RADIUS),
                entity -> entity instanceof Enemy && entity.isAlive()
                        && entity.distanceToSqr(player) <= IGNITE_RADIUS * IGNITE_RADIUS)) {
            combat.ignite(player, target, IGNITE_SECONDS);
        }

        level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(),
                40, 0.6, 0.6, 0.6, 0.08);
        level.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8F, 1.3F);
        return true;
    }
}
