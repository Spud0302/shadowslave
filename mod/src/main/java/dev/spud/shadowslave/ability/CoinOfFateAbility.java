package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Fortune's Dormant ability: flip the coin. Your next hit within ten seconds
 * lands for double damage on heads and half on tails.
 */
public final class CoinOfFateAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.FORTUNE.abilityId();
    static final int ESSENCE_COST = 20;
    static final int ARMED_TICKS = 20 * 10;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.FORTUNE.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (FortuneLuck.coinArmed(player)) {
            AbilityService.feedback(player, "The coin is already in the air.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        FortuneLuck.armCoin(player, ARMED_TICKS);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.WAX_OFF, player.getX(), player.getY() + 2.1, player.getZ(), 8, 0.1, 0.2, 0.1, 0.02);
        level.playSound(null, player.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 0.8F, 1.8F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        if (FortuneLuck.coinArmed(player) && player.serverLevel().getGameTime() % 10 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.WAX_OFF, player.getX(), player.getY() + 2.1, player.getZ(),
                    1, 0.05, 0.05, 0.05, 0.0);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        FortuneLuck.dropCoin(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return FortuneLuck.coinArmed(player);
    }
}
