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
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Seer, Awakened (support): for a minute, players near you see your
 * Forewarning flashes too, and every hostile creature near you glows for a
 * while, so the group fights with your eyes. Alone, the glow still helps.
 */
public final class SharedSightAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SHARED_SIGHT.abilityId();
    static final int ESSENCE_COST = 40;
    static final int DURATION_TICKS = 20 * 60;
    static final double SHARE_RANGE = 32.0;
    static final double GLOW_RANGE = 24.0;
    static final int GLOW_TICKS = 20 * 10;

    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SHARED_SIGHT.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ACTIVE_UNTIL.put(player.getUUID(), level.getGameTime() + DURATION_TICKS);
        for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(GLOW_RANGE),
                AbilityTargets::isHostile)) {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_TICKS, 0, false, false));
        }
        int allies = 0;
        for (ServerPlayer ally : level.players()) {
            if (ally != player && !ally.isSpectator() && ally.distanceToSqr(player) <= SHARE_RANGE * SHARE_RANGE) {
                allies++;
                AbilityService.feedback(ally, player.getScoreboardName() + " shares their sight with you.", ChatFormatting.LIGHT_PURPLE);
                AbilityTargets.beam(level, ParticleTypes.END_ROD, player.getEyePosition(), ally.getEyePosition());
            }
        }
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getEyeY() + 0.3, player.getZ(), 30, 1.2, 0.4, 1.2, 0.01);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.7F, 1.6F);
        if (allies > 0) {
            AbilityService.feedback(player, "Your sight is shared with " + allies + (allies == 1 ? " ally." : " allies."),
                    ChatFormatting.LIGHT_PURPLE);
        }
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        if (until != null && until <= player.serverLevel().getGameTime()) {
            ACTIVE_UNTIL.remove(player.getUUID());
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        ACTIVE_UNTIL.remove(player.getUUID());
    }

    static boolean active(ServerPlayer player) {
        Long until = ACTIVE_UNTIL.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime();
    }
}
