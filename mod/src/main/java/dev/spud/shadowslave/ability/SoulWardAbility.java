package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/** Wellspring, Awakened (shelter): an essence shield of four hearts on the ally you look at, or on yourself. */
public final class SoulWardAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SOUL_WARD.abilityId();
    static final int ESSENCE_COST = 35;
    static final double REACH = 12.0;
    static final int WARD_TICKS = 20 * 30;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SOUL_WARD.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = AbilityTargets.allyOrSelf(player, REACH);
        target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, WARD_TICKS, 1, false, true));
        ServerLevel level = player.serverLevel();
        if (target != player) {
            AbilityTargets.beam(level, ParticleTypes.WAX_ON, player.getEyePosition(), target.position().add(0, target.getBbHeight() * 0.6, 0));
        }
        for (int ring = 0; ring < 3; ring++) {
            double y = target.getY() + 0.3 + ring * target.getBbHeight() * 0.35;
            for (int i = 0; i < 16; i++) {
                double angle = Mth.TWO_PI * i / 16 + ring * 0.3;
                level.sendParticles(ParticleTypes.WAX_ON, target.getX() + Math.cos(angle) * 0.8, y,
                        target.getZ() + Math.sin(angle) * 0.8, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        level.playSound(null, target.blockPosition(), SoundEvents.ARMOR_EQUIP_GOLD.value(), SoundSource.PLAYERS, 1.0F, 1.2F);
        level.playSound(null, target.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.5F, 1.6F);
        return Activation.USED;
    }
}
