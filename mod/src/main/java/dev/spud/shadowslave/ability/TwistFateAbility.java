package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Fortune, Awakened (defense): for twenty seconds the next attack aimed at you
 * has an even chance to miss. Hit or miss, that one attack spends the ward.
 */
public final class TwistFateAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.TWIST_FATE.abilityId();
    static final int ESSENCE_COST = 35;
    static final int WARD_TICKS = 20 * 20;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.TWIST_FATE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (FortuneLuck.twisted(player)) {
            AbilityService.feedback(player, "Fate is already twisted around you.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        FortuneLuck.twist(player, WARD_TICKS);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.8, 0.5, 0.6);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.PLAYERS, 1.0F, 1.4F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        if (FortuneLuck.twisted(player) && player.serverLevel().getGameTime() % 5 == 0) {
            double angle = player.serverLevel().getGameTime() * 0.3;
            player.serverLevel().sendParticles(ParticleTypes.ENCHANT, player.getX() + Math.cos(angle) * 0.8, player.getY() + 1.0,
                    player.getZ() + Math.sin(angle) * 0.8, 2, 0.0, 0.1, 0.0, 0.0);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        FortuneLuck.untwist(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return FortuneLuck.twisted(player);
    }
}
