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
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Warden, Awakened (movement): dash straight to your marked prey, landing a
 * step short of it, and strike faster for a few seconds. Needs a mark.
 */
public final class RelentlessHuntAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.RELENTLESS_HUNT.abilityId();
    static final int ESSENCE_COST = 30;
    static final double MAX_RANGE = 32.0;
    static final double STOP_SHORT = 1.6;
    static final int HASTE_TICKS = 20 * 5;

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.RELENTLESS_HUNT.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        Optional<LivingEntity> mark = MarkPreyAbility.markOf(player);
        if (mark.isEmpty()) {
            AbilityService.feedback(player, "You have no marked prey to hunt.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        LivingEntity prey = mark.get();
        if (prey.distanceTo(player) > MAX_RANGE) {
            AbilityService.feedback(player, "Your prey is too far away.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Vec3 landing = landingSpot(player, prey);
        if (landing == null) {
            AbilityService.feedback(player, "No room to reach your prey.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        ServerLevel level = player.serverLevel();
        Vec3 from = player.position();
        AbilityTargets.beam(level, ParticleTypes.SWEEP_ATTACK, from.add(0, 1.0, 0), landing.add(0, 1.0, 0));
        player.teleportTo(landing.x, landing.y, landing.z);
        player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, prey.getEyePosition());
        player.resetFallDistance();
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, HASTE_TICKS, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, HASTE_TICKS, 0, false, true));
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 0.7F);
        level.playSound(null, player.blockPosition(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 0.8F, 0.8F);
        return Activation.USED;
    }

    /** A free spot a step short of the prey on the line from the player; null if there is none. */
    static Vec3 landingSpot(ServerPlayer player, LivingEntity prey) {
        Vec3 toPlayer = player.position().subtract(prey.position()).multiply(1, 0, 1);
        Vec3 direction = toPlayer.lengthSqr() < 1.0e-4 ? new Vec3(1, 0, 0) : toPlayer.normalize();
        double reach = STOP_SHORT + prey.getBbWidth() / 2;
        for (double back = reach; back <= reach + 2.0; back += 0.5) {
            Vec3 spot = prey.position().add(direction.scale(back));
            if (player.level().noCollision(player, player.getBoundingBox().move(spot.subtract(player.position())))) {
                return spot;
            }
        }
        return null;
    }
}
