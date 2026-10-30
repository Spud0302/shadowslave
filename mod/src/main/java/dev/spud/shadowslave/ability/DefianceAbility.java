package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;

import java.util.List;

/**
 * Unbroken, Awakened (command): dare every hostile creature within sixteen
 * blocks. They turn on you and cannot turn away for eight seconds, and you
 * brace against them.
 */
public final class DefianceAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.DEFIANCE.abilityId();
    static final int ESSENCE_COST = 30;
    static final double RADIUS = 16.0;
    static final int DARE_TICKS = 20 * 8;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.DEFIANCE.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<Mob> foes = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(RADIUS), AbilityTargets::isHostile);
        if (foes.isEmpty()) {
            AbilityService.feedback(player, "There is nothing here to defy.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        for (Mob foe : foes) {
            UnbrokenWill.dare(foe, player, DARE_TICKS);
            AbilityTargets.beam(level, ParticleTypes.CRIMSON_SPORE, foe.getEyePosition(), player.getEyePosition());
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, foe.getX(), foe.getEyeY() + 0.4, foe.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DARE_TICKS, 0, false, true, true));
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.6, 0.8, 0.6, 0.3);
        level.playSound(null, player.blockPosition(), SoundEvents.IRON_GOLEM_REPAIR, SoundSource.PLAYERS, 1.0F, 0.6F);
        return Activation.USED;
    }
}
