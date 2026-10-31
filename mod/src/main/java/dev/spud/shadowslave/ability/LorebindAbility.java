package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Scribe, Awakened: study the creature you look at. For a minute you, and any
 * clanmate within 32 blocks of you, deal a quarter more damage to every
 * creature of its kind.
 */
public final class LorebindAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.LOREBIND.abilityId();
    static final int ESSENCE_COST = 35;
    static final double REACH = 24.0;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.LOREBIND.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity subject = AbilityTargets.lookedAt(player, REACH, entity -> entity != player && !(entity instanceof ServerPlayer));
        if (subject == null) {
            AbilityService.feedback(player, "Look at the creature you want to study.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ScribeLore.study(player, subject.getType());
        ServerLevel level = player.serverLevel();
        AbilityTargets.beam(level, ParticleTypes.ENCHANT, player.getEyePosition(), subject.position().add(0, subject.getBbHeight() * 0.6, 0));
        level.sendParticles(ParticleTypes.ENCHANT, subject.getX(), subject.getY() + subject.getBbHeight(), subject.getZ(), 30, 0.4, 0.4, 0.4, 0.4);
        level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0F, 0.7F);
        AbilityService.feedback(player, "Lorebound: " + subject.getType().getDescription().getString()
                + ". You and your clan strike its kind harder for " + ScribeLore.LOREBIND_TICKS / 20 + " s.", ChatFormatting.GOLD);
        return Activation.USED;
    }

    @Override
    public void clear(ServerPlayer player) {
        ScribeLore.forget(player.getUUID());
    }
}
