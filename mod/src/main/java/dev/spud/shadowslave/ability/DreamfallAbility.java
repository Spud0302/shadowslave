package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;

import java.util.List;

/**
 * Lull, Awakened (control): every hostile creature within {@link #RADIUS}
 * blocks whose rank is no higher than yours falls asleep for four seconds.
 */
public final class DreamfallAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.DREAMFALL.abilityId();
    static final int ESSENCE_COST = 45;
    static final double RADIUS = 6.0;
    static final int SLEEP_TICKS = 20 * 4;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.DREAMFALL.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<Mob> sleepers = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS),
                mob -> AbilityTargets.isHostile(mob) && mob.distanceToSqr(player) <= RADIUS * RADIUS && Slumber.canLull(player, mob));
        if (sleepers.isEmpty()) {
            AbilityService.feedback(player, "Nothing near you to lull.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        sleepers.forEach(mob -> Slumber.put(mob, SLEEP_TICKS));
        for (int i = 0; i < 48; i++) {
            double angle = i * Math.PI * 2 / 48;
            level.sendParticles(ParticleTypes.END_ROD, player.getX() + Math.cos(angle) * RADIUS * 0.8, player.getY() + 0.3,
                    player.getZ() + Math.sin(angle) * RADIUS * 0.8, 1, 0.0, 0.2, 0.0, 0.0);
        }
        level.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 2.0, player.getZ(), 6, 0.8, 0.3, 0.8, 0.0);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 0.6F);
        AbilityService.feedback(player, sleepers.size() + (sleepers.size() == 1 ? " creature sleeps." : " creatures sleep."),
                ChatFormatting.LIGHT_PURPLE);
        return Activation.USED;
    }
}
