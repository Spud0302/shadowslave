package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * Herald, Awakened (support): swear an oath to the ally you look at (another
 * player or your pet). For twenty seconds half of every blow that falls on
 * them falls on you instead.
 */
public final class OathOfArmsAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.OATH_OF_ARMS.abilityId();
    static final int ESSENCE_COST = 35;
    static final double REACH = 16.0;
    static final int OATH_TICKS = 20 * 20;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.OATH_OF_ARMS.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        if (HeraldCommand.oath(player).isPresent()) {
            AbilityService.feedback(player, "Your oath still binds you.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        LivingEntity ally = AbilityTargets.lookedAt(player, REACH,
                entity -> entity != player && CarryTheFlameAbility.isAlly(player, entity));
        if (ally == null) {
            AbilityService.feedback(player, "Look at an ally to swear your oath to.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        HeraldCommand.swear(player, ally, OATH_TICKS);
        ServerLevel level = player.serverLevel();
        AbilityTargets.beam(level, ParticleTypes.ENCHANTED_HIT, player.getEyePosition(), ally.position().add(0, ally.getBbHeight() * 0.6, 0));
        level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.PLAYERS, 1.0F, 0.8F);
        if (ally instanceof ServerPlayer sworn) {
            sworn.displayClientMessage(Component.literal(player.getGameProfile().getName()
                    + " swears to share your wounds.").withStyle(ChatFormatting.GOLD), true);
        }
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Optional<HeraldCommand.Oath> oath = HeraldCommand.oath(player);
        if (oath.isEmpty() || player.serverLevel().getGameTime() % 10 != 0) {
            return;
        }
        Entity ally = player.serverLevel().getEntity(oath.get().ally());
        if (ally != null && ally.distanceToSqr(player) < 48 * 48) {
            AbilityTargets.beam(player.serverLevel(), ParticleTypes.ENCHANTED_HIT, player.position().add(0, 1.0, 0),
                    ally.position().add(0, ally.getBbHeight() * 0.6, 0));
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        HeraldCommand.release(player.getUUID());
    }

    @Override
    public boolean primed(ServerPlayer player) {
        return HeraldCommand.oath(player).isPresent();
    }
}
