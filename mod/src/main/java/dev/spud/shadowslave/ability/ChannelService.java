package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.soul.SoulRank;
import dev.spud.shadowslave.soul.SoulService;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Conscious channeling (DESIGN): from Awakened on, a player can push raw
 * essence through their body without an ability. The channel key bursts them
 * forward and up; with sneak held it gathers essence into the next melee blow.
 */
public final class ChannelService {
    public static final int LEAP_COST = 20;
    public static final int STRIKE_COST = 25;
    static final double LEAP_FORWARD = 1.1;
    static final double LEAP_UP = 0.75;
    /** Blocks of fall a leap forgives, roughly its own height. */
    static final float LEAP_FALL_FORGIVEN = 6.0F;
    static final int LEAP_GRACE_TICKS = 60;
    static final float STRIKE_MULTIPLIER = 1.75F;
    static final int STRIKE_WINDOW_TICKS = 20 * 6;

    public enum Outcome {
        LEAPT,
        PRIMED,
        NOT_AWAKENED,
        DRAINED,
        ALREADY_PRIMED
    }

    private static final Map<UUID, Long> STRIKE_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LEAPT_AT = new ConcurrentHashMap<>();

    private ChannelService() {
    }

    public static boolean canChannel(ServerPlayer player) {
        return SoulService.get(player).soulRank().map(rank -> rank.ordinal() >= SoulRank.AWAKENED.ordinal()).orElse(false);
    }

    /** One press of the channel key: leap, or prime a heavy strike while sneaking. */
    public static Outcome channel(ServerPlayer player, boolean strike) {
        if (!canChannel(player)) {
            AbilityService.feedback(player, "Channeling opens once you Awaken.", ChatFormatting.GRAY);
            return Outcome.NOT_AWAKENED;
        }
        if (strike && strikePrimed(player)) {
            return Outcome.ALREADY_PRIMED;
        }
        int cost = strike ? STRIKE_COST : LEAP_COST;
        if (!EssenceService.covers(player, cost)) {
            AbilityService.feedback(player, "Not enough essence to channel.", ChatFormatting.GRAY);
            return Outcome.DRAINED;
        }
        EssenceService.spend(player, cost);
        ServerLevel level = player.serverLevel();
        if (strike) {
            STRIKE_UNTIL.put(player.getUUID(), level.getGameTime() + STRIKE_WINDOW_TICKS);
            level.sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX(), player.getY() + 1.0, player.getZ(),
                    12, 0.4, 0.5, 0.4, 0.1);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.5F);
        } else {
            leap(player);
        }
        AbilityService.syncStatus(player);
        return strike ? Outcome.PRIMED : Outcome.LEAPT;
    }

    private static void leap(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        Vec3 direction = flat.lengthSqr() < 1.0e-4 ? Vec3.ZERO : flat.normalize();
        player.setDeltaMovement(direction.x * LEAP_FORWARD, LEAP_UP, direction.z * LEAP_FORWARD);
        player.hurtMarked = true;
        player.resetFallDistance();
        ServerLevel level = player.serverLevel();
        LEAPT_AT.put(player.getUUID(), level.getGameTime());
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 0.1, player.getZ(),
                16, 0.3, 0.05, 0.3, 0.03);
        level.playSound(null, player.blockPosition(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 0.8F, 1.1F);
    }

    public static boolean strikePrimed(ServerPlayer player) {
        Long until = STRIKE_UNTIL.get(player.getUUID());
        return until != null && until > player.serverLevel().getGameTime();
    }

    /** Drops an expired strike and tells the player it faded. Returns whether the HUD needs an update. */
    static boolean tick(ServerPlayer player) {
        Long until = STRIKE_UNTIL.get(player.getUUID());
        if (until != null && until <= player.serverLevel().getGameTime()) {
            STRIKE_UNTIL.remove(player.getUUID());
            AbilityService.feedback(player, "The gathered essence fades.", ChatFormatting.GRAY);
            return true;
        }
        return false;
    }

    /** A primed strike lands harder and throws the target back. */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof ServerPlayer player)
                || event.getSource().getEntity() != player || !strikePrimed(player)) {
            return;
        }
        STRIKE_UNTIL.remove(player.getUUID());
        LivingEntity target = event.getEntity();
        event.setAmount(event.getAmount() * STRIKE_MULTIPLIER);
        Vec3 away = target.position().subtract(player.position()).multiply(1, 0, 1);
        if (away.lengthSqr() > 1.0e-4) {
            away = away.normalize();
            target.knockback(1.2, -away.x, -away.z);
        }
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                1, 0.0, 0.0, 0.0, 0.0);
        level.playSound(null, target.blockPosition(), SoundEvents.MACE_SMASH_GROUND, SoundSource.PLAYERS, 0.9F, 0.9F);
        AbilityService.syncStatus(player);
    }

    /** A channeled leap forgives about its own height of fall. */
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        Long leapt = LEAPT_AT.get(player.getUUID());
        if (leapt == null) {
            return;
        }
        if (player.serverLevel().getGameTime() - leapt <= LEAP_GRACE_TICKS) {
            event.setDistance(Math.max(0.0F, event.getDistance() - LEAP_FALL_FORGIVEN));
        }
        LEAPT_AT.remove(player.getUUID());
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        STRIKE_UNTIL.remove(event.getEntity().getUUID());
        LEAPT_AT.remove(event.getEntity().getUUID());
    }
}
