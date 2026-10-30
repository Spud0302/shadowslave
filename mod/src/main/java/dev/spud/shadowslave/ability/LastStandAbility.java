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
 * Unbroken, Dormant: brace yourself. The next blow that would kill you leaves
 * you on one heart instead. Once it has saved you, it can't be braced again
 * for three minutes.
 */
public final class LastStandAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.UNBROKEN.abilityId();
    static final int ESSENCE_COST = 30;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.UNBROKEN.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (UnbrokenWill.braced(player)) {
            AbilityService.feedback(player, "You are already braced.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        long resting = UnbrokenWill.resting(player);
        if (resting > 0) {
            AbilityService.feedback(player, "Your will is still mending (" + (resting + 19) / 20 + " s).", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        UnbrokenWill.brace(player);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.4, 0.7, 0.4, 0.2);
        level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        if (UnbrokenWill.braced(player) && player.tickCount % 20 == 0) {
            player.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 0.1, player.getZ(),
                    4, 0.4, 0.0, 0.4, 0.0);
        }
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return UnbrokenWill.braced(player);
    }
}
