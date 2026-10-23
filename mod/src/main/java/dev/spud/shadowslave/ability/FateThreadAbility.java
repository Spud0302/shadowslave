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
 * Seer, Awakened (escape): snap back along your thread to where you stood
 * three seconds ago. The thread is a short trail of where you have been,
 * sampled every few ticks while you hold the ability.
 */
public final class FateThreadAbility implements AspectAbility {
    public static final ResourceLocation ID = KitLadder.FATE_THREAD.abilityId();
    static final int ESSENCE_COST = 35;
    static final int REWIND_TICKS = 20 * 3;
    static final int SAMPLE_TICKS = 5;

    record Point(long time, ResourceKey<Level> dimension, Vec3 position) {
    }

    private static final Map<UUID, Deque<Point>> TRAILS = new ConcurrentHashMap<>();

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return KitLadder.FATE_THREAD.name();
    }

    @Override
    public int essenceCost() {
        return ESSENCE_COST;
    }

    @Override
    public Activation activate(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Point back = pointBefore(TRAILS.get(player.getUUID()), level.getGameTime() - REWIND_TICKS);
        if (back == null || !back.dimension().equals(level.dimension())) {
            AbilityService.feedback(player, "Your thread has not been spun yet.", ChatFormatting.GRAY);
            return Activation.FAILED;
        }
        Vec3 from = player.position();
        AbilityTargets.beam(level, ParticleTypes.END_ROD, from.add(0, 1.0, 0), back.position().add(0, 1.0, 0));
        player.teleportTo(back.position().x, back.position().y, back.position().z);
        player.resetFallDistance();
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, back.position().x, back.position().y + 1.0, back.position().z,
                30, 0.3, 0.5, 0.3, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6F, 1.6F);
        TRAILS.remove(player.getUUID());
        return Activation.USED;
    }

    /** The newest point at or before {@code time}, or the oldest one if the trail is younger than that. */
    static Point pointBefore(Deque<Point> trail, long time) {
        if (trail == null || trail.isEmpty()) {
            return null;
        }
        Point best = trail.peekFirst();
        for (Point point : trail) {
            if (point.time() <= time) {
                best = point;
            }
        }
        return best;
    }

    @Override
    public void tick(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        if (level.getGameTime() % SAMPLE_TICKS != 0) {
            return;
        }
        Deque<Point> trail = TRAILS.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());
        trail.addLast(new Point(level.getGameTime(), level.dimension(), player.position()));
        while (trail.size() > REWIND_TICKS / SAMPLE_TICKS + 2) {
            trail.removeFirst();
        }
    }

    @Override
    public void clear(ServerPlayer player) {
        TRAILS.remove(player.getUUID());
    }
}
