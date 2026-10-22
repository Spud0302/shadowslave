package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** Mist, Awakened (sight): every creature and player nearby glows through walls for a while. */
public final class ThroughTheHazeAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.THROUGH_THE_HAZE.abilityId();
    static final int ESSENCE_COST = 30;
    static final double RADIUS = 32.0;
    static final int DURATION_TICKS = 20 * 10;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.THROUGH_THE_HAZE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int seen = 0;
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(RADIUS),
                entity -> entity != player && entity.isAlive() && (entity instanceof Mob || entity instanceof Player))) {
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, DURATION_TICKS, 0, false, false));
            seen++;
        }
        level.sendParticles(ParticleTypes.WHITE_ASH, player.getX(), player.getEyeY(), player.getZ(), 60, 3.0, 1.5, 3.0, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 0.7F, 0.6F);
        AbilityService.feedback(player, seen == 1 ? "The haze shows one shape." : "The haze shows " + seen + " shapes.",
                ChatFormatting.GRAY);
        return Activation.USED;
    }
}
