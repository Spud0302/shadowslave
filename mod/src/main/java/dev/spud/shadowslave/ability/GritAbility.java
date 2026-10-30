package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Unbroken, Awakened (defense): plant your feet for ten seconds. Each blow you
 * take where you stand turns aside a tenth more of the next, up to half.
 * Stepping away ends it.
 */
public final class GritAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.GRIT.abilityId();
    static final int ESSENCE_COST = 30;
    static final int PLANT_TICKS = 20 * 10;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.GRIT.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (UnbrokenWill.planted(player)) {
            AbilityService.feedback(player, "Your feet are already planted.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        UnbrokenWill.plant(player, PLANT_TICKS);
        ServerLevel level = player.serverLevel();
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(player.blockPosition().below())),
                player.getX(), player.getY() + 0.1, player.getZ(), 40, 0.6, 0.05, 0.6, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.6F, 0.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        int stacks = UnbrokenWill.gritStacks(player);
        if (UnbrokenWill.planted(player) && player.tickCount % 10 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 0.1, player.getZ(),
                    2 + stacks * 2, 0.5, 0.0, 0.5, 0.0);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        UnbrokenWill.unplant(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return UnbrokenWill.planted(player);
    }
}
