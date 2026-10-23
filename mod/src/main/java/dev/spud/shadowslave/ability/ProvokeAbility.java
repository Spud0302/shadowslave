package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.AspectKitCatalog;
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
 * Fury's Dormant ability: a roar. Every hostile within {@link #RADIUS} blocks
 * turns on you, you brace (Resistance I) and the rage rises in you.
 */
public final class ProvokeAbility implements AspectAbility {
    public static final ResourceLocation ID = AspectKitCatalog.FURY.abilityId();
    static final int ESSENCE_COST = 20;
    static final double RADIUS = 12.0;
    static final float RAGE = 20.0F;
    static final int BRACE_TICKS = 20 * 6;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return AspectKitCatalog.FURY.abilityName();
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
            AbilityService.feedback(player, "There is nothing here to provoke.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        for (Mob foe : foes) {
            foe.setTarget(player);
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, foe.getX(), foe.getEyeY() + 0.4, foe.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BRACE_TICKS, 0, false, true, true));
        FuryRage.add(player, RAGE);
        level.sendParticles(ParticleTypes.SONIC_BOOM, player.getX(), player.getEyeY(), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, player.blockPosition(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.0F, 1.0F);
        return Activation.USED;
    }
}
