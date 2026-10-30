package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

/**
 * Cantor, Awakened (support): speak the name of the ally you look at. Every
 * ailment on them is cleared and they run faster for eight seconds.
 */
public final class RallyingNameAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.RALLYING_NAME.abilityId();
    static final int ESSENCE_COST = 30;
    static final double REACH = 24.0;
    static final int SPEED_TICKS = 20 * 8;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.RALLYING_NAME.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity ally = AbilityTargets.lookedAt(player, REACH,
                entity -> entity != player && CarryTheFlameAbility.isAlly(player, entity));
        if (ally == null) {
            AbilityService.feedback(player, "Look at an ally to call by name.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        cleanse(ally);
        ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, SPEED_TICKS, 1, false, true, true));
        ServerLevel level = player.serverLevel();
        AbilityTargets.beam(level, ParticleTypes.NOTE, player.getEyePosition(), ally.position().add(0, ally.getBbHeight() * 0.6, 0));
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, ally.getX(), ally.getY() + 1.0, ally.getZ(), 15, 0.4, 0.6, 0.4, 0.0);
        level.playSound(null, ally.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0F, 1.5F);
        if (ally instanceof ServerPlayer named) {
            named.displayClientMessage(Component.literal(player.getGameProfile().getName() + " calls your name.")
                    .withStyle(ChatFormatting.AQUA), true);
        }
        return Activation.USED;
    }

    /** Takes every harmful effect off. */
    static void cleanse(LivingEntity ally) {
        List<Holder<MobEffect>> harmful = ally.getActiveEffects().stream().map(MobEffectInstance::getEffect)
                .filter(effect -> effect.value().getCategory() == MobEffectCategory.HARMFUL).toList();
        harmful.forEach(ally::removeEffect);
        ally.clearFire();
    }
}
