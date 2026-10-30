package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import dev.spud.shadowslave.echoes.EchoService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

import java.util.ArrayList;
import java.util.List;

/**
 * Shepherd, Dormant: call the flock. Every summoned Echo and tamed pet of yours
 * nearby runs faster for eight seconds and mends a third of its health.
 */
public final class RallyAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.SHEPHERD.abilityId();
    static final int ESSENCE_COST = 25;
    static final double RADIUS = 32.0;
    static final int SPEED_TICKS = 20 * 8;
    static final float HEAL_SHARE = 0.3F;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.SHEPHERD.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        List<LivingEntity> flock = flock(player);
        if (flock.isEmpty()) {
            AbilityService.feedback(player, "No Echo or pet of yours is near enough to hear.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        for (LivingEntity member : flock) {
            member.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SPEED_TICKS, 1, false, true, true));
            member.heal(member.getMaxHealth() * HEAL_SHARE);
            AbilityTargets.beam(level, ParticleTypes.SOUL, player.position().add(0, 1.2, 0),
                    member.position().add(0, member.getBbHeight() * 0.6, 0));
            level.sendParticles(ParticleTypes.HEART, member.getX(), member.getY() + member.getBbHeight() + 0.3, member.getZ(),
                    4, 0.3, 0.2, 0.3, 0.0);
        }
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.2, player.getZ(), 30, 1.2, 0.1, 1.2, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 0.8F, 1.3F);
        return Activation.USED;
    }

    /** Summoned Echoes and owned pets in this level, within {@link #RADIUS}. */
    static List<LivingEntity> flock(ServerPlayer player) {
        List<LivingEntity> flock = new ArrayList<>();
        for (LivingEntity echo : EchoService.summonedBodies(player)) {
            if (echo.level() == player.level() && echo.distanceToSqr(player) <= RADIUS * RADIUS) {
                flock.add(echo);
            }
        }
        flock.addAll(player.level().getEntitiesOfClass(TamableAnimal.class, player.getBoundingBox().inflate(RADIUS),
                pet -> pet.isAlive() && pet.isOwnedBy(player) && !flock.contains(pet)));
        return flock;
    }
}
