package dev.spud.shadowslave.ability;

import dev.spud.shadowslave.awakening.KitLadder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hourglass, Awakened (escape): snap back to where you stood three seconds
 * ago, and to the health you had then if it was more than you have now.
 */
public final class RewindAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.REWIND.abilityId();
    static final int ESSENCE_COST = 35;
    static final int REWIND_TICKS = 20 * 3;
    static final int SAMPLE_TICKS = 5;

    record Moment(long time, ResourceKey<Level> dimension, Vec3 position, float health) {
    }

    private static final Map<UUID, Deque<Moment>> PASTS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.REWIND.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Moment back = momentBefore(PASTS.get(player.getUUID()), level.getGameTime() - REWIND_TICKS);
        if (back == null || !back.dimension().equals(level.dimension())) {
            AbilityService.feedback(player, "There is no past here to return to yet.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        AbilityTargets.beam(level, ParticleTypes.ENCHANT, player.position().add(0, 1.0, 0), back.position().add(0, 1.0, 0));
        player.teleportTo(back.position().x, back.position().y, back.position().z);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        if (back.health() > player.getHealth()) {
            player.setHealth(back.health());
        }
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, back.position().x, back.position().y + 1.0, back.position().z,
                30, 0.3, 0.5, 0.3, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8F, 0.6F);
        PASTS.remove(player.getUUID());
        return Activation.USED;
    }

    /** The newest moment at or before {@code time}, or the oldest one if the past is younger than that. */
    static Moment momentBefore(Deque<Moment> past, long time) {
        if (past == null || past.isEmpty()) {
            return null;
        }
        Moment best = past.peekFirst();
        for (Moment moment : past) {
            if (moment.time() <= time) {
                best = moment;
            }
        }
        return best;
    }

    @Override
    public void tick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (level.getGameTime() % SAMPLE_TICKS == 0) {
            remember(player);
        }
    }

    static void remember(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Deque<Moment> past = PASTS.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());
        past.addLast(new Moment(level.getGameTime(), level.dimension(), player.position(), player.getHealth()));
        while (past.size() > REWIND_TICKS / SAMPLE_TICKS + 2) {
            past.removeFirst();
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        PASTS.remove(player.getUUID());
    }
}
