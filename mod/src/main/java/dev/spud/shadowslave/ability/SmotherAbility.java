package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.ability.combat.AbilityCombat;
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
 * Mist, Ascended (force): mist pours into the creature you look at. For five
 * seconds it chokes: it is blinded, crawls, hits weakly and is hurt each second.
 */
public final class SmotherAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.SMOTHER.abilityId();
    static final int ESSENCE_COST = 45;
    static final double REACH = 16.0;
    static final int DURATION_TICKS = 20 * 5;
    static final float DAMAGE_PER_SECOND = 2.0F;

    private record Choke(UUID target, long until) {
    }

    private static final Map<UUID, Choke> CHOKING = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.SMOTHER.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        LivingEntity target = AbilityTargets.lookedAt(player, REACH, AbilityTargets::isHostile);
        if (target == null) {
            AbilityService.feedback(player, "Look at a hostile creature to smother it.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        CHOKING.put(player.getUUID(), new Choke(target.getUUID(), level.getGameTime() + DURATION_TICKS));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DURATION_TICKS, 0));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DURATION_TICKS, 2));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DURATION_TICKS, 1));
        AbilityTargets.beam(level, ParticleTypes.CLOUD, player.getEyePosition(), target.getEyePosition());
        level.playSound(null, target.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.4F, 1.6F);
        return Activation.USED;
    }

    @Override
    public void tick(ServerPlayer player) {
        Choke choke = CHOKING.get(player.getUUID());
        if (choke == null) {
            return;
        }
        ServerLevel level = player.serverLevel();
        if (!(level.getEntity(choke.target()) instanceof LivingEntity target) || !target.isAlive()
                || choke.until() <= level.getGameTime()) {
            CHOKING.remove(player.getUUID());
            return;
        }
        level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getEyeY(), target.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
        if (level.getGameTime() % 20 == 0) {
            AbilityCombat.get().damage(player, target, DAMAGE_PER_SECOND);
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        CHOKING.remove(player.getUUID());
    }
}
