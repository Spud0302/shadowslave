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
 * Scribe, Dormant: write a rune on the ground where you stand. For ten seconds
 * no hostile creature of your rank or lower can cross into it; those inside
 * are pushed out to its edge.
 */
public final class WardScriptAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.SCRIBE.abilityId();
    static final int ESSENCE_COST = 30;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.SCRIBE.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ScribeLore.write(player);
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 0.2, player.getZ(), 60, 1.5, 0.1, 1.5, 0.5);
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 0.8F);
        AbilityService.feedback(player, "Ward Script: nothing of your rank or lower may cross it for "
                + ScribeLore.WARD_TICKS / 20 + " s.", ChatFormatting.GOLD);
        return Activation.USED;
    }

    @Override
    public void clear(ServerPlayer player) {
        ScribeLore.forget(player.getUUID());
    }
}
