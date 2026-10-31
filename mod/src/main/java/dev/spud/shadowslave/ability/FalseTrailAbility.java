package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Saboteur, Awakened: for a minute your steps leave no path on anyone's map,
 * and a false trail runs off where you look. Creatures hunting you follow it.
 */
public final class FalseTrailAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.FALSE_TRAIL.abilityId();
    static final int ESSENCE_COST = 30;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.FALSE_TRAIL.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (SaboteurCraft.hidden(player)) {
            AbilityService.feedback(player, "Your false trail is still fresh.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        SaboteurCraft.layTrail(player);
        player.serverLevel().sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.5, player.getZ(), 20, 0.4, 0.4, 0.4, 0.02);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.SAND_BREAK, SoundSource.PLAYERS, 1.0F, 0.7F);
        AbilityService.feedback(player, "Your way is hidden, and a false one runs off ahead.", ChatFormatting.GOLD);
        return Activation.USED;
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return SaboteurCraft.hidden(player);
    }
}
