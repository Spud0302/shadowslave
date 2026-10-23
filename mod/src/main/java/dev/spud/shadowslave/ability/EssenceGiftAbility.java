package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Wellspring's Dormant ability: pour essence into the ally you look at; the
 * gift grows as it passes, so they get more than you spent. An ally with no
 * soul core to hold essence, a pet, or you yourself when nobody is in sight,
 * is mended instead.
 */
public final class EssenceGiftAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.WELLSPRING.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 12.0;
    static final int GIFT = 40;
    static final float MEND = 6.0F;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.WELLSPRING.abilityName();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = AbilityTargets.allyOrSelf(player, REACH);
        ServerLevel level = player.serverLevel();
        if (target != player && target instanceof ServerPlayer ally && EssenceService.capacity(ally) > 0) {
            EssenceService.gain(ally, GIFT);
            AbilityService.syncStatus(ally);
            AbilityService.feedback(ally, player.getScoreboardName() + " pours essence into you.", ChatFormatting.GOLD);
        } else {
            target.heal(MEND);
        }
        AbilityTargets.beam(level, ParticleTypes.WAX_OFF, player.getEyePosition(), target.position().add(0, target.getBbHeight() * 0.6, 0));
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                10, 0.3, 0.4, 0.3, 0.0);
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.5F);
        return Activation.USED;
    }
}
